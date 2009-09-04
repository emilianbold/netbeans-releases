/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
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
 *
 * @author caroljmcdonald
 */
@Entity
@Table(name = "product")
@NamedQueries({@NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p"), @NamedQuery(name = "Product.findById", query = "SELECT p FROM Product p WHERE p.id = :id"), @NamedQuery(name = "Product.findByName", query = "SELECT p FROM Product p WHERE p.name = :name"), @NamedQuery(name = "Product.findByDescription", query = "SELECT p FROM Product p WHERE p.description = :description"), @NamedQuery(name = "Product.findByImageurl", query = "SELECT p FROM Product p WHERE p.imageurl = :imageurl")})
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "description")
    private String description;
    @Column(name = "imageurl")
    private String imageurl;
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ManyToOne
    private Category category;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    private Collection<Item> itemCollection;
    public Product() {
    }

    public Product(Integer id) {
        this.id = id;
    }

    public Product(Integer id,  String name, String description) {
        this.id = id;

        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Product)) {
            return false;
        }
        Product other = (Product) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Product[id=" + id + "]";
    }

}
