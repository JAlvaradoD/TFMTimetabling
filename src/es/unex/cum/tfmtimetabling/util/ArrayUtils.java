package es.unex.cum.tfmtimetabling.util;

import java.util.concurrent.ThreadLocalRandom;

/** Utiles para Arrays
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class ArrayUtils {

	/**
	 * Generar array de n elementos con numeros aleatorios no repetidos entre el 0 y
	 * el numero maximo especificado
	 * 
	 * @param n
	 *            numero de elementos del array
	 * @param max
	 *            Valor maximo
	 * @return array de n elementos con numeros aleatorios entre el 0 y max
	 */
	public static int[] generateRandomArrayWithValuesNoRepeating(int n, int max) {
		int[] randomArray = new int[n];
		for (int i = 0; i < randomArray.length; i++) {
			boolean continueGenerating = true;
			int num = 0;
			while (continueGenerating) {
				num = ThreadLocalRandom.current().nextInt(max);
				continueGenerating = false;
				for (int j = 0; j < i; j++) {
					if (randomArray[j] == num) {
						continueGenerating = true;

					}
				}
			}
			randomArray[i] = num;
		}
		return randomArray;
	}

	/**
	 * Generar array numerico de tamanio n, secuencial con inicio en posicion
	 * aleatoria. Cuando la secuencia alcanza el tamanio del array - 1, se reinicia a 0
	 * Eemplo tamanio 10: [4,5,6,7,8,9,0,1,2,3]
	 * 
	 * @param size
	 *            Tamanio del array
	 * @return Array numerico secuencial aleatorio
	 */
	public static int[] generateRandomSequentialArray(int size) {
		int[] randomSequentialArray = new int[size];

		int seq = ThreadLocalRandom.current().nextInt(size);

		for (int i = 0; i < randomSequentialArray.length; i++) {
			randomSequentialArray[i] = seq;
			seq++;
			if (seq == randomSequentialArray.length) {
				seq = 0;
			}
		}

		return randomSequentialArray;

	}

}
