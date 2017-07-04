package com.whatever.app;

/*
 * Automate collection of commands to display when help is called
 */

public class Helper {
	
	// generate, update, insert, setDefault, reset, expressGen, help
	public enum Commands {
		GENERATE, UPDATE, INSERT, SETDEFAULT, RESET, EXPRESSGEN, HELP
	}
    
	/**
	 * Lists commands that can be used
	 */
	public static void displayHelp(){
		System.out.println("Currently supported commands:");
		for(Commands c : Commands.values()){
			System.out.println("  " + c.toString().toLowerCase());
		}
		
		System.out.println("\nUse \"help [command]\" for more detailed help for a command");
		//TODO: Link to java docs
	}
	
	/**
	 * Lists more detailed help for a specific command
	 */
    public static void displayHelp(String command){
    	
    	Commands cmd = Commands.valueOf(command.toUpperCase());
    	System.out.println("");
    	
    	switch(cmd){
    	case GENERATE:
    		System.out.println("generate");
    		System.out.println("  Generate will extract strings from a source file and create an excel file with them.");
    		System.out.println("  It will try to find a path to a source file using it's defaults.");
    		System.out.println("  If it is unable to find any the user will be prompted to specify a path.");
    		System.out.println("  This path may then be saved as the default source path for later use.");
    		System.out.println("  The user will then be prompted to specify a name for the file to be generated.");
    		System.out.println("  This file will be made in the current directory");
    		
    		System.out.println("");
    		
    		System.out.println("generate [path to source]");
    		System.out.println("  If a path to the source is specified the check for a default will be "
    				+ "skipped and the program will proceed as if generate had been called alone.");
    		
    		System.out.println("");
    		
    		System.out.println("  If you would like to specify both the source and destination "
    				+ "from the command line and skip all user input please use 'expressGen'");
    		break;
    	case UPDATE:
    		break;
    	case INSERT:
    		break;
    	case SETDEFAULT:
    		break;
    	case RESET:
    		break;
    	case EXPRESSGEN:
    		break;
    	case HELP:
    		break;
    	default:
    		System.out.println("Unknown command, use \"help\" for a list of commands");
    	}
    	
    }
}
