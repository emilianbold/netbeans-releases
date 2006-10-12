package org.demo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ProductConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        ProductController controller = (ProductController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "product");
        Integer id = new Integer(string);
        return controller.findProduct(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if(object instanceof Product) {
            Product o = (Product) object;
            return "" + o.getProductId();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: org.demo.Product");
        }
    }
}
