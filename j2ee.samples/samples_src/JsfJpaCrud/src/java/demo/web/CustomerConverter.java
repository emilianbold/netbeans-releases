/*
 * CustomerConverter.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.Customer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author martinadamek
 */
public class CustomerConverter implements Converter {
    
    /** Creates a new instance of CustomerConverter */
    public CustomerConverter() {
    }

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        if (string == null) {
            return null;
        }
        Integer id = new Integer(string);
        CustomerController controller = (CustomerController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "customer");
        
        return controller.findCustomer(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof Customer) {
            Customer o = (Customer) object;
            return "" + o.getCustomerId();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: demo.model.Customer");
        }
    }
    
}
