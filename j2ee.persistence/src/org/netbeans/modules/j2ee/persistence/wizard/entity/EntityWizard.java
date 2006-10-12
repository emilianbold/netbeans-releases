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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.io.IOException;
import java.lang.reflect.Modifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.jmi.javamodel.AttributeValue;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.*;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.common.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */

public final class EntityWizard implements WizardDescriptor.InstantiatingIterator {
    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private EntityWizardDescriptor ejbPanel;
    private WizardDescriptor wiz;
    private SourceGroup[] sourceGroups;
    
    public static EntityWizard create() {
        return new EntityWizard();
    }
    
    public String name() {
        return NbBundle.getMessage(EntityWizard.class, "LBL_EntityEJBWizardTitle");
    }
    
    public void uninitialize(WizardDescriptor wiz) {
    }
    
    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        sourceGroups = Util.getJavaSourceGroups(project);
        ejbPanel = new EntityWizardDescriptor();
        WizardDescriptor.Panel p = new ValidatingPanel(JavaTemplates.createPackageChooser(project,sourceGroups, ejbPanel, true));
        panels = new WizardDescriptor.Panel[] {p};
        Util.mergeSteps(wiz, panels, null);
        
    }
    
    public Set instantiate() throws IOException {

        if (ejbPanel.getPersistenceUnit() != null){
            ProviderUtil.addPersistenceUnit(ejbPanel.getPersistenceUnit(), Templates.getProject(wiz));
        }

        FileObject result = generateEntity(
                Templates.getTargetFolder(wiz),
                Templates.getTargetName(wiz),
                ejbPanel.getPrimaryKeyClassName(),
                sourceGroups,
                false // setting PROPERTY access type by default
                );
        return Collections.singleton(result);
    }
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (! hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public static FileObject generateEntity(FileObject targetFolder, String targetName, String primaryKeyClassName, SourceGroup[] sourceGroups, boolean isAccessProperty) {
        JavaClass javaClass = null;
        boolean rollback = true;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            
            javaClass = JMIGenerationUtil.createEntityClass(targetFolder, targetName);
            
            JMIGenerationUtil.addInterface(javaClass, "java.io.Serializable"); //NOI18N
            
            Annotation entityAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.Entity", Collections.EMPTY_LIST /*entityAttributeValues*/);
            javaClass.getAnnotations().add(entityAnnotation);
            Annotation idAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.Id", Collections.EMPTY_LIST);
            AttributeValue strategyAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "strategy", "javax.persistence.GenerationType", "AUTO");
            Annotation generatedValueAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.GeneratedValue", Collections.singletonList(strategyAttibuteValue));
            String memberName = "id";   // NOI18N
            String capitalizedMemberName = "Id";   // NOI18N
            Field idField = JMIGenerationUtil.createField(javaClass, memberName, Modifier.PRIVATE, primaryKeyClassName);
            List features = javaClass.getFeatures();
            features.add(0, idField);
            
            Method getter = JMIGenerationUtil.createGetterMethod(memberName,
                    capitalizedMemberName, primaryKeyClassName, javaClass);
            List addAnnotationsList = (isAccessProperty ? getter.getAnnotations() : idField.getAnnotations());
            addAnnotationsList.add(idAnnotation);
            addAnnotationsList.add(generatedValueAnnotation);
            features.add(getter);
            features.add(JMIGenerationUtil.createSetterMethod(memberName,
                    capitalizedMemberName, primaryKeyClassName, javaClass));
            
            List idFieldList = Collections.singletonList(idField);
            Method hashCodeMethod = JMIGenerationUtil.createHashCodeMethod(javaClass, idFieldList);
            features.add(hashCodeMethod);
            Method equalsMethod = JMIGenerationUtil.createEntityEqualsMethod(javaClass, idFieldList);
            features.add(equalsMethod);
            Method toString = JMIGenerationUtil.createToStringMethod(javaClass, idFieldList);
            features.add(toString);
            
            rollback = false;
        } catch (DataObjectNotFoundException dex) {
            ErrorManager.getDefault().notify(dex);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        FileObject fo = javaClass == null ? null : JavaModel.getFileObject(javaClass.getResource());
        Project project = FileOwnerQuery.getOwner(targetFolder);
        if (fo != null && !Util.isSupportedJavaEEVersion(project) && ProviderUtil.getDDFile(project) != null) {
            PUDataObject pudo = ProviderUtil.getPUDataObject(project);
            PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
            //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
            if (pu.length == 1) {
                pudo.addClass(pu[0], javaClass.getName());
            }
        }
        return fo;
    }
    
    /**
     * A panel which checks whether the target project has a valid server set,
     * otherwise it delegates to the real panel.
     */
    private static final class ValidatingPanel extends DelegatingWizardDescriptorPanel {
        
        public ValidatingPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
        
        public boolean isValid() {
            if (!ProviderUtil.isValidServerInstanceOrNone(getProject())) {
                getWizardDescriptor().putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_MissingServer")); // NOI18N
                return false;
            }
            return super.isValid();
        }
    }
}
