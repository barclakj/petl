package com.bobsoft.petl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PetlApp {
	private static Logger log = Logger.getLogger(PetlApp.class.getCanonicalName());
	
	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("petl.jar <configfile.json>");
			System.exit(-1);
		}
		
		String confFile = args[0];
		
		do {
			confFile = processPetl(confFile);
		} while (confFile!=null);
	}
	
	public static String processPetl(String confFile) {
		PetlApp app = new PetlApp();
		Petl config = app.loadConfig(confFile);
		System.out.println(new Date() + " - Executing Petl: " + config.getTitle());
		PetlInputReader ir = app.loadData(config);
		app.processData(ir, config);
		config.disconnect();
		
		String next = config.getNextPetl();
		ir = null;
		config = null;
		return next;
	}
	
	public Petl loadConfig(String filename) {
		Petl conf = new Petl();
		try {
			conf.loadJSON(filename);
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "FileNotFoundException:" + e.getMessage() + " (File: " + filename + ")", e);
			conf = null;
		}
		return conf;
	}

	public PetlInputReader loadData(Petl conf) {
		PetlInputReader ir = new PetlInputReader();
		ir.init(conf.getInputFile());
		return ir;
	}
	
	public void processData(PetlInputReader src, Petl conf) {
		String line = null;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(conf.getOutputFile())));
			while((line=src.next())!=null) {
				List<String> inputValues = src.splitLine(line);
				List<String> outputValues = new ArrayList<String>();
				for(int i=0;i<conf.getOutputParamSize();i++) {
					outputValues.add( conf.merge(i, inputValues));
				}
				StringBuffer outputLine = new StringBuffer();
				boolean pad = false;
				for(String val : outputValues) {
					if (pad) {
						outputLine.append(conf.getDelimiter());
					}
					if (val==null) {
						log.finest("Null - append zero length string... (or not since a waste of time)");
						// outputLine.append("");
					} else {
						outputLine.append(val);
					}
					pad = true;
				}
				bw.write(outputLine.toString() + "\n");
				bw.flush();
			}
			bw.close();
			bw = null;
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException:" + e.getMessage() + " (OF: " + conf.getOutputFile() + ")", e);
		}
	}
}
