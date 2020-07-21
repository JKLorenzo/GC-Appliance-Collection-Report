package com.jklorenzo.collectionreport.Objects;

public class Rank {
    private int collectorIndex;
    private double total;

    public Rank(int collectorIndex, double total) {
        this.collectorIndex = collectorIndex;
        this.total = total;
    }

    public int getCollectorIndex() {
        return collectorIndex;
    }

    public void setCollectorIndex(int collectorIndex) {
        this.collectorIndex = collectorIndex;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
