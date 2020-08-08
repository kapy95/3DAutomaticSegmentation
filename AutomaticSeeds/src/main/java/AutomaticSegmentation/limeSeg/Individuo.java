package AutomaticSegmentation.limeSeg;

import java.io.File;

public class Individuo {
	private float f_pressure; // variable con el valor de la presion [-0.03..0.03].
	private float d0;//d_0: 1 and >20 pixels.
	private float range_d0;//from 0.5 to >10
	private File dir;
	private Double meanVertex;
	private Double stdVertex; //standard deviation of the objects of the cells 
	private Double averageVolume;
	private Double averageCentroid;
	public Double score;
	public long time;
	public Integer notNullCells;
	private String selectionMethod;
	private String offspringMethod;
	private String Identifier;
	private Double stdFaces;
	private Double stdVolume;
	private Double distance;
	private boolean stdCondition;
	
	/**
	 * 
	 */
	public Individuo() {
		super();
		setStdFaces(0.0);
		setOffspringMethod("");
		setSelectionMethod("");
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the fp
	 */
	public float getFp() {
		return f_pressure;
	}
	/**
	 * @param fp the fp to set
	 */
	public void setF_pressure(float fp) {
		this.f_pressure = fp;
	}
	/**
	 * @return the d0
	 */
	public float getD0() {
		return d0;
	}
	/**
	 * @param d0 the d0 to set
	 */
	public void setD0(float d0) {
		this.d0 = d0;
	}
	/**
	 * @return the min_range_d0
	 */
	public float getRange_d0() {
		return range_d0;
	}
	/**
	 * @param min_range_d0 the min_range_d0 to set
	 */
	public void setRange_d0(float min_range_d0) {
		this.range_d0 = min_range_d0;
	}
	/**
	 * @return the dir
	 */
	public File getDir() {
		return dir;
	}
	/**
	 * @param dir the dir to set
	 */
	public void setDir(File dir) {
		this.dir = dir;
	}

	public Double getStdVertex() {
		return stdVertex;
	}

	public void setStdVertex(Double stdVertex) {
		this.stdVertex = stdVertex;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}
	public void setTime(long eaTime) {//it initializes the variable with the time that the evolutionary algorithm lasts to generate the individual.
		this.time=eaTime;
	}
	
	public long getTime() {
		return time;
	}
	
	public String getSelectionMethod() {
		return this.selectionMethod;
	}
	
	public void setSelectionMethod(String selectionMethod) {
		this.selectionMethod=selectionMethod;
	}
	
	public String getOffspringMethod() {
		return this.offspringMethod;
	}
	
	public void setOffspringMethod(String selectionMethod) {
		this.offspringMethod=selectionMethod;
	}
	
	public String getIdentifier() {
		return this.Identifier;
	}
	
	public void setIdentifier(String ident) {
		 this.Identifier=ident;
	}
	//It copies an individual to another working directory, which will be the new directory of the new generation

	public Double getStdFaces() {
		return stdFaces;
	}

	public void setNotNullCells(Integer cells) {
		this.notNullCells = cells;
	}
	
	public Integer getNotNullCells() {
		return notNullCells;
	}

	public void setStdFaces(Double stdFaces) {
		this.stdFaces = stdFaces;
	}


	public Double getAverageVolume() {
		return averageVolume;
	}

	public void setAverageVolume(Double averageVolume) {
		this.averageVolume = averageVolume;
	}

	public Double getStdVolume() {
		return stdVolume;
	}

	public void setStdVolume(Double stdVolume) {
		this.stdVolume = stdVolume;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getMeanVertex() {
		return meanVertex;
	}

	public void setMeanVertex(Double meanVertex) {
		this.meanVertex = meanVertex;
	}

	public Double getAverageCentroid() {
		return averageCentroid;
	}

	public void setAverageCentroid(Double averageCentroid) {
		this.averageCentroid = averageCentroid;
	}

	public boolean isStdCondition() {
		return stdCondition;
	}

	public void setStdCondition(boolean stdCondition) {
		this.stdCondition = stdCondition;
	}
	
	

}
