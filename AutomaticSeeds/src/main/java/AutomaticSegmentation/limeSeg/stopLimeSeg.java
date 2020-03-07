package AutomaticSegmentation.limeSeg;

import eu.kiaru.limeseg.LimeSeg;

public class stopLimeSeg extends Thread {
	
	public void run() {
		
		long startTime = System.currentTimeMillis();
		long endTime=0;
		
		boolean cond=true;
		
		
		while (cond==true) {
			endTime= System.currentTimeMillis();
			System.out.println((endTime-startTime) /1000);
			
			if( ((endTime-startTime) /1000) >120) { //si el tiempo de ejecucion es mayor que 100 segundos
				System.out.println("PAM");
				cond=false;
				LimeSeg.stopOptimisation();
			}

		}
	
		System.out.println("Ha salido del while");
		
	}
	

}
