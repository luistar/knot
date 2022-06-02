package org.unina.spatialanalysis.mapmatcher.osmfilereader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.unina.spatialanalysis.mapmatcher.entity.osm.Node;
import org.unina.spatialanalysis.mapmatcher.entity.osm.OsmDataManager;
import org.unina.spatialanalysis.mapmatcher.entity.osm.Way;

/**
 * A basic file reader for an OSM file treaded as a simple text file. 
 * The file creates an @see OsmDataManager which is then used as a reference
 * for the rest of the Analyzer.
 * This OsmFileReader ensures that by the end of the file parsing:
 * 1. For every Node contained in the file an instance of Node will be created.
 * 2  For every Way:
 * 		1. An instance of way will be created;
 * 		2. Each pair key/value associated to the created Way will be contained in 
 * 		   the created Way tags map.
 * 		3. Every object Node that is referenced by the created Way will contain a reference 
 * 		   to the created Way.
 * @author sinog
 *
 */
public class OsmFileReader {
		
	private static final int EQUAL_SIGN =61;
	private static final int WHITESPACE = 32;
	private static final int SLASH = 47;
	private static final int QUOTES = 34;
	
	File osmFile;
	
	public OsmFileReader(File osmFile) {
		super();
		this.osmFile = osmFile;
	}
	
	public OsmDataManager readOsmFile() throws IOException {
		Scanner myScanner = new Scanner(osmFile, "UTF-8");
		myScanner.useDelimiter(">");
		String currentLine;
		OsmDataManager res = new OsmDataManager();
		Node currentNode;
		Way currentWay = null;
		List<Long> nodesInAWay = new ArrayList<Long>();
		
		boolean foundHighway = false;
		
		int i = 0;
		int nodes = 0;
		int ways = 0;
		int nd = 0;
		int tagsNumber = 0;
		int numberOfNdListCreated = 0;
		boolean check = false;
		
		LocalDateTime  begin = LocalDateTime.now();
		
		while(myScanner.hasNext()) {
			currentLine=myScanner.next();
			if(currentLine.contains("<node") && !currentLine.contains("</node")) {
				currentNode = parseNode(currentLine);
				res.addNode(currentNode);
				nodes++;
			}else if(currentLine.contains("<way") && !currentLine.contains("</way")) {
				ways++;
				currentWay = parseWay(currentLine);
				check = true;
			}else if(currentLine.contains("<tag") && currentWay!=null) {
				String [] tags = parseTag(currentLine);
				if(tags[0].equals("highway")) {
					foundHighway=true;
				}
				currentWay.addTags(tags[0], tags[1]);
				tagsNumber++;
			}else if(currentLine.contains("<nd")) {
				long ndId = parseNd(currentLine);
				if(currentWay!=null) {
					nodesInAWay.add(ndId);
				}
				if(check) {
					check = false;
					numberOfNdListCreated++;
				}
				nd++;
			}else if(currentLine.contains("</way")) {
				if(foundHighway) {
					res.addWay(currentWay, nodesInAWay);
				}
				check = false;
				nodesInAWay.clear();
				currentWay = null;
				foundHighway = false;
			}
			i++;
		}
		System.out.println("Finished Reading line by line, read "+ i + " lines :\n"
						+ "Read nodes: " + nodes + "\n"
						+ "ways: " + ways +"\n"
						+ "nd: "  + nd + " in " + numberOfNdListCreated +" lists \n"
						+" tags: " + tagsNumber + "\n" + res.printStats()
						+" begin at " + begin.toString() + "\n"
						+ "finish at: " + LocalDateTime.now().toString() +".");
		myScanner.close();
		return res;
	}

	private long parseNd(String toParse) throws UnsupportedEncodingException {
		
		long id = 0;
		
		byte key [] = new byte[30];
		int keyIndex = 0;
		int valueIndex = 0;
		byte value[] = new byte[500];
		byte c;
		
		int quotesMet = 0;
		
		boolean readingKey = true;
		boolean readingValue = false;
		boolean finishedReadingValue = false;
		toParse = toParse.substring(toParse.indexOf(" "));
		byte[] asUtf8Bytes = toParse.getBytes("UTF-8");
		for(int i = 0; i< asUtf8Bytes.length; i++) {
			if(id!=0) {
				//check if read everything of interest and get out.
				break;
			}
			c = asUtf8Bytes[i];
			
			if((c==WHITESPACE || c==SLASH)  && finishedReadingValue) {
				//Reached a new key
				readingKey = true;
				readingValue = false;
				finishedReadingValue = false;
			
				String toCheck = new String(key, "UTF-8");
				String valueToParse = new String(value, "UTF-8");
				toCheck = toCheck.trim();
				valueToParse = valueToParse.trim();
			
				
				//storing values	
				if(toCheck.equals("ref")) {
					id=Long.parseLong(valueToParse);
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];
				}
			}else if(c == EQUAL_SIGN) {
				//reached a new value
				readingValue = true;
				readingKey = false;
			}else {
				if(readingKey) {
					key[keyIndex] = c;
					keyIndex++;
					finishedReadingValue = false;
				}else if(readingValue) {
					if(c!=QUOTES) {
						value[valueIndex] =  c;
						valueIndex++;	
					}else {
						quotesMet++;
						if(quotesMet == 2) {
							finishedReadingValue = true;
							quotesMet = 0;
						}
					}
				}
			}
		}
		return id;
	}

	private String[] parseTag(String toParse) throws UnsupportedEncodingException {
		
		String [] result = new String[2];
		
		byte key [] = new byte[30];
		int keyIndex = 0;
		int valueIndex = 0;
		byte value[] = new byte[500];
		
		byte c;
		
		boolean readingKey = true;
		boolean readingValue = false;
		boolean finishedReadingValue = false;
		
		int quotesMet = 0;
		toParse = toParse.substring(toParse.indexOf(" "));
		byte[] asUtf8Bytes = toParse.getBytes("UTF-8");
		for(int i = 0; i<asUtf8Bytes.length; i++) {
			c = asUtf8Bytes[i];
			if((c==WHITESPACE || c==SLASH) && finishedReadingValue) {
				//Reached a new key
				readingKey = true;
				readingValue = false;
				finishedReadingValue = false;
				
				String toCheck = new String(key, "UTF-8");
				String valueToParse = new String(value, "UTF-8");
				toCheck = toCheck.trim();
				valueToParse = valueToParse.trim();
			
		
				//storing values	
				if(toCheck.equals("k")) {
					result[0] = valueToParse;
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];
					
				}else if(toCheck.equals("v")) {
					result[1] = valueToParse;
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];
				}
			}else if(c == EQUAL_SIGN) {
				//reached a new value
				readingValue = true;
				readingKey = false;
				finishedReadingValue = false;
			}else {
				if(readingKey) {
					key[keyIndex] = c;
					keyIndex++;
				}else if(readingValue) {
					if(c!=QUOTES) {
						value[valueIndex] =  c;
						valueIndex++;	
					}else {
						quotesMet++;
						if(quotesMet == 2) {
							finishedReadingValue = true;
							quotesMet = 0;
						}
					}
				}
			}
		}
		return result;
	}

	private Node parseNode(String toParse) throws UnsupportedEncodingException {
	
		long id = 0;
		double lat = 0;
		double lon = 0;
		
		byte key [] = new byte[30];
		int keyIndex = 0;
		int valueIndex = 0;
		byte value[] = new byte[500];
		byte c;
		
		boolean readingKey = true;
		boolean readingValue = false;
		boolean finishedReadingValue = false;
		
		int quotesMet=0;
		
		toParse = toParse.substring(toParse.indexOf(" "));
		byte[] asUtf8Bytes = toParse.getBytes("UTF-8");
		
		for(int i = 0; i<asUtf8Bytes.length; i++) {
			if(id!=0 && lat != 0 && lon!=0) {
				//check if read everything of interest and get out.
				break;
			}
			c = asUtf8Bytes[i];
			
			if((c==WHITESPACE || c==SLASH)  && finishedReadingValue) {
				//Reached a new key
				readingKey = true;
				readingValue = false;
				finishedReadingValue = false;

				
				String toCheck = new String(key, "UTF-8");
				String valueToParse = new String(value, "UTF-8");
				toCheck = toCheck.trim();
				valueToParse = valueToParse.trim();
			
				//storing values	
				if(toCheck.equals("id")) {
					id=Long.parseLong(valueToParse);
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];
					
				}else if(toCheck.equals("lat")) {
					lat = Double.parseDouble(valueToParse);
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];
				}else if(toCheck.equals("lon")) {
					lon = Double.parseDouble(valueToParse);
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];	
				}
			}else if(c == EQUAL_SIGN) {
				//reached a new value
				readingValue = true;
				readingKey = false;
				finishedReadingValue = false;
			}else {
				if(readingKey) {
					key[keyIndex] = c;
					keyIndex++;
				}else if(readingValue) {
					if(c!=QUOTES) {
						value[valueIndex] = c;
						valueIndex++;	
					}else {
						quotesMet++;
						if(quotesMet == 2) {
							finishedReadingValue = true;
							quotesMet = 0;
						}
					}
				}
			}
		}
		return new Node(id, lon, lat);
	}
	
	private Way parseWay(String toParse) throws UnsupportedEncodingException {
		long id = 0;
		
		byte key [] = new byte[30];
		int keyIndex = 0;
		int valueIndex = 0;
		byte value[] = new byte[500];
		byte c;
		
		boolean readingKey = true;
		boolean readingValue = false;
		boolean finishedReadingValue = false;
		
		int quotesMet = 0;
		
		toParse = toParse.substring(toParse.indexOf(" "));
		byte[] asUtf8Bytes = toParse.getBytes("UTF-8");
		
		for(int i = toParse.indexOf(" ")+1; i< toParse.length(); i++) {
			if(id!=0) {
				//check if read everything of interest and get out.
				break;
			}
			c =asUtf8Bytes[i];
			
			if((c == WHITESPACE || c==SLASH)  && finishedReadingValue) {
				//Reached a new key
				readingKey = true;
				readingValue = false;
				finishedReadingValue = false;
				
		
				String toCheck = new String(key, "UTF-8");
				String valueToParse = new String(value, "UTF-8");
				toCheck = toCheck.trim();
				valueToParse = valueToParse.trim();
			

				//storing values	
				if(toCheck.equals("id")) {
					id=Long.parseLong(valueToParse);
					keyIndex=0;
					valueIndex=0;
					key = new byte[30];
					value = new byte[500];
				}
			}else if(c == EQUAL_SIGN) {
				//reached a new value
				readingValue = true;
				readingKey = false;
				finishedReadingValue = false;
			}else {
				if(readingKey) {
					key[keyIndex] = c;
					keyIndex++;
				}else if(readingValue) {
					if(c!=QUOTES) {
						value[valueIndex] = c;
						valueIndex++;	
					}else {			
						quotesMet++;
						if(quotesMet == 2) {
							finishedReadingValue = true;
							quotesMet = 0;
						}
					}	
				}
			}
		}
		return new Way(id);
	}
	
	 public static String charToHex(char c) {
	      // Returns hex String representation of char c
	      byte hi = (byte) (c >>> 8);
	      byte lo = (byte) (c & 0xff);
	      return byteToHex(hi) + byteToHex(lo);
	   }
	 
	 public static String byteToHex(byte b) {
	      // Returns hex String representation of byte b
	      char hexDigit[] = {
	         '0', '1', '2', '3', '4', '5', '6', '7',
	         '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	      };
	      char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
	      return new String(array);
	   }
}
