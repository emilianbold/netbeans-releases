/*
 * Orders.java
 *
 * Created on May 25, 2006, 11:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.demo;

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
 *
 * @author ak199487
 */
@Entity
@Table(name = "ORDERS")
@NamedQueries( {@NamedQuery(name = "Orders.findByOrderNum", query = "SELECT o FROM Orders o WHERE o.orderNum = :orderNum"), @NamedQuery(name = "Orders.findByQuantity", query = "SELECT o FROM Orders o WHERE o.quantity = :quantity"), @NamedQuery(name = "Orders.findByShippingCost", query = "SELECT o FROM Orders o WHERE o.shippingCost = :shippingCost"), @NamedQuery(name = "Orders.findBySalesDate", query = "SELECT o FROM Orders o WHERE o.salesDate = :salesDate"), @NamedQuery(name = "Orders.findByShippingDate", query = "SELECT o FROM Orders o WHERE o.shippingDate = :shippingDate"), @NamedQuery(name = "Orders.findByFreightCompany", query = "SELECT o FROM Orders o WHERE o.freightCompany = :freightCompany")})
public class Orders implements Serializable {

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

    @JoinColumn(name = "CUSTOMER_ID")
    @ManyToOne
    private Customer customerId;

    @JoinColumn(name = "PRODUCT_ID")
    @ManyToOne
    private Product productId;
    
    /** Creates a new instance of Orders */
    public Orders() {
    }

    public Orders(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getOrderNum() {
        return this.orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Short getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getShippingCost() {
        return this.shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Date getSalesDate() {
        return this.salesDate;
    }

    public void setSalesDate(Date salesDate) {
        this.salesDate = salesDate;
    }

    public Date getShippingDate() {
        return this.shippingDate;
    }

    public void setShippingDate(Date shippingDate) {
        this.shippingDate = shippingDate;
    }

    public String getFreightCompany() {
        return this.freightCompany;
    }

    public void setFreightCompany(String freightCompany) {
        this.freightCompany = freightCompany;
    }

    public Customer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(Customer customerId) {
        this.customerId = customerId;
    }

    public Product getProductId() {
        return this.productId;
    }

    public void setProductId(Product productId) {
        this.productId = productId;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.orderNum != null ? this.orderNum.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Orders)) {
            return false;
        }
        Orders other = (Orders)object;
        if (this.orderNum != other.orderNum && (this.orderNum == null || !this.orderNum.equals(other.orderNum))) return false;
        return true;
    }

    public String toString() {
        //TODO change toString() implementation to return a better display name
        return "" + this.orderNum;
    }
    
}
