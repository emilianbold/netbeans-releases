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
import jpa.controllers.ProductCodeJpaController;
import jpa.entities.ProductCode;
import jsf.util.JsfUtil;
import jpa.controllers.exceptions.NonexistentEntityException;
import jpa.controllers.exceptions.IllegalOrphanException;
import jsf.util.PagingInfo;

/**
 *
 * @author mbohm
 */
public class ProductCodeController {

    public ProductCodeController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (ProductCodeJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "productCodeJpa");
        pagingInfo = new PagingInfo();
        converter = new ProductCodeConverter();
    }
    private ProductCode productCode = null;
    private List<ProductCode> productCodeItems = null;
    private ProductCodeJpaController jpaController = null;
    private ProductCodeConverter converter = null;
    private PagingInfo pagingInfo = null;

    public PagingInfo getPagingInfo() {
        if (pagingInfo.getItemCount() == -1) {
            pagingInfo.setItemCount(jpaController.getProductCodeCount());
        }
        return pagingInfo;
    }

    public SelectItem[] getProductCodeItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(jpaController.findProductCodeEntities(), false);
    }

    public SelectItem[] getProductCodeItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findProductCodeEntities(), true);
    }

    public ProductCode getProductCode() {
        if (productCode == null) {
            productCode = (ProductCode) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentProductCode", converter, null);
        }
        if (productCode == null) {
            productCode = new ProductCode();
        }
        return productCode;
    }

    public String listSetup() {
        reset(true);
        return "productCode_list";
    }

    public String createSetup() {
        reset(false);
        productCode = new ProductCode();
        return "productCode_create";
    }

    public String create() {
        try {
            jpaController.create(productCode);
            JsfUtil.addSuccessMessage("ProductCode was successfully created.");
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return listSetup();
    }

    public String detailSetup() {
        return scalarSetup("productCode_detail");
    }

    public String editSetup() {
        return scalarSetup("productCode_edit");
    }

    private String scalarSetup(String destination) {
        reset(false);
        productCode = (ProductCode) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentProductCode", converter, null);
        if (productCode == null) {
            String requestProductCodeString = JsfUtil.getRequestParameter("jsfcrud.currentProductCode");
            JsfUtil.addErrorMessage("The productCode with id " + requestProductCodeString + " no longer exists.");
            return relatedOrListOutcome();
        }
        return destination;
    }

    public String edit() {
        String productCodeString = converter.getAsString(FacesContext.getCurrentInstance(), null, productCode);
        String currentProductCodeString = JsfUtil.getRequestParameter("jsfcrud.currentProductCode");
        if (productCodeString == null || productCodeString.length() == 0 || !productCodeString.equals(currentProductCodeString)) {
            String outcome = editSetup();
            if ("productCode_edit".equals(outcome)) {
                JsfUtil.addErrorMessage("Could not edit productCode. Try again.");
            }
            return outcome;
        }
        try {
            jpaController.edit(productCode);
            JsfUtil.addSuccessMessage("ProductCode was successfully updated.");
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
        String idAsString = JsfUtil.getRequestParameter("jsfcrud.currentProductCode");
        String id = idAsString;
        try {
            jpaController.destroy(id);
            JsfUtil.addSuccessMessage("ProductCode was successfully deleted.");
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

    public List<ProductCode> getProductCodeItems() {
        if (productCodeItems == null) {
            getPagingInfo();
            productCodeItems = jpaController.findProductCodeEntities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());
        }
        return productCodeItems;
    }

    public String next() {
        reset(false);
        getPagingInfo().nextPage();
        return "productCode_list";
    }

    public String prev() {
        reset(false);
        getPagingInfo().previousPage();
        return "productCode_list";
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
        productCode = null;
        productCodeItems = null;
        pagingInfo.setItemCount(-1);
        if (resetFirstItem) {
            pagingInfo.setFirstItem(0);
        }
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        ProductCode newProductCode = new ProductCode();
        String newProductCodeString = converter.getAsString(FacesContext.getCurrentInstance(), null, newProductCode);
        String productCodeString = converter.getAsString(FacesContext.getCurrentInstance(), null, productCode);
        if (!newProductCodeString.equals(productCodeString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }

}
