package com.example.DemoCheck.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "productlines")
public class ProductLine {

    @Id
    @Column(name = "productLine", length = 50, nullable = false)
    private String productLine;

    @Column(name = "textDescription", length = 4000)
    private String textDescription;

    @Lob
    @Column(name = "htmlDescription", columnDefinition = "MEDIUMTEXT")
    private String htmlDescription;

    @Lob
    @Column(name = "image", columnDefinition = "MEDIUMBLOB")
    private byte[] image;

    public ProductLine() {
    }

    public ProductLine(String productLine, String textDescription, String htmlDescription, byte[] image) {
        this.productLine = productLine;
        this.textDescription = textDescription;
        this.htmlDescription = htmlDescription;
        this.image = image;
    }

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }

    public String getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public void setHtmlDescription(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}