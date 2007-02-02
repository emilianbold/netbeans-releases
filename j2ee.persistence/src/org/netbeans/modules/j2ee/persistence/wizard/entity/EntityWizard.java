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
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.openide.*;
import org.openide.util.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.java.classpath.ClassPathProvider;
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
        
        FileObject result = generateEntity(
                Templates.getTargetFolder(wiz),
                Templates.getTargetName(wiz),
                ejbPanel.getPrimaryKeyClassName(),
                false // setting PROPERTY access type by default
                );
        
        try{
            PersistenceUnit punit = ejbPanel.getPersistenceUnit();
            if (punit != null){
                ProviderUtil.addPersistenceUnit(punit, Templates.getProject(wiz));
            }
            addEntityToPersistenceUnit(result);
        } catch (InvalidPersistenceXmlException ipx){
            // just log for debugging purposes, at this point the user has
            // already been warned about an invalid persistence.xml
            Logger.getLogger(EntityWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NO18N
        }
        
        return Collections.singleton(result);
    }
    
    /**
     * Adds the given entity to the persistence unit defined in the project in which this wizard
     * was invoked.
     * @param entity the entity to be added.
     */
    private void addEntityToPersistenceUnit(FileObject entity) throws InvalidPersistenceXmlException{
        
        Project project = Templates.getProject(wiz);
        String entityFQN = "";
        ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
        if (classPathProvider != null) {
            entityFQN = classPathProvider.findClassPath(entity, ClassPath.SOURCE).getResourceName(entity, '.', false);
        }
        
        if (project != null && !Util.isSupportedJavaEEVersion(project) && ProviderUtil.getDDFile(project) != null) {
            PUDataObject pudo = ProviderUtil.getPUDataObject(project);
            PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
            //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
            if (pu.length == 1) {
                pudo.addClass(pu[0], entityFQN);
            }
        }
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
     * Generates an entity class.
     *
     * @param targetFolder the target folder for the entity.
     * @param targetName the target name of the entity.
     * @param primaryKeyClassName the name of the primary key class, needs to be
     *  resolvable in the generated entity's scope.
     * @param isAccessProperty defines the access strategy for the id field.
     * @return a FileObject representing the generated entity.
     */
    public static FileObject generateEntity(FileObject targetFolder, String targetName,
            final String primaryKeyClassName, final boolean isAccessProperty) throws IOException {
        
        FileObject entityFo = GenerationUtils.createClass(targetFolder, targetName, null);
        
        ClassPath compile = ClassPath.getClassPath(targetFolder, ClassPath.COMPILE);
        Set<ClassPath> compileClassPaths = new HashSet<ClassPath>();
        compileClassPaths.add(compile);
        
        JPAClassPathHelper cpHelper = new JPAClassPathHelper(
                Collections.<ClassPath>singleton(ClassPath.getClassPath(targetFolder, ClassPath.BOOT)), 
                Collections.<ClassPath>singleton(ClassPath.getClassPath(targetFolder, ClassPath.COMPILE)), 
                Collections.<ClassPath>singleton(ClassPath.getClassPath(targetFolder, ClassPath.SOURCE))
                );
        

        JavaSource targetSource = JavaSource.create(cpHelper.createClasspathInfo(), entityFo);
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                ClassTree clazz = genUtils.getClassTree();
                ClassTree modifiedClazz = genUtils.ensureNoArgConstructor(clazz);
                TreeMaker make = workingCopy.getTreeMaker();
                
                String idFieldName = "id"; // NO18N
                
                TypeMirror type = workingCopy.getTreeUtilities().parseType(primaryKeyClassName, genUtils.getTypeElement());
                Tree typeTree = make.Type(type);
                
                VariableTree idField = make.Variable(genUtils.createModifiers(Modifier.PRIVATE), idFieldName, typeTree, null);
                ModifiersTree idMethodModifiers = genUtils.createModifiers(Modifier.PUBLIC);
                MethodTree idGetter = genUtils.createPropertyGetterMethod(idMethodModifiers, idFieldName, typeTree);
                MethodTree idSetter = genUtils.createPropertySetterMethod(idMethodModifiers, idFieldName, typeTree);
                AnnotationTree idAnnotation = genUtils.createAnnotation("javax.persistence.Id"); //NO18N
                ExpressionTree generationStrategy = genUtils.createAnnotationArgument("strategy", "javax.persistence.GenerationType", "AUTO"); //NO18N
                AnnotationTree generatedValueAnnotation = genUtils.createAnnotation("javax.persistence.GeneratedValue", Collections.singletonList(generationStrategy)); //NO18N
                
                if (isAccessProperty){
                    idField = genUtils.addAnnotation(idField, idAnnotation);
                    idField = genUtils.addAnnotation(idField, generatedValueAnnotation);
                } else {
                    idGetter = genUtils.addAnnotation(idGetter, idAnnotation);
                    idGetter = genUtils.addAnnotation(idGetter, generatedValueAnnotation);
                }
                
                modifiedClazz = genUtils.addClassFields(clazz, Collections.singletonList(idField));
                modifiedClazz = make.addClassMember(modifiedClazz, idSetter);
                modifiedClazz = make.addClassMember(modifiedClazz, idGetter);
                modifiedClazz = genUtils.addImplementsClause(modifiedClazz, "java.io.Serializable");
                modifiedClazz = genUtils.addAnnotation(modifiedClazz, genUtils.createAnnotation("javax.persistence.Entity"));
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        
        targetSource.runModificationTask(task).commit();
        
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
