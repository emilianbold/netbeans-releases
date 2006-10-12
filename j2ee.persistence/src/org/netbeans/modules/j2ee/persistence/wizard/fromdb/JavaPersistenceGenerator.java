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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.mdr.events.AttributeEvent;
import org.netbeans.api.mdr.events.MDRChangeEvent;
import org.netbeans.api.mdr.events.MDRChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.AttributeValue;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Entity;
import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Table;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.*;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.openide.util.NbBundle;

/** Generator of Java Persistence API ORM classes from DB.
 *
 * @author Pavel Buzek
 */
public class JavaPersistenceGenerator implements PersistenceGenerator {
    
    private Map<String, String> entityName2TableName = new HashMap<String, String>();
        
    private HashMap genDataCache = new HashMap(13);
    
    // options (not currently exposed in UI)
    // field vs. property access
    private boolean fieldAccess = true;
    // named input params for named queries vs. positional params
    private boolean genNamedParams = true;
    // should generated Entity Classes implement Serializable?
    private boolean genSerializableEntities = true;
    
    public JavaPersistenceGenerator() {
    }
    
    private String getCmrFieldBaseType(RelationshipRole role, String pkg, JavaClass jc) {
        String jcPkgName = jc.getResource().getPackageName();
        boolean samePackages = org.openide.util.Utilities.compareObjects(pkg, jcPkgName);
        RelationshipRole rA = role.getParent().getRoleA();
        RelationshipRole rB = role.getParent().getRoleB();
        RelationshipRole otherRole = role.equals(rA) ? rB : rA;
        String typeName = (samePackages || pkg == null || pkg.length() == 0) ? otherRole.getEntityName() : pkg + "." + otherRole.getEntityName(); //NOI18N
        return typeName;
        /*
        if (role.isToMany()) {
            return java.util.Collection.class.getName() + "<" + typeName + ">";
        } else {
            return typeName;
        }
         */
    }
    
    public void generateBeans(final ProgressPanel progressPanel,
            final RelatedCMPHelper helper,
            final FileObject dbSchemaFile,
            final ProgressHandle handle,
            boolean justTesting) throws IOException {
        
        MDRInstanceNameListener mdrListener = new MDRInstanceNameListener();
        EntityClass[] genBeans = helper.getBeans();
        int max = genBeans.length;
        handle.switchToDeterminate(max);
        String pkgName = helper.getPackageName();
        HashMap<String,EntityClass> beanMap = new HashMap<String, EntityClass>();
        List fileObjects = new ArrayList(genBeans.length);
        for(EntityClass e : genBeans) {
            beanMap.put(e.getClassName(), e);
        }
        for (int i = 0; i < genBeans.length; i++) {
            String className = genBeans[i].getClassName();
            FileObject packageFileObject = genBeans[i].getPackageFileObject();
            
            if (packageFileObject.getFileObject(className, "java") != null) { // NOI18N
                // avoid generating classes which already exist
                handle.progress(i);
                continue;
            }
            
            handle.progress(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_GeneratingClass", className), i);
            progressPanel.setText(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_GeneratingClass", className));
            
            CMPMappingModel dbMappings = genBeans[i].getCMPMapping();
            boolean rollback = true;
            boolean needsPKClass = !genBeans[i].isUsePkField();
            JavaClass pkClass = null;
            String pkClassName = null;
            ArrayList fieldsToAdd = new ArrayList();
            ArrayList nonNullableFields = new ArrayList();
            ArrayList methodsToAdd = new ArrayList();
            ArrayList fieldsToAddPK = null;
            ArrayList methodsToAddPK = null;
            ArrayList pkColumns = new ArrayList();
            ArrayList namedQueriesList = new ArrayList();
            String namedQueryPrefix = null;
            Field pkField = null;
            String pkFieldName = null;
            boolean genQueries = helper.isGenerateFinderMethods();
            
            if (i > 20) {
                mdrListener.register(className);
            }

            JavaClass javaClass = JMIGenerationUtil.createEntityClass(packageFileObject, className);
            
            if (i > 20) {
                mdrListener.waitInstance();
                mdrListener.unregister();
            }

            if (needsPKClass) {
                pkClassName = className + "PK"; //NOI18N
                pkClass = JMIGenerationUtil.createClass(packageFileObject, pkClassName);
                String templateJavaDoc = pkClass.getJavadocText();
                pkClass.setJavadocText(NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_PKClass", 
                    className, pkClassName)  + templateJavaDoc);
            }
            
            JavaModel.getJavaRepository().beginTrans(true);
            try {
                List javaClassFeatures = javaClass.getFeatures();
                
                if (genSerializableEntities) {
                    JMIGenerationUtil.addInterface(javaClass, "java.io.Serializable"); //NOI18N
                }

                List entityAttributeValues = null; //isAccessProperty ? null : Collections.singletonList(
                Annotation entityAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.Entity", entityAttributeValues);
                javaClass.getAnnotations().add(entityAnnotation);
                
                AttributeValue tableNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "name", dbMappings.getTableName()); //NOI18N
                Annotation tableAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.Table", Collections.singletonList(tableNameAttrValue)); //NOI18N
                javaClass.getAnnotations().add(tableAnnotation);
                
//TODO: javadoc - generate or fake in test mode
//        b.setCommentDataAuthor(authorOverride);
//        b.setCommentDataDate(dateOverride);

                if (needsPKClass) {
                    pkFieldName = getFieldName(pkClassName);
                    
                    // pkClass must be serializable
                    JMIGenerationUtil.addInterface(pkClass, "java.io.Serializable"); //NOI18N

                    // add @Embeddable()
                    Annotation embeddableAnnotation = JMIGenerationUtil.createAnnotation(pkClass, 
                            "javax.persistence.Embeddable", null); //NOI18N
                    pkClass.getAnnotations().add(embeddableAnnotation);
                    
                    // public no arg constructor required -- it is part of the 
                    // template used unless it has been customized
                    JMIGenerationUtil.ensurePublicConstructor(pkClass);
 
                    fieldsToAddPK = new ArrayList();
                    methodsToAddPK = new ArrayList();            

                    // add field, getter, and setter for the PK class to the Entity
                    pkField = createField(pkFieldName, pkClassName, Modifier.PROTECTED, javaClass);
                    pkField.setJavadocText(NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_EmbeddedId"));
                    fieldsToAdd.add(pkField);
                    
                    Method getter = JMIGenerationUtil.createGetterMethod(pkFieldName, pkClassName, pkClassName, javaClass);
                    methodsToAdd.add(getter);

                    Annotation embeddedIdAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.EmbeddedId", Collections.EMPTY_LIST); //NOI18N
                    addAnnotation(fieldAccess, pkField, getter, embeddedIdAnnotation);

                    methodsToAdd.add(JMIGenerationUtil.createSetterMethod(pkFieldName, pkClassName, pkClassName, javaClass));
                }

                // workaround for issuezilla bug 77758
                Map fieldToTypeName = new HashMap();
                // end of workaround
                
                Iterator fieldIt = genBeans[i].getFields().iterator();
                String beanName = javaClass.getSimpleName();
                while (fieldIt.hasNext()) {
                    EntityMember m = (EntityMember) fieldIt.next();
                    String memberName = m.getMemberName();
                    String memberType = m.getMemberType();
                    String cmFieldMethodName = getCapitalizedFieldName(memberName);
                    boolean isPKMember = m.isPrimaryKey();
                    boolean addMembersToPKClass = (needsPKClass && isPKMember);
                    JavaClass addToClass = (addMembersToPKClass ? pkClass : javaClass);
                    ArrayList addToFieldsList = (addMembersToPKClass ? fieldsToAddPK : fieldsToAdd);
                    ArrayList addToMethodsList = (addMembersToPKClass ? methodsToAddPK : methodsToAdd);
                    
                    String temporal = null;
                    if ("java.sql.Date".equals(memberType)) { //NOI18N
                        m.setMemberType("java.util.Date");
                        temporal = "DATE";
                    } else if ("java.sql.Time".equals(memberType)) { //NOI18N
                        m.setMemberType("java.util.Date");
                        temporal = "TIME";
                    } else if ("java.sql.Timestamp".equals(memberType)) { //NOI18N
                        m.setMemberType("java.util.Date");
                        temporal = "TIMESTAMP";
                    }
                    
                    if (temporal != null) {  // memberType changed, update it
                        memberType = m.getMemberType();
                    }
                    
                    Field field = createField(memberName, memberType, Modifier.PRIVATE, addToClass);
                    addToFieldsList.add(field);
                    // workaround for issuezilla bug 77758
                    fieldToTypeName.put(field, memberType);
                    // end of workaround
 
                    Method getter = JMIGenerationUtil.createGetterMethod(memberName, cmFieldMethodName, memberType, addToClass);
                    
                    if (isPKMember) {
                        if (!needsPKClass) {
                            pkField = field;
                        }
                        pkFieldName = pkField.getName();
                        genBeans[i].setPkFieldName(pkFieldName);
                        
                        //add @Id() only if not in an embeddable PK class
                        if (!needsPKClass) {
                            Annotation idAnnotation = JMIGenerationUtil.createAnnotation(addToClass, "javax.persistence.Id", Collections.EMPTY_LIST); //NOI18N
                            addAnnotation(fieldAccess, field, getter, idAnnotation);
                        }
                    }
                    boolean isLobType = m.isLobType();
                    if (isLobType) {
                        Annotation lobAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.Lob", Collections.EMPTY_LIST);
                        addAnnotation(fieldAccess, field, getter, lobAnnotation);
                    }

                    ArrayList colAttrs = new ArrayList();
                    String columnName = (String) dbMappings.getCMPFieldMapping().get(memberName);
                    AttributeValue columnNameAttrValue = JMIGenerationUtil.createAttributeValue(addToClass, "name", columnName); //NOI18N
                    colAttrs.add(columnNameAttrValue);
                    if (!m.isNullable()) {
                        AttributeValue columnNullableAttrValue = JMIGenerationUtil.createAttributeValue(addToClass, "nullable", false); //NOI18N
                        colAttrs.add(columnNullableAttrValue);
                        if (!isPKMember)
                            nonNullableFields.add(field);
                        else
                            pkColumns.add(columnName);
                    }
                    Annotation columnAnnotation = JMIGenerationUtil.createAnnotation(addToClass, "javax.persistence.Column", colAttrs); //NOI18N
                    addAnnotation(fieldAccess, field, getter, columnAnnotation);

                    if (temporal != null) {
                        AttributeValue tempAttrValue = JMIGenerationUtil.createAttributeValue(addToClass, "", "javax.persistence.TemporalType", temporal); //NOI18N
                        Annotation temporalAnnotation = JMIGenerationUtil.createAnnotation(addToClass, "javax.persistence.Temporal", Collections.singletonList(tempAttrValue)); //NOI18N
                        addAnnotation(fieldAccess, field, getter, temporalAnnotation);
                    }
                    addToMethodsList.add(getter);                    
                    addToMethodsList.add(JMIGenerationUtil.createSetterMethod(memberName, cmFieldMethodName, memberType, addToClass));
                    
                    // generate equivalent of finder methods - named query annotations
                    if (genQueries && !isLobType) {
                        ArrayList queryAttrs = new ArrayList();
                        AttributeValue queryNameAttrValue = JMIGenerationUtil.createAttributeValue(
                                javaClass, "name", beanName + ".findBy" + cmFieldMethodName); //NOI18N
                        queryAttrs.add(queryNameAttrValue);
                        
                        if (namedQueryPrefix == null) {
                            char firstLetter = beanName.toLowerCase().charAt(0);
                            namedQueryPrefix = "SELECT " + firstLetter + " FROM " + beanName + " " + // NOI18N
                                    firstLetter + " WHERE " + firstLetter + "."; // NOI18N
                        }
                        // need a prefix of "pkFieldName." if this is part of a composite pk
                        String memberAccessString = ((addMembersToPKClass) ? 
                            (pkFieldName + "." + memberName) : memberName);     // NOI18N
                        AttributeValue queryStringAttrValue = JMIGenerationUtil.createAttributeValue(
                                javaClass, "query", namedQueryPrefix + //NOI18N
                                memberAccessString + ((genNamedParams) ? (" = :" + memberName) : "= ?1")); //NOI18N
                        queryAttrs.add(queryStringAttrValue);
                        Annotation namedQueryAnnotation = JMIGenerationUtil.createAnnotation(
                                javaClass, "javax.persistence.NamedQuery", queryAttrs); //NOI18N
                        namedQueriesList.add(namedQueryAnnotation);
                    }
                }

                if (genQueries && !namedQueriesList.isEmpty()) {
                    ArrayList queryAttrs = new ArrayList();
                    AttributeValue queryValueAttrValue = JMIGenerationUtil.createAttributeValue(
                            javaClass, "", namedQueriesList); //NOI18N
                    queryAttrs.add(queryValueAttrValue);
                    Annotation namedQueriesAnnotation = JMIGenerationUtil.createAnnotation(
                            javaClass, "javax.persistence.NamedQueries", queryAttrs); //NOI18N
                    javaClass.getAnnotations().add(namedQueriesAnnotation);
                }

                // add constructor which takes pk fields as args 
                List pkFields = Collections.singletonList(pkField);
                javaClassFeatures.add(JMIGenerationUtil.createConstructor(javaClass, pkFields));

                // if different than pk fields constructor, add constructor 
                // which takes all non-nullable non-relationship fields as args
                if (nonNullableFields.size() > 0) {
                    nonNullableFields.add(0, pkField);
                    javaClassFeatures.add(JMIGenerationUtil.createConstructor(javaClass, nonNullableFields));
                }

                for (Iterator it = genBeans[i].getRoles().iterator(); it.hasNext();) {
                    RelationshipRole role = (RelationshipRole) it.next();
                    
                    String memberName = role.getFieldName();
                    String cmFieldMethodName = getCapitalizedFieldName(memberName);
                    
                    boolean isToMany = role.isToMany();
                    String rv = getCmrFieldBaseType(role, genBeans[i].getPackage(), javaClass);
                    String rvType = isToMany ? java.util.Collection.class.getName() : rv;
                    Field field = createField(memberName, rvType, Modifier.PRIVATE, javaClass);
                    if (isToMany) {
                        List typeArgs = ((MultipartId)field.getTypeName()).getTypeArguments();
                        typeArgs.add(JMIGenerationUtil.createImport(javaClass, rv));
                    }
                    fieldsToAdd.add(field);

                    Method getter = JMIGenerationUtil.createGetterMethod(memberName, cmFieldMethodName, rvType, javaClass);
                    if (isToMany) {
                        List typeArgs = ((MultipartId)getter.getTypeName()).getTypeArguments();
                        typeArgs.add(JMIGenerationUtil.createImport(javaClass, rv));
                    }
                    
                    boolean cascadeDelete = role.isCascade();
                    
                    ArrayList annAttributes = new ArrayList();
                    if (cascadeDelete) {
                        AttributeValue cascadeAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "cascade", "javax.persistence.CascadeType", "ALL"); //NOI18N
                        annAttributes.add(cascadeAttibuteValue);
                    }
                    if (role.equals(role.getParent().getRoleB())) {
                        AttributeValue mappedByAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "mappedBy", role.getParent().getRoleA().getFieldName()); //NOI18N
                        annAttributes.add(mappedByAttibuteValue);
                    } else {
                        if (role.isMany() && role.isToMany()) {
                            ArrayList joinAttributes = new ArrayList();
                            AttributeValue joinTableNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "name", (String) dbMappings.getJoinTableMapping().get(role.getFieldName())); //NOI18N
                            joinAttributes.add(joinTableNameAttrValue);
                            
                            CMPMappingModel.JoinTableColumnMapping joinColumnMap = dbMappings.getJoinTableColumnMppings().get(role.getFieldName());
                            
                            ArrayList joinCols = new ArrayList();
                            String[] colNames = joinColumnMap.getColumns();
                            String[] refColNames = joinColumnMap.getReferencedColumns();
                            for(int colIndex = 0; colIndex < colNames.length; colIndex++) {
                                AttributeValue joinColumnNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "name", colNames[colIndex] ); //NOI18N
                                AttributeValue joinColumnRefNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "referencedColumnName", refColNames[colIndex] ); //NOI18N
                                ArrayList attrs = new ArrayList();
                                attrs.add(joinColumnNameAttrValue);
                                attrs.add(joinColumnRefNameAttrValue);
                                Annotation joinColumnAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.JoinColumn", attrs); //NOI18N
                                joinCols.add(joinColumnAnnotation);
                            }
                            AttributeValue joinColumnsNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "joinColumns", joinCols);
                            joinAttributes.add(joinColumnsNameAttrValue);
                            
                            ArrayList inverseCols = new ArrayList();
                            String[] invColNames = joinColumnMap.getInverseColumns();
                            String[] refInvColNames = joinColumnMap.getReferencedInverseColumns();
                            for(int colIndex = 0; colIndex < invColNames.length; colIndex++) {
                                AttributeValue joinColumnNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "name", invColNames[colIndex] ); //NOI18N
                                AttributeValue joinColumnRefNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "referencedColumnName", refInvColNames[colIndex] ); //NOI18N
                                ArrayList attrs = new ArrayList();
                                attrs.add(joinColumnNameAttrValue);
                                attrs.add(joinColumnRefNameAttrValue);
                                Annotation joinColumnAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.JoinColumn", attrs); //NOI18N
                                inverseCols.add(joinColumnAnnotation);
                            }
                            AttributeValue inverseColumnsNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "inverseJoinColumns", inverseCols);
                            joinAttributes.add(inverseColumnsNameAttrValue);
                            
                            Annotation joinTableAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.JoinTable", joinAttributes); //NOI18N
                            addAnnotation(fieldAccess, field, getter, joinTableAnnotation);
                        } else {
                            String[] colNames = (String[]) dbMappings.getCmrFieldMapping().get(role.getFieldName());
                            CMPMappingModel relatedMappings = beanMap.get(role.getParent().getRoleB().getEntityName()).getCMPMapping();
                            String[] invColNames = (String[]) relatedMappings.getCmrFieldMapping().get(role.getParent().getRoleB().getFieldName());
                            if (colNames.length == 1) {
                                AttributeValue joinColumnNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "name", colNames[0]); //NOI18N
                                AttributeValue joinColumnRefNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "referencedColumnName", invColNames[0]); //NOI18N
                                ArrayList<AttributeValue> attrs = new ArrayList<AttributeValue>();
                                attrs.add(joinColumnNameAttrValue);
                                attrs.add(joinColumnRefNameAttrValue);
                                makeReadOnlyIfNecessary(pkColumns, colNames[0], javaClass, attrs);
                                Annotation joinColumnAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.JoinColumn", attrs); //NOI18N
                                addAnnotation(fieldAccess, field, getter, joinColumnAnnotation);
                            } else {
                                ArrayList joinCols = new ArrayList();
                                for(int colIndex = 0; colIndex < colNames.length; colIndex++) {
                                    AttributeValue joinColumnNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "name", colNames[colIndex] ); //NOI18N
                                    AttributeValue joinColumnRefNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "referencedColumnName", invColNames[colIndex]); //NOI18N
                                    ArrayList<AttributeValue> attrs = new ArrayList<AttributeValue>();
                                    attrs.add(joinColumnNameAttrValue);
                                    attrs.add(joinColumnRefNameAttrValue);
                                    makeReadOnlyIfNecessary(pkColumns, colNames[colIndex], javaClass, attrs);
                                    Annotation joinColumnAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.JoinColumn", attrs); //NOI18N
                                    joinCols.add(joinColumnAnnotation);
                                }
                                AttributeValue joinColumnsNameAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "value", joinCols);
                                Annotation joinColumnsAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.JoinColumns", Collections.singletonList(joinColumnsNameAttrValue)); //NOI18N
                                addAnnotation(fieldAccess, field, getter, joinColumnsAnnotation);
                            }
                        }
                    }
                    String relationAnn;
                    if (role.isMany() && role.isToMany()) {
                        relationAnn = "ManyToMany"; //NOI18N
                    } else if (role.isMany()) {
                        relationAnn = "ManyToOne"; //NOI18N
                    } else if (role.isToMany()) {
                        relationAnn = "OneToMany"; //NOI18N
                    } else {
                        relationAnn = "OneToOne";  //NOI18N
                    }
                    Annotation relAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence." + relationAnn, annAttributes); //NOI18N
                    addAnnotation(fieldAccess, field, getter, relAnnotation);
                    methodsToAdd.add(getter);
                    Method setter = JMIGenerationUtil.createSetterMethod(memberName, cmFieldMethodName, rvType, javaClass);
                    if (isToMany) {
                        List typeArgs = ((MultipartId)((Parameter)setter.getParameters().get(0)).getTypeName()).getTypeArguments();
                        typeArgs.add(JMIGenerationUtil.createImport(javaClass, rv));
                    }
                    methodsToAdd.add(setter);
                }

                // add equals and hashCode methods
                methodsToAdd.add(JMIGenerationUtil.createHashCodeMethod(javaClass, pkFields));
                methodsToAdd.add(JMIGenerationUtil.createEntityEqualsMethod(javaClass, pkFields));
                methodsToAdd.add(JMIGenerationUtil.createToStringMethod(javaClass, pkFields));
                
                // add fields, get/set methods, and equals/hashCode methods to pk class as well
                // as updating the pk field based constructor in the entity class
                if (fieldsToAddPK != null) {
                    Constructor constructor = JMIGenerationUtil.createConstructor(javaClass, Modifier.PUBLIC);
                    String simpleName = pkClass.getSimpleName();
                    StringBuffer constructorBody = new StringBuffer("this." + pkFieldName
                            + " = new " + simpleName + "("); //NOI18N
                    StringBuffer constructorJavaDoc = new StringBuffer(
                            NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_Constructor", 
                                simpleName));
                    List params = constructor.getParameters();
                    List featuresList = pkClass.getFeatures();
                    int pkFieldsCount = fieldsToAddPK.size();

                    // fields should be before constructors (which are added to features earlier)
                    Collections.reverse(fieldsToAddPK);
                    featuresList.add(JMIGenerationUtil.createConstructor(pkClass, fieldsToAddPK));
                    for(int j = 0; j < pkFieldsCount; j++) {
                        Field nextField = (Field)fieldsToAddPK.get(j);
                        String fieldName = nextField.getName();
                        Type fieldType = nextField.getType();
                        Parameter fieldParameter = JMIGenerationUtil.createParameter(
                                javaClass, fieldName, fieldType.getName());
                        
                        featuresList.add(0, nextField);
                        params.add(fieldParameter);
                        constructorBody.append(fieldName);
                        constructorBody.append((j < (pkFieldsCount - 1)) ? ", " : ");"); //NOI18N
                        // workaround for issuezilla bug 77758
                        String fieldTypeName = (String)fieldToTypeName.get(nextField);
                        // end of workaround
                        JMIGenerationUtil.createImport(javaClass, fieldTypeName);
                        constructorJavaDoc.append(NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_ConstructorParam", 
                            fieldName, simpleName));

                    }
                    constructor.setBodyText(constructorBody.toString());
                    constructor.setJavadocText(constructorJavaDoc.toString());
                    javaClassFeatures.add(constructor);

                    methodsToAddPK.add(JMIGenerationUtil.createHashCodeMethod(pkClass, fieldsToAddPK));
                    methodsToAddPK.add(JMIGenerationUtil.createEntityEqualsMethod(pkClass, fieldsToAddPK));
                    methodsToAddPK.add(JMIGenerationUtil.createToStringMethod(pkClass, fieldsToAddPK));
                    
                    for(Iterator it = methodsToAddPK.iterator(); it.hasNext();) {
                        featuresList.add((Method) it.next());
                    }
                }
                
                Collections.reverse(fieldsToAdd);
                for(Iterator it = fieldsToAdd.iterator(); it.hasNext();) {
                    // fields should be before constructor (which is added to features earlier)
                    javaClassFeatures.add(0, (Field) it.next());
                }
                for(Iterator it = methodsToAdd.iterator(); it.hasNext();) {
                    javaClassFeatures.add((Method) it.next());
                }

                rollback = false;
            } catch (Exception e) {
                String message = e.getMessage();
                String newMessage = ((message == null) ? 
                    NbBundle.getMessage(RelatedCMPWizard.class, "ERR_GeneratingClass_NoExceptionMessage", className) :
                    NbBundle.getMessage(RelatedCMPWizard.class, "ERR_GeneratingClass", className, message));
                IOException wrappedException = new IOException(newMessage);
                wrappedException.initCause(e);
                throw wrappedException;
            } finally {
                JavaModel.getJavaRepository().endTrans(rollback);
            }
            
            FileObject fo = javaClass == null ? null : JavaModel.getFileObject(javaClass.getResource());
            Project project = FileOwnerQuery.getOwner(packageFileObject);
            if (fo != null) {
                fileObjects.add(fo);
                if (!Util.isSupportedJavaEEVersion(project) && ProviderUtil.getDDFile(project) != null) {
                    PUDataObject pudo = ProviderUtil.getPUDataObject(project);
                    PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
                    //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
                    if (pu.length == 1) {
                        pudo.addClass(pu[0], javaClass.getName());
                    }
                }
            }
        }
        JavaMetamodel manager = JavaMetamodel.getManager();
        for (Iterator iter = fileObjects.iterator(); iter.hasNext(); ) {
            FileObject fileObj = (FileObject)iter.next();
            manager.addModified(fileObj);
        }
        JavaModel.getJavaRepository().beginTrans(true); 
        JavaModel.getJavaRepository().endTrans(false);
        handle.progress(max);
    }
    
    public void init(WizardDescriptor wiz) {
        // get the table names for all entities in the project
        Project project = Templates.getProject(wiz);
        try {
            processEntities(PersistenceUtils.getEntityClasses(project));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        processEntities(PersistenceUtils.getAnnotationEntityClasses(project));
    }

    private void processEntities(Set<Entity> entityClasses) {
        for (Entity entity : entityClasses) {
            Table entityTable = entity.getTable();
            if (entityTable != null) {
                entityName2TableName.put(entityTable.getName(), entity.getClass2());
            }
        }
    }
    
    public void uninit() {
    }
    
    public String getFQClassName(String tableName) {
        return entityName2TableName.get(tableName);
    }
    
    public String generateEntityName(String name) {
        return name;
    }
    
    public Set createdObjects() {
        return Collections.EMPTY_SET;
    }

    private String getFieldName(String capitalizedFieldName) {
        StringBuffer sb = new StringBuffer(capitalizedFieldName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    private String getCapitalizedFieldName(String fieldName) {
        StringBuffer sb = new StringBuffer(fieldName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    private Field createField(String fieldName, String fieldType, int modifiers, JavaClass javaClass) {
        return JMIGenerationUtil.createField(javaClass, fieldName, modifiers, fieldType);
    }

    private void addAnnotation(boolean isFieldAccess, Field field, Method getter, Annotation annotation) {
        if (isFieldAccess) {
            field.getAnnotations().add(annotation);
        } else {
            getter.getAnnotations().add(annotation);
        }
    }

    private void makeReadOnlyIfNecessary(List pkColumns, String testColumnName, JavaClass javaClass, List attrs) {
        // if the join column is a pk column, add insertable=false, updatable=false
        if (pkColumns.contains(testColumnName)) {
            AttributeValue insertableAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "insertable", false); //NOI18N
            AttributeValue updatableAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "updatable", false); //NOI18N
            attrs.add(insertableAttrValue);
            attrs.add(updatableAttrValue);
        }
    }
    
    /**
     * Hackish fix for issue 76916. Should be removed in 6.0.
     */
    private class MDRInstanceNameListener implements MDRChangeListener {
        
        private boolean instanceDetected;
        private String name;
        
        public synchronized void change(MDRChangeEvent e) {
            Object newElem = ((AttributeEvent)e).getNewElement();
            if (newElem instanceof String && ((String)newElem).endsWith(name)) {
                instanceDetected = true;
                notify();
            }
        }
        
        public synchronized void waitInstance() {
            if (instanceDetected) {
                return;
            } else {
                try {
                    wait(3000); // set bound on time for safety reasons...
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        public void register(String name) {
            synchronized (this) {
                instanceDetected = false;
                this.name = name;
            }
            JavaModel.getJavaRepository().addListener(this, AttributeEvent.EVENTMASK_ATTRIBUTE);
        }
        
        public void unregister() {
            JavaModel.getJavaRepository().removeListener(this);
        }
    }
}
