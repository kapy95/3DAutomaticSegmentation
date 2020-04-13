package AutomaticSegmentation.limeSeg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class generationalChange {
	
	private ArrayList<Individuo> previousPopulation;
	
	public generationalChange(ArrayList<Individuo> population) {
		
		super();
		
		//first the population is sorted by the standard deviation of the individuals
		Collections.sort(population, new Comparator<Individuo>() {
			public int compare(Individuo i1, Individuo i2) {

				return i1.getStdVertex().compareTo(i2.getStdVertex());
			}
        });
		
		//then the we get the individuals which score is better than 75, but we will keep only the two individuals with the best standard deviations and the best scores
		Object[] bestIndividuals= population.stream().filter(ind-> ind.getScore()>75).toArray();
		Object bestIndividual = bestIndividuals[0];
		Object bestIndividual2 = bestIndividuals[1];
		
		//these individuals will be included in the next population directly (this tactic is elitism):
		Individuo bestIndividualdef1 = Individuo.class.cast(bestIndividual);
		Individuo bestIndividualdef2 = Individuo.class.cast(bestIndividual2);
		
		//now the best individuals are removed owing to they are not going to received changes:
		population.remove(bestIndividualdef1);
		population.remove(bestIndividualdef2);
		
		this.setPreviousPopulation(population);
			
	}
	
	
	public void main() {
		
		//aqui voy a realizar la division de la poblacion en partes para que se utilicen distintos metodos para generar la nueva poblacion
		
	}
	
	
	
	public ArrayList<Individuo> rouletteWheelSelection(ArrayList<Individuo> pob){
		ArrayList<Individuo> pobroul = new ArrayList<Individuo>();
		
		double sum=pob.stream().mapToDouble(a -> a.getScore()).sum();
		
		int i=0;
		
		double rng = (Math.random()*((sum-0)+1))+0;
		double partialSum=0;
		
		for (i=0;i<pob.size();i++){
		
			partialSum+=pob.get(i).getScore();
			
				if(partialSum>=rng) {
					
					pobroul.add(pob.get(i));
				}
		}
			
		return pobroul;
	}
	
	
	public ArrayList<Individuo> getPreviousPopulation() {
		return previousPopulation;
	}
	
	
	public void setPreviousPopulation(ArrayList<Individuo> previousPopulation) {
		this.previousPopulation = previousPopulation;
	}
	
	
	

}
