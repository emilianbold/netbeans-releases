/*
 * Product.java
 *
 * Created on May 25, 2006, 11:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.demo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author ak199487
 */
@Entity
@Table(name = "PRODUCT")
@NamedQueries( {@NamedQuery(name = "Product.findByProductId", query = "SELECT p FROM Product p WHERE p.productId = :productId"), @NamedQuery(name = "Product.findByPurchaseCost", query = "SELECT p FROM Product p WHERE p.purchaseCost = :purchaseCost"), @NamedQuery(name = "Product.findByQuantityOnHand", query = "SELECT p FROM Product p WHERE p.quantityOnHand = :quantityOnHand"), @NamedQuery(name = "Product.findByMarkup", query = "SELECT p FROM Product p WHERE p.markup = :markup"), @NamedQuery(name = "Product.findByAvailable", query = "SELECT p FROM Product p WHERE p.available = :available"), @NamedQuery(name = "Product.findByDescription", query = "SELECT p FROM Product p WHERE p.description = :description")})
public class Product implements Serializable {

    @Id
    @Column(name = "PRODUCT_ID", nullable = false)
    private Integer productId;

    @Column(name = "PURCHASE_COST")
    private BigDecimal purchaseCost;

    @Column(name = "QUANTITY_ON_HAND")
    private Integer quantityOnHand;

    @Column(name = "MARKUP")
    private BigDecimal markup;

    @Column(name = "AVAILABLE")
    private String available;

    @Column(name = "DESCRIPTION")
    private String description;

    @JoinColumn(name = "MANUFACTURE_ID")
    @ManyToOne
    private org.demo.Manufacture manufactureId;

    @JoinColumn(name = "PRODUCT_CODE")
    @ManyToOne
    private org.demo.ProductCode productCode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private java.util.Collection <org.demo.Orders> orders;
    
    /** Creates a new instance of Product */
    public Product() {
    }

    public Product(Integer productId) {
        this.productId = productId;
    }

    public Integer getProductId() {
        return this.productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public BigDecimal getPurchaseCost() {
        return this.purchaseCost;
    }

    public void setPurchaseCost(BigDecimal purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public Integer getQuantityOnHand() {
        return this.quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public BigDecimal getMarkup() {
        return this.markup;
    }

    public void setMarkup(BigDecimal markup) {
        this.markup = markup;
    }

    public String getAvailable() {
        return this.available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public org.demo.Manufacture getManufactureId() {
        return this.manufactureId;
    }

    public void setManufactureId(org.demo.Manufacture manufactureId) {
        this.manufactureId = manufactureId;
    }

    public org.demo.ProductCode getProductCode() {
        return this.productCode;
    }

    public void setProductCode(org.demo.ProductCode productCode) {
        this.productCode = productCode;
    }

    public java.util.Collection <org.demo.Orders> getOrders() {
        return this.orders;
    }

    public void setOrders(java.util.Collection <org.demo.Orders> orders) {
        this.orders = orders;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.productId != null ? this.productId.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Product)) {
            return false;
        }
        Product other = (Product)object;
        if (this.productId != other.productId && (this.productId == null || !this.productId.equals(other.productId))) return false;
        return true;
    }

    public String toString() {
        //TODO change toString() implementation to return a better display name
        return "" + this.productId;
    }
    
}
