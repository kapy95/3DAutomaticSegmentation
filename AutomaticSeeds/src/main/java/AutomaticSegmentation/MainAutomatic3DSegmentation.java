/**
 * AutomaticSegmentation
 */

package AutomaticSegmentation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.python.modules.math;

//import org.reflections.vfs.Vfs.File;

//import javax.swing.SwingUtilities;

import AutomaticSegmentation.gui.MainWindow;
import AutomaticSegmentation.limeSeg.Individuo;
import AutomaticSegmentation.limeSeg.SphereSegAdapted;
import AutomaticSegmentation.limeSeg.evolutionary_algorithm;
import eu.kiaru.limeseg.LimeSeg;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

/**
 * 
 * @author
 *
 */
public class MainAutomatic3DSegmentation extends Thread implements PlugIn {
	/**
	 * 
	 */
	MainWindow mainWindow;

	/**
	 * Constructor by default
	 */
	public MainAutomatic3DSegmentation() {
		super();
	}

	/**
	 * Debug mode
	 * 
	 * @param args
	 *            default arguments
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins
		// menu
		Class<?> clazz = MainAutomatic3DSegmentation.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				url.length() - clazz.getName().length() - ".class".length() - "classes".length());
		System.setProperty("plugins.dir", pluginsDir);
		
		// start ImageJ
		new ImageJ();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
		
	}

	/*
	 * Plugin run method (non-Javadoc)
	 * 
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		// Build GUI
		/*SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Create the main window
				mainWindow = new MainWindow();
				mainWindow.pack();
				mainWindow.setVisible(true);
			}
			
		});
		*/
		
		//establezco el directorio de trabajo con las imágenes y roi
		File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM");
		evolutionary_algorithm ev=new evolutionary_algorithm();
		ev.setDir(dir);
		
		//Primera poblacion
		ev.InitialPopulationGenerator(100,0);
		ev.FitnessCalculation();
		ev.MutationFunction();
	
		int i;
		//empezamos en 1 porque la poblacion inicial no contaría como una iteración del algoritmo
		
		for(i=1;i<=200;i++) {
			
			//ev.PopulationGenerator(50,i);
			ev.FitnessCalculation();
			ev.MutationFunction();
			
		}
		
		/*
		//llamo a la clase que va a llamar limeseg:
		File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM");
		SphereSegAdapted seg=new SphereSegAdapted();
		seg.set_path(dir.toString());
		seg.setD_0(4.0f);
		seg.setF_pressure(-0.018f);
		seg.setZ_scale((float) 4.06);
		seg.setRange_in_d0_units(2.4f);
		seg.start();//empieza a ejecutarse run del hilo de limeseg

		long startTime = System.currentTimeMillis();
		long endTime=0;
		
		boolean cond=true;
		while (seg.isAlive() && cond==true) {
			
			endTime= System.currentTimeMillis();
			System.out.println((endTime-startTime) /1000);
			
			if( ((endTime-startTime) /1000) >100) { //si el tiempo de ejecucion es mayor que 20 segundos corta?
				cond=false;
				System.out.println("PAM");
				LimeSeg.stopOptimisation();
				LimeSeg.saveStateToXmlPly((seg.getPath().toString()+"\\resultados1"));
				//seg.interrupt();
			}

		}
		LimeSeg.saveStateToXmlPly((seg.getPath().toString()+"\\resultados1"));
       	LimeSeg.clear3DDisplay();
       	LimeSeg.clearAllCells();
		
		SphereSegAdapted seg2=new SphereSegAdapted();
		seg2.set_path(dir.toString());
		seg2.setD_0(7.0f);
		seg2.setF_pressure(-0.0059f);
		seg2.setZ_scale((float) 4.06);
		seg2.setRange_in_d0_units(4.3f);
		seg2.start();//empieza a ejecutarse run del hilo de limeseg

		long startTime2 = System.currentTimeMillis();
		long endTime2=0;
		
		boolean cond2=true;
		while (seg2.isAlive() && cond2==true) {
			
			endTime2= System.currentTimeMillis();
			System.out.println((endTime2-startTime2) /1000);
			
			if( ((endTime2-startTime2) /1000) >100) { //si el tiempo de ejecucion es mayor que 20 segundos corta?
				cond2=false;
				System.out.println("PAM2");
				LimeSeg.stopOptimisation();
				LimeSeg.saveStateToXmlPly((seg.getPath().toString()+"\\resultados2"));
				//seg.interrupt();
			}

		}
		LimeSeg.stopOptimisation();
		LimeSeg.saveStateToXmlPly((seg.getPath().toString()+"\\resultados2"));
		
		
		/*
		
		System.out.println("El bucle ha terminado");
		
       	File newdir =new File(dir.toString()+"\\resultados"+String.valueOf(1));
       	newdir.mkdir();
       	//LimeSeg.saveStateToXmlPly((newdir.toString()));
       	
       	File[] listOfFiles = newdir.listFiles();
       	System.out.println(listOfFiles.length);
       	//listOfFiles.remove(listOfFiles.size()); //el archivo limeseg params se elimina,(si pones delete se elimina
       	//del directorio la idea seria copiar toda la lista menos el último
       	ArrayList<Integer> listOfElements = new ArrayList<Integer>();
       	
       	int i;
       	//la máxima iteracion es length-1 porque el ultimo elemento es el limesegparams que no nos interesa
       	for(i=0;i<(listOfFiles.length-1);i++) {
       		
	       		File ruta= new File(listOfFiles[i].toString()+"\\T_1.ply");
	       		System.out.println(ruta.toString());
	       		
	       	   try {
				Scanner in = new Scanner(new FileReader(ruta.toString()));
				StringBuilder sb = new StringBuilder();
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
       	
		Double std=0d;
		double media=0;
		
		for(Integer elementosCelulaSegmentada:listOfElements) {
			media+=elementosCelulaSegmentada;
		}
		
		media=media/listOfElements.size();
		System.out.println(media);
		
		for(Integer elementosCelulaSegmentada:listOfElements) {
			//restamos, elevamos al cuadrado y sumamos
			std+=Math.pow((elementosCelulaSegmentada-media),2);
			
		}
		
		std=math.sqrt( (std/listOfElements.size()) );
		System.out.println(std);
		*/

       	
 

	}
	

	/**
	 * Static method to enable oval selection It is mainly used to create ROIs
	 */
	public static void callToolbarOval() {
		ij.gui.Toolbar.getInstance().setTool(ij.gui.Toolbar.OVAL);
	}

	/**
	 * Static method to enable polygon selection It is mainly used to create
	 * ROIs
	 */
	public static void callToolbarPolygon() {
		ij.gui.Toolbar.getInstance().setTool(ij.gui.Toolbar.POLYGON);
	}
}