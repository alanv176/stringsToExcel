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
			
			generateXls(route);
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
        		lineToAdd.replaceAll("\\", "");
    		    cell.setCellValue(lineToAdd);
    		    
    		    /*
    		     * Sets row height to accommodate for multiple lines
    		     * Generally setWrapText(true) is enough, though there is a bug in libreOffice that requires this type of manual height setting
    		     */
//    		    if(lineToAdd.length() > 70){
//                	row.setHeight((short)(row.getHeight()*(lineToAdd.length() / 70 + 1)));
//                }
    		    
    		    CellStyle style = workbook.createCellStyle();
                style.setWrapText(true);
                style.setFont(font);
                cell.setCellStyle(style);
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
}
