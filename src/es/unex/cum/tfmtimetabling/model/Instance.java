package es.unex.cum.tfmtimetabling.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Instancia
 * 
 * @author Jorge
 *
 */
public class Instance {
	private String name;
	private int courses;
	private int rooms;
	private int days;
	private int timeslots;
	private int timeslotsPerDay;
	private int curricula;
	private int constraints;
	private Course[] coursesList;
	private Room[] roomsList;
	private Curricula[] curriculaList;
	private UConstraints[] uConstraintsList;

	public Instance() {

	}

	public Instance(String name, int courses, int rooms, int days, int timeslotsPerDay, int curricula, int constraints,
			Course[] coursesList, Room[] roomsList, Curricula[] curriculaList, UConstraints[] uConstraintsList) {
		super();
		this.name = name;
		this.courses = courses;
		this.rooms = rooms;
		this.days = days;
		this.timeslots = days * timeslotsPerDay;
		this.timeslotsPerDay = timeslotsPerDay;
		this.curricula = curricula;
		this.constraints = constraints;
		this.coursesList = coursesList;
		this.roomsList = roomsList;
		this.curriculaList = curriculaList;
		this.uConstraintsList = uConstraintsList;
	}

	/**
	 * Obtener la lista de todas las clases de todas las asignaturas
	 * 
	 * @return Clases de todas las asignaturas
	 */
	public ArrayList<Lecture> getLecturesList() {
		ArrayList<Lecture> lectureList = new ArrayList<Lecture>();
		for (Course course : coursesList) {
			String[] curriculumId = getCurriculumIdOfCourse(course.getCourseId());
			for (int i = 0; i < course.getLectures(); i++) {
				lectureList.add(new Lecture(course.getCourseId() + "_" + i, course, curriculumId));
			}
		}
		return lectureList;
	}

	/**
	 * Obtener el Id de la Titulacion/Titulaciones a la que pertenece una asignatura
	 * 
	 * @param courseId
	 *            ID Asignatura
	 * @return Titulacion/Titulaciones a la que pertenece
	 */
	public String[] getCurriculumIdOfCourse(String courseId) {
		ArrayList<String> curriculumIdList = new ArrayList<>();
		for (int i = 0; i < curriculaList.length; i++) {
			Curricula c = curriculaList[i];
			String[] memberId = c.getMemberId();
			for (int j = 0; j < memberId.length; j++) {
				if (memberId[j].equals(courseId)) {
					curriculumIdList.add(c.getCurriculumId());
				}
			}
		}
		String[] curriculumId = new String[curriculumIdList.size()];
		curriculumIdList.toArray(curriculumId);
		return curriculumId;
	}

	/**
	 * La asignatura esta disponible en un dia y timeslot en concreto?
	 * 
	 * @param courseId
	 *            Id Asignatura
	 * @param day
	 *            Dia de la semana
	 * @param timeslot
	 *            Timeslot
	 * @return Disponibilidad
	 */
	public boolean courseIsAvailable(String courseId, int day, int timeslot) {
		for (int i = 0; i < uConstraintsList.length; i++) {
			UConstraints uConstraint = uConstraintsList[i];
			if (uConstraint.getCourseId().equals(courseId)) {
				if (uConstraint.getDay() == day && uConstraint.getDayPeriod() == timeslot) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Devolver matriz de disponibilidad de una asignatura
	 * 
	 * @param courseId
	 *            Id Asignatura
	 * @return Matriz (Booleana) de disponibilidad de una asignatura [dia, timeslot]
	 */
	public boolean[][] courseIsAvailable(String courseId) {
		boolean[][] availability = new boolean[days][timeslotsPerDay];
		for (boolean[] row : availability) {
			Arrays.fill(row, true);
		}
		for (int i = 0; i < uConstraintsList.length; i++) {
			UConstraints uConstraint = uConstraintsList[i];
			if (uConstraint.getCourseId().equals(courseId)) {
				availability[uConstraint.getDay()][uConstraint.getDayPeriod()] = false;
			}
		}
		return availability;
	}

	/**
	 * Devolver los dias que esta disponible una asignatura
	 * 
	 * @param courseId
	 *            Id Asignatura
	 * @return Array Booleano que indica los dias disponibles
	 */
	public boolean[] daysCourseIsAvailable(String courseId) {
		boolean[] day = new boolean[days];
		Arrays.fill(day, true);
		HashMap<Integer, Integer> dayTimeslots = new HashMap<>();
		for (int i = 0; i < uConstraintsList.length; i++) {
			UConstraints uConstraint = uConstraintsList[i];
			if (uConstraint.getCourseId().equals(courseId)) {
				Integer timeslotsDay = dayTimeslots.get(uConstraint.getDay());
				if (timeslotsDay != null) {
					dayTimeslots.put(uConstraint.getDay(), timeslotsDay + 1);
				} else {
					dayTimeslots.put(uConstraint.getDay(), 1);
				}
			}
		}
		for (Map.Entry<Integer, Integer> entry : dayTimeslots.entrySet()) {
			if (entry.getValue() == timeslotsPerDay) {
				day[entry.getKey()] = false;
			}
		}
		return day;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCourses() {
		return courses;
	}

	public void setCourses(int courses) {
		this.courses = courses;
	}

	public int getRooms() {
		return rooms;
	}

	public void setRooms(int rooms) {
		this.rooms = rooms;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getTimeslots() {
		return timeslots;
	}

	public void setTimeslots(int timeslots) {
		this.timeslots = timeslots;
	}

	public int getTimeslotsPerDay() {
		return timeslotsPerDay;
	}

	public void setTimeslotsPerDay(int timeslotsPerDay) {
		this.timeslotsPerDay = timeslotsPerDay;
	}

	public int getCurricula() {
		return curricula;
	}

	public void setCurricula(int curricula) {
		this.curricula = curricula;
	}

	public int getConstraints() {
		return constraints;
	}

	public void setConstraints(int constraints) {
		this.constraints = constraints;
	}

	public Course[] getCoursesList() {
		return coursesList;
	}

	public void setCoursesList(Course[] coursesList) {
		this.coursesList = coursesList;
	}

	public int getPositionCourse(String courseId) {
		for (int i = 0; i < coursesList.length; i++) {
			if (coursesList[i].getCourseId().equals(courseId)) {
				return i;
			}
		}
		return -1;
	}

	public Room[] getRoomsList() {
		return roomsList;
	}

	/** Obtener lista aula-indice_lista
	 * 
	 * @return Lista aula-indice_lista
	 */
	public HashMap<Integer, String> getRoomsIdList() {
		HashMap<Integer, String> roomsId = new HashMap<>();
		for (int i = 0; i < roomsList.length; i++) {
			roomsId.put(i, roomsList[i].getRoomId());
		}
		return roomsId;
	}

	public void setRoomsList(Room[] roomsList) {
		this.roomsList = roomsList;
	}

	public Curricula[] getCurriculaList() {
		return curriculaList;
	}

	public void setCurriculaList(Curricula[] curriculaList) {
		this.curriculaList = curriculaList;
	}

	public UConstraints[] getuConstraintsList() {
		return uConstraintsList;
	}

	public void setuConstraintsList(UConstraints[] uConstraintsList) {
		this.uConstraintsList = uConstraintsList;
	}

	@Override
	public String toString() {
		return "Instance [name=" + name + ", courses=" + courses + ", rooms=" + rooms + ", days=" + days
				+ ", timeslotsPerDay=" + timeslotsPerDay + ", curricula=" + curricula + ", constraints=" + constraints
				+ ", coursesList=" + Arrays.toString(coursesList) + ", roomsList=" + Arrays.toString(roomsList)
				+ ", curriculaList=" + Arrays.toString(curriculaList) + ", uConstraintsList="
				+ Arrays.toString(uConstraintsList) + "]";
	}
}