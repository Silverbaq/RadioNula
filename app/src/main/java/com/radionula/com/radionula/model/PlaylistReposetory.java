package com.radionula.com.radionula.model;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by silverbaq on 12/6/15.
 */
public class PlaylistReposetory{
    List<NulaTrack> tracks = new ArrayList<NulaTrack>();

    private void updatePlaylist(String url){
        new RSSFeedTask().execute(url);
    }

    public List<NulaTrack> getPlaylist(){
        return tracks;
    }



class RSSFeedTask extends AsyncTask<String, Void, Void>{

    @Override
    protected Void doInBackground(String... params) {
        String rssurl = "http://radionula.com/rss.xml";

        URL url1;
        try {
            url1 = new URL(rssurl);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url1.openStream()));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("item");


            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);


                Element fstElmnt = (Element) node;

                // getting title
                NodeList titleList = fstElmnt.getElementsByTagName("title");
                Element titleElement = (Element) titleList.item(0);
                titleList = ((Node) titleElement).getChildNodes();
                String titel = ((Node) titleList.item(0)).getNodeValue();

                // getting image
                NodeList imageList = fstElmnt.getElementsByTagName("image");
                Node imageNode = imageList.item(0);

                Element urlElement = (Element) imageNode;
                NodeList urlList = urlElement.getElementsByTagName("url");
                Element urlelmt = (Element) urlList.item(0);
                urlList = ((Node) urlelmt).getChildNodes();
                String url = ((Node)urlList.item(0)).getNodeValue();

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