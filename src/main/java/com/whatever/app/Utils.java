package com.whatever.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

public class Utils {
	public static final Scanner s = new Scanner(System.in);
	public static final Path tempFile = Paths.get("./tempFile.txt");
	public static final Path defaultFile = Paths.get("./default.properties");
	
	public static void setDefaults(){
		try(FileOutputStream output = new FileOutputStream("default.properties")){
			Properties defaults = getDefaults();
			
			System.out.println("Enter default route for source file:");
			defaults.setProperty("source", s.nextLine());
			
			defaults.store(output, null);
		}
		catch(Exception e){
			System.out.println("Problem setting defaults");
			System.out.println(e);
			System.exit(0);
		}
		
	}
	
	public static Properties getDefaults(){
		try(FileInputStream input = new FileInputStream("default.properties")){
			Properties prop = new Properties();
			prop.load(input);
			return prop;
		}
		catch(Exception e){
			try(FileOutputStream output = new FileOutputStream("default.properties")){
				Properties prop = new Properties();
				prop.store(output, null);
				return prop;
			}
			catch(Exception err){
				System.out.println("Problem creating default.properties");
				System.out.println(err);
			}
		}
		return new Properties();
	}
}
