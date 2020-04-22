package AutomaticSegmentation.limeSeg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class generationalChange {
	
	private ArrayList<Individuo> previousPopulation;// list of individuals which participate in the selection
	private ArrayList<Individuo> candidates;//list of individuals which will generate the new population after mutation methods...etc-
	
	public generationalChange(ArrayList<Individuo> population) {
		
		super();
	
		//First we get the two individuals with maximum score, they will pass directly to the next generation:
		Individuo bestIndividual = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		this.candidates.add(bestIndividual);
		population.remove(bestIndividual);//it is removed so that it is not consider in the selection process
		
		//we do the same for the second best individual:
		Individuo bestIndividual2 = Collections.max(population, Comparator.comparingDouble(Individuo::getScore));
		this.candidates.add(bestIndividual2);
		population.remove(bestIndividual2);
		
		
		//finally the rest of the population is passed to the list of individuals which participate in the selection:
		this.setPreviousPopulation(population);
			
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
	
	
	
	public ArrayList<Individuo> rouletteWheelSelection(ArrayList<Individuo> pob,int selpobnum){//selpobnum is the number of candidates to be selected to generate the new population
		ArrayList<Individuo> pobroul = new ArrayList<Individuo>();
		
		double sum=pob.stream().mapToDouble(a -> a.getScore()).sum();
		double[]partialSums=new double[pob.size()];//array with the likelihoods of all candidates;
		double partialsum=0;//each individual will have the sum of the scores of the previous individuals as likelihood
		int i;
		
		//the probabilities are saved:
		for(i=0;i<pob.size();i++) {
			partialsum+=pob.get(i).getScore();
			partialSums[i]=partialsum;
		}
		
		int j=0;
		
		//first we calculate the first value, where the roulette will stop
		double rng = (Math.random()*((sum-partialSums[0])+1))+partialSums[0];//it generates a number between the first score and the sum of all scores, which will state the candidates to be chosen
		
		//now we execute the roulette until the number of selected candidates is equal to selpobnum:
		
		while(pobroul.size()<=selpobnum) {//while the size of the candidates selected is not equal to selpobnum the roulette will be repeated again:
			
			//if the partialsum of the candidate is greater than the value of the roulette, it will be selected
			if(partialSums[j]>=rng) {
						
						pobroul.add(pob.get(i));
			}
			
			//if we have not looped all the array, we continue checking if the likelihood is greater than rng;
			if(j<partialSums.length) {
				
				j++;
				
			}else {
				//if we have checked the whole array, it starts again:
				j=0;
				rng = (Math.random()*((sum-partialSums[0])+1))+partialSums[0];
			}
							
		}
		
			
		return pobroul;
	}
	
	
	public ArrayList<Individuo> tournamentSelection(ArrayList<Individuo> pob, int numIndividuals){
		//numIndividuals is the variable to represent the number of individuals selected for the new population after the tournaments
		
		//The fittest individuals of the tournaments will be stored in this variable:
		ArrayList<Individuo> fittestIndividuals = new ArrayList<Individuo>();
		
		//the lower numIndividuals is, the lower it is the likelihood of weak individuals to pass to the next population,
		//since the tournaments will include more individuals fitter than them.
		
		//the comparison will be done bearing in mind tournamentSize, which establishes how many individuals will be involved in one tournament;
		int tournamentSize= Math.round(pob.size()/numIndividuals);
		
		int i=1;
		int j=0;
		
		for(i=0;i<=numIndividuals;i=i+1){
			
				//tournamentIndividuals represents the individuals which will participate in the tournament:
				ArrayList<Individuo> tournamentIndividuals = new ArrayList<Individuo>();
			
				//now we select the individuals using the size of the tournament to select the individuals:
				//for example the selection of 40 candidates of a population of 200 candidates (tournamentSize=5): 
				//tournament 1(starts in individual 0 and finishes the selection in the individual 4):0,1,2,3,4
				//tournament 3(starts in individual 5 and finishes the selection in the individual 9):5,6,7,8,9
				//etc
			
				for(j=i*tournamentSize; j<(j+tournamentSize);j++) {
					tournamentIndividuals.add(pob.get(j));
				}
				
				//Individuo fittestIndividual = Collections.max(tournamentIndividuals, Comparator.comparingDouble(Individuo::getScore));->calcula el maximo 
				
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
				
	
		}
		
		
		return fittestIndividuals;
		
	}
	
	
	public ArrayList<Individuo> getPreviousPopulation() {
		return previousPopulation;
	}
	
	
	public void setPreviousPopulation(ArrayList<Individuo> previousPopulation) {
		this.previousPopulation = previousPopulation;
	}
	
	
	

}
