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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
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
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.w3c.dom.NodeList;

/**
 * These classes represents an occurence in a faces configuration file.
 * @author Petr Pisl
 */
public class Occurrences {
    
    private static final Logger LOGGER = Logger.getLogger(Occurrences.class.getName());
    
    public static abstract class OccurrenceItem {
        // the faces configuration file
        protected FileObject config;
        
        protected String oldValue;
        
        protected String newValue;
        
        
        public OccurrenceItem(FileObject config, String newValue, String oldValue){
            this.config = config;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
        
        public String getNewValue(){
            return newValue;
        }
        
        public String getOldValue(){
            return oldValue;
        }
        
        public FileObject getFacesConfig() {
            return config;
        }
        
        public String getElementText(){
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<font color=\"#0000FF\">");    //NOI18N
            stringBuffer.append("&lt;").append(getXMLElementName()).append("&gt;</font><b>"); //NOI18N
            stringBuffer.append(oldValue).append("</b><font color=\"#0000FF\">&lt;/").append(getXMLElementName()); //NOI18N
            stringBuffer.append("&gt;</font>");     //NOI18N
            return stringBuffer.toString();
        }
        
        protected abstract String getXMLElementName();
        
        //for changes like rename, move, change package...
        public abstract void performChange();
        public abstract void undoChange();
        public abstract String getChangeMessage();
        
        
        public String getRenamePackageMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_Package_Rename",  //NOI18N
                    new Object[] {getElementText()});
        }
        
        // save delete
        public abstract void performSafeDelete();
        public abstract void undoSafeDelete();
        public abstract String getSafeDeleteMessage();
        
        // usages 
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
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
            return null;
        }
        
        public abstract PositionBounds getChangePosition();
        
        
    }
    
    /**
     * Implementation for ManagedBean
     */
    public static class ManagedBeanClassItem extends OccurrenceItem{
        private final ManagedBean bean;
        // needed for undo
        private final ManagedBean copy;
        
        public ManagedBeanClassItem(FileObject config, ManagedBean bean, String newValue){
            super(config, newValue, bean.getManagedBeanClass());
            this.bean = bean;
            this.copy = (ManagedBean) bean.copy(bean.getParent());
        }
        
        protected String getXMLElementName(){
            return "managed-bean-class"; //NOI18N
        }
        
        public void performChange(){
            changeBeanClass(oldValue, newValue);
        }
        
        public void undoChange(){
            changeBeanClass(newValue, oldValue);
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_WhereUsed", //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        public String getChangeMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_Rename",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        public void performSafeDelete() {
            FacesConfig faces = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            Collection<ManagedBean> beans = faces.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (oldValue.equals(managedBean.getManagedBeanClass())){
                    faces.getModel().startTransaction();
                    faces.removeManagedBean(managedBean);
                    faces.getModel().endTransaction();
                    break;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addManagedBean(copy);
            facesConfig.getModel().endTransaction();
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_ManagedBeanClass_SafeDelete",  //NOI18N
                    new Object[] { bean.getManagedBeanName(), getElementText()});
        }
        
        private void changeBeanClass(String oldClass, String newClass){
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            List <ManagedBean> beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if (oldClass.equals(managedBean.getManagedBeanClass())){
                    facesConfig.getModel().startTransaction();
                    managedBean.setManagedBeanClass(newClass);
                    facesConfig.getModel().endTransaction();
                    break;
                }
            }
        }
        
        public PositionBounds getChangePosition() {
            PositionBounds position = null;
            try{
                DataObject dataObject = DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets;
                if (bean.getManagedBeanName() != null) {
                    offsets = JSFEditorUtilities.getManagedBeanDefinition(document, "managed-bean-name", bean.getManagedBeanName()); //NOI18N
                } else {
                    offsets = JSFEditorUtilities.getManagedBeanDefinition(document, "managed-bean-class", bean.getManagedBeanClass()); //NOI18N
                }
                String text = document.getText(offsets);
                int offset = text.indexOf(getXMLElementName());
                offset = offsets[0] + text.indexOf(oldValue, offset);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            } catch (DataObjectNotFoundException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            }
            return position;
        };
        
    }
    
    
    public static class ConverterClassItem extends OccurrenceItem {
        private final Converter converter;
        // needed for undo
        private final Converter copy;
        
        public ConverterClassItem(FileObject config, Converter converter, String newValue){
            super(config, newValue, converter.getConverterClass());
            this.converter = converter;
            this.copy = (Converter) converter.copy(converter.getParent());
        }
        
        protected String getXMLElementName(){
            return "converter-class"; //NOI18N
        }
        
        public void performChange(){
            changeConverterClass(oldValue, newValue);
        }
        
        public void undoChange(){
            changeConverterClass(newValue, oldValue);
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_WhereUsed", //NOI18N
                    new Object[] { converter.getConverterId(), getElementText()});
        }
        
        public String getChangeMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterClass_Rename", //NOI18N
                    new Object[] { converter.getConverterId(), getElementText()});
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
                    break;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addConverter(copy);
            facesConfig.getModel().endTransaction();
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_Converter_SafeDelete", //NOI18N
                    new Object[] { converter.getConverterId(), getElementText()});
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
                    break;
                }
            }
        }
        
        public PositionBounds getChangePosition() {
            PositionBounds position = null;
            try{
                DataObject dataObject = DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets;
                if (converter.getConverterId() != null) {
                    offsets = JSFEditorUtilities.getConverterDefinition(document, "converter-id", converter.getConverterId()); //NOI18N
                } else {
                    offsets = JSFEditorUtilities.getConverterDefinition(document, "converter-class", converter.getConverterClass()); //NOI18N
                }
                String text = document.getText(offsets);
                int offset = text.indexOf(getXMLElementName());
                offset = offsets[0] + text.indexOf(oldValue, offset);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            } catch (DataObjectNotFoundException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            }
            return position;
        };
        
    }
    
    public static class ConverterForClassItem extends OccurrenceItem {
        private final Converter converter;
        //needed for undo
        private final Converter copy;

        public ConverterForClassItem(FileObject config, Converter converter, String newValue){
            super(config, newValue, converter.getConverterForClass());
            this.converter = converter;
            this.copy = (Converter) converter.copy(converter.getParent());

        }
        
        protected String getXMLElementName(){
            return "converter-for-class"; //NOI18N
        }
        
        public void performChange(){
            changeConverterForClass(oldValue, newValue);
        }
        
        public void undoChange(){
            changeConverterForClass(newValue, oldValue);
        }
        
        public String getWhereUsedMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterForClass_WhereUsed", //NOI18N
                    new Object[] { converter.getConverterId(), getElementText()});
        }
        
        public String getChangeMessage(){
            return NbBundle.getMessage(Occurrences.class, "MSG_ConverterForClass_Rename", //NOI18N
                    new Object[] { converter.getConverterId(), getElementText()});
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
                    break;
                }
            }
        }
        
        public void undoSafeDelete() {
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config, true).getRootComponent();
            facesConfig.getModel().startTransaction();
            facesConfig.addConverter(copy);
            facesConfig.getModel().endTransaction();
        }
        
        public String getSafeDeleteMessage() {
            return NbBundle.getMessage(Occurrences.class, "MSG_Converter_SafeDelete", //NOI18N
                    new Object[] { converter.getConverterId(), getElementText()});
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
                    break;
                }
            }
        }
        
        public PositionBounds getChangePosition() {
            PositionBounds position = null;
            try{
                DataObject dataObject = DataObject.find(config);
                BaseDocument document = JSFEditorUtilities.getBaseDocument(dataObject);
                int [] offsets;
                if (converter.getConverterId() != null) {
                    offsets = JSFEditorUtilities.getConverterDefinition(document, "converter-id", converter.getConverterId()); //NOI18N
                } else {
                    offsets = JSFEditorUtilities.getConverterDefinition(document, "converter-for-class", converter.getConverterForClass()); //NOI18N
                }
                String text = document.getText(offsets);
                int offset = text.indexOf(getXMLElementName());
                offset = offsets[0] + text.indexOf(oldValue, offset);
                position =  createPosition(offset, offset + oldValue.length());
            } catch (BadLocationException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            } catch (DataObjectNotFoundException exception) {
                LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            }
            return position;
        };
    }
    
    public static List <OccurrenceItem> getAllOccurrences(WebModule webModule, String oldName, String newName){
        List result = new ArrayList();
        assert webModule != null;
        assert oldName != null;
        
        LOGGER.fine("getAllOccurences("+ webModule.getDocumentBase().getPath() + ", " + oldName + ", " + newName + ")"); //NOI18N
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
    
    public static List <OccurrenceItem> getPackageOccurrences(WebModule webModule, String oldPackageName,
            String newPackageName, boolean renameSubpackages){
        List result = new ArrayList();
        assert webModule != null;
        assert oldPackageName != null;
        
        if (webModule != null){
            // find all jsf configuration files in the web module
            FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
            
            if (configs != null){
                for (int i = 0; i < configs.length; i++) {
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configs[i], true).getRootComponent();
                    List <Converter> converters = facesConfig.getConverters();
                    for (Iterator<Converter> it = converters.iterator(); it.hasNext();) {
                        Converter converter = it.next();
                        if (JSFRefactoringUtils.containsRenamingPackage(converter.getConverterClass(), oldPackageName, renameSubpackages))
                            result.add(new ConverterClassItem(configs[i], converter, 
                                    getNewFQCN(newPackageName, oldPackageName, converter.getConverterClass())));
                        if (JSFRefactoringUtils.containsRenamingPackage(converter.getConverterForClass(), oldPackageName, renameSubpackages))
                            result.add(new ConverterForClassItem(configs[i], converter,
                                    getNewFQCN(newPackageName, oldPackageName, converter.getConverterForClass())));
                    }
                    List<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                    for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                        ManagedBean managedBean = it.next();
                        if (JSFRefactoringUtils.containsRenamingPackage(managedBean.getManagedBeanClass(), oldPackageName, renameSubpackages))
                            result.add(new ManagedBeanClassItem(configs[i], managedBean,
                                    getNewFQCN(newPackageName, oldPackageName, managedBean.getManagedBeanClass())));
                    }
                    
                }
            }
        }
        return result;
        
    }
    
    /**
     * A helper method, which is used for obtaining new FQCN, when a package is renamed. 
     * @param newPackageName the new package name. It must to be always full qualified package name.
     * @param oldPackageName the old package name. It must to be always full qualified package name.
     * @param oldFQCN the full qualified class name
     * @param folderRename Indicates whether the changing package is based on the 
     * renaming package or renaming folder.
     * @returns new FQCN for the class. 
     */
    public static String getNewFQCN(String newPackageName, String oldPackageName, String oldFQCN){
        String value = oldFQCN;
        
        if (oldPackageName.length() == 0){
            value = newPackageName + '.' + oldFQCN;
        }
        else {
            if (oldFQCN.startsWith(oldPackageName)){
                value = value.substring(oldPackageName.length());
                if (newPackageName.length() > 0){
                    value = newPackageName + value;
                }
                if (value.charAt(0) == '.'){
                    value = value.substring(1);
                }
            }
         }
         return value;
    }
}
