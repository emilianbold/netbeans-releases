/*
 * MicroMarketConverter.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.MicroMarket;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author martinadamek
 */
public class MicroMarketConverter implements Converter {
    
    /** Creates a new instance of MicroMarketConverter */
    public MicroMarketConverter() {
    }

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        if (string == null) {
            return null;
        }
        String id = string;
        MicroMarketController controller = (MicroMarketController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "microMarket");
        
        return controller.findMicroMarket(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof MicroMarket) {
            MicroMarket o = (MicroMarket) object;
            return "" + o.getZipCode();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: demo.model.MicroMarket");
        }
    }
    
}
