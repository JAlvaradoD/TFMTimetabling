package es.unex.cum.tfmtimetabling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Solucion;
import es.unex.cum.tfmtimetabling.util.CheckFunctions;
import es.unex.cum.tfmtimetabling.util.FileUtils;
import es.unex.cum.tfmtimetabling.util.GAFunctions;
import es.unex.cum.tfmtimetabling.util.Globals;
import es.unex.cum.tfmtimetabling.util.MetaheuristicFunctions;
import es.unex.cum.tfmtimetabling.util.Params;
import es.unex.cum.tfmtimetabling.util.RuntimeControl;

/** Clase Principal
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Main {

	private static final String FORMAT_NAME_FILE = "%d-%d-%d-%3.2f-%d-%3.2f-%3.2f";

	public static void main(String[] args) {

		// Inicializacion de variables para leer los parametros
		String executionName = "";
		String pathParams = "";
		String pathInstance = Globals.INSTACE_PATH;
		String pathOutput = null;
		String pathOutputHtml = "";

		String pathOutputCSV = "output.csv";
		String outputCSVFormat = "%d;%d\n";

		// Parametros del algoritmo
		Params params = new Params();

		// Leemos los argumentos
		try {

			if (args.length >= 3 && args.length <= 5) {
				// Argumentos por fichero de parametros
				executionName = args[0];
				pathParams = args[1];
				pathInstance = args[2];
				if (args.length >= 4) {
					pathOutput = args[3];
				}
				if (args.length == 5) {
					pathOutputHtml = args[4];
				}
				// Leemos los parametros desde el fichero
				if (!params.loadParams(pathParams)) {
					System.out.println("Error al leer el fichero de parametros");
					System.exit(0);
				}
			} else if (args.length >= 10 && args.length <= 12) {
				// Argumentos por parametros dados
				executionName = args[0];
				params.setGAPopulationSize(Integer.parseInt(args[1]));
				params.setGAIterations(Integer.parseInt(args[2]));
				params.setGaRuntime(Integer.parseInt(args[3]));
				params.setGATypeSelection(Integer.parseInt(args[4]));
				params.setGACrossoverRate(Float.parseFloat(args[5]));
				params.setGaCrossoverTimeslots(Integer.parseInt(args[6]));
				params.setGAMutationRate(Float.parseFloat(args[7]));
				params.setGAHillClimbingRate(Float.parseFloat(args[8]));
				pathInstance = args[9];
				if (args.length >= 11) {
					pathOutput = args[10];
				}
				if (args.length == 12) {
					pathOutputHtml = args[11];
				}
			} else {
				printUsage();
				System.exit(0);
			}
		} catch (NumberFormatException e) {
			System.out.println("Error, parametros no válidos");
			System.exit(0);
		}

		// Leemos la instancia
		Instance ins = FileUtils.readInstanceFromFile(pathInstance);

		// Comprobamos que los parametros son validos
		if (!params.validateParams(ins)) {
			System.out.println("Error, parametros no válidos");
			System.exit(0);
		}

		// Generar el nombre de fichero segun los parametros y la fecha de ejecucion
		String autoFileName = String.format(Locale.US, FORMAT_NAME_FILE, params.getGAPopulationSize(),
				params.getGAIterations(), params.getGATypeSelection(), params.getGACrossoverRate(),
				params.getGaCrossoverTimeslots(), params.getGAMutationRate(), params.getGAHillClimbingRate());

		autoFileName = FileUtils.getFileNameWithoutExtension(pathInstance) + "--" + autoFileName.replace(".", "_")
				+ "--" + executionName;

		/*
		 * Generar el nombre del fichero de salida a partir de los parametros si no ha
		 * sido establecido en los argumentos
		 */
		if (pathOutput == null) {
			pathOutput = autoFileName + ".out";
		}

		// Generamos el nombre del fichero CSV a partir de los parametros
		pathOutputCSV = autoFileName + ".csv";

		// Inicializamos el fichero CSV
		FileUtils.writeTextFile(pathOutputCSV, "");

		// Generamos la poblacion inicial
		ArrayList<Solucion> actualPopulation = GeneticAlgorithm.initializePopulationRandom(ins, params);
		System.out.println("Poblacion Inicial Generada OK");
		Collections.sort(actualPopulation);

		// Iniciamos el control de tiempo
		RuntimeControl runtime = new RuntimeControl(params.getGaRuntime());
		runtime.startRuntime();

		// Hacemos las interacciones del algoritmo genetico
		for (int i = 0; i < params.getGAIterations() && !runtime.checkRuntime(); i++) {

			// Ejecutamos los operadores geneticos
			ArrayList<Solucion> newPopulation = GeneticAlgorithm.ejecucionOperadores(actualPopulation, params, ins);
			// Ejecutamos el algoritmo de búsqueda local
			actualPopulation = MetaheuristicFunctions.hillClimbingPopulation(newPopulation, ins, params);
			// Ordenar la poblacion de menor a mayor fitness
			Collections.sort(actualPopulation);
			// Indicamos el fitness por pantalla
			System.out.println("Generacion: " + (i + 1) + " - Mejor Fitness: " + actualPopulation.get(0).getFitness());
			// Escribimos en el fichero CSV el valor fitness de la poblacion actual
			FileUtils.appendTextFile(pathOutputCSV,
					String.format(outputCSVFormat, i + 1, actualPopulation.get(0).getFitness()));

		}

		// Imprimimos la mejor solucion obtenida
		System.out.println("Fitness Mejor Solucion: " + actualPopulation.get(0).getFitness());
		// GAFunctions.fitness(actualPopulation.get(0).getSolucion(), params, ins);
		System.out.println("Solucion Valida? "
				+ CheckFunctions.solucionEsValida(actualPopulation.get(0).getSolucion(), ins, params.isDebugMode()));
		GAFunctions.printSolucion(actualPopulation.get(0).getSolucion(), ins);

		// Guardamos en un fichero la solucion para el validador de ITC2007
		FileUtils.writeSolution(pathOutput, actualPopulation.get(0), ins);

		// Guardamos en un fichero HTML la solucion
		if (pathOutputHtml.length() > 0) {
			FileUtils.writeSolucionToHTML(actualPopulation.get(0).getSolucion(), actualPopulation.get(0).getFitness(),
					pathOutputHtml, ins);
		}
	}

	/**
	 * Imprimir como se usa el programa
	 * 
	 */
	private static void printUsage() {
		System.out.println("Modo 1: <ejecucion> <parametros_algoritmo> <instancia> [<solucion>] [<solucionhtml>]");
		System.out.println(" |-> ejecucion: nombre de la ejecucion");
		System.out.println(" |-> parametros_algoritmo: ficheros con los parametros a ejecutar");
		System.out.println(" |-> instancia: fichero con la instancia (ej: comp01.ctt)");
		System.out.println(" |-> solucion: fichero donde se almacenara la solucion (opcional)");
		System.out.println(" |-> solucion_html: solucion en formato html (opcional)");
		System.out.println("Modo 2: <ejecucion> <ga_poblacion> <ga_iteraciones> <ga_runtime> <ga_tipo_seleccion> ");
		System.out.println("<ga_ratio_cruce> <ga_timeslots_cruce> <ga_ratio_mutacion> <ga_ratio_hillclimbing>");
		System.out.println("<instancia> [<solucion>] [<solucionhtml>]");
		System.out.println(" |-> ejecucion: nombre de la ejecucion");
		System.out.println(" |-> ga_poblacion: tamaño poblacion algoritmo genetico");
		System.out.println(" |-> ga_iteraciones: iteraciones a realizar el algoritmo genetico");
		System.out.println(" |-> ga_runtime: tiempo de ejecucion maxima del algoritmo en minutos (0 = sin limite)");
		System.out.println(" |-> ga_tipo_seleccion: 1 - por torneo | 2 - por ruleta");
		System.out.println(" |-> ga_ratio_cruce: ratio cruze algoritmo genetico (0.0 - 1.0)");
		System.out.println(" |-> ga_timeslots_cruce: timeslots que se cruzaran en el algoritmo genetico");
		System.out.println(" |-> ga_ratio_mutacion: ratio mutacion algoritmo genetico (0.0 - 1.0)");
		System.out.println(" |-> ga_ratio_hillclimbing: ratio algoritmo hillclimbing (0.0 - 1.0)");
		System.out.println(" |-> instancia: fichero con la instancia (ej: comp01.ctt)");
		System.out.println(" |-> solucion: fichero donde se almacenara la solucion (opcional)");
		System.out.println(" |-> solucion_html: solucion en formato html (opcional)");
	}

}
