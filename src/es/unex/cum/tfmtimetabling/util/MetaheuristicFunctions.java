package es.unex.cum.tfmtimetabling.util;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import es.unex.cum.tfmtimetabling.model.Instance;
import es.unex.cum.tfmtimetabling.model.Lecture;
import es.unex.cum.tfmtimetabling.model.Solucion;

/** Metaheuristicas: Busqueda Local
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class MetaheuristicFunctions {

	/**
	 * Aplicar el algoritmo de busqueda local a x individuos de la poblacion segun
	 * el ratio establecido
	 * 
	 * @param population
	 *            Poblacion a aplicar el algoritmo de busqueda local
	 * @param ins
	 *            Instancia
	 * @param params
	 *            Parametros del algoritmo
	 * @return Poblacion procesada con el algoritmo de busqueda local
	 */
	public static ArrayList<Solucion> hillClimbingPopulation(ArrayList<Solucion> population, Instance ins,
			Params params) {
		ArrayList<Solucion> populationProcessed = new ArrayList<Solucion>();

		// Numero de individuos a procesar
		int numIndvHillClimbing = (int) (params.getGAPopulationSize() * params.getGAHillClimbingRate());

		for (int i = 0; i < numIndvHillClimbing; i++) {
			// Obtenemos un individuo de forma aleatoria
			int randomIdx = ThreadLocalRandom.current().nextInt(population.size());
			Solucion randomIndv = population.get(randomIdx);
			// Aplicamos la busqueda local a ese individuo
			randomIndv = hillClimbing(randomIndv, ins, params);

			// Agregamos el individuo a la cola de procesados
			populationProcessed.add(randomIndv);
			// Y lo eliminamos de la lista de individuos
			population.remove(randomIdx);
		}

		// Agregamos el resto de individuos a la cola de procesados
		for (int i = 0; i < population.size(); i++) {
			populationProcessed.add(population.get(i));
		}

		return populationProcessed;
	}


	/**
	 * Algoritmo de Busqueda local Primer Mejor
	 * 
	 * @param individuo
	 *            Individuo-Solucion
	 * @param ins
	 *            Instancia
	 * @param params
	 *            Parametros del algoritmo
	 * @return Mejor solucion obtenida por el algoritmo
	 */
	public static synchronized Solucion hillClimbing(Solucion individuo, Instance ins, Params params) {
		Lecture[][] mejorSolucion = GAFunctions.copySolutionArray(individuo.getSolucion());
		int mejorFitness = individuo.getFitness();

		boolean run = true;

		while (run) {
			// Se genera una secuencia aleatoria para recorrer todos los eventos (se empieza
			// por un evento aleatorio)
			int[] sequenceRooms = ArrayUtils.generateRandomSequentialArray(mejorSolucion.length);
			int[] sequenceTimeslots = ArrayUtils.generateRandomSequentialArray(mejorSolucion[0].length);

			boolean mejorSolucionEncontrada = false;

			// Se realiza el recorrido
			for (int i = 0; i < mejorSolucion.length && !mejorSolucionEncontrada; i++) {
				for (int j = 0; j < mejorSolucion[i].length && !mejorSolucionEncontrada; j++) {
					int r1 = sequenceRooms[i];
					int t1 = sequenceTimeslots[j];
					Lecture lecture = mejorSolucion[r1][t1];
					
					if (lecture != null) {
						// Busqueda de soluciones vecinas
						for (int x = 0; x < mejorSolucion.length && !mejorSolucionEncontrada; x++) {
							for (int y = 0; y < mejorSolucion[x].length && !mejorSolucionEncontrada; y++) {
								int r2 = sequenceRooms[x];
								int t2 = sequenceTimeslots[y];
								if ((r1 != r2 && t1 != t2) && mejorSolucion[r2][t2] == null) {
									Lecture[][] aux = GAFunctions.copySolutionArray(mejorSolucion);
									aux[r1][t1] = null;
									// Si la solucion es valida (hard constrains)
									if (CheckFunctions.checkLecture(aux, lecture, r2, t2, ins, false)) {
										// Procedemos a calcular su fitness
										aux[r2][t2] = lecture;
										int fitnessAux = GAFunctions.fitness(aux, params, ins);
										if (fitnessAux < mejorFitness) {
											// Solucion Vecina mejor encontrada
											mejorSolucion = GAFunctions.copySolutionArray(aux);
											mejorFitness = fitnessAux;
											mejorSolucionEncontrada = true;
											if (params.isDebugMode()) {
												System.out.println(
														"Hill Climbing - Mejor Solucion Encontrada: " + mejorFitness);
											}
										}
									}

								}
							}
						}

					}
				}
			}

			if (!mejorSolucionEncontrada) {
				run = false;
			}
		}

		// Si no hay fitness mejor de todas las convinaciones, se termina y devuelve el
		// individuo

		return new Solucion(mejorSolucion, mejorFitness);
	}

}
