package com.radionula.model;

/**
 * Created by silverbaq on 12/6/15.
 */
public class NulaTrack {
    private String artist;
    private String titel;
    private String image;

    public NulaTrack(String artist, String titel, String image) {
        this.setArtist(artist);
        this.setTitel(titel);
        this.setImage(image);
    }

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
