package jycamus90.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.spideruci.cerebro.layout.model.SourceLineNode;

public class DatabaseReader {
	private static Connection c = null;
	
	private List<String> authorList = new ArrayList<>();
	private List<String> allCommitList = new ArrayList<>();
	private Map<Integer, Map<Integer, Integer>> stmtMap = new LinkedHashMap<>();
	private Map<Integer, Double> suspiciousMap = new HashMap<>();
	private Map<Integer, Double> confidenceMap = new HashMap<>();
	private Map<String, Integer> sourceMap = new HashMap<>();
	private Map<Integer, String> sourcePathMap = new HashMap<>();
	private Map<String, List<String>> commitFilesMap = new HashMap<>();
	private Map<String, List<String>> fileHistoryMap = new HashMap<>();

	public DatabaseReader(Connection c){
		this.c = c;
	}
	
	public Map<Integer, Map<Integer, Integer>> getStmtMap(){
		return stmtMap;
	}
	
	public Map<Integer, Double> getSuspiciousMap(){
		return suspiciousMap;
	}
	
	public Map<Integer, Double> getConfidenceMap(){
		return confidenceMap;
	}
	
	public Map<String, Integer> getSourceMap(){
		return sourceMap;
	}
	
	public Map<Integer, String> getSourcePathMap(){
		return sourcePathMap;
	}
	
	public int getAuthorCount(){
		return authorList.size();
	}
	
	public List<String> getAuthorList(){
		return authorList;
	}
	
	public List<String> getAllCommitList(){
		return allCommitList;
	}
	
	public Map<String, List<String>> getCommitFileMap(){
		return commitFilesMap;
	}
	
	public Map<String, List<String>> getFileHistoryMap(){
		return fileHistoryMap;
	}
	
	public void getSTMT(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM STMT";
			ResultSet rs = s.executeQuery(sql);
						
			while(rs.next()){
				int stmtId = rs.getInt("STMT_ID");
				int sourceId = rs.getInt("SOURCE_ID");
				int lineNum = rs.getInt("LINE_NUM");
				
				Map<Integer, Integer> temp = stmtMap.get(sourceId);

				if(temp == null){
					temp = new HashMap<>();
				}
				
				temp.put(lineNum, stmtId);

				stmtMap.put(sourceId, temp);
			}
			
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	
	public void getSuspicious(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM SUSPICIOUS";
			ResultSet rs = s.executeQuery(sql);

						
			while(rs.next()){
				int stmtId = rs.getInt("STMT_ID");
				double suspiciousValue = rs.getDouble("SUSPICIOUS");
				
				suspiciousMap.put(stmtId, suspiciousValue);
								
			}
			
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getConfidence(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM CONFIDENCE";
			ResultSet rs = s.executeQuery(sql);
						
			while(rs.next()){
				int stmtId = rs.getInt("STMT_ID");
				double confidenceValue = rs.getDouble("CONFIDENCE");
				
				confidenceMap.put(stmtId, confidenceValue);
								
			}
			
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getSource(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM SOURCE";
			ResultSet rs = s.executeQuery(sql);
						
			while(rs.next()){
				int sourceId = rs.getInt("SOURCE_ID");
				String fqn = rs.getString("FQN");
				
				sourceMap.put(fqn, sourceId);
								
			}
			
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getSourceId(String fqn){
		Statement s;
		int sourceId = -1;
		
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM SOURCE "
					+ "WHERE FQN LIKE \"%" + fqn + "%\"";
			ResultSet rs = s.executeQuery(sql);

			sourceId = rs.getInt("SOURCE_ID");

			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return sourceId;
	}
	
	public void getSourcePath(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM SOURCE_PATH";
			ResultSet rs = s.executeQuery(sql);
						
			while(rs.next()){
				int sourceId = rs.getInt("SOURCE_ID");
				String path = rs.getString("PATH");
				
				sourcePathMap.put(sourceId, path);			
			}
			
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getUniqueCommits(){
		Statement s;
		
		try {
			s = c.createStatement();
			String sql = "SELECT DISTINCT COMMIT_ID "
					+ "FROM SOURCE_LINE_AUTHOR_HISTORY";
			ResultSet rs = s.executeQuery(sql);
						
			while(rs.next()){
				String commit = rs.getString("COMMIT_ID");

				allCommitList.add(commit);
			}
			
			s.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getAuthorId(String name){
		Statement s;
		int authorId = 0;
		
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM AUTHOR "
					+ "WHERE AUTHOR LIKE \"%" + name + "%\"";
			ResultSet rs = s.executeQuery(sql);
			
			authorId = rs.getInt("AUTHOR_ID");
			
			s.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return authorId;
	}
	
	public Map<Integer, List<Integer>> getSourceLineByAuthor(String author){
		Statement s;
		Map<Integer, List<Integer>> sourceLineByAuthor = new HashMap<>();

		try {
			s = c.createStatement();
			
			String sql1 = "SELECT * "
					+ "FROM AUTHOR "
					+ "WHERE AUTHOR LIKE \"%" + author + "%\"";
			
			ResultSet rs1 = s.executeQuery(sql1);
			
			int authorId = rs1.getInt("AUTHOR_ID");

			String sql2 = "SELECT * "
					+ "FROM SOURCE_LINE_AUTHOR_HISTORY "
					+ "WHERE AUTHOR_ID = " + authorId;

			
			ResultSet rs2 = s.executeQuery(sql2);
						
			while(rs2.next()){
				int sourceId = rs2.getInt("SOURCE_ID");
				int lineNum = rs2.getInt("LINE_NUM");
				
				if(sourceLineByAuthor.get(sourceId) == null)
					sourceLineByAuthor.put(sourceId, new ArrayList<Integer>());
				
				sourceLineByAuthor.get(sourceId).add(lineNum);
			}
			
			s.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sourceLineByAuthor;
	}
	
	
	public List<String> getCommits(int sourceId, int lineNum){
		Statement s;
		List<String> commits = new ArrayList<>();
		
		try {
			s = c.createStatement();
			String sql = "SELECT COMMIT_ID "
					+ "FROM SOURCE_LINE_AUTHOR_HISTORY "
					+ "WHERE SOURCE_ID = " + sourceId + " AND LINE_NUM = " + lineNum;
			ResultSet rs = s.executeQuery(sql);
			
			while(rs.next()){
				String commitId = rs.getString("COMMIT_ID");
				
				commits.add(commitId);			
			}
			
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return commits;
	}
	
	
	public void getCommitFiles(){
		Statement s;
		
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM COMMIT_FILE";
			ResultSet rs = s.executeQuery(sql);
			
			while(rs.next()){
				String commitId = rs.getString("COMMIT_ID");
				String file = rs.getString("SOURCE_FQN");
				
				if(commitFilesMap.get(commitId) == null)
					commitFilesMap.put(commitId, new ArrayList<String>());
				
				commitFilesMap.get(commitId).add(file);			
			}
			s.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getCommitFiles(String commitId){
		Statement s;
		List<String> fileList = new ArrayList<>();
		
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM COMMIT_FILE "
					+ "WHERE COMMIT_ID LIKE \"%" + commitId + "%\"";
			ResultSet rs = s.executeQuery(sql);
			
			while(rs.next()){
				String file = rs.getString("SOURCE_FQN");
				
				fileList.add(file);			
			}
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return fileList;
	}
	
	public void getFileHistory(){
		Statement s;
		
		try {
			s = c.createStatement();
			String sql = "SELECT * "
					+ "FROM FILE_HISTORY";
			ResultSet rs = s.executeQuery(sql);
			
			while(rs.next()){
				String oldFqn = rs.getString("OLD_FQN");
				String nowFqn = rs.getString("NOW_FQN");
				
				if(fileHistoryMap.get(oldFqn) == null)
					fileHistoryMap.put(oldFqn, new ArrayList<String>());
				
				fileHistoryMap.get(oldFqn).add(nowFqn);			
			}
			s.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected ResultSet executePsmt(String sql, Object... args) {
		ResultSet rs = null;
		
		try{
			PreparedStatement psmt = c.prepareStatement(sql);
			for(int i=0; i<args.length; ++ i){
				psmt.setObject(i+1, args[i]);
			}
			rs = psmt.executeQuery();			
			psmt.close();
		}catch(Exception e){
			System.out.println(sql);
			e.printStackTrace();
		}
		return rs;
	}

}
