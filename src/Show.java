/*
 *  Simple container class that stores all show nfo
 *  
 */
public class Show {
	private int id;
	private String title;
	private double rating;
	private String plot;
	private String posterURL;
	private String genre;
	

	Show(int inputID, String inputTitle) {
		setId(inputID);
		setTitle(inputTitle);
	}
	
	String generateShowInfoHTML() {
		String html = "<h1>Show " + getId() + ": " + getTitle() + "</h1>\n";
		
		if (getRating() == Double.NaN) {
			html += "<p> Rating: not available </p>\n";
		} else {
			html += "<p> Rating: " + getRating() + "</p>\n";
		}
		
		html += "<p> Genre: " + getGenre() + "</p>\n";
		html += "<p> Plot: " + getPlot() + "</p>\n";
		html += "<p> Poster: <img src=\"" + getPosterURL() + "\"> </p>\n";
		
		
		return html;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(String rating) {
		try {
			this.rating = Double.parseDouble(rating);
		} catch (Exception e) {
			this.rating = Double.NaN;
		}
	}

	public String getPlot() {
		return plot;
	}

	public void setPlot(String plot) {
		this.plot = plot;
	}

	public String getPosterURL() {
		return posterURL;
	}

	public void setPosterURL(String posterURL) {
		this.posterURL = posterURL;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	
}
