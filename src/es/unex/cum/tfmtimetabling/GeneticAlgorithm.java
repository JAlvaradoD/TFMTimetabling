package es.unex.cum.tfmtimetabling;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Lecture;
import es.unex.cum.tfmtimetabling.model.Solucion;
import es.unex.cum.tfmtimetabling.util.ArrayUtils;
import es.unex.cum.tfmtimetabling.util.CheckFunctions;
import es.unex.cum.tfmtimetabling.util.CourseUtils;
import es.unex.cum.tfmtimetabling.util.GAFunctions;
import es.unex.cum.tfmtimetabling.util.Params;

/**
 * Algoritmo Genetico
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class GeneticAlgorithm {

	/**
	 * Inicializar poblacion inicial
	 * 
	 * @param ins
	 *            Instancia
	 * @param params
	 *            Parametros del algoritmo
	 * 
	 * @return Poblacion Inicial
	 */
	public static ArrayList<Solucion> initializePopulationRandom(Instance ins, Params params) {
		// Array para almacenar la poblacion inicial
		ArrayList<Solucion> initialPopulation = new ArrayList<Solucion>();
		// Obtenemos la informacion necesaria de la instancia
		int days = ins.getDays();
		int timeslots = ins.getTimeslotsPerDay();
		int numRooms = ins.getRooms();
		// Room[] roomList = ins.getRoomsList();

		// Pruebas
		int numTimeslots = days * timeslots * numRooms;

		// Generamos los individuos (soluciones)
		for (int p = 0; p < params.getGAPopulationSize(); p++) {

			// Lista de Eventos (Asignaturas)
			ArrayList<Lecture> lectureList = ins.getLecturesList();
			Lecture[][] initialSolution = new Lecture[numRooms][days * timeslots];

			int numLecturesInserted = 0;
			int numLectures = lectureList.size();

			// Mientras haya eventos en la lista
			while (lectureList.size() > 0) {
				boolean inserted = false;
				String noInsertedReason = "";
				// Se coge una evento y se elimina de la lista
				int random = ThreadLocalRandom.current().nextInt(lectureList.size());
				Lecture lecture = lectureList.get(random);
				lectureList.remove(random);
				// Iniciamos la funciones del algoritmo de generacion
				CourseUtils courseUtils = new CourseUtils(lecture.getCourse(), ins);
				// System.out.println("Lecture: " + lecture.getId());
				// Se obtienen los dias disponibles de la asignatura
				ArrayList<Integer> daysAvailable = courseUtils.daysAvailableCourse(initialSolution);

				// Si no hay dias disponibles
				int timeout = 0;
				while (daysAvailable.size() == 0 && timeout < 1000) {
					// Muevo un evento de forma aleatoria
					courseUtils.moveLectureRandom(initialSolution);
					// Vuelvo a comprobar si hay dias disponibles para el evento
					daysAvailable = courseUtils.daysAvailableCourse(initialSolution);
					timeout++;
				}

				// Si no hay dias disponibles, se indica y se procede mas abajo a reiniciar la
				// solucion
				if (daysAvailable.size() == 0) {
					noInsertedReason = "No hay dias disponibles para la asignatura";
				}

				while (daysAvailable.size() > 0 && !inserted) {
					// Se coge un dia y se elimina de la lista
					int randomDay = ThreadLocalRandom.current().nextInt(daysAvailable.size());
					int day = daysAvailable.get(randomDay);
					daysAvailable.remove(randomDay);
					// System.out.println("Day: " + day);
					// Se obtienen los timeslots disponibles ese dia para la asignatura
					ArrayList<Integer> timeslotsAvailable = courseUtils.timeslotsAvailableCourseInDay(day,
							initialSolution);

					if (timeslotsAvailable.size() == 0) {
						noInsertedReason = "No hay timeslots disponibles para la asignatura";
					}

					while (timeslotsAvailable.size() > 0 && !inserted) {
						// Se obtiene un timeslot y se elimina de la lista
						int randomTimeslot = ThreadLocalRandom.current().nextInt(timeslotsAvailable.size());
						int timeslot = timeslotsAvailable.get(randomTimeslot);
						timeslotsAvailable.remove(randomTimeslot);
						// System.out.println("Timeslot: " + timeslot);
						// Se obtienen las aulas disponibles para ese timeslot
						ArrayList<Integer> roomsAvailable = courseUtils.roomAvailableInTimeslot(day, timeslot,
								initialSolution);

						if (roomsAvailable.size() == 0) {
							// No hay aulas disponibles para la asignatura
							noInsertedReason = "No hay aulas disponibles para la asignatura";
						}

						while (roomsAvailable.size() > 0 && !inserted) {
							// Se obtiene un Aula y se elimina de la lista
							int randomRoom = ThreadLocalRandom.current().nextInt(roomsAvailable.size());
							int roomId = roomsAvailable.get(randomRoom);
							roomsAvailable.remove(randomRoom);
							int t = (day * timeslots) + timeslot;
							// Se inserta el evento
							initialSolution[roomId][t] = lecture;
							inserted = true;
							numLecturesInserted++;
							if (params.isDebugMode()) {
								System.out.println("Evento: " + lecture.getId() + " insertado OK");
							}
						}
					}
				}
				if (!inserted) {
					// Si no se ha podido insertar el evento
					// Mensaje de depuracion
					if (params.isDebugMode()) {
						System.out.println("Evento: " + lecture.getId() + " no insertado!");
						System.out.println("Motivo: " + noInsertedReason);

						System.out.println("Se ha insertado " + numLecturesInserted + " de " + numLectures + " | Hay "
								+ numTimeslots + " timeslots");
						// GAFunctions.printSolucion(initialSolution, ins);
						System.out.println("Se reinicia la generacion del horario");
					}
					// Se reinicia la solucion y la lista de clases
					numLecturesInserted = 0;
					lectureList = ins.getLecturesList();
					initialSolution = new Lecture[numRooms][days * timeslots];
				}
			}
			// Guardamos el individuo (solucion) y calculamos su fitness inicial
			Solucion solucion = new Solucion(initialSolution, GAFunctions.fitness(initialSolution, params, ins));
			// Agergamos el individuo a la solucion
			initialPopulation.add(solucion);

			System.out.println("Individuo " + (p + 1) + " generado OK con fitness: " + solucion.getFitness());
		}
		return initialPopulation;
	}

	/**
	 * Ejecucion de los operadores de Cruce y Mutacion
	 * 
	 * @param actualPopulation
	 *            Poblacion actual
	 * @param ins
	 *            Instancia
	 * @param params
	 *            Parametros del algoritmo
	 * @return Nueva Poblacion/Generacion
	 */
	@SuppressWarnings("all")
	public static ArrayList<Solucion> ejecucionOperadores(ArrayList<Solucion> actualPopulation, Params params,
			Instance ins) {
		ArrayList<Solucion> newPopulation = new ArrayList<>();
		/*
		 * Elitismo (nos quedamos con las dos mejores soluciones y las agregamos a la
		 * siguiente generacion)
		 */
		newPopulation.add(actualPopulation.get(0));
		actualPopulation.remove(0);
		newPopulation.add(actualPopulation.get(0));
		actualPopulation.remove(0);

		int count = 0;

		// Cruce y Mutacion
		while (count < actualPopulation.size()) {
		
			Solucion[] padres;
			// Seleccionamos los padres
			if (params.getGATypeSelection() == 1) {
				// Seleccion por Torneo
				padres = GAFunctions.tournamentSelection2(actualPopulation);
			} else {
				// Seleccion por ruleta
				padres = GAFunctions.rouletteWheelSelection(actualPopulation);
			}
			count += 2;

			// Cruze y mutacion
			if (Math.random() <= params.getGACrossoverRate()) {
				// Hacemos el cruze de los dos padres
				Solucion[] hijos = crossoverOp2(padres[0], padres[1], params, ins);

				// Introducimos los hijos resultantes en la nueva generacion
				newPopulation.add(hijos[0]);
				newPopulation.add(hijos[1]);
			} else {
				// Introducimos los individuos tal cual en la nueva generacion
				newPopulation.add(padres[0]);
				newPopulation.add(padres[1]);
			}

		}
		return newPopulation;
	}

	/**
	 * Operador de Cruze 1 (se bloquea en ciertas instancias!!!)
	 * 
	 * @param indv1
	 *            Individuo Padre 1
	 * @param indv2
	 *            Individuo Padre 2
	 * @param params
	 *            Parametros del algoritmo
	 * @param ins
	 *            Inslancia
	 * @return Individuos Hijos (2)
	 */
	@SuppressWarnings("unused")
	private static Solucion[] crossoverOp1(Solucion indv1, Solucion indv2, Params params, Instance ins) {
		Solucion[] hijos = new Solucion[2];

		boolean cruzeEsValido = false;

		Lecture[][] individuo1;
		Lecture[][] individuo2;

		while (!cruzeEsValido) {
			// Obtenemos los padres
			individuo1 = GAFunctions.copySolutionArray(indv1.getSolucion());
			individuo2 = GAFunctions.copySolutionArray(indv2.getSolucion());

			/*
			 * System.out.println("Individuo1 - Solucion valida? " +
			 * CheckFunctions.solucionEsValida(individuo1, ins));
			 * System.out.println("Individuo2 - Solucion valida? " +
			 * CheckFunctions.solucionEsValida(individuo2, ins));
			 */

			// Cogemos un timeslot al azar de cada padre
			int timeslot1 = ThreadLocalRandom.current().nextInt(individuo1[0].length);
			int timeslot2 = ThreadLocalRandom.current().nextInt(individuo2[0].length);
			// Hacemos el cruze

			// System.out.println("Inicio Cruce...");
			// Creacion del hijo 1
			// System.out.println("Creacion Hijo 1");
			for (int room = 0; room < ins.getRooms(); room++) {
				if (individuo1[room][timeslot1] == null) {
					individuo1[room][timeslot1] = individuo2[room][timeslot2];
					// Pruebas
					/*
					 * if (individuo1[room][timeslot1] != null) {
					 * individuo1[room][timeslot1].setId("*" + individuo1[room][timeslot1].getId() +
					 * "*"); }
					 */
				}
			}

			// Creacion del hijo 2
			// System.out.println("Creacion Hijo 2");
			for (int room = 0; room < ins.getRooms(); room++) {
				if (individuo2[room][timeslot2] == null) {
					individuo2[room][timeslot2] = individuo1[room][timeslot1];
					// Pruebas
					/*
					 * if (individuo2[room][timeslot2] != null) {
					 * individuo2[room][timeslot2].setId("*" + individuo2[room][timeslot2].getId() +
					 * "*"); }
					 */
				}
			}

			// Verificamos si el cruze es válido
			cruzeEsValido = CheckFunctions.checkTimeslot(individuo1, timeslot1, ins, params.isDebugMode())
					&& CheckFunctions.checkTimeslot(individuo2, timeslot2, ins, params.isDebugMode());

			if (cruzeEsValido) {

				// System.out.println("Cruce valido");
				// System.out.println("Hijo 1 - Timeslot: " + timeslot1);
				// System.out.println("Hijo 2 - Timeslot: " + timeslot2);

				// Eliminamos las posibles clases duplocadas
				GAFunctions.removeDuplicateLectures(individuo1, timeslot1);
				GAFunctions.removeDuplicateLectures(individuo2, timeslot2);

				// Realizamos la mutacion
				if (Math.random() <= params.getGAMutationRate()) {
					// individuo1 = mutationOp1(individuo1, ins);
					if (params.isDebugMode()) {
						System.out.println("Se ha mutado un individuo");
					}
				}
				if (Math.random() <= params.getGAMutationRate()) {
					individuo2 = mutationOp(individuo2, ins, params.isDebugMode());
					if (params.isDebugMode()) {
						System.out.println("Se ha mutado un individuo");
					}
				}

				// Metemos los hijos en el array para devolverlos
				hijos[0] = new Solucion(individuo1, GAFunctions.fitness(individuo1, params, ins));
				hijos[1] = new Solucion(individuo2, GAFunctions.fitness(individuo2, params, ins));

			} else {
				/*
				 * System.out.println("Cruce no Valido"); GAFunctions.printSolucion(individuo1,
				 * ins); System.out.println(""); GAFunctions.printSolucion(individuo2, ins);
				 */
			}
			// FileUtils.writeCrossoverToHTML(indv1.getSolucion(),indv2.getSolucion(),
			// individuo1,individuo2, "cruce.html",timeslot1,timeslot2,cruzeEsValido,ins);
			// System.exit(0);

		}
		// System.out.println("Hijo1 - Solucion valida? " +
		// CheckFunctions.solucionEsValida(hijos[0].getSolucion(), ins));
		// GAFunctions.printSolucion(hijos[0].getSolucion(), ins);
		// System.out.println("");
		// System.out.println("Hijo2 - Solucion valida? " +
		// CheckFunctions.solucionEsValida(hijos[1].getSolucion(), ins));
		// GAFunctions.printSolucion(hijos[1].getSolucion(), ins);

		// System.exit(0);

		return hijos;
	}

	/**
	 * Operador de Cruze 2
	 * 
	 * @param indv1
	 *            Individuo Padre 1
	 * @param indv2
	 *            Individuo Padre 2
	 * @param params
	 *            Parametros del algoritmo
	 * @param ins
	 *            Inslancia
	 * @return Individuos Hijos (2)
	 */
	private static Solucion[] crossoverOp2(Solucion indv1, Solucion indv2, Params params, Instance ins) {
		Solucion[] hijos = new Solucion[2];

		Lecture[][] individuo1;
		Lecture[][] individuo2;

		/*
		 * En primer lugar copiamos los padres para poder trabajar con ellos y una vez
		 * terminado el cruce seran los hijos resultantes
		 */
		individuo1 = GAFunctions.copySolutionArray(indv1.getSolucion());
		individuo2 = GAFunctions.copySolutionArray(indv2.getSolucion());

		// Generamos varios timeslots aleatorios
		int[] randomTimeslots = ArrayUtils.generateRandomArrayWithValuesNoRepeating(params.getGaCrossoverTimeslots(),
				individuo1[0].length);

		// int nLecturesCross = 0;

		for (int i = 0; i < randomTimeslots.length; i++) {
			// Seleccionamos un timeslot de forma aleatoria
			int randomTimeslot = randomTimeslots[i];

			/*
			 * Hacemos el cruce en el timeslot completo, es decir, recorremos todas las
			 * aulas del timeslot
			 */
			for (int room = 0; room < ins.getRooms(); room++) {

				// Obtemeos las clases que se van a cruzar
				Lecture l1 = individuo1[room][randomTimeslot];
				Lecture l2 = individuo2[room][randomTimeslot];

				boolean cross = false; // Pruebas

				// Ahora se pueden dar una serie de casos:

				if (l1 != null && l2 != null) {
					/*
					 * 1º Caso: existen eventos en ambos individuos (no hay huecos)
					 */

					// Comprobamos si se pueden intercambiar
					if (CheckFunctions.checkLecture(individuo1, l2, room, randomTimeslot, ins, false)
							&& CheckFunctions.checkLecture(individuo2, l1, room, randomTimeslot, ins, false)) {

						// System.out.println("Cruce l1 y l2");

						// Hacemos una copia de los individuos en caso de que no se haga correctamente
						// el cruce
						Lecture[][] individuo1_copia = GAFunctions.copySolutionArray(individuo1);
						Lecture[][] individuo2_copia = GAFunctions.copySolutionArray(individuo2);

						// Hacemos el cruce
						individuo1[room][randomTimeslot] = l2;
						individuo2[room][randomTimeslot] = l1;

						if (!l1.getId().equals(l2.getId())) {
							// Proceso de restauracion (solo si ambas clases no son la misma)
							// =======================
							// Eliminamos los eventos duplicados
							GAFunctions.removeDuplicateLecture(individuo1, room, randomTimeslot);
							GAFunctions.removeDuplicateLecture(individuo2, room, randomTimeslot);
							// Intentamos mover los eventos sobreescritos en ambos individuos y si no es
							// posible restablecemos el individuo
							if (!CheckFunctions.insertLecture(individuo1, l1, ins, params.isDebugMode())) {		
								individuo1 = GAFunctions.copySolutionArray(individuo1_copia);
							}

							if (!CheckFunctions.insertLecture(individuo2, l2, ins, params.isDebugMode())) {
								individuo2 = GAFunctions.copySolutionArray(individuo2_copia);
							}

							cross = true;
						}
					}
				} else if (l1 == null && l2 != null) {
					/*
					 * 2º Caso: hay un hueco en el primer individuo y existe evento en el segundo
					 * individuo
					 */
					// Comprobamos si se puede copiar el evento del individo 2 al individuo 1
					if (CheckFunctions.checkLecture(individuo1, l2, room, randomTimeslot, ins, false)) {
						// Hacemos el cruce (copia)
						individuo1[room][randomTimeslot] = l2;
						// Eliminamos el evento duplicado
						GAFunctions.removeDuplicateLecture(individuo1, room, randomTimeslot);
						cross = true;
					}
				} else if (l1 != null && l2 == null) {
					/*
					 * 3º Caso: existe evento en el primer individuo y hay un hueco en el segundo
					 * individuo
					 */
					// Comprobamos si se puede copiar el evento del individo 1 al individuo 2
					if (CheckFunctions.checkLecture(individuo2, l1, room, randomTimeslot, ins, false)) {
						// Hacemos el cruce (copia)
						individuo2[room][randomTimeslot] = l1;
						// Eliminamos el evento duplicado
						GAFunctions.removeDuplicateLecture(individuo2, room, randomTimeslot);
						cross = true;
					}
				}
				
				if (params.isDebugMode()) {
					// Mensajes de depuracion
					if (cross) {
						System.out.println("Solucion Valida I1? "
								+ CheckFunctions.solucionEsValida(individuo1, ins, params.isDebugMode()));
						System.out.println("Solucion Valida I2? "
								+ CheckFunctions.solucionEsValida(individuo2, ins, params.isDebugMode()));
					} else {
						System.out.println("Cruce no valido");
					}
				}
				

			}

		}


		// Realizamos la mutacion
		if (Math.random() <= params.getGAMutationRate()) {
			individuo1 = mutationOp(individuo1, ins, params.isDebugMode());
			if (params.isDebugMode()) {
				System.out.println("Se ha mutado un individuo");
			}
		}
		if (Math.random() <= params.getGAMutationRate()) {
			individuo2 = mutationOp(individuo2, ins, params.isDebugMode());
			if (params.isDebugMode()) {
				System.out.println("Se ha mutado un individuo");
			}
		}

		/*
		 * Los individuos con los que hemos trabajado los devolvemos como los hijos
		 * resultantes del cruce
		 */
		hijos[0] = new Solucion(individuo1, GAFunctions.fitness(individuo1, params, ins));
		hijos[1] = new Solucion(individuo2, GAFunctions.fitness(individuo2, params, ins));

		return hijos;
	}

	/**
	 * Operador de Mutacion
	 * 
	 * @param solution
	 *            Solucion a mutar
	 * @param ins
	 *            Instancia
	 * @param debugMode
	 *            Mostrar mensajes de depuracion
	 * @return Solucion (Inididuo) mutado
	 */
	public static Lecture[][] mutationOp(Lecture[][] solution, Instance ins, boolean debugMode) {
		// Obtenemos el numero de aulas y timeslots
		int rooms = solution.length;
		int timeslots = solution[0].length;

		// Inicializamos las variables
		int x1, x2, y1, y2;
		Lecture l1, l2;
		Lecture[][] mutatedSolution = new Lecture[rooms][timeslots];
		boolean mutacionEsValida = false;

		// Mientras la mutacion no sea válida
		while (!mutacionEsValida) {
			// Copiamos la solucion para trabajar con ella y no sobreescribir la original
			mutatedSolution = GAFunctions.copySolutionArray(solution);

			// Obtenemos los eventos de dos timeslots de forma aleatoria
			x1 = ThreadLocalRandom.current().nextInt(rooms);
			y1 = ThreadLocalRandom.current().nextInt(timeslots);
			x2 = ThreadLocalRandom.current().nextInt(rooms);
			y2 = ThreadLocalRandom.current().nextInt(timeslots);

			l1 = solution[x1][y1];
			l2 = solution[x2][y2];

			// Se realiza el intercambio (Mutacion)
			mutatedSolution[x1][y1] = l2;
			mutatedSolution[x2][y2] = l1;

			// Comprobamos que la solucion es válida
			mutacionEsValida = CheckFunctions.solucionEsValida(mutatedSolution, ins, debugMode);

			if (debugMode) {
				if (!mutacionEsValida) {
					System.out.println("Mutacion no válida");
				}
			}

		}

		// Devolvemos la solucion mutada
		return mutatedSolution;
	}
}
