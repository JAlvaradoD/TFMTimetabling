package es.unex.cum.tfmtimetabling.model;

/** Modelo Evento Asignatura (ITC 2007: Lecture)
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class Lecture {
	private String id;
	private Course course;
	private String[] curriculumId;
	
	
	public Lecture(String id, Course course, String[] curriculumId) {
		super();
		this.id = id;
		this.course = course;
		this.curriculumId = curriculumId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	public String[] getCurriculumId() {
		return curriculumId;
	}
	public void setCurriculumId(String[] curriculumId) {
		this.curriculumId = curriculumId;
	}
	
}
