package AutomaticSegmentation.limeSeg;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import java.io.File;
import java.io.IOException;

import org.smurn.jply.Element;
import org.smurn.jply.ElementReader;
import org.smurn.jply.PlyReader;
import org.smurn.jply.PlyReaderFile;

import org.bridj.cpp.std.list;
import org.python.modules.math;

import com.google.common.base.Objects;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import eu.kiaru.limeseg.LimeSeg;
import eu.kiaru.limeseg.struct.Cell;
import eu.kiaru.limeseg.struct.CellT;
import eu.kiaru.limeseg.struct.DotN;
import eu.kiaru.limeseg.struct.Vector3D;
import imageware.ByteBuffer;

public class evolutionary_algorithm {
	//"C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM"
	public File dir;
	private ArrayList<Individuo> poblacion;
	//private limeseg 
	private String bestDir; 
	private String secondbestDir; 
	private int gen;
	private int numberOfCells;
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
        writer1.append("StdVertex");
        writer1.append(',');
        writer1.append("meanVertex");
        writer1.append(',');
        writer1.append("AverageVolume");
        writer1.append(',');
        writer1.append("StdVolume");
        writer1.append(',');
        writer1.append("StdFaces");
        writer1.append(',');
        writer1.append("NotNullCells");
        writer1.append(',');
        writer1.append("Score");
        writer1.append(',');
        writer1.append("SelectionMethod");
        writer1.append(',');
        writer1.append("offSpringMethod");
        writer1.append(',');
        writer1.append("Time");
        writer1.append('\n');
        
		File dir = new File(this.dir.toString()+"\\datos\\RoiSet");
		File[] listOfFiles = dir.listFiles();
		this.setNumberOfCells(listOfFiles.length);
		
		this.InitialPopulationGenerator(200,0);
	
		this.FitnessCalculation();
		this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion0\\resultadoPob0.csv");
		this.writeGlobalResultsCSV(writer1);
		generationalChange change=new generationalChange(this.poblacion,numIndividuals,0,this.dir.toString());
		change.main(i,this.dir.toString());
		ArrayList<Individuo> newPopulation=change.getNextPopulation();
		//getObjectPendingFinalizationCount 
	
		for(i=1;i<iters;i++){//i=200
			this.setGen(i);
			this.NewPopulationGenerator(newPopulation, i);
			this.FitnessCalculation();
			//System.gc();	
			this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(i)+"\\resultadoPob"+String.valueOf(i)+".csv");
			//System.gc();	
			this.writeGlobalResultsCSV(writer1);
			generationalChange iterativeChange=new generationalChange(this.poblacion,numIndividuals,i,this.dir.toString());
			//System.gc();	
			iterativeChange.main(i,this.dir.toString());
			//System.gc();	

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
            writer.append("StdVertex");
            writer.append(',');
            writer.append("AverageVolume");
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
            writer.append(String.valueOf(bestIndividual.getStdVertex()));
            writer.append(',');
            writer.append(String.valueOf(bestIndividual.getAverageVolume()));
            writer.append(',');
            writer.append("VolumeStd");
            writer.append(',');
            writer.append("FacesStd");
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
			float ZS=3.51f;//float ZS=4.06f;// variable con el valor del z_scale
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
					float randomRange_D0=1.0f + rand.nextFloat() * (8.5f-0.5f) ;
			
					ind.setF_pressure(randomF_pressure);
					ind.setD0(randomD0);
					ind.setRange_d0(randomRange_D0);
					
					//llamo a la clase que va a llamar limeseg:
					seg.setD_0(ind.getD0());
					seg.setF_pressure(ind.getFp());
					seg.setZ_scale(ZS);
					seg.setRange_in_d0_units(ind.getRange_d0());
					seg.start();
					
					

					long startTime = System.currentTimeMillis();
					long endTime=0;
					
					ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+"-gen"+String.valueOf(iter)));
					ind.getDir().mkdir();//it creates the directory for that individual

					boolean corte=false;
					while (seg.isAlive() && ((endTime-startTime) /1000)<12) {//12
						
						endTime= System.currentTimeMillis();
						System.out.println((endTime-startTime) /1000);
						
			
						if( ( (endTime-startTime) /1000) >10) { //si el tiempo de ejecucion es mayor que 100 segundos
							LimeSeg.stopOptimisation();
							
							
							//corte=true;
							}
					}
					
					//seg.interrupt();
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
				//ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+String.valueOf(iter)));
				ind.setIdentifier("resultado"+String.valueOf(i)+"-gen"+String.valueOf(iter));

		       	LimeSeg.saveStateToXmlPly(ind.getDir().toString());//it saves the solution of the individual
		       	
		       	LimeSeg.clear3DDisplay();
		       	LimeSeg.clearAllCells();
				seg	= null;
		       	System.out.println("Ha terminado una iteración del for");

		       	//seg.interrupt();
		       	poblacion.add(ind);
			}
		
	}
	
	
	public void FitnessCalculation() {
	
		ArrayList<Double> globalMeanStdObjects= new ArrayList<Double>();
		ArrayList<Double> globalMeanStdFaces= new ArrayList<Double>();
		ArrayList<Double>globalAverageVolumes=new ArrayList<Double>();
		ArrayList<Double>globalMeanVertex=new ArrayList<Double>();
		ArrayList<Double>globalAverageStdVolumes=new ArrayList<Double>();
		ArrayList<Double>globalAverageCentroids=new ArrayList<Double>();
		ArrayList<Integer> globalNumberOfCellsNotNull=new ArrayList<Integer>();
		
		Double mean;
       	Integer j=0;
       	ArrayList<Individuo> individuals= this.getPopulation();
       	for(Individuo res: individuals ) {
       		
       		File[] listOfCells=res.getDir().listFiles();
       		
       		ArrayList<Integer> listOfElements = new ArrayList<Integer>();
    		ArrayList<Integer>listOfFaces=new ArrayList<Integer>();
    		
	       	int i;
   			Integer cells=0;
		       	//la máxima iteracion es length-1 porque el ultimo elemento es el limesegparams que no nos interesa
		       	for(i=0;i<(listOfCells.length-1);i++) {

			       		File ruta= new File(listOfCells[i].toString()+"\\T_1.ply");
			       		System.out.println(ruta.toString());
			     
			       	   try {
						Scanner in = new Scanner(new FileReader(ruta.toString()));
						String numberOfVertex="";
						if(ruta.length()!=0) {
							
						//el numero de elementos siempre va antes que la primera propiedad, por tanto si la siguiente linea es property no debe entrar ya que tenemos el numero
						/*while(in.hasNext("property")==false) {
						   numberOfVertex =in.next().toString();
						   //System.out.println(numberOfVertex);
						}*/

						//si hacen falta las caras:
						String previo="";
						String siguiente="";
						String vertex="vertex";
						String face="face";
						boolean termina=false;
						while(termina==false) {
							System.out.println(in.next());
							if(in.hasNext("vertex")){
								vertex=in.next().toString();
								numberOfVertex=in.next().toString();
								
							}else if(in.next().contains("face")){
								face=in.next().toString();
								termina=true;
							}
							

						}
						

						listOfFaces.add(Integer.parseInt(face));
						
						if(Integer.parseInt(numberOfVertex)!=0) {
							cells++;
						}
						
						}else {
							
							 numberOfVertex="0";
						}
						
						
						in.close();
						System.out.println(Integer.parseInt(numberOfVertex));
						listOfElements.add(Integer.parseInt(numberOfVertex));
						
						
									
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
		       	}
		    	
		       	Double std=0.0;
		       	if(listOfElements.isEmpty()==true) {
			       		mean=0.0d;
			       		std=0.0d;
		       	}else {
				       	mean=(double) (listOfElements.stream().mapToInt(Integer::intValue).sum())/listOfElements.size();
				       	std=calculaSTD(listOfElements);
		       	}
		       	
		       	Double stdVolume=calculaSTD2( getCellVolumes(res.getDir().toString()) );
		       	res.setMeanVertex(mean);
		       	res.setStdVertex(std);
		       	res.setStdVolume(stdVolume);
		       	
		       	mean=(double) (listOfElements.stream().mapToInt(Integer::intValue).sum()/listOfElements.size());
		       	Double averageVolume=null;
		       	Double averageCentroid=null;
		       	
		       	if(mean>0) {
		       		averageVolume=getCellAverageVolumes(res.getDir().toString());
		       		averageCentroid=getCellAverageCentroids(res.getDir().toString());

		       	}else {
		       		averageVolume=0.0;
		       		averageCentroid=0.0;
		       	}

		       	//globalMeanCellObjects.add(mean);
		       	Double stdFaces=calculaSTD(listOfFaces);
		       	res.setStdFaces(stdFaces);
		       	res.setAverageVolume(averageVolume);
		       	res.setNotNullCells(cells);
		       	globalNumberOfCellsNotNull.add(cells);
		       	res.setAverageCentroid(averageCentroid);
		       	
	       		globalAverageVolumes.add(averageVolume);
		       	globalMeanStdFaces.add(stdFaces);
		       	globalMeanVertex.add(mean);
		       	globalMeanStdObjects.add(std);
		       	globalAverageStdVolumes.add(stdVolume);
		       	globalAverageCentroids.add(averageCentroid);

       	}
       	
       
       	
       	/*
       	Collections.sort(globalMeanStdObjects);
       	Double minStd=globalMeanStdObjects.get(0);
       	Double maxStd=globalMeanStdObjects.get(globalMeanStdObjects.size()-1);
       	
       	Collections.sort(globalAverageVolumes);
    	Double minAverageVolume=globalAverageVolumes.get(0);
    	Double maxAverageVolume=globalAverageVolumes.get(globalAverageVolumes.size()-1);
    	*/
       	
       	//sum of all average volumes, this is done in order to normalize the values later
       	
       	Double totalVolumeAverage=(double)globalAverageVolumes.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalStdVolume= globalAverageStdVolumes.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalStdElementAverage=globalMeanStdObjects.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalStdFaces=globalMeanStdFaces.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalMeanVertex=globalMeanVertex.stream().mapToDouble(Double::doubleValue).sum();
       	
       	int minimumOfCells=(int) Math.round((globalNumberOfCellsNotNull.stream().mapToInt(Integer::intValue).sum()/globalNumberOfCellsNotNull.size())/2);
       	//Double stdMedianVertexNormalized=getMedianStd(globalMeanStdObjects)/totalStdElementAverage;
       	Double stdMedianStdVolume=getMedianStd(globalAverageStdVolumes)/totalStdVolume;
       	/*		
    	int percentile=75;
       	Double percentileValue=calcPercentiles(globalAverageStdVolumes,percentile);
       	
       	Double percentileValue2=calcPercentiles(globalAverageStdVolumes,25);
       	
       	Double percentileDif=percentileValue-percentileValue2;
       	*/
       	
       	/*Collections.sort(globalMeanStdFaces);
       	Double minStdFaces=	globalMeanStdFaces.get(0);
       	Double maxStdFaces=	globalMeanStdFaces.get(globalMeanStdObjects.size()-1);*/
       	
       	//Collections.sort(globalMeanCellObjects);
       	//Double minMean=globalMeanCellObjects.get(0);
       	//Double maxMean=globalMeanCellObjects.get(globalMeanCellObjects.size()-1);
       	ArrayList<Individuo> elementsToBeDeleted= new ArrayList<Individuo>();
       	
       	Double globalAverageStdVolume=totalStdVolume/globalAverageStdVolumes.size();
       	
       	
       	int i=0;
       	for(i=0;i<individuals.size();i++) {
       		Individuo res=individuals.get(i);
       		Double score=0.0d;
       		
       		 if(res.getStdVertex()==0) {
       			//individuals.remove(i);
           		elementsToBeDeleted.add(res);
           		
       		 }else if(res.getAverageVolume()<0) {
           		elementsToBeDeleted.add(res);
           	
       		 }else if(res.getNotNullCells()<minimumOfCells){
       				
       			 elementsToBeDeleted.add(res);
       			 res.setStdCondition(true);
       			 Double normalizedVolume=globalAverageVolumes.get(i)/totalVolumeAverage;
       			 res.setScore(normalizedVolume);
       			 
       		 }else{
       			/*
       			
       			System.out.println((globalStd/res.getStdVertex())*(res.getMeanVertex()/globalMean));
           		res.setScore( (globalStd/res.getStdVertex())*(res.getMeanVertex()/globalMean) );
           		*/
       		
       			/*normalización a uno:
       			Double normalizedStdVertex=1-(res.getStdVertex()-minStd)/(maxStd-minStd);
       			//Double normalizedStdFaces=1-(res.getStdFaces()-minStdFaces)/(maxStdFaces-minStdFaces);
       			Double normalizedVolume=(globalAverageVolumes.get(i)-minAverageVolume)/(maxAverageVolume-minAverageVolume);
       			*/
       			/*Double normalizedStdVertex=globalMeanStdObjects.get(i)/totalStdElementAverage;
       			*/
       			Double normalizedVolume=globalAverageVolumes.get(i)/totalVolumeAverage;
       			Double normalizedStdVolume=res.getStdVolume()/totalStdVolume;
       			//Double normalizedStdFaces=res.getStdFaces()/totalStdFaces;*/
       			Double distanceOfMedian = distanceOfMedian(normalizedStdVolume,stdMedianStdVolume);
       			Double normalizedMeanVertex=res.getMeanVertex()/totalMeanVertex;
       			score=normalizedVolume*100;//+(res.getNotNullCells()/83);
       			//score= 
           		//res.setScore(score);
       			 
       			//score=( ((1-normalizedStdVertex)*50) +(normalizedVolume*100)+ ((1-normalizedStdFaces)*50) );
       			res.setDistance(distanceOfMedian);
           		res.setScore(score);
           		res.setStdCondition(false);
           		this.poblacion.set(i,res);
           		
           		
       		}
       		
       	
       	}
       	
       	this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(this.gen)+"\\resultadoFitnessPob"+String.valueOf(this.gen)+".csv");
       	
       	
       	if(elementsToBeDeleted.isEmpty()==false) {//if it is not empty, then the elements to be deleted are erased.
       		
	       	for(Individuo pos: elementsToBeDeleted) {
	       		this.poblacion.remove(pos);
	       	}
       	}
       
	}
	
	
	public void FitnessCalculation2() {
		
		ArrayList<Double> globalMeanStdObjects= new ArrayList<Double>();
		ArrayList<Double> globalMeanStdFaces= new ArrayList<Double>();
		ArrayList<Double>globalAverageVolumes=new ArrayList<Double>();

		ArrayList<Double>globalAverageStdVolumes=new ArrayList<Double>();
		
		Double mean;
       	Integer j=0;
       	ArrayList<Individuo> individuals= this.getPopulation();
       	for(Individuo res: individuals ) {
       		
       		File[] listOfCells=res.getDir().listFiles();
       		
       		ArrayList<Integer> listOfElements = new ArrayList<Integer>();
    		ArrayList<Integer>listOfFaces=new ArrayList<Integer>();
    		
	       	int i;
   			Integer cells=0;
		       	//la máxima iteracion es length-1 porque el ultimo elemento es el limesegparams que no nos interesa
		       	for(i=0;i<(listOfCells.length-1);i++) {

			       		File ruta= new File(listOfCells[i].toString()+"\\T_1.ply");
			       		System.out.println(ruta.toString());
			     
			       	   try {
						Scanner in = new Scanner(new FileReader(ruta.toString()));
						String numberOfVertex="";
						if(ruta.length()!=0) {
							
						//el numero de elementos siempre va antes que la primera propiedad, por tanto si la siguiente linea es property no debe entrar ya que tenemos el numero
						/*while(in.hasNext("property")==false) {
						   numberOfVertex =in.next().toString();
						   //System.out.println(numberOfVertex);
						}*/

						//si hacen falta las caras:
						String previo="";
						String siguiente="";
						String vertex="vertex";
						String face="face";
						boolean termina=false;
						while(termina==false) {
							System.out.println(in.next());
							if(in.hasNext("vertex")){
								vertex=in.next().toString();
								numberOfVertex=in.next().toString();
								
							}else if(in.next().contains("face")){
								face=in.next().toString();
								termina=true;
							}
							

						}
						

						listOfFaces.add(Integer.parseInt(face));
						
						
						}else {
							
							 numberOfVertex="0";
							 cells++;
						}
						
						
						in.close();
						System.out.println(Integer.parseInt(numberOfVertex));
						listOfElements.add(Integer.parseInt(numberOfVertex));
						
						
									
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
		       	}
		    	
		       	Double std=0.0;
		       	if(listOfElements.isEmpty()==true) {
			       		mean=0.0d;
			       		std=0.0d;
		       	}else {
				       	mean=(double) (listOfElements.stream().mapToInt(Integer::intValue).sum())/listOfElements.size();
				       	std=calculaSTD(listOfElements);
		       	}
		       	
		       	Double stdVolume=calculaSTD2( getCellVolumes(res.getDir().toString()) );
		       	res.setStdVertex(std);
		       	res.setStdVolume(stdVolume);
		       	
		       	mean=(double) (listOfElements.stream().mapToInt(Integer::intValue).sum()/listOfElements.size());
		       	Double averageVolume=null;
		       	
		       	if(mean>0) {
		       		averageVolume=getCellAverageVolumes(res.getDir().toString());
		       		globalAverageVolumes.add(averageVolume);
		       	}else {
		       		averageVolume=0.0;
		       		globalAverageVolumes.add(0.0);
		       		
		       	}

		       	//globalMeanCellObjects.add(mean);
		       	Double stdFaces=calculaSTD(listOfFaces);
		       	res.setStdFaces(stdFaces);
		       	globalMeanStdFaces.add(stdFaces);
		       	
		       	res.setAverageVolume(averageVolume);
		       	globalMeanStdObjects.add(std);
		       	globalAverageStdVolumes.add(stdVolume);
		       	res.setNotNullCells(cells);
       	}
       	
       
       	
       	/*
       	Collections.sort(globalMeanStdObjects);
       	Double minStd=globalMeanStdObjects.get(0);
       	Double maxStd=globalMeanStdObjects.get(globalMeanStdObjects.size()-1);
       	
       	Collections.sort(globalAverageVolumes);
    	Double minAverageVolume=globalAverageVolumes.get(0);
    	Double maxAverageVolume=globalAverageVolumes.get(globalAverageVolumes.size()-1);
    	*/
       	
       	//sum of all average volumes, this is done in order to normalize the values later
       	int percentile=90;
       	Double percentileValue=calcPercentiles(globalMeanStdObjects,percentile);
       	Double percentileValue75 =calcPercentiles(globalAverageStdVolumes,75);
       	Double percentileValue50 =calcPercentiles(globalAverageStdVolumes,50);
       	
       	Double totalVolumeAverage=(double)globalAverageVolumes.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalStdVolume= globalAverageStdVolumes.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalStdElementAverage=globalMeanStdObjects.stream().mapToDouble(Double::doubleValue).sum();
       	Double totalStdFaces=globalMeanStdFaces.stream().mapToDouble(Double::doubleValue).sum();
       	
       	Double stdMedianVertexNormalized=getMedianStd(globalMeanStdObjects)/totalStdElementAverage;
       	
       	/*Collections.sort(globalMeanStdFaces);
       	Double minStdFaces=	globalMeanStdFaces.get(0);
       	Double maxStdFaces=	globalMeanStdFaces.get(globalMeanStdObjects.size()-1);*/
       	
       	//Collections.sort(globalMeanCellObjects);
       	//Double minMean=globalMeanCellObjects.get(0);
       	//Double maxMean=globalMeanCellObjects.get(globalMeanCellObjects.size()-1);
       	ArrayList<Individuo> elementsToBeDeleted= new ArrayList<Individuo>();
       	
       	int i=0;
       	for(i=0;i<individuals.size();i++) {
       		Individuo res=individuals.get(i);
       		Double score=0.0d;
       		
       		 if(res.getStdVertex()==0 || percentileValue<res.getStdVertex()) {
       			//individuals.remove(i);
           		elementsToBeDeleted.add(res);
       		 }else if(res.getAverageVolume()<0) {
       			 
           		elementsToBeDeleted.add(res);
           	
       		 }else{
       			/*
       			
       			System.out.println((globalStd/res.getStdVertex())*(res.getMeanVertex()/globalMean));
           		res.setScore( (globalStd/res.getStdVertex())*(res.getMeanVertex()/globalMean) );
           		*/
       		
       			/*normalización a uno:
       			Double normalizedStdVertex=1-(res.getStdVertex()-minStd)/(maxStd-minStd);
       			//Double normalizedStdFaces=1-(res.getStdFaces()-minStdFaces)/(maxStdFaces-minStdFaces);
       			Double normalizedVolume=(globalAverageVolumes.get(i)-minAverageVolume)/(maxAverageVolume-minAverageVolume);
       			*/
       			Double normalizedStdVertex=globalMeanStdObjects.get(i)/totalStdElementAverage;
       			Double normalizedVolume=globalAverageVolumes.get(i)/totalVolumeAverage;
       			Double normalizedStdVolume=res.getStdVolume()/totalStdVolume;
       			Double normalizedStdFaces=res.getStdFaces()/totalStdFaces;
       			Double distanceOfMedian = distanceOfMedian(normalizedStdVertex,stdMedianVertexNormalized);
       			score=normalizedVolume*1000*res.getNotNullCells();
       			//score= 
           		//res.setScore(score);
       			 
       			//score=( ((1-normalizedStdVertex)*50) +(normalizedVolume*100)+ ((1-normalizedStdFaces)*50) );
       			res.setDistance(distanceOfMedian);
           		res.setScore(score);
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
			float maximumTime=12.0f+(iter-1)*0.8f;//10.0f
			this.deletePopulation();
			this.poblacion=new ArrayList<Individuo>();
			this.poblacion.add(newPopulation.get(0));
			this.poblacion.add(newPopulation.get(1));
			
			//only Zscale has the same value for the new generations:
			float ZS=3.51f; //float ZS=4.06f;// variable con el valor del z_scale
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
					

			       	LimeSeg.clear3DDisplay();
			       	LimeSeg.clearAllCells();
			       
			       	
					Individuo ind= newPopulation.get(i);
					
					ind.setDir(new File(dirPob.toString()+"\\resultado"+String.valueOf(i)+"-gen"+String.valueOf(iter)));
					//llamo a la clase que va a llamar limeseg:
					SphereSegAdapted seg2=new SphereSegAdapted();
					seg2.set_path(dir.toString());
					seg2.setD_0(ind.getD0());
					seg2.setF_pressure(ind.getFp());
					seg2.setZ_scale(ZS);
					seg2.setRange_in_d0_units(ind.getRange_d0());
					 writer.append("resultado"+String.valueOf(i)+"-gen"+String.valueOf(iter));
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
					
					ind.getDir().mkdir();//it creates the directory for that individual

					
					boolean corte=false;
					
					memoryRegisters.add((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024.0 * 1024.0 * 1024.0));
					
					while (seg2.isAlive() && ((endTime2-startTime2) /1000)<maximumTime+5 ) {
						endTime2=System.currentTimeMillis();
						memoryRegisters.add((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024.0 * 1024.0 * 1024.0));
						
						System.out.println((endTime2-startTime2) /1000);
						
						if( ( (endTime2-startTime2) /1000)>maximumTime) { //si el tiempo de ejecucion es mayor que 100 segundos
							LimeSeg.requestStopOptimisation=true;
							LimeSeg.stopOptimisation();
							/*LimeSeg.clearDotsFromCellT();
							LimeSeg.clearOverlay();
							LimeSeg.removeDot();
							LimeSeg.showGUI();
							LimeSeg.update3DDisplay();*/
						

						}
						
					}
					


					LimeSeg.stopOptimisation();

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
					ind.setIdentifier("resultado"+String.valueOf(i)+"-gen"+String.valueOf(iter));
					ind.getDir().mkdir();//it creates the directory for that individual
					/*if(poblacion.get(i-1).getTime()==0 && poblacion.get(i).getTime()==0  && poblacion.get(i+1).getTime()==0) {
						deleteAllCellT();
					}else {*/
			       	LimeSeg.saveStateToXmlPly(ind.getDir().toString());//it saves the solution of the individual
			       	LimeSeg.clear3DDisplay();
			       	LimeSeg.clearAllCells();
			       	LimeSeg.clearOptimizer();
			       	LimeSeg.clearOverlay();
			       	//LimeSeg.opt.requestResetDotsConvergence=true;
			       	//LimeSeg.opt.fillHoles();

			       	LimeSeg.updateOverlay();
			       	LimeSeg.update3DDisplay();
			       	
					
			       	
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
		
		//System.out.println(media);
		//System.out.println(sum);
		
		media=media/elementosSegmentacion.size();
		
		for(Integer elementosCelulaSegmentada:elementosSegmentacion) {
			//restamos, elevamos al cuadrado y sumamos
			std+=Math.pow((elementosCelulaSegmentada-media),2);
			
		}
		System.gc();
		std=math.sqrt( (std/elementosSegmentacion.size()) );
		return std;
		
	}
	
	
	public Double calculaSTD2 (ArrayList<Double> volumes /*elementosSegmentacion*/) {

		//System.out.println(elementosSegmentacion.contains(null));
		Double std=0d;
		Double media=0d;
		Double sum = volumes.stream().mapToDouble(a -> a).sum();
		
		for(Double elementosCelulaSegmentada:volumes) {
			media+=elementosCelulaSegmentada;
		}
		
		//System.out.println(media);
		//System.out.println(sum);
		
		media=media/volumes.size();
		
		for(Double elementosCelulaSegmentada:volumes) {
			//restamos, elevamos al cuadrado y sumamos
			std+=Math.pow((elementosCelulaSegmentada-media),2);
			
		}
		System.gc();
		std=math.sqrt( (std/volumes.size()) );
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
             writer.append("StdVertex");
             writer.append(',');
             writer.append("meanVertex");
             writer.append(',');
             writer.append("AverageVolume");
             writer.append(',');
             writer.append("VolumeStd");
             writer.append(',');
             writer.append("FacesStd");
             writer.append(',');
             writer.append("NotNullCells");
             writer.append(',');
             writer.append("MedianDistance");
             writer.append(',');
             writer.append("NotNullCells");
             writer.append(',');
             writer.append("StdCondition");
             writer.append(',');
             writer.append("Score");
             writer.append(',');
             writer.append("SelectionMethod");
             writer.append(',');
             writer.append("offSpringMethod");
             writer.append(',');
             writer.append("Time");
             writer.append(',');
             writer.append("Directory");
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
                  writer.append(String.valueOf(ind.getStdVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getMeanVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getAverageVolume()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getStdVolume()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getStdFaces()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getNotNullCells()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getDistance()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getNotNullCells()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.isStdCondition()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getScore()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getSelectionMethod()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getOffspringMethod()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getTime()));
                  writer.append(',');
                  writer.append(ind.getDir().toString());
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
                  writer.append(String.valueOf(ind.getStdVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getMeanVertex()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getAverageVolume()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getStdVolume()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getStdFaces()));
                  writer.append(',');
                  writer.append(String.valueOf(ind.getNotNullCells()));
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
	
	
	
    public ArrayList<Individuo> startAgain(String directoryRoute) {
    	
    	 String line = "";
         String cvsSplitBy = ",";
         ArrayList<Individuo> newIndividuals = new ArrayList<Individuo>();
         
         try (BufferedReader br = new BufferedReader(new FileReader(directoryRoute))) {

             while ((line = br.readLine()) != null) {
            	 Individuo ind= new Individuo();
            	 
                 String[] individuoString = line.split(cvsSplitBy);
                 
                 if(individuoString[0].contains("MetodoSeleccionado")==false) {
	                 ind.setD0(Float.parseFloat(individuoString[7]));
	                 ind.setRange_d0(Float.parseFloat(individuoString[8]));
	                 ind.setF_pressure(Float.parseFloat(individuoString[9]));
	                 ind.setOffspringMethod(individuoString[0]);
	                 newIndividuals.add(ind);
	                 
                 }
                 
             }

         } catch (IOException e) {
             e.printStackTrace();
         }
         
         return newIndividuals;
   }
    
   public void mainStartAgain(String populationResults,int numIndividuals,int lastIter,int iters) {
	   
	   String initialhour=String.valueOf(LocalDateTime.now().getHour())+"."+String.valueOf(LocalDateTime.now().getMinute());
	   int i=0;
		
	   try{
			
		   File file1 = new File(this.dir.toString()+"\\resultados\\resultadosGlobales2.csv");
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
	       
	       ArrayList<Individuo> newPopulation=startAgain(populationResults);
	       File dirPob=new File(dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(lastIter));
	       dirPob.mkdir();
	       this.NewPopulationGenerator(newPopulation, lastIter);
	       this.FitnessCalculation();
	       System.gc();	
	       this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(lastIter)+"\\resultadoPob"+String.valueOf(lastIter)+".csv");
	       System.gc();	
	       this.writeGlobalResultsCSV(writer1);
	       generationalChange iterativeChange=new generationalChange(this.poblacion,numIndividuals,lastIter,this.dir.toString());
	       System.gc();	
	       iterativeChange.main(lastIter,this.dir.toString());
	       
		   System.gc();
			
			for(i=lastIter+1;i<iters;i++){//i=200
				
				this.NewPopulationGenerator(newPopulation, i);
				this.FitnessCalculation();
				System.gc();	
				this.writeResultsCSV(this.dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(i)+"\\resultadoPob"+String.valueOf(i)+".csv");
				System.gc();	
				this.writeGlobalResultsCSV(writer1);
				iterativeChange=new generationalChange(this.poblacion,numIndividuals,i,this.dir.toString());
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
			
			try{
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
		           writer.append(String.valueOf(bestIndividual.getStdVertex()));
		           writer.append(',');
		           writer.append(String.valueOf(bestIndividual.getAverageVolume()));
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
   
   
   public int[][] readPLY(String filename){
	   
	   int [][] vertex= null;
	   
	   try {
		   FileInputStream binaryPly= new FileInputStream(filename);
		   ObjectInputStream ply= new ObjectInputStream (binaryPly);
		   while(ply.available()>0) {
			   
		   int x = ply.readInt();
		   int y = ply.readInt();
		   
		   }
		   
	   }catch(IOException e) {
		   
		   e.printStackTrace();
	   }
	   
	   return vertex;

   }
		
	
	public void deletePopulation() {
		this.poblacion=null;
		System.gc();
	}
	
	public void addIndividual(Individuo i) {
		this.poblacion.add(i);
	}
	
	public Double getCellAverageVolumes(String directory){
		

		
		LimeSeg.loadStateFromXmlPly(directory);
		ArrayList<Cell> cells=LimeSeg.allCells;
		ArrayList<Double> cellVolumes= new ArrayList<Double>();
		
		for (Cell c:cells) {
			LimeSeg.currentCell=c;
			CellT ct = c.getCellTAt(1);
			System.out.println(ct.getVolume());
			cellVolumes.add(ct.getVolume());
			/*
			for (DotN dn:ct.dots) {
				float x=dn.pos.x;
				float y=dn.pos.y;
				float z=dn.pos.z;
				//System.out.println("P=\t"+dn.pos.x+"\t"+dn.pos.y+"\t"+dn.pos.z+"\t N=\t"+dn.Norm.x+"\t"+dn.Norm.y+"\t"+dn.Norm.z+"\n");
			}*/
			
		}

		Double average=cellVolumes.stream().mapToDouble(Double::doubleValue).sum()/cellVolumes.size();
		
		return average;
	}
	
	
	public Double getCellAverageCentroids(String directory){
		
		LimeSeg.loadStateFromXmlPly(directory);
		ArrayList<Cell> cells=LimeSeg.allCells;
		ArrayList<Double> cellCentroids= new ArrayList<Double>();
		
		for (Cell c:cells) {
			LimeSeg.currentCell=c;
			CellT ct = c.getCellTAt(1);
			Vector3D centroid=ct.center;
			double averageCenter=Math.sqrt(Math.pow(centroid.x,2)+Math.pow(centroid.y,2)+Math.pow(centroid.z,2));
			ArrayList<Double> distancesCentroid=new ArrayList<Double>();
			
			for(DotN point: ct.dots) {
				double averageCenterCell=Math.sqrt(Math.pow(point.pos.x,2)+Math.pow(point.pos.y,2)+Math.pow(point.pos.z,2));
				distancesCentroid.add(Math.sqrt(Math.pow((averageCenter-averageCenterCell),2)));
			}
			
			double AveragePointsDistanceOfCentroid=distancesCentroid.stream().mapToDouble(Double::doubleValue).sum()/distancesCentroid.size();
			cellCentroids.add(AveragePointsDistanceOfCentroid);
		}

		Double average=cellCentroids.stream().mapToDouble(Double::doubleValue).sum()/cellCentroids.size();
		
		return average;
	}
	
	public ArrayList<Double> getCellVolumes(String directory){
		

		
		LimeSeg.loadStateFromXmlPly(directory);
		ArrayList<Cell> cells=LimeSeg.allCells;
		ArrayList<Double> cellVolumes= new ArrayList<Double>();
		
		for (Cell c:cells) {
			LimeSeg.currentCell=c;
			CellT ct = c.getCellTAt(1);
			System.out.println(ct.getVolume());
			cellVolumes.add(ct.getVolume());
			/*
			for (DotN dn:ct.dots) {
				float x=dn.pos.x;
				float y=dn.pos.y;
				float z=dn.pos.z;
				//System.out.println("P=\t"+dn.pos.x+"\t"+dn.pos.y+"\t"+dn.pos.z+"\t N=\t"+dn.Norm.x+"\t"+dn.Norm.y+"\t"+dn.Norm.z+"\n");
			}*/
			
		}

		
		return cellVolumes;
	}
	
	public void deleteAllCellT() {
		ArrayList<Cell> cells=LimeSeg.allCells;
		
		for (Cell c:cells) {
			LimeSeg.currentCell=c;
			LimeSeg.clearCurrentCell();
			LimeSeg.clearDotsFromCellT();
			LimeSeg.clearOverlay();
	       	LimeSeg.jcr.removeDisplayedCell(c);
	       	//puedes probar a borrar los dots a ver
		}
		
		LimeSeg.jcr.glWindow.destroy();
	}
	
	
	public void ReadPly(String directory) {
   		DataInputStream input;
		try {
			input = new DataInputStream(new BufferedInputStream(new FileInputStream(directory)));
			try {
				
				while(input.readLine()!=null) {
					String linea=input.readLine();
					System.out.println(linea);
					
					if(linea.contains("end_header")){
						
						while(input.readLine()!=null) {

							int x=input.readInt() ;
							System.out.println(x);
							String bytes=String.valueOf(x);
							int a=Character.getNumericValue(bytes.charAt(0));
							int b=Character.getNumericValue(bytes.charAt(1));
							int c=Character.getNumericValue(bytes.charAt(2));
							int d=Character.getNumericValue(bytes.charAt(3));
							int valor=(((a & 0xff) << 24) | ((b & 0xff) << 16) |  ((c & 0xff) << 8) | (d & 0xff));
							System.out.println(valor);
							
							int v2 =x & 0xFF;
							System.out.println(v2);
							
							byte b2 = (byte)x;
							int v3 =  b2 &255;
							System.out.println(v3);
							  System.out.println((int)x);

						}
					}

				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}
	
	public Double getMedianStd(ArrayList<Double> stds) {
		Double median=null;
		Collections.sort(stds);
		Integer posMedianValues=(int) Math.round(stds.size()/2);
		median=(stds.get(posMedianValues)+stds.get(posMedianValues+1))/2;
		
		return median;
		
	}
	
    public Double distanceOfMedian(Double std, Double stdMedian) {
    	Double distance=null;
    	
    	
    	distance=Math.pow((stdMedian-std),2);
    	Double normalizedDistance=math.sqrt(distance)/stdMedian;
    	
    	return normalizedDistance;
    }
    
    
    public Double calcPercentiles(ArrayList<Double> list,int percentile) {
        
    	int size = list.size();
		Collections.sort(list);
        /*Double sum = (double) 0;
        for (int i = 0; i < size; i++) {
            sum += list.get(i);
        }
        Double percentage=(double) percentile/100;
        Double sumPercentile=percentage*sum;
        Double sumList=(double) 0;
        
        int j=0;

        while(sumPercentile>sumList) {
        	
        	sumList=sumList+list.get(j);
        	j++;
        
        }
        
        return list.get(j);*/
    	
        Double percentage=(double) percentile/100;
        Double pos= percentage*(size-1);
        int posUp=(int) Math.ceil(pos);
       
        int posDown=(int) Math.floor(pos);
        
        Double percentileValue=(list.get(posDown)+list.get(posUp))/2;
        
        return percentileValue;
    }


	public int getGen() {
		return gen;
	}


	public void setGen(int gen) {
		this.gen = gen;
	}


	public int getNumberOfCells() {
		return numberOfCells;
	}


	public void setNumberOfCells(int numberOfCells) {
		this.numberOfCells = numberOfCells;
	}
	
}
