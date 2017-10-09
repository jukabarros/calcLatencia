package csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import entity.CassandraTime;

public class CSVReader {

	/**
	 * Ler todos os CSV de um repositorio especifico
	 * @param csvDirectory
	 */
	public List<String> readCSVDirectory(String csvDirectory) {
		File directory = new File(csvDirectory);
		List<String> allCsvFiles = new ArrayList<String>();
		//get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()){
				if (file.getName().endsWith(".csv")){
					allCsvFiles.add(file.getAbsolutePath());
				}
				else {
					System.out.println("*** Erro: "+file.getName()+ " não é um arquivo CSV");
				}
			}
		}

		return allCsvFiles;
	}

	/**
	 * Ler um csv especifico do Log do Cassandra
	 * @param csvFile
	 * @throws IOException 
	 */
	public List<CassandraTime> readCSVFileFromLogCassandra(String csvFile) throws IOException {
		BufferedReader br = null;
		String line = "";
		String lineSplitBy = ";";
		List<CassandraTime> cassandraTimes = new ArrayList<CassandraTime>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] csvLine = line.split(lineSplitBy);
				Integer sample = Integer.parseInt(csvLine[0]);
				Date init = this.convertToDate(csvLine[1]);
				Date end = this.convertToDate(csvLine[2]);
				Integer time = Integer.parseInt(csvLine[3]);
				CassandraTime ct = new CassandraTime(sample, init, end, time);
				cassandraTimes.add(ct);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return cassandraTimes;
	}

	/**
	 * Ler um csv especifico do Log do YCSB
	 * @param csvFile
	 * @throws IOException 
	 */
	public void readCSVFileFromLogYCSB(String csvFile) throws IOException {
		BufferedReader br = null;
		String line = "";
		String lineSplitBy = ";";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] csvLine = line.split(lineSplitBy);
				String sample = csvLine[0];
				String date = csvLine[1];
				String txReq = csvLine[2];
				String latency = csvLine[3];
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Converte a string data do CSV para o formato Date
	 * @param dateStr
	 * @return
	 */
	public Date convertToDate (String dateStr) {
		// Exemplo: 13:21:40,134
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss,SSS");
		try {
			Date date = formatter.parse(dateStr);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println("Erro na conversão da Data");
			return null;
		}
	}

}
