package com.example.clover.pojo;

import java.io.Serializable;

public class LibraryCardItem implements Serializable {
    private String itemTitle;
    private String itemText;
    private int id;
    private boolean state; //true is library, false is archive

    private static final long serialVersionUID = 1L;

    public LibraryCardItem(String title, String text){
        itemTitle = title;
        itemText = text;
        state = true;
    }

    public LibraryCardItem(){

    }

    public void setId(int ids){
        id = ids;
    }

    public String getItemTitle(){
        return itemTitle;
    }

    public String getItemText(){
        return itemText;
    }

    public int getId() { return id; }

    public void switchState() { state = !state; }
}
