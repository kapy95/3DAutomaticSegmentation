/**
 * AutomaticSegmentation
 */

package AutomaticSegmentation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import org.python.modules.math;

//import org.reflections.vfs.Vfs.File;

//import javax.swing.SwingUtilities;

import AutomaticSegmentation.gui.MainWindow;
import AutomaticSegmentation.limeSeg.Individuo;
import AutomaticSegmentation.limeSeg.SphereSegAdapted;
import AutomaticSegmentation.limeSeg.evolutionary_algorithm;
import AutomaticSegmentation.limeSeg.generationalChange;
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
		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Introduzca el número generaciones: ");
        
        int gens=0;
        
        try {
        	gens = Integer.parseInt(br.readLine());
        	 
        } catch (IOException e) {
			// TODO Auto-generated catch block
        	System.err.println("Formato no válido vuelva a intentarlo");
		}
        
        System.out.print("Introduzca el número de individuos por generación: ");
        int inds=0;;
        try {
            inds = Integer.parseInt(br.readLine());
        } catch(NumberFormatException | IOException nfe) {
            System.err.println("Formato no válido vuelva a intentarlo");
        }
        
	    //System.out.println("You entered float "+b)
        System.out.print("Introduzca el directorio con los datos y la carpeta de resultados: ");
        String userDirectory ="";
        try {
        	userDirectory = br.readLine();
        } catch(IOException nfe) {
            System.err.println("Formato no válido vuelva a intentarlo");
        }
        
		//String userDirectory = System.getProperty("user.dir");
		File dir = new File(userDirectory);
		
		
        System.out.print("Introduzca el Zscale de las imágenes: ");
        float zScale=0.0f;
        try {
            zScale = Float.parseFloat(br.readLine());
            
        } catch(NumberFormatException | IOException nfe) {
            System.err.println("Formato no válido vuelva a intentarlo");
        }
		
		
		evolutionary_algorithm ev=new evolutionary_algorithm(dir);

		ev.main(inds,gens,zScale);
       	
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