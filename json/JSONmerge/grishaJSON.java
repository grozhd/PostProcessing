package utils;

import org.json.simple.*;
/**
 * What I suggest to you here Grisha is that you iterate through all the JSON to fill 
 * your data structure (I guess you have one that holda all your TVShows). You can access
 * the desire field as shown below. If you need help, whatsapp.
 * The jar file needed is in the archive.
 * 
 * 
 * NB: if no informations are available for a TVShow (because they did not exist in the DB i crawled) 
 * the field rating = "not found" and the other fields won't exist (except for name and id of course).
 * So before you use any informations from the JSON file, make sure that rating != "not found". 
 * @author nassimdrissielkamili
 *
 */
public class grishaJSON {
	
		//open the desired file
		FileReader reader = new FileReader("data/imdb_ratings.json");
		JSONParser jsonParser = new JSONParser();
		//parse the file so that each cell in the JSONArray contains one JSON (respectively one full TVShow)
		JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
		
		//this part won't probably help you, it's just here to show you how to access the fields.
		Iterator<JSONObject> iterator = jsonArray.iterator();
		while (iterator.hasNext()) {
			JSONObject json = (JSONObject) iterator.next();
				TVShow tv = mainMemory.get(json.get("title"));
				if (!json.get("rating").equals("not found")) {
					tv.setRating(Double.parseDouble((String) json
							.get("rating")));
					tv.setPlot((String) json.get("plot"));
					tv.setImageURL((String) json.get("poster"));
					tv.setGenre((String) json.get("genre"));

				}else{
					tv.setRating(RATING_NOT_FOUND);
				}
				tv.toString();
			}
		}

	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
