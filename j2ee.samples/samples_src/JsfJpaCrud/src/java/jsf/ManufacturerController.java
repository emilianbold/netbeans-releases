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
import jpa.controllers.ManufacturerJpaController;
import jpa.entities.Manufacturer;
import jsf.util.JsfUtil;
import jpa.controllers.exceptions.NonexistentEntityException;
import jpa.controllers.exceptions.IllegalOrphanException;
import jsf.util.PagingInfo;

/**
 *
 * @author mbohm
 */
public class ManufacturerController {

    public ManufacturerController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (ManufacturerJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "manufacturerJpa");
        pagingInfo = new PagingInfo();
        converter = new ManufacturerConverter();
    }
    private Manufacturer manufacturer = null;
    private List<Manufacturer> manufacturerItems = null;
    private ManufacturerJpaController jpaController = null;
    private ManufacturerConverter converter = null;
    private PagingInfo pagingInfo = null;

    public PagingInfo getPagingInfo() {
        if (pagingInfo.getItemCount() == -1) {
            pagingInfo.setItemCount(jpaController.getManufacturerCount());
        }
        return pagingInfo;
    }

    public SelectItem[] getManufacturerItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(jpaController.findManufacturerEntities(), false);
    }

    public SelectItem[] getManufacturerItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findManufacturerEntities(), true);
    }

    public Manufacturer getManufacturer() {
        if (manufacturer == null) {
            manufacturer = (Manufacturer) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentManufacturer", converter, null);
        }
        if (manufacturer == null) {
            manufacturer = new Manufacturer();
        }
        return manufacturer;
    }

    public String listSetup() {
        reset(true);
        return "manufacturer_list";
    }

    public String createSetup() {
        reset(false);
        manufacturer = new Manufacturer();
        return "manufacturer_create";
    }

    public String create() {
        try {
            jpaController.create(manufacturer);
            JsfUtil.addSuccessMessage("Manufacturer was successfully created.");
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, "A persistence error occurred.");
            return null;
        }
        return listSetup();
    }

    public String detailSetup() {
        return scalarSetup("manufacturer_detail");
    }

    public String editSetup() {
        return scalarSetup("manufacturer_edit");
    }

    private String scalarSetup(String destination) {
        reset(false);
        manufacturer = (Manufacturer) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentManufacturer", converter, null);
        if (manufacturer == null) {
            String requestManufacturerString = JsfUtil.getRequestParameter("jsfcrud.currentManufacturer");
            JsfUtil.addErrorMessage("The manufacturer with id " + requestManufacturerString + " no longer exists.");
            return relatedOrListOutcome();
        }
        return destination;
    }

    public String edit() {
        String manufacturerString = converter.getAsString(FacesContext.getCurrentInstance(), null, manufacturer);
        String currentManufacturerString = JsfUtil.getRequestParameter("jsfcrud.currentManufacturer");
        if (manufacturerString == null || manufacturerString.length() == 0 || !manufacturerString.equals(currentManufacturerString)) {
            String outcome = editSetup();
            if ("manufacturer_edit".equals(outcome)) {
                JsfUtil.addErrorMessage("Could not edit manufacturer. Try again.");
            }
            return outcome;
        }
        try {
            jpaController.edit(manufacturer);
            JsfUtil.addSuccessMessage("Manufacturer was successfully updated.");
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
        String idAsString = JsfUtil.getRequestParameter("jsfcrud.currentManufacturer");
        Integer id = new Integer(idAsString);
        try {
            jpaController.destroy(id);
            JsfUtil.addSuccessMessage("Manufacturer was successfully deleted.");
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

    public List<Manufacturer> getManufacturerItems() {
        if (manufacturerItems == null) {
            getPagingInfo();
            manufacturerItems = jpaController.findManufacturerEntities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem());
        }
        return manufacturerItems;
    }

    public String next() {
        reset(false);
        getPagingInfo().nextPage();
        return "manufacturer_list";
    }

    public String prev() {
        reset(false);
        getPagingInfo().previousPage();
        return "manufacturer_list";
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
        manufacturer = null;
        manufacturerItems = null;
        pagingInfo.setItemCount(-1);
        if (resetFirstItem) {
            pagingInfo.setFirstItem(0);
        }
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        Manufacturer newManufacturer = new Manufacturer();
        String newManufacturerString = converter.getAsString(FacesContext.getCurrentInstance(), null, newManufacturer);
        String manufacturerString = converter.getAsString(FacesContext.getCurrentInstance(), null, manufacturer);
        if (!newManufacturerString.equals(manufacturerString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }

}
