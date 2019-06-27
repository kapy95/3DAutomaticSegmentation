
package PostProcessingGland.GUI;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import PostProcessingGland.Elements.Cell3D;
// import JTableModel;
import PostProcessingGland.GUI.CustomElements.CustomCanvas;
import PostProcessingGland.GUI.CustomElements.SegmentationOverlay;
import PostProcessingGland.PostProcessingGland;
import eu.kiaru.limeseg.LimeSeg;
import eu.kiaru.limeseg.io.IOXmlPlyLimeSeg;
import eu.kiaru.limeseg.struct.Cell;
import eu.kiaru.limeseg.struct.DotN;
import fiji.util.gui.OverlayedImageCanvas.Overlay;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;

public class PostProcessingWindow extends ImageWindow implements
	ActionListener
{

	
	private IOXmlPlyLimeSeg OutputLimeSeg;
	private Hashtable<Integer, ArrayList<Roi>> cellsROIs;
	private CustomCanvas canvas;
	private SegmentationOverlay overlayResult;
	private Cell LimeSegCell;
	private LimeSeg limeSeg;
	public ArrayList<Cell> allCells;

	private JFrame processingFrame;
	private JPanel upRightPanel;
	private JPanel middlePanel;
	private JPanel bottomRightPanel;
	private JButton btnSave;
	private JButton btnInsert;
	private JButton btnLumen;
	private JSpinner labelCell;

	// private JPanel IdPanel;

	private String initialDirectory;
	public Cell3D PostProcessCell;
	public ArrayList<Cell3D> all3dCells;

	// private JTableModel tableInf;
	// private Scrollbar sliceSelector;

	public PostProcessingWindow(ImagePlus raw_img) {
		super(raw_img, new CustomCanvas(raw_img));

		this.initialDirectory = raw_img.getOriginalFileInfo().directory;
		limeSeg = new LimeSeg();
		LimeSeg.allCells = new ArrayList<Cell>();
		LimeSegCell = new Cell();
		all3dCells = new ArrayList<Cell3D>();
		String directory = this.initialDirectory.toString();
		File dir = new File(directory + "/OutputLimeSeg");
		File[] files = dir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.startsWith("cell_");
			}
		});
		for (File f : files) {
			String path = f.toString();
			LimeSegCell.id_Cell = path.substring(path.indexOf("_") + 1);
			OutputLimeSeg.hydrateCellT(LimeSegCell, path);
			PostProcessCell = new Cell3D(LimeSegCell.id_Cell,LimeSegCell.cellTs.get(0).dots);
			PostProcessCell.labelCell= Integer.parseInt(LimeSegCell.id_Cell);
			all3dCells.add(PostProcessCell);
		}
		
		Collections.sort(all3dCells, new Comparator<Cell3D>(){
      @Override
			public int compare(Cell3D cel1, Cell3D cel2) {
        return cel1.getID().compareTo(cel2.getID());
     }
 });

		canvas = (CustomCanvas) super.getCanvas();
		PostProcessingGland.callToolbarPolygon();

		// Init attributes.
		cellsROIs = new Hashtable<Integer, ArrayList<Roi>>();
		processingFrame = new JFrame("PostProcessingGland");
		upRightPanel = new JPanel();
		middlePanel = new JPanel();
		bottomRightPanel = new JPanel();
		labelCell = new JSpinner();
		btnSave = new JButton("Save Cell");
		btnInsert = new JButton("Modify Cell");
		btnLumen = new JButton("Add Lumen");
		
		
		// tableInf = tableInfo;
		overlayResult = new SegmentationOverlay();
		if (overlayResult != null) {
			
			if (canvas.getImageOverlay() == null) {
				canvas.clearOverlay();
				raw_img.setOverlay(overlayResult.getAllOverlays(43,15,all3dCells, raw_img));
				overlayResult.setImage(raw_img);
				canvas.addOverlay(overlayResult);
				canvas.setImageOverlay(overlayResult);
				
			}
		}

		// removeAll();

		initGUI(raw_img);

		setEnablePanels(false);

		// threadFinished = false;

	}

	private void initGUI(ImagePlus raw_img) {
		
		labelCell.setModel(new SpinnerNumberModel(1, 1,all3dCells.size(), 1));

		upRightPanel.setLayout(new MigLayout());
		upRightPanel.setBorder(BorderFactory.createTitledBorder("ID Cell"));
		upRightPanel.add(labelCell);
		
		middlePanel.setLayout(new MigLayout());
		middlePanel.setBorder(BorderFactory.createTitledBorder("Cell Correction"));
		middlePanel.add(btnSave, "wrap");
		middlePanel.add(btnInsert, "wrap");
		
		bottomRightPanel.setLayout(new MigLayout());
		bottomRightPanel.setBorder(BorderFactory.createTitledBorder("Lumen Processing"));
		bottomRightPanel.add(btnLumen);
		

		processingFrame.setLayout(new MigLayout());
		processingFrame.add(canvas, "alignx center, span 1 5");
		processingFrame.add(upRightPanel, "wrap, gapy 10::50, aligny top");
		processingFrame.add(middlePanel, "aligny center, wrap, gapy 10::50");
		processingFrame.add(bottomRightPanel);
		processingFrame.setMinimumSize(new Dimension(1024, 1024));
		processingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		processingFrame.pack();
		processingFrame.setVisible(true);

		initializeGUIItems(raw_img);

	}

	private void initializeGUIItems(ImagePlus raw_img) {
		canvas.addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent ce) {
				Rectangle r = canvas.getBounds();
				canvas.setDstDimensions(r.width, r.height);
			}
			
		});
	}

	/**
	 * Enable/disable all the panels in the window
	 * 
	 * @param enabled true it will enable panels, false disable all panels
	 */
	protected void setEnablePanels(boolean enabled) {
		btnSave.setEnabled(true);
		btnInsert.setEnabled(true);
	}

	/**
	 * Disable all the action buttons
	 */
	protected void disableActionButtons() {
		btnSave.setEnabled(false);
		btnInsert.setEnabled(false);
	}

	/**
	 * Enable all the action buttons
	 */
	protected void enableActionButtons() {
		btnSave.setEnabled(true);
		btnInsert.setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

}
