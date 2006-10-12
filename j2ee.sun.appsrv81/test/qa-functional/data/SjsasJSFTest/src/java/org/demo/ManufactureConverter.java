package org.demo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ManufactureConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        ManufactureController controller = (ManufactureController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "manufacture");
        Integer id = new Integer(string);
        return controller.findManufacture(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if(object instanceof Manufacture) {
            Manufacture o = (Manufacture) object;
            return "" + o.getManufactureId();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: org.demo.Manufacture");
        }
    }
}
