import java.util.List;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

public class WordCloudCreator {
	/*
	 *  Creates a WordCloud for WordEntries, links are pointing to the same page
	 */
	public static Cloud createWordCloud(List<WordEntry> topicTopWords, double maxWeight) {
		Cloud cloud = new Cloud();
		cloud.setMaxWeight(maxWeight);
		cloud.setDefaultLink("?");
		
		for (WordEntry we : topicTopWords) {
			
			Tag tag = new Tag(we.word, we.probability);
			cloud.addTag(tag);
		}
				
				return cloud;
	}
	
	/*
	 *  Creates a WordCloud for WordEntries, adding links to topics pages
	 *  linkDir specifies the path to folder of topic html pages
	 */
	public static Cloud createWordCloud(List<WordEntry> topicTopWords, double maxWeight, String linkDir) {
		Cloud cloud = new Cloud();
		cloud.setMaxWeight(maxWeight);
		cloud.setDefaultLink("http://www.google.com");
				
		for (WordEntry we : topicTopWords) {
			try {
				String link = "../topics/topic" + we.topicNumber + ".html";
						
				// check if the word exists
				boolean wordExists = true;
				while (wordExists) {
					wordExists = false;
					for (Tag tag : cloud.tags()) {
						if (tag.getName().equals(we.word)) {
							wordExists = true;
							we.word = we.word + "*";
							break;
						}
					}
				}
				
				Tag tag = new Tag(we.word, link, we.probability);
				cloud.addTag(tag);

				

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return cloud;
	}

}
