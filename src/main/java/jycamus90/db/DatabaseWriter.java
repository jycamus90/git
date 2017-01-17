package jycamus90.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter {
	private static Connection c = null;

	private Map<String, Integer> aid = new HashMap<String, Integer>();

	private static int AuthorID = 0; 

	public DatabaseWriter(Connection c) {
		this.c = c;
	}
	
	public void clean(){
		String author = "DROP TABLE IF EXISTS AUTHOR;";
		String fileHistory = "DROP TABLE IF EXISTS FILE_HISTORY";
		String commitFile = "DROP TABLE IF EXISTS COMMIT_FILE";
		String sourceLineAuthorHistory = "DROP TABLE IF EXISTS SOURCE_LINE_AUTHOR_HISTORY";
		String sourcePath = "DROP TABLE IF EXISTS SOURCE_PATH";
		
		execute(author);
		execute(fileHistory);
		execute(commitFile);
		execute(sourceLineAuthorHistory);
		execute(sourcePath);
		
	}
	
	private int getAuthorID(String author){

		int id;
		if(!aid.containsKey(author)){
			id = AuthorID++;
			aid.put(author, id);
		}
		else id = aid.get(author);
		return id;
	}
	
	public void createAuthorTable(){
		String authorTable="CREATE TABLE IF NOT EXISTS `AUTHOR` ( "
				+ "`AUTHOR_ID`		INTEGER,"
				+ "`AUTHOR`	    	TEXT"			
				+ ");";

		execute(authorTable);
		
		System.out.println("Create AUTHOR table");
	}

	public void insertAuthorTable(String author){
		System.out.println("Insert author " + author);
		String sql = "INSERT INTO AUTHOR "
				+"VALUES(?,?)";
		executePsmt(sql, getAuthorID(author), author);
	}
	
	public void createFileHistoryTable(){
		String fileHistoryTable="CREATE TABLE IF NOT EXISTS `FILE_HISTORY` ( "
				+ "`OLD_FQN`		TEXT,"
				+ "`NOW_FQN`	TEXT"			
				+ ");";
		
		execute(fileHistoryTable);
		
		System.out.println("Create File_History table");
	}
	
	public void insertFileHistoryTable(String old, String now){
		System.out.println("Insert old file name: " + old + " now: " + now);
		String sql = "INSERT INTO FILE_HISTORY "
				+"VALUES(?,?)";
		executePsmt(sql, old, now);
	}
	
	public void createCommitTable(){
		String commitTable="CREATE TABLE IF NOT EXISTS `COMMIT_FILE` ( "
				+ "`COMMIT_ID`	TEXT,"
				+ "`SOURCE_FQN`	TEXT"			
				+ ");";
		
		execute(commitTable);
		
		System.out.println("Create COMMIT table");
	}
	
	public void insertCommitTable(String commit, String source){
		System.out.println("Insert commit " + commit);
		String sql = "INSERT INTO COMMIT_FILE "
				+"VALUES(?,?)";
		executePsmt(sql, commit, source);
	}

	public void createSourceLineAuthorHistoryTable(){
		String sourceLineAuthorTable="CREATE TABLE IF NOT EXISTS `SOURCE_LINE_AUTHOR_HISTORY` ( "
				+ "`SOURCE_ID`		INTEGER,"
				+ "`LINE_NUM`	    INTEGER,"
				+ "`COMMIT_ID`	    	TEXT,"
				+ "`AUTHOR_ID`	    INTEGER,"
				+ "`DATE`	    	TEXT"
				+ ");";

		execute(sourceLineAuthorTable);
		
		System.out.println("Create Source_LINE_AUTHOR_HISTORY table");
	}

	public void insertSourceLineAuthorHistoryTable(int sourceId, int lineNum, String commit, String author, String date){
		System.out.println("Insert source " + sourceId + " line " + lineNum + " author " + author);
		String sql = "INSERT INTO SOURCE_LINE_AUTHOR_HISTORY "
				+"VALUES(?,?,?,?,?)";
		executePsmt(sql, sourceId, lineNum, commit, getAuthorID(author), date);
	}
	
	public void createSourcePathTable(){
		String fileHistoryTable="CREATE TABLE IF NOT EXISTS `SOURCE_PATH` ( "
				+ "`SOURCE_ID`		INTEGER,"
				+ "`PATH`	TEXT"			
				+ ");";
		
		execute(fileHistoryTable);
		
		System.out.println("Create Source_path table");
	}
	
	public void insertSourcePathTable(int sourceId, String path){
		System.out.println("Insert sourceId " + sourceId + " path: " + path);
		String sql = "INSERT INTO SOURCE_PATH "
				+"VALUES(?,?)";
		executePsmt(sql, sourceId, path);
	}
	
	protected void execute(String query){
		try {
			Statement s = c.createStatement();
			s.execute(query);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	protected void executePsmt(String sql, Object... args) {
		try{
			PreparedStatement psmt = c.prepareStatement(sql);
			for(int i=0; i<args.length; ++ i){
				psmt.setObject(i+1, args[i]);
			}
			psmt.executeUpdate();
			psmt.close();
		}catch(Exception e){
			System.out.println(sql);
			e.printStackTrace();
		}
	}
	
}
