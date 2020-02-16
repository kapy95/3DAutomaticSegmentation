/**
 * AutomaticSegmentation
 */
package AutomaticSegmentation;


import java.io.File;

//import org.reflections.vfs.Vfs.File;

//import javax.swing.SwingUtilities;

import AutomaticSegmentation.gui.MainWindow;
import AutomaticSegmentation.limeSeg.SphereSegAdapted;
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
		File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM\\Datos");
		
		//llamo a la clase que va a llamar limeseg:
		SphereSegAdapted seg=new SphereSegAdapted();
		seg.set_path(dir.toString());
		seg.setD_0(5);
		seg.setF_pressure((float) 0.02);
		seg.setZ_scale((float) 4.06);
		seg.setRange_in_d0_units(2);
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
				cond=false;
				LimeSeg.stopOptimisation();
				//LimeSeg.saveStateToXmlPly((seg.getPath().toString()+"\\resultados"));
				//seg.interrupt();
			}

		}
		
		/*
		//así se registra automaticamente una vez solo pero hay que esperar ese tiempo siempre (100 segundos) y no se que puede pasar si ha terminado
		//el hilo de sphereSegAdapted y hago LimeSeg.stopOptimization
		boolean cond=true;
		while (cond==true) {
			
			endTime= System.currentTimeMillis();
			System.out.println("Este es el tiempo final:");
			System.out.println(endTime/1000);
			System.out.println("Resta:");
			System.out.println((endTime-startTime) /1000);
			
			if( ((endTime-startTime) /1000) >100) { //si el tiempo de ejecucion es mayor que 20 segundos corta?
				cond=false;
				System.out.println("PAM");
				LimeSeg.stopOptimisation();
				//LimeSeg.saveStateToXmlPly((seg.getPath().toString()+"\\resultados"));
				//seg.interrupt();
			}

		}
		*/
		
		
		/*
		File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM\\Datos\\RoiSet");
		File[] listOfFiles = dir.listFiles();
        
		int nRois=listOfFiles.length;
		
		String roiname=dir.toString()+"\\"+listOfFiles[1].getName();
		System.out.println(roiname);
			
		//Roi roi=RoiDecoder.open("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM\\Datos\\RoiSet\\0024-0377-0550.roi");
		Roi roi=RoiDecoder.open(roiname);
		System.out.println(roi.getFloatHeight());
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