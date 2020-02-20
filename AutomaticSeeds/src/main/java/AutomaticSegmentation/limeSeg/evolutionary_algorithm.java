package AutomaticSegmentation.limeSeg;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.bridj.cpp.std.list;
import org.python.modules.math;

import eu.kiaru.limeseg.LimeSeg;

public class evolutionary_algorithm {
	//"C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM"
	public File dir;

	public static void main(String[] args) {
		
	}
	
	public void MutationFunction(Map<Double,Integer> stdsWithResults,Integer nPoblacion) {
		Double stdmin1=(Double) stdsWithResults.values().toArray()[0];
		Double stdmin2=(Double) stdsWithResults.values().toArray()[1];
		
		Integer[]resultadosMin=null;
		resultadosMin[0]=stdsWithResults.get(stdmin1);
		resultadosMin[1]=stdsWithResults.get(stdmin2);
		
		float min_fp=-0.03f; // variable con el valor de la presion [-0.03..0.03].
		float min_d0=1;//d_0: 1 and >20 pixels.
		float min_range_d0=0.5f;// from 0.5 to >10
		
		//factores por lo que se van multiplicando y sumando los valores d_0 y demás de cada poblacion
		float factor_fp=(float) (0.06/nPoblacion);
		float factor_d0=(float) (19/nPoblacion);
		float factor_rangeD0= (float) (9.5f/nPoblacion);
		
		float fp=(float)(min_fp+(factor_fp*resultadosMin[0]));
		float d0=(float)(min_d0+(factor_d0*resultadosMin[0]));
		float range_d0=(float)(min_range_d0+(factor_rangeD0*resultadosMin[0]));
		
		
		
		
	}
	
	
	public Map<Double,Integer> EvaluationFunction() {
		
		//mapa que relaciona la desviacion tipica con su segmentacion
		 Map<Double,Integer> stdsWithResults= new TreeMap<Double,Integer>();//TreeMap porque va ordenando las claves de menor a mayor 
			
		//ArrayList<Double> stds = new ArrayList<Double>();//standard deviations
       	File resdir =new File(dir.toString()+"\\resultados");// path de los resultados
   
       	File[] listOfResults = resdir.listFiles();
       	System.out.println(listOfResults.length);
       	//
       	for(File result: listOfResults) {
       	
       		File[] listOfCells=result.listFiles();
       	
       		ArrayList<Integer> listOfElements = new ArrayList<Integer>();
       	
	       	int i;
	       	
		       	//la máxima iteracion es length-1 porque el ultimo elemento es el limesegparams que no nos interesa
		       	for(i=0;i<(listOfCells.length-1);i++) {
		       		
			       		File ruta= new File(listOfCells[i].toString()+"\\T_1.ply");
			       		System.out.println(ruta.toString());
			       		
			       	   try {
						Scanner in = new Scanner(new FileReader(ruta.toString()));
						String numberOfVertex="";
						
						//el numero de elementos siempre va antes que la primera propiedad, por tanto si la siguiente linea es property no debe entrar ya que tenemos el numero
						while(in.hasNext("property")==false) {
						   numberOfVertex =in.next().toString();
						   System.out.println(numberOfVertex);
						}
						in.close();
						
						System.out.println(numberOfVertex);
						
						listOfElements.add(Integer.parseInt(numberOfVertex));
									
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
		       	}
		       	
		       	//hacer desviacion estandar de lisfOfElements y añadirla en el map de tal forma que sepamos que solución le corresponde
		       	stdsWithResults.put(calculaSTD(listOfElements),i);
		    	
       	}
       	//y cuando acabe pues devolver ese map
		return stdsWithResults; 
       
	}
	
	
	public void PopulationGenerator(Integer nPoblacion) {
		
		//valores mínimos:
		float ZS=4.06f;// variable con el valor del z_scale
		float min_fp=-0.03f; // variable con el valor de la presion [-0.03..0.03].
		float min_d0=1;//d_0: 1 and >20 pixels.
		float min_range_d0=0.5f;// from 0.5 to >10
		//0.2/5=0.004 asi aumento la diferencia por cada iteracion
		
		//factores por lo que se van multiplicando y sumando los valores d_0 y demás de cada poblacion
		float factor_fp=(float) (0.06/nPoblacion);
		float factor_d0=(float) (19/nPoblacion);
		float factor_rangeD0= (float) (9.5f/nPoblacion);
		//IntStream.iterate(start, i -> i + 1).limit(limit).boxed().collect(Collectors.toList());
		
		int i;
		
		for(i=0;i<=nPoblacion;i++) {
			
			float fp=(float)(min_fp+(factor_fp*i));
			float d0=(float)(min_d0+(factor_d0*i));
			float range_d0=(float)(min_range_d0+(factor_rangeD0*i));
			
			//llamo a la clase que va a llamar limeseg:
			SphereSegAdapted seg=new SphereSegAdapted();
			seg.set_path(dir.toString());
			seg.setD_0(d0);
			seg.setF_pressure(fp);
			seg.setZ_scale(ZS);
			seg.setRange_in_d0_units(range_d0);
			seg.start();//empieza a ejecutarse run del hilo de limeseg
			
			long startTime = System.currentTimeMillis();
			long endTime=0;
			
			boolean cond=true;
			
			while (seg.isAlive() && cond==true) {
				endTime= System.currentTimeMillis();
				System.out.println((endTime-startTime) /1000);
				
				if( ((endTime-startTime) /1000) >100) { //si el tiempo de ejecucion es mayor que 100 segundos
					cond=false;
					LimeSeg.stopOptimisation();
				}

			}
		
			System.out.println("Ha salido del while");
	       	File dirNuevo= new File(dir.toString()+"\\resultados\\resultado"+String.valueOf(i));
	       	dirNuevo.mkdir();
	       	LimeSeg.saveStateToXmlPly(dirNuevo.toString());
	       	LimeSeg.clear3DDisplay();
	       	
		}
	}
	
	
	public void setDir(File directorio) {
		//establezco el directorio de trabajo con las imágenes y roi
		//File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM");
		this.dir=directorio;
	}
	
	public ArrayList<String> fitnessEvaluation(ArrayList<Double> stds){
		
		ArrayList<String> mejoresParametros;
		
		return null;
		
	}
	
	
	public Double calculaSTD (ArrayList<Integer> elementosSegmentacion) {

		Double std=null;
		double media=0;
		
		for(Integer elementosCelulaSegmentada:elementosSegmentacion) {
			media+=elementosCelulaSegmentada;
		}
		
		media=media/elementosSegmentacion.size();
		
		for(Integer elementosCelulaSegmentada:elementosSegmentacion) {
			//restamos, elevamos al cuadrado y sumamos
			std+=Math.pow((elementosCelulaSegmentada-media),2);
			
		}
		
		std=math.sqrt( (std/elementosSegmentacion.size()) );
		return std;
		
	}
	

}
