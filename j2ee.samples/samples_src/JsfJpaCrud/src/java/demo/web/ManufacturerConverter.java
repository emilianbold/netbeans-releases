/*
 * ManufacturerConverter.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.Manufacturer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author martinadamek
 */
public class ManufacturerConverter implements Converter {
    
    /** Creates a new instance of ManufacturerConverter */
    public ManufacturerConverter() {
    }

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        if (string == null) {
            return null;
        }
        Integer id = new Integer(string);
        ManufacturerController controller = (ManufacturerController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "manufacturer");
        
        return controller.findManufacturer(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof Manufacturer) {
            Manufacturer o = (Manufacturer) object;
            return "" + o.getManufacturerId();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: demo.model.Manufacturer");
        }
    }
    
}
