package AutomaticSegmentation.limeSeg;

import java.io.File;

import eu.kiaru.limeseg.LimeSeg;

public class evolutionary_algorithm {
	

	public static void main(String[] args) {

	}
	
	
	public int ObjectiveFunction() {
		
		int x=0;
		return x;
	}
	
	public void PopulationGenerator() {
		int i =0;
		//establezco el directorio de trabajo con las imágenes y roi
		
		File dir = new File("C:\\Users\\Carlo\\Documents\\Máster ISCDG\\TFM");
		
		float ZS=(float) 4.06;// variable con el valor del z_scale
		float FP= (float) 0.02; // variable con el valor de la presion
		float Rd0=2;
		
		for(i=0;i<5;i++) {
			
			//llamo a la clase que va a llamar limeseg:
			SphereSegAdapted seg=new SphereSegAdapted();
			seg.set_path(dir.toString());
			seg.setD_0(5);
			seg.setF_pressure(FP);
			seg.setZ_scale(ZS);
			seg.setRange_in_d0_units(Rd0);
			seg.start();//empieza a ejecutarse run del hilo de limeseg
			
			long startTime = System.currentTimeMillis();
			long endTime=0;
			
			boolean cond=true;
			
			while (seg.isAlive() && cond==true) {
				endTime= System.currentTimeMillis();
				System.out.println((endTime-startTime) /1000);
				
				if( ((endTime-startTime) /1000) >100) { //si el tiempo de ejecucion es mayor que 20 segundos corta?
					cond=false;
					LimeSeg.stopOptimisation();
				}

			}
			
	       	File dirNuevo= new File(dir.toString()+"\\resultados"+String.valueOf(i));
	       	dirNuevo.mkdir();
	       	//LimeSeg.saveStateToXmlPly(dir.toString()+"\\resultados"+String.valueOf(i));
	       	LimeSeg.saveStateToXmlPly(dirNuevo.toString());
			
		}
	}

}
