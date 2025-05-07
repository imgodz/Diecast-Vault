package com.imgodz.diecastvault;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diecast_table")
public class Diecast {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    @ColumnInfo(name = "model_maker")
    private String modelMaker;

    @ColumnInfo(name = "release_info")
    private String releaseInfo;

    private int price;

    @ColumnInfo(name = "set_series")
    private String setSeries;

    @ColumnInfo(name = "purchase_date")
    private String purchaseDate;

    @ColumnInfo(name = "primary_color")
    private String primaryColor;

    @ColumnInfo(name = "secondary_color")
    private String secondaryColor;

    private String livery;

    private String category;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    public Diecast() {
    }

    public Diecast(int id, String name, String modelMaker,
                   String releaseInfo, int price, String setSeries, String purchaseDate, String primaryColor, String secondaryColor,
                   String livery, String category, String imagePath) {
        this.id = id;
        this.name = name;
        this.modelMaker = modelMaker;
        this.releaseInfo = releaseInfo;
        this.price = price;
        this.setSeries = setSeries;
        this.purchaseDate = purchaseDate;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.livery = livery;
        this.category = category;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelMaker() {
        return modelMaker;
    }

    public void setModelMaker(String modelMaker) {
        this.modelMaker = modelMaker;
    }

    public String getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(String releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSetSeries() {
        return setSeries;
    }

    public void setSetSeries(String setSeries) {
        this.setSeries = setSeries;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getLivery() {
        return livery;
    }

    public void setLivery(String livery) {
        this.livery = livery;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
