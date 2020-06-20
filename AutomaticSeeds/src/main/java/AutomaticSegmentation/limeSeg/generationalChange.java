package AutomaticSegmentation.limeSeg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

public class generationalChange {
	
	public ArrayList<Individuo> nextGeneration;//list of individuals which will generate the new population after mutation methods...etc-
	public ArrayList<Individuo> previousGeneration;
	public int nextGenerationSize;
	
	public generationalChange(ArrayList<Individuo> population, int nextPopulationSize,int iter,String dir) {
		super();
		this.nextGenerationSize=nextPopulationSize;
		this.nextGeneration =new ArrayList<Individuo>(nextPopulationSize);
		//First we get the two individuals with maximum score, they will pass directly to the next generation:
		Individuo bestIndividual = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		

		File srcDir =  bestIndividual.getDir();
		
		File destDir=new File(dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(iter+1));
		destDir.mkdir();
		
		File destDirInd1=new File(destDir.toString()+"\\mejor individuo gen"+ String.valueOf(iter)); //incluir mejor resultado aqui en el titulo para identificarlo y no volver a calcularlo
		//bestIndividual.setDir(destDirInd1);
		
		try {
   		 FileUtils.copyDirectory(srcDir, destDirInd1);
		} catch (IOException e) {
   		 e.printStackTrace();
		}
		
		
		this.nextGeneration.add(bestIndividual);
		population.remove(bestIndividual);//the best individual is removed temporarily in order to find the second maximum value

		//we do the same for the second best individual:
		Individuo bestIndividual2 = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		
		File destDirInd2=new File(destDir.toString()+"\\segundo mejor individuo gen"+ String.valueOf(iter));
		File srcInd2Dir1 =  bestIndividual2.getDir();
		
		//bestIndividual2.setDir(new File(destDir.toString()+"\\resultado"+String.valueOf(res+1)+String.valueOf(iter+1)));
		//bestIndividual2.setDir(destDirInd2);
		this.nextGeneration.add(bestIndividual2);
		
		try {
	   		 FileUtils.copyDirectory(srcInd2Dir1, destDirInd2);
			} catch (IOException e) {
	   		 e.printStackTrace();
		}
		
		population.add(bestIndividual);//we add the bestIndividual again
		this.previousGeneration = new ArrayList<Individuo>();
		this.previousGeneration.addAll(population);
		
	}
	
	
	public void main(int iter,String dir) {
		ArrayList<Individuo> population= new ArrayList<Individuo>();
	    population.addAll(this.previousGeneration);
	    ArrayList<ArrayList<Integer>> probabilities=likelyhoodsCalculation(population);
		int counterMutated=0;
		int counterCrossover=0;
		File dirPob=new File(dir.toString()+"\\resultados\\resultado generacion"+String.valueOf(iter));
		int maxCrossover=(int) Math.round(0.75f*(this.previousGeneration.size()-2));//minus 3 because two individuals have been already selected and java starts in 0 so -3
		int maxMutated=(this.previousGeneration.size()-2)-maxCrossover;
		Random rand= new Random();
		int contador=2;
		try {
			FileWriter writer = new FileWriter(dirPob+"\\RecambioGeneracional"+String.valueOf(iter)+".csv");
			
	        writer.append("MetodoSeleccionado");
            writer.append(',');
            writer.append("Individuo1 D_0");
            writer.append(',');
            writer.append("Individuo1 Range_D0");
            writer.append(',');
            writer.append("Individuo1 f_pressure");
            writer.append(',');
            writer.append("Individuo2 D_0");
            writer.append(',');
            writer.append("Individuo2 Range_D0");
            writer.append(',');
            writer.append("Individuo2 f_pressure");
            writer.append(',');
			writer.append("IndividuoGenerado");
            writer.append(',');
            writer.append("IndividuoGenerado D_0");
            writer.append(',');
            writer.append("IndividuoGenerado Range_D0");
            writer.append(',');
            writer.append("IndividuoGenerado f_pressure");
            writer.append('\n');
            
            writer.flush();
            
		while(contador<=this.nextGenerationSize-1) {
			String metodo=null;
			Individuo newIndividual=null;

			

			Individuo selectedIndividual1= new Individuo();
			Individuo selectedIndividual2=new Individuo();
			//now two individuals are selected for crossover by either roulette or tournament:
		
			//first a number between 2 and 1 is calculated:
			int selectedMethodI1=1 + (int)(Math.random() * ((2 - 1) + 1));
			if(selectedMethodI1==1) {//if it is equal to 1, rouletteWheelSelection will be chosen as selection method 

				selectedIndividual1=this.rouletteWheelSelection(probabilities);
				selectedIndividual1.setSelectionMethod("Ruleta");
				
			}else {//else tournament selection will be chosen as selection method 

				selectedIndividual1=this.tournamentSelection(population, 2);
				selectedIndividual1.setSelectionMethod("Torneo");
			}
			
			int selectedMethod1=0;//it selects whether mutation or crossover is going to be used:
			//1-> crossover is selected, 2->mutation is selected
			if(counterCrossover==maxCrossover) {//if the maximum percentage of indiviuals generated by crossover has been reached, the rest of individuals will be generated by mutation
				
				selectedMethod1=2;
				
			}else if(counterMutated==maxMutated){//if the maximum percentage of indiviuals generated by mutation has been reached, the rest of individuals will be generated by crossover
				
				selectedMethod1=1;
				
			}else {//if none of the maximums is reached, the individual will be generated by one of the methods randomly
				
				selectedMethod1 =	1 + (int)(Math.random() * ((2 - 1) + 1));
			}
			
			

		//if selectedMethod1 is equal to 1 and if the maximum amount of individuals created by crossover is lower than 85% of the new populationsize, the new individual will be created by crossover:
			if(selectedMethod1==1) {
				//the same process to choose the second candidate for crossover
				int selectedMethodI2=1 + (int)(Math.random() * ((2 - 1) + 1));
				
				if(selectedMethodI2==1) {

					selectedIndividual2=this.rouletteWheelSelection(probabilities);
					selectedIndividual2.setSelectionMethod("Ruleta");
					
				}else {
					
					selectedIndividual2=this.tournamentSelection(population, 2);
					selectedIndividual2.setSelectionMethod("Torneo");
				}
				
				counterCrossover=counterCrossover+1;// we increase the counter
				
				int selectedMethod2 =	1 + (int)(Math.random() * ((3 - 1) + 1));//it selects which crossover method is going to be selected:
			
				if(selectedMethod2==1) {//only crossover
					metodo="OnlyCrossover";
					int selectedMethodPureCrossover =	1 + (int)(Math.random() * ((2 - 1) + 1));//it selects which crossover method is going to be selected:
					
					if(selectedMethodPureCrossover==1) {//if it is equal to 1 it will be selected double point crossover
						newIndividual=this.DoublePointCrossOver(selectedIndividual1, selectedIndividual2);
						
					}else {//else it will be selected single point crossover
						newIndividual=this.SinglePointCrossOver(selectedIndividual1, selectedIndividual2);
					}
					
					
				}else if(selectedMethod2==2) {//crossover+mutation:
					metodo="crossover+mutation";
					Individuo i1mutated=this.mutation(selectedIndividual1);
					Individuo i2mutated=this.mutation(selectedIndividual2);
					
					int selectedMethodCrossover =	1 + (int)(Math.random() * ((2 - 1) + 1));//it selects which crossover method is going to be selected:
					
					if(selectedMethodCrossover==1) {//if it is equal to 1 it will be selected double point crossover
						newIndividual=this.DoublePointCrossOver(i1mutated, i2mutated);
						
					}else {//else it will be selected single point crossover
						newIndividual=this.SinglePointCrossOver(i1mutated, i2mutated);
					}
					
					
				}else { //blend algorithm will be selected for crossover
					metodo="blendcrossover";
					newIndividual=this.blendCrossOver(selectedIndividual1, selectedIndividual2);
				}
				
				
			}else if (selectedMethod1==2){//if the maximum amout of individuals generated by mutation has not been reached, the individual will be generated only by mutation:
				metodo="mutation";
				counterMutated=counterMutated+1;
				
				newIndividual=this.mutation(selectedIndividual1);
				
			}
			newIndividual.setSelectionMethod(selectedIndividual1.getSelectionMethod()+selectedIndividual2.getSelectionMethod());
			newIndividual.setOffspringMethod(metodo);
			this.nextGeneration.add(newIndividual);
			
			writer.append(metodo);
	        writer.append(',');
	        writer.append(String.valueOf(selectedIndividual1.getD0()));
	        writer.append(',');
	        writer.append(String.valueOf(selectedIndividual1.getRange_d0()));
	        writer.append(',');
	        writer.append(String.valueOf(selectedIndividual1.getFp()));
	        writer.append(',');
	        writer.append(String.valueOf(selectedIndividual2.getD0()));
	        writer.append(',');
	        writer.append(String.valueOf(selectedIndividual2.getRange_d0()));
	        writer.append(',');
	        writer.append(String.valueOf(selectedIndividual2.getFp()));
	        writer.append(',');
	        writer.append(String.valueOf(contador));
	        writer.append(',');
	        writer.append(String.valueOf(nextGeneration.get(contador).getD0()));
	        writer.append(',');
	        writer.append(String.valueOf(nextGeneration.get(contador).getRange_d0()));
	        writer.append(',');
	        writer.append(String.valueOf(nextGeneration.get(contador).getFp()));
	        writer.append('\n');
	            
	        writer.flush();
			
			contador++;
			
			}
		
		writer.close();
		}catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
	
	
	public Individuo rouletteWheelSelection(ArrayList<ArrayList<Integer>> probabilities){//maxRange is a parameter to determine the range of numbers of the wheel selection
		Individuo selectedIndividual = null;
		
		int j=0;
		//the last element of the last array is the sum of all the scores
		int sum=probabilities.get(probabilities.size()-1).get(probabilities.get(probabilities.size()-1).size()-1);
		//Now we create a number between 0 and 100. This number determines where the roulette will stop, the individual with that number will be selected
		//int rng =  + (int) (Math.random()*((k-1)+1)); //it generates a number between 0 and sum-1,which will establish the candidate to be chosen
		int rng = ThreadLocalRandom.current().nextInt(0,sum-1);
		System.out.println(rng);
		while(selectedIndividual == null) {
			
			ArrayList<Integer> a = probabilities.get(j);
			//boolean contains = IntStream.of(a).anyMatch(x -> x == rng);
			
			if(a.contains(rng) == true) {
				selectedIndividual=this.previousGeneration.get(j);
			}
			
			
			j=j+1;
		}
		
	
		return selectedIndividual;
	}
	
	public ArrayList<ArrayList<Integer>> likelyhoodsCalculation(ArrayList<Individuo> pob){
		
		int globalIndex= 0;
		int k=0;
		//int sum=(int) pob.stream().mapToDouble(a -> a.getScore()).sum();//the sum of all scores is calculated to create likelihoods
		int sum=0;
		
		for(Individuo ind: pob) {

			 int score=(int) Math.round(ind.getScore());
			 
			 if(score==0) {
				 score=1;
			 }
			 
			 sum=sum+score;
		}
		
		ArrayList<ArrayList<Integer>>probabilities=new ArrayList<ArrayList<Integer> >();//array with the likelihoods of all candidates;
		//System.out.println(numbers.length);
		
		ArrayList<Integer> numbers=new ArrayList<Integer>();//an array will be created with the size of maxRange, whose values will go from 0 to maxRange.
		int z;
		for(z=0;z<=sum+1;z++) {
			numbers.add(z);
			//System.out.println(z);
		}
		
		int i;
		for(i=0;i<pob.size();i++) { 
			System.out.println("Individuo"+String.valueOf(i));
			//for each individual his probability is calcualted:pob.get(i).getScore()/sum)
			//depending on the likelihood they will get more numbers of the array:Math.round( ( (pob.get(i).getScore()/sum) *maxRange) )
			//For example, if there is a population with three individuals: I1 (Score:10), I2 (Score:20), I3(Score:30), the scores sum is 60
			//therefore: P(I1)=10/60=0.16667=0.17, P(I2)=20/60=0.33333...=0.33, P(I3)=30/60=1/2=0.5, where P is the probability of an individual
			 int range=(int) Math.round( pob.get(i).getScore() );
			 System.out.print("Rango:");
			 System.out.println(range);
			 if(range==0) {
				 
				 range=1;

			 }
			//Finally, if we create an array of 10 numbers representing the likelihoods [0,1,2,3...10] their corresponding numbers are:
			//I1:10 x 0.2= 2 numbers (1,2), I2:10 X0.33=3.3=3 3 numbers (3,4,5), and I3: 10 x 0.5 = 5 numbers (6,7,8,9,10)
			 //probabilities[i]=Arrays.copyOfRange(numbers, globalIndex, range);//calculus of the corresponding numbers of an individual
			 ArrayList<Integer> initial=new ArrayList<Integer>(range);
			 k=globalIndex;
			 
 			 for(k=globalIndex;k<(range+globalIndex);k++) {
				 System.out.print(numbers.get(k));
				 System.out.print(",");
				 initial.add(numbers.get(k));
			 }
			 System.out.println(" ");
			 probabilities.add(initial);
			 globalIndex=k;
			 
			 //Thus, the higher is the fitness of the individuals, the more is the likelihood of being selected
		}
		
		return probabilities;
		
	}
	
	public Individuo tournamentSelection(ArrayList<Individuo> pob, int numIndividuals){
		//numIndividuals is the variable to represent the number of individuals which participate in a tournament
		
		//the lower numIndividuals is, the lower it is the likelihood of an individual to pass to the next population since they have to compete against more individuals to be selected
		
		//the comparison will be done bearing in mind tournamentSize, which establishes how many individuals will be involved in one tournament;
		int numOfTournaments= Math.round(pob.size()/numIndividuals);
		
		int i=0;
		int z=0;
		ArrayList<ArrayList<Individuo>> winners =  new ArrayList<ArrayList<Individuo>>(); 
		winners.add(pob);//the first arraylist of winners will be the whole population
		
		while(winners.get(z).size()!=1) {
		int j=0;
		ArrayList<Individuo> tournamentWinners = new ArrayList<Individuo>();//individuals which only win a tournament:

			for(i=0;j<winners.get(z).size()-1;i=i+1){
					
					//tournamentIndividuals represents the individuals which will participate in the tournament:
					ArrayList<Individuo> tournamentIndividuals = new ArrayList<Individuo>();
					
					//now we select the individuals using the size of the tournament to select the individuals:
					//for example the selection of 40 candidates of a population of 200 candidates (tournamentSize=5): 
					//tournament 1(starts in individual 0 and finishes the selection in the individual 4):0,1,2,3,4
					//tournament 2(starts in individual 5 and finishes the selection in the individual 9):5,6,7,8,9
					//etc
					j=i*numIndividuals;
					
					while( j<( (i*2)+numIndividuals)  && j<winners.get(z).size()) {
						tournamentIndividuals.add(winners.get(z).get(j));
						j++;
					}

					
					if(j==winners.get(z).size()-1 && winners.get(z).size()%2!=0) {
						tournamentIndividuals.add(winners.get(z).get(j));
					}
						
					
					Random rand = new Random();
					double rng = 0 + rand.nextFloat()*1;
					if(rng<0.75) {//if rng is not greater than 0.75 the best Individual is selected
						
						Individuo fittestIndividual = Collections.max(tournamentIndividuals, Comparator.comparingDouble(Individuo::getScore));//it calculates the maximum of the array
						tournamentWinners.add(fittestIndividual);
					
					}else {//else the worst individual is selected
							
						Individuo worstIndividual = Collections.min(tournamentIndividuals, Comparator.comparingDouble(Individuo::getScore));//it calculates the minimum of the array
						tournamentWinners.add(worstIndividual);
					}
					
					/*
					//now we filter the Individuals basing on the score:
					ArrayList <Individuo>fittestIndividual= new ArrayList<Individuo>();
					tournamentIndividuals.stream().filter(a-> a.getScore()>75).forEach(a->fittestIndividual.add(a));
					
					if(fittestIndividual.isEmpty()) {
						
						 tournamentIndividuals.stream().filter(a-> a.getScore()>25).forEach(a->fittestIndividual.add(a));
						 
					}else if(fittestIndividual.size()>1) {
						
						//if there is more than one individual of the tournament with equal score, it will be selected the fittest individual 
						//depending on the standard deviation:
						
						Collections.sort(fittestIndividual, new Comparator<Individuo>() {
							public int compare(Individuo i1, Individuo i2) {
	
								return i1.getStdVertex().compareTo(i2.getStdVertex());
							}
				        });
						
						fittestIndividuals.add(fittestIndividual.get(0));
					
					}else {
						//if there is only one candidate, it will be in the first position
						fittestIndividuals.add(fittestIndividual.get(0));
					}
					*/
			}
			
			winners.add(tournamentWinners);
			z++;
		}
		
		
		return winners.get(z).get(0);//returns the only element of the last list,which will be the winner of the tournament.
		
	}
	
	public Individuo SinglePointCrossOver(Individuo i1, Individuo i2) {
		Individuo indGeneratedSPX = new Individuo();
		int rng = 1 + (int)(Math.random() * ((2 - 1) + 1));
		
		if(rng==1) {//if rng==1 the individual generated by crossover will receive the gene Fp from I1, thus the genes rangeD0 an D0 will be inherited from I2
		
		indGeneratedSPX.setF_pressure(i1.getFp()); 
		indGeneratedSPX.setRange_d0(i2.getRange_d0());
		indGeneratedSPX.setD0(i2.getD0());

		}else {//if rng==2 the individual generated by crossover will receive the gene Fp from I2, thus the genes rangeD0 an D0 will be inherited from I1
			
			indGeneratedSPX.setF_pressure(i2.getFp()); 
			indGeneratedSPX.setRange_d0(i1.getRange_d0());
			indGeneratedSPX.setD0(i1.getD0());
		}
		
		return indGeneratedSPX;
	}
	
	
	public Individuo DoublePointCrossOver(Individuo i1, Individuo i2) {
		
		Individuo indGeneratedSPX = new Individuo();
		int rng = 1 + (int)(Math.random() * ((2 - 1) + 1));
		
		if(rng==1) {//if rng==1 the individual generated by crossover will receive the genes Fp and Range_D0 from I1, and only D0 from I2
		
		indGeneratedSPX.setF_pressure(i1.getFp()); 
		indGeneratedSPX.setRange_d0(i1.getRange_d0());
		indGeneratedSPX.setD0(i2.getD0());

		}else {//if rng==2 the individual generated by crossover will receive the genes Fp and Range_D0 from I2, and only D0 from I1
			
			indGeneratedSPX.setF_pressure(i2.getFp()); 
			indGeneratedSPX.setRange_d0(i2.getRange_d0());
			indGeneratedSPX.setD0(i1.getD0());
		}
		
		return indGeneratedSPX;
	}
	
	
	public Individuo blendCrossOver(Individuo i1, Individuo i2) {//blend alpha algorithm for crossover in real coded algorithms
		
		Individuo indGenerated= new Individuo();
		
		float[] D0_values = {i1.getD0(),i2.getD0()};
		float[] Range_D0_values = {i1.getRange_d0(),i2.getRange_d0()};
		float[] F_pressure_values = {i1.getFp(),i2.getFp()};
		
		//gene:limeseg parameters
		//Now all the arrays are ordered in ascending order so that we can figure out which gene has the minimum value and the maximum value
		Arrays.sort(D0_values);
		Arrays.sort(Range_D0_values);
		Arrays.sort(F_pressure_values);
		
		//therefore the first element will be the minimum and the next element will be the maximum
		float minD0=D0_values[0];
		float maxD0=D0_values[1];
		
		float minRange_D0_values=Range_D0_values[0];
		float maxRange_D0_values=Range_D0_values[1];

		
		float minF_pressure_values=F_pressure_values[0];
		float maxF_pressure_values=F_pressure_values[1];
		
		//the parameters of the BLX algorithm are calculated in order to produce new individuals:
		float alfa=0.1f;//fixed value of the algorithm, it must be within 0 and 1, in other words:[0,1]
		
		//First the bounds to generate the new values are calculated for each parameter (gene)
		float upperBoundD0=maxD0+(alfa*(maxD0-minD0));
		float lowerBoundD0=minD0+(alfa*(maxD0-minD0));
		
		//if the upperbound is bigger than the maximum of limeseg, it will be set to the maximum of limeseg:
		if(upperBoundD0>18.0f) {
			upperBoundD0=18.0f;
		}
		
		//if the lowerbound is lower than the minimum of limeseg, it will be set to the minimum of limeseg:
		if(lowerBoundD0<1.0f) {
			lowerBoundD0=1.0f;
		}
		
		//the same process is repeated for the others genes:
		
		//RangeD0:
		float upperBoundRangeD0=maxRange_D0_values+(alfa*(maxRange_D0_values-minRange_D0_values));
		float lowerBoundRangeD0=minRange_D0_values+(alfa*(maxRange_D0_values-minRange_D0_values));
		
		if(upperBoundRangeD0>8.5f) {
			upperBoundRangeD0=8.5f;
		}
		
		if(lowerBoundRangeD0<0.5f) {
			lowerBoundRangeD0=0.5f;
		}
		
		
		//F_pressure:
		float upperBoundF_pressure_values=maxF_pressure_values+(alfa*(maxF_pressure_values-minF_pressure_values));
		float lowerBoundF_pressure_values=minF_pressure_values+(alfa*(maxF_pressure_values-minF_pressure_values));
		
		if(upperBoundF_pressure_values>0.025f) {
			upperBoundF_pressure_values=0.025f;
		}
		
		if(lowerBoundF_pressure_values<-0.025f) {
			lowerBoundF_pressure_values=-0.025f;
		}
		//Min + (int)(Math.random() * ((Max - Min) + 1))
		
		//finally the values for the individual are calculated:
		Random rand = new Random();
		
		float D0 = lowerBoundD0 + (float) (rand.nextFloat() * (upperBoundD0 - lowerBoundD0))  ;
		float range_D0= lowerBoundRangeD0 + (float) (rand.nextFloat() * (upperBoundRangeD0 - lowerBoundRangeD0) );
		float f_pressure= lowerBoundF_pressure_values + (float) (rand.nextFloat()  * ( upperBoundF_pressure_values- lowerBoundF_pressure_values) );
		
		indGenerated.setD0(D0);
		indGenerated.setRange_d0(range_D0);
		indGenerated.setF_pressure(f_pressure);
		
		
		return indGenerated;
	}
	
	
	public Individuo mutation(Individuo i1) {
		
		Individuo indMutated=new Individuo();
		
		//First the bound to generate the new value of D0 are calculated
		float lowerBoundD0=i1.getD0()-0.5f;
		float upperBoundD0=i1.getD0()+0.5f;

		//if the upperbound is bigger than the maximum of limeseg, it will be set to the maximum of limeseg:
		if(upperBoundD0>18.0f) {
			upperBoundD0=18.0f;
		}
		
		//if the lowerbound is lower than the minimum of limeseg, it will be set to the minimum of limeseg:
		if(lowerBoundD0<1.0f) {
			lowerBoundD0=1.0f;
		}
		
		//then we calculate the value for D0:
		Random rand = new Random();
		float D0 = lowerBoundD0 + (float) (rand.nextFloat() * (upperBoundD0 - lowerBoundD0) );
		
		
		float rng = 0.7f;//*rand.nextFloat();//it generates a number between 0 and 1
		
		if(rng>0.8) {// if rng is greater than 0.8  other genes are also mutated
			
			//the same process is applied to the other genes
			
			//RangeD0:
			float upperBoundRangeD0=i1.getRange_d0()+0.25f;
			float lowerBoundRangeD0=i1.getRange_d0()-0.25f;
			
			if(upperBoundRangeD0>8.5f) {
				upperBoundRangeD0=8.5f;
			}
			
			if(lowerBoundRangeD0<0.5f) {
				lowerBoundRangeD0=0.5f;
			}
			
			
			//F_pressure:
			float upperBoundF_pressure_values=i1.getFp()+0.05f;
			float lowerBoundF_pressure_values=i1.getFp()-0.05f;
			
			if(upperBoundF_pressure_values>0.025f) {
				upperBoundF_pressure_values=0.025f;
			}
			
			if(lowerBoundF_pressure_values<-0.03f) {
				lowerBoundF_pressure_values=-0.03f;
			}
			//finally the values for the individual are calculated:
			
			float range_D0= lowerBoundRangeD0 +  (rand.nextFloat() * ( upperBoundRangeD0 - lowerBoundRangeD0) );
			float f_pressure= lowerBoundF_pressure_values + (float) (rand.nextFloat() * (upperBoundF_pressure_values- lowerBoundF_pressure_values) );
			
			indMutated.setF_pressure(f_pressure);
			indMutated.setRange_d0(range_D0);
			
		}else {
			
			indMutated.setF_pressure(i1.getFp());
			indMutated.setRange_d0(i1.getRange_d0());
		}
		
		indMutated.setD0(D0);
		return indMutated ;
		
		
	}
	
	/*
	public ArrayList<Individuo> getPreviousPopulation() {
		return previousPopulation;
	}
	
	
	public void setPreviousPopulation(ArrayList<Individuo> previousPopulation) {
		this.previousPopulation = previousPopulation;
	}
	*/
	
	public ArrayList<Individuo> getNextPopulation(){
		return this.nextGeneration;
	}
	
	

}
