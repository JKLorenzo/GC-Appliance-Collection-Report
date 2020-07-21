package com.jklorenzo.collectionreport.Objects;

public class CollectorCaption {
    private String name, ranking, caption1, caption2, amount1, amount2;
    private int image;

    public CollectorCaption(String name, String ranking, String caption1, String amount1, String caption2, String amount2, int image){
        this.name = name;
        this.ranking = ranking;
        this.caption1 = caption1;
        this.amount1 = amount1;
        this.caption2 = caption2;
        this.amount2 = amount2;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRanking(){return ranking;}

    public void setRanking(String name) {
        this.ranking = ranking;
    }

    public String getCaption1() {
        return caption1;
    }

    public void setCaption1(String caption1) {
        this.caption1 = caption1;
    }

    public String getCaption2() {
        return caption2;
    }

    public void setCaption2(String caption2) {
        this.caption2 = caption2;
    }

    public String getAmount1() {
        return amount1;
    }

    public void setAmount1(String amount1) {
        this.amount1 = amount1;
    }

    public String getAmount2() {
        return amount2;
    }

    public void setAmount2(String amount2) {
        this.amount2 = amount2;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
