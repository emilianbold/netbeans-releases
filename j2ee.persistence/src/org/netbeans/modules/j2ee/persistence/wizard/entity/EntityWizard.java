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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.openide.*;
import org.openide.util.*;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;

/**
 * A wizard for creating entity classes.
 * 
 * @author Martin Adamek
 * @author Erno Mononen
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
    
    /**
     * Generates an entity class and adds it to an appropriate persistence unit
     *  if needed. 
     * @param targetFolder the target folder for the entity.
     * @param targetName the target name of the entity.
     * @param primaryKeyClassName the fully qualified name of the primary key class.
     * @param isAccessProperty defines the access strategy for the id field.
     * @return a FileObject representing the generated entity.
     */
    public static FileObject generateEntity(FileObject targetFolder, String targetName, 
            final String primaryKeyClassName, final boolean isAccessProperty) throws IOException {
        
        FileObject entityFo = GenerationUtils.createClass(targetFolder, targetName, null);
        JavaSource targetSource = JavaSource.forFileObject(entityFo);
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                ClassTree clazz = genUtils.getClassTree();
                
                String idFieldName = "id"; // NO18N
                VariableTree idField = genUtils.createField(Modifier.PRIVATE, primaryKeyClassName, idFieldName);
                MethodTree idGetter = genUtils.createPropertyGetterMethod(primaryKeyClassName, idFieldName);
                MethodTree idSetter = genUtils.createPropertySetterMethod(primaryKeyClassName, idFieldName);
                ExpressionTree generationStrategy = genUtils.createAnnotationArgument("strategy", "javax.persistence.GenerationType", "AUTO"); //NO18N
                AnnotationTree idAnnotation = genUtils.createAnnotation("javax.persistence.Id", Collections.singletonList(generationStrategy)); //NO18N
                
                if (isAccessProperty){
                    idField = genUtils.addAnnotation(idAnnotation, idField);
                } else {
                    idGetter = genUtils.addAnnotation(idAnnotation, idGetter);
                }
                ClassTree modifiedClazz = genUtils.addClassFields(clazz, Collections.singletonList(idField));
                
                TreeMaker make = workingCopy.getTreeMaker();
                modifiedClazz = make.addClassMember(modifiedClazz, idSetter);
                modifiedClazz = make.addClassMember(modifiedClazz, idGetter);
                modifiedClazz = genUtils.addImplementsClause(modifiedClazz, "java.io.Serializable");
                modifiedClazz = genUtils.addAnnotation(genUtils.createAnnotation("javax.persistence.Entity"), modifiedClazz);
                
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        
        targetSource.runModificationTask(task).commit();
        
        Project project = FileOwnerQuery.getOwner(targetFolder);
        if (project != null && !Util.isSupportedJavaEEVersion(project) && ProviderUtil.getDDFile(project) != null) {
            PUDataObject pudo = ProviderUtil.getPUDataObject(project);
            PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
            //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
            if (pu.length == 1) {
                pudo.addClass(pu[0], targetName);
            }
        }
        return entityFo;
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
