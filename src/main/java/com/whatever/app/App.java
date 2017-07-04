package com.whatever.app;

import java.nio.file.Files;

/*
 * TODO: Make this or another file that updates existing excel sheets
 *       Set defaults command that sets all defaults
 *           Read properties, if blank keep property otherwise override
 *       Use a file chooser instead of requiring user to manually type out path
 *       Print useful error messages and errors for each catch
 *       Comment code
 *       Allow for doing multiple files at once
 *       Ask to use default or specify new path
 *       Account for special symbols e.g euro
 */
public class App 
{
	public static void main( String[] args ){
		switch(args[0]){
		case "generate":
			System.out.println("");
			try{
				if(args[1] != null){
					Generator.saveDefaultRoute(args[1]);
				}
			}
			catch(Exception e){
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
			System.out.println("");
			break;
			
		default:
			System.out.println("Command not found, use \"help\" for a list of commands");
		}
	}
}