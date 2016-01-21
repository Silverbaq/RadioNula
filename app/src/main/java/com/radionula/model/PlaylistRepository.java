package com.radionula.model;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.radionula.utils.HttpRequest;

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

    public void updateFeed(String url){
        this.feedUrl = url;
        myHandler.removeCallbacks(this);
        myHandler.post(this);
    }

    @Override
    public void run() {
        new RSSFeedTask().execute(feedUrl);
        myHandler.postDelayed(this, 20000);
    }


    class RSSFeedTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (_current == null || !tracks.get(0).getTitel().equals(_current.getTitel())){
                _current = tracks.get(0);
                triggerObserver();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tracks.clear();
        }

        @Override
        protected Void doInBackground(String... params) {

            URL url1;
            try {
                //
                // Downloading RSS feed to String
                String xmlString = HttpRequest.get(params[0]).body();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                //
                // Building document from XML String
                Document doc = db.parse(new InputSource(new StringReader(xmlString)));
                doc.getDocumentElement().normalize();

                //
                // Getting all nodes
                NodeList nodeList = doc.getElementsByTagName("item");


                for (int i = 0; i < nodeList.getLength(); i++) {

                    Node node = nodeList.item(i);


                    Element fstElmnt = (Element) node;

                    //
                    // Getting title
                    NodeList titleList = fstElmnt.getElementsByTagName("title");
                    Element titleElement = (Element) titleList.item(0);
                    titleList = ((Node) titleElement).getChildNodes();
                    String titel = ((Node) titleList.item(0)).getNodeValue();

                    //
                    // Getting image
                    NodeList imageList = fstElmnt.getElementsByTagName("image");
                    Node imageNode = imageList.item(0);

                    Element urlElement = (Element) imageNode;
                    NodeList urlList = urlElement.getElementsByTagName("url");
                    Element urlelmt = (Element) urlList.item(0);
                    urlList = ((Node) urlelmt).getChildNodes();
                    String url = ((Node) urlList.item(0)).getNodeValue();

                    String urlformated = url.replace(" ", "%20");
                    String[] splitTitle = titel.split(" - ");

                    tracks.add(new NulaTrack(splitTitle[0], splitTitle[1], urlformated));
                    Log.d("debug", "track added");


                }

            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }
    }
}