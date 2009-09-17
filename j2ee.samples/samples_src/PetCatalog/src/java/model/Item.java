/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author caroljmcdonald
 */
@Entity
@Table(name = "ITEM")
@NamedQueries({@NamedQuery(name = "Item.findAll", query = "SELECT i FROM Item i"), @NamedQuery(name = "Item.findByItemid", query = "SELECT i FROM Item i WHERE i.itemid = :itemid"), @NamedQuery(name = "Item.findByProductid", query = "SELECT i FROM Item i WHERE i.productid = :productid"), @NamedQuery(name = "Item.findByName", query = "SELECT i FROM Item i WHERE i.name = :name"), @NamedQuery(name = "Item.findByDescription", query = "SELECT i FROM Item i WHERE i.description = :description"), @NamedQuery(name = "Item.findByImageurl", query = "SELECT i FROM Item i WHERE i.imageurl = :imageurl"), @NamedQuery(name = "Item.findByImagethumburl", query = "SELECT i FROM Item i WHERE i.imagethumburl = :imagethumburl"), @NamedQuery(name = "Item.findByPrice", query = "SELECT i FROM Item i WHERE i.price = :price")})
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ITEMID")
    private String itemid;
    @Basic(optional = false)
    @Column(name = "PRODUCTID")
    private String productid;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "IMAGEURL")
    private String imageurl;
    @Column(name = "IMAGETHUMBURL")
    private String imagethumburl;
    @Basic(optional = false)
    @Column(name = "PRICE")
    private BigDecimal price;

    public Item() {
    }

    public Item(String itemid) {
        this.itemid = itemid;
    }

    public Item(String itemid, String productid, String name, String description, BigDecimal price) {
        this.itemid = itemid;
        this.productid = productid;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getImagethumburl() {
        return imagethumburl;
    }

    public void setImagethumburl(String imagethumburl) {
        this.imagethumburl = imagethumburl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (itemid != null ? itemid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Item)) {
            return false;
        }
        Item other = (Item) object;
        if ((this.itemid == null && other.itemid != null) || (this.itemid != null && !this.itemid.equals(other.itemid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Item[itemid=" + itemid + "]";
    }

}
