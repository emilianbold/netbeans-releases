/*
 * DiscountCode.java
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author ak199487
 */
@Entity
@Table(name = "DISCOUNT_CODE")
@NamedQueries( {@NamedQuery(name = "DiscountCode.findByDiscountCode", query = "SELECT d FROM DiscountCode d WHERE d.discountCode = :discountCode"), @NamedQuery(name = "DiscountCode.findByRate", query = "SELECT d FROM DiscountCode d WHERE d.rate = :rate")})
public class DiscountCode implements Serializable {

    @Id
    @Column(name = "DISCOUNT_CODE", nullable = false)
    private String discountCode;

    @Column(name = "RATE")
    private BigDecimal rate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "discountCode")
    private java.util.Collection <org.demo.Customer> customer;
    
    /** Creates a new instance of DiscountCode */
    public DiscountCode() {
    }

    public DiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getDiscountCode() {
        return this.discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public java.util.Collection <org.demo.Customer> getCustomer() {
        return this.customer;
    }

    public void setCustomer(java.util.Collection <org.demo.Customer> customer) {
        this.customer = customer;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.discountCode != null ? this.discountCode.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        if (!(object instanceof DiscountCode)) {
            return false;
        }
        DiscountCode other = (DiscountCode)object;
        if (this.discountCode != other.discountCode && (this.discountCode == null || !this.discountCode.equals(other.discountCode))) return false;
        return true;
    }

    public String toString() {
        //TODO change toString() implementation to return a better display name
        return "" + this.discountCode;
    }
    
}
