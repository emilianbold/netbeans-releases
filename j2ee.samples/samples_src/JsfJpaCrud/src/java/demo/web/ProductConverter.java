/*
 * ProductConverter.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.Product;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author martinadamek
 */
public class ProductConverter implements Converter {
    
    /** Creates a new instance of ProductConverter */
    public ProductConverter() {
    }

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        if (string == null) {
            return null;
        }
        Integer id = new Integer(string);
        ProductController controller = (ProductController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "product");
        
        return controller.findProduct(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof Product) {
            Product o = (Product) object;
            return "" + o.getProductId();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: demo.model.Product");
        }
    }
    
}
