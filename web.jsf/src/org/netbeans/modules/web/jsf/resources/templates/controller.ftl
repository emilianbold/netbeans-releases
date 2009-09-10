<#if comment>

  TEMPLATE DESCRIPTION:

  This is Java template for 'JSF Pages From Entity Beans' controller class. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    controllerClassName - controller class name (type: String)
    controllerPackageName - controller package name (type: String)
    entityClassName - entity class name without package (type: String)
    entityFullClassName - fully qualified entity class name (type: String)
    ejbClassName - EJB class name (type: String)
    ejbFullClassName - fully qualified EJB class name (type: String)
    managedBeanName - name of managed bean (type: String)
    keyEmbedded - is entity primary key is an embeddable class (type: Boolean)
    keyType - fully qualified class name of entity primary key
    keyBody - body of Controller.Converter.getKey() method
    keyStringBody - body of Controller.Converter.getStringKey() method
    keyGetter - entity getter method returning primaty key instance

  This template is accessible via top level menu Tools->Templates and can
  be found in category JavaServer Faces->JSF from Entity.

</#if>
package ${controllerPackageName};

import ${entityFullClassName};
import ${controllerPackageName}.util.JsfUtil;
import ${controllerPackageName}.util.PaginationHelper;
import ${ejbFullClassName};

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@ManagedBean (name="${managedBeanName}")
@SessionScoped
public class ${controllerClassName} {

    private ${entityClassName} current;
    private DataModel items = null;
    @EJB private ${ejbFullClassName} ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public ${controllerClassName}() {
    }

    public ${entityClassName} getSelected() {
        if (current == null) {
            current = new ${entityClassName}();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ${ejbClassName} getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem()+getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (${entityClassName})getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new ${entityClassName}();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage("${entityClassName} was successfully created.");
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
    }

    public String prepareEdit() {
        current = (${entityClassName})getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("${entityClassName} was successfully updated.");
            return "View";
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
    }

    public String destroy() {
        current = (${entityClassName})getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage("${entityClassName} was successfully deleted.");
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count-1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=${entityClassName}.class)
    public static class ${controllerClassName}Converter implements Converter {
<#if keyEmbedded>

        private static final String SEPARATOR = "<_unique--separator_>";
        private static final String SEPARATOR_ESCAPED = "\\<\\_unique\\-\\-separator\\_\\>";
</#if>

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ${controllerClassName} controller = (${controllerClassName})facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "${managedBeanName}");
            return controller.ejbFacade.find(getKey(value));
        }

        ${keyType} getKey(String value) {
            ${keyType} key;
${keyBody}
            return key;
        }

        String getStringKey(${keyType} value) {
            StringBuffer sb = new StringBuffer();
${keyStringBody}
            return sb.toString();
        }

        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ${entityClassName}) {
                ${entityClassName} o = (${entityClassName}) object;
                return getStringKey(o.${keyGetter}());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+${controllerClassName}.class.getName());
            }
        }

    }

}
