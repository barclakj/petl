package test.bobsoft.petl;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.bobsoft.petl.PetlApp;
import com.bobsoft.petl.Petl;
import com.bobsoft.petl.PetlInputReader;

public class TestPetl {

	@Test
	public void test() {
		String str1 = "select name from bob where index='{2}' and other='{1}' or {this is not{2}";
		String str2 = "{1}";
		String str3 = "{this is a test}";
		String str4 = "static value";
		
		Petl conf = new Petl();
		conf.setInputFile("/home/barclakj/Documents/sample.csv");
		conf.setOutputFile("/home/barclakj/Documents/sample-out.csv");
		
		conf.addOutputMap(str1);
		conf.addOutputMap(str2);
		conf.addOutputMap(str3);
		conf.addOutputMap(str4);
		
		PetlApp app = new PetlApp();
		
		PetlInputReader ir = new PetlInputReader();
		ir.init(conf.getInputFile());
		
		app.processData(ir, conf);
	}

	@Test
	public void testB() {
		Petl conf = new Petl();
		try {
			conf.loadJSON("/home/barclakj/Dropbox/Kevin/petl/test/config1.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
