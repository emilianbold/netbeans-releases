/*
 * Copyright (c) 2008, Sun Microsystems, Inc. All rights reserved.
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

package jpa.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
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
 * @author mbohm
 */
@Entity
@Table(name = "PURCHASE_ORDER")
@NamedQueries({@NamedQuery(name = "PurchaseOrder.findAll", query = "SELECT p FROM PurchaseOrder p"), @NamedQuery(name = "PurchaseOrder.findByOrderNum", query = "SELECT p FROM PurchaseOrder p WHERE p.orderNum = :orderNum"), @NamedQuery(name = "PurchaseOrder.findByQuantity", query = "SELECT p FROM PurchaseOrder p WHERE p.quantity = :quantity"), @NamedQuery(name = "PurchaseOrder.findByShippingCost", query = "SELECT p FROM PurchaseOrder p WHERE p.shippingCost = :shippingCost"), @NamedQuery(name = "PurchaseOrder.findBySalesDate", query = "SELECT p FROM PurchaseOrder p WHERE p.salesDate = :salesDate"), @NamedQuery(name = "PurchaseOrder.findByShippingDate", query = "SELECT p FROM PurchaseOrder p WHERE p.shippingDate = :shippingDate"), @NamedQuery(name = "PurchaseOrder.findByFreightCompany", query = "SELECT p FROM PurchaseOrder p WHERE p.freightCompany = :freightCompany")})
public class PurchaseOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ORDER_NUM")
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
    @ManyToOne(optional = false)
    private Customer customerId;
    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    @ManyToOne(optional = false)
    private Product productId;

    public PurchaseOrder() {
    }

    public PurchaseOrder(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Short getQuantity() {
        return quantity;
    }

    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Date getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(Date salesDate) {
        this.salesDate = salesDate;
    }

    public Date getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(Date shippingDate) {
        this.shippingDate = shippingDate;
    }

    public String getFreightCompany() {
        return freightCompany;
    }

    public void setFreightCompany(String freightCompany) {
        this.freightCompany = freightCompany;
    }

    public Customer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Customer customerId) {
        this.customerId = customerId;
    }

    public Product getProductId() {
        return productId;
    }

    public void setProductId(Product productId) {
        this.productId = productId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (orderNum != null ? orderNum.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PurchaseOrder)) {
            return false;
        }
        PurchaseOrder other = (PurchaseOrder) object;
        if ((this.orderNum == null && other.orderNum != null) || (this.orderNum != null && !this.orderNum.equals(other.orderNum))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jpa.entities.PurchaseOrder[orderNum=" + orderNum + "]";
    }

}
