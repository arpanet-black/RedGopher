package black.arpanet.util.text;

import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArpanetStringUtil {
	
	private static final String[] WHITESPACE_CHARS = new String[]{" ", "\n", "\t"};
	private static final String[] BREAK_ON_CHARS = new String[]{"-", ".", ",",":",";","?","!"};
	
	private static final Logger LOG = LogManager.getLogger(ArpanetStringUtil.class);	

	public static String breakLines(String str, int len, String joinStr) {
		
		StringReader sr = new StringReader(str);
		char[] chBuff = new char[len];
		int numChars = 0;
		int totalChars = 0;
		List<String> lines = new ArrayList<String>((str.length()/len) + 1);
		try {
			while((numChars = sr.read(chBuff)) > -1) {
				totalChars += numChars;
				String line = (new String(chBuff)).substring(0,numChars);
				lines.add(line);				
			}
		} catch (IOException ex) {
			w(LOG, String.format("Exception near character %s breaking string into multiple lines: %s", totalChars, str),ex);
		}
		
		for(String s2 : lines) {
			System.out.println("LINE LENGTH: " + s2.length() + "<<" + s2 + ">>");
		}
		
		String[] wordBreakFixed = fixWordBreaks(lines.toArray(new String[]{}), len, joinStr);
		
		return String.join(joinStr, wordBreakFixed).trim();
	}
	
	private static String[] fixWordBreaks(String[] lines, int len, String joinStr) {
		
		List<String> fixed = new ArrayList<String>();
		
		for(int i = 0; i < lines.length; i++) {
			
			//If this is the last line
			//then it must be checked for length
			if(i == lines.length - 1) {
				if(lines[i].length() > len) {
					//Recursively break the line
					fixed.add(breakLines(lines[i], len, joinStr));
				} else {
					//Else, done!
					fixed.add(lines[i]);
				}
			} else { 

				String line = lines[i];
				String nextLine = lines[i + 1];

				if(line.length() <= len && lineEndIsValid(line) && lineStartIsValid(nextLine)) {
					fixed.add(line); //Add this line since it is fine
					continue;
				} else {
					String fullLine = line + nextLine;
					String[] twoLines = breakOnWord(fullLine, len);
					fixed.add(twoLines[0]); //Add the properly broken one
					lines[i+1] = twoLines[1]; //update the next line for breaking
					continue;
				}
			}
		}
		
		return fixed.toArray(new String[]{});
		
	}


	private static String[] breakOnWord(String fullLine, int len) {

		String[] twoLines = new String[2];
		
		//Search from the middle for a valid word break.
		//Since the last break was on the max character boundry (len)
		//we will search backwards for a valid break char
		for(int i = len-1; i > -1; i--) {
			//If we have a valid break char
			//then this will be the end of the first line
			String currChar = String.valueOf(fullLine.charAt(i));
			if(ArrayUtils.contains(WHITESPACE_CHARS, currChar) || ArrayUtils.contains(BREAK_ON_CHARS, currChar)) {
				
				//Break after the actual character = i+1
				twoLines[0] = fullLine.substring(0, i+1);
				twoLines[1] = fullLine.substring(i+1);
				
				break;
			}
		}
		
		return twoLines;
	}

	private static boolean lineStartIsValid(String line) {
		boolean valid = !StringUtils.startsWithAny(line, BREAK_ON_CHARS);
		return valid;
	}


	private static boolean lineEndIsValid(String line) {
		boolean valid = StringUtils.endsWithAny(line, WHITESPACE_CHARS);
		valid = valid || StringUtils.endsWithAny(line, BREAK_ON_CHARS);
		return valid;
	}
}
