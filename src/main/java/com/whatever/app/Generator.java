package com.whatever.app;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * TODO: Make getSubstring optional
 *       Find any string in a file
 *           ['\"].*['\"] should work for single lines since greedy * will get to eol and then work back to last ' or "
 */

public class Generator {
	
	public static void saveDefaultRoute(String sourceRoute){
		sourceRoute = sourceRoute.replaceFirst("^~", System.getProperty("user.home"));
		try{
			System.out.println("Would you like to save this as the default route?(y/n)");
			if(Utils.s.nextLine().equals("y")){
				try(FileOutputStream output = new FileOutputStream("default.properties")){
					Properties prop = Utils.getDefaults();
					
					prop.setProperty("source", sourceRoute);
					prop.store(output, null);	
				}
				catch(Exception e){
					System.out.println("Problem creating default properties file");
				}
			}
		}
		catch(Exception e){
			System.out.println("Problem in Generator.saveDefaultRoute");
			System.out.println(e);
	    }
		finally{
			generateXls(sourceRoute);
		}
	}
	
	public static void useDefaultRoute(){
		String sourceRoute;
		try{
			Properties prop = Utils.getDefaults();

			sourceRoute = prop.getProperty("source");
			if(sourceRoute == null){
			    throw new IllegalArgumentException("Default can't be null");
			}
			Path path = Paths.get(sourceRoute);
			if(!Files.isRegularFile(path)){
				System.out.println("The default path does not lead to a valid file");
				throw new IllegalArgumentException("The file does not exist");
			}
			
			generateXls(sourceRoute);
		}
		catch(Exception err){
			System.out.println("No defaults set, please provide a route to source file:");
			try{
				sourceRoute = Utils.s.nextLine();
				saveDefaultRoute(sourceRoute);
			}
			catch(Exception e){
				System.out.println("There was a problem with the route input");
				System.exit(0);
			}
		}
	}
	
	public static void copyFile(String sourceRoute){
		try{
			Path source = Paths.get(sourceRoute);

			System.out.println("Copying file");
			Files.copy(source, Utils.tempFile, StandardCopyOption.REPLACE_EXISTING);

		}
		catch(Exception e){
			System.out.println("Problem copying file");
	        System.out.println(e);
			System.exit(0);
		}
	}
	
	public static void getSubstring(){
		try(Scanner contentScanner = new Scanner(Utils.tempFile, "UTF-8").useDelimiter("\\Z")){
			String content = contentScanner.next();
			System.out.println("Substring obtained");
			Pattern pattern = Pattern.compile("(?<LangEN>Lang_EN\\s?:\\s?\\{.*?\\})", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(content);

			if(matcher.find()){
				System.out.println("Lang_EN found");
				String LangEN = matcher.group("LangEN");
				Files.write(Utils.tempFile, LangEN.getBytes());
			}
			else{
				System.out.println("Substring not found");
				System.exit(0);
			}
		}
		catch(Exception e){
			System.out.println("Problem writing substring to file");
			System.out.println(e);
			System.exit(0);
		}

	}
	
    public static void generateXls(String sourceRoute){
    	copyFile(sourceRoute);
    	getSubstring();
    	String createdFileName = createFileName("xlsx");
    	
    	try(FileOutputStream outputStream = new FileOutputStream(Paths.get("./" + createdFileName).toFile());
    		Scanner language = new Scanner(Utils.tempFile, "UTF-8");
    		XSSFWorkbook workbook = new XSSFWorkbook();	){
    		
    		Pattern comment = Pattern.compile("^\\s*?#(.*$)");
        	Pattern content = Pattern.compile(":\\s?['\"](.*)['\"]$");
        	Pattern multiLine = Pattern.compile(":\\s?['\"](.*?)[^'\"]$");
        	
        	int colWidth = 255*70; //70 characters(each unit is 1/255 of a character)
        	
        	
            XSSFSheet sheet = workbook.createSheet("AppTranslation");
            sheet.setColumnWidth(0, colWidth);
            sheet.setColumnWidth(1, colWidth);
            int rowCount = 0;
        	
        	while(language.hasNextLine()){
        		//System.out.println("Creating sheet, at row " + rowCount);
        		String currLine = language.nextLine();
        		Matcher commentMatcher = comment.matcher(currLine);
        		Matcher singleLineMatcher = content.matcher(currLine);
        		Matcher multiLineMatcher = multiLine.matcher(currLine);
        		
        		String lineToAdd;
        		XSSFFont font= workbook.createFont();
        		
        		if(commentMatcher.find()){
        			rowCount++;
        			lineToAdd = commentMatcher.group(1);
        			
        			font.setBold(true);	
        		}
        		else if(singleLineMatcher.find()){
        			lineToAdd = singleLineMatcher.group(1);
        		}
        		else if(multiLineMatcher.find()){
    				System.out.println("doing multi, at row " + rowCount);
    				lineToAdd = multiLineMatcher.group(1) + doMultiLine(language);
        		}
        		else{
        			//System.out.println("The Matcher didn't find anything on this line");
        			continue;
        		}
        		
        		Row row = sheet.createRow(++rowCount);
        		Cell cell = row.createCell(0);
        		lineToAdd = lineToAdd.replace("\\", "");
    		    cell.setCellValue(lineToAdd);
    		    
    		    /*
    		     * Sets row height to accommodate for multiple lines
    		     * Generally setWrapText(true) is enough, though there is a bug in libreOffice that requires this type of manual height setting
    		     */
    		    if(lineToAdd.length() > 70){
                	row.setHeight((short)(row.getHeight()+(lineToAdd.length() * 4 - lineToAdd.length() % 70)));
                }
    		    
    		    CellStyle style = workbook.createCellStyle();
                style.setWrapText(true);
                style.setFont(font);
                cell.setCellStyle(style);
        	}
        	
            workbook.write(outputStream);
        			
        	Files.delete(Utils.tempFile);
        	System.out.println("Completed successfully");
    	} 	
    	catch(Exception e){
    		System.out.println("Problem in generateXls");
    		System.out.println(e);
    		System.exit(0);
    	}
    }
    
    public static String doMultiLine(Scanner s){
    	//System.out.println("Doing multiline");
    	String currLine = s.nextLine();
    	String fullLine = "";
    	
    	Pattern theEnd = Pattern.compile("\\s*(\\s.+)['\"]$");	
    	Matcher theEndMatcher = theEnd.matcher(currLine);
    	
    	while(!theEndMatcher.find()){
    		fullLine += " " + currLine.trim();
    		currLine = s.nextLine();
    		theEndMatcher = theEnd.matcher(currLine);
    	}
    	
    	fullLine += theEndMatcher.group(1);
    	
    	return fullLine;
    }
    
    /**
     * Takes user input for file name, ensures it's valid and then adds extension if none is provided by the user
     * @param extension the extension the file should have
     * @return
     */
    public static String createFileName(String extension){
    	try{
    		System.out.println("What would you like to name the new file?");
    		String fileName = Utils.s.nextLine();
    		
    		Pattern validateFile = Pattern.compile("[^-_.A-Za-z0-9]");
    		Matcher isNotValid = validateFile.matcher(fileName);
    		Pattern fileExtension = Pattern.compile("\\." + extension + "$");
    		Matcher hasExtension = fileExtension.matcher(fileName);
    		
    		while(isNotValid.find()){
    			System.out.println("Please enter a valid filename");
    			fileName = Utils.s.nextLine();
    			hasExtension = fileExtension.matcher(fileName);
    		}
    		
    		if(!hasExtension.find()){
    			fileName = fileName + "." + extension;
    		}
    		
    		System.out.println("Filename: " + fileName);
    		return fileName;
    	}
    	catch(Exception e){
    		System.out.println("Trouble getting name of file to create");
    		System.out.println(e);
    		System.exit(0);
    	}
    	return "default." + extension;
    }
    
    /**
     * For use with the express command
     * This method accepts all arguments needed when the jar is called
     * This makes it easy to add the programs functionality to a script and not needing to
     *   stop for user input
     *   
     * @param sourceFileRoute path to the source file from which to extract the strings
     * @param pathToNewFile path to the file which will be created
     */
    
    public static void generateXls(String sourceFileRoute, String pathToNewFile){
    	copyFile(sourceFileRoute);
    	getSubstring();
    	pathToNewFile = pathToNewFile.replaceFirst("~", System.getProperty("user.home"));
    	
    	try(FileOutputStream outputStream = new FileOutputStream(Paths.get(pathToNewFile).toFile());
    		Scanner language = new Scanner(Utils.tempFile, "UTF-8");
    		XSSFWorkbook workbook = new XSSFWorkbook();){
    		
    		Pattern comment = Pattern.compile("^\\s*?#(.*$)");
        	Pattern content = Pattern.compile(":\\s?['\"](.*)['\"]$");
        	Pattern multiLine = Pattern.compile(":\\s?['\"](.*?)[^'\"]$");
        	
        	int colWidth = 255*70; //70 characters(each unit is 1/255 of a character)
        	
        	
            XSSFSheet sheet = workbook.createSheet("AppTranslation");
            sheet.setColumnWidth(0, colWidth);
            sheet.setColumnWidth(1, colWidth);
            int rowCount = 0;
        	
        	while(language.hasNextLine()){
        		//System.out.println("Creating sheet, at row " + rowCount);
        		String currLine = language.nextLine();
        		Matcher commentMatcher = comment.matcher(currLine);
        		Matcher singleLineMatcher = content.matcher(currLine);
        		Matcher multiLineMatcher = multiLine.matcher(currLine);
        		
        		String lineToAdd;
        		XSSFFont font= workbook.createFont();
        		
        		if(commentMatcher.find()){
        			rowCount++;
        			lineToAdd = commentMatcher.group(1);
        			
        			font.setBold(true);	
        		}
        		else if(singleLineMatcher.find()){
        			lineToAdd = singleLineMatcher.group(1);
        		}
        		else if(multiLineMatcher.find()){
    				System.out.println("doing multi, at row " + rowCount);
    				lineToAdd = multiLineMatcher.group(1) + doMultiLine(language);
        		}
        		else{
        			//System.out.println("The Matcher didn't find anything on this line");
        			continue;
        		}
        		
        		Row row = sheet.createRow(++rowCount);
        		Cell cell = row.createCell(0);
        		lineToAdd = lineToAdd.replace("\\", "");
    		    cell.setCellValue(lineToAdd);
    		    
    		    /*
    		     * Sets row height to accommodate for multiple lines
    		     * Generally setWrapText(true) is enough, though there is a bug in libreOffice that requires this type of manual height setting
    		     */
    		    if(lineToAdd.length() > 70){
                	row.setHeight((short)(row.getHeight()+(lineToAdd.length() * 4 - lineToAdd.length() % 70)));
                }
    		    
    		    CellStyle style = workbook.createCellStyle();
                style.setWrapText(true);
                style.setFont(font);
                cell.setCellStyle(style);
        	}
        	
            workbook.write(outputStream);
        			
        	Files.delete(Utils.tempFile);
        	System.out.println("Completed successfully");
    	} 	
    	catch(Exception e){
    		System.out.println("Problem in generateXls");
    		System.out.println(e);
    		System.exit(0);
    	}
    }
}
