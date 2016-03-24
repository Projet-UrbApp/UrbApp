package com.ecn.urbapp.utils;

public class RowItem {
	//TODO Adddescription for javadoc
    private String imagePath;
	//TODO Adddescription for javadoc
    private String title;
	//TODO Adddescription for javadoc
    private String desc;

	//TODO Adddescription for javadoc
    public RowItem(String imagePath, String title, String desc) {
        this.imagePath = imagePath;
        this.title = title;
        this.desc = desc;
    }
	//TODO Adddescription for javadoc
    public String getImagePath() {
        return imagePath;
    }
	//TODO Adddescription for javadoc
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
	//TODO Adddescription for javadoc
    public String getDesc() {
        return desc;
    }
	//TODO Adddescription for javadoc
    public void setDesc(String desc) {
        this.desc = desc;
    }
	//TODO Adddescription for javadoc
    public String getTitle() {
        return title;
    }
	//TODO Adddescription for javadoc
    public void setTitle(String title) {
        this.title = title;
    }
	//TODO Adddescription for javadoc
    @Override
    public String toString() {
        return title + "\n" + desc;
    }
}