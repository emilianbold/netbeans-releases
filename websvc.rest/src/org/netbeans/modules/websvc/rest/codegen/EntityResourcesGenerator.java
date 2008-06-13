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
package org.netbeans.modules.websvc.rest.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
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
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.modules.websvc.rest.support.WebXmlHelper;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author PeterLiu
 */
public class EntityResourcesGenerator extends AbstractGenerator {

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
        Constants.HTTP_RESPONSE,
        Constants.CONTEXT,
        Constants.URI_INFO
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
        Constants.WEB_APPLICATION_EXCEPTION,
        Constants.NO_RESULT_EXCEPTION
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
        Constants.URI_BUILDER
    };
    private static final String mimeTypes = "{\"" + MimeType.XML.value() + "\", \"" +
            MimeType.JSON.value() + "\"}";        //NOI18N

    private String persistenceUnitName;
    private String targetPackageName;
    private FileObject targetFolder;
    private String packageName;
    private FileObject resourceFolder;
    private String resourcePackageName;
    private FileObject converterFolder;
    private String converterPackageName;
    private EntityResourceBeanModel model;
    private Project project;

    /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public EntityResourcesGenerator(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName, String persistenceUnitName) {
        this(model, project, targetFolder, targetPackageName, null, null, persistenceUnitName);
    }

    public EntityResourcesGenerator(EntityResourceBeanModel model,
            String resourcePackage, String converterPackage) {
        this(model, null, null, null, resourcePackage, converterPackage, null);
    }

    /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public EntityResourcesGenerator(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName,
            String resourcePackage, String converterPackage,
            String persistenceUnitName) {
        this.model = model;
        this.project = project;
        this.persistenceUnitName = persistenceUnitName;
        this.targetFolder = targetFolder;
        this.targetPackageName = targetPackageName;

        if (resourcePackage == null) {
            this.resourcePackageName = targetPackageName + "." + this.RESOURCE_FOLDER;
        } else {
            this.resourcePackageName = resourcePackage;
        }

        if (converterPackage == null) {
            this.converterPackageName = targetPackageName + "." + this.CONVERTER_FOLDER;
        } else {
            this.converterPackageName = converterPackage;
        }

        this.packageName = packageName;
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
        classes.add(getPersistenceServiceClassType());
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
        generatePersistenceService();
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
        boolean useResourceLocalTx = false;

        if (RestUtils.hasJTASupport(project)) {
            template = PERSISTENCE_SERVICE_TEMPLATE;
        } else {
            template = PERSISTENCE_SERVICE_NOJTA_TEMPLATE;
            useResourceLocalTx = true;
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
                            persistenceUnitName);
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        //Make necessary changes to the persistence.xml
        PersistenceHelper.modifyPersistenceXml(project, useResourceLocalTx);

        // Add <persistence-unit-ref> to web.xml
        WebXmlHelper.addPersistenceUnitRef(project, persistenceUnitName);
    }

    private void generateUriResolver() {
        reportProgress(getUriResolverClassType(), false);

        JavaSource source = JavaSourceHelper.createJavaSource(URI_RESOLVER_TEMPLATE, converterFolder, getConverterPackageName(), URI_RESOLVER);

        reportProgress(getUriResolverClassType(), true);

        // Add PersistenceService import
//        try {
//            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
//                public void run(WorkingCopy copy) throws IOException {
//                    copy.toPhase(JavaSource.Phase.RESOLVED);
//                    
//                    JavaSourceHelper.addImports(copy,
//                            new String[] { getPersistenceServiceClassType() });
//                }
//            });
//            
//            result.commit();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
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

                    JavaSourceHelper.addClassAnnotation(copy,
                            new String[]{RestConstants.PATH_ANNOTATION},
                            new Object[]{bean.getUriTemplate(),
                                JavaSourceHelper.createIdentifierTree(copy,
                                "{" + getItemResourceName(bean) + ".class}")
                            });


                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    modifiedTree = addResourceBeanFields(copy, modifiedTree, bean);
                    modifiedTree = addStatefulResourceBeanConstructor(copy, modifiedTree, bean);
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

    private void modifyItemResourceBean(JavaSource source, final EntityResourceBean bean) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    JavaSourceHelper.addImports(copy, getItemResourceImports(bean));

                    /*
                    JavaSourceHelper.addClassAnnotation(copy,
                    new String[] {Constants.URI_TEMPLATE_ANNOTATION},
                    new Object[] {bean.getUriTemplate()});
                     */

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = tree;

                    modifiedTree = addResourceBeanFields(copy, modifiedTree, bean);
                    modifiedTree = addStatefulResourceBeanConstructor(copy, modifiedTree, bean);
                    modifiedTree = addItemGetMethod(copy, modifiedTree, bean, mimeTypes);
                    modifiedTree = addItemPutMethod(copy, modifiedTree, bean, mimeTypes);

                    modifiedTree = addItemDeleteMethod(copy, modifiedTree, bean);

                    for (RelatedEntityResource relatedResource : bean.getSubResources()) {
                        modifiedTree = addItemGetResourceMethod(copy, modifiedTree, bean, relatedResource);
                    }

                    modifiedTree = addGetEntityMethod(copy, modifiedTree, bean);
                    modifiedTree = addUpdateEntityMethod(copy, modifiedTree, bean);
                    //modifiedTree = addGetUriMethod(copy, modifiedTree, bean);

                    copy.rewrite(tree, modifiedTree);
                }
            });

            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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

    private void modifyItemConverter(JavaSource source, final EntityResourceBean bean) {
        try {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    List<String> imports = new ArrayList<String>(Arrays.asList(ITEM_CONVERTER_IMPORTS));
                    imports.add(getEntityClassType(bean));
                    imports.add(resourcePackageName + "." + PERSISTENCE_SERVICE);
                    JavaSourceHelper.addImports(copy, imports.toArray(new String[imports.size()]));

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

        return imports.toArray(new String[imports.size()]);
    }

    private String[] getItemResourceImports(EntityResourceBean bean) {
        List<String> imports = new ArrayList<String>(Arrays.asList(ITEM_IMPORTS));

        Set<String> classes = new HashSet<String>();
        for (RelatedEntityResource resource : bean.getSuperResources()) {
            classes.add(getEntityClassType(resource.getResourceBean()));
        }

        for (RelatedEntityResource resource : bean.getSubResources()) {
            EntityResourceBean subBean = resource.getResourceBean();

            if (subBean.isContainer()) {
                classes.add(Constants.COLLECTION_TYPE);
            } else {
                //Only add non java.lang types.
                String type = getIdFieldType(subBean);
                if (!type.startsWith("java.lang.")) {
                    classes.add(type);
                }
            }
        }
        imports.addAll(classes);
        imports.add(getConverterType(bean));

        return imports.toArray(new String[imports.size()]);
    }

    private EntityResourceBean getItemConverterBean(EntityResourceBean containerBean) {
        return model.getItemResourceBean(containerBean.getEntityClassInfo());
    }

    private ClassTree addResourceBeanFields(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE};
        ClassTree modifiedTree = tree;

        String[] annotations = null;

        if (bean.isContainer()) {
            annotations = new String[]{RestConstants.CONTEXT_ANNOTATION};
        }

        modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                annotations, null, "context", Constants.URI_INFO);  //NOI18

        if (!bean.isContainer()) {
            String ids[] = getIdFieldIdArray(bean, false, null);
            Object types[] = getIdFieldTypeArray(bean, false, null);

            for (int i = 0; i < ids.length; i++) {
                modifiedTree = JavaSourceHelper.addField(copy, modifiedTree, modifiers,
                        annotations, null, ids[i], (String) types[i]);  //NOI18

            }
        }

        return modifiedTree;
    }

    private ClassTree addStatefulResourceBeanConstructor(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        String[] parameters = null;
        Object[] types = null;

        if (bean.isContainer()) {
            parameters = new String[]{"context"};
            types = new String[]{Constants.URI_INFO};
        } else {
            parameters = getIdFieldIdArray(bean, true, "context");
            types = getIdFieldTypeArray(bean, true, Constants.URI_INFO);
        }

        String bodyText = "{";
        for (int i = 0; i < parameters.length; i++) {
            bodyText += "this." + parameters[i] + " = " + parameters[i] + ";";
        }
        bodyText += "}";

        String comment = "Constructor used for instantiating an instance of dynamic resource.\n\n" +
                "@param context HttpContext inherited from the parent resource";

        return JavaSourceHelper.addConstructor(copy, tree,
                new Modifier[]{Modifier.PUBLIC},
                parameters, types, bodyText, comment);
    }

    private ClassTree addContainerGetMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = HttpMethodType.GET.prefix();
        String[] annotations = new String[]{
            RestConstants.GET_ANNOTATION,
            RestConstants.PRODUCE_MIME_ANNOTATION
        };

        Object[] annotationAttrs = new Object[]{
            null,
            JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
        };

        Object returnType = getConverterType(bean);

        String[] parameters = new String[]{"start", "max", "expandLevel"};        //NOI18N

        String intType = Integer.TYPE.getName();
        Object[] paramTypes = new String[]{intType, intType, intType};          //NOI18N

        String[][] paramAnnotations = new String[][]{
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION},
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION},
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION}
        };

        Object[][] paramAnnotationAttrs = new Object[][]{
            {"start", "0"},
            {"max", "10"},
            {"expandLevel", "1"},
        };

        String bodyText = "{ try {" +
                "return new $CONVERTER$(getEntities(start, max), context.getAbsolutePath(), expandLevel);" +
                "} finally {" +
                "PersistenceService.getInstance().close();" +
                "}" +
                "}";
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

    private ClassTree addContainerPostMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        String methodName = HttpMethodType.POST.prefix();
        String[] annotations = new String[]{
            RestConstants.POST_ANNOTATION,
            RestConstants.CONSUME_MIME_ANNOTATION
        };
        Object[] annotationAttrs = new Object[]{
            null,
            JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
        };
        //Object returnType = getConverterType(model.getItemResourceBean(bean.getEntityClassInfo()));
        Object returnType = Constants.HTTP_RESPONSE;

        String[] params = new String[]{"data"};
        Object[] paramTypes = new Object[]{getItemConverterName(bean)};

        String bodyText = "{" +
                "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                "try {" +
                "persistenceSvc.beginTx();" +
                "$CLASS$ entity = data.getEntity();" +
                "createEntity(entity);" +
                "persistenceSvc.commitTx();" +
                "return Response.created(context.getAbsolutePath().resolve($ID_TO_URI$ + \"/\")).build();" +
                "} finally {" +
                "persistenceSvc.close();" +
                "}" +
                "}";


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

    private ClassTree addContainerGetResourceMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, RelatedEntityResource relatedResource) {
        EntityResourceBean subBean = relatedResource.getResourceBean();
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = new String[]{RestConstants.PATH_ANNOTATION};
        String[] annotationAttrs = new String[]{subBean.getUriTemplate()};
        Object returnType = getResourceType(subBean);
        String resourceName = getResourceName(subBean);
        String methodName = "get" + resourceName;

        String[] parameters = getIdFieldIdArray(subBean, false, null);
        Object[] paramTypes = getIdFieldTypeArray(subBean, false, null);
        String[] paramAnnotations = getIdFieldUriParamArray(subBean, false, null);
        Object[] paramAnnotationAttrs = getIdFieldNameArray(subBean, false, null);
        String argList = getIdFieldIdList(subBean) + ", context";

        String bodyText = "{ return new $RESOURCE$(" + argList + "); }";
        bodyText = bodyText.replace("$RESOURCE$", resourceName);

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

        String bodyText = "{" +
                "return PersistenceService.getInstance().createQuery(\"SELECT e FROM $CLASS$ e\")." +
                "setFirstResult(start).setMaxResults(max).getResultList();" +
                "}";

        String[] parameters = new String[]{"start", "max"};
        Object[] paramTypes = new Object[]{"int", "int"};

        bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean));

        String comment = "Returns all the entities associated with this resource.\n\n" +
                "@return a collection of $CLASS$ instances";
        comment = comment.replace("$CLASS$", getEntityClassName(bean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null,
                "getEntities", returnType, parameters, paramTypes, null, null,
                bodyText, comment);
    }

    private String getGetEntitiesSubText(EntityResourceBean bean) {
        String template = "if (parent instanceof $CLASS$) {" +
                "return (($CLASS$) parent).$GETTER$();}";

        String bodyText = "";

        for (RelatedEntityResource relatedResource : bean.getSuperResources()) {
            EntityResourceBean superBean = relatedResource.getResourceBean();
            FieldInfo fieldInfo = relatedResource.getFieldInfo();

            bodyText = bodyText + template.replace("$CLASS$",
                    getEntityClassName(superBean)).
                    replace("$GETTER$", getGetterName(fieldInfo));
        }

        return bodyText;
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

        bodyText += "PersistenceService.getInstance().persistEntity(entity);";
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
                "oldEntity.$GETTER$().remove(entity);" +
                "}" +
                "}";                                                //NOI18N
        
        String manyToManyTemplate = "for ($CLASS$ value : entity.$GETTER$()) {" +
                "value.$REVERSE_GETTER$().add(entity);" +
                "}";                                                //NOI18N
 
        String bodyText = "";
     
        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo reverseFieldInfo = subResource.getReverseFieldInfo();
          
            if (reverseFieldInfo == null) continue;
            
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

    private ClassTree addGetUriMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        String bodyText = "{return context.getAbsolutePath();}";

        String comment = "Returns the URI associated with this resource.\n\n" +
                "@return URI associated with this resource";

        return JavaSourceHelper.addMethod(copy, tree,
                new Modifier[]{Modifier.PRIVATE}, null, null,
                "getUri", Constants.URI_TYPE,
                null, null, null, null, bodyText, comment);
    }

    private ClassTree addItemGetMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = HttpMethodType.GET.prefix();
        String[] annotations = new String[]{
            RestConstants.GET_ANNOTATION,
            RestConstants.PRODUCE_MIME_ANNOTATION
        };

        Object[] annotationAttrs = new Object[]{
            null,
            JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
        };

        Object returnType = getConverterType(bean);

        String[] parameters = new String[]{"expandLevel"};        //NOI18N

        Object[] paramTypes = new String[]{Integer.TYPE.getName()};          //NOI18N

        String[][] paramAnnotations = new String[][]{
            {RestConstants.QUERY_PARAM_ANNOTATION, RestConstants.DEFAULT_VALUE_ANNOTATION}
        };

        Object[][] paramAnnotationAttrs = new Object[][]{
            {"expandLevel", "1"},
        };
        String bodyText = "{ try {" +
                "return  new $CONVERTER$(getEntity(), context.getAbsolutePath(), expandLevel); " +
                "} finally {" +
                "PersistenceService.getInstance().close();" +
                "}" +
                "}";

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

    private ClassTree addItemPutMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, String mimeTypes) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String methodName = HttpMethodType.PUT.prefix();
        String[] annotations = new String[]{
            RestConstants.PUT_ANNOTATION,
            RestConstants.CONSUME_MIME_ANNOTATION
        };

        Object[] annotationAttrs = new Object[]{
            null,
            JavaSourceHelper.createIdentifierTree(copy, mimeTypes)
        };

        String[] params = new String[]{"data"};
        Object[] paramTypes = new String[]{getConverterType(bean)};

        String bodyText = "{ " +
                "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                "try {" +
                "persistenceSvc.beginTx();" +
                "updateEntity(getEntity(), data.getEntity());" +
                "persistenceSvc.commitTx();" +
                "} finally {" +
                "persistenceSvc.close();" +
                "}" +
                "}";

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

    private ClassTree addItemDeleteMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        Object returnType = Constants.VOID;

        String[] annotations = new String[]{
            RestConstants.DELETE_ANNOTATION
        };
        Object[] annotationAttrs = new Object[]{null};

        String bodyText = "{" +
                "PersistenceService persistenceSvc = PersistenceService.getInstance();" +
                "try {" +
                "persistenceSvc.beginTx();" +
                "$CLASS$ entity = getEntity();" +
                "persistenceSvc.removeEntity(entity);" +
                "persistenceSvc.commitTx();" +
                "} finally {" +
                "persistenceSvc.close();" +
                "}" +
                "}";
        bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean));

        String comment = "Delete method for deleting an instance of $CLASS identified by id.\n\n" +
                "@param id identifier for the entity\n";
        comment = comment.replace("$CLASS", getEntityClassName(bean));

        return JavaSourceHelper.addMethod(copy, tree, modifiers, annotations,
                annotationAttrs, HttpMethodType.DELETE.prefix(), returnType,
                null, null, null, null, bodyText, comment);
    }

    private ClassTree addItemGetResourceMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean, RelatedEntityResource relatedResource) {
        EntityResourceBean subBean = relatedResource.getResourceBean();
        FieldInfo fieldInfo = relatedResource.getFieldInfo();
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        String[] annotations = new String[]{RestConstants.PATH_ANNOTATION};

        String uriTemplate = fieldInfo.getName() + "/";
        String[] annotationAttrs = new String[]{uriTemplate};
        Object returnType = getResourceType(subBean);
        String resourceName = getResourceName(subBean);

        String getterName = getGetterName(fieldInfo);
        String methodName = getterName + RESOURCE_SUFFIX;     //NOI18N

        String bodyText = null;

        if (subBean.isItem()) {
            bodyText = "{" +
                    "final $CLASS$ parent = getEntity();" +
                    "return new $RESOURCE$($ID_PARAMS$, context) {" +
                    "@Override protected $SUBCLASS$ getEntity() {" +
                    "$SUBCLASS$ entity = parent.$GETTER$();" +
                    "if (entity == null) {" +
                    "throw new WebApplicationException(new Throwable(\"Resource for \" + context.getAbsolutePath() + \" does not exist.\"), 404);" +
                    "}" +
                    "return entity;" +
                    "}" +
                    "};" +
                    "}";
            bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean)).
                    replace("$RESOURCE$", resourceName).
                    replace("$SUBCLASS$", getEntityClassName(subBean)).
                    replace("$ID_PARAMS$", getIdFieldNullValueList(subBean)).
                    replace("$GETTER$", getterName);
        } else {
            bodyText = "{" +
                    "final $CLASS$ parent = getEntity();" +
                    "return new $RESOURCE$(context) {" +
                    "@Override protected Collection<$SUBCLASS$> getEntities(int start, int max) {" +
                    "Collection<$SUBCLASS$> result = new java.util.ArrayList<$SUBCLASS$>();" +
                    "int index = 0;" +
                    "for ($SUBCLASS$ e : parent.$GETTER$()) {" +
                    "if (index >= start && (index - start) < max) {" +
                    "result.add(e);" +
                    "}" +
                    "index++;" +
                    "}" +
                    "return result;" +
                    "}" +
                    "@Override protected void createEntity($SUBCLASS$ entity) {" +
                    "super.createEntity(entity);";

            if (fieldInfo.isOneToMany()) {
                bodyText += "entity.$SETTER$(parent);";

                for (RelatedEntityResource subResource : getItemSubResource(subBean).getSubResources()) {
                    if (bean == subResource.getResourceBean()) {
                        bodyText = bodyText.replace("$SETTER$", getSetterName(subResource.getFieldInfo()));

                        break;
                    }
                }
            } else if (fieldInfo.isManyToMany()) {
                bodyText += "if (!entity.$GETTER2$().contains(parent)) {" +
                        "entity.$GETTER2$().add(parent);" +
                        "}";

                for (RelatedEntityResource subResource : getItemSubResource(subBean).getSubResources()) {
                    EntityResourceBean subSubBean = subResource.getResourceBean();
                    if (subSubBean.isContainer()) {
                        if (bean == getItemSubResource(subSubBean)) {
                            bodyText = bodyText.replace("$GETTER2$", getGetterName(subResource.getFieldInfo()));

                            break;
                        }
                    }
                }
            }

            bodyText += "}};}";

            bodyText = bodyText.replace("$CLASS$", getEntityClassName(bean)).
                    replace("$RESOURCE$", resourceName).
                    replace("$SUBCLASS$", getEntityClassName(subBean)).
                    replace("$GETTER$", getterName);
        }

        String comment = "Returns a dynamic instance of $RESOURCE$ used for entity navigation.\n\n" +
                "@param id identifier for the parent entity\n" +
                "@return an instance of $RESOURCE$";

        comment = comment.replace("$RESOURCE$", getResourceName(subBean));

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                methodName, returnType, null, null, null, null,
                bodyText, comment);
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

        String bodyText = "{" +
                "try {" +
                "$NEW_ID_STMT$" +
                "return ($CLASS$) PersistenceService.getInstance()." +
                "createQuery(\"SELECT e FROM $CLASS$ e where e.$ID$ = :$ID$\")." +
                "setParameter(\"$ID$\", $ID_STRING$).getSingleResult();" +
                "} catch (NoResultException ex) {" +
                "throw new WebApplicationException(new Throwable(\"Resource for \" + context.getAbsolutePath() + \" does not exist.\"), 404);" +
                "}" +
                "}";

        String id = bean.getEntityClassInfo().getIdFieldInfo().getName();
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

    private String getGetEntitySubText(EntityResourceBean bean) {
        String template1 = "if (parent instanceof $SUPERCLASS$) {" +
                "for ($CLASS$ e : (($SUPERCLASS$) parent).$SUPERGETTER$()) {" +
                "if (e.$GETTER$().equals(id)) {" +
                "return e;" + "}}}";

        String template2 = "if (parent instanceof $SUPERCLASS$) {" +
                "return (($SUPERCLASS$) parent).$SUPERGETTER$();" +
                "}";

        String bodyText = "";

        for (RelatedEntityResource resource : bean.getSuperResources()) {
            EntityResourceBean superBean = resource.getResourceBean();
            FieldInfo fieldInfo = resource.getFieldInfo();
            String template = null;

            // Skip 1-to-M relationships.
            if (fieldInfo.isOneToMany()) {
                //template = template1;
                continue;
            } else {
                template = template2;
            }

            bodyText = bodyText +
                    template.replace("$SUPERCLASS$", getEntityClassName(superBean)).
                    replace("$SUPERGETTER$", getGetterName(fieldInfo)).
                    replace("$CLASS$", getEntityClassName(bean)).
                    replace("$GETTER$", getGetterName(bean.getEntityClassInfo().getIdFieldInfo()));
        }

        return bodyText;
    }

    private ClassTree addUpdateEntityMethod(WorkingCopy copy, ClassTree tree,
            EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PROTECTED};
        Object returnType = getEntityClassType(bean);
        String[] params = new String[]{"entity", "newEntity"};
        Object[] paramTypes = new Object[]{
            getEntityClassType(bean), getEntityClassType(bean)
        };

//        String bodyText = "{ newEntity.$SETTER$(entity.$GETTER$();";
//        bodyText = bodyText.replace("$SETTER$", getIdSetter(bean)).
//                replace("$GETTER$", getIdGetter(bean));

        String bodyText = "{" + getRemoveOneToManyRelSubText(bean);

        bodyText = bodyText +
                "entity = PersistenceService.getInstance().mergeEntity(newEntity);";

        bodyText = bodyText + getUpdateOneToManyRelSubText(bean) +
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

    private String getRemoveOneToManyRelSubText(EntityResourceBean bean) {
        String template = "entity.$GETTER$().removeAll(newEntity.$GETTER$());" +
                "for ($CLASS$ value : entity.$GETTER$()) {" +
                "value.$SETTER$(null);" +
                "}";

        String bodyText = "";

        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo fieldInfo = subResource.getFieldInfo();

            if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                EntityResourceBean subBean = subResource.getResourceBean();
                EntityResourceBean itemResource = getItemSubResource(subBean);

                for (RelatedEntityResource subSubResource : itemResource.getSubResources()) {
                    EntityResourceBean subSubBean = subSubResource.getResourceBean();

                    if (bean == subSubBean) {
                        bodyText = bodyText + template.replace("$CLASS$", getEntityClassName(subBean)).
                                replace("$GETTER$", getGetterName(fieldInfo)).
                                replace("$SETTER$", getSetterName(subSubResource.getFieldInfo()));
                        break;
                    }
                }
            }
        }

        return bodyText;
    }

    private String getUpdateOneToManyRelSubText(EntityResourceBean bean) {
        String template = "for ($CLASS$ value : entity.$GETTER$()) {" +
                "value.$SETTER$(entity);" +
                "}";

        String bodyText = "";

        for (RelatedEntityResource subResource : bean.getSubResources()) {
            FieldInfo fieldInfo = subResource.getFieldInfo();

            if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                EntityResourceBean subBean = subResource.getResourceBean();
                EntityResourceBean itemResource = getItemSubResource(subBean);

                for (RelatedEntityResource subSubResource : itemResource.getSubResources()) {
                    EntityResourceBean subSubBean = subSubResource.getResourceBean();

                    if (bean == subSubBean) {
                        bodyText = bodyText + template.replace("$CLASS$", getEntityClassName(subBean)).
                                replace("$GETTER$", getGetterName(fieldInfo)).
                                replace("$SETTER$", getSetterName(subSubResource.getFieldInfo()));
                        break;
                    }
                }
            }
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
                    "this.expandLevel = expandLevel;}";         //NOI18N

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

            bodyText = "{this.entities = entities; this.uri = uri; this.expandLevel = expandLevel}"; //NOI18N

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
                "entities.add(item.resolveEntity()); } " +
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

            bodyText = "{return (expandLevel > 0) ? getEntity().$GETTER$() : null;}";         //NOI18N

            bodyText = bodyText.replace("$GETTER$", getterName);                    //NOI18N

        } else {
            if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                EntityResourceBean foreignBean = model.getContainerResourceBean(fieldInfo.getTypeArg());
                returnType = getConverterType(foreignBean);

                bodyText = "{if (expandLevel > 0) {" +
                        "if (getEntity().$GETTER$() != null) {" +
                        "return new $CONVERTER$(getEntity().$GETTER$(), uri.resolve(\"$FIELD$/\"), expandLevel - 1);" +
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
                        "if (getEntity().$GETTER$() != null) {" +
                        "return new $ITEM_CONVERTER$(getEntity().$GETTER$(), uri.resolve(\"$FIELD$/\"), expandLevel - 1, false);" +
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

            bodyText = "{ getEntity().$SETTER$(value); }";      //NOI18N

            bodyText = bodyText.replace("$SETTER$", setterName).
                    replace("$CLASS$", getEntityClassName(bean));       //NOI18N

        } else {
            if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                EntityResourceBean foreignBean = model.getContainerResourceBean(fieldInfo.getTypeArg());
                String relatedEntityName = model.getItemResourceBean(fieldInfo.getTypeArg()).getName();
                argType = getConverterType(foreignBean);
                bodyText = "{getEntity().$SETTER$((value != null) ? value.getEntities() : null);}";     //NOI18N    

                bodyText = bodyText.replace("$CLASS$", relatedEntityName). //NOI18N
                        replace("$SETTER$", setterName);        //NOI18N

            } else { // should be 1-1

                EntityResourceBean foreignBean = model.getItemResourceBean(fieldInfo.getType());
                argType = getConverterType(foreignBean);

                bodyText = "{getEntity().$SETTER$((value != null) ? value.resolveEntity() : null);}";        //NOI18N

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

        String className = getEntityClassName(bean);
        String bodyText = "{ if (entity == null) { " +
                "entity = new $CLASS$();" +
                "}" +
                "return entity; }";         //NOI18N

        bodyText = bodyText.replace("$CLASS$", className);      //NOI18N

        String comment = "Returns the $CLASS$ entity.\n\n" +
                "@return an entity";                            //NOI18N

        comment = comment.replace("$CLASS$", className);         //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, null, "getEntity",
                getEntityClassType(bean), null, null,
                null, null, bodyText, comment);                 //NOI18N

    }

    private ClassTree addItemConverterResolveEntityMethod(WorkingCopy copy,
            ClassTree tree, EntityResourceBean bean) {
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};

        String className = getEntityClassName(bean);
        String bodyText = "{ if (entity != null) {" +
                "return PersistenceService.getInstance().resolveEntity($CLASS$.class, entity.$ID_GETTER$());" +
                "} else {" +
                "return ($CLASS$) UriResolver.getInstance().resolve($ITEM_CONVERTER$.class, uri);" +
                "}" +
                "}";                //NOI18N

        bodyText = bodyText.replace("$CLASS$", className).
                replace("$ID_GETTER$", getIdGetter(bean)).
                replace("$ITEM_CONVERTER$", getItemConverterName(bean)); //NOI18N

        String comment = "Returns the resolved $CLASS$ entity.\n\n" +
                "@return an resolved entity";                            //NOI18N

        comment = comment.replace("$CLASS$", className);         //NOI18N

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, null, null, "resolveEntity",
                getEntityClassType(bean), null, null,
                null, null, bodyText, comment);                 //NOI18N

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
        return Util.getSingularName(bean);
    }

    private String getItemResourceName(EntityResourceBean bean) {
        return getItemName(bean) + RESOURCE_SUFFIX;
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

    private String getIdFieldNullValueList(EntityResourceBean bean) {
        FieldInfo field = bean.getEntityClassInfo().getIdFieldInfo();

        if (field.isEmbeddedId()) {
            Collection<FieldInfo> fieldInfos = field.getFieldInfos();
            int index = 0;
            String valueList = "";

            for (FieldInfo f : fieldInfos) {
                if (index > 0) {
                    valueList += ", ";
                }

                valueList += getNullValue(f);
                index++;
            }
            return valueList;
        } else {
            return getNullValue(field);
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

    private String getIdFieldParamList(EntityResourceBean bean) {
        FieldInfo idField = bean.getEntityClassInfo().getIdFieldInfo();

        if (idField.isEmbeddedId()) {
            Collection<FieldInfo> fields = idField.getFieldInfos();
            int index = 0;
            int size = fields.size();
            String paramStr = "";

            for (FieldInfo f : fields) {
                if (index++ > 0) {
                    paramStr += ", ";
                }

                paramStr += f.getSimpleTypeName() + " id" + index;
            }

            return paramStr;
        } else {
            String type = idField.getSimpleTypeName();
            if (type.equals("Character")) {
                type = "String";
            }

            return type + " id";
        }
    }

    private String getIdGetter(EntityResourceBean bean) {
        return getGetterName(bean.getEntityClassInfo().getIdFieldInfo());
    }

    private String getIdSetter(EntityResourceBean bean) {
        return getSetterName(bean.getEntityClassInfo().getIdFieldInfo());
    }

    private String getEntityClassName(EntityResourceBean bean) {
        return bean.getEntityClassInfo().getName();
    }

    private String getEntityClassType(EntityResourceBean bean) {
        return bean.getEntityClassInfo().getType();
    }

    private String getPersistenceServiceClassType() {
        return getResourcePackageName() + "." + PERSISTENCE_SERVICE;
    }

    private String getUriResolverClassType() {
        return getConverterPackageName() + "." + URI_RESOLVER;
    }

    private String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }

    private String lowerCaseFirstLetter(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
    }

    private String demodulize(String str) {

        return Inflector.getInstance().demodulize(str);
    }

    private String getNullValue(FieldInfo f) {
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
