package es.unex.cum.tfmtimetabling.util;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import es.unex.cum.tfmtimetabling.model.Course;
import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Lecture;
import es.unex.cum.tfmtimetabling.model.Room;

/**
 * Funciones para la generacion de solucion inicial
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class CourseUtils {

	private Course course;
	private Instance ins;
	private String[] _curriculumIdCourse;
	private Room[] _roomList;
	private boolean[][] _availability;
	private boolean[][] _availabilityConstrains;

	public CourseUtils(Course course, Instance ins) {
		this.course = course;
		this.ins = ins;
		this._roomList = ins.getRoomsList();
		this._curriculumIdCourse = ins.getCurriculumIdOfCourse(this.course.getCourseId());
		this._availability = ins.courseIsAvailable(this.course.getCourseId());
		this._availabilityConstrains = ins.courseIsAvailable(this.course.getCourseId());
	}

	/**
	 * Dias que se puede dar la asignatura
	 * 
	 * @param solution
	 *            Solucion Actual
	 * @return Dias disponibles donde se puede impartir la asignatura
	 */
	public ArrayList<Integer> daysAvailableCourse(Lecture[][] solution) {
		ArrayList<Integer> daysAvailable = new ArrayList<>();
		for (int day = 0; day < _availability.length; day++) {
			int numOcuppedTimeslots = 0;
			for (int timeslot = 0; timeslot < _availability[day].length; timeslot++) {
				if (_availability[day][timeslot]) {
					int y = (day * ins.getTimeslotsPerDay()) + timeslot;
					int numOcuppedRooms = 0;
					boolean sameCurriculumOrTeacher = false;
					for (int x = 0; x < solution.length; x++) {
						Lecture l = solution[x][y];
						if (l != null) {
							if (l.getCourse().getTeacher().equals(this.course.getTeacher()) || sameCurriculum(l)) {
								sameCurriculumOrTeacher = true;
							}
							numOcuppedRooms++;
						} else {
							// Comprobar capacidad aula (Pruebas)
							if (course.getStudents() > _roomList[x].getCapacity()) {
								numOcuppedRooms++;
							}
						}
					}
					if (numOcuppedRooms == ins.getRooms() || sameCurriculumOrTeacher) {
						_availability[day][timeslot] = false;
						numOcuppedTimeslots++;
					}
				} else {
					numOcuppedTimeslots++;
				}
			}
			if (numOcuppedTimeslots < ins.getTimeslotsPerDay()) {
				daysAvailable.add(day);
			}
		}
		return daysAvailable;
	}

	/**
	 * Timeslots disponibles para la asignatura en un dia en concreto
	 * 
	 * @param day
	 *            Dia
	 * @param solution
	 *            Solucion Actual
	 * @return Timeslots disponibles donde se puede impartir la asignatura
	 */
	public ArrayList<Integer> timeslotsAvailableCourseInDay(int day, Lecture[][] solution) {
		ArrayList<Integer> timeslotsAvailable = new ArrayList<>();
		for (int timeslot = 0; timeslot < _availability[day].length; timeslot++) {
			if (_availability[day][timeslot]) {
				timeslotsAvailable.add(timeslot);
			}
		}
		return timeslotsAvailable;
	}

	/**
	 * Aulas disponibles en un timeslot en concreto
	 * 
	 * @param day
	 *            Dia
	 * @param timeslot
	 *            Timeslot
	 * @param solution
	 *            Solucion Actual
	 * @return Aulas disponibles donde se puede impartir la asignatura
	 */
	public ArrayList<Integer> roomAvailableInTimeslot(int day, int timeslot, Lecture[][] solution) {
		ArrayList<Integer> roomsAvailable = new ArrayList<>();
		if (_availability[day][timeslot]) {
			for (int x = 0; x < solution.length; x++) {
				int y = (day * ins.getTimeslotsPerDay()) + timeslot;
				Lecture l = solution[x][y];
				if (l == null) {
					// if(_roomList[x].getCapacity() >= course.getStudents()) {
					roomsAvailable.add(x);
					// }
				}
			}
		}
		return roomsAvailable;
	}

	/**
	 * Mover un evento de la solucion inicial de forma aleatoria
	 * 
	 * @param solution
	 *            Solucion inicial
	 */
	public void moveLectureRandom(Lecture[][] solution) {
		boolean moved = false;
		// int timeout = 0;
		while (!moved) {
			// Cogemos un aula y timeslot de forma aleatoria
			int rRoom = ThreadLocalRandom.current().nextInt(solution.length);
			int rTimeslot = ThreadLocalRandom.current().nextInt(solution[0].length);
			int day = rTimeslot / ins.getTimeslotsPerDay();
			int timeslot = rTimeslot % ins.getTimeslotsPerDay();
			// Obtenemos el evento
			Lecture l = solution[rRoom][rTimeslot];
			if (l != null) {
				// Si hay evento (no hay hueco) intenamos moverlo
				Integer[] pos = CheckFunctions.lectureCanBeMoved(solution, l, rRoom, rTimeslot, ins);
				// Si se puede mover
				if (pos != null) {
					// Lo movemos
					solution[rRoom][rTimeslot] = null;
					solution[pos[0]][pos[1]] = l;
					moved = true;
					// Si el hueco dejado por el evento movido no inclumple la hard constrain de
					// disponibilidad del evento que queremos insertar
					if (_availabilityConstrains[day][timeslot]) {
						/*
						 * Comprobamos si en ese hueco se puede insertar la asignura (resto de Hard
						 * Constrains tales como que no haya una asignatura de la misma titulacion o que
						 * tenga el mismo profespor en el mismo timeslot)
						 */
						boolean tAvailable = true;
						for (int r = 0; r < solution.length; r++) {
							Lecture l2 = solution[r][rTimeslot];
							if (l2 != null) {
								if (sameCurriculum(l2)
										|| l2.getCourse().getTeacher().equals(this.course.getTeacher())) {

									tAvailable = false;
								}
							}
						}
						if (tAvailable) {
							// Si no incumple ninguna hard constrains, marcamos el hueco como disponible
							_availability[day][timeslot] = true;
						}
					}
				}
			}
			// timeout++;
		}
	}

	/**
	 * Comprobar si un evento de una asignatura pertenece a la misma titulacion del evento que
	 * estamos procesando
	 * 
	 * @param lecture
	 *            Evento a comparar
	 * @return True en caso de que pertenezca a la misma titulacion
	 */
	private boolean sameCurriculum(Lecture lecture) {
		String[] curriculumIdCourse = lecture.getCurriculumId();
		for (int k = 0; k < _curriculumIdCourse.length; k++) {
			for (int m = 0; m < curriculumIdCourse.length; m++) {
				if (_curriculumIdCourse[k].equals(curriculumIdCourse[m])) {
					return true;
				}
			}
		}
		return false;
	}
}
