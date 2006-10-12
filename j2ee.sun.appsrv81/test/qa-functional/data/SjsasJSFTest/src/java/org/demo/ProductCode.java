/*
 * ProductCode.java
 *
 * Created on May 25, 2006, 11:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.demo;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author ak199487
 */
@Entity
@Table(name = "PRODUCT_CODE")
@NamedQueries( {@NamedQuery(name = "ProductCode.findByProdCode", query = "SELECT p FROM ProductCode p WHERE p.prodCode = :prodCode"), @NamedQuery(name = "ProductCode.findByDiscountCode", query = "SELECT p FROM ProductCode p WHERE p.discountCode = :discountCode"), @NamedQuery(name = "ProductCode.findByDescription", query = "SELECT p FROM ProductCode p WHERE p.description = :description")})
public class ProductCode implements Serializable {

    @Id
    @Column(name = "PROD_CODE", nullable = false)
    private String prodCode;

    @Column(name = "DISCOUNT_CODE", nullable = false)
    private char discountCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productCode")
    private java.util.Collection <org.demo.Product> product;
    
    /** Creates a new instance of ProductCode */
    public ProductCode() {
    }

    public ProductCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public ProductCode(String prodCode, char discountCode) {
        this.prodCode = prodCode;
        this.discountCode = discountCode;
    }

    public String getProdCode() {
        return this.prodCode;
    }

    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public char getDiscountCode() {
        return this.discountCode;
    }

    public void setDiscountCode(char discountCode) {
        this.discountCode = discountCode;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.util.Collection <org.demo.Product> getProduct() {
        return this.product;
    }

    public void setProduct(java.util.Collection <org.demo.Product> product) {
        this.product = product;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.prodCode != null ? this.prodCode.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ProductCode)) {
            return false;
        }
        ProductCode other = (ProductCode)object;
        if (this.prodCode != other.prodCode && (this.prodCode == null || !this.prodCode.equals(other.prodCode))) return false;
        return true;
    }

    public String toString() {
        //TODO change toString() implementation to return a better display name
        return "" + this.prodCode;
    }
    
}
