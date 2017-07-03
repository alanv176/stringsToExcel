package com.whatever.app;

/*
 * TODO: Make this or another file that updates existing excel sheets
 *       Set defaults command that sets all defaults
 *           Read properties, if blank keep property otherwise override
 *       Use a file chooser instead of requiring user to manually type out path
 *       Print useful error messages and errors for each catch
 *       Comment code
 *       Rename variables
 *       Fix row height option for libre office
 */
public class App 
{
	public static void main( String[] args ){
		String sourceRoute = null;

		switch(args[0]){
		case "generate":
			System.out.println("");
			try{
				if(args[1] != null){
					Generator.saveDefaultRoute(sourceRoute);
				}
			}
			catch(Exception e){
				Generator.useDefaultRoute();
			}
			break;
			
		case "update":
			try{
				if(args[1] != null){
					Updater.saveDefaultRoute(sourceRoute);
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
			
		case "help":
			System.out.println("");
			break;
			
		default:
			System.out.println("Command not found, use \"help\" for a list of commands");
		}
	}
}