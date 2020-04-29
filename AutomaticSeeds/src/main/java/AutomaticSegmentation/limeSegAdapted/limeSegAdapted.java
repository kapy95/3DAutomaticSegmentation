package AutomaticSegmentation.limeSegAdapted;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

import org.scijava.command.Command;

import eu.kiaru.limeseg.LimeSeg;
import eu.kiaru.limeseg.gui.CurrentCellColorLUT;
import eu.kiaru.limeseg.gui.DefaultDotNColorSupplier;
import eu.kiaru.limeseg.gui.DisplayableOutput;
import eu.kiaru.limeseg.gui.DotNColorSupplier;
import eu.kiaru.limeseg.gui.JFrameLimeSeg;
import eu.kiaru.limeseg.gui.JOGL3DCellRenderer;
import eu.kiaru.limeseg.gui.JTableCellsExplorer;
import eu.kiaru.limeseg.ij1script.HandleIJ1Extension;
import eu.kiaru.limeseg.ij1script.IJ1ScriptableMethod;
import eu.kiaru.limeseg.io.IOXmlPlyLimeSeg;
import eu.kiaru.limeseg.opt.Optimizer;
import eu.kiaru.limeseg.struct.Cell;
import eu.kiaru.limeseg.struct.CellT;
import eu.kiaru.limeseg.struct.DotN;
import eu.kiaru.limeseg.struct.PolygonSkeleton;
import eu.kiaru.limeseg.struct.Skeleton2D;
import eu.kiaru.limeseg.struct.Vector3D;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.macro.Functions;
import ij.process.FloatPolygon;
import net.imglib2.RandomAccessibleInterval;

public class limeSegAdapted extends LimeSeg {   
	 
    public Optimizer Opt2;                   			// Associated Optimizer, filled with dots currently being Optimized
    public JOGL3DCellRenderer jcr2;					// Associated 3D Viewer
    
     public ArrayList<cellAdapted> allCells2;  				// List of cells currently stored by LimeSeg
     public boolean notifyCellExplorerCellsModif2;		// Flags any change of allCells to the cellExplorer tab (Swing GUI)
   
     public ArrayList<DotnAdapted> dots_to_overlay2;			// Set of dots being overlayed on workingImP
    
     public cellAdapted currentCell2; //lo dejamos aquí   						// Current selected Cell of limeseg
    
     public DotnAdapted currentDot2;							// Current selected Dot of limeseg
   
	 public ImagePlus workingImP2;            			// Current working image in IJ1 format - Used for IJ1 interaction (ROI, and JOGLCellRenderer synchronization)
	
	 public int currentChannel2=1;						// specifies the Channel of the workingImP to work with
    	
	 public int currentFrame2=1;						// specifies the Frame of the workingImP to work with
    
     public  ArrayList<DotnAdapted> copiedDots2;				// Internal equivalent of Clipboard for set of dots in limeseg
     Skeleton2D skeleton2;  							// Current skeleton of Limeseg. Serves to generate initial shapes in 3D
    
     public boolean OptimizerIsRunning2=false;			// Flags if Optimizer is running
     public boolean requestStopOptimisation2=false;	// Flags is a stop Optimisation command has been requested
    
    // GUI
     JFrameLimeSeg jfs3Di;							// LimeSeg GUI (Swing)
     JTableCellsExplorer cExplorer;					// Cell Explorer (Swing table)
    
    // Macro extensions IJ1
     boolean extensionsHaveBeenRegistered = false;	// IJ1 scripting with macro extensions
    // End of  variables    
    
    /**
     * Initializes LimeSeg:
     * 	- creates an Optimizer
     *  - creates list of dots to overlay
     */
    public void initialize() {
    	if (copiedDots2==null) {
    		copiedDots2= new ArrayList<>();
    	}
    	if (allCells2==null) {
            allCells2=new ArrayList<>();
            notifyCellExplorerCellsModif2=true;
            notifyCellRendererCellsModif2=true;
        }
        if (dots_to_overlay2==null) {
        	dots_to_overlay2 = new ArrayList<>();
        }
    	initOptimizer2();
    }    
    
    void initOptimizer2() {
        if (Opt2==null) {        
                Opt2 = new Optimizer(this);
        }
    } 
    
    /**
     *  Tabs in LimeSeg GUI
     */
    final  public String 
    		TS=",", // target separator, do not use comma in other
    		IO="I/O",
    		VIEW_2D="2D View",
    		VIEW_3D="3D View",
    		Opt="Optimizer",
    		CURRENT_CELL="Current Cell",
    		STATE="STATE",
    		CURRENT_CELLT="Current CellT",
    		CURRENT_DOT="Current Dot",
    		CLIPPED_DOTS="Clipped Dots",
    		BENCHMARK="BenchMark";		
    		
    
    /**
     * returns the state of LimeSeg as a String:
     * @return a string containing working image + ZScale + currentframe +currenchannel
     */
    @DisplayableOutput(pr=0)
     public String getLmsState2() {
    	String str="";
    	str+="State:\n";
    	if (workingImP==null) {
        	str+="\t img     = null\n";
    	} else {
        	str+="\t img = "+workingImP2.getTitle()+"\n";
    	}
    	str+="\t ZScale  = "+Opt2.getOptParam("ZScale")+"\n";
    	str+="\t frame   = "+currentFrame2+"\n";
    	str+="\t channel = "+currentChannel2+"\n";
    	str+="Cells:\n";
    	str+="\t #cells="+allCells2.size()+"\n";
    	return str;    	
    }
    
    /**
     * get Optimizer state as a String
     * @return number of dots in Optimizer, if it is Optimizing and the image
     */
    @DisplayableOutput(pr=5)
     public String getOptState2() {
    	String str="";
    	str+="Optimizer:\n";
    	str+="\t #dots="+Opt2.dots.size()+"\n";
    	//str+="\t "+"\n";
    	str+="\t img="+((Opt2.image3DInfinite==null)?"-":Opt2.image3DInfinite.toString())+"\n";
    	str+="\t isOptimizing="+OptimizerIsRunning2+"\n";
    	return str;
    }
    
    /**
     * get current Cell state
     * @return  infos about the current selected cell as a String
     */
    @DisplayableOutput(pr=1)
     public String getCellState2() {
    	String str="";
    	str+="Cell:\n";
    	str+="\t current="+((currentCell2==null)?"null":currentCell2.id_Cell)+"\n";
    	str+="\t #cellT="+((currentCell2==null)?"-":currentCell2.cellTs.size())+"\n";
    	str+="\t channel="+((currentCell2==null)?"-":currentCell2.cellChannel)+"\n";
    	str+="\t color="+((currentCell2==null)?"[]\n":"["+(new DecimalFormat("#.##").format(currentCell2.color[0]))+";"
    												  +(new DecimalFormat("#.##").format(currentCell2.color[1]))+";"
    												  +(new DecimalFormat("#.##").format(currentCell2.color[2]))+";"
    												  +(new DecimalFormat("#.##").format(currentCell2.color[3]))+"]"+"\n");
    	return str;
    }
    
    /**
     * get copied dots properties
     * @return infos within a String about the dots that have been copied
     */
    @DisplayableOutput(pr=6)
     public String getClippedDotsState2() {
    	String str="";
    	str+="ClippedDots:\n";
    	if (copiedDots!=null) {
    		str+="\t #dots="+copiedDots.size()+"\n";
    	}
    	return str;
    }
    
    /**
     * get CellT state
     * @return infos as a String about the current cell timepoint (number of dots + has the mesh been reconstructed ?)
     */
    @DisplayableOutput(pr=2)
     public String getCellTState2() {
    	String str="CellT:\n";
    	if (currentCell2!=null) {
            cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
            if (ct!=null) {
            	str+="\t frame="+currentFrame+"\n";
            	str+="\t #dots="+ct.dots.size()+"\n";
            	str+="\t tesselated="+ct.tesselated+"\n";
            }
        }   else {
        	str+="\t null \n";
        }
    	return str;
    }
    
    /**
     * Generates a grid of surfels in 2D
     * @param d_0 equilibrium distance between surfels in pixels
     * @param pxi x position start
     * @param pyi y position start
     * @param pxf x position end
     * @param pyf y position end
     * @param pz  z position of the sheet
     * @return List of dots generated
     */
     public ArrayList<DotnAdapted> makeXYSheet2(float d_0, float pxi, float pyi, float pxf, float pyf, float pz) {
        ArrayList<DotnAdapted> ans = new ArrayList<DotnAdapted>();
        for (float x=pxi;x<pxf;x+=d_0) {
            for (float y=pyi;y<pyf;y+=d_0) {
                Vector3D pos = new Vector3D(x,y,pz);
                Vector3D normal = new Vector3D(0,0,1);
                DotnAdapted nd=new DotnAdapted(pos,normal);                            
                ans.add(nd);      
            }
        }
        return ans;
    }
    
    /**
     * Generates a sphere surface made of surfels. All units in pixels
     * @param d_0 equilibrium distance between surfels in pixels
     * @param px sphere center X
     * @param py sphere center Y
     * @param pz sphere center Z
     * @param radius sphere radius
     * @return
     */
     public ArrayList<DotnAdapted> makeSphere2(float d_0, float px, float py, float pz, float radius) {
        ArrayList<DotnAdapted> ans = new ArrayList<DotnAdapted>();
        float dlat=d_0/radius;
        float lat_i=(float) (-java.lang.Math.PI/2);
        float lat_f=(float) (java.lang.Math.PI/2);//-dlat;               
        for (float lat=lat_i+dlat; lat<(lat_f); lat=lat+dlat) {
            // We put points around a circle of radius = radius.cos(lat)
            float R=(float) (radius*java.lang.Math.cos(lat));            
            float N=(int)(java.lang.Math.PI*2.0*R/d_0);
            float dAngle=(float) (java.lang.Math.PI*2.0/N);
            for (float i=0;i<N;i++) {
                Vector3D normal = new Vector3D((float)(R*java.lang.Math.sin(i*dAngle)),
                                                 (float)(R*java.lang.Math.cos(i*dAngle)),
                                                 (float)(radius*java.lang.Math.sin(lat)));
                Vector3D pos = new Vector3D(px+normal.x,
                                              py+normal.y,
                                              pz+normal.z);
                DotnAdapted nd=new DotnAdapted(pos,normal);
                ans.add(nd);               
            }
        }
        return ans;
    }

    /**
     * Generates a sphere surface made of surfels. All units in pixels
     * @param d_0 equilibrium distance between surfels in pixels
     * @param px sphere center X
     * @param py sphere center Y
     * @param pz sphere center Z
     * @param radius sphere radius
     * @return
     */
     public ArrayList<DotnAdapted> makeCylinder2(float d_0, float px, float py, float pz, float radius, float height) {
        ArrayList<DotnAdapted> ans = new ArrayList<DotnAdapted>();
        for (float z=pz-height/2; z<pz+height/2; z=z+d_0) {
            // We put points around a circle of radius = radius.cos(lat)
            float R=(float) radius;
            float N=(int)(java.lang.Math.PI*2.0*R/d_0);
            float dAngle=(float) (java.lang.Math.PI*2.0/N);
            for (float i=0;i<N;i++) {
                Vector3D normal = new Vector3D((float)(R*java.lang.Math.sin(i*dAngle)),
                        (float)(R*java.lang.Math.cos(i*dAngle)),
                        0);
                Vector3D pos = new Vector3D(px+normal.x,
                        py+normal.y,
                        z);
                DotnAdapted nd=new DotnAdapted(pos,normal);
                ans.add(nd);
            }
        }
        return ans;
    }
    
    /*
     * ------------------- Methods and attributes used to avoid conflicts between segmentation threads and 3D viewer thread
     */
     public boolean notifyCellRendererCellsModif2;		// Flags any change of allCells2 to the 3D Viewer (JOGLCellRenderer)
															// When notified, the renderer asks the segmentation thread to give his updated data
     private boolean bufferHasBeenFilled;

    /**
     * 	Function that triggers an update of the 3D viewer
     *  - Located in this class because this has to be done in the thread of the Optimizer, 
     *  and not in the thread of the viewer...
     *  a bit ugly though
     */
    public  void requestFillBufferCellRenderer2() {
        notifyCellRendererCellsModif=false;
        if (OptimizerIsRunning2) {            
            Opt2.requestFillBufferRenderer=true; //System.out.println("La seg tourne, on demande de remplir le buffer");
        } else {
            fillBufferCellRenderer(); //System.out.println("La seg tourne pas, on appelle le remplissage de buffer");
        }        
    }    
    
    /*
     *  Helper function for 3D viewer 
     */
    public  boolean getBuffFilled2() {
        return bufferHasBeenFilled;
    }
    
    /*
     * Helper function for 3D viewer
     */
    public  void setBuffFilled2(boolean flag) {
        bufferHasBeenFilled=flag;
    }
    
    /*
     * Helper function for 3D viewer
     */
    public void fillBufferCellRenderer2() {
        jcr2.fillBufferCellRenderer_PC();
        jcr2.fillBufferCellRenderer_TR();
    }
    
    public  void fillBufferCellRenderer2(ArrayList<DotnAdapted> aList) {
        jcr2.fillBufferCellRenderer_PC(aList);
    }
    
    /*
     * ------------------- End of methods and attributes used to avoid conflicts between segmentation threads and 3D viewer thread
     */
    
    /**
     * adds a cell timepoint to list of points which will be overlayed
     * @param ct cell timepoint to add
     */
     public void addToOverlay2(cellTAdapted ct) {
        for (int i=0;i<ct.dots.size();i++) {
            DotnAdapted dn= ct.dots.get(i);
            this.dots_to_overlay2.add(dn);           
        }
    }
    
    /**
     * adds a cell to list of points which will be overlayed (i.e. all associated cell timepoints)
     * @param c cell timepoint to add
     */
     public void addToOverlay2(cellAdapted c) {
        for (int i=0;i<c.cellTs.size();i++) {
            cellTAdapted ct= c.cellTs.get(i);
            addToOverlay2(ct);
        }
    }  
    
    /**
     * adds all points of all all cellt found to be at the specified frame
     * @param frame
     */
    public void addToOverlay2(int frame) {
        for (int i=0;i<allCells2.size();i++) {
            cellAdapted c= allCells2.get(i);
            cellTAdapted ct = c.getCellTAt(frame);
            if (ct!=null) {
                addToOverlay2(ct);
            }
        }
    }  
    
    /**
     * Sets the image which will be used by the Optimizer
     * @param imageName image which has to be found within the  WindowManager
     */
    @IJ1ScriptableMethod(target=STATE, ui="ImageChooser", tt="(String imageName)", pr=-2)
    public  void setWorkingImage2(String imageName) {
    	System.out.println("imageName="+imageName);
   		ImagePlus imtest = WindowManager.getImage(imageName);
   		if (imtest!=workingImP) {
   			workingImP=imtest;
   			limeSegAdapted.notifyCellExplorerCellsModif=true;
   			updateWorkingImage();    
   		}
    }
    
     void updateWorkingImage() {
    	if ((!OptimizerIsRunning2)&&(workingImP!=null)) {
            setWorkingImage(workingImP,currentChannel,currentFrame);
    	} else {
    		//IJ.log("Cannot change image : the Optimizer is running");
    	}
    }
    
    /**
     * Sets image to be used by the Optimizer using imageID IJ1 reference
     * Can affect to currentframe and currentchannel synchronization
     * @param imageID
     * @param channel
     * @param frame
     */
    public void setWorkingImage2(int imageID, int channel, int frame) {
        setWorkingImage(WindowManager.getImage(imageID),channel, frame);
    }
    
    /**
     * Sets image to be used by the Optimizer using ImagePlus IJ1 reference
     * Can affect to currentframe and currentchannel synchronization
     * @param img The image (ImagePlus) on which the Optimisation will be run
     * @param channel Channel chosen (IJ1 notation)
     * @param frame Frame chosen (IJ1 notation)
     */
    public void setWorkingImage2(ImagePlus img, int channel, int frame) {        
        Opt2.setWorkingImage(img, channel, frame);  
        workingImP = img;
    }
    
    /**
     * Sets image to be used by the Optimizer using RandomAccessibleInterval ImgLib2 Object
     * Can affect to currentframe and currentchannel synchronization
     * @param img RandomAccessibleInterval chosen for segmentation (5D)
     * @param channel Channel chosen
     * @param frame Frame chosen
     */
   public void setWorkingImage2(RandomAccessibleInterval img, int channel, int frame) {        
        if (!OptimizerIsRunning2) {
        	Opt2.setWorkingImage(img, channel, frame);  
        	workingImP = null;
        }
    }
    
    /**
     * Adds a cell in the 3D viewer
     * @param c cell to be displayed by the 3D viewer
     */
    public void putCellTo3DDisplay2(cellAdapted c) {
        make3DViewVisible();
        jcr2.addCellToDisplay(c);
        notifyCellRendererCellsModif=true;
    }    
    
    /**
     * Displays a swing table with current Cells and corresponding CellTs present in LimeSeg
     */
    @IJ1ScriptableMethod(target=STATE, ui="STD", pr=-3)
     public void showTable2() {
    	//System.out.println("Unsupported showTable operation");
        if (cExplorer==null) {cExplorer=new JTableCellsExplorer();//workingImP);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        cExplorer.setVisible(true);
                    }
                });
        } else {
            cExplorer.setVisible(true);
        }
    }
    
    //----------------- Optimizer
    
     ArrayList<cellTAdapted> savedCellTInOptimizer = new ArrayList<>(); // For Optimizer cancellation
    
    /**
     * UNSTABLE WITH GPU MODE - save the state of the Optimizer 
     * - can be restored with restoreOptState method 
     */
   // @IJ1ScriptableMethod(target=this.Opt2, ui="STD", tt="()", pr=1)
     public void saveOptState2() {
    	if (Opt2!=null) {
    		//savedOptDots.clear();
    		savedCellTInOptimizer.clear();
    		for (cellTAdapted ct:Opt2.cellTInOptimizer) {
    			 cellTAdapted nct = ct.clone();
    			savedCellTInOptimizer.add(nct);
    		}
    	}
    }
    
    /**
     * UNSTABLE WITH GPU MODE - restore the state of the Optimizer 
     * that was saved with saveOptState command 
     */
   // @IJ1ScriptableMethod(target=this.Opt2, ui="STD", tt="()", pr=1)
     public void restoreOptState2() {
    	Opt2.freeGPUMem();    	
    	if ((Opt2!=null)) {
    		ArrayList<CellT> ctInOptimizer = Opt2.cellTInOptimizer;    		
    		for (CellT ct:ctInOptimizer) {
    			Cell c = ct.c;
    			CellT ctToRemove = ct.c.getCellTAt(ct.frame);
    			System.out.println("c = "+ct.c.id_Cell);
    			ct.c.cellTs.remove(ctToRemove);
    			if (ct.c.cellTs.size()==0) {
    				boolean remove=true;
    				for (CellT ct2:savedCellTInOptimizer) {
    					if (ct.c==ct2.c) {
    						remove=false;
    					}    					
    				}
    				if (remove) {this.allCells2.remove(c);}
    			}
    		}
    		limeSegAdapted.clearOptimizer();    		

    		for (cellTAdapted ct:savedCellTInOptimizer) {
    			for (DotnAdapted dn:ct.dots) {
    				dn.ct=ct;
    			}
    			cellTAdapted ctToRemove = ct.c.getCellTAt(ct.frame);
    			ct.c.cellTs.remove(ctToRemove);
    			ct.c.cellTs.add(ct);
    			Opt2.addDots(ct);
    		}
    		limeSegAdapted.notifyCellExplorerCellsModif=true;
    		limeSegAdapted.notifyCellRendererCellsModif=true;
    	}
    }
    
    /**
     * Put all cellt from cells in the Optimizer with respect of currentFrame and currentChannel 
     */
    //@IJ1ScriptableMethod(target=this.Opt2, ui="STD", tt="()", pr=6)
     public void putallCells2ToOptimizerRespectChannel2() {
    	for (cellAdapted c:allCells2) {
    		if ((c.cellChannel==limeSegAdapted.currentChannel)&&(c.getCellTAt(limeSegAdapted.currentFrame)!=null)) {
    			this.Opt2.addDots(c.getCellTAt(limeSegAdapted.currentFrame));    		
    		}
    	}
    }
    
    /**
     * Set the working channel for 5D images
     * @param cChannel
     */
    @IJ1ScriptableMethod(target=STATE, ui="STD", tt="(int cChannel)", pr=1)
     public void setCurrentChannel2(int cChannel) {
    	currentChannel=cChannel;
    	updateWorkingImage();
    }
    
    /**
     * Set the spacing ratio between Z spacing and XY spacing (X and Y should be isotropic)
     * For instance if the image was acquired with pixels spacing = 0.2 um and z spacing = 0.6
     * zscale should be equal to 0.6/0.2 = 3
     * @param zscale
     */
    @IJ1ScriptableMethod(target=STATE, ui="STD", tt="(float zscale) // ratio between z spacing and xy spacing (x and y are assumed to isotropic). <br>"
    		+ "For instance if x and y pixel size are 0.2 microns and z sampling is 0.8 microns, then this property should be set to 0.8/0.2 = 4. ", pr=-1)
     public void setZScale2(float zscale) {
        Opt2.setOptParam("ZScale", zscale);
    }
    
    /**
     * Set the bounding box of the Optimizer. 
     * A string should be given formatted as : xmin, xmax, ymin, ymax, zmin, zmax
     * Units are pixels for xy and slice index for z : xmin, xmax
     * @param str
     */
    //@IJ1ScriptableMethod(target=this.Opt2, ui="STD", tt="(String str) // Format MinX,MaxX,MinY,MaxY,MinZ,MaxZ", pr=4)
     public void setOptBounds2(String str) {
        String[] parts=str.substring(0, str.length()).split(",");
            if (parts.length==6){
                int xmin=Integer.parseInt(parts[0]);                    
                int xmax=Integer.parseInt(parts[1]);
                int ymin=Integer.parseInt(parts[2]);                    
                int ymax=Integer.parseInt(parts[3]);
                int zmin=Integer.parseInt(parts[4]);                    
                int zmax=Integer.parseInt(parts[5]);
                Opt2.setOptParam("MinX", xmin);
                Opt2.setOptParam("MaxX", xmax);
                Opt2.setOptParam("MinY", ymin);
                Opt2.setOptParam("MaxY", ymax);
                Opt2.setOptParam("MinZ", zmin);
                Opt2.setOptParam("MaxZ", zmax);
            }
    }
    
    /**
     * Put all cellt from cells in the Optimizer with respect to currentFrame
     */
    //@IJ1ScriptableMethod(target=this.Opt2, ui="STD", pr=2)
     public void putAllCellTsToOptimizer2() {
        for (int i=0;i<allCells2.size();i++) {
            currentCell2 = allCells2.get(i);
            putCurrentCellTToOptimizer();
        }
    }
    
    /**
     * Clears all dots contained in the Optimizer
     */
    //@IJ1ScriptableMethod(target=this.Opt2, ui="STD", pr=2)
     public void clearOptimizer2() {
        if (Opt2!=null) {
        	Opt2.dots.clear();// = new ArrayList<>();//.clear();
        	Opt2.cellTInOptimizer.clear();
        }
    }
    
    /**
     * Asynchroneous request to stop the Optimizer
     */
    //@IJ1ScriptableMethod(target=this.Opt2, ui="STD", pr=1)
     public void stopOptimisation2() {
        requestStopOptimisation = true;
    }
    
    /**
     * Sets Optimizer parameters
     * Default values are put in paranthesis
     * Parameters are :
     *  - paramName : 
     *  		description 
     *  		(default_value; [min...max])
     *  - "d_0": 
     *  		equilibrium distance between surfels d_0 
     *  		(2; [0.5... ])
     *  - "normalForce": 
     *  		pressure applied on each surfel 
     *  		(0; [-0.04...0.04])
     * 	- "ka": 
     * 			amplitude of attractive force between surfels pair 
     * 			(when d above d_0)
     * 			(0.01)
     *  - "pa": 
     *  		power of attractive force 
     *  		(5)
     *  - "pr": 
     *  		power of repulsive force 
     *  		(9) 
     *  - "k_grad": 
     *  		amplitude of attractive force towards image maximum 
     *  		(0.03)
     *  - "k_bend":
     *  		(0.1) 
     *  - "k_align":
     *  		(0.05)
     *  - "fillHoles":
     *  		0 : no new surfel is generated
     *  		1 : if number of neighbors == generateDotIfNeighborEquals then a new surfel is generated
     *  		(1)
     *  - "rmOutliers":
     *  		0 : no surfel is removed
     *  		1 : if number of neighbors not in [rmIfNeighborBelow...rmIfNeighborAbove] then the surfel is removed
     *  		(1)
     *  - "attractToMax":
     *  		0 : no influence of the image (local maximum is not searched)
     *  		1 : surfels are attracted to local maxima
     *  		(1)
     *  - "radiusTresholdInteract":
     *  		in units of d_0, radius of surfels sphere of influence
     *  		(1.75)
     *  - "NStepPerR0":
     *  		sampling of f_dist
     *  		(5000)
     *  - "maxDisplacementPerStep":
     *  		in units of d_0, maximum displacement of surfels between two steps
     *  		(0.3)
     *  - "ageMinGenerate":
     *  		number of equilibrium iteration steps before a surfel is "active" (i.e. can be removed or generate a new one)
     *  		(10)
     *  - "rmIfNeighborBelow":
     *  		lower threshold to remove a surfel
     *  		(5)
     *  - "rmIfNeighborAbove":
     *  		high threshold to remove a surfel
     *  		(11)
     *  - "generateDotIfNeighborEquals":
     *  		if number of neighbors == generateDotIfNeighborEquals
     *  		then the surfel generated a new surfel
     *  		(6)
     *  - "radiusSearch":
     *  		in number of pixels, distance over which a local image maximum is search for
     *  		(5)
     *  - "radiusRes":
     *  		in number of pixels, sampling (=step size) over which maximum is looked for
     *  		(0.5) 
     *  - "radiusDelta":
     *  		in number of pixels along the normal vector, shift between the surfel position and the image range search
     *  		(0) 
     *  - "searchMode":
     *  		for future features (now: 0 = max; expect 1 = min, 2 = grad ...)
     *  		(0)
     *  - "convergenceTimestepSampling":
     *  		number of iterations performed before looking for convergence
     *  		(20)
     *  - "convergenceDistTreshold":
     *  		in d_0 units, distance traveled between two convergence steps below which is surfel is considered as having converged
     *  		(provided norm is also ok)
     *  		(0.1)
     *  - "convergenceNormTreshold":
     *  		if norm (change of normal) below threshold then the surfel could have converged
     *  		(provided dist is also ok)
     *  		(0.1) 
     *  - "radiusRelaxed":
     *  		if number of pixels, distance between the surfel and the image maximum below which
     *  		the surfel is relaxed (i.e. Fpressure and Fsignal are ignored = no normal force exerted)  
     *  		(1)
     *  - "ZScale":
     *  		ratio between Z spacing and XY spacing 
     *  		for instance, if Z slice spacing = 1 um and pixels of camera = 0.2 um, zscale = 1/0.2 = 5 
     *  		(1)
     *  - "MinX":
     *  		x minimal position of surfels (pixel)
     *  		(set by image dimension)
     *  - "MaxX":
     *  		x maximal position of surfels (pixel)
     *  		(set by image dimension)
     *  - "MinY":
     *  		y minimal position of surfels (pixel)
     *  		(set by image dimension)
     *  - "MaxY":
     *  		y maximal position of surfels (pixel)
     *  		(set by image dimension)
     *  - "MinZ":
     *  		minimal z slice position of surfels
     *  		(set by image dimension)
     *  - "MaxZ":
     *  		maximal z slice position of surfels
     *  		(set by image dimension)
     * @param paramName
     * @param value
     */
    //@IJ1ScriptableMethod(target=this.Opt2, ui="STD", tt="(String paramName, double value)", pr=3)
     public void setOptimizerParameter2(String paramName, double value) {
    	assert Opt2!=null;
        Opt2.setOptParam(paramName, (float)value);
    } 
    
    /**
     * Get current Optimizer parameter (see setOptimizerParameter)
     * @param paramName
     * @param value is a table to fit with IJ1 macroextension
     */
    //@IJ1ScriptableMethod(target=this.Opt2, tt="(String paramName, Double value)")
     public void getOptimizerParameter2(String paramName, Double[] value) {
    	assert Opt2!=null;
        value[0]=Opt2.getOptParam(paramName);
    } 
    
    /**
     * Get current Optimizer parameter (see setOptimizerParameter)
     * @param paramName
     * @return
     */
    public double getOptParam(String paramName) {
    	assert Opt2!=null;
        return Opt2.getOptParam(paramName);
    }
    
    /**
     * Runs NStep of iteration for the Optimizer
     * Stops before if:
     * 	- all surfels have converged
     *  - requestStopOptimisation is set to true
     *  - no dots is present anymore
     *  if NStep is set to negative values, then the Optimizer ignores NStep
     * @param NStep
     * @return true is the Optimizer has converged, false otherwise
     */
    //@IJ1ScriptableMethod(target=this.Opt2, tt="(int NStep)", ui="STD", pr=-5, newThread=true)
     public int runOptimisation2(int NStep) {
        boolean hasConverged=false;
        if (OptimizerIsRunning2) {
           //IJ.log("Cannot run Optimisation : it is already running");
        } else {
            Opt2.setCUDAContext();
            long tInit=System.currentTimeMillis();
            OptimizerIsRunning2 = true;
            int i=0;
            while (((i<NStep)||(NStep<0))&&(requestStopOptimisation==false)&&(Opt2.dots.size()>0))  {
                Opt2.nextStep();
                notifyCellRendererCellsModif=true;
                if (Opt2.getRatioOfDotsConverged()==1f) {
                     System.out.println("Everything has converged in "+i+" steps.");  
                     hasConverged=true;
                     break;
                }
                i++;
            }            
            OptimizerIsRunning2 = false;
            requestStopOptimisation = false;
            Opt2.freeGPUMem();
            long tEnd=System.currentTimeMillis();
            System.out.println("Optimization time = \t "+((tEnd-tInit)/1000)+" s");
        }
        return hasConverged?1:0;
    }
    
    //----------------- 2D View
    /**
     * Put current cell to overlays (requires updateOverlay to be effective)
     */
    @IJ1ScriptableMethod(target=VIEW_2D, ui="STD", pr=0)
     public void putCurrentCellToOverlay2() { 
    	if (currentCell2!=null) {
            addToOverlay2(currentCell2);
    	}    			
    }
    
    /**
     * Put dots of current user selected slice to overlays (requires updateOverlay to be effective)
     */
    @IJ1ScriptableMethod(target=VIEW_2D, ui="STD", pr=0)
     public void putCurrentSliceToOverlay2() { 
    	if (workingImP!=null) {
			float ZS=(float) Opt2.getOptParam("ZScale");
			int zSlice; //= workingImP.getZ();

	        if ((workingImP.getNFrames()!=1)||(workingImP.getNChannels()!=1)) {
	        	zSlice = workingImP.getZ();
	        } else {
	        	zSlice = workingImP.getSlice();
	        }
	    	for (cellAdapted c:allCells2) {
	    		cellTAdapted ct = c.getCellTAt(this.currentFrame2);
	    		if (ct!=null) {
	    			for (DotnAdapted dn:ct.dots) {
	    				if ((int)(dn.pos.z/ZS)==zSlice-1) {
	    					this.dots_to_overlay2.add(dn);
	    				}
	    			}
	    		}
	    	}
    	}
    }
    
    @IJ1ScriptableMethod(target=VIEW_2D, ui="STD", pr=0)
     public void putCurrentTimePointToOverlay2() { 
    	if (workingImP!=null) {
	    	for (cellAdapted c:allCells2) {
	    		cellTAdapted ct = c.getCellTAt(this.currentFrame2);
	    		if (ct!=null) {
	    			for (DotnAdapted dn:ct.dots) {
	    					this.dots_to_overlay2.add(dn);
	    			}
	    		}
	    	}
    	}
    }
    
    /**
     *  Clears image overlay (requires updateOverlay to be effective)
     */
    @IJ1ScriptableMethod(target=VIEW_2D, ui="STD", pr=0)
     public void clearOverlay2() {
        if (dots_to_overlay!=null)
            dots_to_overlay.clear();
    }
    
    /**
     * Adds all cells into image overlay (requires updateOverlay to be effective
     */
    @IJ1ScriptableMethod(target=VIEW_2D, ui="STD", pr=1)
     public void addallCells2ToOverlay2() {
        for (int i=0;i<allCells2.size();i++) {
            cellAdapted c= allCells2.get(i);
            addToOverlay2(c);
        }
    } 
    
    /**
     * updates Overlay of the working image with registeres dots to be overlayed
     */
    @IJ1ScriptableMethod(target=VIEW_2D, ui="STD", pr=2)
     public void updateOverlay2() {
        Overlay ov = new Overlay();
        if (workingImP!=null) {
	        workingImP.setOverlay(ov);
	        Iterator<DotnAdapted> i=dots_to_overlay2.iterator();
	        float ZS=(float) Opt2.getOptParam("ZScale");
	        if ((workingImP.getNFrames()!=1)||(workingImP.getNChannels()!=1)) {
	            while (i.hasNext()) {
	                DotnAdapted nd = i.next();
	                PointRoi roi;
	                roi = new PointRoi(nd.pos.x,nd.pos.y);//,c);
	                Color color = new Color((int)(nd.ct.c.color[0]*255),(int)(nd.ct.c.color[1]*255),(int)(nd.ct.c.color[2]*255));
	                roi.setStrokeColor(color);
	                int zpos=1+(int)(nd.pos.z/ZS);
	                if ((zpos>0)&&(zpos<=workingImP.getNSlices())) {
	                    roi.setPosition(nd.ct.c.cellChannel, zpos, nd.ct.frame);
	                    ov.addElement(roi); 
	                }   
	            }   
	        } else {
	            while (i.hasNext()) {
	                DotnAdapted nd = i.next();
	                PointRoi roi;
	                roi = new PointRoi(nd.pos.x,nd.pos.y);//,c);   
	                Color color = new Color((int)(nd.ct.c.color[0]*255),(int)(nd.ct.c.color[1]*255),(int)(nd.ct.c.color[2]*255));
	                roi.setStrokeColor(color);
	                int zpos=1+(int)((float) (nd.pos.z)/(float) (ZS));
	                if ((zpos>0)&&(zpos<=workingImP.getNSlices())) {
	                    roi.setPosition(zpos);
	                    ov.addElement(roi);  
	                }
	            }
	        }
	        workingImP.updateAndDraw();
        }
    } 
    
    //----------------- 3D View
    /**
     * Updates the 3D viewer:
     * 	- triggers a notification that updates the buffered dots in 3D
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", pr=2)
     public void update3DDisplay2() {
        notifyCellRendererCellsModif=true;
    }
    
    /**
     * Displays the 3D viewer
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", newThread=true, pr=0)
     public void make3DViewVisible2() {
        if (jcr2==null) {           
        	try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            jcr2 = new JOGL3DCellRenderer();
            jcr2.launchAnim();
        } else {
            if (jcr2.glWindow.isVisible()==false) {
                jcr2.glWindow.setVisible(true);
                jcr2.animator.start();
            }
        }
    }
    
    /**
     * Puts current cell to 3D display
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", pr=2)
     public void putCurrentCellTo3DDisplay2() {
    	if (jcr2!=null) {
    		putCellTo3DDisplay2(currentCell2);
    	}
    }

    /**
     * Updates the 3D viewer:
     * 	- puts the cell color according to their default color defined in the Cell class
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", pr=2)
     public void setDefaultColor3DDisplay2() {
        make3DViewVisible();
        this.jcr2.colorSupplier = new DotNColorSupplier();
        notifyCellRendererCellsModif=true;
    }

    /**
     * Updates the 3D viewer:
     * 	- displays currentCell in Green, others in Red
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", pr=2)
     public void setCurrentCellColorLUT2() {
        make3DViewVisible();
        this.jcr2.colorSupplier = new CurrentCellColorLUT();
        notifyCellRendererCellsModif=true;
    }
    
    /**
     * Sets the center of the 3D viewer
     * @param px
     * @param py
     * @param pz (in slice number / corrected by ZScale)
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", tt="(float px,float py, float pz)", pr=6)
     public void set3DViewCenter2(float px, float py, float pz) {
    	//make3DViewVisible();
    	if (jcr2!=null) {
    		jcr2.lookAt.x=px;            
        	jcr2.lookAt.y=py;            
        	jcr2.lookAt.z=pz*(float)Opt2.getOptParam("ZScale"); 
    	}
    }
    
    /**
     * Sets the rotation ( point of view of the 3D viewer)
     * @param rx
     * @param ry
     * @param rz
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", tt="(float rx,float ry, float rz)", pr=7)
     public void set3DViewRot2(float rx, float ry, float rz) {
    	//make3DViewVisible();            
        if (jcr2!=null) {
        	jcr2.view_rotx=rx;            
        	jcr2.view_roty=ry;            
        	jcr2.view_rotz=rz;
        }
    }  
    
    /**
     * Clears all objects in the 3D display
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", pr=3)
     public void clear3DDisplay2() {    	
        if (jcr2!=null) {
        	notifyCellRendererCellsModif=true;
        	jcr2.clearDisplayedCells(); 
        }
    }
    
    /**
     * Puts all the cells in the 3D display
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", pr=1, newThread=true)
     public void putallCells2To3DDisplay2() {
        //make3DViewVisible();
        if ((jcr2!=null)&&(allCells2!=null)) {
    	for (int i=0;i<allCells2.size();i++) {
            cellAdapted c= allCells2.get(i);
            jcr2.addCellToDisplay(c);
        }
        notifyCellRendererCellsModif=true;
        }
    }
    
    /**
     * Sets 3D View mode:
     * @param vMode
     * 	0 : Full view
     *  1 : Shows only dots below the displayed slice  
     *  2 : Shows only dots above the displayed slice  
     *  3 : Shows only 9dots within the displayed slice
     *  
     * 	8 : Full view of dots within the Optimizer
     *  9 : Shows only dots of the Optimizer below the displayed slice  
     *  10 : Shows only dots of the Optimizer above the displayed slice  
     *  11 : Shows only dots of the Optimizer within the displayed slice
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", tt="(int vMode)", pr=4)
     public void set3DViewMode2(int vMode) {
    	if (jcr2!=null) {
    		jcr2.setViewMode(vMode);
    	}
    }
    
    /**
     * Sets the zoom value of the 3D Viewer
     * @param zoom
     */
    @IJ1ScriptableMethod(target=VIEW_3D, ui="STD", tt="(float zoom)", pr=5)
     public void set3DViewZoom2(float zoom) {
    	if (jcr2!=null) {
    		jcr2.RatioGlobal=zoom;
    	}
    }
	
    //----------------- Current Cell
    /**
     * Sets cell display mode : dots or triangles (if tesselated)
     * @param vMode
     * 0: dots
     * 1: mesh
     */
    @IJ1ScriptableMethod(target=VIEW_3D+TS+CURRENT_CELL, ui="STD", tt="(int vMode)", pr=8)
	 public void setCell3DDisplayMode2(int vMode) {
		if (currentCell2!=null) {   
			currentCell2.display_mode=vMode;
			notifyCellRendererCellsModif=true;
		}
	}
    
    /**
     * Gives a string identifier to the currentCell2
     * @param id
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, ui="STD", tt="(String id)", pr=0)
	 public void setCellId2(String id) {
    	if (currentCell2!=null) {
		    currentCell2.id_Cell = id;
		    notifyCellExplorerCellsModif=true;
    	}
	}
	
    /**
     * Creates a new cell
     */
	@IJ1ScriptableMethod(target=STATE, ui="STD", pr=2)
	 public void newCell2() {
		System.out.println("Este el cc2:");
		System.out.println(this.currentChannel2);
	    currentCell2 = new cellAdapted(this.currentChannel2,this);        
	    allCells2.add(currentCell2);          
	    notifyCellExplorerCellsModif=true;
	    notifyCellRendererCellsModif=true;
        checkSelectColorLUT();
	}
    
	/**
	 * Select cell by its index
	 * @param index
	 */
	@IJ1ScriptableMethod(target=STATE, ui="STD", tt="(int index)", pr=3)
	 public void selectCellByNumber2(int index) {
   		if ((index>=0)&&(index<allCells2.size())) {
            currentCell2 = allCells2.get(index);
            checkSelectColorLUT();
        }
   	}

    /**
     * Select next cell, according to index
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, ui="STD", tt="()", pr=3)
     public void selectNextCell2() {
        if (currentCell2!=null) {
            int cIndex = allCells2.indexOf(currentCell2);
            if (cIndex!=-1) { // object found
                if (cIndex<allCells2.size()-1) {
                    currentCell2 = allCells2.get(cIndex+1);
                    notifyCellExplorerCellsModif=true;
                    checkSelectColorLUT();
                } else {
                    currentCell2 = allCells2.get(0);
                    if (allCells2.size()>1) {
                        notifyCellExplorerCellsModif=true;
                        checkSelectColorLUT();
                    }
                }
            }
        }
    }

    public  void checkSelectColorLUT2() {
        if (jcr2!=null) {
            if (jcr2.colorSupplier instanceof CurrentCellColorLUT) {
                notifyCellRendererCellsModif=true;
            }
        }
    }

    /**
     * Select next cell, according to index
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, ui="STD", tt="()", pr=3)
     public void selectPreviousCell2() {
        if (currentCell2!=null) {
            int cIndex = allCells2.indexOf(currentCell2);
            if (cIndex!=-1) { // object found
                if (cIndex>0) {
                    currentCell2 = allCells2.get(cIndex-1);
                    notifyCellExplorerCellsModif=true;
                    checkSelectColorLUT();
                } else {
                    currentCell2 = allCells2.get(allCells2.size()-1);
                    if (allCells2.size()>1) {
                        notifyCellExplorerCellsModif=true;
                        checkSelectColorLUT();
                    }
                }
            }
        }
    }

	/**
	 * Set color of current cell
	 * @param r
	 * @param g
	 * @param b
	 * @param a (alpha channel currently unsupported)
	 */
    @IJ1ScriptableMethod(target=CURRENT_CELL, ui="STD", tt="(float r, float g, float b, float a) : Set cell color (parameters are between 0 and 1).", pr=2)
     public void setCellColor2(float r, float g, float b, float a) {
    	if (currentCell2!=null) {
            currentCell2.color[0]=r;
            currentCell2.color[1]=g;
            currentCell2.color[2]=b;
            currentCell2.color[3]=a;
        }
	    notifyCellRendererCellsModif=true;
    }
    
    /**
     * !Warning! removes all cells of LimeSeg
     */
    @IJ1ScriptableMethod(target=STATE, ui="STD", tt="")
     public void clearallCells22() {
    	clearCell("ALL");
    	clearOverlay();
    }
    
    /**
     * Clear the cell named by arg from LimeSeg
     * @param arg
     */
    @IJ1ScriptableMethod(target=STATE, tt="(String arg)")
     public void clearCell2(String arg) {        
        if (arg.toUpperCase().equals("ALL")) {
            if (allCells2!=null) {
                allCells2.clear();
                if ((jcr2 != null)) {
                    jcr2.clearDisplayedCells();
                }
                notifyCellExplorerCellsModif = true;
                notifyCellRendererCellsModif = true;
            }
        } else if (findCell(arg)!=null) {
            cellAdapted c = findCell(arg);           
            if (c!=null) {
                allCells2.remove(c);                
                if ((jcr2!=null)) {jcr2.removeDisplayedCell(c);}
                notifyCellExplorerCellsModif=true;
                notifyCellRendererCellsModif=true;
            }
        } else {
            cellAdapted c = currentCell2;            
            if (c!=null) {
                allCells2.remove(c);
                if ((jcr2!=null)) {jcr2.removeDisplayedCell(c);}
                notifyCellExplorerCellsModif=true;
                notifyCellRendererCellsModif=true;
            }
            currentCell2=null;
        }            
    }
    
    /**
     * Clears current Cell
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, ui="STD", tt="")
     public void clearCurrentCell2() {
    	clearCell("");      
    }
    
    /**
     * Sets cell named by id as the current Cell
     * @param id
     */
    @IJ1ScriptableMethod(target=STATE, ui="STD", tt="(String id)", pr=4)
     public void selectCellById2(String id) {
    	currentCell2=(cellAdapted) findCell(id);
        checkSelectColorLUT();
    }

    //----------------- Current CellT
    /**
     * If a cell timepoint from a cell has been tesselated, value contains the surface of the cell
     * value is a table because of IJ1 macroextension compatibility
     * @param value
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, tt="", pr=0)
     public void getCellSurface2(Double[] value) {
		value[0]=(double)0;
    	if (currentCell2!=null) {
    		if (currentCell2.getCellTAt(currentFrame)!=null) {
    			if (currentCell2.getCellTAt(currentFrame).dots!=null) {
    				cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
    				if (ct.tesselated) {
    					value[0] = ct.getSurface();
    				} else {
    					value[0] = Double.NaN;
    				}
    			} else {
    				value[0]=0.0;
    			}
    		}
    	}
    }
    
    /**
     * If a cell timepoint from a cell has been tesselated, value contains the volume of the cell
     * value is a table because of IJ1 macroextension compatibility
     * @param value
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, tt="", pr=0)
     public void getCellVolume2(Double[] value) {
		value[0]=(double)0;
    	if (currentCell2!=null) {
    		if (currentCell2.getCellTAt(currentFrame)!=null) {
    			if (currentCell2.getCellTAt(currentFrame).dots!=null) {
    				cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
    				if (ct.tesselated) {
    					value[0] = ct.getVolume();
    				} else {
    					value[0] = Double.NaN;
    				}
    			} else {
    				value[0]=0.0;
    			}
    		}
    	}
    }
    
    /**
     * Set current working frame
     * @param cFrame
     */
    @IJ1ScriptableMethod(target=STATE, ui="STD", tt="(int cFrame)", pr=0)
     public void setCurrentFrame2(int cFrame) {
    	currentFrame=cFrame;
    	updateWorkingImage();
    }
    
    /**
     * Puts the cellt of currentCell at currentFrame into the Optimizer
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, ui="STD", pr=0)
     public void putCurrentCellTToOptimizer2() {
        if ((Opt2!=null)&&(currentCell2!=null)) {
        	Opt2.addDots(currentCell2.getCellTAt(currentFrame)); 
        }
    }
    
    /**
     * Constructs the mesh of the current Cell at the current frame
     * @return the number of free edges (hopefully 0)
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, ui="STD", pr=4)
     public int constructMesh2() {
    	//System.out.println("Unsupported tesselation operation");
    	//return 0;
        int ans=-1;
        if (currentCell2!=null) {
            cellTAdapted ct=currentCell2.getCellTAt(currentFrame);
            if (ct!=null) {
                ans=ct.constructMesh();
                ct.modified=true;
                this.notifyCellRendererCellsModif2=true;
                this.notifyCellExplorerCellsModif2=true;
            }            
            currentCell2.modified=true;
        }
        return ans;
    }
    
    /**
     * Puts dots of current CellT into the "clipboard"
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, ui="STD", pr=1)
     public void copyDotsFromCellT2() {
        if (currentCell2==null) {
            return;
        }
        cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
        if (ct==null) {
            return;
        }
        ArrayList<DotnAdapted> dTemp=ct.dots;
        copiedDots2=new ArrayList<>();
        for (int i=0;i<dTemp.size();i++){
            DotnAdapted nd = dTemp.get(i);
            DotnAdapted nd_copy = new DotnAdapted(new Vector3D(nd.pos.x,nd.pos.y,nd.pos.z),new Vector3D(nd.Norm.x,nd.Norm.y,nd.Norm.z));
            nd_copy.N_Neighbor=nd.N_Neighbor;
            nd_copy.userDestroyable=nd.userDestroyable;
            nd_copy.userMovable=nd.userMovable;
            nd_copy.userGenerate=nd.userGenerate;
            nd_copy.userRotatable=nd.userRotatable;
            copiedDots2.add(nd_copy);
        }
    }
    
    /**
     * Paste dots of "clipboard" into the current cellt
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, ui="STD", pr=2)
     public void pasteDotsToCell2T() {      
        if (currentCell2==null) {
            return;
        }
        currentCell2.addDots(currentFrame, copiedDots);
        notifyCellExplorerCellsModif=true;
        notifyCellRendererCellsModif=true;
    }
    
    public  int getMaxFrames2() {
    	int maxFrames=1;
    	if (workingImP!=null) {
    		maxFrames = workingImP.getNFrames();
    	}
    	if (allCells2!=null) {
    		for (cellAdapted c:allCells2) {
    			for (cellTAdapted ct:c.cellTs) {
    				if (ct.frame>maxFrames) {
    					maxFrames=ct.frame;
    				}
    			}
    		}
    	}
    	return maxFrames;
    }
    
    /**
     * removes dots of current cell at current frame
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, ui="STD", pr=3)
     public void clearDotsFromCellT2() {
        if (currentCell2==null) {
            return;
        }
        cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
        if (ct==null) {
            return;
        } else {
            ct.tesselated=false;
            ct.triangles=new ArrayList<>();
            ct.dots=new ArrayList<>();
            ct.modified=true;
            currentCell2.modified=true;
        }
        notifyCellExplorerCellsModif=true;
        notifyCellRendererCellsModif=true;
    }
    
	/**
	 * Removes flagged dots of current cell at current frame
	 */
	@IJ1ScriptableMethod(target=CURRENT_CELLT, ui="STD", pr=5)
	 public void removeFlaggedDots2() {
	    Predicate<DotnAdapted> DotnAdaptedPredicate = nd -> (nd.userDefinedFlag);
	    if (currentCell2!=null) {
	    	cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
		    if (ct!=null) {
		        if (ct.dots.stream().anyMatch(DotnAdaptedPredicate)) {
		            ct.dots.removeIf(DotnAdaptedPredicate);
		        }
		    }
            notifyCellExplorerCellsModif=true;
            notifyCellRendererCellsModif=true;
	    }  	                         
	}
    
    //----------------- Current Dot    
	/**
	 * Select the dot number id as current dot
	 * @param id
	 */
	@IJ1ScriptableMethod(target=CURRENT_DOT, ui="STD", tt="(int id)", pr=0)
	 public void selectDot2(int id) {
		DotnAdapted currentDot2 = this.currentCell2.getCellTAt(currentFrame2).dots.get(id);
	}
	/**
	 * Sets current dot normal vector
	 * @param nx
	 * @param ny
	 * @param nz
	 */
	@IJ1ScriptableMethod(target=CURRENT_DOT, ui="STD", tt="(float nx, float ny, float nz)", pr=2)
	 public void setDotnAdaptedorm2(float nx, float ny, float nz) { 
	    if (currentDot!=null) {
	        currentDot.Norm.x=nx;
	        currentDot.Norm.y=ny;
	        currentDot.Norm.z=nz;
	    }   
	}
	/**
	 * Set current dot normal position
	 * @param px
	 * @param py
	 * @param pz
	 */
	@IJ1ScriptableMethod(target=CURRENT_DOT, ui="STD", tt="(float px, float py, float pz)", pr=1)
	 public void setDotPos2(float px, float py, float pz) { 
	    if (currentDot!=null) {
	        currentDot.pos.x=px;
	        currentDot.pos.y=py;
	        currentDot.pos.z=pz;
	    }   
	}
	/**
	 * Set current dot property
	 * @param mov_
	 * @param rot_
	 * @param des_
	 * @param gen_
	 */
	@IJ1ScriptableMethod(target=CURRENT_DOT, ui="STD", tt="(int mov_,int rot_, int des_, int gen)", pr=3)
	 public void setDotProps2(int mov_, int rot_, int des_, int gen_) {
        boolean mov=(mov_==1);
        boolean rot=(rot_==1);
        boolean des=(des_==1);
        boolean gen=(gen_==1);
        if (currentDot!=null) {
            currentDot.userMovable=mov;
            currentDot.userRotatable=rot;
            currentDot.userDestroyable=des;
            currentDot.userGenerate=gen;
        }		
	}
	/**
	 * Set current dot flag. removedflaggeddots can then be used to remove them
	 * @param flag
	 */
	@IJ1ScriptableMethod(target=CURRENT_DOT, ui="STD", tt="(int flag)", pr=4)
	 public void setDotFlag2(int flag) {
	    if (currentDot!=null) {
	        currentDot.userDefinedFlag = (flag==1);
	    }   
	}
	/**
	 * Removes the current dot
	 */
	@IJ1ScriptableMethod(target=CURRENT_DOT, ui="STD", pr=5)
	 public void removeDot2() {		
	    if (Opt2.dots.contains(currentDot)) {
	        Opt2.dots.remove(currentDot);
	        currentCell2.getCellTAt(currentFrame).dots.remove(currentDot);
	        currentDot=null;
	    } else {
	        currentCell2.getCellTAt(currentFrame).dots.remove(currentDot);
	        currentDot=null;
	    }   
	}

	
	//------------- Clipped Dots
    /**
     * normal vectors of clipped dots *=-1
     */
    @IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", pr=6)
     public void invertClippedDotsPolarity2() {
        if (copiedDots2!=null) {
            for (DotnAdapted dn : copiedDots2) {
                dn.Norm.x*=-1;
                dn.Norm.y*=-1;
                dn.Norm.z*=-1;
            }
        }
    }
    /**
     * Set properties of clipped dots (movable, ratable, destroyable, generate
     * @param mov_
     * @param rot_
     * @param des_
     * @param gen_
     */
    @IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", tt="(int mov_,int rot_, int des_, int gen_)", pr=5)
     public void setClippedDotsProps2(int mov_, int rot_, int des_, int gen_) {
        boolean mov=(mov_==1);
        boolean rot=(rot_==1);
        boolean des=(des_==1);
        boolean gen=(gen_==1);
        if (copiedDots!=null)
        for (int i=0;i<copiedDots.size();i++) {
        	DotnAdapted dn=(DotnAdapted) copiedDots2.get(i);
            dn.userMovable=mov;
            dn.userRotatable=rot;
            dn.userDestroyable=des;
            dn.userGenerate=gen;
        }
    }
    /**
     * Translate clipped dots
     * @param tx
     * @param ty
     * @param tz
     */
    @IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", tt="(float tx,float ty,float tz)", pr=4)
     public void translateDots2(float tx,float ty,float tz) {
        float ZS=(float)Opt2.getOptParam("ZScale");
        if (copiedDots2!=null)
        for (int i=0;i<copiedDots2.size();i++) {
                DotnAdapted dn=(DotnAdapted) copiedDots2.get(i);
                dn.pos.x+=tx;
                dn.pos.y+=ty;
                dn.pos.z+=tz*ZS;
            }
    }
    /**
     * Starts a new skeleton
     */
  	@IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", pr=1)
  	 public void newSkeleton2() {
        this.skeleton2 = new Skeleton2D();
    }
  	
  	/**
  	 * Adds current ROI to skeleton
  	 */
  	@IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", pr=2)
  	 public void addRoiToSkeleton2() {
          if (this.skeleton2!=null) {
              Roi roi = workingImP.getRoi();
              addRoiToSkeleton(roi);
          }
    }
    
  	/**
  	 * Adds input roi to skeleton
  	 * @param roi
  	 */
  	 public void addRoiToSkeleton2(Roi roi) {
  		 if (roi!=null) {
             FloatPolygon pol = roi.getFloatPolygon();
             int zpos=roi.getZPosition();      
             if ((workingImP.getNFrames()==1)||(workingImP.getNChannels()==1)) {
                 // Bug ? getZPosition returns always 0
                 zpos = workingImP.getSlice(); // +1 or -1 ?
             }
             PolygonSkeleton polSk = new PolygonSkeleton();
             ArrayList<Vector3D> vp = new ArrayList<>();
             float ZS=(float)Opt2.getOptParam("ZScale");
             for (int i=0;i<pol.npoints;i++) {
                 vp.add(new Vector3D(pol.xpoints[i],pol.ypoints[i],zpos*ZS));
             }
             if (pol.npoints>1) {
                 // closing polygon
                 vp.add(new Vector3D(pol.xpoints[0],pol.ypoints[0],zpos*ZS));
             }
             polSk.setDots(vp);
             skeleton2.pols.add(polSk);
         }
  	}
  	
  	 public void addRoiToSkeleton2(Roi roi, int zpos) {
 		 if (roi!=null) {
            FloatPolygon pol = roi.getFloatPolygon();
            PolygonSkeleton polSk = new PolygonSkeleton();
            ArrayList<Vector3D> vp = new ArrayList<>();
            float ZS=(float)Opt2.getOptParam("ZScale");
            for (int i=0;i<pol.npoints;i++) {
                vp.add(new Vector3D(pol.xpoints[i],pol.ypoints[i],zpos*ZS));
            }
            if (pol.npoints>1) {
                // closing polygon
                vp.add(new Vector3D(pol.xpoints[0],pol.ypoints[0],zpos*ZS));
            }
            polSk.setDots(vp);
            skeleton2.pols.add(polSk);
        }
 	}
  	
  	/**
  	 * Dispatches surfels on skeleton with respect to d_0 value
  	 * And puts them in clipped dots
  	 */
    @IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", pr=3)
     public void generateAndCopySkeletonSurface2() {
          if ((skeleton2!=null)&&(Opt2!=null)) {
              copiedDots=this.skeleton2.getSurface(Opt2.d_0);
          } else {
              //IJ.error("generateAndCopySkeletonSurface: Skeleton or Optimizer (necessary to get r0 parameter) not initialized.");
          }        
    }
      /**
       * Makes a sphere with specified properties and puts it in clipped dots
       * @param px
       * @param py
       * @param pz
       * @param radius
       */
      @IJ1ScriptableMethod(target=CLIPPED_DOTS, ui="STD", tt="(double px, double py, double pz, double radius)", pr=0)
       public void makeSphere2(float px, float py, float pz, float radius) {
          copiedDots = makeSphere((float)Opt2.getOptParam("d_0"),px,py,(pz*(float)Opt2.getOptParam("ZScale")),radius);
      }
      
      /**
       * Makes a sheet of specified properties and puts it in clipped dots
       * @param pxi
       * @param pyi
       * @param pxf
       * @param pyf
       * @param pz
       */
      @IJ1ScriptableMethod(target=CLIPPED_DOTS)
       public void makeXYSheet2(float pxi, float pyi, float pxf, float pyf, float pz) {       
          copiedDots = makeXYSheet((float)Opt2.getOptParam("d_0"), (float)pxi, (float)pyi, (float)pxf, (float)pyf, (float)pz);
      }
    
     
      
    //------------------- I/O
    /**
     * Writes current data in XML/Ply files
     * @param path
     */
    //@IJ1ScriptableMethod(target=IO, ui="PathWriter", tt="(String path)", pr=1)
     public void saveStateToXmlPlyv12(String path) {
        IOXmlPlyLimeSeg.saveState(new limeSegAdapted(), "0.1", path);
    }
    @IJ1ScriptableMethod(target=IO, ui="PathWriter", tt="(String path)", pr=1)
     public void saveStateToXmlPly2(String path) {
        IOXmlPlyLimeSeg.saveState(new limeSegAdapted(), "0.2", path);
    }
    /**
     * Loads current data in XML/Ply files
     * @param path
     */
    @IJ1ScriptableMethod(target=IO, ui="PathOpener", tt="(String path)", pr=0)
     public void loadStateFromXmlPly2(String path) {
    	    if (jcr2!=null) {clear3DDisplay();}            
            IOXmlPlyLimeSeg.loadState(new limeSegAdapted(), path);      
            notifyCellRendererCellsModif=true;
            notifyCellExplorerCellsModif=true;
    }  
    /**
     * get currentChannel parameter (IJ1 macroextension style)    
     * @param value
     */
    //@IJ1ScriptableMethod(target=this.Opt2, tt="(String paramName, Double value)")
     public void getCurrentChannel2(Double[] value) {
        value[0]=(double) currentChannel;
    } 
    
    /**
     * get currentFrame parameter (IJ1 macroextension style)    
     * @param value
     */
    //@IJ1ScriptableMethod(target=this.Opt2, tt="(String paramName, Double value)")
     public void getCurrentFrame2(Double[] value) {
        value[0]=(double) currentFrame;
    }
    
    /**
     * get number of cells present in LimeSeg (IJ1 macroextension style)    
     * @param value
     */
    //@IJ1ScriptableMethod(target=Opt2, tt="(String paramName, Double value)")
     public void getNCells2(Double[] value) {
    	assert allCells2!=null;
        value[0]=(double) allCells2.size();
    } 
    /**
     * Gets 3D view center position (IJ1 macroextension style)
     * @param pX
     * @param pY
     * @param pZ
     */
    @IJ1ScriptableMethod(target=VIEW_3D, tt="(String paramName, Double value)")
     public void get3DViewCenter2(Double[] pX, Double[] pY, Double[] pZ) {        
        make3DViewVisible();
        pX[0]=(double)(jcr2.lookAt.x);
        pY[0]=(double)(jcr2.lookAt.y);
        pZ[0]=(double)(jcr2.lookAt.z/(float)Opt2.getOptParam("ZScale"));
    }
    /**
     * Get current cell color (IJ1 macroextension style)
     * @param r
     * @param g
     * @param b
     * @param a
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, tt="(String paramName, Double value)")
     public void getCellColor2(Double[] r, Double[] g, Double[] b, Double[] a) {
	    if (currentCell2!=null) {
	        r[0] = new Double(currentCell2.color[0]);
	        g[0] = new Double(currentCell2.color[1]);
	        b[0] = new Double(currentCell2.color[2]);
	        a[0] = new Double(currentCell2.color[3]);
    	} else {
	        r[0] = new Double(0);
	        g[0] = new Double(0);
	        b[0] = new Double(0);
	        a[0] = new Double(0);
    	}
    } 
    /**
     * See {@link #set3DViewMode(int)}
     * @param value
     */
    @IJ1ScriptableMethod(target=VIEW_3D, tt="(String paramName, Double value)")
     public void get3DViewMode2(Double[] value) {
        value[0]=(double) jcr2.getViewMode();
    }

    /**
     * Center the 3D Viewer on the current selected cell
     */
    @IJ1ScriptableMethod(target=VIEW_3D,ui="STD", tt="()")
     public void enableTrackCurrentCell2() {
        make3DViewVisible();
        jcr2.trackCurrentCell=true;
    }

    /**
     * Disable centering the 3D Viewer on the current selected cell
     */
    @IJ1ScriptableMethod(target=VIEW_3D,ui="STD", tt="()")
     public void disableTrackCurrentCell2() {
        make3DViewVisible();
        jcr2.trackCurrentCell=false;
    }

    /**
     * See {@link #setCell3DDisplayMode(int)}
     * @param vMode
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL+TS+VIEW_3D, tt="(int vMode)")
     public void getCell3DDisplayMode2(Double[] vMode) {
		if (currentCell2!=null) {
			vMode[0]=(double)currentCell2.display_mode;
		}
	}    
    /**
     * Get current dot pos (IJ1 macro extension style)
     * @param px
     * @param py
     * @param pz
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, tt="(String paramName, Double value)")
     public void getDotPos2(Double[] px, Double[] py, Double[] pz) {
	    if (currentDot!=null) {
	        px[0] = new Double(currentDot.pos.x);
	        py[0] = new Double(currentDot.pos.y);
	        pz[0] = new Double(currentDot.pos.z);///(float)Opt.getOptParam("ZScale"));
    	} else {
	        px[0] = new Double(0);
	        py[0] = new Double(0);
	        pz[0] = new Double(0);
    	}
    }
    /**
     * Get norm of current dot (IJ1 Macro extension style)
     * @param nx
     * @param ny
     * @param nz
     */
    @IJ1ScriptableMethod(target=CURRENT_CELL, tt="(String paramName, Double value)")
     public void getDotnAdaptedorm2(Double[] nx, Double[] ny, Double[] nz) {
	    if (currentDot!=null) {
	        nx[0] = new Double(currentDot.Norm.x);
	        ny[0] = new Double(currentDot.Norm.y);
	        nz[0] = new Double(currentDot.Norm.z);///(float)Opt.getOptParam("ZScale"));
    	} else {
	        nx[0] = new Double(0);
	        ny[0] = new Double(0);
	        nz[0] = new Double(0);
    	}
    }
    /**
     * Gets number of dots contained in current cell at current frame (IJ1 macro extension style)
     * @param value
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, tt="(Double value)")
     public void getNDots2(Double[] value) {
		value[0]=(double)0;
    	if (currentCell2!=null) {
    		if (currentCell2.getCellTAt(currentFrame)!=null) {
    			if (currentCell2.getCellTAt(currentFrame).dots!=null) {
    				value[0]=new Double(currentCell2.getCellTAt(currentFrame).dots.size());
	   			}
    		}
    	}
    }
    /**
     * Get current cell at current frame center
     * @param px
     * @param py
     * @param pz
     */
    @IJ1ScriptableMethod(target=CURRENT_CELLT, tt="(Double[] px, Double[] py, Double[] pz)")
     public void getCellCenter2(Double[] px, Double[] py, Double[] pz) {
        px[0] = new Double(0);
        py[0] = new Double(0);
        pz[0] = new Double(0);
    	if (currentCell2!=null) {
    		if (currentCell2.getCellTAt(currentFrame)!=null) {
    			if (currentCell2.getCellTAt(currentFrame).dots!=null) {
    				cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
    				ct.updateCenter();
    		        px[0] = new Double(ct.center.x);
    		        py[0] = new Double(ct.center.y);
    		        pz[0] = new Double(ct.center.z/(float)Opt2.getOptParam("ZScale"));
    			}
    		}
    	}
    }
    
    /**
     * Displays LimeSeg GUI
     */
	@IJ1ScriptableMethod
	public  void showGUI2() {
		if (jfs3Di==null) {
            jfs3Di=new JFrameLimeSeg(new limeSegAdapted());
            jfs3Di.setVisible(true);
        } else {
            jfs3Di.setVisible(true);            
        } 
	}
	/**
	 * C Elegans segmentation benchmark
	 * !! Takes a huge amount of time to unzip data
	 */
	/*@IJ1ScriptableMethod(target=BENCHMARK, ui="STD", newThread=true) 
	 public void benchCElegans() {			
		SegCElegans.TestSegEmbryoCElegansFull();			
	}*/
	/*
     public String handleExtension2(String name, Object[] args) {  
    	HandleIJ1Extension.handleExtension(name, args);
    	return null;
    }
    */
    
    public DotnAdapted findDotnAdaptedearTo(float px,float py,float pz) {
        DotnAdapted ans=null;        
        if (currentCell2!=null) {
            cellTAdapted ct = currentCell2.getCellTAt(currentFrame);
            if (ct!=null) {
                float minDist=Float.MAX_VALUE;
                Vector3D v = new Vector3D(px,py,pz);
                if (ct.dots.size()>0) {ans=ct.dots.get(0);}
                for (int i=1;i<ct.dots.size();i++) {
                    DotnAdapted dn = ct.dots.get(i);
                    if (Vector3D.dist2(dn.pos, v)<minDist) {
                        ans=dn;
                        minDist=Vector3D.dist2(dn.pos, v);
                    }                    
                }
            }
        }
        return ans;        
    }
    
     cellAdapted findCell(String id) {
    	 cellAdapted ans=null;
    	 
        for (cellAdapted c : allCells2) {
            if (c.id_Cell.equals(id)) {ans=c;}
        }
        return ans;
    }
        
	@Override
	public void run() {
        if ((!extensionsHaveBeenRegistered)) {     	
        	HandleIJ1Extension.addAClass(this.getClass());        	
            Functions.registerExtensions(new HandleIJ1Extension());
        }
        if (allCells2==null) {
            allCells2=new ArrayList<>();
            notifyCellExplorerCellsModif=true;
            notifyCellRendererCellsModif=true;
        }
        if (dots_to_overlay==null) {
        	dots_to_overlay = new ArrayList<>();
        }
        if (Opt2==null) {
            initOptimizer2(); 
        }
        if (workingImP==null) {
        	// Initialize the plugin with the current open image
        	workingImP = WindowManager.getCurrentImage();
        }                
        //=============================================     
        showGUI();
	}		
}

