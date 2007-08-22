/*
 * DiscountCodeConverter.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.DiscountCode;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author martinadamek
 */
public class DiscountCodeConverter implements Converter {
    
    /** Creates a new instance of DiscountCodeConverter */
    public DiscountCodeConverter() {
    }

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        if (string == null) {
            return null;
        }
        String id = string;
        DiscountCodeController controller = (DiscountCodeController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "discountCode");
        
        return controller.findDiscountCode(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof DiscountCode) {
            DiscountCode o = (DiscountCode) object;
            return "" + o.getDiscountCode();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: demo.model.DiscountCode");
        }
    }
    
}
