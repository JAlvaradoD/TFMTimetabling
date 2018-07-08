package es.unex.cum.tfmtimetabling.model;


/** Modelo Restriccion Disponibilidad (ITC 2007: Unavailable Constraint)
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class UConstraints {
	private String CourseId;
	private int day;
	private int dayPeriod;
	
	
	
	public UConstraints(String courseId, int day, int dayPeriod) {
		super();
		CourseId = courseId; // ID Asignatura
		this.day = day; // Dia
		this.dayPeriod = dayPeriod; // Timeslot
	}
	public String getCourseId() {
		return CourseId;
	}
	public void setCourseId(String courseId) {
		CourseId = courseId;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getDayPeriod() {
		return dayPeriod;
	}
	public void setDayPeriod(int dayPeriod) {
		this.dayPeriod = dayPeriod;
	}
	@Override
	public String toString() {
		return "UnavailabilityConstraints [CourseId=" + CourseId + ", day=" + day + ", dayPeriod=" + dayPeriod + "]";
	}
	
}
