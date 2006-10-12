package org.demo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class CustomerConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        CustomerController controller = (CustomerController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "customer");
        Integer id = new Integer(string);
        return controller.findCustomer(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if(object instanceof Customer) {
            Customer o = (Customer) object;
            return "" + o.getCustomerId();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: org.demo.Customer");
        }
    }
}
