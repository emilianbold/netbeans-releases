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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.WizardProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Generates EJB facades for entity classes.
 * 
 * @author Martin Adamek, Erno Mononen
 */ 
    public final class EjbFacadeWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N

    private static final String FACADE_SUFFIX = "Facade"; //NOI18N
    private static final String FACADE_REMOTE_SUFFIX = FACADE_SUFFIX + "Remote"; //NOI18N
    private static final String FACADE_LOCAL_SUFFIX = FACADE_SUFFIX + "Local"; //NOI18N
    private static final String EJB_LOCAL = "javax.ejb.Local"; //NOI18N
    private static final String EJB_REMOTE = "javax.ejb.Remote"; //NOI18N
    private static final String EJB_STATELESS = "javax.ejb.Stateless"; //NOI18N

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String[] steps;
    private int stepsStartPos;
    private Project project;
    /**
     * Contains the names of the entities. Key the FQN class name, 
     * value the name of the entity.
     */ 
    private final Map<String, String> entityNames = new HashMap<String, String>();
    
    private static final String EJB30_STATELESS_EJBCLASS = "Templates/J2EE/EJB30/StatelessEjbClass.java"; // NOI18N
    
    private WizardDescriptor.Panel[] getPanels() {
        return panels;
    }
    
    public Set instantiate() throws IOException {
        this.project = Templates.getProject(wizard);
        initEntityNames();
        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);

        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final EjbFacadeWizardPanel2 panel = (EjbFacadeWizardPanel2) panels[1];
        String pkg = panel.getPackage();
        
        PersistenceUnit persistenceUnit = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
        if (persistenceUnit != null) {
            try {
                ProviderUtil.addPersistenceUnit(persistenceUnit, project);
            } catch (InvalidPersistenceXmlException ipx) {
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(EjbFacadeWizardIterator.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
            }
        }
        
        for (String entity : entities) {
            createdFiles.addAll(generate(targetFolder, entity, pkg, panel.isRemote(), panel.isLocal()));
        }
        return createdFiles;
    }
    
    /**
     * Generates the facade and the loca/remote interface(s) for the given
     * entity class.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     * 
     * @return a set containing the generated files.
     */ 
    private Set<FileObject> generate(final FileObject targetFolder, final String entityClass, String pkg, final boolean hasRemote, final boolean hasLocal) throws IOException {
        return generate(targetFolder, entityClass, pkg, hasRemote, hasLocal, ContainerManagedJTAInjectableInEJB.class);
    }
    
    
    /**
     * Generates the facade and the loca/remote interface(s) for thhe given
     * entity class.
     * <i>Package private visibility for tests</i>.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     * @param strategyClass the entity manager lookup strategy. 
     * 
     * @return a set containing the generated files.
     */ 
    Set<FileObject> generate(final FileObject targetFolder, final String entityFQN, 
            String pkg, final boolean hasRemote, final boolean hasLocal, final Class<? extends EntityManagerGenerationStrategy> strategyClass) throws IOException {
        
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final String entitySimpleName = JavaIdentifiers.unqualify(entityFQN);
        final String variableName = entitySimpleName.toLowerCase().charAt(0) + entitySimpleName.substring(1);
        
        // create the facade
        final FileObject facade = GenerationUtils.createClass(targetFolder, entitySimpleName + FACADE_SUFFIX, null);
        createdFiles.add(facade);
        // add the @stateless annotation 
        JavaSource source = JavaSource.forFileObject(facade);
        source.runModificationTask(new Task<WorkingCopy>(){
            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(parameter);
                assert classTree != null;
                GenerationUtils genUtils = GenerationUtils.newInstance(parameter);
                AnnotationTree stateless = genUtils.createAnnotation(EJB_STATELESS);
                parameter.rewrite(classTree, genUtils.addAnnotation(classTree, stateless));
            }
        }).commit();

        // generate methods for the facade
        EntityManagerGenerator generator = new EntityManagerGenerator(facade, entityFQN);
        List<GenerationOptions> methodOptions = getMethodOptions(entityFQN, variableName);
        for (GenerationOptions each : methodOptions){
            generator.generate(each, strategyClass);
        }

        // create the interfaces
        final String localInterfaceFQN = pkg + "." + getUniqueClassName(entitySimpleName + FACADE_LOCAL_SUFFIX, targetFolder);
        final String remoteInterfaceFQN = pkg + "." + getUniqueClassName(entitySimpleName + FACADE_REMOTE_SUFFIX, targetFolder);

        if (hasLocal) {
            FileObject local = createInterface(JavaIdentifiers.unqualify(localInterfaceFQN), EJB_LOCAL, targetFolder);
            addMethodToInterface(methodOptions, local);
            createdFiles.add(local);
        }
        if (hasRemote) {
            FileObject remote = createInterface(JavaIdentifiers.unqualify(remoteInterfaceFQN), EJB_REMOTE, targetFolder);
            addMethodToInterface(methodOptions, remote);
            createdFiles.add(remote);
        }

        // add implements clauses to the facade
        source.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(parameter);
                assert classTree != null;
                GenerationUtils genUtils = GenerationUtils.newInstance(parameter);
                ClassTree newClassTree = classTree;
                if (hasLocal){
                    newClassTree = genUtils.addImplementsClause(newClassTree, localInterfaceFQN);
                }
                if (hasRemote){
                    newClassTree = genUtils.addImplementsClause(newClassTree, remoteInterfaceFQN);
                }
                parameter.rewrite(classTree, newClassTree);
            }
        }).commit();
        
        return createdFiles;
    }
    
    /**
     * @return the options representing the methods for a facade, i.e. create/edit/
     * find/remove/findAll.
     */ 
    private List<GenerationOptions> getMethodOptions(String entityFQN, String variableName){

        GenerationOptions createOptions = new GenerationOptions();
        createOptions.setMethodName("create"); //NOI18N
        createOptions.setOperation(GenerationOptions.Operation.PERSIST);
        createOptions.setReturnType("void");//NOI18N
        createOptions.setParameterName(variableName);
        createOptions.setParameterType(entityFQN);

        GenerationOptions editOptions = new GenerationOptions();
        editOptions.setMethodName("edit");//NOI18N
        editOptions.setOperation(GenerationOptions.Operation.MERGE);
        editOptions.setReturnType("void");//NOI18N
        editOptions.setParameterName(variableName);
        editOptions.setParameterType(entityFQN);

        GenerationOptions destroyOptions = new GenerationOptions();
        destroyOptions.setMethodName("remove");//NOI18N
        destroyOptions.setOperation(GenerationOptions.Operation.REMOVE);
        destroyOptions.setReturnType("void");//NOI18N
        destroyOptions.setParameterName(variableName);
        destroyOptions.setParameterType(entityFQN);

        GenerationOptions findOptions = new GenerationOptions();
        findOptions.setMethodName("find");//NOI18N
        findOptions.setOperation(GenerationOptions.Operation.FIND);
        findOptions.setReturnType(entityFQN);//NOI18N
        findOptions.setParameterName("id");//NOI18N
        findOptions.setParameterType("Object");//NOI18N

        GenerationOptions findAllOptions = new GenerationOptions();
        findAllOptions.setMethodName("findAll");//NOI18N
        findAllOptions.setOperation(GenerationOptions.Operation.FIND_ALL);
        findAllOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findAllOptions.setQueryAttribute(getEntityName(entityFQN));

        GenerationOptions findSubOptions = new GenerationOptions();
        findSubOptions.setMethodName("findRange");//NOI18N
        findSubOptions.setOperation(GenerationOptions.Operation.FIND_SUBSET);
        findSubOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findSubOptions.setQueryAttribute(getEntityName(entityFQN));
        findSubOptions.setParameterName("range");//NOI18N
        findSubOptions.setParameterType("int[]");//NOI18N

        GenerationOptions countOptions = new GenerationOptions();
        countOptions.setMethodName("count");//NOI18N
        countOptions.setOperation(GenerationOptions.Operation.COUNT);
        countOptions.setReturnType("int");//NOI18N
        countOptions.setQueryAttribute(getEntityName(entityFQN));

        return Arrays.<GenerationOptions>asList(createOptions, editOptions, destroyOptions, findOptions, findAllOptions, findSubOptions, countOptions);
    }

    /**
     *@return the name for the given <code>entityFQN</code>.
     */ 
    private String getEntityName(String entityFQN){
        String result = entityNames.get(entityFQN);
        return result != null ? result : JavaIdentifiers.unqualify(entityFQN);
    }
    
    /**
     * Initializes the {@link #entityNames} map.
     */ 
    private void initEntityNames() throws IOException{
        if (project == null){
            // just to facilitate testing, avoids the need to provide a project (together with the getEntityName method)
            return;
        }
        //XXX should probably be using MetadataModelReadHelper. needs a progress indicator as well (#113874).
        try {
            EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
            MetadataModel<EntityMappingsMetadata> entityMappingsModel = entityClassScope.getEntityMappingsModel(true);
            Future<Void> result = entityMappingsModel.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, Void>() {

                public Void run(EntityMappingsMetadata metadata) throws Exception {
                    for (Entity entity : metadata.getRoot().getEntity()) {
                        entityNames.put(entity.getClass2(), entity.getName());
                    }
                    return null;
                }
            });
            result.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    String getUniqueClassName(String candidateName, FileObject targetFolder){
        return FileUtil.findFreeFileName(targetFolder, candidateName, "java"); //NOI18N
    }
    
    /**
     * Creates an interface with the given <code>name</code>, annotated with an annotation
     * of the given <code>annotationType</code>. <i>Package private visibility just because of tests</i>.
     * 
     * @param name the name for the interface
     * @param annotationType the FQN of the annotation
     * @param targetFolder the folder to which the interface is generated
     * 
     * @return the generated interface.
     */
    FileObject createInterface(String name, final String annotationType, FileObject targetFolder) throws IOException {
        FileObject sourceFile = GenerationUtils.createInterface(targetFolder, name, null);
        JavaSource source = JavaSource.forFileObject(sourceFile);
        ModificationResult result = source.runModificationTask(new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = SourceUtils.getPublicTopLevelTree(workingCopy);
                assert clazz != null;
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                TreeMaker make = workingCopy.getTreeMaker();
                AnnotationTree annotations = genUtils.createAnnotation(annotationType);
                ModifiersTree modifiers = make.Modifiers(clazz.getModifiers(), Collections.<AnnotationTree>singletonList(annotations));
                ClassTree modifiedClass = make.Class(modifiers, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList());
                workingCopy.rewrite(clazz, modifiedClass);
            }
        });
        result.commit();
        return source.getFileObjects().iterator().next();
    }
    
    /**
     * Adds a method to the given interface.
     * 
     * @param name the name of the method.
     * @param returnType the return type of the method.
     * @param parameterName the name of the parameter for the method.
     * @param parameterType the FQN type of the parameter.
     * @param target the target interface.
     */ 
    void addMethodToInterface(final List<GenerationOptions> options, final FileObject target) throws IOException {
        
        JavaSource source = JavaSource.forFileObject(target);
        ModificationResult result = source.runModificationTask(new Task<WorkingCopy>() {
            
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                GenerationUtils utils = GenerationUtils.newInstance(copy);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(copy);
                assert typeElement != null;
                ClassTree original = copy.getTrees().getTree(typeElement);
                ClassTree modifiedClass = original;
                TreeMaker make = copy.getTreeMaker();
                for (GenerationOptions each : options) {
                    MethodTree method = make.Method(make.Modifiers(Collections.<Modifier>emptySet()), 
                            each.getMethodName(), utils.createType(each.getReturnType(), typeElement), 
                            Collections.<TypeParameterTree>emptyList(), getParameterList(each, make, utils, typeElement),
                            Collections.<ExpressionTree>emptyList(), (BlockTree) null, null);
                    modifiedClass = make.addClassMember(modifiedClass, method);
                }
                copy.rewrite(original, modifiedClass);
            }
        });
        result.commit();
    }
    
    private List<VariableTree> getParameterList(GenerationOptions options, TreeMaker make, GenerationUtils utils, TypeElement scope){
        if (options.getParameterName() == null){
            return Collections.<VariableTree>emptyList();
        }
        VariableTree vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), 
                options.getParameterName(), utils.createType(options.getParameterType(), scope), null);
        return Collections.<VariableTree>singletonList(vt);
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizard.putProperty("NewFileWizard_Title", NbBundle.getMessage(EjbFacadeWizardIterator.class, "Templates/Persistence/ejbFacade"));
        Project project = Templates.getProject(wizard);
        
        // http://www.netbeans.org/issues/show_bug.cgi?id=126642
        //if (Templates.getTargetFolder(wizard) == null) {
        //    Templates.setTargetFolder(wizard, project.getProjectDirectory());
        //}
        
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{new PersistenceClientEntitySelection(NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"), new HelpCtx(EjbFacadeWizardIterator.class.getName() + "$PersistenceClientEntitySelection"), wizard), new EjbFacadeWizardPanel2(project, wizard)};
            if (steps == null) {
                mergeSteps(new String[]{NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"), NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_GeneratedSessionBeans")});
            }
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (c instanceof JComponent) {
                    // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_FacadeWizardTitle");
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    private void mergeSteps(String[] thisSteps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            stepsStartPos = beforeSteps.length;
            if (stepsStartPos > 0 && ("...".equals(beforeSteps[stepsStartPos - 1]))) { // NOI18N
                stepsStartPos--;
            }
        } else {
            beforeSteps = null;
            stepsStartPos = 0;
        }
        
        steps = new String[stepsStartPos + thisSteps.length];
        System.arraycopy(beforeSteps, 0, steps, 0, stepsStartPos);
        System.arraycopy(thisSteps, 0, steps, stepsStartPos, thisSteps.length);
    }
    public static FileObject[] generateSessionBeans(ProgressContributor progressContributor, ProgressPanel progressPanel, List<String> entities, Project project, String jpaControllerPackage, FileObject jpaControllerPackageFileObject, boolean local, boolean remote) throws IOException {
        int progressIndex = 0;
        String progressMsg =  NbBundle.getMessage(EjbFacadeWizardIterator.class, "MSG_Progress_SessionBean_Pre"); //NOI18N;
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);

        int[] nameAttemptIndices = null;

        EjbFacadeWizardIterator iterator=new EjbFacadeWizardIterator();

        FileObject[] sbFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            final String entitySimpleName = JavaIdentifiers.unqualify(entities.get(i));

            progressMsg = NbBundle.getMessage(EjbFacadeWizardIterator.class, "MSG_Progress_SessionBean_Now_Generating", entitySimpleName + FACADE_SUFFIX + ".java");//NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
            sbFileObjects[i]=iterator.generate(jpaControllerPackageFileObject, entities.get(i), jpaControllerPackage, local, remote).iterator().next();
        }

        return sbFileObjects;
    }

    public static  int getProgressStepCount(int numEntites)
    {
        return numEntites;
    }
}
