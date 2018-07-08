package es.unex.cum.tfmtimetabling.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import es.unex.cum.tfmtimetabling.model.Instance;

/**
 * Parametros generales del algoritmo
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Params {

	// Etiquetas para leer los parametros del fichero de propiedades
	private final static String GA_POPULATION_SIZE_TAG = "ga_population_size";
	private final static String GA_ITERATIONS_TAG = "ga_iterations";
	private final static String GA_RUNTIME_TAG = "ga_runtime";
	private final static String GA_TYPE_SELECTION_TAG = "ga_type_selection";
	private final static String GA_CROSSOVER_RATE_TAG = "ga_crossover_rate";
	private final static String GA_CROSSOVER_TIMESLOTS_TAG = "ga_crossover_timeslots";
	private final static String GA_MUTATION_RATE_TAG = "ga_mutation_rate";
	private final static String GA_HILL_CLIMBING_RATE_TAG = "ga_hill_climbing_rate";
	private final static String PENALTY_ROOM_CAPACITY_TAG = "penalty_room_capacity";
	private final static String PENALTY_MIN_WORKING_DAYS_TAG = "penalty_min_working_days";
	private final static String PENALTY_CURRICULUM_COMPACNESS_TAG = "penalty_curriculum_compacness";
	private final static String PENALTY_ROOM_STABILITY_TAG = "penalty_room_stability";
	private final static String DEBUG_MODE_TAG = "debug_mode";

	// Parametros
	// Genetic Algorithm
	private int gaPopulationSize;
	private int gaIterations;
	private int gaRuntime;
	private int gaTypeSelection;
	private float gaCrossoverRate;
	private int gaCrossoverTimeslots;
	private float gaMutationRate;
	private float gaHillClimbingRate;

	// Penalty Points
	private int penaltyRoomCapacity;
	private int penaltyMinWorkingDays;
	private int penaltyCurriculumCompacness;
	private int penaltyRoomStability;

	private boolean debugMode;

	public Params() {
		// Inicializamos los parametros por defecto
		this.gaPopulationSize = Globals.GA_POPULATION_SIZE;
		this.gaIterations = Globals.GA_ITERATIONS;
		this.gaRuntime = Globals.GA_RUNTIME;
		this.gaTypeSelection = Globals.GA_TYPE_SELECTION;
		this.gaCrossoverRate = Globals.GA_CROSSOVER_RATE;
		this.gaCrossoverTimeslots = Globals.GA_CROSSOVER_TIMESLOTS;
		this.gaMutationRate = Globals.GA_MUTATION_RATE;
		this.gaHillClimbingRate = Globals.GA_HILL_CLIMBING_RATE;
		this.penaltyRoomCapacity = Globals.PENALTY_ROOM_CAPACITY;
		this.penaltyMinWorkingDays = Globals.PENALTY_MIN_WORKING_DAYS;
		this.penaltyCurriculumCompacness = Globals.PENALTY_CURRICULUM_COMPACTNESS;
		this.penaltyRoomStability = Globals.PENALTY_ROOM_STABILITY;
		this.debugMode = Globals.DEBUG_MODE;
	}

	/**
	 * Cargar los parametros desde un fichero
	 * 
	 * @param path
	 *            Ruta al fichero
	 * @return True si se han leido los parametros correctamente
	 */
	public boolean loadParams(String path) {
		boolean paramsLoaded = false;
		InputStream in = null;
		try {
			// Intentamos obtener el fichero con los parametros
			Properties prop = new Properties();
			File file = new File(path);
			if (file.exists()) {
				in = new FileInputStream(path);
				// Leemos los parametros del fichero
				prop.load(in);

				this.gaPopulationSize = Integer
						.parseInt(prop.getProperty(GA_POPULATION_SIZE_TAG, String.valueOf(Globals.GA_POPULATION_SIZE)));
				this.gaIterations = Integer
						.parseInt(prop.getProperty(GA_ITERATIONS_TAG, String.valueOf(Globals.GA_ITERATIONS)));

				this.gaRuntime = Integer.parseInt(prop.getProperty(GA_RUNTIME_TAG, String.valueOf(Globals.GA_RUNTIME)));
				
				this.gaTypeSelection = Integer
						.parseInt(prop.getProperty(GA_TYPE_SELECTION_TAG, String.valueOf(Globals.GA_TYPE_SELECTION)));

				this.gaCrossoverRate = Float
						.parseFloat(prop.getProperty(GA_CROSSOVER_RATE_TAG, String.valueOf(Globals.GA_CROSSOVER_RATE)));

				this.gaCrossoverTimeslots = Integer.parseInt(
						prop.getProperty(GA_CROSSOVER_TIMESLOTS_TAG, String.valueOf(Globals.GA_CROSSOVER_TIMESLOTS)));

				this.gaMutationRate = Float
						.parseFloat(prop.getProperty(GA_MUTATION_RATE_TAG, String.valueOf(Globals.GA_MUTATION_RATE)));

				this.gaHillClimbingRate = Float.parseFloat(
						prop.getProperty(GA_HILL_CLIMBING_RATE_TAG, String.valueOf(Globals.GA_HILL_CLIMBING_RATE)));

				this.penaltyRoomCapacity = Integer.parseInt(
						prop.getProperty(PENALTY_ROOM_CAPACITY_TAG, String.valueOf(Globals.PENALTY_ROOM_CAPACITY)));

				this.penaltyMinWorkingDays = Integer.parseInt(prop.getProperty(PENALTY_MIN_WORKING_DAYS_TAG,
						String.valueOf(Globals.PENALTY_MIN_WORKING_DAYS)));

				this.penaltyCurriculumCompacness = Integer.parseInt(prop.getProperty(PENALTY_CURRICULUM_COMPACNESS_TAG,
						String.valueOf(Globals.PENALTY_CURRICULUM_COMPACTNESS)));

				this.penaltyRoomStability = Integer.parseInt(
						prop.getProperty(PENALTY_ROOM_STABILITY_TAG, String.valueOf(Globals.PENALTY_ROOM_STABILITY)));

				this.debugMode = Boolean
						.parseBoolean(prop.getProperty(DEBUG_MODE_TAG, String.valueOf(Globals.DEBUG_MODE)));

				// Indicamos que los parametros han sido leidos correctamente
				paramsLoaded = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Cerramos el fichero de parametros
			if (in != null) {
				try {
					in.close();
				} catch (Exception ex) {
					// NADA
				}
			}
		}
		return paramsLoaded;
	}

	/**
	 * Validar si los parametros son correctos
	 * 
	 * @param ins
	 *            Instancia
	 * 
	 * @return True: parametros validos | False: no validos
	 */
	public boolean validateParams(Instance ins) {
		if (this.gaPopulationSize < 4) {
			return false;
		}
		if (this.gaIterations <= 0) {
			return false;
		}
		if (this.gaRuntime < 0) {
			return false;
		}
		if (this.gaTypeSelection != 1 && this.gaTypeSelection != 2) {
			return false;
		}
		if (this.gaCrossoverRate < 0.0 || this.gaCrossoverRate > 1.0) {
			return false;
		}
		if (this.gaCrossoverTimeslots < 1 || this.gaCrossoverTimeslots > ins.getTimeslots()) {
			return false;
		}
		if (this.gaMutationRate < 0.0 || this.gaMutationRate > 1.0) {
			return false;
		}
		if (this.gaHillClimbingRate < 0.0 || this.gaHillClimbingRate > 1.0) {
			return false;
		}
		if (this.penaltyRoomCapacity < 1 || this.penaltyMinWorkingDays < 1 || this.penaltyCurriculumCompacness < 1
				|| this.penaltyRoomStability < 1) {
			return false;
		}
		return true;
	}

	public int getGAPopulationSize() {
		return gaPopulationSize;
	}

	public void setGAPopulationSize(int gaPopulationSize) {
		this.gaPopulationSize = gaPopulationSize;
	}

	public int getGAIterations() {
		return gaIterations;
	}

	public void setGAIterations(int gaIterations) {
		this.gaIterations = gaIterations;
	}
	
	public int getGaRuntime() {
		return gaRuntime;
	}

	public void setGaRuntime(int gaRuntime) {
		this.gaRuntime = gaRuntime;
	}

	public int getGATypeSelection() {
		return gaTypeSelection;
	}

	public void setGATypeSelection(int gaTypeSelection) {
		this.gaTypeSelection = gaTypeSelection;
	}

	public float getGACrossoverRate() {
		return gaCrossoverRate;
	}

	public void setGACrossoverRate(float gaCrossoverRate) {
		this.gaCrossoverRate = gaCrossoverRate;
	}

	public int getGaCrossoverTimeslots() {
		return gaCrossoverTimeslots;
	}

	public void setGaCrossoverTimeslots(int gaCrossoverTimeslots) {
		this.gaCrossoverTimeslots = gaCrossoverTimeslots;
	}

	public float getGAMutationRate() {
		return gaMutationRate;
	}

	public void setGAMutationRate(float gaMutationRate) {
		this.gaMutationRate = gaMutationRate;
	}

	public float getGAHillClimbingRate() {
		return gaHillClimbingRate;
	}

	public void setGAHillClimbingRate(float gaHillClimbingRate) {
		this.gaHillClimbingRate = gaHillClimbingRate;
	}

	public int getPenaltyRoomCapacity() {
		return penaltyRoomCapacity;
	}

	public void setPenaltyRoomCapacity(int penaltyRoomCapacity) {
		this.penaltyRoomCapacity = penaltyRoomCapacity;
	}

	public int getPenaltyMinWorkingDays() {
		return penaltyMinWorkingDays;
	}

	public void setPenaltyMinWorkingDays(int penaltyMinWorkingDays) {
		this.penaltyMinWorkingDays = penaltyMinWorkingDays;
	}

	public int getPenaltyCurriculumCompacness() {
		return penaltyCurriculumCompacness;
	}

	public void setPenaltyCurriculumCompacness(int penaltyCurriculumCompacness) {
		this.penaltyCurriculumCompacness = penaltyCurriculumCompacness;
	}

	public int getPenaltyRoomStability() {
		return penaltyRoomStability;
	}

	public void setPenaltyRoomStability(int penaltyRoomStability) {
		this.penaltyRoomStability = penaltyRoomStability;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	public String toString() {
		return "Params [gaPopulationSize=" + gaPopulationSize + ", gaIterations=" + gaIterations + ", gaRuntime="
				+ gaRuntime + ", gaTypeSelection=" + gaTypeSelection + ", gaCrossoverRate=" + gaCrossoverRate
				+ ", gaCrossoverTimeslots=" + gaCrossoverTimeslots + ", gaMutationRate=" + gaMutationRate
				+ ", gaHillClimbingRate=" + gaHillClimbingRate + ", penaltyRoomCapacity=" + penaltyRoomCapacity
				+ ", penaltyMinWorkingDays=" + penaltyMinWorkingDays + ", penaltyCurriculumCompacness="
				+ penaltyCurriculumCompacness + ", penaltyRoomStability=" + penaltyRoomStability + ", debugMode="
				+ debugMode + "]";
	}
}
