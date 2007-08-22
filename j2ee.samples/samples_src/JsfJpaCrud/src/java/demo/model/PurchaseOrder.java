/*
 * PurchaseOrder.java
 *
 * Created on August 16, 2007, 10:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class PurchaseOrder
 * 
 * @author martinadamek
 */
@Entity
@Table(name = "PURCHASE_ORDER")
@NamedQueries( {
        @NamedQuery(name = "PurchaseOrder.findByOrderNum", query = "SELECT p FROM PurchaseOrder p WHERE p.orderNum = :orderNum"),
        @NamedQuery(name = "PurchaseOrder.findByQuantity", query = "SELECT p FROM PurchaseOrder p WHERE p.quantity = :quantity"),
        @NamedQuery(name = "PurchaseOrder.findByShippingCost", query = "SELECT p FROM PurchaseOrder p WHERE p.shippingCost = :shippingCost"),
        @NamedQuery(name = "PurchaseOrder.findBySalesDate", query = "SELECT p FROM PurchaseOrder p WHERE p.salesDate = :salesDate"),
        @NamedQuery(name = "PurchaseOrder.findByShippingDate", query = "SELECT p FROM PurchaseOrder p WHERE p.shippingDate = :shippingDate"),
        @NamedQuery(name = "PurchaseOrder.findByFreightCompany", query = "SELECT p FROM PurchaseOrder p WHERE p.freightCompany = :freightCompany")
    })
public class PurchaseOrder implements Serializable {

    @Id
    @Column(name = "ORDER_NUM", nullable = false)
    private Integer orderNum;

    @Column(name = "QUANTITY")
    private Short quantity;

    @Column(name = "SHIPPING_COST")
    private BigDecimal shippingCost;

    @Column(name = "SALES_DATE")
    @Temporal(TemporalType.DATE)
    private Date salesDate;

    @Column(name = "SHIPPING_DATE")
    @Temporal(TemporalType.DATE)
    private Date shippingDate;

    @Column(name = "FREIGHT_COMPANY")
    private String freightCompany;

    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "CUSTOMER_ID")
    @ManyToOne
    private Customer customerId;

    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    @ManyToOne
    private Product productId;
    
    /** Creates a new instance of PurchaseOrder */
    public PurchaseOrder() {
    }

    /**
     * Creates a new instance of PurchaseOrder with the specified values.
     * @param orderNum the orderNum of the PurchaseOrder
     */
    public PurchaseOrder(Integer orderNum) {
        this.orderNum = orderNum;
    }

    /**
     * Gets the orderNum of this PurchaseOrder.
     * @return the orderNum
     */
    public Integer getOrderNum() {
        return this.orderNum;
    }

    /**
     * Sets the orderNum of this PurchaseOrder to the specified value.
     * @param orderNum the new orderNum
     */
    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    /**
     * Gets the quantity of this PurchaseOrder.
     * @return the quantity
     */
    public Short getQuantity() {
        return this.quantity;
    }

    /**
     * Sets the quantity of this PurchaseOrder to the specified value.
     * @param quantity the new quantity
     */
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the shippingCost of this PurchaseOrder.
     * @return the shippingCost
     */
    public BigDecimal getShippingCost() {
        return this.shippingCost;
    }

    /**
     * Sets the shippingCost of this PurchaseOrder to the specified value.
     * @param shippingCost the new shippingCost
     */
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    /**
     * Gets the salesDate of this PurchaseOrder.
     * @return the salesDate
     */
    public Date getSalesDate() {
        return this.salesDate;
    }

    /**
     * Sets the salesDate of this PurchaseOrder to the specified value.
     * @param salesDate the new salesDate
     */
    public void setSalesDate(Date salesDate) {
        this.salesDate = salesDate;
    }

    /**
     * Gets the shippingDate of this PurchaseOrder.
     * @return the shippingDate
     */
    public Date getShippingDate() {
        return this.shippingDate;
    }

    /**
     * Sets the shippingDate of this PurchaseOrder to the specified value.
     * @param shippingDate the new shippingDate
     */
    public void setShippingDate(Date shippingDate) {
        this.shippingDate = shippingDate;
    }

    /**
     * Gets the freightCompany of this PurchaseOrder.
     * @return the freightCompany
     */
    public String getFreightCompany() {
        return this.freightCompany;
    }

    /**
     * Sets the freightCompany of this PurchaseOrder to the specified value.
     * @param freightCompany the new freightCompany
     */
    public void setFreightCompany(String freightCompany) {
        this.freightCompany = freightCompany;
    }

    /**
     * Gets the customerId of this PurchaseOrder.
     * @return the customerId
     */
    public Customer getCustomerId() {
        return this.customerId;
    }

    /**
     * Sets the customerId of this PurchaseOrder to the specified value.
     * @param customerId the new customerId
     */
    public void setCustomerId(Customer customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the productId of this PurchaseOrder.
     * @return the productId
     */
    public Product getProductId() {
        return this.productId;
    }

    /**
     * Sets the productId of this PurchaseOrder to the specified value.
     * @param productId the new productId
     */
    public void setProductId(Product productId) {
        this.productId = productId;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.orderNum != null ? this.orderNum.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this PurchaseOrder.  The result is 
     * <code>true</code> if and only if the argument is not null and is a PurchaseOrder object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PurchaseOrder)) {
            return false;
        }
        PurchaseOrder other = (PurchaseOrder)object;
        if (this.orderNum != other.orderNum && (this.orderNum == null || !this.orderNum.equals(other.orderNum))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.PurchaseOrder[orderNum=" + orderNum + "]";
    }
    
}
