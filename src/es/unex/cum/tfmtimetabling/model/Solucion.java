package es.unex.cum.tfmtimetabling.model;

/**
 * Modelo Solucion
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Solucion implements Comparable<Solucion> {

	private Lecture[][] solucion;
	private int fitness;

	public Solucion(Lecture[][] solucion, int fitness) {
		super();
		this.solucion = solucion;
		this.fitness = fitness;
	}

	public Lecture[][] getSolucion() {
		return solucion;
	}

	public void setSolucion(Lecture[][] solucion) {
		this.solucion = solucion;
	}

	public int getFitness() {
		return fitness;
	}

	public void setFitness(int fitness) {
		this.fitness = fitness;
	}

	@Override
	public int compareTo(Solucion sol) {
		if (this.fitness > sol.getFitness()) {
			return 1;
		}
		if (this.fitness < sol.getFitness()) {
			return -1;
		}
		return 0;
	}

}
