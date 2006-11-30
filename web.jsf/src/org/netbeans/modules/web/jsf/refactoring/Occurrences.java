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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.config.model.Converter;
import org.netbeans.modules.web.jsf.config.model.FacesConfig;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
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
        protected JSFConfigDataObject config;
        protected String newValue;
        protected String oldValue;
        
        public OccurrenceItem(JSFConfigDataObject config, String newValue, String oldValue){
            this.config = config;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
        
        public JSFConfigDataObject getConfigDO() {
            return config;
        }
        
        public String getElementText(){
            StringBuffer sb = new StringBuffer();
            sb.append("<font color=\"#0000FF\">");      //NOI18N
            sb.append("&lt;").append(getXMLElementName()).append("&gt;</font><b>");   //NOI18N
            sb.append(oldValue).append("</b><font color=\"#0000FF\">&lt;/").append(getXMLElementName());//NOI18N
            sb.append("&gt;</font>");//NOI18N
            return sb.toString();
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
            CloneableEditorSupport editor = JSFEditorUtilities.findCloneableEditorSupport(config);
            if (editor != null){
                PositionRef start=editor.createPositionRef(startOffset, Bias.Forward);
                PositionRef end=editor.createPositionRef(endOffset, Bias.Backward);
                return new PositionBounds(start,end);
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
        private ManagedBean bean;
        
        public ManagedBeanClassItem(JSFConfigDataObject config, ManagedBean bean, String newValue){
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
            try {
                FacesConfig faces = config.getFacesConfig();
                ManagedBean[] beans = faces.getManagedBean();
                for (int i = 0; i < beans.length; i++) {
                    if (bean.getManagedBeanName().equals(beans[i].getManagedBeanName())){
                        faces.removeManagedBean(beans[i]);
                        continue;
                    }
                }
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public void undoSafeDelete() {
            try {
                FacesConfig faces = config.getFacesConfig();
                faces.addManagedBean(bean);
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_SafeDelete",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        private void changeBeanClass(String className){
            try {
                FacesConfig faces = config.getFacesConfig();
                ManagedBean[] beans = faces.getManagedBean();
                for (int i = 0; i < beans.length; i++) {
                    if (bean.getManagedBeanName().equals(beans[i].getManagedBeanName())){
                        beans[i].setManagedBeanClass(className);
                        continue;
                    }
                }
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            BaseDocument document = JSFEditorUtilities.getBaseDocument(config);
            int [] offsets = JSFEditorUtilities.getManagedBeanDefinition(document, bean.getManagedBeanName());
            try {
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            BaseDocument document = JSFEditorUtilities.getBaseDocument(config);
            int [] offsets = JSFEditorUtilities.getManagedBeanDefinition(document, bean.getManagedBeanName());
            position =  createPosition(offsets[0], offsets[1]);
            return position;
        };
    }
    
    public static class ConverterClassItem extends OccurrenceItem {
        private Converter converter;
        
        public ConverterClassItem(JSFConfigDataObject config, Converter converter, String newValue){
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
            try {
                FacesConfig faces = config.getFacesConfig();
                Converter[] converters = faces.getConverter();
                for (int i = 0; i < converters.length; i++) {
                    if (oldValue.equals(converters[i].getConverterClass())){
                        faces.removeConverter(converters[i]);
                        continue;
                    }
                }
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public void undoSafeDelete() {
            try {
                FacesConfig faces = config.getFacesConfig();
                faces.addConverter(converter);
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_SafeDelete",  //NOI18N
                    new Object[] { getElementText()});
        }
        
        private void changeConverterClass(String oldClass, String newClass){
            try {
                FacesConfig faces = config.getFacesConfig();
                Converter[] converters = faces.getConverter();
                for (int i = 0; i < converters.length; i++) {
                    if (oldClass.equals(converters[i].getConverterClass())){
                        converters[i].setConverterClass(newClass);
                        continue;
                    }
                }
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            BaseDocument document = JSFEditorUtilities.getBaseDocument(config);
            int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
            try {
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            BaseDocument document = JSFEditorUtilities.getBaseDocument(config);
            int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
            position =  createPosition(offsets[0], offsets[1]);
            return position;
        };
    }
    
    public static class ConverterForClassItem extends OccurrenceItem {
        private Converter converter;
        
        public ConverterForClassItem(JSFConfigDataObject config, Converter converter, String newValue){
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
            try {
                FacesConfig faces = config.getFacesConfig();
                Converter[] converters = faces.getConverter();
                for (int i = 0; i < converters.length; i++) {
                    if (oldValue.equals(converters[i].getConverterClass())){
                        faces.removeConverter(converters[i]);
                        continue;
                    }
                }
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public void undoSafeDelete() {
            try {
                FacesConfig faces = config.getFacesConfig();
                faces.addConverter(converter);
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_SafeDelete",  //NOI18N
                    new Object[] { getElementText()});
        }
        
        private void changeConverterForClass(String oldClass, String newClass){
            try {
                FacesConfig faces = config.getFacesConfig();
                Converter[] converters = faces.getConverter();
                for (int i = 0; i < converters.length; i++) {
                    if (oldClass.equals(converters[i].getConverterForClass())){
                        converters[i].setConverterForClass(newClass);
                        continue;
                    }
                }
                config.write(faces);
            } catch (IOException e){
                ErrorManager.getDefault().notify(e);
            }
        }
        
        public PositionBounds getClassDefinitionPosition() {
            PositionBounds position = null;
            BaseDocument document = JSFEditorUtilities.getBaseDocument(config);
            int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
            try {
                String text = document.getText(offsets);
                int offset = offsets[0] + text.indexOf(oldValue);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return position;
        };
        
        public PositionBounds getElementDefinitionPosition() {
            PositionBounds position = null;
            BaseDocument document = JSFEditorUtilities.getBaseDocument(config);
            int [] offsets = JSFEditorUtilities.getConverterDefinition(document, converter.getConverterForClass());
            position =  createPosition(offsets[0], offsets[1]);
            return position;
        };
    }
    
    public static List <OccurrenceItem> getAllOccurrences(WebModule wm, String oldName, String newName){
        List result = new ArrayList();
        assert wm != null;
        assert oldName != null;
        assert newName != null;
        
        LOGGER.fine("getAllOccurences("+ wm.getDocumentBase().getPath() + ", " + oldName + ", " + newName + ")");
        if (wm != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = JSFConfigUtilities.getConfiFilesFO(wm.getDeploymentDescriptor());
            
            if (configs != null){
                try {
                    for (int i = 0; i < configs.length; i++) {
                        DataObject dObject = DataObject.find(configs[i]);
                        if (dObject instanceof JSFConfigDataObject){
                            JSFConfigDataObject configDO = (JSFConfigDataObject) dObject ;
                            FacesConfig config = configDO.getFacesConfig();
                            Converter[] converters = config.getConverter();
                            for (int j = 0; j < converters.length; j++) {
                                if (oldName.equals(converters[j].getConverterClass()))
                                    result.add(new ConverterClassItem(configDO, converters[j], newName));
                                else if (oldName.equals(converters[j].getConverterForClass()))
                                    result.add(new ConverterForClassItem(configDO, converters[j], newName));
                            }
                            ManagedBean[] managedBeans = config.getManagedBean();
                            for (int j = 0; j < managedBeans.length; j++) {
                                if (oldName.equals(managedBeans[j].getManagedBeanClass()))
                                    result.add(new ManagedBeanClassItem(configDO, managedBeans[j], newName));
                            }
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            
        }
        return result;
    }
    
}
