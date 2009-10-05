/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author marekfukala
 */
@ManagedBean(name="Company")
@RequestScoped
public class Company {

    protected String name;
    protected Product primaryProduct;
    protected List<Product> products = new ArrayList<Product>();

    /** Creates a new instance of Company */
    public Company() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrimaryProduct(Product product) {
        this.primaryProduct = product;
    }

    public Product getPrimaryProduct() {
        return this.primaryProduct;
    }

    public Collection<Product> getProducts() {
        return this.products;
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public boolean removeProduct(Product product) {
        return this.products.remove(product);
    }
    
}
