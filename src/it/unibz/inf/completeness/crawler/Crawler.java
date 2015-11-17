package it.unibz.inf.completeness.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Crawling natural language completeness statements on Wikipedia (https://www.mediawiki.org/wiki/API:Search)
 * HTTP library: http://unirest.io/java.html
 * 
 * @author Fariz Darari (fadirra@gmail.com)
 */
public class Crawler {

	private static String fileName = "completeness-statements.txt"; // write crawling results here
	
	public static void main(String[] args) {
		
		boolean shouldWeStop = false;
		JSONArray allSearchResults = new JSONArray();
		String stringAllSearchResults = "";
		
		// arguments for API call
		String srsearch = "\"this is a complete list\""; // Search for all page titles (or content) that have this value.
		int srlimit = 50; // How many total pages to return. No more than 50 (500 for bots) allowed. (Default: 10)
		int sroffset = 0; // Use this value to continue paging (returned by query).
		String format = "json"; // This tells the API that we want data to be returned in JSON format.
		
		try {
			
			while(!shouldWeStop) {
			
				// execute API call
				HttpResponse<JsonNode> apiCall = Unirest.get("http://en.wikipedia.org/w/api.php?")
				  .queryString("action", "query")
				  .queryString("list", "search")
				  .queryString("srsearch", srsearch)
				  .queryString("srlimit", srlimit)
				  .queryString("sroffset", sroffset)
				  .queryString("format", format)
				  .asJson();
				
				// print API call
				System.out.println("http://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + srsearch +"&srlimit="+srlimit+"&sroffset="+sroffset+"&format="+format);
				
				// example result of API call = {"batchcomplete":"","continue":{"sroffset":1,"continue":"-||"},"query":{"searchinfo":{"totalhits":735},"search":[{"ns":0,"title":"List of Speedway Grand Prix riders","snippet":"<span class=\"searchmatch\">This</span> <span class=\"searchmatch\">is</span> <span class=\"searchmatch\">a</span> <span class=\"searchmatch\">complete</span> <span class=\"searchmatch\">list</span> of riders who have entered a FIM Speedway Grand Prix since 1995. After 2015 Scandinavian SGP in Stockholm (26 September 2015)","size":9074,"wordcount":512,"timestamp":"2015-09-27T09:12:07Z"}]}}
				
				JSONArray searchArray = apiCall.getBody().getObject().getJSONObject("query").getJSONArray("search"); // getting search results from the returned JSON
				for(int i = 0; i < searchArray.length(); i++) {
					String title = searchArray.getJSONObject(i).get("title").toString();
					String delimiter = "@"; // delimiter between title and snippet where the search keywords occur
					String snippet = searchArray.getJSONObject(i).get("snippet").toString().replaceAll("<span class=\"searchmatch\">", "").replaceAll("</span>", ""); // remove HTML tags for search keywords found in snippet
					stringAllSearchResults = stringAllSearchResults + title + delimiter + snippet + "\n";
				}
				
				sroffset = sroffset + srlimit;
				allSearchResults = concatArray(allSearchResults, searchArray);
				shouldWeStop = apiCall.getBody().getObject().isNull("continue"); // check if there is still another page/offset of results
				
			}
			
		} catch (UnirestException e) { e.printStackTrace(); }

		System.out.println("Number of results: " + allSearchResults.length());

		try {
			writeToFile(stringAllSearchResults, fileName);
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	/**
	 * Concatenating two JSON arrays
	 * Credit: http://stackoverflow.com/questions/4590753/concate-jsonarray
	 * 
	 * @param arr1
	 * @param arr2
	 * @return the concatenation of arr1 and arr2
	 * @throws JSONException
	 */
	private static JSONArray concatArray(JSONArray arr1, JSONArray arr2)
	        throws JSONException {
	    JSONArray result = new JSONArray();
	    for (int i = 0; i < arr1.length(); i++) {
	        result.put(arr1.get(i));
	    }
	    for (int i = 0; i < arr2.length(); i++) {
	        result.put(arr2.get(i));
	    }
	    return result;
	}


	/**
	 * Writing a string to a file
	 * Credit: http://www.mkyong.com/java/how-to-write-to-file-in-java-bufferedwriter-example/
	 * 
	 * @param str
	 * @param fileName
	 * @throws IOException
	 */
	private static void writeToFile(String str, String fileName)
			throws IOException {
		File file = new File(fileName);
		if (!file.exists()) { // if file doesn't exists, then create it
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(str);
		bw.close();
	}
	
}