package com.whatever.app;

/**
 * TODO: Account for multiline strings
 *       Fix row height for long strings
 *       Make this or another file that updates existing excel sheets
 *       Set defaults command that sets all defaults
 *           Read properties, if blank keep property otherwise override
 *       Try to replac single and double quote with one regex ['\"](.*)['\"]$
 */
public class App 
{
	public static void main( String[] args ){
		String route = null;
		Generator generator = new Generator();

		switch(args[0]){
		case "generate":
			System.out.println("");
			try{
				if(args[1] != null){
					generator.saveDefaultRoute(route);
				}
			}
			catch(Exception e){
				generator.useDefaultRoute();
			}
			break;
		case "update":
			System.out.println("");
			break;
		case "insert":
			System.out.println("");
			break;
		case "help":
			System.out.println("");
			break;
		default:
			System.out.println("Command not found, use help for a list of commands");
		}
	}
}