package entity;

/**
 * Classe que representa o valor da latencia
 * categorizado
 * @author juccelino.barros
 *
 */
public class CatLatencia {
	
	// Valor Latencia
	private Double latency;
	
	private boolean isflush;
	
	private boolean isCompSimples;
	
	private boolean isCompMega;

	public CatLatencia() {
	
	}

	public Double getLatency() {
		return latency;
	}


	public void setLatency(Double latency) {
		this.latency = latency;
	}


	public boolean isIsflush() {
		return isflush;
	}

	public void setIsflush(boolean isflush) {
		this.isflush = isflush;
	}

	public boolean isCompSimples() {
		return isCompSimples;
	}

	public void setCompSimples(boolean isCompSimples) {
		this.isCompSimples = isCompSimples;
	}

	public boolean isCompMega() {
		return isCompMega;
	}

	public void setCompMega(boolean isCompMega) {
		this.isCompMega = isCompMega;
	}


}
