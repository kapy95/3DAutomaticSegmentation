package AutomaticSegmentation.limeSeg;

import java.util.ArrayList;

import eu.kiaru.limeseg.cudaHelper.FloatGpuArray;
import eu.kiaru.limeseg.cudaHelper.IntGpuArray;
import eu.kiaru.limeseg.struct.DotN;

import static java.util.stream.IntStream.range;

/**
 * Helper function to handle lost of dots by GPU
 * @author Nicolas Chiaruttini
 */
class GPUDots {
    
    final public  int
            PX=0,PY=1,PZ=2,
            NX=3,NY=4,NZ=5,
            FX=6,FY=7,FZ=8,
            MX=9,MY=10,MZ=11,
            RFX=12, RFY=13, RFZ=14,
            SUPER_DOT_RADIUS_SQUARED = 15,
            RELAXED=16,
            N_PARAMS_FLOAT=17;
    
    final public  int
          N_NEIGH=0,
          CELL_ID=1,
          HAS_CONVERGED=2,          
          ALL_NEIGHBORS_HAVE_CONVERGED=3,  
          ALL_NEIGHBORS_HAVE_CONVERGED_PREVIOUSLY=4,
          N_PARAMS_INT=5;           
    
    public boolean hasBeenPushed;
    
    IntGpuArray[] iGA_Int;
    FloatGpuArray[] iGA_Float;
    
    int numberOfDots;
    
    public GPUDots() {
        hasBeenPushed=false;
        numberOfDots=0;
        // Initialisation of gpu arrays
        iGA_Float = new FloatGpuArray[this.N_PARAMS_FLOAT];
        for (int i=0;i<this.N_PARAMS_FLOAT;i++) {
            iGA_Float[i] = new FloatGpuArray();
        }
        
        iGA_Int = new IntGpuArray[this.N_PARAMS_INT];
        for (int i=0;i<this.N_PARAMS_INT;i++) {
            iGA_Int[i] = new IntGpuArray();
        }
    }
    
    public void setAllNeighborsConvergenceTo1() {
        for(int index=0;index<iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].length;index++) {
            iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].cpuArray[index]=1;
        }
        iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].push();
    }
    
    public void push(final ArrayList<DotN> dotList) {
        numberOfDots = dotList.size();
        // Mem allocation check
        for (int i=0;i<this.N_PARAMS_FLOAT;i++) {
            iGA_Float[i].ensureAllocatedSize(numberOfDots, false, false);
            iGA_Float[i].length=numberOfDots;
        }
        for (int i=0;i<this.N_PARAMS_INT;i++) {
            iGA_Int[i].ensureAllocatedSize(numberOfDots, false, false);
            iGA_Int[i].length=numberOfDots;
        }
        //---------------------------------
        // All set
        // First : hydrate tabs
        range(0,dotList.size()).parallel().forEach( index -> {
            DotN dn = dotList.get(index);
            //int index = dn.dotIndex;
            iGA_Float[this.PX].cpuArray[index] = dn.pos.x;  iGA_Float[this.PY].cpuArray[index] = dn.pos.y;  iGA_Float[this.PZ].cpuArray[index] = dn.pos.z;
            iGA_Float[this.NX].cpuArray[index] = dn.Norm.x; iGA_Float[this.NY].cpuArray[index] = dn.Norm.y; iGA_Float[this.NZ].cpuArray[index] = dn.Norm.z;
            
            iGA_Float[this.SUPER_DOT_RADIUS_SQUARED].cpuArray[index]=dn.superDotRadiusSquared;
            iGA_Float[this.RELAXED].cpuArray[index]=0;//dn.relaxed;
            
            if (!dn.isSuperDot) {
                iGA_Int[this.CELL_ID].cpuArray[index]=dn.ct.idInt;
            } else {
                iGA_Int[this.CELL_ID].cpuArray[index]=-1;
            }
            iGA_Int[this.HAS_CONVERGED].cpuArray[index]=dn.hasConverged?1:0;
            iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].cpuArray[index]=dn.allNeighborsHaveConverged?1:0;
            iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED_PREVIOUSLY].cpuArray[index]=dn.allNeighborsHaveConvergedPreviously?1:0;
        });
        
        // Second : push into device hydrated tabs
        
        iGA_Float[this.PX].push();iGA_Float[this.PY].push();iGA_Float[this.PZ].push();
        iGA_Float[this.NX].push();iGA_Float[this.NY].push();iGA_Float[this.NZ].push();
        iGA_Float[this.SUPER_DOT_RADIUS_SQUARED].push();
        iGA_Float[this.RELAXED].push();
        
        iGA_Int[this.CELL_ID].push();
        iGA_Int[this.HAS_CONVERGED].push();
        iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].push();
        iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED_PREVIOUSLY].push();
        
        // Last : reset (=put zero) in GPUMem when needed
        // FX, MX, RFX
        iGA_Float[this.FX].setGpuMemToZero();
        iGA_Float[this.FY].setGpuMemToZero();
        iGA_Float[this.FZ].setGpuMemToZero();
        
        iGA_Float[this.MX].setGpuMemToZero();
        iGA_Float[this.MY].setGpuMemToZero();
        iGA_Float[this.MZ].setGpuMemToZero();
        
        iGA_Float[this.RFX].setGpuMemToZero();
        iGA_Float[this.RFY].setGpuMemToZero();
        iGA_Float[this.RFZ].setGpuMemToZero();
        
        // N_Neigh
        iGA_Int[this.N_NEIGH].setGpuMemToZero();
        this.hasBeenPushed=true;
    }
    
    public void pop(ArrayList<DotN> dotList) {
        // Returns updated:
        // Float : Moment, Force, RepulsiveForce        
        // Int : N_Neigh, and all_neighbors_have_converged
        // Fetch Data from GPU
        // Copy the data from the device back to the host and clean up
       
        iGA_Float[this.FX].pop();iGA_Float[this.FY].pop();iGA_Float[this.FZ].pop();
        iGA_Float[this.MX].pop();iGA_Float[this.MY].pop();iGA_Float[this.MZ].pop();
        iGA_Float[this.RFX].pop();iGA_Float[this.RFY].pop();iGA_Float[this.RFZ].pop();
        
        iGA_Float[this.RELAXED].pop();
        iGA_Int[this.N_NEIGH].pop();
        iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].pop();
        // Put data into dotList
        dotList.parallelStream().forEach(dn -> {
            int index = dn.dotIndex;
            
            dn.force.x += iGA_Float[this.FX].cpuArray[index];
            dn.force.y += iGA_Float[this.FY].cpuArray[index];
            dn.force.z += iGA_Float[this.FZ].cpuArray[index];
            
            dn.repForce.x += iGA_Float[this.RFX].cpuArray[index];
            dn.repForce.y += iGA_Float[this.RFY].cpuArray[index];
            dn.repForce.z += iGA_Float[this.RFZ].cpuArray[index];
            
            dn.moment.x += iGA_Float[this.MX].cpuArray[index];
            dn.moment.y += iGA_Float[this.MY].cpuArray[index];
            dn.moment.z += iGA_Float[this.MZ].cpuArray[index];
            
            dn.relaxed = java.lang.Float.max(iGA_Float[this.RELAXED].cpuArray[index],dn.relaxed);
            dn.N_Neighbor = iGA_Int[this.N_NEIGH].cpuArray[index];
                
            dn.allNeighborsHaveConverged = (iGA_Int[this.ALL_NEIGHBORS_HAVE_CONVERGED].cpuArray[index]==1);  
        });
    }
    
    public void freeMem() {
            hasBeenPushed=false;
            //Floats          
            for (int i=0;i<this.N_PARAMS_FLOAT;i++) {
                iGA_Float[i].freeMem();
            }            
            // Ints
            for (int i=0;i<this.N_PARAMS_INT;i++) {
                iGA_Int[i].freeMem();
            }
    }
}

