/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import javax.persistence.Basic;
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
 * @author caroljmcdonald
 */
@Entity
@Table(name = "address")
@NamedQueries({@NamedQuery(name = "Address.findAll", query = "SELECT a FROM Address a"), @NamedQuery(name = "Address.findById", query = "SELECT a FROM Address a WHERE a.id = :id"), @NamedQuery(name = "Address.findByStreet1", query = "SELECT a FROM Address a WHERE a.street1 = :street1"), @NamedQuery(name = "Address.findByStreet2", query = "SELECT a FROM Address a WHERE a.street2 = :street2"), @NamedQuery(name = "Address.findByCity", query = "SELECT a FROM Address a WHERE a.city = :city"), @NamedQuery(name = "Address.findByState", query = "SELECT a FROM Address a WHERE a.state = :state"), @NamedQuery(name = "Address.findByZip", query = "SELECT a FROM Address a WHERE a.zip = :zip"), @NamedQuery(name = "Address.findByLatitude", query = "SELECT a FROM Address a WHERE a.latitude = :latitude"), @NamedQuery(name = "Address.findByLongitude", query = "SELECT a FROM Address a WHERE a.longitude = :longitude")})
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "street1")
    private String street1;
    @Column(name = "street2")
    private String street2;
    @Basic(optional = false)
    @Column(name = "city")
    private String city;
    @Basic(optional = false)
    @Column(name = "state")
    private String state;
    @Basic(optional = false)
    @Column(name = "zip")
    private String zip;
    @Basic(optional = false)
    @Column(name = "latitude")
    private BigDecimal latitude;
    @Basic(optional = false)
    @Column(name = "longitude")
    private BigDecimal longitude;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "address")
    private Collection<Item> itemCollection;

    public Address() {
    }

    public Address(Integer id) {
        this.id = id;
    }

    public Address(Integer id, String street1, String city, String state, String zip, BigDecimal latitude, BigDecimal longitude) {
        this.id = id;
        this.street1 = street1;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public Collection<Item> getItemCollection() {
        return itemCollection;
    }

    public void setItemCollection(Collection<Item> itemCollection) {
        this.itemCollection = itemCollection;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Address)) {
            return false;
        }
        Address other = (Address) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Address[id=" + id + "]";
    }
}
