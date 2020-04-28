package AutomaticSegmentation.limeSeg;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.bridj.cpp.std.list;
import org.python.modules.math;

import com.google.common.base.Objects;


import eu.kiaru.limeseg.LimeSeg;

public class evolutionary_algorithm {
	//"C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM"
	public File dir;
	private ArrayList<Individuo> poblacion;
	//private limeseg 
	/**
	 * 
	 */
	public evolutionary_algorithm(File path) {
		super();
		// TODO Auto-generated constructor stub
		this.poblacion=new ArrayList<Individuo>();
		this.dir=path;
	}
	
	
	public void main() {
		
		this.InitialPopulationGenerator(5,0);
		this.FitnessCalculation();
		int i=0;
		generationalChange change=new generationalChange(this.poblacion,10);
		change.main();
		this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generación 0\\resultadoPob0.csv");
		this.deletePopulation();
		ArrayList<Individuo> newPopulation=change.getNextPopulation();
			
		for(i=1;i<200;i++){
			
			this.NewPopulationGenerator(newPopulation, i);
			this.FitnessCalculation();
			generationalChange iterativeChange=new generationalChange(this.poblacion,10);
			iterativeChange.main();
			this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generación "+String.valueOf(i)+"\\resultadoPob"+String.valueOf(i)+".csv");
			this.deletePopulation();
			
			newPopulation=iterativeChange.getNextPopulation();
			
		}
	
		Individuo bestIndividual = Collections.max(this.poblacion, Comparator.comparingDouble(Individuo::getScore));
		System.out.println(bestIndividual.getD0());
		System.out.println(bestIndividual.getRange_d0());
		System.out.println(bestIndividual.getFp());
		System.out.println(bestIndividual.getDir());
		
	}
	
	
	public void InitialPopulationGenerator(Integer nPoblacion,int iter) {
		 //la variable iter se utiliza para generar nombres distintos a los resultados, y saber en que generacion estamos
		//si la generacion es la inicial se inicia con un rango determinado por la poblacion deseada:
		
		//para generar valores aleatorias sería así: int randomInt = (int)(10.0 * Math.random());
		//con math.random generamos valores del 0.0 al 1.0 y eso habría que multiplicarlo por el máximo de los valores de limeseg
			//valores mínimos:
			float ZS=4.06f;// variable con el valor del z_scale
			float min_fp=-0.03f; // variable con el valor de la presion [-0.03..0.03].
			float min_d0=1;//d_0: 1 and >20 pixels.
			float min_range_d0=0.5f;// from 0.5 to >10
			//0.2/5=0.004 asi aumento la diferencia por cada iteracion
			
			//factores por lo que se van multiplicando y sumando los valores d_0 y demás de cada poblacion
			float factor_fp=(float) (0.05/(nPoblacion-1));
			float factor_d0=(float) (18.0f/(nPoblacion-1));//poner 19.0f
			float factor_rangeD0= (float) (8.5f/(nPoblacion-1));
			//IntStream.iterate(start, i -> i + 1).limit(limit).boxed().collect(Collectors.toList());
			
			int i;
			File dirPob=new File(dir.toString()+"\\resultados\\resultado generación 0");
			dirPob.mkdir();
			
			for(i=0;i<=(nPoblacion-1);i++) {
				
				SphereSegAdapted seg=new SphereSegAdapted();
				seg.set_path(dir.toString());
					
					//System.out.println(resultado"+String.valueOf(i)+String.valueOf(iter));
					
					Individuo ind=new Individuo();
					
					ind.setF_pressure((float)(min_fp+(factor_fp*i)) );
					ind.setD0((float)(min_d0+(factor_d0*i)));
					ind.setRange_d0( (float)(min_range_d0+(factor_rangeD0*i)));
					
					//llamo a la clase que va a llamar limeseg:
					try {
					seg.setD_0(ind.getD0());
					seg.setF_pressure(ind.getFp());
					seg.setZ_scale(ZS);
					seg.setRange_in_d0_units(ind.getRange_d0());
					seg.start();
					
					//sls.start();//empieza a ejecutarse la función run del hilo de stopLimeSeg
					long startTime = System.currentTimeMillis();
					long endTime=0;
					
					
					while (seg.isAlive()) {
						endTime= System.currentTimeMillis();
						//System.out.println((endTime-startTime) /1000);
						
						if( ((endTime-startTime) /1000) >30) { //si el tiempo de ejecucion es mayor que 100 segundos
							LimeSeg.requestStopOptimisation=true;
							LimeSeg.stopOptimisation();

							}
						}
	
					System.out.println("Ha salido del while");
					
					//Evolutionary Algorithm is going to wait for sphere seg adapted to finish
					try{
						seg.join();
						System.out.println("Espera");
					}catch(Exception e) {
						System.out.println("No funciona");
					}
					
			}catch(Exception e) {
				System.out.println("Excepcion");
			}
				//ind.setDir(new File(dir.toString()+"\\resultados\\resultado"+String.valueOf(i)+String.valueOf(iter)));
				ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+String.valueOf(iter)));
				ind.getDir().mkdir();//it creates the directory for that individual
		       	LimeSeg.saveStateToXmlPly(ind.getDir().toString());//it saves the solution of the individual
		       	
		       	LimeSeg.clear3DDisplay();
		       	LimeSeg.clearAllCells();
		       	
		       	System.out.println("Ha terminado una iteración del for");
		       	
		       	//seg.interrupt();
		       	poblacion.add(ind);
			}
		
	}
	
	
	public void FitnessCalculation() {
	
			
		//ArrayList<Double> stds = new ArrayList<Double>();//standard deviations
		ArrayList<Double> globalMeanCellObjects= new ArrayList<Double>();
		ArrayList<Double> globalMeanStdObjects= new ArrayList<Double>();
		
		Double mean;
       	Integer j=0;
       	ArrayList<Individuo> individuals= this.getPopulation();
       	for(Individuo res: individuals ) {
       	
       		File[] listOfCells=res.getDir().listFiles();
       	
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
						   //System.out.println(numberOfVertex);
						}
						in.close();
						System.out.println(Integer.parseInt(numberOfVertex));
						listOfElements.add(Integer.parseInt(numberOfVertex));
									
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
		       	}
		       	
		       	mean=(double) (listOfElements.stream().mapToInt(Integer::intValue).sum()/listOfElements.size());
		       	res.setMeanVertex(mean);
		       	globalMeanCellObjects.add(mean);
		       	
		       	Double std=calculaSTD(listOfElements);
		       	res.setStdVertex(std);
		       	globalMeanStdObjects.add(std);
		    	
       	}
	
       	
	
       	Double globalStd=globalMeanStdObjects.stream().mapToDouble(Double::doubleValue).sum()/globalMeanStdObjects.size();
       	Double globalMean=globalMeanCellObjects.stream().mapToDouble(Double::doubleValue).sum()/globalMeanCellObjects.size();
       	
       	
       	for(Individuo res: individuals) {
       		
       		//if the standard deviation of the elements is less than the 25% of the global, it punishes the solution, with 50 points less
       		if(res.getStdVertex()<(0.25*globalStd)){
       			res.setScore(res.getScore()-50.0);
       			
       		//if the standard deviation of the elements is greater than the 25% of the global and less than the 50%, there is no punish
       		}else if((0.25*globalStd)<res.getStdVertex() && res.getStdVertex()<(0.5*globalStd)){
       			res.setScore(res.getScore()-0);
       			
       		//if the standard deviation of the elements is greater than the 50% of the global and less than the 75%, there is a punish with 25 points less
       		}else if((0.5*globalStd)<res.getStdVertex() && res.getStdVertex()<(0.75*globalStd)) {
       			res.setScore(res.getScore()-25);
       			
       		}else {//if the standard deviation of the elements is greater than the 75%,it is punished with 50 points less
       			
       			res.setScore(res.getScore()-50);
       		}
       		
       		//if the mean of the elements is less than the 25% of the global, it punishes the solution, with 50 points less
       		if(res.getMeanVertex()<(0.25*globalMean)){
       			res.setScore(res.getScore()-50.0);
       			
       		//if the mean of the elements is greater than the 25% of the global and less than the 50%, there is no punish
       		}else if((0.25*globalMean)<res.getMeanVertex() && res.getMeanVertex()<(0.5*globalMean)){
       			res.setScore(res.getScore()-0);
       			
       		//if the mean of the elements is greater than the 50% of the global and less than the 75%, there is a punish with 25 points less
       		}else if((0.5*globalMean)<res.getStdVertex() && res.getStdVertex()<(0.75*globalMean)) {
       			res.setScore(res.getScore()-25);
       			
       		}else {//if the mean of the elements is greater than the 75%,it is punished with 50 points less
       			
       			res.setScore(res.getScore()-50);
       		}
       	}
      
       
	}
	
	
	
	public void NewPopulationGenerator(ArrayList<Individuo> newPopulation,int iter) {
		
			//the folder for the new individuals is created

			File dirPob=new File(dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(iter));
			dirPob.mkdir();
			
			this.poblacion=new ArrayList<Individuo>();
			
			//only Zscale has the same value for the new generations:
			float ZS=4.06f;// variable con el valor del z_scale
			int i=0;
			
				for(Individuo ind:newPopulation) {
					
					//llamo a la clase que va a llamar limeseg:
					SphereSegAdapted seg=new SphereSegAdapted();
					seg.set_path(dir.toString());
					seg.setD_0(ind.getD0());
					seg.setF_pressure(ind.getFp());
					seg.setZ_scale(ZS);
					seg.setRange_in_d0_units(ind.getRange_d0());
					
					seg.run();//empieza a ejecutarse run
					
					long startTime = System.currentTimeMillis();
					long endTime=0;
					
					while (seg.isAlive()) {
						endTime= System.currentTimeMillis();
						System.out.println((endTime-startTime) /1000);
						
						if( ((endTime-startTime) /1000) >60) { //si el tiempo de ejecucion es mayor que 100 segundos
							System.out.println("PAM");
							LimeSeg.stopOptimisation();
							}
						}

					System.out.println("Ha salido del while");
					
					//Evolutionary Algorithm is going to wait for sphere seg adapted to finish
					try{
						seg.join();
						System.out.println("Espera");
					}catch(Exception e) {
						System.out.println("No funciona");
					}
				
					System.out.println("Ha salido del while");
					ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+String.valueOf(iter)));
					ind.getDir().mkdir();//it creates the directory for that individual
			       	LimeSeg.saveStateToXmlPly(ind.getDir().toString());//it saves the solution of the individual
			       	LimeSeg.clear3DDisplay();
			       	LimeSeg.clearAllCells();
			       	
			       	poblacion.add(ind);
			       	i++;
			}
	}
	
	
	public Double calculaSTD (ArrayList<Integer> elementosSegmentacion) {

		//System.out.println(elementosSegmentacion.contains(null));
		Double std=0d;
		Double media=0d;
		Double sum = elementosSegmentacion.stream().mapToDouble(a -> a).sum();
		
		for(Integer elementosCelulaSegmentada:elementosSegmentacion) {
			media+=elementosCelulaSegmentada;
		}
		
		System.out.println(media);
		System.out.println(sum);
		
		media=media/elementosSegmentacion.size();
		
		for(Integer elementosCelulaSegmentada:elementosSegmentacion) {
			//restamos, elevamos al cuadrado y sumamos
			std+=Math.pow((elementosCelulaSegmentada-media),2);
			
		}
		
		std=math.sqrt( (std/elementosSegmentacion.size()) );
		return std;
		
	}
	
	
	
	public void setDir(File directorio) {
		//establezco el directorio de trabajo con las imágenes y roi
		//File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM");
		this.dir=directorio;
	}
	
	
	public ArrayList<Individuo> getPopulation(){
	
		return this.poblacion;
	
	}
	
	public void writeResultsCSV(String fileName) {
		
		ArrayList<Individuo> data=this.poblacion;
		
		try
        {
			File file = new File(fileName);
            FileWriter writer = new FileWriter(file);

             writer.append("Directory");
             writer.append(',');
             writer.append("D_0");
             writer.append(',');
             writer.append("Range_D0");
             writer.append(',');
             writer.append("f_pressure");
             writer.append(',');
             writer.append("Score");
             writer.append('\n');

             for (Individuo ind:data) {
                  writer.append(ind.getDir().toString());
                  writer.append(',');
                  writer.append(String.valueOf(ind.getD0()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getRange_d0()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getFp()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getScore()));
                  writer.append('\n');
             }

             writer.flush();
             writer.close();
        } catch(IOException e) {
              e.printStackTrace();
        } 
   }
		
	
	public void deletePopulation() {
		
		this.poblacion=null;
	}
}
