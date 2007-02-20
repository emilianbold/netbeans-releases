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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class Occurrences {
    
    private static final Logger LOGGER = Logger.getLogger(Occurrences.class.getName());
    
    public static abstract class OccurrenceItem {
        // the faces configuration file
        protected FileObject config;
        protected String newValue;
        protected String oldValue;
        
        public OccurrenceItem(FileObject config, String newValue, String oldValue){
            this.config = config;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
        
        public FileObject getFacesConfig() {
            return config;
        }
        
        public String getElementText(){
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<font color=\"#0000FF\">");
            stringBuffer.append("&lt;").append(getXMLElementName()).append("&gt;</font><b>");
            stringBuffer.append(oldValue).append("</b><font color=\"#0000FF\">&lt;/").append(getXMLElementName());
            stringBuffer.append("&gt;</font>");
            return stringBuffer.toString();
        }
        
        protected abstract String getXMLElementName();
        
        public abstract void performRename();
        public abstract void undoRename();
        public abstract String getRenameMessage();
        
        public abstract void performSafeDelete();
        public abstract void undoSafeDelete();
        public abstract String getSafeDeleteMessage();
        
        public abstract String getWhereUsedMessage();
        
        protected PositionBounds createPosition(int startOffset, int endOffset) {
            try{
                DataObject dataObject = DataObject.find(config);
                if (dataObject instanceof JSFConfigDataObject){
                    CloneableEditorSupport editor
                            = JSFEditorUtilities.findCloneableEditorSupport((JSFConfigDataObject)dataObject);
                    if (editor != null){
                        PositionRef start=editor.createPositionRef(startOffset, Bias.Forward);
                        PositionRef end=editor.createPositionRef(endOffset, Bias.Backward);
                        return new PositionBounds(start,end);
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return null;
        }
        
        public PositionBounds getClassDefinitionPosition() {
            return createPosition(0, 0);
        };
        
        public PositionBounds getElementDefinitionPosition() {
            return createPosition(0, 0);
        };
    }
    
    public static class ManagedBeanClassItem extends OccurrenceItem{
        private final ManagedBean bean;
        
        public ManagedBeanClassItem(FileObject config, ManagedBean bean, String newValue){
            super(config, newValue, bean.getManagedBeanClass());
            this.bean = bean;
        }
        
        protected String getXMLElementName(){
            return "managed-bean-class"; //NOI18N
        }
        
        public void performRename(){
            changeBeanClass(newValue);
        }
        
        public void undoRename(){
            changeBeanClass(oldValue);
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_WhereUsed", //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        public String getRenameMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_Rename",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        public void performSafeDelete() {
            FacesConfig faces = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            Collection<ManagedBean> beans = faces.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (bean.getManagedBeanName().equals(managedBean.getManagedBeanName())){
                    faces.getModel().startTransaction();
                    faces.removeManagedBean(managedBean);
                    faces.getModel().endTransaction();
                    continue;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addManagedBean(bean);
            facesConfig.getModel().endTransaction();
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_SafeDelete",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        private void changeBeanClass(String className){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <ManagedBean> beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (bean.getManagedBeanName().equals(managedBean.getManagedBeanName())){
                    facesConfig.getModel().startTransaction();
                    managedBean.setManagedBeanClass(className);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
                
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            try{
                JSFConfigDataObject dataObject = (JSFConfigDataObject)DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets = JSFEditorUtilities.getManagedBeanDefinition(document, bean.getManagedBeanName());
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            try {
                JSFConfigDataObject dataObject = (JSFConfigDataObject)DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets = JSFEditorUtilities.getManagedBeanDefinition(document, bean.getManagedBeanName());
                position =  createPosition(offsets[0], offsets[1]);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
    }
    
    public static class ConverterClassItem extends OccurrenceItem {
        private final Converter converter;
        
        public ConverterClassItem(FileObject config, Converter converter, String newValue){
            super(config, newValue, converter.getConverterClass());
            this.converter = converter;
        }
        
        protected String getXMLElementName(){
            return "converter-class"; //NOI18N
        }
        
        public void performRename(){
            changeConverterClass(oldValue, newValue);
        }
        
        public void undoRename(){
            changeConverterClass(newValue, oldValue);
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_WhereUsed", getElementText()); //NOI18N
        }
        
        public String getRenameMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_Rename", getElementText()); //NOI18N
        }
        
        public void performSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <Converter> converters = facesConfig.getConverters();
            for (Iterator<Converter> it = converters.iterator(); it.hasNext();) {
                Converter converter = it.next();
                if (oldValue.equals(converter.getConverterClass())){
                    facesConfig.getModel().startTransaction();
                    facesConfig.removeConverter(converter);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addConverter(converter);
            facesConfig.getModel().endTransaction();
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_SafeDelete",  //NOI18N
                    new Object[] { getElementText()});
        }
        
        private void changeConverterClass(String oldClass, String newClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <Converter> converters = facesConfig.getConverters();
            for (Iterator<Converter> it = converters.iterator(); it.hasNext();) {
                Converter converter = it.next();
                if (oldClass.equals(converter.getConverterClass())){
                    converter.getModel().startTransaction();
                    converter.setConverterClass(newClass);
                    converter.getModel().endTransaction();
                    continue;
                }
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            try{
                JSFConfigDataObject dataObject = (JSFConfigDataObject)DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
                
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            try{
                JSFConfigDataObject dataObject = (JSFConfigDataObject)DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
                position =  createPosition(offsets[0], offsets[1]);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
    }
    
    public static class ConverterForClassItem extends OccurrenceItem {
        private final Converter converter;
        
        public ConverterForClassItem(FileObject config, Converter converter, String newValue){
            super(config, newValue, converter.getConverterForClass());
            this.converter = converter;
        }
        
        protected String getXMLElementName(){
            return "converter-for-class"; //NOI18N
        }
        
        public void performRename(){
            changeConverterForClass(oldValue, newValue);
        }
        
        public void undoRename(){
            changeConverterForClass(newValue, oldValue);
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterForClass_WhereUsed", getElementText()); //NOI18N
        }
        
        public String getRenameMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterForClass_Rename", getElementText()); //NOI18N
        }
        
        public void performSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <Converter> converters = facesConfig.getConverters();
            for (Iterator<Converter> it = converters.iterator(); it.hasNext();) {
                Converter converter = it.next();
                if (oldValue.equals(converter.getConverterClass())){
                    facesConfig.getModel().startTransaction();
                    facesConfig.removeConverter(converter);
                    facesConfig.getModel().endTransaction();
                    continue;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addConverter(converter);
            facesConfig.addConverter(converter);
            facesConfig.getModel().endTransaction();
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_SafeDelete",  //NOI18N
                    new Object[] { getElementText()});
        }
        
        private void changeConverterForClass(String oldClass, String newClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List<Converter> converters = facesConfig.getConverters();
            for (Iterator<Converter> it = converters.iterator(); it.hasNext();) {
                Converter converter = it.next();
                if (oldClass.equals(converter.getConverterForClass())){
                    converter.getModel().startTransaction();
                    converter.setConverterForClass(newClass);
                    converter.getModel().endTransaction();
                    continue;
                }
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            try{
                JSFConfigDataObject dataObject = (JSFConfigDataObject)DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            try{
                JSFConfigDataObject dataObject = (JSFConfigDataObject)DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
                position =  createPosition(offsets[0], offsets[1]);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return position;
        };
    }
    
    public static List <OccurrenceItem> getAllOccurrences(WebModule webModule, String oldName, String newName){
        List result = new ArrayList();
        assert webModule != null;
        assert oldName != null;
        assert newName != null;
        
        LOGGER.fine("getAllOccurences("+ webModule.getDocumentBase().getPath() + ", " + oldName + ", " + newName + ")");
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();
                    List <Converter> converters = facesConfig.getConverters();
                    for (Iterator<Converter> it = converters.iterator(); it.hasNext();) {
                        Converter converter = it.next();
                        if (oldName.equals(converter.getConverterClass()))
                            result.add(new ConverterClassItem(configs[i], converter, newName));
                        else if (oldName.equals(converter.getConverterForClass()))
                            result.add(new ConverterForClassItem(configs[i], converter, newName));
                    }
                    List<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                    for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                        ManagedBean managedBean = it.next();
                        if (oldName.equals(managedBean.getManagedBeanClass()))
                            result.add(new ManagedBeanClassItem(configs[i], managedBean, newName));
                        
                    }
                    
                }
            }
        }
        return result;
    }
    
}
