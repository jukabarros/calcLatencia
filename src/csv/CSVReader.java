package csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	 * Ler um csv especifico
	 * @param csvFile
	 * @throws IOException 
	 */
	public void readCSVFileFromLogCassandra(String csvFile) throws IOException{
		BufferedReader br = null;
		String line = "";
		String lineSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] csvLine = line.split(lineSplitBy);
				String sample = csvLine[0];
				String dateInit = csvLine[1];
				String dateEnd = csvLine[2];
				String time = csvLine[3];
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

}
