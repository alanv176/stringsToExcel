package com.whatever.app;

import java.io.FileInputStream;
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

public class Generator {
	
	private Path dest = Paths.get("./tempFile.txt");
	
	public void saveDefaultRoute(String route){
		route = route.replaceFirst("^~", System.getProperty("user.home"));
		try(Scanner s = new Scanner(System.in)){
			System.out.println("Would you like to save this as the default route?(y/n)");
			if(s.nextLine().equals("y")){
				try(FileOutputStream output = new FileOutputStream("default.properties")){
					System.out.println("Creating default.properties");
					Properties prop = new Properties();
					
					prop.setProperty("source", route);
					prop.store(output, null);	
				}
				catch(Exception e){
					System.out.println("Problem creating file");
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
	    }
		finally{
			generateXls(route);
		}
	}
	
	public void useDefaultRoute(){
		String route;
		try(FileInputStream input = new FileInputStream("default.properties")){
			Properties prop = new Properties();

			prop.load(input);

			route = prop.getProperty("source");
		}
		catch(Exception err){
			System.out.println("Please provide a route to source file:");
			try(Scanner s = new Scanner(System.in)){
				route = s.nextLine();
				saveDefaultRoute(route);
			}
			catch(Exception e){
				System.out.println("There was a problem with the route input");
				System.exit(0);
			}
		}
	}
	
	public void getFile(String route){
		try{
			Path source = Paths.get(route);

			System.out.println("Copying file");
			Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);

		}
		catch(Exception e){
			System.out.println("Problem copying file");
	        System.out.println(e);
			System.exit(0);
		}
	}
	
	public void getSubstring(){
		try(Scanner contentScanner = new Scanner(dest).useDelimiter("\\Z")){
			String content = contentScanner.next();
			System.out.println("Substring obtained");
			Pattern pattern = Pattern.compile("(?<LangEN>Lang_EN\\s?:\\s?\\{.*?\\})", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(content);

			if(matcher.find()){
				System.out.println("Lang_EN found");
				String LangEN = matcher.group("LangEN");
				Files.write(dest, LangEN.getBytes());
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
	
    public void generateXls(String route){
    	getFile(route);
    	getSubstring();
    	
    	try(FileOutputStream outputStream = new FileOutputStream(Paths.get("./blankTranslation.xlsx").toFile());
    		Scanner language = new Scanner(dest);
    		XSSFWorkbook workbook = new XSSFWorkbook();	){
    		
    		Pattern comment = Pattern.compile("^\\s*?#(.*$)");
        	Pattern singleQuote = Pattern.compile(":\\s?'(.*)'$");
        	Pattern doubleQuote = Pattern.compile(":\\s?\"(.*)\"$");
        	Pattern multiLine = Pattern.compile(":\\s?['\"](.*?)[\\w\\.\\?]$");
        	
        	String currLine;
        	Matcher commentMatcher;
        	Matcher singleQuoteMatcher;
        	Matcher doubleQuoteMatcher;
        	Matcher multiLineMatcher;
        	
        	int colWidth = 255*70; //70 characters(each unit is 1/255 of a character)
        	
        	
            XSSFSheet sheet = workbook.createSheet("AppTranslation");
            sheet.setColumnWidth(0, colWidth);
            sheet.setColumnWidth(1, colWidth);
            int rowCount = 0;
        	
        	while(language.hasNextLine()){
        		//System.out.println("Creating sheet, at row " + rowCount);
        		currLine = language.nextLine();
        		commentMatcher = comment.matcher(currLine);
        		singleQuoteMatcher = singleQuote.matcher(currLine);
        		doubleQuoteMatcher = doubleQuote.matcher(currLine);
        		multiLineMatcher = multiLine.matcher(currLine);
        		
        		
        		if(commentMatcher.find()){
        			rowCount += 2;
        			Row row = sheet.createRow(rowCount);
        			Cell cell = row.createCell(0);
        		    cell.setCellValue(commentMatcher.group(1));
        			
        			CellStyle style = workbook.createCellStyle();
        			XSSFFont font= workbook.createFont();
        			font.setBold(true);
        			style.setFont(font);
                    cell.setCellStyle(style);
        		}
        		else if(singleQuoteMatcher.find()){
        			Row row = sheet.createRow(++rowCount);
        			Cell cell = row.createCell(0);
        			if(multiLineMatcher.find()){
        				System.out.println("Doing multi, at row " + rowCount);
            			cell.setCellValue(singleQuoteMatcher.group(1) + doMultiLine(language));
            		}
        			else{
        				cell.setCellValue(singleQuoteMatcher.group(1));
        			}
        			
        			CellStyle style = workbook.createCellStyle();
                    style.setWrapText(true);
                    if(currLine.length() > 70){
                    	row.setHeight((short)(row.getHeight()*2));
                    }
                    cell.setCellStyle(style);
        		}
        		else if(doubleQuoteMatcher.find()){
        			Row row = sheet.createRow(++rowCount);
        			Cell cell = row.createCell(0);
        			if(multiLineMatcher.find()){
        				System.out.println("doing multi, at row " + rowCount);
        				cell.setCellValue(doubleQuoteMatcher.group(1) + doMultiLine(language));
            		}
        			else{
        				cell.setCellValue(doubleQuoteMatcher.group(1));
        			}
        			
        			CellStyle style = workbook.createCellStyle();
                    style.setWrapText(true);
                    cell.setCellStyle(style);
        		}
//        		else{
//        			System.out.println("The Matcher didn't find anything on this line");
//        		}
        	}
        	
            workbook.write(outputStream);
        			
        	Files.delete(dest);
        	System.out.println("Completed successfully");
    	}
    	
    	catch(Exception e){
    		System.out.println("Problem in generateXls");
    		System.exit(0);
    	}
    }
    
    public String doMultiLine(Scanner s){
    	//System.out.println("Doing multiline");
    	String currLine = s.nextLine();
    	String fullLine = "";
    	
    	Pattern theEnd = Pattern.compile("(.+)['\"]$");	
    	Matcher theEndMatcher = theEnd.matcher(currLine);
    	
    	while(!theEndMatcher.find()){
    		fullLine += currLine;
    		currLine = s.nextLine();
    		theEndMatcher = theEnd.matcher(currLine);
    	}
    	
    	fullLine += theEndMatcher.group(0);
    	
    	return fullLine;
    }
}
