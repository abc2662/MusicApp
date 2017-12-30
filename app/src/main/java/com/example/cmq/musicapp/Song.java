package com.example.cmq.musicapp;

public class Song {
    private String Title;
    private int File;
    private String Link;

    public Song(String title, int file) {
        Title = title;
        File = file;
    }

    public Song(String title, String link) {
        Title = title;
        Link = link;
    }

    public String getTitle() {
        return Title;
    }

    public String getLink() {
        return Link;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getFile() {
        return File;
    }

    public void setFile(int file) {
        File = file;
    }
}
