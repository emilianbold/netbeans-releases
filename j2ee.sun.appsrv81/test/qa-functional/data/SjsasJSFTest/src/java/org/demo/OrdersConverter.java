package org.demo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class OrdersConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uIComponent, String string) {
        OrdersController controller = (OrdersController) facesContext.getApplication().getELResolver().getValue(
            facesContext.getELContext(), null, "orders");
        Integer id = new Integer(string);
        return controller.findOrders(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent uIComponent, Object object) {
        if(object instanceof Orders) {
            Orders o = (Orders) object;
            return "" + o.getOrderNum();
        } else {
            throw new IllegalArgumentException("object:" + object + " of type:" + object.getClass().getName() + "; expected type: org.demo.Orders");
        }
    }
}
