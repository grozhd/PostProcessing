import java.util.List;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.formatters.HTMLFormatter;


public class HTMLCreator {
	public static String createHTML(Cloud cloud, String header, String css) {
		HTMLFormatter formatter = new HTMLFormatter();
		formatter.setHtmlTemplateTop("<html>\n" + css + "<body>\n" + header + "\n");
		
		String bottom = "<div id=\"button\"><input type=\"button\" onClick=\"location.href='/index'\" value=\"Change TV Shows selection\" ></div>";
		
		formatter.setHtmlTemplateBottom(bottom + "</body>\n" + "</html>\n");


		String htmlCode = formatter.html(cloud);
		htmlCode = htmlCode.substring(htmlCode.indexOf('\n')+1);
		htmlCode = htmlCode.substring(htmlCode.indexOf('\n')+1);
		
		
		return htmlCode;
	}
	
	public static String createList(List<WordEntry> topicTop) {
		String listHTML = "<ol>\n";
					
		for (int wordNum = 0; wordNum < topicTop.size(); ++wordNum) {	
			WordEntry we = topicTop.get(wordNum);
			listHTML = listHTML + "<li>" + we.word + ": " + we.probability + "</li>\n";
		}
		listHTML = listHTML + "</ol>\n";
		
		return listHTML;
	}
	
	
}
