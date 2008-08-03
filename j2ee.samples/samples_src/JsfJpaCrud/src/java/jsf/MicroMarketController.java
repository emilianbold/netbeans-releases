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
import jpa.controllers.MicroMarketJpaController;
import jpa.entities.MicroMarket;
import jsf.util.JsfUtil;
import jpa.controllers.exceptions.NonexistentEntityException;
import jsf.util.PagingInfo;

/**
 *
 * @author mbohm
 */
public class MicroMarketController {

    public MicroMarketController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (MicroMarketJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "microMarketJpa");
        pagingInfo = new PagingInfo();
        converter = new MicroMarketConverter();
    }
    private MicroMarket microMarket = null;
    private List<MicroMarket> microMarketItems = null;
    private MicroMarketJpaController jpaController = null;
    private MicroMarketConverter converter = null;
    private PagingInfo pagingInfo = null;

    public PagingInfo getPagingInfo() {
        if (pagingInfo.getItemCount() == -1) {
            pagingInfo.setItemCount(jpaController.getMicroMarketCount());
        }
        return pagingInfo;
    }

    public SelectItem[] getMicroMarketItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(jpaController.findMicroMarketEntities(), false);
    }

    public SelectItem[] getMicroMarketItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findMicroMarketEntities(), true);
    }

    public MicroMarket getMicroMarket() {
        if (microMarket == null) {
            microMarket = (MicroMarket) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentMicroMarket", converter, null);
        }
        if (microMarket == null) {
            microMarket = new MicroMarket();
        }
        return microMarket;
    }

    public String listSetup() {
        reset(true);
        return "microMarket_list";
    }

    public String createSetup() {
        reset(false);
        microMarket = new MicroMarket();
        return "microMarket_create";
    }

    public String create() {
        try {
            jpaController.create(microMarket);
            JsfUtil.addSuccessMessage("MicroMarket was successfully created.");
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return listSetup();
    }

    public String detailSetup() {
        return scalarSetup("microMarket_detail");
    }

    public String editSetup() {
        return scalarSetup("microMarket_edit");
    }

    private String scalarSetup(String destination) {
        reset(false);
        microMarket = (MicroMarket) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentMicroMarket", converter, null);
        if (microMarket == null) {
            String requestMicroMarketString = JsfUtil.getRequestParameter("jsfcrud.currentMicroMarket");
            JsfUtil.addErrorMessage("The microMarket with id " + requestMicroMarketString + " no longer exists.");
            return relatedOrListOutcome();
        }
        return destination;
    }

    public String edit() {
        String microMarketString = converter.getAsString(FacesContext.getCurrentInstance(), null, microMarket);
        String currentMicroMarketString = JsfUtil.getRequestParameter("jsfcrud.currentMicroMarket");
        if (microMarketString == null || microMarketString.length() == 0 || !microMarketString.equals(currentMicroMarketString)) {
            String outcome = editSetup();
            if ("microMarket_edit".equals(outcome)) {
                JsfUtil.addErrorMessage("Could not edit microMarket. Try again.");
            }
            return outcome;
        }
        try {
            jpaController.edit(microMarket);
            JsfUtil.addSuccessMessage("MicroMarket was successfully updated.");
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
        String idAsString = JsfUtil.getRequestParameter("jsfcrud.currentMicroMarket");
        String id = idAsString;
        try {
            jpaController.destroy(id);
            JsfUtil.addSuccessMessage("MicroMarket was successfully deleted.");
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

    public List<MicroMarket> getMicroMarketItems() {
        if (microMarketItems == null) {
            getPagingInfo();
            microMarketItems = jpaController.findMicroMarketEntities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());
        }
        return microMarketItems;
    }

    public String next() {
        reset(false);
        getPagingInfo().nextPage();
        return "microMarket_list";
    }

    public String prev() {
        reset(false);
        getPagingInfo().previousPage();
        return "microMarket_list";
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
        microMarket = null;
        microMarketItems = null;
        pagingInfo.setItemCount(-1);
        if (resetFirstItem) {
            pagingInfo.setFirstItem(0);
        }
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        MicroMarket newMicroMarket = new MicroMarket();
        String newMicroMarketString = converter.getAsString(FacesContext.getCurrentInstance(), null, newMicroMarket);
        String microMarketString = converter.getAsString(FacesContext.getCurrentInstance(), null, microMarket);
        if (!newMicroMarketString.equals(microMarketString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }

}
