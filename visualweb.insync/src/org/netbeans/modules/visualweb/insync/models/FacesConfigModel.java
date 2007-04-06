/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.models;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.util.Lookup;


/**
 * Wrapper class for all the faces config files in a project
 */
public class FacesConfigModel extends Model{
    WebModule webModule;
    
    //--------------------------------------------------------------------------------- Construction

    public FacesConfigModel(FacesModelSet owner) {
        super(owner, owner.getProject().getProjectDirectory());
        webModule = WebModule.getWebModule(owner.getProject().getProjectDirectory());
    }
    
    private JSFConfigModel getDefaultFacesConfigModel() {
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        assert Trace.trace("insync.faces.model", "FCM.FacesConfigModel file:" + configFiles[0]);
        return ConfigurationUtils.getConfigModel(configFiles[0], true);
    }
    
    //--------------------------------------------------------------------------------- MBean access

    public ManagedBean[] getManagedBeans() {
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        List<ManagedBean> managedBeans = new ArrayList<ManagedBean>();
        for(FileObject configFile : configs) {
            JSFConfigModel model = ConfigurationUtils.getConfigModel(configFile, true);
            if(!isBusted(model)) {
                managedBeans.addAll(model.getRootComponent().getManagedBeans());
            }
        }

        return managedBeans.toArray(new ManagedBean[0]);
    }

    public ManagedBean getManagedBean(String name) {
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        for(FileObject configFile : configs) {
            JSFConfigModel model = ConfigurationUtils.getConfigModel(configFile, true);
            if(!isBusted(model)) {
                Collection<ManagedBean> managedBeans = model.getRootComponent().getManagedBeans();
                for(ManagedBean managedBean : managedBeans) {
                    if(managedBean.getManagedBeanName().equals(name)) {
                        return managedBean;
                    }
                }
            }
        }
        return null;
    }
    
    public FacesConfig getFacesConfigForManagedBean(String name) {
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        for(FileObject configFile : configs) {
            JSFConfigModel model = ConfigurationUtils.getConfigModel(configFile, true);
            if(!isBusted(model)) {
                FacesConfig facesConfig = model.getRootComponent();
                Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                for(ManagedBean managedBean : managedBeans) {
                    if(managedBean.getManagedBeanName().equals(name)) {
                        return facesConfig;
                    }
                }
            }
        }
        return null;
    }    

    public ManagedBean ensureManagedBean(String name, String className, ManagedBean.Scope scope) {
        ManagedBean mb = getManagedBean(name);
        if (mb == null) {
            mb = addManagedBean(name, className, scope);
            assert Trace.trace("insync.faces.model", "FCM.ensureManagedBean added:" + mb);
        }
        return mb;
    }

    /*
    public void repackageManagedBean(String oldName, String newName, String oldPkg, String newPkg) {
        ManagedBean mb = getManagedBean(oldName);
        if (mb == null) {
            // There is no managed bean by that name;
            return;
        }
        mb.setName(newName);
        String clazz = mb.getClazz();
        clazz = clazz.replaceFirst(oldPkg, newPkg);
        mb.setClazz(clazz);
        // !EAT TODO Why is this not done through dirty / sync ?
        flush();
    }
     * */

    public ManagedBean addManagedBean(String name, String className, ManagedBean.Scope scope) {
        JSFConfigModel model = getDefaultFacesConfigModel();
        if(!isBusted(model)) {
            ManagedBean bean = model.getFactory().createManagedBean();
            bean.setManagedBeanName(name);
            bean.setManagedBeanClass(className);
            bean.setManagedBeanScope(scope);
            model.startTransaction();
            model.getRootComponent().addManagedBean(bean);
            model.endTransaction();
            save(model);
            return bean;
        }
        return null;
    }
     
    public void removeManagedBean(ManagedBean bean) {
        JSFConfigModel model = getDefaultFacesConfigModel();
        if(!isBusted(model)) {
            model.startTransaction();
            model.getRootComponent().removeManagedBean(bean);
            model.endTransaction();
            save(model);
        }
    }
    
    public static String getPackageName(ManagedBean bean) {
        String clazz = bean.getManagedBeanClass();
        int term = clazz.lastIndexOf('.');
        return term >= 0 ? clazz.substring(0, term) : "";
    }
    
    public void save(JSFConfigModel model) {
        DataObject dObj = (DataObject)model.getModelSource().getLookup().lookup(DataObject.class);
        if (dObj != null) {
            SaveCookie saveCookie = (SaveCookie)dObj.getLookup().lookup(SaveCookie.class);
            try {
                saveCookie.save();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public boolean isBusted() {
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        for(FileObject configFile : configs) {
            JSFConfigModel model = ConfigurationUtils.getConfigModel(configFile, true);
            if(isBusted(model)) {
                return true;
            }
        }
        return false;
    }
    
    private URL url;
    {
        try {
        url = new URL("xxxxx");
        }catch( MalformedURLException e){
        }
    }
    private boolean isBusted(JSFConfigModel model) {
        return model.getState() != State.VALID ? true : false;
    }
    
    public ParserAnnotation[] getErrors() {
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        List<ParserAnnotation> parserAnnotationsList = new ArrayList<ParserAnnotation>();
        for(FileObject configFile : configs) {
            JSFConfigModel model = ConfigurationUtils.getConfigModel(configFile, true);
            if(model.getState() == State.NOT_WELL_FORMED) {
                /*
                Collection<? extends Validator> validators = Lookup.getDefault().lookupAll(Validator.class);
                for(Validator validator : validators) {
                    if(validator != null) {
                        Validation validation = new Validation();
                        validator.validate(model, validation, ValidationType.COMPLETE);
                        for( Validator.ResultItem item : validation.getValidationResult()) {
                            ParserAnnotation annotation = new ParserAnnotation(item.getDescription(),
                                    configFile, item.getLineNumber(), item.getColumnNumber());
                            parserAnnotationsList.add(annotation);
                        }
                    }
                }
                 * */
                //Temporarily indicate there is error in config file until we find out a
                //way to get the errors in the config file
                ParserAnnotation annotation = new ParserAnnotation("Error in config file", configFile, 1, 1);
                parserAnnotationsList.add(annotation);
            }
        }        
        return parserAnnotationsList.toArray(ParserAnnotation.EMPTY_ARRAY);
    }    

    //Dummy implementation of abstract methods
    public UndoEvent writeLock(String description) {
        return null;
    }

    public void writeUnlock(UndoEvent event) {
    }

    public boolean isWriteLocked() {
        return false;
    }

    protected void syncImpl() {
    }

    public void saveUnits() {
    }

    public void flushImpl() {
    }

    public void flushNonJavaUnitsImpl() {
    }
}
