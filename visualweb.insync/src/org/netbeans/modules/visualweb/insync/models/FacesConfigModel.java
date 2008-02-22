/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

/**
 * Wrapper class for all the faces config files in a project
 */
public class FacesConfigModel extends Model{
    WebModule webModule;
    
    //XAM AbstractModelFactory caches the XAM Model using WeakHashmap, wherein the value is a weak
    //reference to XAM Model. Therefore it is the responsibility of clients to hold-on to XAM Model
    //if they want to avoid re-creation of Model. Re-creation cost could be significant when insync 
    //calls getManagedBean many hundreds of times(see #125957). This logic indeed should go inside
    //web/jsf but may not be implemented in 6.1 time frame. Temporarily we have a workaround, where-in
    //we hold-on to XAM Models, note that we do not use these Models.
    List<JSFConfigModel> configModels;
    
    //--------------------------------------------------------------------------------- Construction

    public FacesConfigModel(FacesModelSet owner) {
        super(owner, owner.getProject().getProjectDirectory());
        webModule = WebModule.getWebModule(owner.getProject().getProjectDirectory());
    }
    
    /**
     * @return default configuration file for the project
     */ 
    public FileObject getFile() {
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        if(configFiles.length > 0) {
            assert Trace.trace("insync.faces.model", "FCM.FacesConfigModel file:" + configFiles[0]);
            return configFiles[0];
        }
        return null;
    }
    
    private JSFConfigModel getDefaultFacesConfigModel() {
        return ConfigurationUtils.getConfigModel(getFile(), true);
    }
    
    //--------------------------------------------------------------------------------- MBean access

    public ManagedBean[] getManagedBeans() {
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        refreshJSFConfigModelReferences(configs);
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
        refreshJSFConfigModelReferences(configs);
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
        refreshJSFConfigModelReferences(configs);
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

    private void refreshJSFConfigModelReferences(FileObject[] configs) {
    	List<JSFConfigModel> newConfigModels = new ArrayList<JSFConfigModel>();
        try {
            for(FileObject configFile : configs) {
                JSFConfigModel model = ConfigurationUtils.getConfigModel(configFile, true);
                if(model != null) {
                    newConfigModels.add(model);	                
                }
            }
        } finally {
        	configModels = newConfigModels;
        }
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
        //Get the model in which the bean to be removed exists
        JSFConfigModel model = bean.getParent().getModel();
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
            if(saveCookie != null) {
                try {
                    saveCookie.save();
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
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
