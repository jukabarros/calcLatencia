package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csv.CSVReader;
import entity.CassandraTime;
import entity.CatLatencia;

public class AppMain {

	public static void main(String[] args) {
		String csvDirectory = "";
		if (args.length == 0) {
			csvDirectory = "csvs/";
		} else {
			csvDirectory = args[0];
		}
		CSVReader csvReader = new CSVReader();
		List<CatLatencia> clFlush = new ArrayList<CatLatencia>();

		List<String> allCsvs = csvReader.readCSVDirectory(csvDirectory);
		System.out.println("*** Arquivos CSVS ***");
		System.out.println(allCsvs);
		
		try {
			List<CassandraTime> compMegaTimes = new ArrayList<CassandraTime>();
			List<CassandraTime> compSimplesTimes = new ArrayList<CassandraTime>();
			List<CassandraTime> flushTimes = new ArrayList<CassandraTime>();
			for (int i = 0; i < allCsvs.size(); i++) {
				if (allCsvs.get(i).endsWith("compMega.csv")) {
					compMegaTimes = csvReader.readCSVFileFromLogCassandra(allCsvs.get(i));
				}
				else if (allCsvs.get(i).endsWith("compSimples.csv")) {
					
				}
				else if (allCsvs.get(i).endsWith("flush.csv")) {
					
				}
			}
			csvReader.readCSVFileFromLogCassandra(allCsvs.get(0));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("*** Erro na leitura do CSV | AppMain.java ln 30");
		}
	}

}
