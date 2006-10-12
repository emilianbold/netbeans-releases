package org.demo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class DiscountCodeConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        DiscountCodeController controller = (DiscountCodeController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "discountCode");
        String id = string;
        return controller.findDiscountCode(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if(object instanceof DiscountCode) {
            DiscountCode o = (DiscountCode) object;
            return "" + o.getDiscountCode();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: org.demo.DiscountCode");
        }
    }
}
