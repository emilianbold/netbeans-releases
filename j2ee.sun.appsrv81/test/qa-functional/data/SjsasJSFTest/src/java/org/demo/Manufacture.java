/*
 * Manufacture.java
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
@Table(name = "MANUFACTURE")
@NamedQueries( {@NamedQuery(name = "Manufacture.findByManufactureId", query = "SELECT m FROM Manufacture m WHERE m.manufactureId = :manufactureId"), @NamedQuery(name = "Manufacture.findByName", query = "SELECT m FROM Manufacture m WHERE m.name = :name"), @NamedQuery(name = "Manufacture.findByAddressline1", query = "SELECT m FROM Manufacture m WHERE m.addressline1 = :addressline1"), @NamedQuery(name = "Manufacture.findByAddressline2", query = "SELECT m FROM Manufacture m WHERE m.addressline2 = :addressline2"), @NamedQuery(name = "Manufacture.findByCity", query = "SELECT m FROM Manufacture m WHERE m.city = :city"), @NamedQuery(name = "Manufacture.findByState", query = "SELECT m FROM Manufacture m WHERE m.state = :state"), @NamedQuery(name = "Manufacture.findByZip", query = "SELECT m FROM Manufacture m WHERE m.zip = :zip"), @NamedQuery(name = "Manufacture.findByPhone", query = "SELECT m FROM Manufacture m WHERE m.phone = :phone"), @NamedQuery(name = "Manufacture.findByFax", query = "SELECT m FROM Manufacture m WHERE m.fax = :fax"), @NamedQuery(name = "Manufacture.findByEmail", query = "SELECT m FROM Manufacture m WHERE m.email = :email"), @NamedQuery(name = "Manufacture.findByRep", query = "SELECT m FROM Manufacture m WHERE m.rep = :rep")})
public class Manufacture implements Serializable {

    @Id
    @Column(name = "MANUFACTURE_ID", nullable = false)
    private Integer manufactureId;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "manufactureId")
    private java.util.Collection <org.demo.Product> product;
    
    /** Creates a new instance of Manufacture */
    public Manufacture() {
    }

    public Manufacture(Integer manufactureId) {
        this.manufactureId = manufactureId;
    }

    public Integer getManufactureId() {
        return this.manufactureId;
    }

    public void setManufactureId(Integer manufactureId) {
        this.manufactureId = manufactureId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressline1() {
        return this.addressline1;
    }

    public void setAddressline1(String addressline1) {
        this.addressline1 = addressline1;
    }

    public String getAddressline2() {
        return this.addressline2;
    }

    public void setAddressline2(String addressline2) {
        this.addressline2 = addressline2;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return this.zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return this.fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRep() {
        return this.rep;
    }

    public void setRep(String rep) {
        this.rep = rep;
    }

    public java.util.Collection <org.demo.Product> getProduct() {
        return this.product;
    }

    public void setProduct(java.util.Collection <org.demo.Product> product) {
        this.product = product;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.manufactureId != null ? this.manufactureId.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Manufacture)) {
            return false;
        }
        Manufacture other = (Manufacture)object;
        if (this.manufactureId != other.manufactureId && (this.manufactureId == null || !this.manufactureId.equals(other.manufactureId))) return false;
        return true;
    }

    public String toString() {
        //TODO change toString() implementation to return a better display name
        return "" + this.manufactureId;
    }
    
}
