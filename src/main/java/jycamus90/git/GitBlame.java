package jycamus90.git;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameGenerator;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import jycamus90.db.DatabaseReader;
import jycamus90.db.DatabaseWriter;
import jycamus90.db.SQLiteDB;

public class GitBlame {

	Map<String, Integer> source = new HashMap<>();

	public void init(String dbPath, String projectPath){
		
		//args[0] = path to main directory
		// /Users/Ku/Documents/uci/research/joda-time/src/main/java

		//args[1] = path to db
		// /Users/Ku/Documents/uci/research/tacoco/joda/joda-err1/joda-time-err1.db

		SQLiteDB db = setUpDB(dbPath);
		getAuthorship(projectPath, db);

	}

	public SQLiteDB setUpDB(String dbPath){
		SQLiteDB db = new SQLiteDB();
		db.openConnection(dbPath);

		DatabaseReader dr = db.runDatabaseReader();
		dr.getSource();
		source = dr.getSourceMap();

		return db;

	}

//	public static void setUpProcess(String path){
//		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
//
//		Repository repository;
//		try {
//			repository = repositoryBuilder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
//			System.out.println("Having repository: " + repository.getDirectory());
//
////			ObjectId commitId = repository.re
//			
////			BlameGenerator bg = new BlameGenerator(repository, "src/main/java/org/joda/time/tz/DateTimeZoneBuilder.java");
////			System.out.println(bg.getSourceAuthor());
//			
////			Ref head = repository.getRef("HEAD");
////			System.out.println(repository.getTags());
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}


	public void getAuthorship(String path, SQLiteDB db){

		DatabaseWriter dw = db.runDatabaseWriter();
//		dw.createSourceLineAuthorTable();

		try {
			Git git = Git.open(new File(path));
			BlameResult blameResult;
			
			for(Entry<String, Integer> e: source.entrySet()){
				String newPath = "src/main/java/" + e.getKey().replaceAll(".java", "").replaceAll("\\.", "/") + ".java";
				blameResult = git.blame().setFilePath(newPath).call();

				int size = blameResult.getResultContents().size();

				for(int i = 0; i < size; i++ ){

//					dw.insertSourceLineAuthorTable(e.getValue(), i+1, 
//												blameResult.getSourceAuthor(i).getName(), blameResult.getSourceAuthor(i).getWhen().toString());
				}

			}

			git.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
//		catch (ParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	}



}
