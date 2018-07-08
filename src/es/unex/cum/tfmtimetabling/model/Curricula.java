package es.unex.cum.tfmtimetabling.model;

import java.util.Arrays;

/** Modelo Titulacion (ITC 2007: Curricula)
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Curricula {
	private String curriculumId;
	private int courses;
	private String[] memberId;
	
	
	public Curricula(String curriculumId, int courses, String[] memberId) {
		super();
		this.curriculumId = curriculumId; // ID
		this.courses = courses; // Numero de asignaturas
		this.memberId = memberId; // Array Asignaturas
	}
	public String getCurriculumId() {
		return curriculumId;
	}
	public void setCurriculumId(String curriculumId) {
		this.curriculumId = curriculumId;
	}
	public int getCourses() {
		return courses;
	}
	public void setCourses(int courses) {
		this.courses = courses;
	}
	public String[] getMemberId() {
		return memberId;
	}
	public void setMemberId(String[] memberId) {
		this.memberId = memberId;
	}
	@Override
	public String toString() {
		return "Curricula [curriculumId=" + curriculumId + ", courses=" + courses + ", memberId="
				+ Arrays.toString(memberId) + "]";
	}
}
