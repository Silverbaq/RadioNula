package com.radionula.model;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

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


    @Override
    public void run() {
        new RSSFeedTask().execute(feedUrl);
        myHandler.postDelayed(this, 20000);
    }


    class RSSFeedTask extends AsyncTask<String, Void, Void> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;

        NulaTrack _tempCurrent;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (_tempCurrent != null) {
                if (_current == null || !_tempCurrent.getTitel().equals(_current.getTitel())) {
                    _current = _tempCurrent;
                    tracks.add(0, _tempCurrent);
                    triggerObserver();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //tracks.clear();
        }

        @Override
        protected Void doInBackground(String... params) {
            String stringUrl = params[0];
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);

                //Create a connection
                HttpURLConnection connection = (HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();

                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                String result = stringBuilder.toString();

                JSONObject jsonObject = new JSONObject(result);

                JSONObject ch1 = jsonObject.getJSONObject("ch1");
                JSONObject currentSong = ch1.getJSONObject("currentSong");

                String artist = currentSong.getString("artist");
                String title = currentSong.getString("title");
                String cover = currentSong.getString("cover");

                _tempCurrent = new NulaTrack(artist, title, cover);
                Log.d("debug", "track added");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}