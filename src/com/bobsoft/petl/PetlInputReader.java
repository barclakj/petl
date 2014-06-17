package com.bobsoft.petl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class PetlInputReader {
	private static Logger log = Logger.getLogger(PetlInputReader.class.getCanonicalName());
	private List<String> dataLines = null;
	private String delimiter = "|";
	private int index = 0;
	
	
	public static String getValue(String param, List<String> vals) {
		String paramNum = param.substring(1, param.length()-1);
		System.out.println(paramNum);
		int pnum = -1;
		try {
			pnum = Integer.parseInt(paramNum);
		} catch (NumberFormatException nfe) {
			pnum = -1;
		}
		if (pnum<0 || pnum>=vals.size()) {
			return null;
		} else {
			return vals.get(pnum);
		} 
	}
	
	public void init(String filename) {
		init(filename, "|");
	}
	
	public void init(String filename, String _del) {
		delimiter = _del;
		dataLines = readFile(filename);
		index = 0;
	}
	
	public List<String> splitLine(String line) {
		List<String> vals = new ArrayList<String>();
		
		int prevIdx = 0;
		int idx = -1;
		while(((idx=line.indexOf(delimiter, idx)))>=0) {
			String val = line.substring(prevIdx, idx);
			log.finest("Found: " + val);
			vals.add(val);
			prevIdx = idx+1;
			idx++;
		}
		if (prevIdx==0 && idx==-1) {
			vals.add(line);
		} else if (prevIdx>0) {
			vals.add(line.substring(prevIdx));
		}
		
		return vals;
	}
		
	public String next() {
		String line = null;
		if (dataLines!=null) {
			if (index<dataLines.size()) {
				line = dataLines.get(index);
				index++;
			}
		}
		log.finest("Read next line: " + line);
		return line;
	}
	
	private static List<String> readFile(String filename) {
		File f = new File(filename);
		if (f.exists()) {
			List<String> dataLines = new ArrayList<String>();
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
				
				String line = null;
				while ((line=br.readLine())!=null) {
					dataLines.add(line);
				}
				br.close();
				br = null;
			} catch (IOException e) {
				log.warning("IOException reading input file: " + e.getMessage() + " (" + filename + ")");
				dataLines = null;
			}
			return dataLines;
		} else {
			log.warning("File does not exist: " + filename);
			return null;
		}
	}
}
