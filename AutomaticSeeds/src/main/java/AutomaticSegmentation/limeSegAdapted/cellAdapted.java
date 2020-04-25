package AutomaticSegmentation.limeSegAdapted;

import java.util.ArrayList;

import eu.kiaru.limeseg.LimeSeg;
import eu.kiaru.limeseg.struct.Cell;
import eu.kiaru.limeseg.struct.CellT;
import eu.kiaru.limeseg.struct.DotN;
import eu.kiaru.limeseg.struct.Vector3D;

public class cellAdapted extends Cell{


	    /**
	     * List of cell timepoints contained within this cell
	     */
	    public ArrayList<cellTAdapted> cellTs = new ArrayList<>();                       

	    /**
	     * static counter to generate new cell identifiers
	     */
	    public int idCell = 0;                   

	    /**
	     * 
	     */
	    public String prefixCell = "cell_";

	    /**
	     * unique(?) String identifier of Cell 
	     */
	    public String id_Cell;

	    /**
	     * Index used for GPU computation only (@see GpuDots)
	     */
	    public int tempIndex;

	    /**
	     * display mode for JOGLCellRenderer
	     * 0 = dots
	     * 1 = triangles (if the corresponding CellT has been tesselated)
	     */
	    public int display_mode;

	    /**
	     * channel to which this CellT belongs - useful for visualization purpose only
	     */
	    public int cellChannel;    

	    /**
	     * Color of the cell format RGBA as float [3]
	     * A is currently disabled
	     */
	    public float color[] = new float[] { 0.1f, 0.8f, 0.0f, 1.0f };

	    /**
	     * Notifies if the cell has been modified, i.e.
	     * if a new CellT is created
	     * if new dots are added to any CellT
	     * ... still needs to be checked
	     */
	    public boolean modified = true;

	    /**
	     * Constructor
	     * @param channel_ to which this cell is attached, for display purpose only
	     */
	    public cellAdapted(int channel_,limeSegAdapted lime) {     
	        cellChannel=channel_;
	        //idCell++;
	        id_Cell=prefixCell+idCell;
	        // Dirty Hack to avoid duplicate keys...
	        // Fix : change allCells into hashtable...
	        boolean idAlreadyExists=false;
	        for (cellAdapted c:lime.allCells2) {
	        	if (c.id_Cell.equals(id_Cell)) {
	        		idAlreadyExists=true;
	        		break;
	        	}
	        }
	        if (idAlreadyExists) {
	        	while (idAlreadyExists) {
	        		idCell++;
	        		id_Cell=prefixCell+idCell;
	                // Dirty Hack to avoid duplicate keys...
	                // Fix : change allCells into hashtable...
	                idAlreadyExists=false;
	                for (cellAdapted c:lime.allCells2) {
	                	if (c.id_Cell.equals(id_Cell)) {
	                		idAlreadyExists=true;
	                		break;
	                	}
	                }
	        	}
	        }
	        id_Cell=prefixCell+idCell;
	    }

	    /**
	     * Constructor this initializes the channel property to zero
	     */
	    public cellAdapted() {
	    	super();
	    	this.cellChannel=0;
	    }

	    /**
	     * Get the CellT at a specific timepoint for this Cell
	     * @param frame Timepoint of this Cell
	     * @return the CellT object at this timeframe for this Cell, if it exists. returns null otherwise
	     */
	    public cellTAdapted getCellTAt(int frame) {
	        cellTAdapted ct=null;
	        for (int i=0;i<cellTs.size();i++) {
	            cellTAdapted  ct_test = cellTs.get(i);
	            if (ct_test.frame==frame) {
	                ct=ct_test;
	            }
	        }
	        return ct;
	    }

	    /**
	     * Creates an empty CellT for a specific timepoint
	     * @param frame frame at which this CellT is created
	     */
	    public void addTimePoint(int frame) {
	        if (this.getCellTAt(frame)==null) {
	            cellTs.add(new cellTAdapted(this,frame));
	        }        
	        modified=true;
	    }

	    /**
	     * Adds the dots contained in dots_in to a specific timepoint of this cell. 
	     * If no CellT exist for the specified frame, a new CellT is created. 
	     * Otherwise the points are added to the already existing points
	     * @param frame timepoint at which the dots are added
	     * @param dots_in array of dots to be added
	     */
	    public void addDots(int frame, ArrayList<DotN> dots_in) {
	        cellTAdapted curCellT=this.getCellTAt(frame);        
	        if (curCellT==null) {
	            curCellT=new cellTAdapted(this,frame);
	            cellTs.add(curCellT);
	        }
	        if (dots_in!=null)
	        for (int i=0;i<dots_in.size();i++) {
	            DotN nd = dots_in.get(i);
	            DotnAdapted nd_copy = new DotnAdapted(new Vector3D(nd.pos.x ,nd.pos.y ,nd.pos.z ),
	            		                new Vector3D(nd.Norm.x,nd.Norm.y,nd.Norm.z));
	            nd_copy.ct=curCellT;
	            nd_copy.N_Neighbor=nd.N_Neighbor;
	            nd_copy.userMovable=nd.userMovable;
	            nd_copy.userDestroyable=nd.userDestroyable;
	            nd_copy.userGenerate=nd.userGenerate;
	            nd_copy.userRotatable=nd.userRotatable;
	            curCellT.dots.add(nd_copy);                
	        }            
	        curCellT.updateCenter();        
	        modified=true;
	    } 

	    @Override
	    public String toString() {
	    	return this.id_Cell;
	    }

}