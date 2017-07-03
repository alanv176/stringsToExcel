package com.whatever.app;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Updater extends Generator{
    
	public static void generateXls(String route){
		/*
		 * Find first line of source file
		 * Find first line of excel file to update that matches first line of source file
		 * Loop through each file line by line checking if the lines match
		 * If they don't, insert new cell in excel containing current line from source
		 * Save updated excel file
		 */
		
		Path sourceFile = Paths.get(route);
		Path fileToUpdate;
		
		try(Scanner s = new Scanner(System.in)){
			System.out.println("Input path to file you wish to update");
            fileToUpdate = Paths.get(s.nextLine());
		}
		catch(Exception e) {
			System.out.println("There was a problem retrieving file to update");
			System.out.println(e);
		}
		
		
		
	}
}
