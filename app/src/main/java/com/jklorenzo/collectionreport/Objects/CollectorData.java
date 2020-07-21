package com.jklorenzo.collectionreport.Objects;

public class CollectorData {
    private String caption, amount;

    public CollectorData(String caption, String amount){
        this.caption = caption;
        this.amount = amount;
    }

    public String getCaption(){
        return caption;
    }

    public void setCaption(String caption){
        this.caption = caption;
    }

    public String getAmount(){
        return amount;
    }

    public void setAmount(String amount){
        this.amount = amount;
    }
}
