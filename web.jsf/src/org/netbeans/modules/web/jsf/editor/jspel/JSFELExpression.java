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

package org.netbeans.modules.web.jsf.editor.jspel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.completion.ELExpression;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JSFELExpression extends ELExpression{
    
    public static final int EL_JSF_BEAN = 100;
    public static final int EL_JSF_RESOURCE_BUNDLE = 101;
    
    private WebModule webModule;
    
    protected String bundle;
    
    public JSFELExpression(WebModule wm, JspSyntaxSupport sup){
        super(sup);
        this.webModule = wm;
    }
    
    @Override
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int value = EL_UNKNOWN;
        
        if (dotIndex > -1){
            String first = expr.substring(0, dotIndex);
            
            // look through all registered managed beans
            List <ManagedBean> beans = JSFBeanCache.getBeans(webModule);
            for (int i = 0; i < beans.size(); i++) {
                if (beans.get(i).getManagedBeanName().equals(first)){
                    value = EL_JSF_BEAN;
                    break;
                }
            }
            
            // look trhough all registered resource bundles
            List <ResourceBundle> bundles = getJSFResourceBundles(webModule);
            for (int i = 0; i < bundles.size(); i++) {
                if (first.equals(bundles.get(i).getVar())) {
                    value = EL_JSF_RESOURCE_BUNDLE;
                    bundle = bundles.get(i).getBaseName();
                    break;
                }
            }
        } else if (dotIndex == -1) {
            value = EL_START;
        }
        return value;
    }
    
    @Override 
    public String getObjectClass(){
        String beanName = extractBeanName();
  
        List <ManagedBean>beans = JSFBeanCache.getBeans(webModule);
        
        for (ManagedBean bean : beans){
            if (beanName.equals(bean.getManagedBeanName())){
                return bean.getManagedBeanClass();
            }
        }
        
        return null;
    }
    
    /**
     * Finds list of all ResourceBundles, which are registered in all
     * JSF configuration files in a web module.
     * @param webModule
     * @return
     */
    public List <ResourceBundle> getJSFResourceBundles(WebModule webModule){
        FileObject[] files = ConfigurationUtils.getFacesConfigFiles(webModule);
        ArrayList <ResourceBundle> bundles = new ArrayList<ResourceBundle>();
        
        for (int i = 0; i < files.length; i++) {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(files[i], true).getRootComponent();
            for (int j = 0; j < facesConfig.getApplications().size(); j++) {
                Collection<ResourceBundle> resourceBundles = facesConfig.getApplications().get(j).getResourceBundles();
                for (Iterator<ResourceBundle> it = resourceBundles.iterator(); it.hasNext();) {
                    bundles.add(it.next());   
                }
            }
        }
        return bundles;
    }
    
    public  List<CompletionItem> getPropertyKeys(String propertyFile, String prefix) {
        ArrayList<CompletionItem> items = new ArrayList<CompletionItem>();
        java.util.ResourceBundle labels = null;
        ClassPath classPath;
        ClassLoader classLoader;
        
        try { // try to find on the source classpath
            classPath = ClassPath.getClassPath(sup.getFileObject(), ClassPath.SOURCE);
            classLoader = classPath.getClassLoader(false);
            labels = java.util.ResourceBundle.getBundle(propertyFile, Locale.getDefault(), classLoader);
        }  catch (MissingResourceException exception) {
            // There is not the property on source classpath - try compile
            try {
                classLoader = ClassPath.getClassPath(sup.getFileObject(), ClassPath.COMPILE).getClassLoader(false);
                labels = java.util.ResourceBundle.getBundle(propertyFile, Locale.getDefault(), classLoader);
            } catch (MissingResourceException exception2) {
                // the propertyr file wasn't find on the compile classpath as well
            }
        }
        
        if (labels != null) {  
            // the property file was found
            Enumeration<String> keys = labels.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                if (key.startsWith(prefix)) {
                    StringBuffer helpText = new StringBuffer();
                    helpText.append(key).append("=<font color='#ce7b00'>"); //NOI18N
                    helpText.append(labels.getString(key)).append("</font>"); //NOI18N
                    items.add(new JSFResultItem.JSFResourceItem(key, helpText.toString()));
                }
            }
        }
        
        return items;
    }
}
