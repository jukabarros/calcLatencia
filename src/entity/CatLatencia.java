package entity;

public class CatLatencia {
	
	private Double tempo;
	
	private boolean isflush;
	
	private boolean isCompSimples;
	
	private boolean isCompMega;

	public CatLatencia() {
	
	}

	public Double getTempo() {
		return tempo;
	}

	public void setTempo(Double tempo) {
		this.tempo = tempo;
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

	@Override
	public String toString() {
		return "CatLatencia [tempo=" + tempo + ", isflush=" + isflush + ", isCompSimples=" + isCompSimples
				+ ", isCompMega=" + isCompMega + "]";
	}
	

}
