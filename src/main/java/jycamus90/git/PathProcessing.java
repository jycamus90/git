package jycamus90.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jycamus90.db.DatabaseReader;
import jycamus90.db.DatabaseWriter;
import jycamus90.db.SQLiteDB;



public class PathProcessing {

	private File root;
	private Map<String, Integer> source = new HashMap<>();
	private DatabaseWriter dw;

	public void start(String dbPath) {
		// TODO Auto-generated method stub
		SQLiteDB db = setUpDB(dbPath);
		
		dw = db.runDatabaseWriter();
		dw.clean();

		dw.createSourcePathTable();
		
		for(Entry<String, Integer> e: source.entrySet())
				search(this.getRoot(), e.getKey());
	}

	public PathProcessing(String root){
		this.root = new File(root);
	}

	private SQLiteDB setUpDB(String dbPath){
		SQLiteDB db = new SQLiteDB();
		db.openConnection(dbPath);

		DatabaseReader dr = db.runDatabaseReader();
		dr.getSource();
		source = dr.getSourceMap();

		return db;
	}

	private File getRoot(){
		return root;
	}

	private void search(File folder, String sourceName){

		final String name = sourceName.replaceAll(".java", "");
		final String[] names = name.split("\\.");

		for(File file : folder.listFiles()){

			if(file.isDirectory()){
				search(file, sourceName);
			}
			else if(file.isFile() && 
					file.getAbsolutePath().contains("src") &&
					file.getName().endsWith(".java") && 
					file.getName().equals(names[names.length - 1].concat(".java"))){

				String path = file.getAbsolutePath();

				boolean flag = true;

				for(String s : names){
					if(!path.contains(s) & flag)
						flag = false;
				}

				if(flag){
//					System.out.println(file.getAbsolutePath().replaceFirst(root.getAbsolutePath(), ""));
//					System.out.println(source.get(sourceName));
					dw.insertSourcePathTable(source.get(sourceName), file.getAbsolutePath().replaceFirst(root.getAbsolutePath(), ""));
				}

			}
		}
	}

}
