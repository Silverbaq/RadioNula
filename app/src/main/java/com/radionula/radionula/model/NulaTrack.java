package com.radionula.radionula.model;

/**
 * Created by silverbaq on 12/6/15.
 */
public class NulaTrack {
    private int id;
    private String artist;
    private String titel;
    private String image;

    public NulaTrack(String artist, String titel, String image) {
        this.id = -1;
        this.setArtist(artist);
        this.setTitel(titel);
        this.setImage(image);
    }

    public int getId(){return id;}
    public void setId(int id){this.id = id;}

    public String getArtist() {
        return artist;
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitel() {
        return titel;
    }

    void setTitel(String titel) {
        this.titel = titel;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
