package com.radionula.model;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.radionula.utils.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by silverbaq on 12/6/15.
 */
public class PlaylistRepository extends Observable implements Runnable {
    List<NulaTrack> tracks = new ArrayList<NulaTrack>();
    Handler myHandler = new Handler();
    private NulaTrack _current;
    private String feedUrl;

    public PlaylistRepository(String feedUrl) {
        myHandler.post(this);
        this.feedUrl = feedUrl;
    }

    public List<NulaTrack> getPlaylist() {
        return tracks;
    }

    public void triggerObserver() {
        setChanged();
        notifyObservers();
    }

    public void updateFeed(String url) {
        this.feedUrl = url;
        myHandler.removeCallbacks(this);
        myHandler.post(this);
    }

    public void stopFeed() {
        // TODO: Need to be tested
        myHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        new RSSFeedTask().execute(feedUrl);
        myHandler.postDelayed(this, 20000);
    }


    class RSSFeedTask extends AsyncTask<String, Void, Void> {


        NulaTrack _tempCurrent;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (_current == null || !_tempCurrent.getTitel().equals(_current.getTitel())) {
                _current = _tempCurrent;
                tracks.add(0, _tempCurrent);
                triggerObserver();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //tracks.clear();
        }

        @Override
        protected Void doInBackground(String... params) {

            URL url1;
            try {
                //
                // Downloading RSS feed to String

                String jsonString = HttpRequest.get(params[0]).body();

                JSONObject reader = new JSONObject(jsonString);

                JSONObject ch1 = reader.getJSONObject("ch1");
                JSONObject currentSong = ch1.getJSONObject("currentSong");

                String artist = currentSong.getString("artist");
                String title = currentSong.getString("title");
                String cover = currentSong.getString("cover");

                //String urlformated = url.replace(" ", "%20");
                //String[] splitTitle = titel.split(" - ");

                //_tempCurrent = new  NulaTrack(splitTitle[0], splitTitle[1], urlformated);
                _tempCurrent = new NulaTrack(artist, title, cover);
                Log.d("debug", "track added");


//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}