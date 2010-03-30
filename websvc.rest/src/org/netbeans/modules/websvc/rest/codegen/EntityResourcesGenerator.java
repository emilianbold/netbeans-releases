/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.codegen.model.RelatedEntityResource;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.Inflector;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author PeterLiu
 */
public abstract class EntityResourcesGenerator extends AbstractGenerator {

    public static final String RESOURCE_FOLDER = "service";   //NOI18N

    public static final String CONVERTER_FOLDER = "converter";      //NOI18N

    public static final String RESOURCE_SUFFIX = GenericResourceBean.RESOURCE_SUFFIX;
    public static final String CONVERTER_SUFFIX = "Converter";      //NOI18N

    private static final String DEFAULT_TEMPLATE = "Templates/WebServices/DefaultResource.java"; //NOI18N

    private static final String URI_RESOLVER_TEMPLATE = "Templates/WebServices/UriResolver.java";  //NOI18N

    private static final String URI_RESOLVER = "UriResolver";
    private static final String PERSISTENCE_SERVICE_TEMPLATE = "Templates/WebServices/PersistenceService.java";    //NOI18N

    private static final String PERSISTENCE_SERVICE_NOJTA_TEMPLATE = "Templates/WebServices/PersistenceServiceNoJTA.java";    //NOI18N

    private static final String PERSISTENCE_SERVICE = "PersistenceService";     //NOI18N

    private static final String DEFAULT_PU_FIELD = "DEFAULT_PU";        //NOI18N

    private static final String[] CONTAINER_IMPORTS = {
        RestConstants.PATH,
        RestConstants.GET,
        RestConstants.POST,
        RestConstants.PRODUCE_MIME,
        RestConstants.CONSUME_MIME,
        RestConstants.PATH_PARAM,
        RestConstants.QUERY_PARAM,
        RestConstants.DEFAULT_VALUE,
        RestConstants.HTTP_RESPONSE,
        RestConstants.CONTEXT,
        RestConstants.URI_INFO,
        Constants.ENTITY_MANAGER_TYPE
    };
    private static final String[] ITEM_IMPORTS = {
        RestConstants.PATH,
        RestConstants.GET,
        RestConstants.PUT,
        RestConstants.DELETE,
        RestConstants.PRODUCE_MIME,
        RestConstants.CONSUME_MIME,
        RestConstants.QUERY_PARAM,
        RestConstants.DEFAULT_VALUE,
        RestConstants.CONTEXT,
        RestConstants.URI_INFO,
        RestConstants.WEB_APPLICATION_EXCEPTION,
        Constants.NO_RESULT_EXCEPTION,
        Constants.ENTITY_MANAGER_TYPE
    };
    private static final String[] CONTAINER_CONVERTER_IMPORTS = {
        Constants.XML_ROOT_ELEMENT,
        Constants.XML_ELEMENT,
        Constants.XML_TRANSIENT,
        Constants.XML_ATTRIBUTE,
        Constants.ARRAY_LIST_TYPE
    };
    private static final String[] ITEM_CONVERTER_IMPORTS = {
        Constants.XML_ROOT_ELEMENT,
        Constants.XML_ELEMENT,
        Constants.XML_TRANSIENT,
        Constants.XML_ATTRIBUTE,
        RestConstants.URI_BUILDER,
        Constants.ENTITY_MANAGER_TYPE
    };
    private static final String mimeTypes = "{\"" + MimeType.XML.value() + "\", \"" +
            MimeType.JSON.value() + "\"}";        //NOI18N

    protected PersistenceUnit persistenceUnit;
    protected String targetPackageName;
    protected FileObject targetFolder;
    protected FileObject resourceFolder;
    protected String resourcePackageName;
    protected FileObject converterFolder;
    protected String converterPackageName;
    protected EntityResourceBeanModel model;
    protected Project project;
    protected boolean injectEntityManager = false;
    protected boolean useEjbInjections = false;
    private static final String GET_ENTITY_MANAGER_STMT = "EntityManager em = PersistenceService.getInstance().getEntityManager();";

    public EntityResourcesGenerator() {
        
    }
    
    /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public void initialize(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName, PersistenceUnit persistenceUnit) {
        initialize(model, project, targetFolder, targetPackageName, null, null, persistenceUnit);
    }

    public void initialize(EntityResourceBeanModel model,
            String resourcePackage, String converterPackage) {
        initialize(model, null, null, null, resourcePackage, converterPackage, null);
    }

    /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public void initialize(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName,
            String resourcePackage, String converterPackage,
            PersistenceUnit persistenceUnit) {
        this.model = model;
        this.project = project;
        this.persistenceUnit = persistenceUnit;
        this.targetFolder = targetFolder;
        this.targetPackageName = targetPackageName;

        if (resourcePackage == null) {
            this.resourcePackageName = targetPackageName + "." + RESOURCE_FOLDER;
        } else {
            this.resourcePackageName = resourcePackage;
        }

        if (converterPackage == null) {
            this.converterPackageName = targetPackageName + "." + CONVERTER_FOLDER;
        } else {
            this.converterPackageName = converterPackage;
        }

    }

    private String toFilePath(String packageName) {
        return packageName.replace(".", "/");
    }

    private FileObject getSourceRootFolder(FileObject packageFolder, String packageName) {
        String[] segments = packageName.split("\\.");
        FileObject ret = packageFolder;
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];

            if (segment.length() == 0) {
                return ret;
            }

            if (ret == null || !segments[i].equals(ret.getNameExt())) {
                throw new IllegalArgumentException("Unmatched folder: " + packageFolder.getPath() + " and package name: " + packageName);
            }
            ret = ret.getParent();
        }
        return ret;
    }

    public Collection<String> previewClasses() {
        Collection<String> classes = new ArrayList<String>();
        Collection<EntityResourceBean> beans = model.getResourceBeans();

        for (EntityResourceBean bean : beans) {
            classes.add(getResourceType(bean));
            classes.add(getConverterType(bean));
        }

        if (!injectEntityManager) {
            classes.add(getPersistenceServiceClassType());
        }

        classes.add(getUriResolverClassType());

        return Utils.sortKeys(classes);
    }

    /**
     * Generates RESTful resources
     * @param pHandle ProgressHandle. May be null, e.g., if method is called in conjunction with other generators that have ProgressHandles
     * that are already running.
     * @return
     * @throws java.io.IOException
     */
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if (pHandle != null) {
            initProgressReporting(pHandle);
        }

        createFolders();

        if (!injectEntityManager) {
            generatePersistenceService();
        }

        //Make necessary changes to the persistence.xml
        new PersistenceHelper(project).configure(model.getBuilder().getAllEntityNames(),
                !RestUtils.hasJTASupport(project));

        //
        //Delegate to J2eeEntityResourcesGenerator or SpringEntityResourceGenerator to
        // perform the rest of the persistence configuration.
        //
        configurePersistence();

        generateUriResolver();

        Map<EntityResourceBean, JavaSource> resourceMap = new HashMap<EntityResourceBean, JavaSource>();
        Map<EntityResourceBean, JavaSource> converterMap = new HashMap<EntityResourceBean, JavaSource>();

        Collection<EntityResourceBean> resourceBeans = model.getResourceBeans();

        for (EntityResourceBean bean : resourceBeans) {
            resourceMap.put(bean, generateResourceBean(bean));
            converterMap.put(bean, generateConverter(bean));
        }

        for (EntityResourceBean bean : resourceBeans) {
            modifyResourceBean(resourceMap.get(bean), bean);
            modifyConverter(converterMap.get(bean), bean);
        }

        finishProgressReporting();

        return new HashSet<FileObject>();
    }

    private void createFolders() {
        FileObject sourceRootFolder = getSourceRootFolder(targetFolder, targetPackageName);
        File sourceRootDir = FileUtil.toFile(sourceRootFolder);
        try {
            String resourceFolderPath = toFilePath(resourcePackageName);
            resourceFolder = sourceRootFolder.getFileObject(resourceFolderPath);
            if (resourceFolder == null) {
                resourceFolder = FileUtil.createFolder(new File(sourceRootDir, resourceFolderPath));
            }

            String converterFolderPath = toFilePath(converterPackageName);
            converterFolder = sourceRootFolder.getFileObject(converterFolderPath);
            if (converterFolder == null) {
                converterFolder = FileUtil.createFolder(new File(sourceRootDir, converterFolderPath));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void generatePersistenceService() throws IOException {
        reportProgress(getPersistenceServiceClassType(), false);

        String template = null;

        if (RestUtils.hasJTASupport(project)) {
            template = PERSISTENCE_SERVICE_TEMPLATE;
        } else {
            template = PERSISTENCE_SERVICE_NOJTA_TEMPLATE;
        }

        JavaSource source = JavaSourceHelper.createJavaSource(template,
                resourceFolder, getResourcePackageName(), PERSISTENCE_SERVICE);
        if (source == null) {
            return;
        }

        reportProgress(getPersistenceServiceClassType(), true);

        // Modify the PU name
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    JavaSourceHelper.replaceFieldValue(copy,
                            JavaSourceHelper.getField(copy, DEFAULT_PU_FIELD),
                            persistenceUnit.getName());
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void configurePersistence() {
    }

    private void generateUriResolver() {
        reportProgress(getUriResolverClassType(), false);

        JavaSource source = JavaSourceHelper.createJavaSource(URI_RESOLVER_TEMPLATE, converterFolder, getConverterPackageName(), URI_RESOLVER);

        reportProgress(getUriResolverClassType(), true);
    }

    private JavaSource generateResourceBean(EntityResourceBean bean) {
        reportProgress(getResourceType(bean), false);

        return JavaSourceHelper.createJavaSource(DEFAULT_TEMPLATE, resourceFolder,
                getResourcePackageName(), getResourceName(bean));
    }

    private JavaSource generateConverter(EntityResourceBean bean) {
        reportProgress(getConverterType(bean), false);

        return JavaSourceHelper.createJavaSource(DEFAULT_TEMPLATE, converterFolder,
                getConverterPackageName(), getConverterName(bean));
    }

    private void modifyResourceBean(JavaSource source, EntityResourceBean bean) {
        reportProgress(getResourceType(bean), true);

        if (bean.isContainer()) {
            modifyContainerResourceBean(source, bean);
        } else {
            modifyItemResourceBean(source, bean);
        }
    }

    private void modifyContainerResourceBean(JavaSource source, final EntityResourceBean bean) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    JavaSourceHelper.addImports(copy, getContainerResourceImports(bean));

                    String[] annotations = combineStringArrays(
                            new String[]{RestConstants.PATH_ANNOTATION},
                            getAdditionalContainerResourceAnnotations()
                            );
                    Object[] annotationAttrs = combineObjectArrays(
                            new Object[]{bean.getUriTemplate()},
                            getAdditionalContainerResourceAnnotationAttrs()
                            );
                    
                    JavaSourceHelper.addClassAnnotation(copy, annotations, annotationAttrs);

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    modifiedTree = addResourceBeanFields(copy, modifiedTree, bean);
                    modifiedTree = addResourceBeanAccessors(copy, modifiedTree, bean);
                    modifiedTree = addContainerGetMethod(copy, modifiedTree, bean, mimeTypes);
                    modifiedTree = addContainerPostMethod(copy, modifiedTree, bean, mimeTypes);

                    for (RelatedEntityResource relatedResource : bean.getSubResources()) {
                        modifiedTree = addContainerGetResourceMethod(copy, modifiedTree, bean, relatedResource);
                    }

                    modifiedTree = addGetEntitiesMethod(copy, modifiedTree, bean);
                    modifiedTree = addCreateEntityMethod(copy, modifiedTree, bean);
                    //modifiedTree = addGetUriMethod(copy, modifiedTree, bean);

                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String[] getAdditionalContainerResourceAnnotations() {
        return null;
    }
    
    protected Object[] getAdditionalContainerResourceAnnotationAttrs() {
        return null;
    }
    
    private void modifyItemResourceBean(JavaSource source, final EntityResourceBean bean) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    JavaSourceHelper.addImports(copy, getItemResourceImports(bean));

                    String[] annotations = getAdditionalItemResourceAnnotations();
                    Object[] annotationAttrs = getAdditionalItemResourceAnnotationAttrs();
                    
                    if (annotations != null) {
                        JavaSourceHelper.addClassAnnotation(copy, annotations, annotationAttrs);
                    }
                    
                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    modifiedTree = addResourceBeanFields(copy, modifiedTree, bean);
                    modifiedTree = addResourceBeanAccessors(copy, modifiedTree, bean);
                    modifiedTree = addItemGetMethod(copy, modifiedTree, bean, mimeTypes);
                    modifiedTree = addItemPutMethod(copy, modifiedTree, bean, mimeTypes);

                    modifiedTree = addItemDeleteMethod(copy, modifiedTree, bean);
                    modifiedTree = addGetEntityMethod(copy, modifiedTree, bean);
                    modifiedTree = addUpdateEntityMethod(copy, modifiedTree, bean);
                    modifiedTree = addDeleteEntityMethod(copy, modifiedTree, bean);

                    for (RelatedEntityResource relatedResource : bean.getSubResources()) {
                        modifiedTree = addItemGetResourceMethod(copy, modifiedTree, bean, relatedResource);
                    }

                    for (RelatedEntityResource relatedResource : bean.getSubResources()) {
                        modifiedTree = addSubresourceClass(copy, modifiedTree, bean, relatedResource);
                    }

                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String[] getAdditionalItemResourceAnnotations() {
        return null;
    }
    
    protected Object[] getAdditionalItemResourceAnnotationAttrs() {
        return null;
    }
    
    private void modifyConverter(JavaSource source, EntityResourceBean bean) {
        reportProgress(getConverterType(bean), true);

        if (bean.isContainer()) {
            modifyContainerConverter(source, bean);
        } else {
            modifyItemConverter(source, bean);
        }
    }

    private void modifyContainerConverter(JavaSource source, final EntityResourceBean bean) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    JavaSourceHelper.addImports(copy, CONTAINER_CONVERTER_IMPORTS);

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    JavaSourceHelper.addClassAnnotation(copy,
                            new String[]{Constants.XML_ROOT_ELEMENT_ANNOTATION},
                            new Object[]{
                                JavaSourceHelper.createAssignmentTree(copy, "name", //I18N
                                getConverterXMLName(bean))
                            });

                    modifiedTree = addConverterFields(copy, modifiedTree, bean);
                    modifiedTree = addConverterConstructor(copy, modifiedTree, bean);
                    modifiedTree = addGetItemsMethod(copy, modifiedTree, bean);
                    modifiedTree = addSetItemsMethod(copy, modifiedTree, bean);
                    modifiedTree = addGetUriMethod(copy, modifiedTree);
                    modifiedTree = addContainerConverterGetEntitiesMethod(copy, modifiedTree, bean);

                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void modifyItemConverterDefaultConstructor(JavaSource source,
            final EntityResourceBean bean) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    JavaSourceHelper.addImports(copy, new String[]{getEntityClassType(bean)});

                    String bodyText = "{ entity = new $CLASS$(); }";
                    bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean));

                    JavaSourceHelper.replaceMethodBody(copy,
                            JavaSourceHelper.getDefaultConstructor(copy),
                            bodyText);
                }
            });
            result.commit();

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void modifyItemConverter(JavaSource source, final EntityResourceBean bean) {
        modifyItemConverterDefaultConstructor(source, bean);

        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    String[] imports = getItemConverterImports(bean);
                    JavaSourceHelper.addImports(copy, imports);

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    JavaSourceHelper.addClassAnnotation(copy,
                            new String[]{Constants.XML_ROOT_ELEMENT_ANNOTATION},
                            new Object[]{
                                JavaSourceHelper.createAssignmentTree(copy, "name",
                                getConverterXMLName(bean))
                            });

                    modifiedTree = addConverterFields(copy, modifiedTree, bean);
                    modifiedTree = addConverterConstructor(copy, modifiedTree, bean);

                    for (FieldInfo fieldInfo : bean.getEntityClassInfo().getFieldInfos()) {
                        modifiedTree = addGetterMethod(copy, modifiedTree, fieldInfo);
                        modifiedTree = addSetterMethod(copy, modifiedTree, fieldInfo, bean);
                    }

                    modifiedTree = addGetUriMethod(copy, modifiedTree);
                    modifiedTree = addSetUriMethod(copy, modifiedTree);
                    modifiedTree = addItemConverterGetEntityMethod(copy, modifiedTree, bean);
                    modifiedTree = addItemConverterResolveEntityMethod(copy, modifiedTree, bean);
                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String[] getContainerResourceImports(EntityResourceBean bean) {
        List<String> imports = new ArrayList<String>(Arrays.asList(CONTAINER_IMPORTS));

        Set<String> entityClasses = new HashSet<String>();
        EntityResourceBean itemBean = getItemSubResource(bean);

        for (RelatedEntityResource resource : itemBean.getSubResources()) {
            entityClasses.add(getEntityClassType(resource.getResourceBean()));
        }

        imports.addAll(entityClasses);
        imports.add(getConverterType(bean));
        imports.add(getConverterType(getItemConverterBean(bean)));

        if (injectEntityManager) {
            imports.add(Constants.PERSISTENCE_CONTEXT);
        }

        imports.addAll(getAdditionalContainerResourceImports(bean));

        return imports.toArray(new String[imports.size()]);
    }

    protected List<String> getAdditionalContainerResourceImports(EntityResourceBean bean) {
        return Collections.emptyList();
    }

    private Collection<String> getRelatedClasses(EntityResourceBean bean) {
        Set<String> classes = new HashSet<String>();
        for (RelatedEntityResource resource : bean.getSuperResources()) {
            classes.add(getEntityClassType(resource.getResourceBean()));
        }

        for (RelatedEntityResource resource : bean.getSubResources()) {
            EntityResourceBean subBean = resource.getResourceBean();

            if (subBean.isContainer()) {
                classes.add(resource.getFieldInfo().getType());
            } else {
                //Only add non java.lang types.
                String type = getIdFieldType(subBean);
                if (!type.startsWith("java.lang.")) {
                    classes.add(type);
                }
            }
        }

        return classes;
    }

    private String[] getItemResourceImports(EntityResourceBean bean) {
        List<String> imports = new ArrayList<String>(Arrays.asList(ITEM_IMPORTS));

        imports.addAll(getRelatedClasses(bean));
        imports.add(getConverterType(bean));

        imports.addAll(getAdditionalItemResourceImports(bean));

        return imports.toArray(new String[imports.size()]);
    }

    protected List<String> getAdditionalItemResourceImports(EntityResourceBean bean) {
        return Collections.emptyList();
    }

    private EntityResourceBean getItemConverterBean(EntityResourceBean containerBean) {
        return model.getItemResourceBean(containerBean.getEntityClassInfo());
    }

    private String[] getItemConverterImports(EntityResourceBean bean) {
        List<String> imports = new ArrayList<String>(Arrays.asList(ITEM_CONVERTER_IMPORTS));
        imports.add(getEntityClassType(bean));
        imports.addAll(getRelatedClasses(bean));

        return imports.toArray(new String[imports.size()]);
    }

    private ClassTree addResourceBeanFields(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        ClassTree modifiedTree = tree;
        Modifier[] modifiers = new Modifier[]{Modifier.PROTECTED};

        // Add id field for item resource
        if (!bean.isContainer()) {
            String ids[] = getIdFieldIdArray(bean, false, null);
            Object types[] = getIdFieldTypeArray(bean, false, null);

            for (int i = 0; i < ids.length; i++) {
                modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                        null, null, ids[i], (String) types[i]);  //NOI18N

            }
        }

        // Add @PersistenceContext EntityManager em
        if (injectEntityManager) {
            modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                    bean.isContainer() ? new String[]{Constants.PERSISTENCE_CONTEXT_ANNOTATION} : null,
                    bean.isContainer() ? new Object[]{JavaSourceHelper.createAssignmentTree(copy, "unitName",
                        persistenceUnit.getName())
                    } : null,
                    "em", Constants.ENTITY_MANAGER_TYPE);  //NOI18N

        }

        // Add @Context UriInfo context
        String[] annotations = new String[]{RestConstants.CONTEXT_ANNOTATION};
        modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                annotations, null, "uriInfo", RestConstants.URI_INFO);  //NOI18N

        if (!useEjbInjections) {
            modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                    annotations, null, "resourceContext", RestConstants.RESOURCE_CONTEXT);  //NOI18N
        } else {
            annotations = new String[]{RestConstants.EJB};
            modifiers = new Modifier[]{Modifier.PRIVATE};
            if (bean.isContainer()) {
                for (RelatedEntityResource subResource: bean.getSubResources()) {
                    EntityResourceBean subResourceBean = subResource.getResourceBean();
                    String subResourceType = getResourceType(subResourceBean);
                    String subResourceId = lowerCaseFirstLetter(getResourceName(subResourceBean));
                    modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                            annotations, null, subResourceId, subResourceType);
                }
            } else {
                for (RelatedEntityResource relatedResource : bean.getSubResources()) {
                    FieldInfo fieldInfo = relatedResource.getFieldInfo();
                    String subResourceType = capitalizeFirstLetter(getResourceNameFromField(fieldInfo));
                    String subResourceId = lowerCaseFirstLetter(getResourceNameFromField(fieldInfo));
                    modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                            annotations, null, subResourceId , subResourceType);
                }
            }
        }

        return modifiedTree;
    }

    private ClassTree addResourceBeanAccessors(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        if (!bean.isContainer()) {
            String ids[] = getIdFieldIdArray(bean, false, null);
            Object types[] = getIdFieldTypeArray(bean, false, null);

            for (int i = 0; i < ids.length; i++) {
                tree = addAccessorMethods(copy, tree, ids[i], types[i]);
            }

            if (injectEntityManager) {
                tree = addAccessorMethods(copy, tree, "em", Constants.ENTITY_MANAGER_TYPE); //
            }
        }

        return tree;
    }

    protected ClassTree addAccessorMethods(WorkingCopy copy, ClassTree tree, String name, Object type) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};

//        String methodName = "get" + capitalizeFirstLetter(name);
//        String bodyText = "{ return " + name + ";}";            //NOI18N
//
//        tree = JavaSourceHelper.addMethod(copy, tree,
//                modifiers, null, null, methodName,
//                type, null, null,
//                null, null, bodyText, null);

        String methodName = "set" + capitalizeFirstLetter(name);
        String bodyText = "{ this." + name + " = " + name + ";}";            //NOI18N

        String[] params = new String[]{name};
        Object[] paramTypes = new Object[]{type};

        tree = JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null, methodName,
                Constants.VOID, params, paramTypes,
                null, null, bodyText, null);

        return tree;
    }

    private ClassTree addContainerGetMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = HttpMethodType.GET.prefix();
        String[] annotations = combineStringArrays(new String[]{
                    RestConstants.GET_ANNOTATION,
                    RestConstants.PRODUCE_MIME_ANNOTATION
                }, getAdditionalContainerGetMethodAnnotations());

        Object[] annotationAttrs = combineObjectArrays(new Object[]{
                    null,
                    JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
                }, getAdditionalContainerGetMethodAnnotationAttrs());

        Object returnType = getConverterType(bean);

        String[] parameters = new String[]{"start", "max", "expandLevel", "query"};        //NOI18N

        String intType = Integer.TYPE.getName();
        Object[] paramTypes = new String[]{intType, intType, intType, String.class.getSimpleName()};          //NOI18N

        String[][] paramAnnotations = new String[][]{
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION},
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION},
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION},
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION}
        };

        Object[][] paramAnnotationAttrs = new Object[][]{
            {"start", "0"},
            {"max", "10"},
            {"expandLevel", "1"},
            {"query", "SELECT e FROM " + getEntityClassName(bean) + " e"}
        };

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                    "try {" +
                    "persistenceSvc.beginTx();";
        }

        bodyText += "return new $CONVERTER$(getEntities(start, max, query), uriInfo.getAbsolutePath(), expandLevel);";

        if (!injectEntityManager) {
            bodyText += "} finally {" +
                    "persistenceSvc.commitTx();" +
                    "persistenceSvc.close();" +
                    "}";
        }

        bodyText += "}";
        bodyText = bodyText.replace("$CONVERTER$", getConverterName(bean));

        String comment = "Get method for retrieving a collection of $CLASS$ instance in XML format.\n\n" +
                "@return an instance of $CONVERTER$";
        comment = comment.replace("$CLASS$", getEntityClassName(bean)).
                replace("$CONVERTER$", getConverterName(bean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType, parameters, paramTypes,
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N

    }

    protected String[] getAdditionalContainerGetMethodAnnotations() {
        return null;
    }

    protected Object[] getAdditionalContainerGetMethodAnnotationAttrs() {
        return null;
    }

    private ClassTree addContainerPostMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        String methodName = HttpMethodType.POST.prefix();
        String[] annotations = combineStringArrays(new String[]{
                    RestConstants.POST_ANNOTATION,
                    RestConstants.CONSUME_MIME_ANNOTATION
                }, getAdditionalContainerPostMethodAnnotations());

        Object[] annotationAttrs = combineObjectArrays(new Object[]{
                    null,
                    JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
                }, getAdditionalContainerPostMethodAnnotationAttrs());

        //Object returnType = getConverterType(model.getItemResourceBean(bean.getEntityClassInfo()));
        Object returnType = RestConstants.HTTP_RESPONSE;

        String[] params = new String[]{"data"};
        Object[] paramTypes = new Object[]{getItemConverterName(bean)};

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                    "try {" +
                    "persistenceSvc.beginTx();" +
                    "EntityManager em = persistenceSvc.getEntityManager();";
        }

        bodyText += this.getEntityClassName(bean) + " entity = data.resolveEntity(em);" +
                "createEntity(data.resolveEntity(em));";

        if (!injectEntityManager) {
            bodyText += "persistenceSvc.commitTx();";
        }

        bodyText += "return Response.created(uriInfo.getAbsolutePath().resolve($ID_TO_URI$ + \"/\")).build();";

        if (!injectEntityManager) {
            bodyText += "} finally {" +
                    "persistenceSvc.close();" +
                    "}";
        }

        bodyText += "}";

        bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean)).
                replace("$ID_TO_URI$", getIdFieldToUriStmt(bean));

        String comment = "Post method for creating an instance of $CLASS$ using XML as the input format.\n\n" +
                "@param data an $CONVERTER$ entity that is deserialized from an XML stream\n" +
                "@return an instance of $CONVERTER$";

        comment = comment.replace("$CLASS$", getEntityClassName(bean)).
                replace("$CONVERTER$", getItemConverterName(bean));

        return JavaSourceHelper.addMethod(copy, tree, Constants.PUBLIC, annotations,
                annotationAttrs, methodName, returnType,
                params, paramTypes, null, null,
                bodyText, comment);
    }

    protected String[] getAdditionalContainerPostMethodAnnotations() {
        return null;
    }

    protected Object[] getAdditionalContainerPostMethodAnnotationAttrs() {
        return null;
    }

    private ClassTree addContainerGetResourceMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, RelatedEntityResource relatedResource) {
        EntityResourceBean subBean = relatedResource.getResourceBean();
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = new String[]{RestConstants.PATH_ANNOTATION};
        String[] annotationAttrs = new String[]{subBean.getUriTemplate()};
        String returnType = getResourceType(subBean);
        String resourceName = getResourceName(subBean);
        String methodName = "get" + resourceName;

        String[] parameters = getIdFieldIdArray(subBean, false, null);
        Object[] paramTypes = getIdFieldTypeArray(subBean, false, null);
        String[] paramAnnotations = getIdFieldUriParamArray(subBean, false, null);
        Object[] paramAnnotationAttrs = getIdFieldNameArray(subBean, false, null);

        String subResourceId = lowerCaseFirstLetter(resourceName);
        String bodyText = "{"; //NOI18N
        if (!useEjbInjections) {
            bodyText = "{$CLASS$ "+ subResourceId + " = resourceContext.getResource($CLASS$.class);";
            bodyText = bodyText.replace("$CLASS$", getResourceName(subBean));
        }

        for (int i = 0; i < parameters.length; i++) {
            String id = parameters[i];
            bodyText += subResourceId + ".set" + capitalizeFirstLetter(id) + "(" + id + ");";
            if (injectEntityManager) {
                bodyText += subResourceId + ".setEm(em);";
            }
        }

        bodyText += "return " + subResourceId + ";}";

        String comment = "Returns a dynamic instance of $RESOURCE$ used for entity navigation.\n\n" +
                "@return an instance of $RESOURCE$";

        comment = comment.replace("$RESOURCE$", getResourceName(subBean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType,
                parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);
    }

    private ClassTree addGetEntitiesMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PROTECTED};
        Tree returnType = JavaSourceHelper.createParameterizedTypeTree(copy,
                Constants.COLLECTION_TYPE,
                new String[]{getEntityClassType(bean)});

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += GET_ENTITY_MANAGER_STMT;
        }

        bodyText += "return em.createQuery(query).setFirstResult(start).setMaxResults(max).getResultList();" +
                "}";

        String[] parameters = new String[]{"start", "max", "query"};
        Object[] paramTypes = new Object[]{"int", "int", "String"};

        bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean));

        String comment = "Returns all the entities associated with this resource.\n\n" +
                "@return a collection of $CLASS$ instances";
        comment = comment.replace("$CLASS$", getEntityClassName(bean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null,
                "getEntities", returnType, parameters, paramTypes, null, null,
                bodyText, comment);
    }

    private ClassTree addCreateEntityMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PROTECTED};
        Object returnType = Constants.VOID; //getEntityClassType(bean);

        String[] params = new String[]{"entity"};
        Object[] paramTypes = new Object[]{getEntityClassType(bean)};

        FieldInfo idField = bean.getEntityClassInfo().getIdFieldInfo();

        String bodyText = "{";

        if (idField.isGeneratedValue()) {
            bodyText += "entity." + getSetterName(idField) + "(null);";
        }

        if (!injectEntityManager) {
            bodyText += GET_ENTITY_MANAGER_STMT;
        }

        bodyText += "em.persist(entity);";
        bodyText = bodyText + getCreateRelationshipsSubText(getItemSubResource(bean)) +
                "}";

        String comment = "Persist the given entity.\n\n" +
                "@param entity the entity to persist";

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null,
                "createEntity", returnType, params, paramTypes,
                null, null, bodyText, comment);
    }

    private String getCreateRelationshipsSubText(EntityResourceBean bean) {
        String oneToOneTemplate = "$CLASS$ $FIELD$ = entity.$GETTER$();" +
                "if ($FIELD$ != null) {" +
                "$FIELD$.$REVERSE_SETTER$(entity);" +
                "}";                                                //NOI18N

        String manyToOneTemplate = "$CLASS$ $FIELD$ = entity.$GETTER$();" +
                "if ($FIELD$ != null) {" +
                "$FIELD$.$REVERSE_GETTER$().add(entity);" +
                "}";                                                //NOI18N

        String oneToManyTemplate = "for ($CLASS$ value : entity.$GETTER$()) {" +
                "$ENTITY_CLASS$ oldEntity = value.$REVERSE_GETTER$();" +
                "value.$REVERSE_SETTER$(entity);" +
                "if (oldEntity != null) {" +
                "oldEntity.$GETTER$().remove(value);" +
                "}" +
                "}";                                                //NOI18N

        String manyToManyTemplate = "for ($CLASS$ value : entity.$GETTER$()) {" +
                "value.$REVERSE_GETTER$().add(entity);" +
                "}";                                                //NOI18N

        String bodyText = "";

        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo reverseFieldInfo = subResource.getReverseFieldInfo();

            if (reverseFieldInfo == null) {
                continue;
            }
            FieldInfo fieldInfo = subResource.getFieldInfo();
            String template = "";

            if (fieldInfo.isOneToOne()) {
                template = oneToOneTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_SETTER$", getSetterName(reverseFieldInfo));
            } else if (fieldInfo.isManyToOne()) {
                template = manyToOneTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo));
            } else if (fieldInfo.isOneToMany()) {
                template = oneToManyTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeArgName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$ENTITY_CLASS$", getEntityClassName(bean)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo)).
                        replace("$REVERSE_SETTER$", getSetterName(reverseFieldInfo));
            } else if (fieldInfo.isManyToMany()) {
                template = manyToManyTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeArgName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo));
            }

            bodyText += template;
        }

        return bodyText;
    }

    private ClassTree addItemGetMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = HttpMethodType.GET.prefix();

        String[] annotations = combineStringArrays(new String[]{
                    RestConstants.GET_ANNOTATION,
                    RestConstants.PRODUCE_MIME_ANNOTATION
                }, getAdditionalItemGetMethodAnnotations());

        Object[] annotationAttrs = combineObjectArrays(new Object[]{
                    null,
                    JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
                }, getAdditionalItemGetMethodAnnotationAttrs());

        Object returnType = getConverterType(bean);
        String[] parameters = new String[]{"expandLevel"};        //NOI18N
        Object[] paramTypes = new String[]{Integer.TYPE.getName()};          //NOI18N

        String[][] paramAnnotations = new String[][]{
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION}
        };

        Object[][] paramAnnotationAttrs = new Object[][]{
            {"expandLevel", "1"},
        };
        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                    "try {" +
                    "persistenceSvc.beginTx();";
        }

        bodyText += "return  new $CONVERTER$(getEntity(), uriInfo.getAbsolutePath(), expandLevel);";

        if (!injectEntityManager) {
            bodyText += "} finally {" +
                    "PersistenceService.getInstance().close();" +
                    "}";
        }

        bodyText += "}";
        bodyText = bodyText.replace("$CONVERTER$", getConverterName(bean));

        String comment = "Get method for retrieving an instance of $CLASS identified by id in XML format.\n\n" +
                "@param id identifier for the entity\n" +
                "@return an instance of $CONVERTER$";
        comment = comment.replace("$CLASS", getEntityClassName(bean)).
                replace("$CONVERTER$", getConverterName(bean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType,
                parameters, paramTypes, paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N

    }

    protected String[] getAdditionalItemGetMethodAnnotations() {
        return null;
    }

    protected Object[] getAdditionalItemGetMethodAnnotationAttrs() {
        return null;
    }

    private ClassTree addItemPutMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = HttpMethodType.PUT.prefix();

        String[] annotations = combineStringArrays(new String[]{
                    RestConstants.PUT_ANNOTATION,
                    RestConstants.CONSUME_MIME_ANNOTATION
                }, getAdditionalItemPutMethodAnnotations());

        Object[] annotationAttrs = combineObjectArrays(new Object[]{
                    null,
                    JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
                }, getAdditionalItemPutMethodAnnotationAttrs());

        String[] params = new String[]{"data"};
        Object[] paramTypes = new String[]{getConverterType(bean)};

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                    "try {" +
                    "persistenceSvc.beginTx();" +
                    "EntityManager em = persistenceSvc.getEntityManager();";
        }

        bodyText += "updateEntity(getEntity(), data.resolveEntity(em));";

        if (!injectEntityManager) {
            bodyText += "persistenceSvc.commitTx();" +
                    "} finally {" +
                    "persistenceSvc.close();" +
                    "}";
        }

        bodyText += "}";

        String comment = "Put method for updating an instance of $CLASS identified by id using XML as the input format.\n\n" +
                "@param id identifier for the entity\n" +
                "@param data an $CONVERTER$ entity that is deserialized from a XML stream\n";

        comment = comment.replace("$CLASS", getEntityClassName(bean)).
                replace("$CONVERTER$", getConverterName(bean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, Constants.VOID,
                params, paramTypes, null, null,
                bodyText, comment);
    }

    protected String[] getAdditionalItemPutMethodAnnotations() {
        return null;
    }

    protected Object[] getAdditionalItemPutMethodAnnotationAttrs() {
        return null;
    }

    private ClassTree addItemDeleteMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        Object returnType = Constants.VOID;

        String[] annotations = combineStringArrays(new String[]{
                    RestConstants.DELETE_ANNOTATION
                }, getAdditionalItemDeleteMethodAnnotations());

        Object[] annotationAttrs = combineObjectArrays(new Object[]{null},
                getAdditionalItemDeleteMethodAnnotationAttrs());

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                    "try {" +
                    "persistenceSvc.beginTx();";
        }

        bodyText += "deleteEntity(getEntity());";

        if (!injectEntityManager) {
            bodyText += "persistenceSvc.commitTx();" +
                    "} finally {" +
                    "persistenceSvc.close();" +
                    "}";
        }

        bodyText += "}";
        bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean));

        String comment = "Delete method for deleting an instance of $CLASS identified by id.\n\n" +
                "@param id identifier for the entity\n";
        comment = comment.replace("$CLASS", getEntityClassName(bean));

        return JavaSourceHelper.addMethod(copy, tree, modifiers, annotations,
                annotationAttrs, HttpMethodType.DELETE.prefix(), returnType,
                null, null, null, null, bodyText, comment);
    }

    protected String[] getAdditionalItemDeleteMethodAnnotations() {
        return null;
    }

    protected Object[] getAdditionalItemDeleteMethodAnnotationAttrs() {
        return null;
    }

    private ClassTree addSubresourceClass(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, RelatedEntityResource relatedResource) {
        EntityResourceBean subBean = relatedResource.getResourceBean();
        FieldInfo fieldInfo = relatedResource.getFieldInfo();
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.STATIC};
        String resourceName = getResourceName(subBean);
        String parentEntityClass = bean.getEntityClassInfo().getName();
        String className = capitalizeFirstLetter(getResourceNameFromField(fieldInfo));

        String[] annotations = null;
        Object[] annotationAttributes = null;
        if (useEjbInjections) {
            // creating @Stateless:name attribute
            String outerClassName = copy.getCompilationUnit().getSourceFile().getName();
            if (outerClassName.endsWith(".java")) { //NOI18N
                outerClassName=outerClassName.substring(0, outerClassName.length()-5);
            }
            String jndiName = outerClassName+"."+className; //NOI18N
            TreeMaker make = copy.getTreeMaker();
            ExpressionTree nameAttr = make.Assignment(make.Identifier("name"), make.Literal(jndiName)); //NOI18N

            annotationAttributes = new Object[] {nameAttr};
            annotations = new String[] {RestConstants.STATELESS_ANNOTATION};
        }

        ClassTree classTree = JavaSourceHelper.createInnerClass(copy, modifiers,
                className, resourceName, annotations, annotationAttributes);

        classTree = JavaSourceHelper.addField(copy, classTree, new Modifier[]{Modifier.PRIVATE},
                null, null, "parent", parentEntityClass);
        classTree = addAccessorMethods(copy, classTree, "parent", parentEntityClass);

        String bodyText = null;
        modifiers = new Modifier[]{Modifier.PROTECTED};
        String methodName = null;
        Object returnType = null;
        annotations = new String[] {"Override"}; //NOI18N
        String[] params = null;
        String[] paramTypes = null;
        String getterName = getGetterName(fieldInfo);
        String entityClass = getEntityClassName(subBean);

        if (subBean.isItem()) {
            methodName = "getEntity";       //NOI18N    

            returnType = entityClass;

            bodyText = "{" +
                    "$CLASS$ entity = parent.$GETTER$();" +
                    "if (entity == null) {" +
                    "throw new WebApplicationException(new Throwable(\"Resource for \" + uriInfo.getAbsolutePath() + \" does not exist.\"), 404);" +
                    "}" +
                    "return entity;" +
                    "}";
            bodyText = bodyText.replace("$CLASS$", entityClass).replace("$GETTER$", getterName);
        } else {
            methodName = "getEntities";
            returnType = JavaSourceHelper.createParameterizedTypeTree(copy,
                    Constants.COLLECTION_TYPE,
                    new String[]{entityClass});
            params = new String[]{"start", "max", "query"};        //NOI18N

            paramTypes = new String[]{"int", "int", "String"};     //NOI18N

            bodyText = "{" +
                    "Collection<$CLASS$> result = new java.util.ArrayList<$CLASS$>();" +
                    "int index = 0;" +
                    "for ($CLASS$ e : parent.$GETTER$()) {" +
                    "if (index >= start && (index - start) < max) {" +
                    "result.add(e);" +
                    "}" +
                    "index++;" +
                    "}" +
                    "return result;" +
                    "}";

            bodyText = bodyText.replace("$CLASS$", entityClass).replace("$GETTER$", getterName);
        }

        classTree = JavaSourceHelper.addMethod(copy, classTree,
                modifiers, annotations, null,
                methodName, returnType, params, paramTypes, null, null,
                bodyText, null);

        return copy.getTreeMaker().addClassMember(tree, classTree);
    }

    private ClassTree addItemGetResourceMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, RelatedEntityResource relatedResource) {
        EntityResourceBean subBean = relatedResource.getResourceBean();
        FieldInfo fieldInfo = relatedResource.getFieldInfo();
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = combineStringArrays(
                new String[]{RestConstants.PATH_ANNOTATION},
                getAdditionalItemGetResourceMethodAnnotations());

        String uriTemplate = fieldInfo.getName() + "/";
        Object[] annotationAttrs = combineObjectArrays(
                new String[]{uriTemplate},
                getAdditionalItemGetResourceMethodAnnotationAttrs());
        Object returnType = getResourceType(subBean);
        String methodName = getGetterName(fieldInfo) + RESOURCE_SUFFIX;     //NOI18N
        String subResourceId = lowerCaseFirstLetter(getResourceNameFromField(fieldInfo));

        String bodyText = "{"; //NOI18N
        if (!useEjbInjections) {
            bodyText = "{$CLASS$ " + subResourceId + " = resourceContext.getResource($CLASS$.class);"; //NOI18N
            bodyText = bodyText.replace("$CLASS$",  //NOI18N
                    capitalizeFirstLetter(getResourceNameFromField(fieldInfo)));
        }
        bodyText += subResourceId+".setParent(getEntity());" + //NOI18N
                    "return " + subResourceId + ";" + //NOI18N
                    "}"; //NOI18N

        String comment = "Returns a dynamic instance of $RESOURCE$ used for entity navigation.\n\n" +
                "@param id identifier for the parent entity\n" +
                "@return an instance of $RESOURCE$";

        comment = comment.replace("$RESOURCE$", getResourceName(subBean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType, null, null, null, null,
                bodyText, comment);
    }

    protected String[] getAdditionalItemGetResourceMethodAnnotations() {
        return null;
    }
    
    protected Object[] getAdditionalItemGetResourceMethodAnnotationAttrs() {
        return null;
    }
    
    private ClassTree addGetEntityMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PROTECTED};
        FieldInfo idField = bean.getEntityClassInfo().getIdFieldInfo();

        String idString = "id";
        String newIdStatement = "";
        String idType = idField.getType();

        if (idField.isEmbeddedId()) {
            newIdStatement = idType + " id = new " + idType + "(" +
                    getIdFieldIdList(bean) + ");";
        } else {
            // Temporary workaround because Jersey does not support Character type
            // in UriParam.      
            if (idType.equals("java.lang.Character")) { //NOI18N

                idString = "id.charAt(0)";
            }
        }

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += GET_ENTITY_MANAGER_STMT;
        }

        bodyText += "try {" +
                "$NEW_ID_STMT$" +
                "return ($CLASS$) em.createQuery(\"SELECT e FROM $CLASS$ e where e.$ID$ = :$ID$\")." +
                "setParameter(\"$ID$\", $ID_STRING$).getSingleResult();" +
                "} catch (NoResultException ex) {" +
                "throw new WebApplicationException(new Throwable(\"Resource for \" + uriInfo.getAbsolutePath() + \" does not exist.\"), 404);" +
                "}" +
                "}";

        String id = getIdFieldName(bean);
        bodyText = bodyText.replace("$NEW_ID_STMT$", newIdStatement).
                replace("$CLASS$", getEntityClassName(bean)).
                replace("$ID$", id).replace("$ID_STRING$", idString);

        String comment = "Returns an instance of $CLASS$ identified by id.\n\n" +
                "@param id identifier for the entity\n" +
                "@return an instance of $CLASS$";
        comment = comment.replace("$CLASS$", getEntityClassName(bean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null,
                "getEntity", getEntityClassType(bean),
                null, null, null, null,
                bodyText, comment);
    }

    private ClassTree addUpdateEntityMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE};
        Object returnType = getEntityClassType(bean);
        String[] params = new String[]{"entity", "newEntity"};
        Object[] paramTypes = new Object[]{
            getEntityClassType(bean), getEntityClassType(bean)
        };

//        String bodyText = "{ newEntity.$SETTER$(entity.$GETTER$();";
//        bodyText = bodyText.replace("$SETTER$", getIdSetter(bean)).
//                replace("$GETTER$", getIdGetter(bean));

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += GET_ENTITY_MANAGER_STMT;
        }

        bodyText += getUpdateBeforeRelationshipsSubText(bean);

        bodyText = bodyText +
                "entity = em.merge(newEntity);";

        bodyText = bodyText + getUpdateRelationshipsSubText(bean) +
                "return entity;}";

        String comment = "Updates entity using data from newEntity.\n\n" +
                "@param entity the entity to update\n" +
                "@param newEntity the entity containing the new data\n" +
                "@return the updated entity";

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null,
                "updateEntity", returnType, params, paramTypes,
                null, null, bodyText, comment);
    }

    private String getUpdateBeforeRelationshipsSubText(EntityResourceBean bean) {
        String bodyText = "";

        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo fieldInfo = subResource.getFieldInfo();

            String template = "";
            if (fieldInfo.isOneToOne() || fieldInfo.isManyToOne()) {
                template += "$CLASS$ $FIELD$ = entity.$GETTER$();" +
                        "$CLASS$ $FIELD$New = newEntity.$GETTER$();";
                template = template.replace("$CLASS$", fieldInfo.getSimpleTypeName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo));
            } else if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                template += "$HOLDER$<$CLASS$> $FIELD$ = entity.$GETTER$();" +
                        "$HOLDER$<$CLASS$> $FIELD$New = newEntity.$GETTER$();";
                template = template.replace("$HOLDER$", fieldInfo.getSimpleTypeName()).
                        replace("$CLASS$", fieldInfo.getSimpleTypeArgName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo));
            }

            bodyText += template;
        }

        return bodyText;
    }

    private String getUpdateRelationshipsSubText(EntityResourceBean bean) {
        String oneToOneTemplate = "if ($FIELD$ != null && !$FIELD$.equals($FIELD$New)) {" +
                "$FIELD$.$REVERSE_SETTER$(null);" +
                "}" +
                "if ($FIELD$New != null && !$FIELD$New.equals($FIELD$)) {" +
                "$FIELD$New.$REVERSE_SETTER$(entity);" +
                "}";

        String manyToOneTemplate = "if ($FIELD$ != null && !$FIELD$.equals($FIELD$New)) {" +
                "$FIELD$.$REVERSE_GETTER$().remove(entity);" +
                "}" +
                "if ($FIELD$New != null && !$FIELD$New.equals($FIELD$)) {" +
                "$FIELD$New.$REVERSE_GETTER$().add(entity);" +
                "}";

        String oneToManyTemplate = "for ($CLASS$ value : $FIELD$) {" +
                "if (!$FIELD$New.contains(value)) {" +
                "throw new WebApplicationException(new Throwable(\"Cannot remove items from $FIELD$\"));" +
                "}" +
                "}" +
                "for ($CLASS$ value : $FIELD$New) {" +
                "if (!$FIELD$.contains(value)) {" +
                "$ENTITY_CLASS$ oldEntity = value.$REVERSE_GETTER$();" +
                "value.$REVERSE_SETTER$(entity);" +
                "if (oldEntity != null && !oldEntity.equals(entity)) {" +
                "oldEntity.$GETTER$().remove(value);" +
                "}" +
                "}" +
                "}";

        String manyToManyTemplate = "for ($CLASS$ value : $FIELD$) {" +
                "if (!$FIELD$New.contains(value)) {" +
                "value.$REVERSE_GETTER$().remove(entity);" +
                "}" +
                "}" +
                "for ($CLASS$ value : $FIELD$New) {" +
                "if (!$FIELD$.contains(value)) {" +
                "value.$REVERSE_GETTER$().add(entity);" +
                "}" +
                "}";                                            //NOI18N

        String bodyText = "";

        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo reverseFieldInfo = subResource.getReverseFieldInfo();

            if (reverseFieldInfo == null) {
                continue;
            }
            FieldInfo fieldInfo = subResource.getFieldInfo();
            String template = "";

            if (fieldInfo.isOneToOne()) {
                template = oneToOneTemplate.replace("$FIELD$", fieldInfo.getName()).
                        replace("$REVERSE_SETTER$", getSetterName(reverseFieldInfo));
            } else if (fieldInfo.isManyToOne()) {
                template = manyToOneTemplate.replace("$FIELD$", fieldInfo.getName()).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo));
            } else if (fieldInfo.isOneToMany()) {
                template = oneToManyTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeArgName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$ENTITY_CLASS$", getEntityClassName(bean)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo)).
                        replace("$REVERSE_SETTER$", getSetterName(reverseFieldInfo));
            } else if (fieldInfo.isManyToMany()) {
                template = manyToManyTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeArgName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo));
            }

            bodyText += template;
        }

        return bodyText;
    }

    private ClassTree addDeleteEntityMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE};
        String methodName = "deleteEntity";                 //NOI18N

        Object returnType = Constants.VOID;
        String[] params = new String[]{"entity"};           //NOI18N

        Object[] paramTypes = new Object[]{
            getEntityClassType(bean)
        };

        String bodyText = "{";

        if (!injectEntityManager) {
            bodyText += GET_ENTITY_MANAGER_STMT;
        }

        bodyText += getDeleteRelationshipsSubText(bean);        //NOI18N

        bodyText += "em.remove(entity);}";   //NOI18N

        String comment = "Deletes the entity.\n\n" +
                "@param entity the entity to deletle\n";                    //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null,
                methodName, returnType, params, paramTypes,
                null, null, bodyText, comment);
    }

    private String getDeleteRelationshipsSubText(EntityResourceBean bean) {
        String oneToOneTemplate = "$CLASS$ $FIELD$ = entity.$GETTER$();" +
                "if ($FIELD$ != null) {" +
                "$FIELD$.$REVERSE_SETTER$(null);" +
                "}";                                                //NOI18N

        String manyToOneTemplate = "$CLASS$ $FIELD$ = entity.$GETTER$();" +
                "if ($FIELD$ != null) {" +
                "$FIELD$.$REVERSE_GETTER$().remove(entity);" +
                "}";                                                //NOI18N

        String oneToManyTemplate = "if (!entity.$GETTER$().isEmpty()) {" +
                "throw new WebApplicationException(new Throwable(\"Cannot delete entity because $FIELD$ is not empty.\"));" +
                "}";

        String manyToManyTemplate = "for ($CLASS$ value : entity.$GETTER$()) {" +
                "value.$REVERSE_GETTER$().remove(entity);" +
                "}";                                                //NOI18N

        String bodyText = "";

        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo reverseFieldInfo = subResource.getReverseFieldInfo();

            if (reverseFieldInfo == null) {
                continue;
            }
            FieldInfo fieldInfo = subResource.getFieldInfo();
            String template = "";

            if (fieldInfo.isOneToOne()) {
                template = oneToOneTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_SETTER$", getSetterName(reverseFieldInfo));
            } else if (fieldInfo.isManyToOne()) {
                template = manyToOneTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeName()).
                        replace("$FIELD$", fieldInfo.getName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo));
            } else if (fieldInfo.isOneToMany()) {
                template = oneToManyTemplate.replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$FIELD$", fieldInfo.getName());
            } else if (fieldInfo.isManyToMany()) {
                template = manyToManyTemplate.replace("$CLASS$", fieldInfo.getSimpleTypeArgName()).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$REVERSE_GETTER$", getGetterName(reverseFieldInfo));
            }

            bodyText += template;
        }

        return bodyText;
    }

    private ClassTree addConverterFields(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE};
        ClassTree modifiedTree = tree;

        modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                null, null, "expandLevel", Integer.TYPE.getName());   //NOI18N            

        modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                null, null, "uri", Constants.URI_TYPE);             //NOI18N            

        if (bean.isItem()) {
            modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                    null, null, "entity", getEntityClassType(bean));    //NOI18N   

        } else {
            Tree typeTree = JavaSourceHelper.createParameterizedTypeTree(copy,
                    Constants.COLLECTION_TYPE, new String[]{getItemConverterType(bean)});

            modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                    null, null, "items", typeTree);     //NOI18N

            typeTree = JavaSourceHelper.createParameterizedTypeTree(copy,
                    Constants.COLLECTION_TYPE, new String[]{getEntityClassType(bean)});

            modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                    null, null, "entities", typeTree);      //NOI18N

        }

        return modifiedTree;
    }

    private ClassTree addConverterConstructor(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        String[] parameters = null;
        Object[] types = null;
        String bodyText = null;
        String comment = null;
        String converterName = getConverterName(bean);

        if (bean.isItem()) {
            types = new String[]{getEntityClassType(bean),
                        Constants.URI_TYPE, Integer.TYPE.getName(), Boolean.TYPE.getName()
                    };

            parameters = new String[]{"entity", "uri", "expandLevel", "isUriExtendable"};      //NOI18N

            bodyText = "{this.entity = entity;" +
                    "this.uri = (isUriExtendable) ? UriBuilder.fromUri(uri).path(" +
                    getIdFieldToUriStmt(bean.getEntityClassInfo().getIdFieldInfo()) + " + \"/\").build() : uri;" +
                    "this.expandLevel = expandLevel;";
            
            for (FieldInfo f : bean.getEntityClassInfo().getFieldInfos()) {
                if (f.isRelationship()) {
                    bodyText += getGetterName(f) + "();";
                }
            }
            
            bodyText += "}";         //NOI18N

            comment = "Creates a new instance of " + converterName + ".\n\n" +
                    "@param entity associated entity\n" +
                    "@param uri associated uri\n" +
                    "@param expandLevel indicates the number of levels the entity graph should be expanded" +
                    "@param isUriExtendable indicates whether the uri can be extended";         //NOI18N 

            tree = JavaSourceHelper.addConstructor(copy, tree,
                    new Modifier[]{Modifier.PUBLIC},
                    parameters, types, bodyText, comment);

            parameters = new String[]{"entity", "uri", "expandLevel"};      //NOI18N

            bodyText = "{this(entity, uri, expandLevel, false);}";      //NOI18N

            comment = "Creates a new instance of " + converterName + ".\n\n" +
                    "@param entity associated entity\n" +
                    "@param uri associated uri\n" +
                    "@param expandLevel indicates the number of levels the entity graph should be expanded";        //NOI18N

        } else {
            parameters = new String[]{"entities", "uri", "expandLevel"};       //NOI18N

            Tree typeTree = JavaSourceHelper.createParameterizedTypeTree(copy,
                    Constants.COLLECTION_TYPE,
                    new String[]{getEntityClassType(bean)});

            types = new Object[]{typeTree, Constants.URI_TYPE, Integer.TYPE.getName()};

            bodyText = "{" +
                    "this.entities = entities; " +
                    "this.uri = uri; " +
                    "this.expandLevel = expandLevel;" +
                    "get" + getItemName(bean) + "();" +
                    "}"; //NOI18N

            comment = "Creates a new instance of " + converterName + ".\n\n" +
                    "@param entities associated entities\n" +
                    "@param uri associated uri\n" +
                    "@param expandLevel indicates the number of levels the entity graph should be expanded";  //NOI18N

        }

        return JavaSourceHelper.addConstructor(copy, tree,
                new Modifier[]{Modifier.PUBLIC},
                parameters, types, bodyText, comment);
    }

    private ClassTree addGetItemsMethod(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = new String[]{Constants.XML_ELEMENT_ANNOTATION};
        String methodName = "get" + getItemName(bean);              //NOI18

        Object returnType = JavaSourceHelper.createParameterizedTypeTree(copy,
                Constants.COLLECTION_TYPE,
                new String[]{getItemConverterType(bean)});

        String itemConverterName = getItemConverterName(bean);
        String bodyText = "{ " +
                "if (items == null) {" +
                "items = new ArrayList<$ITEM_CONVERTER$>();" +
                "}" +
                "if (entities != null) {" +
                "items.clear();" +
                "for ($CLASS$ entity : entities) {" +
                "items.add(new $ITEM_CONVERTER$(entity, uri, expandLevel, true));" +
                "}" +
                "}" +
                "return items;" +
                "}";       //NOI18N 

        bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean)).
                replace("$ITEM_CONVERTER$", itemConverterName);             //NOI18N

        String comment = "Returns a collection of $ITEM_CONVERTER$.\n\n" +
                "@return a collection of $ITEM_CONVERTER$";                 //NOI18M

        comment = comment.replace("$ITEM_CONVERTER$", itemConverterName);   //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, null,
                methodName, returnType, null, null, null, null,
                bodyText, comment);
    }

    private ClassTree addSetItemsMethod(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        String[] args = new String[]{"items"};           //NOI18N

        Object argType = JavaSourceHelper.createParameterizedTypeTree(copy,
                Constants.COLLECTION_TYPE,
                new String[]{getItemConverterType(bean)});
        String methodName = "set" + getItemName(bean);              //NOI18

        String bodyText = "{this.items = items;}";         //NOI18N

        String comment = "Sets a collection of $ITEM_CONVERTER$.\n\n" +
                "@param a collection of $ITEM_CONVERTER$ to set";           //NOI18N

        comment = comment.replace("$ITEM_CONVERTER$", getItemConverterName(bean));

        return JavaSourceHelper.addMethod(copy, tree, Constants.PUBLIC,
                null, null, methodName, Constants.VOID,
                args, new Object[]{argType}, null, null,
                bodyText, comment);
    }

    private ClassTree addGetUriMethod(WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = new String[]{Constants.XML_ATTRIBUTE_ANNOTATION};
        Object returnType = Constants.URI_TYPE;

        String bodyText = "{return uri;}";              //NOI18N

        String comment = "Returns the URI associated with this converter.\n\n" +
                "@return the uri";          //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, null,
                "getUri", returnType, null, null, null, null,
                bodyText, comment);         //NOI18N

    }

    private ClassTree addSetUriMethod(WorkingCopy copy, ClassTree tree) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        Object returnType = Constants.VOID;
        String[] params = new String[]{"uri"};     //NOI18N

        Object[] paramTypes = new Object[]{Constants.URI_TYPE};

        String bodyText = "{ this.uri = uri; }";        //NOI18N

        String comment = "Sets the URI for this reference converter.\n\n";      //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null, "setUri", returnType,
                params, paramTypes, null, null,
                bodyText, comment);     //NOI18N

    }

    private ClassTree addContainerConverterGetEntitiesMethod(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        String[] annotations = new String[]{Constants.XML_TRANSIENT_ANNOTATION};
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        Tree returnType = JavaSourceHelper.createParameterizedTypeTree(copy,
                Constants.COLLECTION_TYPE,
                new String[]{getEntityClassType(bean)});

        String entityClass = getEntityClassName(bean);
        String bodyText = "{ entities = new ArrayList<$CLASS$>();" +
                "if (items != null) { " +
                "for ($ITEM_CONVERTER$ item : items) {" +
                "entities.add(item.getEntity()); } " +
                "}" +
                "return entities;" +
                "}";        //NOI18N

        bodyText = bodyText.replace("$CLASS$", entityClass).
                replace("$ITEM_CONVERTER$", getItemConverterName(bean));

        String comment = "Returns a collection $CLASS$ entities.\n\n" +
                "@return a collection of $CLASS$ entities";
        comment = comment.replace("$CLASS$", entityClass);

        return JavaSourceHelper.addMethod(copy, tree, modifiers, annotations,
                null, "getEntities", returnType, null, null, null, null,
                bodyText, comment);         //NOI18N

    }

    private ClassTree addGetterMethod(WorkingCopy copy, ClassTree tree,
            FieldInfo fieldInfo) {
        String getterName = getGetterName(fieldInfo);
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = new String[]{Constants.XML_ELEMENT_ANNOTATION};
        Object[] annotationAttrs = null;
        Object returnType = null;
        String bodyText = null;

        if (!fieldInfo.isRelationship()) {
            if (fieldInfo.getTypeArg() == null) {
                returnType = fieldInfo.getType();
            } else {
                returnType = JavaSourceHelper.createParameterizedTypeTree(copy,
                        fieldInfo.getType(),
                        new String[]{fieldInfo.getTypeArg()});
            }

            bodyText = "{return (expandLevel > 0) ? entity.$GETTER$() : null;}";         //NOI18N

            bodyText = bodyText.replace("$GETTER$", getterName);                    //NOI18N

        } else {
            if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                EntityResourceBean foreignBean = model.getContainerResourceBean(fieldInfo.getTypeArg());
                returnType = getConverterType(foreignBean);

                bodyText = "{if (expandLevel > 0) {" +
                        "if (entity.$GETTER$() != null) {" +
                        "return new $CONVERTER$(entity.$GETTER$(), uri.resolve(\"$FIELD$/\"), expandLevel - 1);" +
                        "}" +
                        "}" +
                        "return null;" +
                        "}";        //NOI18N

                bodyText = bodyText.replace("$CONVERTER$", getConverterName(foreignBean)).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$FIELD$", fieldInfo.getName());        //NOI18N

            } else { // should be 1-1 or n-1

                EntityResourceBean foreignBean = model.getItemResourceBean(fieldInfo.getType());
                returnType = getConverterType(foreignBean);

                bodyText = "{if (expandLevel > 0) {" +
                        "if (entity.$GETTER$() != null) {" +
                        "return new $ITEM_CONVERTER$(entity.$GETTER$(), uri.resolve(\"$FIELD$/\"), expandLevel - 1, false);" +
                        "}" +
                        "}" +
                        "return null;}";
                bodyText = bodyText.replace("$ITEM_CONVERTER$", getItemConverterName(foreignBean)).
                        replace("$GETTER$", getGetterName(fieldInfo)).
                        replace("$FIELD$", fieldInfo.getName());
            }
        }

        String comment = "Getter for $FIELD$.\n\n" +
                "@return value for $FIELD$";
        comment = comment.replace("$FIELD$", fieldInfo.getName());

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getterName, returnType, null, null, null, null,
                bodyText, comment);
    }

    private ClassTree addSetterMethod(
            WorkingCopy copy, ClassTree tree, FieldInfo fieldInfo, EntityResourceBean bean) {
        String setterName = getSetterName(fieldInfo);
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = null;
        Object[] annotationAttrs = null;
        String[] args = new String[]{"value"};       //NOI18N

        Object argType = null;
        String bodyText = null;

        if (!fieldInfo.isRelationship()) {
            if (fieldInfo.getTypeArg() == null) {
                argType = fieldInfo.getType();
            } else {
                argType = JavaSourceHelper.createParameterizedTypeTree(copy,
                        fieldInfo.getType(),
                        new String[]{fieldInfo.getTypeArg()});
            }

            bodyText = "{ entity.$SETTER$(value); }";      //NOI18N

            bodyText = bodyText.replace("$SETTER$", setterName).
                    replace("$CLASS$", getEntityClassName(bean));       //NOI18N

        } else {
            if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                EntityResourceBean foreignBean = model.getContainerResourceBean(fieldInfo.getTypeArg());
                String relatedEntityName = model.getItemResourceBean(fieldInfo.getTypeArg()).getName();
                argType = getConverterType(foreignBean);
                bodyText = "{entity.$SETTER$((value != null) ? ";   //NOI18N

                String collectionType = Constants.COLLECTION_TYPE;

                String type = fieldInfo.getType();

                if (type.equals(Constants.LIST_TYPE)) {
                    collectionType = Constants.ARRAY_LIST_TYPE;
                } else if (type.equals(Constants.SET_TYPE)) {
                    collectionType = Constants.HASH_SET_TYPE;
                }

                if (collectionType.equals(Constants.COLLECTION_TYPE)) {
                    bodyText += "value.getEntities() : null);}";     //NOI18N
                } else {
                    bodyText += "new " + collectionType + "<" + fieldInfo.getSimpleTypeArgName() + ">" +
                            "(value.getEntities()) : null);}";
                }

                bodyText = bodyText.replace("$CLASS$", relatedEntityName). //NOI18N
                        replace("$SETTER$", setterName);        //NOI18N

            } else { // should be 1-1

                EntityResourceBean foreignBean = model.getItemResourceBean(fieldInfo.getType());
                argType = getConverterType(foreignBean);

                bodyText = "{entity.$SETTER$((value != null) ? value.getEntity() : null);}";        //NOI18N

                bodyText = bodyText.replace("$CLASS$", foreignBean.getName()).
                        replace("$SETTER$", setterName);            //NOI18N

            }
        }

        String comment = "Setter for $FIELD$.\n\n" +
                "@param value the value to set";
        comment = comment.replace("$FIELD$", fieldInfo.getName());

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                setterName, Constants.VOID, args, new Object[]{argType},
                null, null, bodyText, comment);
    }

    private ClassTree addItemConverterGetEntityMethod(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        String[] annotations = new String[]{Constants.XML_TRANSIENT_ANNOTATION};
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = "getEntity";        //NOI18N

        String returnType = getEntityClassType(bean);

        String className = this.getConverterName(bean);
        String bodyText = "{" +
                "if (entity.$ID_GETTER$() == null) {" +
                "$CLASS$ converter = UriResolver.getInstance().resolve($CLASS$.class, uri);" +
                "if (converter != null) {" +
                "entity = converter.getEntity();" +
                "}" +
                "}" +
                "return entity;" +
                "}";

        bodyText = bodyText.replace("$CLASS$", getConverterName(bean)).
                replace("$ID_GETTER$", getGetterName(bean.getEntityClassInfo().getIdFieldInfo()));

        String comment = "Returns the $CLASS$ entity.\n\n" +
                "@return an entity";                            //NOI18N

        comment = comment.replace("$CLASS$", getEntityClassName(bean));         //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, null, methodName,
                returnType, null, null,
                null, null, bodyText, comment);

    }

    private ClassTree addItemConverterResolveEntityMethod(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = "resolveEntity";            //NOI18N;

        String[] params = new String[]{"em"};      //NOI18N;

        Object[] paramTypes = new Object[]{Constants.ENTITY_MANAGER_TYPE};
        String returnType = getEntityClassType(bean);

        Collection<FieldInfo> fieldInfos = bean.getEntityClassInfo().getFieldInfos();

        String bodyText = "{";

        for (FieldInfo f : fieldInfos) {
            String template = null;
            if (f.isOneToOne() || f.isManyToOne()) {
                template = "$CLASS$ $FIELD$ = entity.$GETTER$();" +
                        "if ($FIELD$ != null) {" +
                        "entity.$SETTER$(em.getReference($CLASS$.class, $FIELD$.$ID_GETTER$()));" +
                        "}";
                template = template.replace("$CLASS$", f.getSimpleTypeName()).
                        replace("$FIELD$", f.getName()).replace("$GETTER$", getGetterName(f)).
                        replace("$SETTER$", getSetterName(f)).replace("$ID_GETTER$",
                        getGetterName(getEntityClassInfo(f.getType()).getIdFieldInfo()));
                bodyText += template;
            } else if (f.isManyToMany() || f.isOneToMany()) {
                String colType = f.getType();
                String concreteColType = Constants.ARRAY_LIST_TYPE;

                if (colType.equals(Constants.SET_TYPE)) {
                    concreteColType = Constants.HASH_SET_TYPE;
                }
                
                template = "$COL_TYPE$<$CLASS$> $FIELD$ = entity.$GETTER$();" +
                        "$COL_TYPE$<$CLASS$> new$FIELD$ = new $CONCRETE_COL_TYPE$<$CLASS$>();" +
                        "if ($FIELD$ != null) {" +
                        "for ($CLASS$ item : $FIELD$) {" +
                        "new$FIELD$.add(em.getReference($CLASS$.class, item.$ID_GETTER$()));" +
                        "}" +
                        "}" +
                        "entity.$SETTER$(new$FIELD$);";
                template = template.replace("$COL_TYPE$", f.getSimpleTypeName()).
                        replace("$CLASS$", f.getSimpleTypeArgName()).
                        replace("$CONCRETE_COL_TYPE$", concreteColType).
                        replace("$FIELD$", f.getName()).replace("$GETTER$", getGetterName(f)).
                        replace("$ID_GETTER$", getGetterName(getEntityClassInfo(f.getTypeArg()).getIdFieldInfo())).
                        replace("$SETTER$", getSetterName(f));
                bodyText += template;
            }
        }

        String className = getEntityClassName(bean);
        bodyText += "return entity;}";

        String comment = "Returns the resolved $CLASS$ entity.\n\n" +
                "@return an resolved entity";                            //NOI18N

        comment = comment.replace("$CLASS$", className);         //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null, methodName,
                returnType, params, paramTypes,
                null, null, bodyText, comment);

    }

    private EntityResourceBean getItemSubResource(EntityResourceBean containerResource) {
        Collection<RelatedEntityResource> subResources = containerResource.getSubResources();
        
        return subResources.iterator().next().getResourceBean();
    }

    private String getResourcePackageName() {
        return resourcePackageName;
    }

    private String getConverterPackageName() {
        return converterPackageName;
    }

    private String getResourceName(EntityResourceBean bean) {
        return bean.getName() + RESOURCE_SUFFIX;
    }

    private String getItemName(EntityResourceBean bean) {
        if (!bean.isContainer()) {
            return bean.getName();
        } else {
            return bean.getSubResources().iterator().next().getResourceBean().getName();
        }
    }

    private String getConverterName(EntityResourceBean bean) {
        return bean.getName() + CONVERTER_SUFFIX;
    }

    private String getItemConverterName(EntityResourceBean bean) {
        return getItemName(bean) + CONVERTER_SUFFIX;
    }

    private String getConverterXMLName(EntityResourceBean bean) {
        return lowerCaseFirstLetter(bean.getName());
    }

    private String getConverterType(EntityResourceBean bean) {
        return getConverterPackageName() + "." + //NOI18N
                bean.getName() + CONVERTER_SUFFIX;
    }

    private String getItemConverterType(EntityResourceBean bean) {
        return getConverterPackageName() + "." + getItemConverterName(bean);
    }

    private String getResourceType(EntityResourceBean bean) {
        return getResourcePackageName() + "." + //NOI18N
                bean.getName() + RESOURCE_SUFFIX;
    }

    private String getGetterName(FieldInfo fieldInfo) {
        return "get" + capitalizeFirstLetter(fieldInfo.getName());      //NOI18N

    }

    private String getSetterName(FieldInfo fieldInfo) {
        return "set" + capitalizeFirstLetter(fieldInfo.getName());      //NOI18N

    }
    private String getResourceNameFromField(FieldInfo fieldInfo) {
        return fieldInfo.getName() + RESOURCE_SUFFIX + "Sub"; //NOI18N
    }

    private String getIdFieldType(EntityResourceBean bean) {
        String type = bean.getEntityClassInfo().getIdFieldInfo().getType();

        // Temporary workaround because Jersey does not support
        // Character type in UriParam
        if (type.equals("java.lang.Character")) {        //NOI18N

            return "java.lang.String";              //NOI18N

        }

        return type;
    }

    private String getIdFieldName(EntityResourceBean bean) {
        return bean.getEntityClassInfo().getIdFieldInfo().getName();
    }

    private String[] getIdFieldIdArray(EntityResourceBean bean, boolean append,
            String additionalId) {
        FieldInfo field = bean.getEntityClassInfo().getIdFieldInfo();

        if (field.isEmbeddedId()) {
            Collection<FieldInfo> fields = field.getFieldInfos();
            int index = 0;
            int size = (append) ? fields.size() + 1 : fields.size();
            String[] idArray = new String[size];

            for (int i = 0; i < size; i++) {
                idArray[i] = "id" + (i + 1);
            }
            if (append) {
                idArray[size - 1] = additionalId;
            }

            return idArray;
        } else {
            if (!append) {
                return new String[]{"id"};
            } else {
                return new String[]{"id", additionalId};
            }
        }
    }

    private String[] getIdFieldNameArray(EntityResourceBean bean, boolean append,
            String additionalName) {
        FieldInfo field = bean.getEntityClassInfo().getIdFieldInfo();

        if (field.isEmbeddedId()) {
            Collection<FieldInfo> fields = field.getFieldInfos();
            int index = 0;
            int size = (append) ? fields.size() + 1 : fields.size();
            String[] fieldArray = new String[size];

            for (FieldInfo f : fields) {
                fieldArray[index++] = f.getName();
            }

            if (append) {
                fieldArray[size - 1] = additionalName;
            }

            return fieldArray;
        } else {
            if (!append) {
                return new String[]{field.getName()};
            } else {
                return new String[]{field.getName(), additionalName};
            }
        }
    }

    private Object[] getIdFieldTypeArray(EntityResourceBean bean, boolean append,
            Object additionalType) {
        FieldInfo field = bean.getEntityClassInfo().getIdFieldInfo();

        if (field.isEmbeddedId()) {
            Collection<FieldInfo> fields = field.getFieldInfos();
            int index = 0;
            int size = (append) ? fields.size() + 1 : fields.size();
            Object[] typeArray = new Object[size];

            for (FieldInfo f : fields) {
                typeArray[index++] = f.getType();
            }

            if (append) {
                typeArray[size - 1] = additionalType;
            }
            return typeArray;
        } else {
            String type = field.getType();
            if (type.equals("java.lang.Character")) {
                type = "java.lang.String";
            }

            if (!append) {
                return new Object[]{type};
            } else {
                return new Object[]{type, additionalType};
            }
        }
    }

    private String[] getIdFieldUriParamArray(EntityResourceBean bean, boolean append,
            String additionalUriParam) {
        FieldInfo field = bean.getEntityClassInfo().getIdFieldInfo();

        if (field.isEmbeddedId()) {
            int size = (append) ? field.getFieldInfos().size() + 1 : field.getFieldInfos().size();
            String[] uriParamArray = new String[size];

            for (int i = 0; i < size; i++) {
                uriParamArray[i] = RestConstants.PATH_PARAM_ANNOTATION;
            }

            if (append) {
                uriParamArray[size - 1] = additionalUriParam;
            }

            return uriParamArray;
        } else {
            if (!append) {
                return new String[]{RestConstants.PATH_PARAM_ANNOTATION};
            } else {
                return new String[]{RestConstants.PATH_PARAM_ANNOTATION, additionalUriParam};
            }
        }
    }

    private String getIdFieldIdList(EntityResourceBean bean) {
        FieldInfo field = bean.getEntityClassInfo().getIdFieldInfo();

        if (field.isEmbeddedId()) {
            int size = field.getFieldInfos().size();
            String idList = "";

            for (int i = 1; i <= size; i++) {
                if (i > 1) {
                    idList += ", ";
                }

                idList += "id" + i;
            }

            return idList;
        } else {
            return "id";        //NOI18N

        }
    }

    private String getIdFieldToUriStmt(EntityResourceBean bean) {
        return getIdFieldToUriStmt(bean.getEntityClassInfo().getIdFieldInfo());
    }

    private String getIdFieldToUriStmt(FieldInfo idField) {
        String getterName = getGetterName(idField);

        if (idField.isEmbeddedId()) {
            Collection<FieldInfo> fields = idField.getFieldInfos();
            int size = fields.size();
            String stmt = "";
            int index = 0;

            for (FieldInfo f : fields) {
                if (index++ > 0) {
                    stmt += " + \",\" + ";
                }
                stmt += "entity." + getterName + "()." +
                        getGetterName(f) + "()";
            }

            return stmt;
        } else {
            return "entity." + getterName + "()";
        }
    }

  
    protected String getIdGetter(EntityResourceBean bean) {
        return getGetterName(bean.getEntityClassInfo().getIdFieldInfo());
    }

    protected String getIdSetter(EntityResourceBean bean) {
        return getSetterName(bean.getEntityClassInfo().getIdFieldInfo());
    }

    protected String getEntityClassName(EntityResourceBean bean) {
        return bean.getEntityClassInfo().getName();
    }

    protected String getEntityClassType(EntityResourceBean bean) {
        return bean.getEntityClassInfo().getType();
    }

    protected String getPersistenceServiceClassType() {
        return getResourcePackageName() + "." + PERSISTENCE_SERVICE;
    }

    protected String getUriResolverClassType() {
        return getConverterPackageName() + "." + URI_RESOLVER;
    }

    protected String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }

    protected String lowerCaseFirstLetter(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
    }

    protected String demodulize(String str) {

        return Inflector.getInstance().demodulize(str);
    }

    protected String getNullValue(FieldInfo f) {
        String type = f.getSimpleTypeName();

        if (type.equals("int") || type.equals("long") || type.equals("float") ||
                type.equals("double") || type.equals("byte") || type.equals("short") ||
                type.equals("char")) {
            return "(" + f.getSimpleTypeName() + ") 0";
        } else if (type.equals("boolean")) {
            return "false";
        }

        return "null";
    }

    protected EntityClassInfo getEntityClassInfo(String className) {
        return model.getBuilder().getEntityClassInfo(className);
    }

    protected String[] combineStringArrays(String[] array1, String[] array2) {
        if (array2 == null || array2.length == 0) {
            return array1;
        }

        String[] newArray = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);

        return newArray;
    }

    protected Object[] combineObjectArrays(Object[] array1, Object[] array2) {
        if (array2 == null || array2.length == 0) {
            return array1;
        }

        Object[] newArray = new Object[array1.length + array2.length];
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);

        return newArray;
    }

    protected int getTotalWorkUnits() {
        int totalUnits = 0;

        for (EntityResourceBean bean : model.getResourceBeans()) {
            totalUnits += 2;

            if (bean.isItem()) {
                totalUnits++;
            }
        }

        totalUnits = (totalUnits + 2) * 2;

        return totalUnits;
    }

    private void reportProgress(String className, boolean modifying) {
        String message = null;

        if (!modifying) {
            message = NbBundle.getMessage(EntityResourcesGenerator.class,
                    "MSG_CreatingClass", className);
        } else {
            message = NbBundle.getMessage(EntityResourcesGenerator.class,
                    "MSG_ModifyingClass", className);
        }

        reportProgress(message);
    }
}
