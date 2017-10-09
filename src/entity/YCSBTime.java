package entity;

import java.util.Date;

/**
 * Classe que representa as informacoes do LOG do YCSB
 * @author juccelino.barros
 *
 */
public class YCSBTime {
	
	private Integer sample;
	
	private Date date;
	
	private Double txReq;
	
	private Double latencyInUs;

	public YCSBTime(Integer sample, Date date, Double txReq, Double latencyInUs) {
		super();
		this.sample = sample;
		this.date = date;
		this.txReq = txReq;
		this.latencyInUs = latencyInUs;
	}

	public Integer getSample() {
		return sample;
	}

	public void setSample(Integer sample) {
		this.sample = sample;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getTxReq() {
		return txReq;
	}

	public void setTxReq(Double txReq) {
		this.txReq = txReq;
	}

	public Double getLatencyInUs() {
		return latencyInUs;
	}

	public void setLatencyInUs(Double latencyInUs) {
		this.latencyInUs = latencyInUs;
	}

	@Override
	public String toString() {
		return "YCSBTime [sample=" + sample + ", date=" + date + ", txReq=" + txReq + ", latencyInUs=" + latencyInUs
				+ "]";
	}

}
