/**
 * AutomaticSegmentation
 */
package AutomaticSegmentation;

//import java.io.File; SOLUCIONARLO PARA IMPORTAR EL TIPO
import java.io.File;
//import org.reflections.vfs.Vfs.File;

//import javax.swing.SwingUtilities;

import AutomaticSegmentation.gui.MainWindow;
import AutomaticSegmentation.limeSeg.SphereSegAdapted;
import eu.kiaru.limeseg.LimeSeg;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

/**
 * 
 * @author
 *
 */
public class MainAutomatic3DSegmentation implements PlugIn {

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
		File dir = new File("C:\\Users\\Carlo\\Desktop\\Máster\\TFM\\prueba\\Datos");
		//dir.mkdir();
		
		//llamo a la clase que va a llamar limeseg:
		SphereSegAdapted seg=new SphereSegAdapted();
		seg.set_path(dir.toString());
		seg.setD_0(5);
		System.out.println(seg.getD_0());
		seg.setF_pressure(4);
		seg.setZ_scale(2);
		seg.setRange_in_d0_units(5);
		
		ImagePlus x=IJ.openImage("C:\\Users\\Carlo\\Desktop\\Máster\\TFM\\prueba\\Datos\\ImageSequence");
		//ImagePlus x=seg.getImp();
		x.getInfoProperty();
		
		//seg.getImp();
		
		//seg.run();
		
		//..etc cargar más paramétros
		//LimeSeg.saveStateToXmlPly(path);
		
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