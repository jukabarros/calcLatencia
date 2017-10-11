package csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import entity.CassandraTime;
import entity.YCSBTime;

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
					System.out.println(file.getName());
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
				Date init = this.convertToDateFromCassandraLog(csvLine[1]);
				Date end = this.convertToDateFromCassandraLog(csvLine[2]);
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
	public List<YCSBTime> readCSVFileFromLogYCSB(String csvFile) throws IOException {
		BufferedReader br = null;
		String line = "";
		String lineSplitBy = ";";
		List<YCSBTime> ycsbTimes = new ArrayList<YCSBTime>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] csvLine = line.split(lineSplitBy);
				Integer sample = Integer.parseInt(csvLine[0]);
				Date date = this.convertToDateFromYCSBLog(csvLine[1]);
				double txReq = Double.parseDouble(csvLine[2]);
				double latency = Double.parseDouble(csvLine[3]);
				YCSBTime yt = new YCSBTime(sample, date, txReq, latency);
				ycsbTimes.add(yt);
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
		return ycsbTimes;
	}

	/**
	 * Converte a string data do CSV do Log Cassandra
	 * para o formato Date
	 * @param dateStr
	 * @return
	 */
	private Date convertToDateFromCassandraLog(String dateStr) {
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
	
	/**
	 * Converte a string data do CSV do Log YCSB
	 * para o formato Date
	 * @param dateStr
	 * @return
	 */
	private Date convertToDateFromYCSBLog(String dateStr) {
		// Exemplo: 11:44:05:460
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		try {
			Date date = formatter.parse(dateStr);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println("Erro na conversão da Data");
			return null;
		}
	}
	
	/**
	 * Exporta as listas com os valores das combinacoes 
	 * para um arquivo CSV
	 * @param avgs
	 */
	public void exportCSVData(List<Double> lists, String nameCSV) {
		FileWriter writer;
		try {
			writer = new FileWriter("results/"+nameCSV);
			for (int i = 0; i < lists.size(); i++) {
				writer.append(lists.get(i)+"\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
