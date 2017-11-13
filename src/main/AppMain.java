package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import csv.CSVReader;
import entity.CassandraTime;
import entity.CatLatencia;
import entity.YCSBTime;

public class AppMain {

	public static void main(String[] args) {
		String csvDirectory = "";
		if (args.length == 0) {
			csvDirectory = "csvs/";
		} else {
			csvDirectory = args[0];
		}
		CSVReader csvReader = new CSVReader();

		System.out.println("*** Localizando arquivos CSVs ***");
		List<String> allCsvs = csvReader.readCSVDirectory(csvDirectory);

		List<CassandraTime> compMegaTimes = new ArrayList<CassandraTime>();
		List<CassandraTime> compSimplesTimes = new ArrayList<CassandraTime>();
		List<CassandraTime> flushTimes = new ArrayList<CassandraTime>();
		List<YCSBTime> ycsbTimes = new ArrayList<YCSBTime>();
		try {
			System.out.println("*** Inserindo os Dados na Memória");
			for (int i = 0; i < allCsvs.size(); i++) {
				if (allCsvs.get(i).endsWith("compMega.csv")) {
					compMegaTimes = csvReader.readCSVFileFromLogCassandra(allCsvs.get(i));
				}
				else if (allCsvs.get(i).endsWith("compSimples.csv")) {
					compSimplesTimes = csvReader.readCSVFileFromLogCassandra(allCsvs.get(i));
				}
				else if (allCsvs.get(i).endsWith("flush.csv")) {
					flushTimes = csvReader.readCSVFileFromLogCassandra(allCsvs.get(i));
				}
				else if (allCsvs.get(i).endsWith("ycsb.csv")) {
					ycsbTimes = csvReader.readCSVFileFromLogYCSB(allCsvs.get(i));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("*** Erro na leitura do CSV | AppMain.java ln 30");
		}
		
		System.out.println("\n*** Resultado");
		List<CatLatencia> catLatencias = getLatenciasCategorizada(compMegaTimes, compSimplesTimes, flushTimes, ycsbTimes);
		
		List<CatLatencia> catLatenciasAVG = getAvgCatlatencia(catLatencias);
		System.out.println("\n\n************* MEDIA FINAL *************");
		System.out.println("\nLatency Flush CSimples CMega");
		for (int i = 0; i < catLatenciasAVG.size(); i++) {
			CatLatencia cl = catLatenciasAVG.get(i);
			System.out.printf("%.2f"+"\t"+cl.isIsflush()+"\t"+cl.isCompSimples()+"\t"+cl.isCompMega(), cl.getLatency());
			System.out.println("\n");
		}
		System.out.println("\n*** END ***");
	}

	/**
	 * Verifica se o tempo da latencia sofreu influencia de alguns dos processos do cassandra:
	 * flush, compSimples e compMega.
	 * Pode haver mais de um processo que pode influenciar o tempo de resposta (latencia)
	 * @param compMegaTimes log das compactacoes mega
	 * @param compSimplesTimes log das compactacoes simples
	 * @param flushTimes log do flush
	 * @param ycsbTimes log do ycsb
	 * @return
	 */
	private static List<CatLatencia> getLatenciasCategorizada(List<CassandraTime> compMegaTimes,
			List<CassandraTime> compSimplesTimes, List<CassandraTime> flushTimes, List<YCSBTime> ycsbTimes) {
		
		List<CatLatencia> catLatencias = new ArrayList<CatLatencia>();
		for (int i = 0; i < ycsbTimes.size(); i++) {
			CatLatencia cl = new CatLatencia();
			Date dateYcsb = ycsbTimes.get(i).getDate();
			cl.setLatency(ycsbTimes.get(i).getLatencyInUs());
			// Flush
			for (int j = 0; j < flushTimes.size(); j++) {
				if (flushTimes.get(j).getDateInit().before(dateYcsb) && flushTimes.get(j).getDateEnd().after(dateYcsb)) {
					cl.setIsflush(true);
					break;
				}
			}
			// Comp Simples
			for (int j = 0; j < compSimplesTimes.size(); j++) {
				if (compSimplesTimes.get(j).getDateInit().before(dateYcsb) && compSimplesTimes.get(j).getDateEnd().after(dateYcsb)) {
					cl.setCompSimples(true);
					break;
				}

			}
			// Comp Mega
			for (int j = 0; j < compMegaTimes.size(); j++) {
				if (compMegaTimes.get(j).getDateInit().before(dateYcsb) && compMegaTimes.get(j).getDateEnd().after(dateYcsb)) {
					cl.setCompMega(true);
					break;
				}

			}
			catLatencias.add(cl);
		}
		return catLatencias;
	}
	
	/**
	 * Calcula a media dos valores obtidos de acordo com o evento
	 * @param catLatencias
	 */
	private static List<CatLatencia> getAvgCatlatencia(List<CatLatencia> catLatencias) {
		List<Double> noOne = new ArrayList<Double>();
		List<Double> justFlush = new ArrayList<Double>();
		List<Double> justCompSimples = new ArrayList<Double>();
		List<Double> justCompMega = new ArrayList<Double>();
		List<Double> flushCompSimples = new ArrayList<Double>();
		List<Double> flushCompMega = new ArrayList<Double>();
		List<Double> compSimplesCompMega = new ArrayList<Double>();
		List<Double> all = new ArrayList<Double>();
		for (int i = 0; i < catLatencias.size(); i++) {
			CatLatencia cl = catLatencias.get(i);
			// Nao ocorreu nada
			if (!cl.isIsflush() && !cl.isCompSimples() && !cl.isCompMega()) {
				noOne.add(cl.getLatency());
			} // Apenas Flush			
			else if (cl.isIsflush() && !cl.isCompSimples() && !cl.isCompMega()) {
				justFlush.add(cl.getLatency());
			} // Apenas Comp Simples 			
			else if (!cl.isIsflush() && cl.isCompSimples() && !cl.isCompMega()) {
				justCompSimples.add(cl.getLatency());
			} // Apenas Comp Mega 			
			else if (!cl.isIsflush() && !cl.isCompSimples() && cl.isCompMega()) {
				justCompMega.add(cl.getLatency());
			} // Flush e Comp Simples			
			else if (cl.isIsflush() && cl.isCompSimples() && !cl.isCompMega()) {
				flushCompSimples.add(cl.getLatency());
			} // Flush e Comp Mega
			else if (cl.isIsflush() && !cl.isCompSimples() && cl.isCompMega()) {
				flushCompMega.add(cl.getLatency());
			} // CompSimples e Comp Mega
			else if (!cl.isIsflush() && cl.isCompSimples() && cl.isCompMega()) {
				compSimplesCompMega.add(cl.getLatency());
			} // aconteceu todos os eventos
			else {
				all.add(cl.getLatency());
			}
		}
		
		List<CatLatencia> catLatenciasAVG = new ArrayList<CatLatencia>();
		CSVReader csvReader = new CSVReader();
		/*
		 * CALCULO DAS MEDIAS DE CADA COMBINACAO
		 * GERA ARQUIVO CSV DE CADA LISTA
		 */
		if (!noOne.isEmpty()) {
			Double averageNoOne = noOne.stream().mapToDouble(val -> val).average().getAsDouble();
			csvReader.exportCSVData(noOne, "noOne.csv");
			CatLatencia onOneCL = new CatLatencia(averageNoOne, false, false, false);
			catLatenciasAVG.add(onOneCL);
		} 
		System.out.println("\n(1) Qntd Registros noOne.csv: "+noOne.size());
	    if (!justFlush.isEmpty()) {
	    	Double averageIsFlush = justFlush.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(justFlush, "justFlush.csv");
	    	CatLatencia isFlushCL = new CatLatencia(averageIsFlush, true, false, false);
	    	catLatenciasAVG.add(isFlushCL);
	    }
	    System.out.println("\n(2) Qntd Registros justFlush.csv: "+justFlush.size());
	    
	    if (!justCompSimples.isEmpty()){
	    	Double averageIsCompSimples = justCompSimples.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(justCompSimples, "justCompSimples.csv");
	    	CatLatencia isCompSimplesCL = new CatLatencia(averageIsCompSimples, false, true, false);
	    	catLatenciasAVG.add(isCompSimplesCL);
	    }
	    System.out.println("\n(3) Qntd Registros justCompSimples.csv: "+justCompSimples.size());
	    
	    if (!justCompMega.isEmpty()){
	    	Double averageIsCompMega = justCompMega.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(justCompMega, "justCompMega.csv");
	    	CatLatencia isCompMegaCL = new CatLatencia(averageIsCompMega, false, false, true);
	    	catLatenciasAVG.add(isCompMegaCL);
	    }
	    System.out.println("\n(4) Qntd Registros justCompMega.csv: "+justCompMega.size());
	    
	    if (!flushCompSimples.isEmpty()){
	    	Double averageFlushAndCompSimples = flushCompSimples.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(flushCompSimples, "flushCompSimples.csv");
	    	CatLatencia isFlushCompSimplesCL = new CatLatencia(averageFlushAndCompSimples, true, true, false);
	    	catLatenciasAVG.add(isFlushCompSimplesCL);
	    }
	    System.out.println("\n(5) Qntd Registros flushCompSimples.csv: "+flushCompSimples.size());
	    
	    if (!flushCompMega.isEmpty()) {
	    	Double averageFlushAndCompMega = flushCompMega.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(flushCompMega, "flushCompMega.csv");
	    	CatLatencia isFlushCompMegaCL = new CatLatencia(averageFlushAndCompMega, true, false, true);
	    	catLatenciasAVG.add(isFlushCompMegaCL);
	    }
	    System.out.println("\n(6) Qntd Registros flushCompMega.csv: "+flushCompMega.size());
	    
	    if (!compSimplesCompMega.isEmpty()) {
	    	Double averageCompSimplesAndCompMega = compSimplesCompMega.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(compSimplesCompMega, "compSimplesCompMega.csv");
	    	CatLatencia CompSimplesCompMegaCL = new CatLatencia(averageCompSimplesAndCompMega, false, true, true);
	    	catLatenciasAVG.add(CompSimplesCompMegaCL);
	    }
	    System.out.println("\n(7) Qntd Registros compSimplesCompMega.csv: "+compSimplesCompMega.size());
	    
	    if (!all.isEmpty()) {
	    	Double averageAll = all.stream().mapToDouble(val -> val).average().getAsDouble();
	    	csvReader.exportCSVData(all, "all.csv");
	    	CatLatencia allCL = new CatLatencia(averageAll, true, true, true);
	    	catLatenciasAVG.add(allCL);
	    }
	    System.out.println("\n(8) Qntd Registros all.csv: "+all.size());
	    
	    System.out.println("\n** Qntd Total Registros: "+catLatencias.size());
	    return catLatenciasAVG;
	}

}
