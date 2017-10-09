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
		System.out.println("*** END ***");
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

}
