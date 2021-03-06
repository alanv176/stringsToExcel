package com.whatever.app;

import java.nio.file.Files;

/*
 * TODO: Make this or another file that updates existing excel sheets
 *       Use a file chooser instead of requiring user to manually type out path
 *       Print useful error messages and errors for each catch
 *       Comment code, javadocs
 *       Allow for doing multiple files at once
 *       Ask to use default or specify new path
 *       Account for special symbols e.g euro
 *       If no destination path use current directory
 *         Separate destination path and naming to allow destination specification
 *         possible default destination folder
 *       Should probably not let nonsense get through to saveDefaultRoute, do check in main or at start of method
 *       Account for arrays in generator
 *       Loop through commands
 *           For each command, loop through additional arguments and handle appropriately
 *       Take URLs as input file
 *       Refactor so methods throw exceptions rather than internal handling
 */
public class App 
{
	public static void main( String[] args ){
		try {
			switch(args[0]){
			case "generate":
				System.out.println("");
				try{
					if(args[1] != null){
						System.out.println("Using specified path");
						Generator.saveDefaultRoute(args[1]);
					}
				}
				catch(Exception e){
					System.out.println("No source specified, trying default");
					Generator.useDefaultRoute();
				}
				break;

			case "update":
				try{
					if(args[1] != null){
						Updater.saveDefaultRoute(args[1]);
					}
				}
				catch(Exception e){
					System.out.println("No source specified, trying default");
					Updater.useDefaultRoute();
				}
				System.out.println("");
				break;

			case "insert":
				System.out.println("");
				break;

			case "setDefault":
				Utils.setDefaults();
				break;

			case "reset":
				try{
					Files.delete(Utils.defaultFile);
					System.out.println("Defaults removed");
				}
				catch(Exception e){
					System.out.println("Defaults did not exist in the first place");
				}
				break;

			case "expressGen":
				try{
					Generator.generateXls(args[1], args[2]);
				}
				catch(Exception e){
					System.out.println("Illegal arguments for expressGen, must specify source and path to created file. Use help expressGen for more details");
				}

			case "help":
				try{
					Helper.displayHelp(args[1]);
				}
				catch(Exception e){
					Helper.displayHelp();
				}
				break;

			default:
				System.out.println("Command not found, use \"help\" for a list of commands");
			}
		}
		catch(NullPointerException e){
			System.out.println("No command specified, use \"help\" for a list of commands");
		}
	}
}