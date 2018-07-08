package es.unex.cum.tfmtimetabling.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import es.unex.cum.tfmtimetabling.model.Course;
import es.unex.cum.tfmtimetabling.model.Curricula;
import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Lecture;
import es.unex.cum.tfmtimetabling.model.Room;
import es.unex.cum.tfmtimetabling.model.Solucion;
import es.unex.cum.tfmtimetabling.model.UConstraints;

/**
 * Utiles para ficheros
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class FileUtils {

	/**
	 * Escribir en fichero
	 * 
	 * @param path
	 *            Path
	 * @param text
	 *            Texto del fichero
	 * 
	 * @return True si se ha escrito el fichero correctamente
	 */
	public static boolean writeTextFile(String path, String text) {
		BufferedWriter bw = null;
		File file = null;
		boolean writeOk = true;
		try {
			file = new File(path);
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(text);
		} catch (IOException e) {
			e.printStackTrace();
			writeOk = false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return writeOk;
	}

	/**
	 * Escribir en fichero (sin sobreescribir el original)
	 * 
	 * @param path
	 *            Path
	 * @param text
	 *            Texto del fichero
	 * @return True si se ha escrito el fichero correctamente
	 */
	public static boolean appendTextFile(String path, String text) {
		BufferedWriter bw = null;
		boolean writeOk = true;
		try {
			bw = new BufferedWriter(new FileWriter(path, true));
			bw.append(text);
		} catch (IOException e) {
			e.printStackTrace();
			writeOk = false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return writeOk;
	}

	/**
	 * Escribir la solucion en un fichero
	 * 
	 * @param path
	 *            Ruta del fichero
	 * @param sol
	 *            Solucion
	 * @param ins
	 *            Instancia
	 */
	public static void writeSolution(String path, Solucion sol, Instance ins) {
		StringBuilder sb = new StringBuilder();
		Lecture[][] solucion = sol.getSolucion();

		Room[] rooms = ins.getRoomsList();

		for (int r = 0; r < solucion.length; r++) {
			for (int t = 0; t < solucion[r].length; t++) {
				Lecture lecture = solucion[r][t];
				if (lecture != null) {
					// Dia y timeslot
					int day = t / ins.getTimeslotsPerDay();
					int timeslot = t % ins.getTimeslotsPerDay();
					sb.append(lecture.getCourse().getCourseId());
					sb.append(" ");
					sb.append(rooms[r].getRoomId());
					sb.append(" ");
					sb.append(day);
					sb.append(" ");
					sb.append(timeslot);
					sb.append("\n");
				}
			}
		}

		writeTextFile(path, sb.toString());
	}

	/**
	 * Escribir solucion en HTML
	 * 
	 * @param solution
	 *            Solucion
	 * @param fitness
	 *            Fitness de la solucion
	 * @param path
	 *            Ruta del fichero HTML a generar con la solucion
	 * @param ins
	 *            Instancia
	 */
	public static void writeSolucionToHTML(Lecture[][] solution, Integer fitness, String path, Instance ins) {

		StringBuilder html = new StringBuilder();
		html.append("<html><head></head><body>");
		if (fitness != null) {
			html.append("<h2>Solucion con Fitness: " + fitness + "</h2>");
		}
		html.append(GAFunctions.solutionToHtml(solution, ins, null, null));
		html.append("</body></html>");
		FileUtils.writeTextFile(path, html.toString());
	}

	public static void writeCrossoverToHTML(Lecture[][] padre1, Lecture[][] padre2, Lecture[][] hijo1,
			Lecture[][] hijo2, String path, int t1, int t2, boolean valido, Instance ins) {
		StringBuilder html = new StringBuilder();
		html.append("<html><head></head><body>");
		html.append("<h2>Cruce valido: " + valido + "</h2>");
		html.append("<h2>Padre 1</h2>");
		html.append(GAFunctions.solutionToHtml(padre1, ins, t1, "yellow"));
		html.append("<h2>Padre 2</h2>");
		html.append(GAFunctions.solutionToHtml(padre2, ins, t2, "orange"));
		html.append("<h2>Hijo 1</h2>");
		html.append(GAFunctions.solutionToHtml(hijo1, ins, t1, "yellow"));
		html.append("<h2>Hijo 2</h2>");
		html.append(GAFunctions.solutionToHtml(hijo2, ins, t2, "orange"));
		html.append("</body></html>");
		FileUtils.writeTextFile(path, html.toString());
	}

	/**
	 * Obtener el nombre de un fichero sin extension
	 * 
	 * @param path
	 *            Ruta del fichero
	 * @return Nombre del fichero sin extension
	 */
	public static String getFileNameWithoutExtension(String path) {
		File f = new File(path);
		String fileName = f.getName();

		if (fileName.indexOf(".") > 0) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		return fileName;
	}

	/**
	 * Leer instancia desde fichero
	 * 
	 * @param path
	 *            Path
	 * @return Instancia
	 */
	public static Instance readInstanceFromFile(String path) {
		final int MODE_DEFAULT = 0;
		final int MODE_READ_COURSES = 1;
		final int MODE_READ_ROOMS = 2;
		final int MODE_READ_CURRICULA = 3;
		final int MODE_READ_CONSTRAINTS = 4;

		// Inicializacion de variables
		Instance instance = new Instance();
		int mode = MODE_DEFAULT;
		int count = 0;
		Course[] coursesList = null;
		Room[] roomsList = null;
		Curricula[] curriculaList = null;
		UConstraints[] uConstraintsList = null;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null) {
				// Leemos una linea del fichero y le quitamos los espacios
				line = line.replace("\t", " ");
				line = line.trim().replaceAll(" +", " ");
				String[] split = line.split(" ");
				switch (mode) {
				case MODE_DEFAULT:
					// Modo por defecto
					switch (split[0]) {
					case "Name:":
						// Obtenemos nombre de instancia
						instance.setName(split[1]);
						break;
					case "Courses:":
						// Obtenemos numero de asignaturas
						instance.setCourses(Integer.parseInt(split[1]));
						coursesList = new Course[instance.getCourses()];
						break;
					case "Rooms:":
						// Obtenemos numero de aulas
						instance.setRooms(Integer.parseInt(split[1]));
						roomsList = new Room[instance.getRooms()];
						break;
					case "Days:":
						// Obtenemos numero de dias que se imparten clases en la semana
						instance.setDays(Integer.parseInt(split[1]));
						break;
					case "Periods_per_day:":
						// Obtenemos numero de periodos diarios
						instance.setTimeslotsPerDay(Integer.parseInt(split[1]));
						break;
					case "Curricula:":
						// Obtenemos numero de cursos
						instance.setCurricula(Integer.parseInt(split[1]));
						curriculaList = new Curricula[instance.getCurricula()];
						break;
					case "Constraints:":
						// Obtenemos numero de restricciones
						instance.setConstraints(Integer.parseInt(split[1]));
						uConstraintsList = new UConstraints[instance.getConstraints()];
						break;
					case "COURSES:":
						// Cambio a modo lectura de asignaturas
						mode = MODE_READ_COURSES;
						count = 0;
						break;
					case "ROOMS:":
						// Cambio a modo lectura de aulas
						mode = MODE_READ_ROOMS;
						count = 0;
						break;
					case "CURRICULA:":
						// Cambio a modo lectura de cursos
						mode = MODE_READ_CURRICULA;
						count = 0;
						break;
					case "UNAVAILABILITY_CONSTRAINTS:":
						// Cambio a modo lectura de restricciones
						mode = MODE_READ_CONSTRAINTS;
						count = 0;
						break;
					default:
						mode = MODE_DEFAULT;
					}
					break;
				case MODE_READ_COURSES:
					// Modo leer asignaturas
					if (split[0].equals("")) {
						mode = MODE_DEFAULT;
					} else {
						coursesList[count] = new Course(split[0], split[1], Integer.parseInt(split[2]),
								Integer.parseInt(split[3]), Integer.parseInt(split[4]));
						count++;
					}
					break;
				case MODE_READ_ROOMS:
					// Modo leer aulas
					if (split[0].equals("")) {
						mode = MODE_DEFAULT;
					} else {
						roomsList[count] = new Room(split[0], Integer.parseInt(split[1]));
						count++;
					}
					break;
				case MODE_READ_CURRICULA:
					// Modo leer cursos
					if (split[0].equals("")) {
						mode = MODE_DEFAULT;
					} else {
						curriculaList[count] = new Curricula(split[0], Integer.parseInt(split[1]),
								Arrays.copyOfRange(split, 2, split.length));
						count++;
					}
					break;
				case MODE_READ_CONSTRAINTS:
					// Modo leer restricciones
					if (split[0].equals("")) {
						mode = MODE_DEFAULT;
					} else {
						uConstraintsList[count] = new UConstraints(split[0], Integer.parseInt(split[1]),
								Integer.parseInt(split[2]));
						count++;
					}
					break;
				}

			}
			// Formamos la instancia con los datos leidos
			instance.setTimeslots(instance.getDays() * instance.getTimeslotsPerDay());
			instance.setCoursesList(coursesList);
			instance.setRoomsList(roomsList);
			instance.setCurriculaList(curriculaList);
			instance.setuConstraintsList(uConstraintsList);
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// NADA
				}
			}
		}
		return instance;
	}
}
