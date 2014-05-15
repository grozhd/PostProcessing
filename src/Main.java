import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mcavallo.opencloud.Cloud;


public class Main {
	
	static private Properties props;
	
	static private ArrayList<String> vocabulary;
	
	/*
	 *  each entry in topicMatrix list is a topic: array of doubles
	 *  of size equal to vocabulary size
	 */
	static private List<double[]> topicMatrix;
	
	/*
	 * stores only top (cloudSize) number of words for each topic
	 * each sublist is sorted by WordEntry.probability
	 */
	static private List<List<WordEntry>> topicMatrixTop;
	
	static private List<String> topicNames;
	
	static private List<Show> shows;

	/*
	 *  each entry in topicProportions list is an array of topic proportions for
	 *  a particular show: array of doubles of size equal to number of topics
	 *  Indices in the list correspond to indices of shows in the "shows" list.
	 */
	static private List<List<WordEntry>> topicProportions;
	
	static private String outputDir;
	
	/*
	 * WordCloud parameteres: font size, number of words to use.
	 */
	static private double maxFontSize;
	static private int cloudSize;
	
	static private String cssShows;
	static private String cssTopics;
	
	public static void loadVocabulary(File vocFile) {
		// initialize vocabulary
		vocabulary = new ArrayList<String>();
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(vocFile));
				
			String line = null;
			
			while((line = fileReader.readLine()) != null) {
				vocabulary.add(line.split("\t")[1]);
			}
			
			fileReader.close();
			
		} catch (Exception e) {
			System.err.println("Error reading the vocabulary file");
			e.printStackTrace();
		}
	}
	
	public static void loadTopics(File topicFile) {
		// initialize the topicMatrix, topicMatrixTop
		topicMatrix = new ArrayList<double[]>();
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(topicFile));
			
			// initialize the topic matrix while reading first line
			String line = fileReader.readLine();
			String[] probStrings = line.split(" ");
			
			for (String pb : probStrings) {
				double[] topic = new double[vocabulary.size()];
				topic[0] = Double.parseDouble(pb);
				topicMatrix.add(topic);	
			}
			
			
			int lineNum = 0;
					
			while((line = fileReader.readLine()) != null) {
				/*
				 *  Parse all the numbers in line as double, and add each number to its topic's
				 *  array. Each line should have one number for each topic.
				*/
				++lineNum;
				
				probStrings = line.split(" ");
				
				for (int topicNum = 0; topicNum < probStrings.length; ++topicNum) {
					topicMatrix.get(topicNum)[lineNum] = Double.parseDouble(probStrings[topicNum]);
				}
			}
			
			fileReader.close();
					
		} catch (Exception e) {
			if (e instanceof IndexOutOfBoundsException) {
				System.err.println("Check if the strings have the correct number of space separated numbers in topics file.");
				System.err.println("It should be the vocabulary size.");
			} else {
				System.err.println("Error reading the topic matrix file");
			}
			e.printStackTrace();
	   }
		
	}
	
	public static void loadTopicName(File topicNamesFile) {
		topicNames = new ArrayList<String>();
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(topicNamesFile));
						
			String line = null;
					
			while((line = fileReader.readLine()) != null) {
				topicNames.add(line);
			}
			
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *  finds top cloudSize words for each topic
	 *  store them as WordEntries
	 */
	public static void buildTopicsTop() {
		topicMatrixTop = new ArrayList<List<WordEntry>>();
	
		for (int topicNum = 0; topicNum < topicMatrix.size(); ++topicNum) {
			// create empty list for current topic
			List<WordEntry> topicTop = new ArrayList<WordEntry>();
			
			// add all words with probabilities as WordEntries to topicTop
			double[] topic = topicMatrix.get(topicNum);
			for (int wordNum = 0; wordNum < vocabulary.size(); ++wordNum) {
				String word = vocabulary.get(wordNum);
				double probability = topic[wordNum];
				topicTop.add(new WordEntry(word, probability, topicNum));
			}
			
			// sort all WordEntries and get top cloudSize
			Collections.sort(topicTop, Collections.reverseOrder());
			topicTop = topicTop.subList(0, cloudSize);
			
			topicMatrixTop.add(topicTop);
		}
		
					
	}
	
	public static void loadTopicProportions(File topicProportionsFile) {
		// initialize the topicProportions
		topicProportions = new ArrayList<List<WordEntry>>(shows.size());
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(topicProportionsFile));
						
			String line = null;
			
			while((line = fileReader.readLine()) != null) {
				/*
				 *  Parse all the numbers in line as double, and add this array 
				 *  of probabilities  (i.e. topic proportions for a show) to the list
				*/
				List<WordEntry> topicProps = new ArrayList<WordEntry>();
				String[] probStrings = line.split(" ");
				
				
				
				for (int i = 0; i < probStrings.length; ++i) {
					WordEntry we = new WordEntry(topicNames.get(i), Double.parseDouble(probStrings[i]), i);
					topicProps.add(we);
				}
			
				topicProportions.add(topicProps);
			}
			
			fileReader.close();
			
			// now we create recsys input file
			createRecSysInput();
			
			// and now we order topic proportions for each show
			for (int showNum = 0; showNum < topicProportions.size(); ++showNum) {
				Collections.sort(topicProportions.get(showNum), Collections.reverseOrder());
			}
			
							
		} catch (Exception e) {
			if (e instanceof IndexOutOfBoundsException) {
				System.err.println("Check if the strings have the correct number of space separated numbers in topics file.");
				System.err.println("It should be the number of topics.");
			} else {
				System.err.println("Error while reading topic proportions!");
			}
			e.printStackTrace();
	   }
		
	}

	public static void loadShows(File showsFile) throws Exception {
		// initialize the topicMatrix
		shows = new ArrayList<Show>();
		
		BufferedReader fileReader = new BufferedReader(new FileReader(showsFile));
						
		String line = null;
		int counter = 0;
			
			
		while((line = fileReader.readLine()) != null) {
			int id = Integer.parseInt(line.split("\\t")[0]);
			String title = line.split("\\t")[1];
				
			if (id != counter) {
				fileReader.close();
				throw new Exception("Show id is not equal to line number! WTF?");
			}
	
			shows.add(new Show(id, title));
			counter++;
		}
		
		fileReader.close();
					
	}
	
	public static void loadShowsInfo(File JSONFile) throws IOException, ParseException {
		// open JSON file
		FileReader reader = new FileReader(JSONFile);
		JSONParser jsonParser = new JSONParser();
		
		//parse the file so that each cell in the JSONArray contains one JSON (respectively one full TVShow)
		JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
			
		//this part won't probably help you, it's just here to show you how to access the fields.
		Iterator<?> iterator = jsonArray.iterator();
		while (iterator.hasNext()) {
			JSONObject json = (JSONObject) iterator.next();
			
			// get the id for the show from JSON
			int id = Integer.parseInt((String) json.get("id"));
			
			// get the show by id
			Show sh = shows.get(id);
			
			// set all fields
			sh.setRating( (String) json.get("rating") );
			sh.setPlot( (String) json.get("plot") );
			sh.setPosterURL( (String) json.get("poster") );
			sh.setGenre( (String) json.get("genre") );		
		}
			

	}
	
	/*
	 * Creates a page for each topic with word distribution.
	 * Creates a page for each show with WordCloud of most important
	 * words based on topic. Each word has a link to it's topic page.
	 */
	public static void createTopicsHTML(){	
		for (int topicNum = 0; topicNum < topicMatrixTop.size(); ++topicNum) {
			String headerHTML;
			headerHTML = "<h1>" + topicNames.get(topicNum) + "</h1>\n";
			
			// get the top words for topic
			List<WordEntry> topicTop = topicMatrixTop.get(topicNum);
			
			// add the list entries for each word to html code
			headerHTML = headerHTML + HTMLCreator.createList(topicTop);
			
			// crate a WordCloud based on topicTop
			Cloud cloud = WordCloudCreator.createWordCloud(topicTop, maxFontSize);
			
			// generate html code
			String htmlCode = HTMLCreator.createHTML(cloud, headerHTML, cssTopics);
			
			// write it to a file
			File htmlFile = new File(outputDir + "\\topics\\" + "topic" + topicNum + ".html");
			try {
				if (!htmlFile.exists()) htmlFile.createNewFile();
				PrintWriter out = new PrintWriter(htmlFile.getPath());
				
				out.println(htmlCode);
				out.close();
			} catch (IOException e) {
				System.err.println(htmlFile.getPath());
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Creates a page for each show with top words distribution.
	 */
	public static void createShowsHTML(){	
		for (int showNum = 0; showNum < topicProportions.size(); ++ showNum) {
			// get topic proportions for the show
			List<WordEntry> topicProps = topicProportions.get(showNum);
			
			// get top words for the show based on show's topic proportions
			List<WordEntry> topWords = getTopWordsForShow(topicProps);
			
			// generate html code for the list of top words
			String headerHTML;
			headerHTML = shows.get(showNum).generateShowInfoHTML();
			headerHTML = headerHTML + HTMLCreator.createList(topWords);
			
			// add html code for the list of topics
			int topicListSize = Integer.parseInt(props.getProperty("topicListSize"));
			topicListSize = Math.min(topicListSize, topicMatrix.size());
			headerHTML = headerHTML + HTMLCreator.createList(topicProps.subList(0, topicListSize));
			
			// add the pie chart of the topics presence in the show
			headerHTML += addTopicPieChart(topicListSize, topicProps);
			
			// crate a WordCloud based on topWords, pass the topics directory
			File topicDir = new File(outputDir + "\\topics");
			Cloud cloud = WordCloudCreator.createWordCloud(topWords, maxFontSize, topicDir.getPath());
						
			// generate html code
			String htmlCode = HTMLCreator.createHTML(cloud, headerHTML, cssShows);
						
			// write it to a file
			File htmlFile = new File(outputDir + "\\shows\\" + "show" + showNum + ".html");
			try {
				if (!htmlFile.exists()) htmlFile.createNewFile();
				PrintWriter out = new PrintWriter(htmlFile.getPath());
							
				out.println(htmlCode);
				out.close();
			} catch (IOException e) {
				System.err.println(htmlFile.getPath());
				e.printStackTrace();
			}
		}
	}
	
	/*
	 *  Creates a pie chart of topics in the show
	 */
	private static String addTopicPieChart(int topicListSize, List<WordEntry> topicProps) {
		int othersTopicPercentage = 100;
		
		String chartScript = "<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n<script type=\"text/javascript\">\ngoogle.load(\"visualization\", \"1\", {packages:[\"corechart\"]});\ngoogle.setOnLoadCallback(drawChart);" +
				"\nfunction drawChart(){\nvar data = google.visualization.arrayToDataTable([\n['Topic Name', 'Probability'],\n";

		for(int i = 0; i < topicListSize - 1; i++) {
			WordEntry we = topicProps.subList(0, topicListSize).get(i);
			int tempProba = (int) Math.floor(we.probability * 100);
			
			//System.err.println(tempProba);
			
			othersTopicPercentage -= tempProba;
			chartScript += "['" + we.word + "', " + tempProba + "],\n";
		}
		
		WordEntry we = topicProps.subList(0, topicListSize).get(topicListSize - 1);
		int tempProba = (int) Math.ceil(we.probability * 100);
		othersTopicPercentage -= tempProba;
		chartScript += "['" + we.word + "', " + tempProba + "],\n";
		chartScript += "['Other Topics', " + othersTopicPercentage + "],\n";
		String varOptions = "\nvar options = {\ntitle: 'Topic repartition',\ntitleTextStyle: {color: \"white\"}, \nbackgroundColor: \"transparent\", \nlegend: {textStyle: {color: 'white', fontSize: 12}} \n};";
		chartScript += "]);" + varOptions + "\nvar chart = new google.visualization.PieChart(document.getElementById('piechart'));\nchart.draw(data, options);\n}\n</script>\n<div id=\"piechart\" style=\"width: 900px; height: 500px;\"></div>";
		
		return chartScript;
	}
	
	/*
	 *  Function takes topic proportions for some show.
	 *  Then finds top cloudSize words for this show from
	 *  all topics, based on topic proportions.
	 */
	public static List<WordEntry> getTopWordsForShow(List<WordEntry> topicProps) {
		List<WordEntry> topWords = new ArrayList<WordEntry>();
		
		// add all WordEntries to topWords, weighted by topic proportions for the show
		for (int topicPropIndex = 0; topicPropIndex < topicMatrixTop.size(); ++topicPropIndex) {
			// get weight (i.e. this topic proportion)
			double topicWeight = topicProps.get(topicPropIndex).probability;
			int topicNum = topicProps.get(topicPropIndex).topicNumber;
			
						
			for (WordEntry we : topicMatrixTop.get(topicNum)) {
				WordEntry weightedWE = new WordEntry(we, topicWeight);
				topWords.add(weightedWE);
				//System.err.println(weightedWE.word + ", " + weightedWE.topicNumber + ", " + weightedWE.probability);
			}
		}
		
		// sort all WordEntries (sort is on the probability)
		Collections.sort(topWords, Collections.reverseOrder());
		
		// return top cloudSize words
		return topWords.subList(0, cloudSize);
	}
	
	/*
	 *  Loads the properties and all the data (populates the member variables).
	 */
	private static void init() throws Exception {
		// load properties
		try {
			InputStream input = new FileInputStream("postprocessing.props");
			props = new Properties();
			props.load(input);
		} catch (Exception e) {
			System.err.println("Error while loading the properties. Check the properties file.");
			e.printStackTrace();
		}
		
		// set the outputDir, check if it exists
		outputDir = props.getProperty("outputDir");
		File outputFolder = new File(outputDir);
		if (!outputFolder.exists()) outputFolder.mkdirs();
		
		// load all the data
		File vocFile = new File(props.getProperty("vocabularyFile"));
		loadVocabulary(vocFile);
		
		//generateRandomData(50, 30, 60);
		//System.exit(0);
		
		File topicsFile = new File(props.getProperty("topicsFile"));
		loadTopics(topicsFile);
		
		File topicNamesFile = new File(props.getProperty("topicNamesFile"));
		loadTopicName(topicNamesFile);
		
		File showsFile = new File(props.getProperty("showsFile"));
		loadShows(showsFile);
		
		File JSONShowsFile = new File(props.getProperty("JSONShowsFile"));
		loadShowsInfo(JSONShowsFile);
		
		File topicProportionsFile = new File(props.getProperty("topicProportionFile"));
		loadTopicProportions(topicProportionsFile);
		
		// load WordCloud parameteres
		maxFontSize = Double.parseDouble(props.getProperty("maxFontSize"));
		cloudSize = Integer.parseInt(props.getProperty("cloudSize"));
		
		// find the most important words in each topic;
	    buildTopicsTop();
	    
	    // load CSS
	    try {
	    	cssShows = new Scanner(new File(props.getProperty("cssShows"))).useDelimiter("\\Z").next();
	    	cssShows = "<style>\n" + cssShows + "</style>\n"; 
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    try {
	    	cssTopics = new Scanner(new File(props.getProperty("cssTopics"))).useDelimiter("\\Z").next();
	    	cssTopics = "<style>\n" + cssTopics + "</style>\n"; 
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	/*
	 *  Builds an input file for RecSysInput
	 */
	private static void createRecSysInput() {
		File recSysInput = new File(props.getProperty("inputRecSysFile"));
		
		try {
			recSysInput.createNewFile();
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(recSysInput));
			
			for (int showNum = 0; showNum < shows.size(); ++showNum) {
				String line = showNum + "\t" + shows.get(showNum) + "\t";
				for (WordEntry we : topicProportions.get(showNum)) {
					line = line + we.probability + "\t";
				}
				// remove last tab
				line = line.trim();
				
				// write line
				fileWriter.write(line);
				fileWriter.newLine();
			}
			
			fileWriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	/*
	 *  DEBUG FUNCTION
	 */
	private static double[] generateDistribution(int dim) {
		Random r = new Random();
		
		double[] dist = new double[dim];
		double sum = 0;
		for (int i = 0; i < dim; ++i) {
			dist[i] = r.nextDouble();
			sum += dist[i];
		}
		
		// normalize
		for (int i = 0; i < dim; ++i) {
			dist[i] /= sum;
		}
		
		return dist;
	}
	
	/*
	 *  DEBUG FUNCTION
	 */
	private static void generateRandomData(int topicNum, int showNum, int vocabularySize) {
		File topicsFile = new File(props.getProperty("topicsFile") + ".random");
		File topicProportionFile = new File(props.getProperty("topicProportionFile") + ".random");
		
		try {	
			topicsFile.createNewFile();
			topicProportionFile.createNewFile();
			
			// writing data in topicsFile
			List<double[]> distribList = new ArrayList<double[]>();
			for (int t = 0; t < topicNum; ++t) {
				double[] dist = generateDistribution(vocabularySize);
				distribList.add(dist);
			}
			
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(topicsFile));
			for (int v = 0; v < vocabularySize; ++v) {
				String line = "";
				for (int t = 0; t < topicNum; ++t) {
					line += distribList.get(t)[v] + " ";	
				}
				line = line.trim();
				fileWriter.write(line);
				fileWriter.newLine();
			}
			fileWriter.close();
			
			// writing data in topicProportionFile
			fileWriter = new BufferedWriter(new FileWriter(topicProportionFile));
			for (int t = 0; t < showNum; ++t) {
				double[] dist = generateDistribution(topicNum);
				String line = "";
				for (double d : dist) line += d + " ";
				line = line.trim();
				
				fileWriter.write(line);
				fileWriter.newLine();
			}
			fileWriter.close();
			
		} catch (Exception e) {
			System.err.println("Data already exists!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		// load all the data
		init();
			
		// create pages for all the topics
	    createTopicsHTML();
	    
	    // create pages for all the shows
	    createShowsHTML();
	    
	}
	
	
	
	
}
