/*
 * Copyright (c) 2008, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package jsf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import jpa.controllers.CustomerJpaController;
import jpa.entities.Customer;
import jsf.util.JsfUtil;
import jpa.controllers.exceptions.NonexistentEntityException;
import jpa.controllers.exceptions.IllegalOrphanException;
import jsf.util.PagingInfo;

/**
 *
 * @author mbohm
 */
public class CustomerController {

    public CustomerController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (CustomerJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "customerJpa");
        pagingInfo = new PagingInfo();
        converter = new CustomerConverter();
    }
    private Customer customer = null;
    private List<Customer> customerItems = null;
    private CustomerJpaController jpaController = null;
    private CustomerConverter converter = null;
    private PagingInfo pagingInfo = null;

    public PagingInfo getPagingInfo() {
        if (pagingInfo.getItemCount() == -1) {
            pagingInfo.setItemCount(jpaController.getCustomerCount());
        }
        return pagingInfo;
    }

    public SelectItem[] getCustomerItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(jpaController.findCustomerEntities(), false);
    }

    public SelectItem[] getCustomerItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findCustomerEntities(), true);
    }

    public Customer getCustomer() {
        if (customer == null) {
            customer = (Customer) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentCustomer", converter, null);
        }
        if (customer == null) {
            customer = new Customer();
        }
        return customer;
    }

    public String listSetup() {
        reset(true);
        return "customer_list";
    }

    public String createSetup() {
        reset(false);
        customer = new Customer();
        return "customer_create";
    }

    public String create() {
        try {
            jpaController.create(customer);
            JsfUtil.addSuccessMessage("Customer was successfully created.");
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return listSetup();
    }

    public String detailSetup() {
        return scalarSetup("customer_detail");
    }

    public String editSetup() {
        return scalarSetup("customer_edit");
    }

    private String scalarSetup(String destination) {
        reset(false);
        customer = (Customer) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentCustomer", converter, null);
        if (customer == null) {
            String requestCustomerString = JsfUtil.getRequestParameter("jsfcrud.currentCustomer");
            JsfUtil.addErrorMessage("The customer with id " + requestCustomerString + " no longer exists.");
            return relatedOrListOutcome();
        }
        return destination;
    }

    public String edit() {
        String customerString = converter.getAsString(FacesContext.getCurrentInstance(), null, customer);
        String currentCustomerString = JsfUtil.getRequestParameter("jsfcrud.currentCustomer");
        if (customerString == null || customerString.length() == 0 || !customerString.equals(currentCustomerString)) {
            String outcome = editSetup();
            if ("customer_edit".equals(outcome)) {
                JsfUtil.addErrorMessage("Could not edit customer. Try again.");
            }
            return outcome;
        }
        try {
            jpaController.edit(customer);
            JsfUtil.addSuccessMessage("Customer was successfully updated.");
        } catch (IllegalOrphanException oe) {
            JsfUtil.addErrorMessages(oe.getMessages());
            return null;
        } catch (NonexistentEntityException ne) {
            JsfUtil.addErrorMessage(ne.getLocalizedMessage());
            return listSetup();
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return detailSetup();
    }

    public String destroy() {
        String idAsString = JsfUtil.getRequestParameter("jsfcrud.currentCustomer");
        Integer id = new Integer(idAsString);
        try {
            jpaController.destroy(id);
            JsfUtil.addSuccessMessage("Customer was successfully deleted.");
        } catch (IllegalOrphanException oe) {
            JsfUtil.addErrorMessages(oe.getMessages());
            return null;
        } catch (NonexistentEntityException ne) {
            JsfUtil.addErrorMessage(ne.getLocalizedMessage());
            return relatedOrListOutcome();
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return relatedOrListOutcome();
    }

    private String relatedOrListOutcome() {
        String relatedControllerOutcome = relatedControllerOutcome();
        if (relatedControllerOutcome != null) {
            return relatedControllerOutcome;
        }
        return listSetup();
    }

    public List<Customer> getCustomerItems() {
        if (customerItems == null) {
            getPagingInfo();
            customerItems = jpaController.findCustomerEntities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());
        }
        return customerItems;
    }

    public String next() {
        reset(false);
        getPagingInfo().nextPage();
        return "customer_list";
    }

    public String prev() {
        reset(false);
        getPagingInfo().previousPage();
        return "customer_list";
    }

    private String relatedControllerOutcome() {
        String relatedControllerString = JsfUtil.getRequestParameter("jsfcrud.relatedController");
        String relatedControllerTypeString = JsfUtil.getRequestParameter("jsfcrud.relatedControllerType");
        if (relatedControllerString != null && relatedControllerTypeString != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            Object relatedController = context.getApplication().getELResolver().getValue(context.getELContext(), null, relatedControllerString);
            try {
                Class<?> relatedControllerType = Class.forName(relatedControllerTypeString);
                Method detailSetupMethod = relatedControllerType.getMethod("detailSetup");
                return (String) detailSetupMethod.invoke(relatedController);
            } catch (ClassNotFoundException e) {
                throw new FacesException(e);
            } catch (NoSuchMethodException e) {
                throw new FacesException(e);
            } catch (IllegalAccessException e) {
                throw new FacesException(e);
            } catch (InvocationTargetException e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    private void reset(boolean resetFirstItem) {
        customer = null;
        customerItems = null;
        pagingInfo.setItemCount(-1);
        if (resetFirstItem) {
            pagingInfo.setFirstItem(0);
        }
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        Customer newCustomer = new Customer();
        String newCustomerString = converter.getAsString(FacesContext.getCurrentInstance(), null, newCustomer);
        String customerString = converter.getAsString(FacesContext.getCurrentInstance(), null, customer);
        if (!newCustomerString.equals(customerString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }

}
