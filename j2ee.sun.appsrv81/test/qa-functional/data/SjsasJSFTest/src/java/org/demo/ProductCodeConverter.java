package org.demo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ProductCodeConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        ProductCodeController controller = (ProductCodeController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "productCode");
        String id = string;
        return controller.findProductCode(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if(object instanceof ProductCode) {
            ProductCode o = (ProductCode) object;
            return "" + o.getProdCode();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: org.demo.ProductCode");
        }
    }
}
