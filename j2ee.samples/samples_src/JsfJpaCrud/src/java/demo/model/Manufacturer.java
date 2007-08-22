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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class Manufacturer
 * 
 */
@Entity
@Table(name = "MANUFACTURER")
@NamedQueries( {
        @NamedQuery(name = "Manufacturer.findByManufacturerId", query = "SELECT m FROM Manufacturer m WHERE m.manufacturerId = :manufacturerId"),
        @NamedQuery(name = "Manufacturer.findByName", query = "SELECT m FROM Manufacturer m WHERE m.name = :name"),
        @NamedQuery(name = "Manufacturer.findByAddressline1", query = "SELECT m FROM Manufacturer m WHERE m.addressline1 = :addressline1"),
        @NamedQuery(name = "Manufacturer.findByAddressline2", query = "SELECT m FROM Manufacturer m WHERE m.addressline2 = :addressline2"),
        @NamedQuery(name = "Manufacturer.findByCity", query = "SELECT m FROM Manufacturer m WHERE m.city = :city"),
        @NamedQuery(name = "Manufacturer.findByState", query = "SELECT m FROM Manufacturer m WHERE m.state = :state"),
        @NamedQuery(name = "Manufacturer.findByZip", query = "SELECT m FROM Manufacturer m WHERE m.zip = :zip"),
        @NamedQuery(name = "Manufacturer.findByPhone", query = "SELECT m FROM Manufacturer m WHERE m.phone = :phone"),
        @NamedQuery(name = "Manufacturer.findByFax", query = "SELECT m FROM Manufacturer m WHERE m.fax = :fax"),
        @NamedQuery(name = "Manufacturer.findByEmail", query = "SELECT m FROM Manufacturer m WHERE m.email = :email"),
        @NamedQuery(name = "Manufacturer.findByRep", query = "SELECT m FROM Manufacturer m WHERE m.rep = :rep")
    })
public class Manufacturer implements Serializable {

    @Id
    @Column(name = "MANUFACTURER_ID", nullable = false)
    private Integer manufacturerId;

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

    @Column(name = "ZIP")
    private String zip;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "REP")
    private String rep;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "manufacturerId")
    private Collection<Product> productCollection;
    
    /** Creates a new instance of Manufacturer */
    public Manufacturer() {
    }

    /**
     * Creates a new instance of Manufacturer with the specified values.
     * @param manufacturerId the manufacturerId of the Manufacturer
     */
    public Manufacturer(Integer manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    /**
     * Gets the manufacturerId of this Manufacturer.
     * @return the manufacturerId
     */
    public Integer getManufacturerId() {
        return this.manufacturerId;
    }

    /**
     * Sets the manufacturerId of this Manufacturer to the specified value.
     * @param manufacturerId the new manufacturerId
     */
    public void setManufacturerId(Integer manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    /**
     * Gets the name of this Manufacturer.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this Manufacturer to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the addressline1 of this Manufacturer.
     * @return the addressline1
     */
    public String getAddressline1() {
        return this.addressline1;
    }

    /**
     * Sets the addressline1 of this Manufacturer to the specified value.
     * @param addressline1 the new addressline1
     */
    public void setAddressline1(String addressline1) {
        this.addressline1 = addressline1;
    }

    /**
     * Gets the addressline2 of this Manufacturer.
     * @return the addressline2
     */
    public String getAddressline2() {
        return this.addressline2;
    }

    /**
     * Sets the addressline2 of this Manufacturer to the specified value.
     * @param addressline2 the new addressline2
     */
    public void setAddressline2(String addressline2) {
        this.addressline2 = addressline2;
    }

    /**
     * Gets the city of this Manufacturer.
     * @return the city
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Sets the city of this Manufacturer to the specified value.
     * @param city the new city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the state of this Manufacturer.
     * @return the state
     */
    public String getState() {
        return this.state;
    }

    /**
     * Sets the state of this Manufacturer to the specified value.
     * @param state the new state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the zip of this Manufacturer.
     * @return the zip
     */
    public String getZip() {
        return this.zip;
    }

    /**
     * Sets the zip of this Manufacturer to the specified value.
     * @param zip the new zip
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * Gets the phone of this Manufacturer.
     * @return the phone
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * Sets the phone of this Manufacturer to the specified value.
     * @param phone the new phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the fax of this Manufacturer.
     * @return the fax
     */
    public String getFax() {
        return this.fax;
    }

    /**
     * Sets the fax of this Manufacturer to the specified value.
     * @param fax the new fax
     */
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * Gets the email of this Manufacturer.
     * @return the email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Sets the email of this Manufacturer to the specified value.
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the rep of this Manufacturer.
     * @return the rep
     */
    public String getRep() {
        return this.rep;
    }

    /**
     * Sets the rep of this Manufacturer to the specified value.
     * @param rep the new rep
     */
    public void setRep(String rep) {
        this.rep = rep;
    }

    /**
     * Gets the productCollection of this Manufacturer.
     * @return the productCollection
     */
    public Collection<Product> getProductCollection() {
        return this.productCollection;
    }

    /**
     * Sets the productCollection of this Manufacturer to the specified value.
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
        hash += (this.manufacturerId != null ? this.manufacturerId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this Manufacturer.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Manufacturer object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Manufacturer)) {
            return false;
        }
        Manufacturer other = (Manufacturer)object;
        if (this.manufacturerId != other.manufacturerId && (this.manufacturerId == null || !this.manufacturerId.equals(other.manufacturerId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.Manufacturer[manufacturerId=" + manufacturerId + "]";
    }
    
}
