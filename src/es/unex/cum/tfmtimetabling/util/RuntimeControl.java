package es.unex.cum.tfmtimetabling.util;

/**
 * Control de tiempo de ejecucion
 * 
 * @author Jorge Alvarado Diaz
 *
 */
public class RuntimeControl {
	private long tStart;
	/**
	 * Minutos de ejecucion maximos
	 * 
	 */
	private int minutes;

	public RuntimeControl(int minutes) {
		this.tStart = 0;
		this.minutes = minutes;
	}

	/**
	 * Iniciar el control de tiempo de ejecucion
	 * 
	 */
	public void startRuntime() {
		this.tStart = System.currentTimeMillis();
	}

	/**
	 * Comprobar si se ha superado el tiempo de ejecucion
	 * 
	 * @return True si se ha superado el tiempo de ejecucion | False si no se ha superado
	 */
	public boolean checkRuntime() {
		// Si esta desahbilitado el control de tiempo de ejecucion, se devuelve siempre
		// false
		if (this.minutes == 0) {
			return false;
		}
		// Calculamos el tiempo de ejecucion 
		int runtime = (int) ((System.currentTimeMillis() - this.tStart) / 60000);
		return runtime >= this.minutes;
	}
}
