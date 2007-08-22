/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
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
 * Entity class Product
 * 
 */
@Entity
@Table(name = "PRODUCT")
@NamedQueries( {
        @NamedQuery(name = "Product.findByProductId", query = "SELECT p FROM Product p WHERE p.productId = :productId"),
        @NamedQuery(name = "Product.findByPurchaseCost", query = "SELECT p FROM Product p WHERE p.purchaseCost = :purchaseCost"),
        @NamedQuery(name = "Product.findByQuantityOnHand", query = "SELECT p FROM Product p WHERE p.quantityOnHand = :quantityOnHand"),
        @NamedQuery(name = "Product.findByMarkup", query = "SELECT p FROM Product p WHERE p.markup = :markup"),
        @NamedQuery(name = "Product.findByAvailable", query = "SELECT p FROM Product p WHERE p.available = :available"),
        @NamedQuery(name = "Product.findByDescription", query = "SELECT p FROM Product p WHERE p.description = :description")
    })
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

    @JoinColumn(name = "MANUFACTURER_ID", referencedColumnName = "MANUFACTURER_ID")
    @ManyToOne
    private Manufacturer manufacturerId;

    @JoinColumn(name = "PRODUCT_CODE", referencedColumnName = "PROD_CODE")
    @ManyToOne
    private ProductCode productCode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private Collection<PurchaseOrder> purchaseOrderCollection;
    
    /** Creates a new instance of Product */
    public Product() {
    }

    /**
     * Creates a new instance of Product with the specified values.
     * @param productId the productId of the Product
     */
    public Product(Integer productId) {
        this.productId = productId;
    }

    /**
     * Gets the productId of this Product.
     * @return the productId
     */
    public Integer getProductId() {
        return this.productId;
    }

    /**
     * Sets the productId of this Product to the specified value.
     * @param productId the new productId
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    /**
     * Gets the purchaseCost of this Product.
     * @return the purchaseCost
     */
    public BigDecimal getPurchaseCost() {
        return this.purchaseCost;
    }

    /**
     * Sets the purchaseCost of this Product to the specified value.
     * @param purchaseCost the new purchaseCost
     */
    public void setPurchaseCost(BigDecimal purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    /**
     * Gets the quantityOnHand of this Product.
     * @return the quantityOnHand
     */
    public Integer getQuantityOnHand() {
        return this.quantityOnHand;
    }

    /**
     * Sets the quantityOnHand of this Product to the specified value.
     * @param quantityOnHand the new quantityOnHand
     */
    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    /**
     * Gets the markup of this Product.
     * @return the markup
     */
    public BigDecimal getMarkup() {
        return this.markup;
    }

    /**
     * Sets the markup of this Product to the specified value.
     * @param markup the new markup
     */
    public void setMarkup(BigDecimal markup) {
        this.markup = markup;
    }

    /**
     * Gets the available of this Product.
     * @return the available
     */
    public String getAvailable() {
        return this.available;
    }

    /**
     * Sets the available of this Product to the specified value.
     * @param available the new available
     */
    public void setAvailable(String available) {
        this.available = available;
    }

    /**
     * Gets the description of this Product.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this Product to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the manufacturerId of this Product.
     * @return the manufacturerId
     */
    public Manufacturer getManufacturerId() {
        return this.manufacturerId;
    }

    /**
     * Sets the manufacturerId of this Product to the specified value.
     * @param manufacturerId the new manufacturerId
     */
    public void setManufacturerId(Manufacturer manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    /**
     * Gets the productCode of this Product.
     * @return the productCode
     */
    public ProductCode getProductCode() {
        return this.productCode;
    }

    /**
     * Sets the productCode of this Product to the specified value.
     * @param productCode the new productCode
     */
    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
    }

    /**
     * Gets the purchaseOrderCollection of this Product.
     * @return the purchaseOrderCollection
     */
    public Collection<PurchaseOrder> getPurchaseOrderCollection() {
        return this.purchaseOrderCollection;
    }

    /**
     * Sets the purchaseOrderCollection of this Product to the specified value.
     * @param purchaseOrderCollection the new purchaseOrderCollection
     */
    public void setPurchaseOrderCollection(Collection<PurchaseOrder> purchaseOrderCollection) {
        this.purchaseOrderCollection = purchaseOrderCollection;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.productId != null ? this.productId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this Product.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Product object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Product)) {
            return false;
        }
        Product other = (Product)object;
        if (this.productId != other.productId && (this.productId == null || !this.productId.equals(other.productId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.Product[productId=" + productId + "]";
    }
    
}
