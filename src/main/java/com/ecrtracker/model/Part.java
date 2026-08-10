package com.ecrtracker.model;

public class Part {

    private Long id;
    private String partNumber;
    private String name;
    private String category;
    private double price;
    private Long linkedEcrId;

    public Part() {
    }

    public Part(Long id, String partNumber, String name,
                String category, double price, Long linkedEcrId) {

        this.id = id;
        this.partNumber = partNumber;
        this.name = name;
        this.category = category;
        this.price = price;
        this.linkedEcrId = linkedEcrId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getLinkedEcrId() {
        return linkedEcrId;
    }

    public void setLinkedEcrId(Long linkedEcrId) {
        this.linkedEcrId = linkedEcrId;
    }

    @Override
    public String toString() {
        return "Part{" +
                "id=" + id +
                ", partNumber='" + partNumber + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", linkedEcrId=" + linkedEcrId +
                '}';
    }
}