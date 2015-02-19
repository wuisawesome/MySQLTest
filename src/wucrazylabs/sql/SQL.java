package wucrazylabs.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface SQL {
	public boolean INSERT(String tableName, String[] cols, String[][] values)
			throws SQLException;

	public boolean safeINSERT(String tableName, String[] cols, String[][] values);

	public List<Map<String, String>> SELECT(String[] cols, String tableName,
			SQLParam[] params, String others) throws SQLException;

	public List<Map<String, String>> safeSELECT(String[] cols,
			String tableName, SQLParam[] params, String others);
}