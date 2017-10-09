package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import csv.CSVReader;
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
			csvReader.readCSVFileFromLogCassandra(allCsvs.get(1));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("*** Erro na leitura do CSV | AppMain.java ln 30");
		}
	}

}
