package AutomaticSegmentation.limeSeg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;

public class generationalChange {
	
	private ArrayList<Individuo> previousPopulation;// list of individuals which participate in the selection
	private ArrayList<Individuo> nextGeneration;//list of individuals which will generate the new population after mutation methods...etc-
	
	public generationalChange(ArrayList<Individuo> population, int nextPopulationSize) {
		super();
		
		this.nextGeneration =new ArrayList<Individuo>(nextPopulationSize);
		
		//First we get the two individuals with maximum score, they will pass directly to the next generation:
		Individuo bestIndividual = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		this.nextGeneration.add(bestIndividual);
		population.remove(bestIndividual);//the best individual is removed temporarily in order to find the second maximum value

		//we do the same for the second best individual:
		Individuo bestIndividual2 = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		this.nextGeneration.add(bestIndividual2);
		population.add(bestIndividual);//we add the bestIndividual again
				
				

		while(this.nextGeneration.size()<nextPopulationSize) {
			
			int selectedMethodForGeneration = 1 + (int)(Math.random() * ((2 - 1) + 1));//this variable represents the method selected to generate one individual of the next population
			//There two methods: only one parent(1) or two parents(2).
			if(selectedMethodForGeneration==1) {//the individual of the next population will be generated taking into account only one individual of the previous population
				
				//elegir la nueva mutacion de un individuo seleccionado por torneo o ruleta
				
			}else {//the individual of the next population will be generated taking into account two individuals of the previous generation
				
				//elegir el método para generar el nuevo individuo
				
			}
		
		}
		
			
	}
	
	
	public void main() {
		
		int numCandidates = Math.round(this.previousPopulation.size()/2);
		int i;
		ArrayList<Individuo> rouletteIndividuals=new ArrayList<Individuo>();
		ArrayList<Individuo> tournamentIndividuals=new ArrayList<Individuo>();
		
		//we will apply one of the selection method to the first half of the population and the other method to the other half:
		for(i=0;i<numCandidates;i++) {
			
			if(i < Math.round(numCandidates/2)){
				
				rouletteIndividuals.add(this.previousPopulation.get(i));
				
			}else {
				
				tournamentIndividuals.add(this.previousPopulation.get(i));
				
			}
		}
		
		//this.rouletteWheelSelection(rouletteIndividuals,)
		
		
		
	}
	
	
	
	public Individuo rouletteWheelSelection(ArrayList<Individuo> pob,int maxRange){//maxRange is a parameter to determine the range of numbers of the wheel selection
		Individuo selectedIndividual = null;
		
		
		int [] numbers=new int[maxRange];//an array will be created with the size of maxRange, whose values will go from 0 to maxRange.
		int z;
		
		for(z=0;z<maxRange;z++) {
			numbers[z]=z;
		}
		
		int globalIndex= 0;
		
		double sum=pob.stream().mapToDouble(a -> a.getScore()).sum();//the sum of all scores is calculated to create likelihoods
		
		int[][]probabilities=new int[pob.size()][];//array with the likelihoods of all candidates;
		
		int i;
		
		for(i=0;i<pob.size();i++) { 
			//for each individual his probability is calcualted:pob.get(i).getScore()/sum)
			//depending on the likelihood they will get more numbers of the array:Math.round( ( (pob.get(i).getScore()/sum) *maxRange) )
			//For example, if there is a population with three individuals: I1 (Score:10), I2 (Score:20), I3(Score:30), the scores sum is 60
			//therefore: P(I1)=10/60=0.16667=0.17, P(I2)=20/60=0.33333...=0.33, P(I3)=30/60=1/2=0.5, where P is the probability of an individual
			 int range=(int) Math.round( ( (pob.get(i).getScore()/sum) *maxRange) );
			 
			//Finally, if we create an array of 10 numbers representing the likelihoods [0,1,2,3...10] their corresponding numbers are:
			//I1:10 x 0.2= 2 numbers (1,2), I2:10 X0.33=3.3=3 3 numbers (3,4,5), and I3: 10 x 0.5 = 5 numbers (6,7,8,9,10)
			 probabilities[i]=Arrays.copyOfRange(numbers, globalIndex, range);//calculus of the corresponding numbers of an individual
			 globalIndex=range+1;
			 
			 //Thus, the higher is the fitness of the individuals, the more is the likelihood of being selected
		}
		
		
		int j=0;
		//Now we create a number between 0 and 100. This number determines where the roulette will stop, the individual with that number will be selected
		int rng = (int) Math.round((Math.random()*((100-0)+1))+0); //it generates a number between 0 and 100,which will establish the candidate to be chosen
		
		while(selectedIndividual == null) {
			
			int[] a = probabilities[j];
			boolean contains = IntStream.of(a).anyMatch(x -> x == rng);
			
			if(contains == true) {
				selectedIndividual=pob.get(j);
			}
			
			
			j=j+1;
		}
		
	
		return selectedIndividual;
	}
	
	
	public Individuo tournamentSelection(ArrayList<Individuo> pob, int numIndividuals){
		//numIndividuals is the variable to represent the number of individuals which participate in a tournament
		
		//The fittest individuals of the tournaments will be stored in this variable:
		ArrayList<Individuo> fittestIndividuals = new ArrayList<Individuo>();
		
		//the lower numIndividuals is, the lower it is the likelihood of an individual to pass to the next population since they have to compete against more individuals to be selected
		
		//the comparison will be done bearing in mind tournamentSize, which establishes how many individuals will be involved in one tournament;
		int numOfTournaments= Math.round(pob.size()/numIndividuals);
		
		int i=1;
		int j=0;
		int z=0;
		ArrayList<ArrayList<Individuo>> winners =  new ArrayList<ArrayList<Individuo>>(numOfTournaments+1); 
		winners.get(0).addAll(pob);//the first arraylist of winners will be the whole population
		
		for(z=0;z<winners.size();z++) {
			
		
			for(i=0;i<winners.get(z).size();i=i+1){
					
					//tournamentIndividuals represents the individuals which will participate in the tournament:
					ArrayList<Individuo> tournamentIndividuals = new ArrayList<Individuo>();
					
					//now we select the individuals using the size of the tournament to select the individuals:
					//for example the selection of 40 candidates of a population of 200 candidates (tournamentSize=5): 
					//tournament 1(starts in individual 0 and finishes the selection in the individual 4):0,1,2,3,4
					//tournament 3(starts in individual 5 and finishes the selection in the individual 9):5,6,7,8,9
					//etc
				
					for(j=i*numIndividuals; j<(j+numIndividuals);j++) {//the individuals 
						tournamentIndividuals.add(winners.get(z).get(j));
					}
					
					double rng = Math.random()*(((1-0)+1))+0;
					if(rng<0.75) {//if rng is not greater than 0.75 the best Individual is selected
						
						Individuo fittestIndividual = Collections.max(tournamentIndividuals, Comparator.comparingDouble(Individuo::getScore));//it calculates the maximum of the array
						winners.get(z+1).add(fittestIndividual);
					
					}else {//else the worst individual is selected
							
						Individuo worstIndividual = Collections.min(tournamentIndividuals, Comparator.comparingDouble(Individuo::getScore));//it calculates the minimum of the array
						winners.get(z+1).add(worstIndividual);
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
			upperBoundD0=8.5f;
		}
		
		if(lowerBoundRangeD0<0.5f) {
			lowerBoundD0=0.5f;
		}
		
		
		//F_pressure:
		float upperBoundF_pressure_values=maxF_pressure_values+(alfa*(maxF_pressure_values-minF_pressure_values));
		float lowerBoundF_pressure_values=minF_pressure_values+(alfa*(maxF_pressure_values-minF_pressure_values));
		
		if(upperBoundF_pressure_values>0.025f) {
			upperBoundD0=0.025f;
		}
		
		if(lowerBoundRangeD0<-0.03f) {
			lowerBoundD0=-0.03f;
		}
		//Min + (int)(Math.random() * ((Max - Min) + 1))
		
		//finally the values for the individual are calculated:
		float D0 = lowerBoundD0 + (float) (Math.random() * ( (upperBoundD0 - lowerBoundD0) + 1) );
		float range_D0= lowerBoundD0 + (float) (Math.random() * ( (upperBoundRangeD0 - lowerBoundRangeD0) + 1) );
		float f_pressure= lowerBoundF_pressure_values + (float) (Math.random() * ( (upperBoundF_pressure_values- lowerBoundF_pressure_values) + 1) );
		
		indGenerated.setD0(D0);
		indGenerated.setRange_d0(range_D0);
		indGenerated.setF_pressure(f_pressure);
		
		
		return indGenerated;
	}
	
	
	
	public ArrayList<Individuo> getPreviousPopulation() {
		return previousPopulation;
	}
	
	
	public void setPreviousPopulation(ArrayList<Individuo> previousPopulation) {
		this.previousPopulation = previousPopulation;
	}
	
	
	

}
