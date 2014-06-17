package com.bobsoft.petl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBSource {
	private static Logger log = Logger.getLogger(DBSource.class.getCanonicalName());
	
	private Connection conn = null;
	
	public void disconnect() {
		if (conn!=null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				conn = null;
			}
		}
	}
	
	public void init(String classname, String connString) {
		log.finest("Connecting to: " + connString + " with driver: " + classname);
		try {
			Class.forName(classname);
		} catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		try {
			conn = DriverManager.getConnection(connString);
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			conn = null;
			System.exit(-1);
		}
	}
	
	public String execute(String statement, List<String> sqlParams) {
		int rowsAffected = 0;
		PreparedStatement stmt = null;
		
		try {
			log.finest("Executing update: " + statement);
			stmt = conn.prepareStatement(statement);
			stmt.clearParameters();
			for (int i=0; i<sqlParams.size();i++) {
				String v = sqlParams.get(i);
				if (v!=null && v.equalsIgnoreCase("")) {
					v = null;
				}
				log.finest("Binding param idx: " + (i+1) + " => " + v);
				if (v!=null) {
					stmt.setString((i+1), v);
				} else {
					stmt.setNull((i+1), java.sql.Types.VARCHAR);
				}
			}
			
			rowsAffected = stmt.executeUpdate();
			log.finest("Updated " + rowsAffected + " rows");
			stmt.close();
		} catch (SQLException e) {
			StringBuffer buf = new StringBuffer();
			for (int i=0; i<sqlParams.size();i++) {
				buf.append( sqlParams.get(i) + ",");
			}
			log.log(Level.WARNING, "\"" + statement + "\" (" + buf.toString() + "): " + e.getMessage()); // , e);
		} finally {
			stmt = null;
		}
		
		return "" + rowsAffected;
	}
	
	public String query(String query, List<String> sqlParams, String delimiter) {
		StringBuffer response = null;
		ResultSet rset = null;
		PreparedStatement stmt = null;
		
		try {
			log.finest("Executing: " + query);
			stmt = conn.prepareStatement(query);
			stmt.clearParameters();
			for (int i=0; i<sqlParams.size();i++) {
				String v = sqlParams.get(i);
				if (v!=null && v.equalsIgnoreCase("")) {
					v = null;
				}
				log.finest("Binding param idx: " + (i+1) + " => " + v);
				if (v!=null) {
					stmt.setString((i+1), v);
				} else {
					stmt.setNull((i+1), java.sql.Types.VARCHAR);
				}
			}
			
			rset = stmt.executeQuery();
			int cols = rset.getMetaData().getColumnCount();
			if (rset.next()) {
				response = new StringBuffer();
				log.finest("Found result... ");
				for (int i=0;i<cols;i++) {
					Object val = rset.getObject((i+1));
					if (val==null) {
						val = "";
					}
					if (response.length()>0) {
						response.append(delimiter);
					}
					response.append(val);
				}
			} else {
				response = new StringBuffer();
				for (int i=0;i<(cols-1);i++) {
					response.append(delimiter);
				}
			}
			rset.close();
			stmt.close();
		} catch (SQLException e) {
			StringBuffer buf = new StringBuffer();
			for (int i=0; i<sqlParams.size();i++) {
				buf.append( sqlParams.get(i) + "," );
			}
			log.log(Level.WARNING, "\"" + query + "\" (" + buf.toString() + "): " + e.getMessage()); // , e);
		} finally {
			rset = null;
			stmt = null;
		}
		
		if (response!=null) return response.toString();
		else return null;
	}
}
