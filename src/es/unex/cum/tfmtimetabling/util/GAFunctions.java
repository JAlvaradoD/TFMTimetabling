package es.unex.cum.tfmtimetabling.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import es.unex.cum.tfmtimetabling.model.Course;
import es.unex.cum.tfmtimetabling.model.Curricula;
import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Lecture;
import es.unex.cum.tfmtimetabling.model.Room;
import es.unex.cum.tfmtimetabling.model.Solucion;

/** Funciones para el Algoritmo Genetico
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class GAFunctions {

	/**
	 * Eliminar los eventos duplicados salvo los del timeslot indicado
	 * 
	 * @param solution
	 *            Solucion
	 * @param timeslot
	 *            Timeslot del que no se eliminaran duplicados
	 */
	public static void removeDuplicateLectures(Lecture[][] solution, int timeslot) {
		for (int room = 0; room < solution.length; room++) {
			Lecture l1 = solution[room][timeslot];
			if (l1 != null) {
				// Obtenemos el id del evento
				String lectureId = l1.getId();
				// Buscamos los posibles eventos duplicados fuera del timeslot
				for (int r = 0; r < solution.length; r++) {
					for (int t = 0; t < solution[r].length; t++) {
						Lecture l2 = solution[r][t];
						if (l2 != null && t != timeslot && l2.getId().equals(lectureId)) {
							// Si existe un evento duplicado, lo eliminamos
							solution[r][t] = null;
						}
					}
				}

			}
		}
	}

	/**
	 * Eliminar los eventos duplicados especificando el aula y timeslot del evento
	 * que no se debe eliminar
	 * 
	 * @param solution
	 *            Solucion
	 * @param room
	 *            Aula
	 * @param timeslot
	 *            Timeslot
	 */
	public static void removeDuplicateLecture(Lecture[][] solution, int room, int timeslot) {
		Lecture lecture = solution[room][timeslot];
		if (lecture != null) {
			// Obtenemos el id del evento
			String lectureId = lecture.getId();
			// Buscamos los posibles eventos duplicados
			for (int r = 0; r < solution.length; r++) {
				for (int t = 0; t < solution[r].length; t++) {
					Lecture l2 = solution[r][t];
					if (l2 != null) {
						if (!(r == room && t == timeslot) && l2.getId().equals(lectureId)) {
							// Si existe un evento duplicado, lo eliminamos
							solution[r][t] = null;
						}
					}
				}
			}
		}
	}

	/**
	 * Seleccion de individuos por torneo
	 * 
	 * @param actualPopulation
	 *            Poblacion actual
	 * @return Individuos (2) seleccionados
	 */
	public static Solucion[] tournamentSelection2(ArrayList<Solucion> actualPopulation) {
		Solucion[] returnIndvs = new Solucion[2];
		// Numero de rondas
		int nRondas = 2;
		// Numero de individuos (2 ^ Rondas)
		int nIndv = (int) Math.pow(2, nRondas);
		// Seleccionamos los individuos que participaran en el torneo
		int[] selectedIndvs = ArrayUtils.generateRandomArrayWithValuesNoRepeating(nIndv, actualPopulation.size());

		// Realizamos las rondas del torneo
		for (int r = 0; r < nRondas - 1; r++) {
			int[] roundSelectedIndv = new int[selectedIndvs.length / 2];
			for (int i = 0; i < roundSelectedIndv.length; i++) {
				// Seleccionados a 2 individuos
				Solucion indv1 = actualPopulation.get(selectedIndvs[i * 2]);
				Solucion indv2 = actualPopulation.get(selectedIndvs[(i * 2) + 1]);

				// Nos quedamos con el mejor de los 2
				if (indv1.getFitness() < indv2.getFitness()) {
					roundSelectedIndv[i] = selectedIndvs[i * 2];
				} else {
					roundSelectedIndv[i] = selectedIndvs[(i * 2) + 1];
				}
			}
			selectedIndvs = roundSelectedIndv;
		}

		// Devolvemos los individuos ganadores del torneo
		returnIndvs[0] = actualPopulation.get(selectedIndvs[0]);
		returnIndvs[1] = actualPopulation.get(selectedIndvs[1]);

		return returnIndvs;

	}

	/**
	 * Seleccion de individuos por Ruleta
	 * 
	 * @param actualPopulation
	 *            Poblacion actual
	 * @return Individuos (2) seleccionados
	 */
	public static Solucion[] rouletteWheelSelection(ArrayList<Solucion> actualPopulation) {
		Solucion[] returnIndvs = new Solucion[2];
		int populationSize = actualPopulation.size();

		for (int i = 0; i < 2; i++) {
			int[] inverseFitness = new int[populationSize];
			double[] weights = new double[populationSize];
			int idxIndv1 = -1;
			int totalWeights = 0;

			boolean findIndv = false;

			// Obtenemos el fitness minimo y maximo
			int minFitness = -1;
			int maxFitness = 0;
			for (int j = 0; j < populationSize; j++) {
				if (j != idxIndv1) {
					int fitness = actualPopulation.get(j).getFitness();
					if (fitness > maxFitness) {
						maxFitness = fitness;
					}
					if (minFitness == -1 || fitness < minFitness) {
						minFitness = fitness;
					}
				}
			}
			
			

			// Calculamos los pesos de los individuos de forma inversa (minimizado)
			int minMaxFitness = minFitness + maxFitness;
			
			for (int j = 0; j < populationSize; j++) {
				if (j != idxIndv1) {
					inverseFitness[j] = minMaxFitness - actualPopulation.get(j).getFitness();
					totalWeights += inverseFitness[j];
				}
			}
			
			// Calculamos los pesos de forma absoluta de cada individuo (entre 0 y 1)
			double sumWeight=0;
			for (int j = 0; j < populationSize; j++) {
				if (j != idxIndv1) {
					sumWeight += (inverseFitness[j]/(double)totalWeights);
					weights[j] = sumWeight;
				}
			}

			// Seleccionamos el individuo
			double rouletteWheel = Math.random();

			for (int j = 0; j < populationSize && !findIndv; j++) {
				if (j != idxIndv1) {
					if (weights[j] >= rouletteWheel) {
						idxIndv1 = j;
						findIndv = true;
						returnIndvs[i] = actualPopulation.get(j);
					}
				}
			}
		}
		return returnIndvs;
	}

	/**
	 * Funcion fitness
	 * 
	 * @param solution
	 *            Solucion a evaular
	 * @param params
	 *            Parametros del algoritmo
	 * @param ins
	 *            Inslancia
	 * @return Valor fitness
	 */
	public static int fitness(Lecture[][] solution, Params params, Instance ins) {
		int fitness = 0;

		Course[] courses = ins.getCoursesList();
		Curricula[] curriculas = ins.getCurriculaList();
		Room[] rooms = ins.getRoomsList();
		// Fintness para cada Soft Constrains
		int fitnessRoomCapacity = 0;
		int fitnessMinWorkingDays = 0;
		int fitnessCurriculumCompacness = 0;
		int fitnessRoomStability = 0;

		// Soft Constrains ====================================================

		// Inicializar variables ------------------
		// MinWorkingDays
		int[] workingDay = new int[ins.getDays()];
		// RoomStability
		HashSet<String> aulasDifAsignatura = null;
		// CurriculumCompactness
		int timeslotsPerDay = ins.getTimeslotsPerDay();
		int[] lecturesDay = new int[ins.getDays()];

		// ----------------------------------------

		for (Course course : courses) {

			Arrays.fill(workingDay, 0);
			Arrays.fill(lecturesDay, 0);
			aulasDifAsignatura = new HashSet<String>();

			for (int r = 0; r < solution.length; r++) {
				for (int t = 0; t < solution[r].length; t++) {
					Lecture lecture = solution[r][t];

					if (lecture != null && lecture.getCourse().getCourseId().equals(course.getCourseId())) {
						// Dia y Timeslot
						int day = t / ins.getTimeslotsPerDay();

						// Room Capacity (fitness)
						fitnessRoomCapacity += evaluateRoomOcupacy(lecture, rooms[r]);

						// Operaciones para el resto de Soft Constrains
						// Establecer los dias que se da la asignatura (MinWorkingDays)
						workingDay[day] = 1;

						// Agregar el aula que se da la asignatura en ese momento (RoomStability)
						aulasDifAsignatura.add(rooms[r].getRoomId());
					}

				}
			}

			// MinWorkignDays
			int workignDays = 0;
			for (int i = 0; i < workingDay.length; i++) {
				if (workingDay[i] == 1) {
					workignDays++;
				}
			}
			if (course.getMinWorkingDays() > workignDays) {
				fitnessMinWorkingDays += (course.getMinWorkingDays() - workignDays);
			}

			// RoomStability
			if (aulasDifAsignatura.size() > 1) {
				fitnessRoomStability += aulasDifAsignatura.size() - 1;
			}

		}

		// Curriculum Compactness
		int[] curriculumSchedule = new int[ins.getTimeslots()];
		for (Curricula curriculum : curriculas) {
			Arrays.fill(curriculumSchedule, 0);
			for (int i = 0; i < solution.length; i++) {
				for (int j = 0; j < solution[i].length; j++) {
					Lecture lecture = solution[i][j];
					if (lecture != null && sameCurriculum(lecture, curriculum)) {
						curriculumSchedule[j] = 1;
					}
				}
			}

			for (int t = 0; t < curriculumSchedule.length; t++) {
				if (curriculumSchedule[t] > 0) {
					if (t % timeslotsPerDay == 0 && curriculumSchedule[t + 1] == 0) {
						fitnessCurriculumCompacness++;
					} else if (t % timeslotsPerDay == timeslotsPerDay - 1 && curriculumSchedule[t - 1] == 0) {
						fitnessCurriculumCompacness++;
					} else if ((t % timeslotsPerDay > 0 && t % timeslotsPerDay < timeslotsPerDay - 1)
							&& (curriculumSchedule[t + 1] == 0 && curriculumSchedule[t - 1] == 0)) {
						fitnessCurriculumCompacness++;
					}
				}
			}
		}

		// Se aplican los puntos de penalizacion
		fitnessRoomCapacity *= params.getPenaltyRoomCapacity();
		fitnessMinWorkingDays *= params.getPenaltyMinWorkingDays();
		fitnessCurriculumCompacness *= params.getPenaltyCurriculumCompacness();
		fitnessRoomStability *= params.getPenaltyRoomStability();

		// Se calcula el fitness a partir de las penalizaciones de las Soft Constrains
		fitness = fitnessRoomCapacity + fitnessMinWorkingDays + fitnessCurriculumCompacness + fitnessRoomStability;

		return fitness;

	}

	/**
	 * Comprobar que un evento pertenece a una titulacion
	 * 
	 * 
	 * @param lecture
	 *            Evento a comprobar
	 * @param curricula
	 *            Titulacion
	 * @return True si el evento pertenece a esa titulacion
	 */
	private static boolean sameCurriculum(Lecture lecture, Curricula curricula) {
		String[] memberId = curricula.getMemberId();

		for (int i = 0; i < memberId.length; i++) {
			if (lecture.getCourse().getCourseId().equals(memberId[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Evaular la ocupacion de un aula en una clase segun el numero de estudiantes
	 * 
	 * @param lecture
	 *            Evento
	 * @param room
	 *            Aula donde se da la asignatura
	 * @return Penalizacion (Sobreocupacion, Infraocupacion)
	 */
	private static int evaluateRoomOcupacy(Lecture lecture, Room room) {
		int penalty = 0;
		int roomCapacity = room.getCapacity();
		int students = lecture.getCourse().getStudents();
		if (students > roomCapacity) {
			penalty += (students - roomCapacity);
		}
		if (students < roomCapacity) {
			// NADA
		}
		return penalty;
	}

	/**
	 * Copiar array de solucion
	 * 
	 * @param solution
	 *            Array a copiar
	 * @return Array copia
	 */
	public static Lecture[][] copySolutionArray(Lecture[][] solution) {
		Lecture[][] copy = new Lecture[solution.length][solution[0].length];
		for (int r = 0; r < solution.length; r++) {
			for (int t = 0; t < solution[r].length; t++) {
				copy[r][t] = solution[r][t];
			}
		}
		return copy;
	}

	/**
	 * Imprimir solucion
	 * 
	 * @param solution
	 *            Solucion a imprimir
	 * @param ins
	 *            Instancia
	 */
	public static void printSolucion(Lecture[][] solution, Instance ins) {
		for (int i = 0; i < solution.length; i++) {
			for (int j = 0; j < solution[i].length; j++) {
				if (j % ins.getTimeslotsPerDay() == 0) {
					System.out.print("|");
				}
				if (solution[i][j] != null) {
					System.out.print(" " + solution[i][j].getId() + " |");
				} else {
					System.out.print(" ------- |");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Convertir Solucion a tabla HTML
	 * 
	 * @param solution
	 *            Solucion
	 * @param ins
	 *            Instancia
	 * @param timeslot
	 *            Timeslot a marcar (opcional)
	 * @param colorTimeslot
	 *            (opcional)
	 * @return Tabla HTML con la solucion
	 */
	public static String solutionToHtml(Lecture[][] solution, Instance ins, Integer timeslot, String colorTimeslot) {
		if (timeslot == null) {
			timeslot = -1;
		}
		Room[] rooms = ins.getRoomsList();
		StringBuilder html = new StringBuilder();
		html.append("<table border=\"1\" style=\"text-align: center;\">");
		html.append("<tr>");
		html.append("<th>Aula</th>");
		for (int i = 0; i < ins.getDays(); i++) {
			html.append("<th colspan=\"" + ins.getTimeslotsPerDay() + "\">Dia " + (i + 1) + "</th>");
		}
		html.append("</th>");
		for (int i = 0; i < solution.length; i++) {
			html.append("<tr>");
			html.append("<th>" + rooms[i].getRoomId() + "</th>");
			for (int j = 0; j < solution[i].length; j++) {
				html.append("<td");
				if (j == timeslot && colorTimeslot != null) {
					html.append(" style=\"background-color: " + colorTimeslot + "\">");
				} else {
					html.append(">");
				}
				if (solution[i][j] != null) {
					html.append(solution[i][j].getId());
				} else {
					html.append("-");
				}
				html.append("</td>");
			}
			html.append("</tr>");
		}
		html.append("</table>");
		return html.toString();
	}
}
