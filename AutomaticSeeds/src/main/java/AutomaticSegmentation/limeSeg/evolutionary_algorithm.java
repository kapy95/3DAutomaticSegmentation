package AutomaticSegmentation.limeSeg;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.lang.management.MemoryMXBean;

import org.bridj.cpp.std.list;
import org.python.modules.math;

import com.google.common.base.Objects;


import eu.kiaru.limeseg.LimeSeg;

public class evolutionary_algorithm {
	//"C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM"
	public File dir;
	private ArrayList<Individuo> poblacion;
	//private limeseg 
	private String bestDir; 
	private String secondbestDir; 
	/**
	 * 
	 */
	public evolutionary_algorithm(File path) {
		super();
		// TODO Auto-generated constructor stub
		this.poblacion=new ArrayList<Individuo>();
		this.dir=path;
	}
	
	
	public void main(int numIndividuals,int iters) {
		
		String initialhour=String.valueOf(LocalDateTime.now().getHour())+"."+String.valueOf(LocalDateTime.now().getMinute());
		int i=0;
		
		try{
			
		File file1 = new File(this.dir.toString()+"\\resultados\\resultadosGlobales.csv");
        FileWriter writer1 = new FileWriter(file1);
        
        writer1.append("Identifier");
        writer1.append(',');
        writer1.append("D_0");
        writer1.append(',');
        writer1.append("Range_D0");
        writer1.append(',');
        writer1.append("f_pressure");
        writer1.append(',');
        writer1.append("MeanVertex");
        writer1.append(',');
        writer1.append("StdVertex");
        writer1.append(',');
        writer1.append("Score");
        writer1.append(',');
        writer1.append("SelectionMethod");
        writer1.append(',');
        writer1.append("offSpringMethod");
        writer1.append(',');
        writer1.append("Time");
        writer1.append('\n');
        
		
		this.InitialPopulationGenerator(numIndividuals,0);
		System.gc();	
		this.FitnessCalculation();
		this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion0\\resultadoPob0.csv");
		this.writeGlobalResultsCSV(writer1);
		generationalChange change=new generationalChange(this.poblacion,numIndividuals,0,this.dir.toString());
		change.main(i,this.dir.toString());
		ArrayList<Individuo> newPopulation=change.getNextPopulation();
		//getObjectPendingFinalizationCount 
		System.gc();	
		for(i=1;i<iters;i++){//i=200
			
			this.NewPopulationGenerator(newPopulation, i);
			this.FitnessCalculation();
			System.gc();	
			this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(i)+"\\resultadoPob"+String.valueOf(i)+".csv");
			System.gc();	
			this.writeGlobalResultsCSV(writer1);
			generationalChange iterativeChange=new generationalChange(this.poblacion,numIndividuals,i,this.dir.toString());
			System.gc();	
			iterativeChange.main(i,this.dir.toString());
			System.gc();	

			newPopulation=iterativeChange.getNextPopulation();
			
		}
		
		this.NewPopulationGenerator(newPopulation, i);
		this.FitnessCalculation();
		this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(i)+"\\resultadoPob"+String.valueOf(i)+".csv");
		
		writer1.close();
		}catch(IOException e) {
            e.printStackTrace();
        } 
		
		
		Individuo bestIndividual = Collections.max(this.poblacion, Comparator.comparingDouble(Individuo::getScore));
		
		try
        {
			File file = new File(this.dir.toString()+"\\resultados"+"\\mejorResultado"+".csv");
            FileWriter writer = new FileWriter(file);

            writer.append("Directory");
            writer.append(',');
            writer.append("D_0");
            writer.append(',');
            writer.append("Range_D0");
            writer.append(',');
            writer.append("f_pressure");
            writer.append(',');
            writer.append("MeanVertex");
            writer.append(',');
            writer.append("StdVertex");
            writer.append(',');
            writer.append("Score");
            writer.append(',');
            writer.append("TotalTime");
            writer.append(',');
            writer.append("NumIndividuals");
            writer.append(',');
            writer.append("Iterations");
            writer.append('\n');
            
            writer.append(bestIndividual.getDir().toString());
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getD0()));
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getRange_d0()));
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getFp()));
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getMeanVertex()));
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getStdVertex()));
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getScore()));
            writer.append(',');
            String finalhour=String.valueOf(LocalDateTime.now().getHour())+"."+String.valueOf(LocalDateTime.now().getMinute());
            writer.append(String.valueOf(finalhour+"-"+initialhour));
            writer.append(',');
            writer.append(String.valueOf(numIndividuals)); 
            writer.append(',');
            writer.append(String.valueOf(i)); 
            writer.append('\n');
            
            System.out.println(bestIndividual.getD0());
            System.out.println(bestIndividual.getRange_d0());
            System.out.println(bestIndividual.getFp());
            System.out.println(bestIndividual.getDir());
            
            writer.flush();
	        writer.close();
	        
   } catch(IOException e) {
         e.printStackTrace();
   } 
		
		
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
			float factor_d0=(float) (17.0f/(nPoblacion-1));//poner 19.0f
			float factor_rangeD0= (float) (8.5f/(nPoblacion-1));
			
			
			int i;
			File dirPob=new File(dir.toString()+"\\resultados\\resultado generacion0");
			dirPob.mkdir();
			Random rand=new Random();
			
			//Random rand= new Random();
			
			for(i=0;i<=(nPoblacion-1);i++) {
				
				SphereSegAdapted seg=new SphereSegAdapted();
				seg.set_path(dir.toString());
					
					//System.out.println(resultado"+String.valueOf(i)+String.valueOf(iter));
					
					Individuo ind=new Individuo();
					/*
					ind.setF_pressure((float)(min_fp+(factor_fp*i)) );
					ind.setD0((float)(min_d0+(factor_d0*i)));
					ind.setRange_d0( (float)(min_range_d0+(factor_rangeD0*i)));
					*/
					float randomF_pressure=-0.025f + rand.nextFloat() * (0.025f+0.025f);
					float randomD0=1.0f + rand.nextFloat() * (18.0f-1.0f);
					float randomRange_D0=0.5f + rand.nextFloat() * (8.5f-0.5f) ;
			
					ind.setF_pressure(randomF_pressure);
					ind.setD0(randomD0);
					ind.setRange_d0(randomRange_D0);
					
					//llamo a la clase que va a llamar limeseg:
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
						System.out.println((endTime-startTime) /1000);
						
						if( ( (endTime-startTime) /1000) >30) { //si el tiempo de ejecucion es mayor que 100 segundos
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
					

				ind.setTime((endTime-startTime) /1000);
				//ind.setDir(new File(dir.toString()+"\\resultados\\resultado"+String.valueOf(i)+String.valueOf(iter)));
				ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+String.valueOf(iter)));
				ind.setIdentifier("resultado"+String.valueOf(i)+String.valueOf(iter));
				ind.getDir().mkdir();//it creates the directory for that individual
		       	LimeSeg.saveStateToXmlPly(ind.getDir().toString());//it saves the solution of the individual
		       	
		       	LimeSeg.clear3DDisplay();
		       	LimeSeg.clearAllCells();
				seg	= null;
		       	System.out.println("Ha terminado una iteración del for");
		       	System.gc();
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
	
       	
       	/*
       	Double globalStd=globalMeanStdObjects.stream().mapToDouble(Double::doubleValue).sum()/globalMeanStdObjects.size();
       	Double globalMean=globalMeanCellObjects.stream().mapToDouble(Double::doubleValue).sum()/globalMeanCellObjects.size();
       	*/
       	Collections.sort(globalMeanStdObjects);
       	//Double minStd=globalMeanStdObjects.get(0);
       	//Double maxStd=globalMeanStdObjects.get(globalMeanStdObjects.size()-1);
       	
       	Collections.sort(globalMeanCellObjects);
       	//Double minMean=globalMeanCellObjects.get(0);
       	//Double maxMean=globalMeanCellObjects.get(globalMeanCellObjects.size()-1);
       	ArrayList<Individuo> elementsToBeDeleted= new ArrayList<Individuo>();
       	
       	
       	int i=0;
       	for(i=0;i<individuals.size();i++) {
       		Individuo res=individuals.get(i);
       		if(res.getMeanVertex()==0) {
       			//individuals.remove(i);
           		elementsToBeDeleted.add(res);
       		}else if(res.getStdVertex()==0) {
       			//individuals.remove(i);
           		elementsToBeDeleted.add(res);
       		}else {
       			/*
       			System.out.print("Std:");
       			System.out.println((globalStd/res.getStdVertex()));
       			
       			System.out.print("Mean:");
       			System.out.println(res.getMeanVertex()/globalMean);
       			
       			System.out.println((globalStd/res.getStdVertex())*(res.getMeanVertex()/globalMean));
           		res.setScore( (globalStd/res.getStdVertex())*(res.getMeanVertex()/globalMean) );
           		*/
       			
       			/*
       			Double normalizedMeanVertex=(res.getMeanVertex()-minMean)/(maxMean-minMean);
       			Double normalizedStdVertex=1-(res.getStdVertex()-minStd)/(maxStd-minStd);
           		res.setScore(normalizedMeanVertex*normalizedStdVertex);
           		*/
       			
       			res.setScore(res.getMeanVertex());
           		this.poblacion.set(i,res);
       		}
       	
       	}
       	
       	if(elementsToBeDeleted.isEmpty()==false) {
       		
	       	for(Individuo pos: elementsToBeDeleted) {
	       		this.poblacion.remove(pos);
	       	}
       	}
       
	}
	
	
	
	public void NewPopulationGenerator(ArrayList<Individuo> newPopulation,int iter) {
		
			//the folder for the new individuals is created

			File dirPob=new File(dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(iter));
			//dirPob.mkdir();
			
			this.deletePopulation();
			this.poblacion=new ArrayList<Individuo>();
			this.poblacion.add(newPopulation.get(0));
			this.poblacion.add(newPopulation.get(1));
			
			//only Zscale has the same value for the new generations:
			float ZS=4.06f;// variable con el valor del z_scale
			int i=0;
			
			Date date = new Date();   // given date
			Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
			calendar.setTime(date);   // assigns calendar to given date 
			calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
			calendar.get(Calendar.HOUR);        // gets hour in 12h format
			calendar.get(Calendar.MONTH);       // gets month number, NOTE this is zero based!
			try {
				FileWriter writer = new FileWriter(dirPob.toString()+"\\Experimentos.csv");
			
             writer.append("Directory");
             writer.append(',');
             writer.append("D_0");
             writer.append(',');
             writer.append("Range_D0");
             writer.append(',');
             writer.append("f_pressure");
             writer.append(',');
             writer.append("CurrentTime");
             writer.append(',');
             writer.append("MaxUsageRam");
             writer.append('\n');
             
             writer.flush();
             
				for(i=2;i<=newPopulation.size()-1;i++) {
					
					Individuo ind= newPopulation.get(i);
					
					ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+String.valueOf(iter)));
					//llamo a la clase que va a llamar limeseg:
					SphereSegAdapted seg2=new SphereSegAdapted();
					seg2.set_path(dir.toString());
					seg2.setD_0(ind.getD0());
					seg2.setF_pressure(ind.getFp());
					seg2.setZ_scale(ZS);
					seg2.setRange_in_d0_units(ind.getRange_d0());
					
					 writer.append("resultado"+String.valueOf(i)+String.valueOf(iter));
		             writer.append(',');
		             writer.append(String.valueOf(ind.getD0()));
		             writer.append(',');
		             writer.append(String.valueOf(ind.getRange_d0()));
		             writer.append(',');
		             writer.append(String.valueOf(ind.getFp()));
		             writer.append(',');
		             writer.append(String.valueOf(LocalDateTime.now().getHour())+":"+String.valueOf(LocalDateTime.now().getMinute()));
		             writer.append(',');
		             
					long startTime2 = System.currentTimeMillis();
					long endTime2=System.currentTimeMillis();
					System.out.println((startTime2-endTime2)/1000);
					
					seg2.start();
					
					ArrayList<Double> memoryRegisters=new ArrayList<Double>();
					
					
					while (seg2.isAlive()) {
						endTime2=System.currentTimeMillis();
						memoryRegisters.add((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024.0 * 1024.0 * 1024.0));
						
						System.out.println((endTime2-startTime2) /1000);
						
						if( ( (endTime2-startTime2) /1000) >30) { //si el tiempo de ejecucion es mayor que 100 segundos
							LimeSeg.stopOptimisation();

							}
					}
					
					writer.append(String.valueOf(Collections.max(memoryRegisters)));	
		            writer.append('\n');
		            writer.flush();
					
					System.out.println("Ha salido del while");
					
					//Evolutionary Algorithm is going to wait for sphere seg adapted to finish
					try{
						seg2.join();
						System.out.println("Espera");
					}catch(Exception e) {
						System.out.println("No funciona");
					}
				
					System.out.println("Ha salido del Join()");
					ind.setTime((endTime2-startTime2) /1000);
					ind.setIdentifier("resultado"+String.valueOf(i)+String.valueOf(iter));
					ind.getDir().mkdir();//it creates the directory for that individual
			       	LimeSeg.saveStateToXmlPly(ind.getDir().toString());//it saves the solution of the individual
			       	LimeSeg.clear3DDisplay();
			       	LimeSeg.clearAllCells();
			       	
			       	seg2=null;
			       	System.gc();
			       	
			       	poblacion.add(ind);
				}
				
				writer.close();
				
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		System.gc();
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

             writer.append("Identifier");
             writer.append(',');
             writer.append("D_0");
             writer.append(',');
             writer.append("Range_D0");
             writer.append(',');
             writer.append("f_pressure");
             writer.append(',');
             writer.append("MeanVertex");
             writer.append(',');
             writer.append("StdVertex");
             writer.append(',');
             writer.append("Score");
             writer.append(',');
             writer.append("SelectionMethod");
             writer.append(',');
             writer.append("offSpringMethod");
             writer.append(',');
             writer.append("Time");
             writer.append('\n');

             for (Individuo ind:data) {
            	  
                  writer.append(ind.getIdentifier());
                  writer.append(',');
                  writer.append(String.valueOf(ind.getD0()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getRange_d0()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getFp()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getMeanVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getStdVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getScore()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getSelectionMethod()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getOffspringMethod()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getTime()));
                  writer.append('\n');
             }

             writer.flush();
             writer.close();
        } catch(IOException e) {
              e.printStackTrace();
        } 
   }
	
	
	public void writeGlobalResultsCSV(FileWriter writer) {
		
		ArrayList<Individuo> data=this.poblacion;
		try
        {
             for (Individuo ind:data) {
            	  
                  writer.append(ind.getIdentifier());
                  writer.append(',');
                  writer.append(String.valueOf(ind.getD0()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getRange_d0()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getFp()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getMeanVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getStdVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getScore()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getSelectionMethod()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getOffspringMethod()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getTime()));
                  writer.append('\n');
             }

             writer.flush();
             
        } catch(IOException e) {
              e.printStackTrace();
        } 
   }
		
	
	public void deletePopulation() {
		this.poblacion=null;
		System.gc();
	}
	
	public void addIndividual(Individuo i) {
		this.poblacion.add(i);
	}
	
}
