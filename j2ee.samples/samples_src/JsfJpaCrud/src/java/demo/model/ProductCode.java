/*
 * ProductCode.java
 *
 * Created on August 16, 2007, 10:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class ProductCode
 * 
 * @author martinadamek
 */
@Entity
@Table(name = "PRODUCT_CODE")
@NamedQueries( {
        @NamedQuery(name = "ProductCode.findByProdCode", query = "SELECT p FROM ProductCode p WHERE p.prodCode = :prodCode"),
        @NamedQuery(name = "ProductCode.findByDiscountCode", query = "SELECT p FROM ProductCode p WHERE p.discountCode = :discountCode"),
        @NamedQuery(name = "ProductCode.findByDescription", query = "SELECT p FROM ProductCode p WHERE p.description = :description")
    })
public class ProductCode implements Serializable {

    @Id
    @Column(name = "PROD_CODE", nullable = false)
    private String prodCode;

    @Column(name = "DISCOUNT_CODE", nullable = false)
    private char discountCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productCode")
    private Collection<Product> productCollection;
    
    /** Creates a new instance of ProductCode */
    public ProductCode() {
    }

    /**
     * Creates a new instance of ProductCode with the specified values.
     * @param prodCode the prodCode of the ProductCode
     */
    public ProductCode(String prodCode) {
        this.prodCode = prodCode;
    }

    /**
     * Creates a new instance of ProductCode with the specified values.
     * @param prodCode the prodCode of the ProductCode
     * @param discountCode the discountCode of the ProductCode
     */
    public ProductCode(String prodCode, char discountCode) {
        this.prodCode = prodCode;
        this.discountCode = discountCode;
    }

    /**
     * Gets the prodCode of this ProductCode.
     * @return the prodCode
     */
    public String getProdCode() {
        return this.prodCode;
    }

    /**
     * Sets the prodCode of this ProductCode to the specified value.
     * @param prodCode the new prodCode
     */
    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    /**
     * Gets the discountCode of this ProductCode.
     * @return the discountCode
     */
    public char getDiscountCode() {
        return this.discountCode;
    }

    /**
     * Sets the discountCode of this ProductCode to the specified value.
     * @param discountCode the new discountCode
     */
    public void setDiscountCode(char discountCode) {
        this.discountCode = discountCode;
    }

    /**
     * Gets the description of this ProductCode.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this ProductCode to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the productCollection of this ProductCode.
     * @return the productCollection
     */
    public Collection<Product> getProductCollection() {
        return this.productCollection;
    }

    /**
     * Sets the productCollection of this ProductCode to the specified value.
     * @param productCollection the new productCollection
     */
    public void setProductCollection(Collection<Product> productCollection) {
        this.productCollection = productCollection;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.prodCode != null ? this.prodCode.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this ProductCode.  The result is 
     * <code>true</code> if and only if the argument is not null and is a ProductCode object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProductCode)) {
            return false;
        }
        ProductCode other = (ProductCode)object;
        if (this.prodCode != other.prodCode && (this.prodCode == null || !this.prodCode.equals(other.prodCode))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.ProductCode[prodCode=" + prodCode + "]";
    }
    
}
