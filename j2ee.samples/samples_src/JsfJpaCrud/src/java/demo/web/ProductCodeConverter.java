/*
 * ProductCodeConverter.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.ProductCode;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author martinadamek
 */
public class ProductCodeConverter implements Converter {
    
    /** Creates a new instance of ProductCodeConverter */
    public ProductCodeConverter() {
    }

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        if (string == null) {
            return null;
        }
        String id = string;
        ProductCodeController controller = (ProductCodeController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "productCode");
        
        return controller.findProductCode(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof ProductCode) {
            ProductCode o = (ProductCode) object;
            return "" + o.getProdCode();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: demo.model.ProductCode");
        }
    }
    
}
