package jycamus90.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jycamus90.db.DatabaseReader;
import jycamus90.db.DatabaseWriter;
import jycamus90.db.SQLiteDB;

public class GitLog {

	private static Map<String, Integer> source = new HashMap<>();
	private static Map<Integer, String> sourcePath = new HashMap<>();
	private static Map<Integer, Integer> lineCount = new HashMap<>();

	private static String ProjectPath;
	private static String DBPath;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ProjectPath = args[0];
		DBPath = args[1];
		
		SQLiteDB db = setUpDB(DBPath);
		
		getLineCount(ProjectPath);
		getAuthorList(ProjectPath, db);
		getAuthorHistory(ProjectPath, db);
		getCommits(ProjectPath, db);
		getFileHistory(ProjectPath, db);

	}

	public static SQLiteDB setUpDB(String dbPath){
		SQLiteDB db = new SQLiteDB();
		db.openConnection(dbPath);

		PathProcessing pp = new PathProcessing(ProjectPath);
		pp.start(dbPath);

		DatabaseReader dr = db.runDatabaseReader();
		dr.getSource();
		dr.getSourcePath();
		source = dr.getSourceMap();
		sourcePath = dr.getSourcePathMap();
		
		
		for(Entry<Integer, String> e: sourcePath.entrySet())
			System.out.println(e.getValue());

		System.out.println();
		System.out.println();
		
		return db;
	}
	
	private static String arrayListToString(List<String> list){
		StringBuilder sb = new StringBuilder();
		
		for (String s : list) {
		      sb.append(s).append(" ");
		} 		
		
		return sb.toString().trim();
	}

	private static List<String> initCommand(){
		List<String> command = new ArrayList<>();

		command.add("bash");
		command.add("-c");
		
		return command;
	}

	private static List<String> setCommitLogCommand(List<String> command, String s){
		List<String> gitCommand = new ArrayList<>();
		//git diff-tree --no-commit-id --name-only -r 0e07ac6b2cff63550d7df336355ca63cc05aa40b
		gitCommand.add("git");
		gitCommand.add("show");
		gitCommand.add("--name-only");
		gitCommand.add("--pretty='format:'");
		gitCommand.add(s);
		
		command.add(arrayListToString(gitCommand));
		
		return command;
	}

	private static List<String> setLineCountCommand(List<String> command, String path){
		List<String> gitCommand = new ArrayList<>();
//		path = pathNameProcessing(path);

		gitCommand.add("git");
		gitCommand.add("blame");
		gitCommand.add("'" + path + "'");
		gitCommand.add("|");
		gitCommand.add("wc");
		gitCommand.add("-l");
		
		command.add(arrayListToString(gitCommand));
		
		return command;
	}
	
	private static List<String> setAuthorListCommand(List<String> command){
		List<String> gitCommand = new ArrayList<>();

		//		gitLogCommand.add("git log --format='%aN' | sort -u");
		gitCommand.add("git");
		gitCommand.add("log");
		gitCommand.add("--format='%aN'");
		gitCommand.add("|");
		gitCommand.add("sort");
		gitCommand.add("-u");
		
		command.add(arrayListToString(gitCommand));
		
		return command;

	}
	
	private static List<String> setAuthorHistoryCommand(List<String> command, String path, int index){
		List<String> gitCommand = new ArrayList<>();
		path = pathNameProcessing(path);

		//	String command = "git log --follow '" + newPath + "' -L" + i + ":" + newPath + " | grep -E 'commit|Author:|Date:'";
		gitCommand.add("git");
		gitCommand.add("log");
		gitCommand.add("--follow");
		gitCommand.add("'" + path + "'");
		gitCommand.add("-L" + index + ":" + path);
		gitCommand.add("|");
		gitCommand.add("grep");
		gitCommand.add("-E");
		gitCommand.add("'commit|Author:|Date:'");
		
		command.add(arrayListToString(gitCommand));
		
		return command;
	}
	
	private static List<String> setFileHistoryCommand(List<String> command, String path){
		List<String> gitCommand = new ArrayList<>();
//		path = pathNameProcessing(path);
		
		//String command = "git log --follow -p --name-only --oneline '" + newPath + "' | grep -v '^.\\{7\\}\\s' | uniq";
		gitCommand.add("git");
		gitCommand.add("log");
		gitCommand.add("--follow");
		gitCommand.add("-p");
		gitCommand.add("--name-only");
		gitCommand.add("--oneline");
		gitCommand.add("'" + path + "'");
		gitCommand.add("|");
		gitCommand.add("grep");
		gitCommand.add("-v");
		gitCommand.add("'^.\\{7\\}\\s'");
		gitCommand.add("|");
		gitCommand.add("uniq");
		
		command.add(arrayListToString(gitCommand));
		
		return command;
	}
	
	private static String pathNameProcessing(String path){
		if(path.contains("$")){
			return path.replace("$", "\\$");
		}
		else
			return path;
	}
	
	
	public static void getLineCount(String path){
		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(path));

		try {
			for(Entry<Integer, String> e: sourcePath.entrySet()){
				String newPath = e.getValue().replaceFirst("/", "");

				List<String> command = initCommand();
//				List<String> command = new ArrayList<>();

//				gitLogCommand.add("git blame '" + newPath + "' | wc -l");
				
				command = setLineCountCommand(command, newPath);

				pb.command(command);

				pb.redirectErrorStream(true);

				Process process = pb.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				
				while( (line = reader.readLine()) != null){
					String temp = line.trim();
					
					System.out.println(temp);
					
					lineCount.put(e.getKey(), Integer.parseInt(temp));
				}

				process.waitFor();
				reader.close();
				process.destroy();		
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getAuthorList(String path, SQLiteDB db){
		DatabaseWriter dw = db.runDatabaseWriter();
		dw.createAuthorTable();

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(path));

		List<String> command = initCommand();
//		gitLogCommand.add("git log --format='%aN' | sort -u");

		command = setAuthorListCommand(command);
		
		pb.command(command);

		pb.redirectErrorStream(true);

		try {
			Process process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while( (line = reader.readLine()) != null){
				System.out.println(line);
				dw.insertAuthorTable(line);
			}

			System.out.println("Read all author information");
			process.waitFor();

			reader.close();
			process.destroy();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getAuthorHistory(String path, SQLiteDB db){

		DatabaseWriter dw = db.runDatabaseWriter();
		dw.createSourceLineAuthorHistoryTable();

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(path));

		try {

			for(Entry<Integer, String> e: sourcePath.entrySet()){
				String newPath = e.getValue().replaceFirst("/", "");

				int size = lineCount.get(e.getKey());
				
				//git log --follow src/main/java/org/joda/time/format/ISOPeriodFormat.java -L1:src/main/java/org/joda/time/format/ISOPeriodFormat.java | cat

				for(int i = 1; i < size; i++ ){
					List<String> command = initCommand();

//					String command = "git log --follow '" + newPath + "' -L" + i + ":" + newPath + " | grep -E 'commit|Author:|Date:'";
					command = setAuthorHistoryCommand(command, newPath, i);
					
//					gitLogCommand.add(command);

					pb.command(command);
					pb.redirectErrorStream(true);
					Process process = pb.start();

					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					String commit = null;
					String author = null;
					String date = null;
					boolean commitRead = false;

					while( (line = reader.readLine()) != null){
						if(line.contains("commit")){
							commit = line.replaceFirst("commit ", "").trim();
							commitRead = true;
						}
						if(commitRead && line.contains("Author:")){
							author = line.replaceFirst("Author: ", "").replaceFirst("<.*>", "").trim();
							commitRead = true;
						}	
						if(commitRead && line.contains("Date:")){
							date = line.replaceFirst("Date: ", "").trim();
							dw.insertSourceLineAuthorHistoryTable(e.getKey(), i, commit, author, date);
							commitRead = false;
						}	
					}
					process.waitFor();
					reader.close();
					process.destroy();
				}
			}
		} catch (IOException e11) {
			// TODO Auto-generated catch block
			e11.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}


	public static void getCommits(String path, SQLiteDB db){
		DatabaseWriter dw = db.runDatabaseWriter();
		DatabaseReader dr = db.runDatabaseReader();

		dw.createCommitTable();

		dr.getUniqueCommits();
		List<String> commitList = dr.getAllCommitList();

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(path));

		try {

			for(String s: commitList){

				List<String> command = initCommand();

				command = setCommitLogCommand(command, s);
//				command.add(s);

				pb.command(command);

				pb.redirectErrorStream(true);
				Process process = pb.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;

				while( (line = reader.readLine()) != null){
					if(line.endsWith(".java") && !line.contains("test") && !line.contains("example")){
						dw.insertCommitTable(s, line);
					}
				}
				process.waitFor();
				reader.close();
				process.destroy();
			}
		} catch (IOException e11) {
			// TODO Auto-generated catch block
			e11.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void getFileHistory(String path, SQLiteDB db){
		DatabaseWriter dw = db.runDatabaseWriter();
		dw.createFileHistoryTable();

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(path));

		try {

			for(Entry<Integer, String> e: sourcePath.entrySet()){
				String newPath = e.getValue().replaceFirst("/", "");

				List<String> command = initCommand();

//				String command = "git log --follow -p --name-only --oneline '" + newPath + "' | grep -v '^.\\{7\\}\\s' | uniq";
//				gitLogCommand.add(command);

				command = setFileHistoryCommand(command, newPath);
				
				pb.command(command);

				pb.redirectErrorStream(true);
				//					pb.inheritIO();
				Process process = pb.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				String now = null;
				boolean flag = true;

				while((line = reader.readLine()) != null){
					if(flag){
						now = line;
						flag = false;
					}
					else{
						dw.insertFileHistoryTable(line, now);
					}
				}
				process.waitFor();
				reader.close();
				process.destroy();
			}
		} catch (IOException e11) {
			// TODO Auto-generated catch block
			e11.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
