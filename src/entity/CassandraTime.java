package entity;

import java.util.Date;

/**
 * Classe que representa o tempo de ocorrencia de um
 * dos processos do Cassandra:
 * flush, compSimples, compMega
 * @author juccelino.barros
 *
 */
public class CassandraTime {
	
	private Integer sample;
	
	private Date dateInit;
	
	private Date dateEnd;
	
	public CassandraTime(Integer sample, Date dateInit, Date dateEnd) {
		this.sample = sample;
		this.dateInit = dateInit;
		this.dateEnd = dateEnd;
	}

	public Integer getSample() {
		return sample;
	}

	public void setSample(Integer sample) {
		this.sample = sample;
	}

	public Date getDateInit() {
		return dateInit;
	}

	public void setDateInit(Date dateInit) {
		this.dateInit = dateInit;
	}

	public Date getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}

	@Override
	public String toString() {
		return "CassandraTime [sample=" + sample + ", dateInit=" + dateInit + ", dateEnd=" + dateEnd +"]";
	}
	
}
