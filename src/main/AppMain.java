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

		System.out.println("*** Arquivos CSVS ***");
		List<String> allCsvs = csvReader.readCSVDirectory(csvDirectory);

		List<CassandraTime> compMegaTimes = new ArrayList<CassandraTime>();
		List<CassandraTime> compSimplesTimes = new ArrayList<CassandraTime>();
		List<CassandraTime> flushTimes = new ArrayList<CassandraTime>();
		List<YCSBTime> ycsbTimes = new ArrayList<YCSBTime>();
		try {
			System.out.println("*** Inserindo os Dados dos CSVs na Memória");
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
					System.out.println("ycsb.csv");
					ycsbTimes = csvReader.readCSVFileFromLogYCSB(allCsvs.get(i));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("*** Erro na leitura do CSV | AppMain.java ln 30");
		}
		
		System.out.println("*** Resultado");
		List<CatLatencia> catLatencias = getLatenciasCategorizada(compMegaTimes, compSimplesTimes, flushTimes, ycsbTimes);
		System.out.println("Latency Flush CSimples CMega");
		for (int i = 0; i < catLatencias.size(); i++) {
			CatLatencia cl = catLatencias.get(i);
			System.out.println(cl.getLatency()+"\t"+cl.isIsflush()+"\t"+cl.isCompSimples()+"\t"+cl.isCompMega());
		}
		
		List<CatLatencia> catLatenciasAVG = getAvgCatlatencia(catLatencias);
		System.out.println("\n\n************* MEDIA *************");
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
				}
			}
			// Comp Simples
			for (int j = 0; j < compSimplesTimes.size(); j++) {
				if (compSimplesTimes.get(j).getDateInit().before(dateYcsb) && compSimplesTimes.get(j).getDateEnd().after(dateYcsb)) {
					cl.setCompSimples(true);
				}

			}
			// Comp Mega
			for (int j = 0; j < compMegaTimes.size(); j++) {
				if (compMegaTimes.get(j).getDateInit().before(dateYcsb) && compMegaTimes.get(j).getDateEnd().after(dateYcsb)) {
					cl.setCompMega(true);
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
		/*
		 * CALCULO DAS MEDIAS DE CADA COMBINACAO
		 */
	    Double averageNoOne = noOne.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia onOneCL = new CatLatencia(averageNoOne, false, false, false);
	    catLatenciasAVG.add(onOneCL);
	    
	    Double averageIsFlush = justFlush.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia isFlushCL = new CatLatencia(averageIsFlush, true, false, false);
	    catLatenciasAVG.add(isFlushCL);
	    
	    Double averageIsCompSimples = justCompSimples.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia isCompSimplesCL = new CatLatencia(averageIsCompSimples, false, true, false);
	    catLatenciasAVG.add(isCompSimplesCL);
	    
	    Double averageIsCompMega = justCompMega.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia isCompMegaCL = new CatLatencia(averageIsCompMega, false, false, true);
	    catLatenciasAVG.add(isCompMegaCL);
	    
	    Double averageFlushAndCompSimples = flushCompSimples.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia isFlushCompSimplesCL = new CatLatencia(averageFlushAndCompSimples, true, true, false);
	    catLatenciasAVG.add(isFlushCompSimplesCL);
	    
	    Double averageFlushAndCompMega = flushCompMega.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia isFlushCompMegaCL = new CatLatencia(averageFlushAndCompMega, true, false, true);
	    catLatenciasAVG.add(isFlushCompMegaCL);
	    
	    Double averageCompSimplesAndCompMega = compSimplesCompMega.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia CompSimplesCompMegaCL = new CatLatencia(averageCompSimplesAndCompMega, false, true, true);
	    catLatenciasAVG.add(CompSimplesCompMegaCL);
	    
	    Double averageAll = all.stream().mapToDouble(val -> val).average().getAsDouble();
	    CatLatencia allCL = new CatLatencia(averageAll, true, true, true);
	    catLatenciasAVG.add(allCL);
	    
	    return catLatenciasAVG;
	}

}
