/*
 * DiscountCode.java
 *
 * Created on August 16, 2007, 10:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * Entity class DiscountCode
 * 
 * @author martinadamek
 */
@Entity
@Table(name = "DISCOUNT_CODE")
@NamedQueries( {
        @NamedQuery(name = "DiscountCode.findByDiscountCode", query = "SELECT d FROM DiscountCode d WHERE d.discountCode = :discountCode"),
        @NamedQuery(name = "DiscountCode.findByRate", query = "SELECT d FROM DiscountCode d WHERE d.rate = :rate")
    })
public class DiscountCode implements Serializable {

    @Id
    @Column(name = "DISCOUNT_CODE", nullable = false)
    private String discountCode;

    @Column(name = "RATE")
    private BigDecimal rate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "discountCode")
    private Collection<Customer> customerCollection;
    
    /** Creates a new instance of DiscountCode */
    public DiscountCode() {
    }

    /**
     * Creates a new instance of DiscountCode with the specified values.
     * @param discountCode the discountCode of the DiscountCode
     */
    public DiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    /**
     * Gets the discountCode of this DiscountCode.
     * @return the discountCode
     */
    public String getDiscountCode() {
        return this.discountCode;
    }

    /**
     * Sets the discountCode of this DiscountCode to the specified value.
     * @param discountCode the new discountCode
     */
    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    /**
     * Gets the rate of this DiscountCode.
     * @return the rate
     */
    public BigDecimal getRate() {
        return this.rate;
    }

    /**
     * Sets the rate of this DiscountCode to the specified value.
     * @param rate the new rate
     */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    /**
     * Gets the customerCollection of this DiscountCode.
     * @return the customerCollection
     */
    public Collection<Customer> getCustomerCollection() {
        return this.customerCollection;
    }

    /**
     * Sets the customerCollection of this DiscountCode to the specified value.
     * @param customerCollection the new customerCollection
     */
    public void setCustomerCollection(Collection<Customer> customerCollection) {
        this.customerCollection = customerCollection;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.discountCode != null ? this.discountCode.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this DiscountCode.  The result is 
     * <code>true</code> if and only if the argument is not null and is a DiscountCode object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DiscountCode)) {
            return false;
        }
        DiscountCode other = (DiscountCode)object;
        if (this.discountCode != other.discountCode && (this.discountCode == null || !this.discountCode.equals(other.discountCode))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.DiscountCode[discountCode=" + discountCode + "]";
    }
    
}
