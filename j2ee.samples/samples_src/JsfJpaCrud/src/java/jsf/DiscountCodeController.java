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
import jpa.controllers.DiscountCodeJpaController;
import jpa.entities.DiscountCode;
import jsf.util.JsfUtil;
import jpa.controllers.exceptions.NonexistentEntityException;
import jpa.controllers.exceptions.IllegalOrphanException;
import jsf.util.PagingInfo;

/**
 *
 * @author mbohm
 */
public class DiscountCodeController {

    public DiscountCodeController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (DiscountCodeJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "discountCodeJpa");
        pagingInfo = new PagingInfo();
        converter = new DiscountCodeConverter();
    }
    private DiscountCode discountCode = null;
    private List<DiscountCode> discountCodeItems = null;
    private DiscountCodeJpaController jpaController = null;
    private DiscountCodeConverter converter = null;
    private PagingInfo pagingInfo = null;

    public PagingInfo getPagingInfo() {
        if (pagingInfo.getItemCount() == -1) {
            pagingInfo.setItemCount(jpaController.getDiscountCodeCount());
        }
        return pagingInfo;
    }

    public SelectItem[] getDiscountCodeItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(jpaController.findDiscountCodeEntities(), false);
    }

    public SelectItem[] getDiscountCodeItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findDiscountCodeEntities(), true);
    }

    public DiscountCode getDiscountCode() {
        if (discountCode == null) {
            discountCode = (DiscountCode) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentDiscountCode", converter, null);
        }
        if (discountCode == null) {
            discountCode = new DiscountCode();
        }
        return discountCode;
    }

    public String listSetup() {
        reset(true);
        return "discountCode_list";
    }

    public String createSetup() {
        reset(false);
        discountCode = new DiscountCode();
        return "discountCode_create";
    }

    public String create() {
        try {
            jpaController.create(discountCode);
            JsfUtil.addSuccessMessage("DiscountCode was successfully created.");
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return listSetup();
    }

    public String detailSetup() {
        return scalarSetup("discountCode_detail");
    }

    public String editSetup() {
        return scalarSetup("discountCode_edit");
    }

    private String scalarSetup(String destination) {
        reset(false);
        discountCode = (DiscountCode) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentDiscountCode", converter, null);
        if (discountCode == null) {
            String requestDiscountCodeString = JsfUtil.getRequestParameter("jsfcrud.currentDiscountCode");
            JsfUtil.addErrorMessage("The discountCode with id " + requestDiscountCodeString + " no longer exists.");
            return relatedOrListOutcome();
        }
        return destination;
    }

    public String edit() {
        String discountCodeString = converter.getAsString(FacesContext.getCurrentInstance(), null, discountCode);
        String currentDiscountCodeString = JsfUtil.getRequestParameter("jsfcrud.currentDiscountCode");
        if (discountCodeString == null || discountCodeString.length() == 0 || !discountCodeString.equals(currentDiscountCodeString)) {
            String outcome = editSetup();
            if ("discountCode_edit".equals(outcome)) {
                JsfUtil.addErrorMessage("Could not edit discountCode. Try again.");
            }
            return outcome;
        }
        try {
            jpaController.edit(discountCode);
            JsfUtil.addSuccessMessage("DiscountCode was successfully updated.");
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
        String idAsString = JsfUtil.getRequestParameter("jsfcrud.currentDiscountCode");
        Character id = new Character(idAsString.charAt(0));
        try {
            jpaController.destroy(id);
            JsfUtil.addSuccessMessage("DiscountCode was successfully deleted.");
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

    public List<DiscountCode> getDiscountCodeItems() {
        if (discountCodeItems == null) {
            getPagingInfo();
            discountCodeItems = jpaController.findDiscountCodeEntities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());
        }
        return discountCodeItems;
    }

    public String next() {
        reset(false);
        getPagingInfo().nextPage();
        return "discountCode_list";
    }

    public String prev() {
        reset(false);
        getPagingInfo().previousPage();
        return "discountCode_list";
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
        discountCode = null;
        discountCodeItems = null;
        pagingInfo.setItemCount(-1);
        if (resetFirstItem) {
            pagingInfo.setFirstItem(0);
        }
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        DiscountCode newDiscountCode = new DiscountCode();
        String newDiscountCodeString = converter.getAsString(FacesContext.getCurrentInstance(), null, newDiscountCode);
        String discountCodeString = converter.getAsString(FacesContext.getCurrentInstance(), null, discountCode);
        if (!newDiscountCodeString.equals(discountCodeString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }

}
