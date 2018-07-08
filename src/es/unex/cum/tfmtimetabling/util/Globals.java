package es.unex.cum.tfmtimetabling.util;

/** Constantes Globales
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Globals {
	// Ruta a la instancia
	public final static String INSTACE_PATH = "instances/comp06.ctt";
	// ALGORITMO GENETICO
	// Tamanio poblacion
	public final static int GA_POPULATION_SIZE = 60;
	public final static float GA_CROSSOVER_RATE = 0.8f;
	public final static int GA_CROSSOVER_TIMESLOTS = 1;
	public final static float GA_MUTATION_RATE = 0.04f;
	public final static float GA_HILL_CLIMBING_RATE = 0.05f;
	// Seleccion: 1 - Por torneo | 2 - Por ruleta
	public final static int GA_TYPE_SELECTION = 1; 
	// Iteracciones Algoritmo Genetico
	public final static int GA_ITERATIONS = 10000;
	// Tiempo de ejecucion del algoritmo genético
	public final static int GA_RUNTIME = 0;
	// Puntos de penalizacion Soft Constrains
	public final static int PENALTY_ROOM_CAPACITY = 1;
	public final static int PENALTY_MIN_WORKING_DAYS = 5;
	public final static int PENALTY_CURRICULUM_COMPACTNESS = 2;
	public final static int PENALTY_ROOM_STABILITY = 1;
	
	public final static boolean DEBUG_MODE = false;
}