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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf;
//TODO: RETOUCHE
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
//TODO: RETOUCHE
//import org.netbeans.modules.javacore.api.JavaModel;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Petr Pisl
 */


public class JSFInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    /** Creates a new instance of JSFInjectionTargetQueryImplementation */
    public JSFInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller,
                                     TypeElement typeElement) {
        boolean is = false;
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to JSFInjectionTargetQueryImplementation.isInjectionTarget(CompilationController,TypeElement)"); // NOI18N
        }

        // Find the web module, where the class is
        WebModule wm  = WebModule.getWebModule(controller.getFileObject());
        // Is the web modile 2.5 servlet spec or higher?
        if (wm != null && !wm.getJ2eePlatformVersion().equals(WebModule.J2EE_13_LEVEL)
        && !wm.getJ2eePlatformVersion().equals(WebModule.J2EE_14_LEVEL)){
            // Get deployment desctriptor from the web module
            FileObject dd = wm.getDeploymentDescriptor();
            if (dd != null){
                // Get all jsf configurations files
                FileObject[] jsfConfigs = JSFConfigUtilities.getConfiFilesFO(dd);
                try {
                    for (int i = 0; i < jsfConfigs.length && !is ; i++) {
                        DataObject dObject;
                        dObject = DataObject.find(jsfConfigs[i]);
                        if (dObject instanceof JSFConfigDataObject){
                            // Get manage beans from the configuration file
                            ManagedBean [] beans = ((JSFConfigDataObject)dObject).getFacesConfig().getManagedBean();
                            for (int j = 0; j < beans.length && !is; j++) {
                                //TODO: RETOUCHE - need to be tested, when the functionality will be accessible.
                                if (typeElement.getQualifiedName().toString().equals(beans[j].getManagedBeanClass()))
                                    is = true;
                            }
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return is;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller,
                                             TypeElement typeElement) {
        return false;
    }
    
    /** For method return true if:
     *      1) The web module follows 2.5 servlet specification or higher
     *      2) The jc is defined as manage bean in a jsp configuration file. 
     */
    /*public boolean isInjectionTarget(JavaClass jc) {
        boolean is = false;
        if (jc == null) {
            throw new NullPointerException("Passed null to JSFInjectionTargetQueryImplementation.isInjectionTarget(JavaClass)"); // NOI18N
        }
        FileObject classFile = JavaModel.getFileObject(jc.getResource());
        // Find the web module, where the class is
        WebModule wm = WebModule.getWebModule(classFile);
        // Is the web modile 2.5 servlet spec or higher?
        if (wm != null && !wm.getJ2eePlatformVersion().equals(WebModule.J2EE_13_LEVEL)
        && !wm.getJ2eePlatformVersion().equals(WebModule.J2EE_14_LEVEL)){
            // Get deployment desctriptor from the web module
            FileObject dd = wm.getDeploymentDescriptor();
            if (dd != null){
                // Get all jsf configurations files
                FileObject[] jsfConfigs = JSFConfigUtilities.getConfiFilesFO(dd);
                try {
                    for (int i = 0; i < jsfConfigs.length && !is ; i++) {
                        DataObject dObject;
                        dObject = DataObject.find(jsfConfigs[i]);
                        if (dObject instanceof JSFConfigDataObject){
                            // Get manage beans from the configuration file
                            ManagedBean [] beans = ((JSFConfigDataObject)dObject).getFacesConfig().getManagedBean();
                            for (int j = 0; j < beans.length && !is; j++) {
                                System.out.println("name: " +jc.getName());
                                if (jc.getName().equals(beans[j].getManagedBeanClass()))
                                    is = true;
                            }
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return is;
    }

    public boolean isStaticReferenceRequired(JavaClass jc) {
        return false;
    }
    */
}
