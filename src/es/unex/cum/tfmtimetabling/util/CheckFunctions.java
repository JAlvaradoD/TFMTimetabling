package es.unex.cum.tfmtimetabling.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import es.unex.cum.tfmtimetabling.model.Course;
import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Lecture;

/**
 * Funciones de comprobacion y validacion
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class CheckFunctions {

	/**
	 * Comprobar que no se impartan dos asignaturas de una misma titulacion/mismo
	 * profesor en el mismo timeslot de un dia en concreto
	 * 
	 * @param solution
	 *            Solucion
	 * @param course
	 *            Asignatura a comprobar
	 * @param timeslot
	 *            Timeslot Eje Y Matriz Solucion
	 * @param ins
	 *            Instancia
	 * @return True si ya se imparte una asignatura de la misma titulacion en el
	 *         mismo timeslot | False en caso contrario
	 * 
	 */
	public static int checkCurriculaSameTimeslot(Lecture[][] solution, Course course, int timeslot, Instance ins) {
		String[] curriculumId = ins.getCurriculumIdOfCourse(course.getCourseId());
		for (int room = 0; room < solution.length; room++) {
			if (solution[room][timeslot] != null) {

				// Comprobar que haya otra asigantura de la misma titulacion en el mismo
				// timeslot
				String[] curriculumIdSolution = solution[room][timeslot].getCurriculumId();
				for (int k = 0; k < curriculumId.length; k++) {
					for (int m = 0; m < curriculumIdSolution.length; m++) {
						if (curriculumId[k].equals(curriculumIdSolution[m])) {
							// return true;
							return -1;
						}
					}
				}

				// Comprobar si el profesor imparte una asignatura en el mismo timeslot
				if (course.getTeacher().equals(solution[room][timeslot].getCourse().getTeacher())) {
					// return true;
					return -2;
				}

			}
		}
		// return false;
		return 0;
	}

	/**
	 * Comprobar si el evento puede ir en un espacio en concreto especificando el
	 * Aula y el Timeslot
	 * 
	 * @param solution
	 *            Solucion
	 * @param lecture
	 *            Evento
	 * @param room
	 *            Aula especificada
	 * @param timeslot
	 *            Timeslot especificado
	 * @param ins
	 *            Instancia
	 * @param ignoreTimeslotAvailability
	 *            Ignorar si existe una asignatura en el espacio (aula y timeslot)
	 *            indicado
	 * @return True si el evento puede ir en el espacio en concreto
	 */
	public static boolean checkLecture(Lecture[][] solution, Lecture lecture, int room, int timeslot, Instance ins,
			boolean ignoreTimeslotAvailability) {
		String[] curriculumId1 = ins.getCurriculumIdOfCourse(lecture.getCourse().getCourseId());
		boolean[][] availability = ins.courseIsAvailable(lecture.getCourse().getCourseId());
		int day = timeslot / ins.getTimeslotsPerDay();
		int tDay = timeslot % ins.getTimeslotsPerDay();

		// Comprobamos que el timeslot este vacio
		if (ignoreTimeslotAvailability && solution[room][timeslot] != null) {
			return false;
		}

		// Comprobamos disponibilidad primero
		if (!availability[day][tDay]) {
			return false;
		}

		// Despues verificamos el resto de Hards Constrains
		for (int room2 = 0; room2 < ins.getRooms(); room2++) {
			Lecture lecture2 = solution[room2][timeslot];
			if (room2 != room && lecture2 != null) {
				// Comprobar que el profesor imparta una unica asignatura en el timeslot
				if (lecture.getCourse().getTeacher().equals(lecture2.getCourse().getTeacher())) {
					return false;
				}
				// Comprobar si existe asigantura de la misma titulacion
				String[] curriculumId2 = lecture2.getCurriculumId();
				for (int i = 0; i < curriculumId1.length; i++) {
					for (int j = 0; j < curriculumId2.length; j++) {
						if (curriculumId1[i].equals(curriculumId2[j])) {
							return false;
						}
					}
				}
			}

		}

		return true;
	}

	/**
	 * Comprobar si se puede mover un evento
	 * 
	 * @param solution
	 *            Solucion
	 * @param lecture
	 *            Evento
	 * @param room
	 *            Aula actual donde se ubica la clase a mover
	 * @param timeslot
	 *            Timeslot actual donde se ubica la clase a mover
	 * @param ins
	 *            Instancia
	 * @return Posicion a la que se puede mover la lectura | Null si no se puede
	 *         mover
	 */
	public static Integer[] lectureCanBeMoved(Lecture[][] solution, Lecture lecture, int room, int timeslot,
			Instance ins) {
		// Inicializamos las variables
		Integer[] slot = new Integer[2];
		final int numTimeslots = ins.getTimeslotsPerDay() * ins.getDays();
		int timeout = 0;

		// Obtenemos un aula y timeslot de forma aleatoria
		int randomRoom = ThreadLocalRandom.current().nextInt(ins.getRooms());
		int randomTimeslot = ThreadLocalRandom.current().nextInt(numTimeslots);

		// Mientras el aula y timeslot sea diferentes al actual y el evento no se pueda
		// mover
		while (!checkLecture(solution, lecture, randomRoom, randomTimeslot, ins, true)) {

			// Obtenemos otro aula y timeslot de forma aleatoria
			randomRoom = ThreadLocalRandom.current().nextInt(ins.getRooms());
			randomTimeslot = ThreadLocalRandom.current().nextInt(numTimeslots);

			// Si no podemos mover el aula (timeout) paramos el metodo devolviendo null
			timeout++;
			if (timeout >= 10000) {
				return null;
			}

		}

		// Devolvemos la posicion donde se puede mover el evento
		slot[0] = randomRoom;
		slot[1] = randomTimeslot;

		return slot;
	}

	/**
	 * Compronar si se puede insertar una clase en la solucion
	 * 
	 * @param solution
	 *            Solucion
	 * @param lecture
	 *            Evento
	 * @param ins
	 *            Instancia
	 * @param debugMode
	 *            Mostrar mensajes de depuracion
	 * @return True si se ha podido insertar | False si no
	 */
	public static boolean insertLecture(Lecture[][] solution, Lecture lecture, Instance ins, boolean debugMode) {

		// Inicializamos las variables
		final int numTimeslots = ins.getTimeslotsPerDay() * ins.getDays();
		int timeout = 0;

		// Obtenemos un aula y timeslot de forma aleatoria
		int randomRoom = ThreadLocalRandom.current().nextInt(ins.getRooms());
		int randomTimeslot = ThreadLocalRandom.current().nextInt(numTimeslots);

		// Mientras el evento no se pueda insertar
		while (!checkLecture(solution, lecture, randomRoom, randomTimeslot, ins, true)) {

			// Obtenemos otro aula y timeslot de forma aleatoria
			randomRoom = ThreadLocalRandom.current().nextInt(ins.getRooms());
			randomTimeslot = ThreadLocalRandom.current().nextInt(numTimeslots);

			// Si no se pueda insertar el aula (timeout) devolvemos false
			timeout++;
			if (timeout >= 10000) {
				return false;
			}

		}

		// Insertamos el evento
		solution[randomRoom][randomTimeslot] = lecture;
		return true;

	}

	/**
	 * Comprobar que la solucion es valida
	 * 
	 * @param solution
	 *            Solucion
	 * @param ins
	 *            Instance
	 * @param debugMode
	 *            Mostrar mensajes de depuracion
	 * @return True si la solucion es valida | False si no lo es
	 */
	public static boolean solucionEsValida(Lecture[][] solution, Instance ins, boolean debugMode) {

		HashMap<String, Integer> courseLectures = new HashMap<>();
		Course[] courses = ins.getCoursesList();

		for (int timeslot = 0; timeslot < solution[0].length; timeslot++) {

			// Comprobamos el timeslot
			if (!checkTimeslot(solution, timeslot, ins, debugMode)) {
				// System.out.println("Timeslot no valido");
				return false;
			}

			// Comprobamos el numero de asignaturas
			for (int room = 0; room < solution.length; room++) {
				Lecture l = solution[room][timeslot];
				if (l != null) {
					String courseId = l.getCourse().getCourseId();
					// Numero de eventos por asignatura
					if (courseLectures.get(courseId) != null) {
						courseLectures.put(courseId, courseLectures.get(courseId) + 1);
					} else {
						courseLectures.put(courseId, 1);
					}
				}
			}
		}

		// Comprobar el numero de eventos por asignatura
		for (Course course : courses) {
			if (courseLectures.get(course.getCourseId()) != null) {
				if (courseLectures.get(course.getCourseId()) != course.getLectures()) {
					if (debugMode) {
						System.out.println("Numero de eventos no valido para la asignatura: " + course.getCourseId());
					}
					return false;

				}
			} else {
				if (debugMode) {
					System.out.println("Numero de eventos no valido para la asignatura");
				}
				return false;
			}
		}

		return true;
	}

	/**
	 * Comprobar un timeslot en concreto si es valido
	 * 
	 * @param solution
	 *            Solucion
	 * @param timeslot
	 *            Timeslot
	 * @param ins
	 *            Instancia
	 * @param debugMode
	 *            Mostrar mensajes de depuracion
	 * @return True si el timeslot es valido | False si no lo es
	 */
	public static boolean checkTimeslot(Lecture[][] solution, int timeslot, Instance ins, boolean debugMode) {
		HashSet<String> teachers = new HashSet<>();
		int day = timeslot / ins.getTimeslotsPerDay();
		int tDay = timeslot % ins.getTimeslotsPerDay();

		// System.out.println("Timeslot" + timeslot + " = Day: " + day + " | tDay: " +
		// tDay);

		for (int room = 0; room < ins.getRooms(); room++) {
			Lecture l = solution[room][timeslot];
			if (l != null) {
				boolean[][] availability = ins.courseIsAvailable(l.getCourse().getCourseId());
				// Comprobar la disponibilidad de la asignatura

				if (!availability[day][tDay]) {
					if (debugMode) {
						System.out.println(
								"Asigantura " + l.getId() + " no Disponible: Dia " + day + " | timeslot: " + tDay);
					}
					return false;
				}

				// Comprobamos si el mismo profesor da en diferentes aulas en el mismo timeslot
				String teacher = l.getCourse().getTeacher();
				if (teachers.contains(teacher)) {
					if (debugMode) {
						System.out
								.println("Profesor imparte en otro evento en el mismo timeslot (T: " + timeslot + ")");
					}
					return false;
				} else {
					teachers.add(teacher);
				}

				// Comprobar que haya otra asigantura de la misma titulacion en el mismo
				// timeslot
				String[] curriculumId1 = l.getCurriculumId();
				for (int room2 = 0; room2 < ins.getRooms(); room2++) {
					Lecture l2 = solution[room2][timeslot];
					if (room != room2 && l2 != null) {
						String[] curriculumId2 = l2.getCurriculumId();
						for (int i = 0; i < curriculumId1.length; i++) {
							for (int j = 0; j < curriculumId2.length; j++) {
								if (curriculumId1[i].equals(curriculumId2[j])) {
									if (debugMode) {
										System.out.println("Dia: " + day + " Timeslot: " + tDay
												+ " - Existe una asignatura de la misma titulacion en el mismo timeslot (T: "
												+ timeslot + " - L1: " + l.getId() + " | L2: " + l2.getId() + ")");
									}
									return false;
								}
							}
						}
					}
				}
			}
		}

		return true;
	}
}
