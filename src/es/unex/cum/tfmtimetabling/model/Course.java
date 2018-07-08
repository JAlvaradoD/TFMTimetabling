package es.unex.cum.tfmtimetabling.model;

/** Modelo Asignatura (ITC 2007: Course)
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Course {
	private String courseId;
	private String teacher;
	private int lectures;
	private int minWorkingDays;
	private int students;
	
	public Course(String courseId, String teacher, int lectures, int minWorkingDays, int students) {
		super();
		this.courseId = courseId; // ID Asignatura
		this.teacher = teacher; // ID Profesor
		this.lectures = lectures; // Horas de Clase
		this.minWorkingDays = minWorkingDays; // Numero minimo dia distrubir clases (lecturas) (Soft)
		this.students = students; // Numero de Estudiantes
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getTeacher() {
		return teacher;
	}
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	public int getLectures() {
		return lectures;
	}
	public void setLectures(int lectures) {
		this.lectures = lectures;
	}
	public int getMinWorkingDays() {
		return minWorkingDays;
	}
	public void setMinWorkingDays(int minWorkingDays) {
		this.minWorkingDays = minWorkingDays;
	}
	public int getStudents() {
		return students;
	}
	public void setStudents(int students) {
		this.students = students;
	}
	@Override
	public String toString() {
		return "Course [courseId=" + courseId + ", teacher=" + teacher + ", lectures=" + lectures + ", minWorkingDays="
				+ minWorkingDays + ", students=" + students + "]";
	}
	
}
