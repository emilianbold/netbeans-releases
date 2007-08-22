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
 * Entity class Customer
 * 
 */
@Entity
@Table(name = "CUSTOMER")
@NamedQueries( {
        @NamedQuery(name = "Customer.findByCustomerId", query = "SELECT c FROM Customer c WHERE c.customerId = :customerId"),
        @NamedQuery(name = "Customer.findByZip", query = "SELECT c FROM Customer c WHERE c.zip = :zip"),
        @NamedQuery(name = "Customer.findByName", query = "SELECT c FROM Customer c WHERE c.name = :name"),
        @NamedQuery(name = "Customer.findByAddressline1", query = "SELECT c FROM Customer c WHERE c.addressline1 = :addressline1"),
        @NamedQuery(name = "Customer.findByAddressline2", query = "SELECT c FROM Customer c WHERE c.addressline2 = :addressline2"),
        @NamedQuery(name = "Customer.findByCity", query = "SELECT c FROM Customer c WHERE c.city = :city"),
        @NamedQuery(name = "Customer.findByState", query = "SELECT c FROM Customer c WHERE c.state = :state"),
        @NamedQuery(name = "Customer.findByPhone", query = "SELECT c FROM Customer c WHERE c.phone = :phone"),
        @NamedQuery(name = "Customer.findByFax", query = "SELECT c FROM Customer c WHERE c.fax = :fax"),
        @NamedQuery(name = "Customer.findByEmail", query = "SELECT c FROM Customer c WHERE c.email = :email"),
        @NamedQuery(name = "Customer.findByCreditLimit", query = "SELECT c FROM Customer c WHERE c.creditLimit = :creditLimit")
    })
public class Customer implements Serializable {

    @Id
    @Column(name = "CUSTOMER_ID", nullable = false)
    private Integer customerId;

    @Column(name = "ZIP", nullable = false)
    private String zip;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ADDRESSLINE1")
    private String addressline1;

    @Column(name = "ADDRESSLINE2")
    private String addressline2;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "CREDIT_LIMIT")
    private Integer creditLimit;

    @JoinColumn(name = "DISCOUNT_CODE", referencedColumnName = "DISCOUNT_CODE")
    @ManyToOne
    private DiscountCode discountCode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerId")
    private Collection<PurchaseOrder> purchaseOrderCollection;
    
    /** Creates a new instance of Customer */
    public Customer() {
    }

    /**
     * Creates a new instance of Customer with the specified values.
     * @param customerId the customerId of the Customer
     */
    public Customer(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * Creates a new instance of Customer with the specified values.
     * @param customerId the customerId of the Customer
     * @param zip the zip of the Customer
     */
    public Customer(Integer customerId, String zip) {
        this.customerId = customerId;
        this.zip = zip;
    }

    /**
     * Gets the customerId of this Customer.
     * @return the customerId
     */
    public Integer getCustomerId() {
        return this.customerId;
    }

    /**
     * Sets the customerId of this Customer to the specified value.
     * @param customerId the new customerId
     */
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the zip of this Customer.
     * @return the zip
     */
    public String getZip() {
        return this.zip;
    }

    /**
     * Sets the zip of this Customer to the specified value.
     * @param zip the new zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * Gets the name of this Customer.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this Customer to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the addressline1 of this Customer.
     * @return the addressline1
     */
    public String getAddressline1() {
        return this.addressline1;
    }

    /**
     * Sets the addressline1 of this Customer to the specified value.
     * @param addressline1 the new addressline1
     */
    public void setAddressline1(String addressline1) {
        this.addressline1 = addressline1;
    }

    /**
     * Gets the addressline2 of this Customer.
     * @return the addressline2
     */
    public String getAddressline2() {
        return this.addressline2;
    }

    /**
     * Sets the addressline2 of this Customer to the specified value.
     * @param addressline2 the new addressline2
     */
    public void setAddressline2(String addressline2) {
        this.addressline2 = addressline2;
    }

    /**
     * Gets the city of this Customer.
     * @return the city
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Sets the city of this Customer to the specified value.
     * @param city the new city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the state of this Customer.
     * @return the state
     */
    public String getState() {
        return this.state;
    }

    /**
     * Sets the state of this Customer to the specified value.
     * @param state the new state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the phone of this Customer.
     * @return the phone
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * Sets the phone of this Customer to the specified value.
     * @param phone the new phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the fax of this Customer.
     * @return the fax
     */
    public String getFax() {
        return this.fax;
    }

    /**
     * Sets the fax of this Customer to the specified value.
     * @param fax the new fax
     */
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * Gets the email of this Customer.
     * @return the email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Sets the email of this Customer to the specified value.
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the creditLimit of this Customer.
     * @return the creditLimit
     */
    public Integer getCreditLimit() {
        return this.creditLimit;
    }

    /**
     * Sets the creditLimit of this Customer to the specified value.
     * @param creditLimit the new creditLimit
     */
    public void setCreditLimit(Integer creditLimit) {
        this.creditLimit = creditLimit;
    }

    /**
     * Gets the discountCode of this Customer.
     * @return the discountCode
     */
    public DiscountCode getDiscountCode() {
        return this.discountCode;
    }

    /**
     * Sets the discountCode of this Customer to the specified value.
     * @param discountCode the new discountCode
     */
    public void setDiscountCode(DiscountCode discountCode) {
        this.discountCode = discountCode;
    }

    /**
     * Gets the purchaseOrderCollection of this Customer.
     * @return the purchaseOrderCollection
     */
    public Collection<PurchaseOrder> getPurchaseOrderCollection() {
        return this.purchaseOrderCollection;
    }

    /**
     * Sets the purchaseOrderCollection of this Customer to the specified value.
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
        hash += (this.customerId != null ? this.customerId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this Customer.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Customer object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer)object;
        if (this.customerId != other.customerId && (this.customerId == null || !this.customerId.equals(other.customerId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.Customer[customerId=" + customerId + "]";
    }
    
}
