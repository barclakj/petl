package com.bobsoft.petl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

public class Petl {
	private static Logger log = Logger.getLogger(Petl.class.getCanonicalName());
	
	private String inputFile = null;
	private String outputFile = null;
	private String delimiter = "|";
	private String driver = null;
	private String connectionString = null;
	private DBSource dbSource = null;
	
	private Map<Integer, List<Integer>> outputParamMap = new HashMap<Integer, List<Integer>>();
	private Map<Integer, String> outputMap = new HashMap<Integer, String>();
	
	private String title = null;
	private String nextPetl = null;
	
	public void disconnect() {
		if (dbSource!=null) {
			dbSource.disconnect();
			dbSource = null;
		}
	}
	
	
	
	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getNextPetl() {
		return nextPetl;
	}



	public void setNextPetl(String nextPetl) {
		if (nextPetl==null || nextPetl.trim().equalsIgnoreCase("")) {
			this.nextPetl = null;
		} else this.nextPetl = nextPetl;
	}



	public String getDriver() {
		return driver;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public void setConnection(String connectionString, String driver) {
		log.finest("Connection: " + connectionString  + " (" + driver + ")");
		this.connectionString = connectionString;
		this.driver = driver;
		if (connectionString!=null && driver !=null) {
			dbSource = new DBSource();
			dbSource.init(driver, connectionString);
		}
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		log.finest("InputFile: " + inputFile);
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		log.finest("OutputFile: " + outputFile);
		this.outputFile = outputFile;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		log.finest("Delimiter: " + delimiter);
		this.delimiter = delimiter;
	}

	public int getOutputParamSize() {
		return outputMap.size();
	}

	public void addOutputMap(String outputParamString) {
		log.finest("Output Item: " + outputParamString);
		Integer pos = outputMap.size();
		outputMap.put(pos, outputParamString);
		List<Integer> params = new ArrayList<Integer>();
		int idx = -1;
		while(((idx=outputParamString.indexOf("{", idx+1)))>=0) {
			String paramName = outputParamString.substring(idx, outputParamString.indexOf("}", idx)+1);
			// log.info("Found: " + paramName + " in " + outputParamString);
			int pval = getParamNumber(paramName);
			if (pval>=0 && !params.contains(pval)) {
				params.add( pval );
			}
		}
		outputParamMap.put(pos, params);
	}
	
	public String merge(int paramNum, List<String> values) {
		boolean sqlexec = false;
		List<String> sqlParams = null;
		String result = "";
		String cmd = outputMap.get(paramNum);
		if (cmd!=null) {
			if (cmd.startsWith("@")) {
				sqlexec = true;
				sqlParams = new ArrayList<String>();
			}
			StringBuffer sb = new StringBuffer();
			
			int prevIdx = 0;
			int idx = -1;
			while(((idx=cmd.indexOf("{", idx+1)))>=0) {
				log.finest(sb.toString() + " " + idx + " " + prevIdx + " " + cmd);
				sb.append(cmd.substring(prevIdx, idx));
				
				String paramName = cmd.substring(idx, cmd.indexOf("}", idx)+1);
				// System.out.println("PNAME:" + paramName);
				int pval = getParamNumber(paramName);
				pval = pval -1; 
				if (pval>=0 && pval<values.size()) {
					String val = values.get(pval);
					if (!sqlexec) {
						sb.append(val);
					} else {
						sb.append("?");
						sqlParams.add(val);
					}
					prevIdx = cmd.indexOf("}", idx)+1;
				} else {
					if (pval<0) {
						// invalid number treat as a string and passthrough
						sb.append("{");
						prevIdx = idx+1;
					} else {
						// real number, just not in set.
						prevIdx = cmd.indexOf("}", idx)+1;
					}
				}
				
			}
			sb.append( cmd.substring(prevIdx) );
			result = sb.toString();
		}
		
		if (result!=null && sqlexec && dbSource!=null) {
			// it's a sql command so get the result from the database
			String stmt = result.substring(1);
			log.finest("Calling database with statement: " + stmt);
			if (stmt!=null && stmt.startsWith("select ")) {
				result = dbSource.query(stmt, sqlParams, delimiter);
			} else if (stmt!=null && 
					(stmt.startsWith("insert ") ||
							stmt.startsWith("update ") ||
							stmt.startsWith("delete ")	) ) {
				result = dbSource.execute(stmt, sqlParams);
			}
		}

		return result;
	}
	
	
	private static int getParamNumber(String param) {
		String paramNum = param.substring(1, param.length()-1);
		int pnum = -1;
		try {
			pnum = Integer.parseInt(paramNum);
		} catch (NumberFormatException nfe) {
			pnum = -1;
		}
		return pnum;
	}
	
	public void loadJSON(String filename) throws FileNotFoundException {
		JsonParserFactory factory=JsonParserFactory.getInstance();
		JSONParser parser=factory.newJsonParser();
		Map jsonData=parser.parseJson(new FileInputStream(filename), "UTF-8");
		
		this.setDelimiter( (String)(((Map)jsonData.get("petl")).get("delimiter")) );
		this.setInputFile( (String)(((Map)jsonData.get("petl")).get("input")) );
		this.setOutputFile( (String)(((Map)jsonData.get("petl")).get("output")) );
		this.setNextPetl( (String)(((Map)jsonData.get("petl")).get("nextPetl")) );
		this.setTitle( (String)(((Map)jsonData.get("petl")).get("title")) );
		
		ArrayList outputParams = (ArrayList)(((Map)jsonData.get("petl")).get("outputFormat"));
		for(int i=0;i<outputParams.size();i++) {
			this.addOutputMap((String)(outputParams.get(i)));
		}
		
		this.setConnection( (String)(((Map)jsonData.get("petl")).get("connString")),
						(String)(((Map)jsonData.get("petl")).get("driver"))
						);
	}
}
