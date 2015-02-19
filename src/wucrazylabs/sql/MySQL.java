package wucrazylabs.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MySQL implements SQL {
	
	public static final int DEFAULT_PORT = 3306;
	
	protected Connection conn;
	protected String db;

	public static MySQL safeInit(String user, String password,
			String serverName, long port, String db) {
		try {
			return new MySQL(user, password, serverName, port, db);
		} catch (Exception e) {
			return null;
		}
	}

	public MySQL(String user, String password, String serverName, long port,
			String db) throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");

		this.db = db;
		// Properties prop = new Properties();
		// prop.put("user", user);
		// prop.put("password", password);
		String url = "jdbc:mysql://" + serverName + ":" + port + "/" + db;
		// String url = "jbdc:mysql:"+serverName+"/"+db;
		System.out.println(url);
		conn = DriverManager.getConnection(url, user, password);

	}

	public boolean INSERT(String tableName, String[] cols, String[][] values)
			throws SQLException {
		String query = "INSERT INTO " + tableName;
		if (cols.length > 0) {
			query += "(";
			for (int i = 0; i < cols.length; i++)
				query += " " + cols[i] + ",";
			query = query.substring(0, query.length() - 1);
			query += ")";
		}
		query += " VALUES ";

		String[] params = new String[cols.length * values.length];
		for (int i = 0; i < values.length; i++) {
			query += "(";
			for (int j = 0; j < values[i].length; j++) {
				query += "?,";
				int index = i * values[i].length + j;
				params[index] = values[i][j];
			}
			query = query.substring(0, query.length() - 1);
			query += "),";
		}
		query = query.substring(0, query.length() - 1);

		executeUpdate(query, params);
		return true;
	}

	public boolean safeINSERT(String tableName, String[] cols, String[][] values) {
		try {
			return INSERT(tableName, cols, values);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * @param cols The columns which will be returned
	 * @param tableName The table name to query
	 * @param params Aditional parameters to filter results by
	 * @param others additional information you would like to be added to the query THIS IS NOT SANITIZED
	 * @return A list of maps where each map entry the list represents 1 row of data mapped by column as key and entry as value
	 * @throws SQLException
	 */
	public List<Map<String, String>> SELECT(String[] cols, String tableName,
			SQLParam[] params, String others) throws SQLException {
		String query = "SELECT ";
		if (cols.length < 1)
			query += "*";
		else {
			/*
			 * MySQL is ridiculously dumb and can't bind column names in
			 * prepared statements either.
			 */
			// query += "?";
			// for (int i = 1; i < cols.length; i++)
			// query += ",'?'";
			for (int i = 0; i < cols.length; i++) {
				String str = cols[i];
				query += str + ", ";
			}
			query = query.substring(0, query.length() - 2);
		}

		query += " FROM " + tableName;

		/*
		 * MySQL table param can't be binded, must be statically chosen because
		 * MySQL is stupid
		 */
		// query += " FROM ?";

		if (params.length >= 1) {
			/* Column names can't be in prepared statements, same as above. */
			// query += " WHERE ?=?";
			query += " WHERE " + params[0].col + "=?";
			// for (int i = 0; i < cols.length; i++)
			// query += "&&?=?";
			for (int i = 1; i < params.length; i++)
				query += "&&" + params[i].col + "=?";
		}

		if (others!=null)
			query += " " + others;
		
		String[] bindParams = new String[params.length];
		// String[] bindParams = new String[cols.length + 1 + (2 *
		// params.length)];
		// int i = 0;
		// for (i = 0; i < cols.length; i++)
		// bindParams[i] = cols[i];
		// bindParams[i] = tableName;
		for (int j = 0; j < params.length; j++) {
			// bindParams[++i] = params[j].col;
			// bindParams[++i] = params[j].value;
			bindParams[j] = params[j].value;
		}

		ResultSet results = executeQuery(query, bindParams);
		if (results == null)
			return null;

		return parseResultSet(results);
	}

	/**
	 * @param cols The columns which will be returned
	 * @param tableName The table name to query
	 * @param params Aditional parameters to filter results by
	 * @param others additional information you would like to be added to the query THIS IS NOT SANITIZED
	 * @return A list of maps where each map entry the list represents 1 row of data mapped by column as key and entry as value
	 * @throws SQLException
	 */
	public List<Map<String, String>> safeSELECT(String[] cols,
			String tableName, SQLParam[] params, String others) {
		try {
			return SELECT(cols, tableName, params, others);
		} catch (Exception e) {
			return null;
		}
	}

	protected List<Map<String, String>> parseResultSet(ResultSet results)
			throws SQLException {
		LinkedList<Map<String, String>> parsed = new LinkedList<Map<String, String>>();
		LinkedList<String> colNames = new LinkedList<String>();
		ResultSetMetaData rsmd = results.getMetaData();
		for (int j = 1; j <= rsmd.getColumnCount(); j++) {
			colNames.addLast(rsmd.getColumnName(j));
		}
		while (results.next()) {
			HashMap<String, String> map = new HashMap<String, String>();
			for (String colName : colNames)
				map.put(colName, results.getString(colName));
			parsed.addLast(map);
		}
		return parsed;
	}

	protected ResultSet executeQuery(String query, String[] bindParams)
			throws SQLException {
		PreparedStatement stmt;
		stmt = conn.prepareStatement(query);// Create prepared Statement
		for (int i = 0; i < bindParams.length; i++)
			// Bind all parameters to it
			stmt.setString(i + 1, bindParams[i]);
		ResultSet rs = stmt.executeQuery();
		return rs;
	}

	protected int executeUpdate(String query, String[] bindParams)
			throws SQLException {
		PreparedStatement stmt;
		stmt = conn.prepareStatement(query);// Create prepared Statement
		for (int i = 0; i < bindParams.length; i++)
			// Bind all parameters to it
			stmt.setString(i + 1, bindParams[i]);
		return stmt.executeUpdate();
	}
}
