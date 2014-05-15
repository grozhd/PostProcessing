/*
 *  Simple container class that stores:
 *  word, it's probability in some topic, topic's number.
 *  Allows to compare WordEntries based on probability.
 */
public class WordEntry implements Comparable<WordEntry> {
		String word;
		Double probability;
		int topicNumber;
		
		WordEntry(String inputWord, Double inputProbability, int inputTopicNum) {
			this.word = inputWord;
			this.probability = inputProbability;
			this.topicNumber = inputTopicNum;
		}
		
		// copy constructor with factor
		WordEntry(WordEntry we, Double probabilityFactor) {
			this.word = we.word;
			this.topicNumber = we.topicNumber;
			this.probability = probabilityFactor * we.probability;
		}
		
		
		@Override
		public String toString() {
			return word + ": " + probability + ", from topic " + topicNumber;
		}
		
		// comparator based on probability
		@Override
		public int compareTo(WordEntry we) {
			return this.probability.compareTo(we.probability);
		}
}