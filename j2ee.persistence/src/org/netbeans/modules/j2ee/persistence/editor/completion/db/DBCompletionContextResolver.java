///*
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// *
// * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
// *
// * The contents of this file are subject to the terms of either the GNU
// * General Public License Version 2 only ("GPL") or the Common
// * Development and Distribution License("CDDL") (collectively, the
// * "License"). You may not use this file except in compliance with the
// * License. You can obtain a copy of the License at
// * http://www.netbeans.org/cddl-gplv2.html
// * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
// * specific language governing permissions and limitations under the
// * License.  When distributing the software, include this License Header
// * Notice in each file and include the License file at
// * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
// * particular file as subject to the "Classpath" exception as provided
// * by Sun in the GPL Version 2 section of the License file that
// * accompanied this code. If applicable, add the following below the
// * License Header, with the fields enclosed by brackets [] replaced by
// * your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * Contributor(s):
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// * Microsystems, Inc. All Rights Reserved.
// *
// * If you wish your version of this file to be governed by only the CDDL
// * or only the GPL Version 2, indicate your decision by adding
// * "[Contributor] elects to include this software in this distribution
// * under the [CDDL or GPL Version 2] license." If you do not indicate a
// * single choice of license, a recipient has the option to distribute
// * your version of this file under either the CDDL, the GPL Version 2 or
// * to extend the choice of license to its licensees as provided above.
// * However, if you add GPL Version 2 code and therefore, elected the GPL
// * Version 2 license, then the option applies only if the new code is
// * made subject to such option by the copyright holder.
// */
//
//package org.netbeans.modules.j2ee.persistence.editor.completion.db;
//
//import java.lang.reflect.Modifier;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeSet;
//import javax.swing.text.BadLocationException;
//import org.netbeans.api.db.explorer.DatabaseConnection;
//import org.netbeans.api.project.FileOwnerQuery;
//import org.netbeans.api.project.Project;
//import org.netbeans.editor.ext.java.JCExpression;
//import org.netbeans.jmi.javamodel.Annotation;
//import org.netbeans.jmi.javamodel.ClassDefinition;
//import org.netbeans.jmi.javamodel.Feature;
//import org.netbeans.jmi.javamodel.Field;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.Type;
//import org.netbeans.modules.dbschema.ColumnElement;
//import org.netbeans.modules.dbschema.TableElement;
//import org.netbeans.modules.j2ee.common.DatasourceHelper;
//import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
//import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
//import org.netbeans.modules.j2ee.metadata.JMIClassIntrospector;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Entity;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.EntityMappings;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.ManyToMany;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.ManyToOne;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.OneToMany;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.OneToOne;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.SecondaryTable;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Table;
//import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
//import org.netbeans.modules.j2ee.persistence.editor.completion.AnnotationUtils;
//import org.netbeans.modules.j2ee.persistence.editor.completion.CompletionContextResolver;
//import org.netbeans.modules.j2ee.persistence.editor.completion.JMIUtils;
//import org.netbeans.modules.j2ee.persistence.editor.completion.NNCompletionQuery;
//import org.netbeans.modules.j2ee.persistence.editor.completion.NNParser;
//import org.netbeans.modules.j2ee.persistence.editor.completion.NNResultItem;
//import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
//import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
//import org.netbeans.modules.javacore.JMManager;
//import org.openide.ErrorManager;
//import org.openide.awt.StatusDisplayer;
//import org.openide.filesystems.FileObject;
//import org.openide.util.NbBundle;
//
///**
// *
// * @author Marek Fukala
// */
// TODO: RETOUCHE
//public class DBCompletionContextResolver implements CompletionContextResolver {
//    
//    private DatabaseConnection dbconn;
//    private DBMetaDataProvider provider;
//    
//    //annotations names handled somehow by this completion context resolver
//    private static final String[] ANNOTATION_QUERY_TYPES = {
//        "Table", //0
//        "SecondaryTable", //1
//        "Column", //2
//        "PrimaryKeyJoinColumn", //3
//        "JoinColumn", //4
//        "JoinTable", //5
//        "PersistenceUnit", //6
//        "PersistenceContext", //7
//        "ManyToMany"//8
//    };
//    
//    private static final String PERSISTENCE_PKG = "javax.persistence";
//    
//    public List resolve(JCExpression exp, NNCompletionQuery.Context ctx) {
//        
//        List<NNResultItem> result = new ResultItemsFilterList(ctx);
//        
//        //parse the annotation
//        NNParser.NN parsedNN = ctx.getParsedAnnotation();
//        if (parsedNN == null) return result;
//        
//        NNParser.NNAttr nnattr = parsedNN.getAttributeForOffset(ctx.getCompletionOffset());
//        if(nnattr == null) return result;
//        
//        String annotationName = parsedNN.getName();
//        if(annotationName == null) return result;
//        
//        try {
//            //get nn index from the nn list
//            int index = getAnnotationIndex(annotationName);
//            if(index == 6 || index == 7) {
//                //we do not need database connection for PU completion
//                completePersistenceUnitContext(ctx, parsedNN, nnattr, result);
//            } else if(index != -1) {
//                //the completion has been invoked in supported annotation and there is no db connection initialized yet
//                //try to init the database connection
//                dbconn = findDatabaseConnection(ctx);
//                if(dbconn != null) {
//                    // DatabaseConnection.getJDBCConnection() unfortunately acquires Children.MUTEX read access;
//                    // it should not be called in a MDR transaction, as this is deadlock-prone
//                    assert Thread.currentThread() != JMManager.getTransactionMutex().getThread();
//                    
//                    Connection conn = dbconn.getJDBCConnection();
//                    if(conn != null) {
//                        this.provider = getDBMetadataProvider(dbconn, conn);
//                    } else {
//                        //Database connection not established ->
//                        //put 'connect' CC item
//                        result = new ArrayList();
//                        result.add(new NNResultItem.NoConnectionElementItem(dbconn));
//                        return result;
//                    }
//                } else {
//                    //no database connection -> give up
//                    ErrorManager.getDefault().log("No Database Connection.");
//                    return result;
//                }
//            }
//            
//            //test if the initialization of DB and DBMetadataProvider has succeeded
//            if(this.provider != null) {
//                //and retrieve the CC items under MDR transaction
//                JMIUtils utils = JMIUtils.get(ctx.getBaseDocument());
//                utils.beginTrans(false);
//                try {
//                    ((JMManager) JMManager.getManager()).setSafeTrans(true);
//                    switch(index) {
//                        case 0:
//                            completeTable(parsedNN, nnattr, result, false);//Table
//                            break;
//                        case 5: //JoinTable
//                        case 1:
//                            completeTable(parsedNN, nnattr, result, true);//SecondaryTable
//                            break;
//                        case 2:
//                            completeColumn(ctx, parsedNN, nnattr, result);//Column
//                            break;
//                        case 3:
//                            completePrimaryKeyJoinColumn(ctx, parsedNN, nnattr, result);
//                            break;
//                        case 4:
//                            completeJoinColumn(ctx, parsedNN, nnattr, result); //JoinColumn
//                            break;
//                        case 8:
//                            completeManyToMany(ctx, parsedNN, nnattr, result);
//                    }
//                } finally {
//                    utils.endTrans(false);
//                }
//            }
//            
//        } catch (SQLException e) {
//            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
//        }
//        
//        return result;
//    }
//    
//    /** @return index of the annotation type qhich is going to be queried or -1 if no such annotation found. */
//    private int getAnnotationIndex(String annotationName) {
//        if(annotationName.startsWith(PERSISTENCE_PKG)) {
//            //cut off the package
//            annotationName = annotationName.substring(annotationName.lastIndexOf('.') + 1);
//        }
//        for(int i = 0; i < ANNOTATION_QUERY_TYPES.length; i++) {
//            if(ANNOTATION_QUERY_TYPES[i].equals(annotationName)) return i;
//        }
//        return -1;
//    }
//    
//    private DBMetaDataProvider getDBMetadataProvider(DatabaseConnection dbconn, Connection con) {
//        return DBMetaDataProvider.get(con, dbconn.getDriverClass());
//    }
//    
//    private DatabaseConnection findDatabaseConnection(NNCompletionQuery.Context ctx) {
//        PersistenceUnit[] pus = ctx.getPersistenceUnits();
//        if(pus == null || pus.length == 0) {
//            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "There isn't any defined persistence unit for this class in the project!");
//            return null;
//        }
//        PersistenceUnit pu = pus[0]; // XXX only using the first persistence unit
//        
//        // try to find a connection specified using the PU properties
//        DatabaseConnection dbconn = ProviderUtil.getConnection(pu);
//        if (dbconn != null) {
//            return dbconn;
//        }
//        
//        // try to find a datasource-based connection, but only for a FileObject-based context,
//        // otherwise we don't have a J2eeModuleProvider to retrieve the DS's from
//        String datasourceName = ProviderUtil.getDatasourceName(pu);
//        if (datasourceName == null) {
//            return null;
//        }
//        FileObject fo = ctx.getFileObject();
//        if (fo == null) {
//            return null;
//        }
//        Project project = FileOwnerQuery.getOwner(fo);
//        if (project == null) {
//            return null;
//        }
//        J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
//        if (moduleProvider == null) {
//            return null;
//        }
//        Datasource datasource = DatasourceHelper.findDatasource(moduleProvider, datasourceName);
//        if (datasource == null) {
//            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The " + datasourceName + " was not found."); // NOI18N
//            return null;
//        }
//        List<DatabaseConnection> dbconns = DatasourceHelper.findDatabaseConnections(datasource);
//        if (dbconns.size() > 0) {
//            return dbconns.get(0);
//        }
//        return null;
//    }
//    
//    private List completeTable(NNParser.NN nn, NNParser.NNAttr nnattr, List<NNResultItem> results, boolean secondaryTable) throws SQLException {
//        String completedMember = nnattr.getName();
//        Map<String,Object> members = nn.getAttributes();
//        
//        if ("catalog".equals(completedMember)) { // NOI18N
//            Catalog[] catalogs = provider.getCatalogs();
//            for (int i = 0; i < catalogs.length; i++) {
//                String catalogName = catalogs[i].getName();
//                if (catalogName != null) {
//                    results.add(new NNResultItem.CatalogElementItem(catalogName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                }
//            }
//        } else if ("schema".equals(completedMember)) { // NOI18N
//            String catalogName = getThisOrDefaultCatalog((String)members.get("catalog")); // NOI18N
//            Catalog catalog = provider.getCatalog(catalogName);
//            if (catalog != null) {
//                Schema[] schemas = catalog.getSchemas();
//                for (int i = 0; i < schemas.length; i++) {
//                    results.add(new NNResultItem.SchemaElementItem(schemas[i].getName(), nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                }
//            }
//        } else if ("name".equals(completedMember)) { // NOI18N
//            String catalogName = getThisOrDefaultCatalog((String)members.get("catalog")); // NOI18N
//            String schemaName = getThisOrDefaultSchema((String)members.get("schema")); // NOI18N
//            Schema schema = DBMetaDataUtils.getSchema(provider, catalogName, schemaName);
//            if (schema != null) {
//                String[] tableNames = schema.getTableNames();
//                for (int i = 0; i < tableNames.length; i++) {
//                    results.add(new NNResultItem.TableElementItem(tableNames[i], nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                }
//            }
//        }
//        return results;
//    }
//    
//    private List completePrimaryKeyJoinColumn(NNCompletionQuery.Context ctx, NNParser.NN nn, NNParser.NNAttr nnattr, List<NNResultItem> results) throws SQLException {
//        String completedMember = nnattr.getName();
//        Map<String,Object> members = nn.getAttributes();
//        
//        if ("name".equals(completedMember)) { // NOI18N
//            //XXX should I take into account the @SecondaryTable here???
//            Entity entity = PersistenceUtils.getEntity(ctx.getJavaClass(), ctx.getEntityMappings());
//            if(entity != null) {
//                Table table = entity.getTable();
//                if(table != null) {
//                    String tableName = table.getName();
//                    if(tableName != null) {
//                        String catalogName = getThisOrDefaultCatalog(table.getCatalog());
//                        String schemaName = getThisOrDefaultSchema(table.getSchema());
//                        //if(DEBUG) System.out.println("Columns for " + catalogName + "." + schemaName + "." + tableName);
//                        TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
//                        if(tableElement != null) {
//                            ColumnElement[] columnElements = tableElement.getColumns();
//                            for (int i = 0; i < columnElements.length; i++) {
//                                results.add(new NNResultItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, true, -1));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        
//        return results;
//    }
//    
//    
//    private List completeColumn(NNCompletionQuery.Context ctx, NNParser.NN nn, NNParser.NNAttr nnattr, List<NNResultItem> results) throws SQLException {
//        String completedMember = nnattr.getName();
//        Map<String,Object> members = nn.getAttributes();
//        
//        if ("table".equals(completedMember)) { // NOI18N
//            Set/*<String>*/ mappingTables = getMappingEntityTableNames(ctx.getJavaClass());
//            for (Iterator i = mappingTables.iterator(); i.hasNext();) {
//                String tableName = (String)i.next();
//                results.add(new NNResultItem.TableElementItem(tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//            }
//        }
//        if ("name".equals(completedMember)) { // NOI18N
//            String catalogName = null;
//            String schemaName = null;
//            String tableName = (String)members.get("table"); // NOI18N
//            
//            if (tableName == null) {
//                //no table attribute provided
//                //get the columns from @Table and @SecondaryTable(s) annotations
//                Entity entity = PersistenceUtils.getEntity(ctx.getJavaClass(), ctx.getEntityMappings());
//                if(entity != null) {
//                    Table table = entity.getTable();
//                    if(table != null) {
//                        //the entity has table defined
//                        tableName = table.getName();
//                        if(tableName != null) {
//                            catalogName = getThisOrDefaultCatalog(table.getCatalog());
//                            schemaName = getThisOrDefaultSchema(table.getSchema());
//                            TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
//                            if(tableElement != null) {
//                                ColumnElement[] columnElements = tableElement.getColumns();
//                                for (int i = 0; i < columnElements.length; i++) {
//                                    results.add(new NNResultItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                                }
//                            }
//                        }
//                    }
//                    SecondaryTable[] stables = entity.getSecondaryTable();
//                    if(stables != null) {
//                        for(int idx = 0; idx < stables.length; idx++) {
//                            String secTableName = stables[idx].getName();
//                            TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, secTableName);
//                            if(tableElement != null) {
//                                ColumnElement[] columnElements = tableElement.getColumns();
//                                for (int i = 0; i < columnElements.length; i++) {
//                                    results.add(new NNResultItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                                }
//                            }
//                        }
//                    }
//                }
//            } else {
//                //table attribute of @Column annotation provided
//                catalogName = getThisOrDefaultCatalog(catalogName);
//                schemaName = getThisOrDefaultSchema(schemaName);
//                TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
//                if(tableElement != null) {
//                    ColumnElement[] columnElements = tableElement.getColumns();
//                    for (int i = 0; i < columnElements.length; i++) {
//                        results.add(new NNResultItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                    }
//                }
//            }
//        }
//        
//        return results;
//    }
//    
//    private List completeJoinColumn(NNCompletionQuery.Context ctx, NNParser.NN nn, NNParser.NNAttr nnattr, List<NNResultItem> results) throws SQLException {
//        String completedMember = nnattr.getName();
//        Map<String,Object> members = nn.getAttributes();
//        
//        if ("name".equals(completedMember)) { // NOI18N
//            //I need to get @Table annotation to get know which table is primary for this class
//            //XXX should I take into account the @SecondaryTable here???
//            Entity entity = PersistenceUtils.getEntity(ctx.getJavaClass(), ctx.getEntityMappings());
//            //TODO it is bad - since we should allow the CC to complete even the class is not "Entity"
//            if(entity != null && entity.getAttributes() != null) {
//                String propertyName = ctx.getCompletedMemberName();
//                String resolvedClassName = ctx.getCompletedMemberClassName();
////                JCClass resolvedClass = ctx.getSyntaxSupport().getClassFromName(resolvedClassName, true);
//                Type type = ctx.getSyntaxSupport().getTypeFromName(resolvedClassName, false, null, false);
//                
//                if(type == null) {
//                    //show an error message
//                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NNCompletionQuery.class, "MSG_CannotFindClass", new Object[]{resolvedClassName, propertyName}));
//                    return Collections.EMPTY_LIST;
//                }
//                
//                String resolvedType = type.getName();
//                
//                if(DEBUG) System.out.println("completion called on property " + propertyName + " of " + resolvedType + " type.");
//                
//                EntityMappings em = ctx.getEntityMappings();
//                
//                //set is in the declared class
//                ManyToOne[] m2o = entity.getAttributes().getManyToOne();
//                OneToOne[] o2o = entity.getAttributes().getOneToOne();
//                
//                //set is in the declaring class
//                OneToMany[] o2m = entity.getAttributes().getOneToMany();
//                //set is in both refered and declaring class
//                ManyToMany[] m2m = entity.getAttributes().getManyToMany();
//                
//                ManyToOne m2onn = null;
//                if(m2o != null) {
//                    for(int i = 0; i < m2o.length; i++) {
//                        if(m2o[i].getName().equals(propertyName)) {
//                            m2onn = m2o[i];
//                            break;
//                        }
//                    }
//                }
//                OneToOne o2onn = null;
//                if(o2o != null) {
//                    for(int i = 0; i < o2o.length; i++) {
//                        if(o2o[i].getName().equals(propertyName)) {
//                            o2onn = o2o[i];
//                            break;
//                        }
//                    }
//                }
//                
//                OneToMany o2mnn = null;
//                if(o2m != null) {
//                    for(int i = 0; i < o2m.length; i++) {
//                        if(o2m[i].getName().equals(propertyName)) {
//                            o2mnn = o2m[i];
//                            break;
//                        }
//                    }
//                }
//                
//                ManyToMany m2mnn = null;
//                if(m2m != null) {
//                    for(int i = 0; i < m2m.length; i++) {
//                        if(m2m[i].getName().equals(propertyName)) {
//                            m2mnn = m2m[i];
//                            break;
//                        }
//                    }
//                }
//                
//                
//                if(m2onn != null || o2onn != null) {
//                    if(DEBUG) System.out.println("found OneToOne or ManyToOne annotation on the completed field.");
//                    //OneToOne or ManyToOne
//                    //find the entity according to the type of the referred object
//                    Entity ent = PersistenceUtils.getEntity(resolvedType, ctx.getEntityMappings());
//                    
//                    //also check whether the entity is explicitly determined by "targetEntity"
//                    //attribute of the OneToOne or ManyToOne annotations
//                    if(m2onn != null) {
//                        String targetEntity = m2onn.getTargetEntity();
//                        if(targetEntity != null) {
//                            ent = PersistenceUtils.getEntity(targetEntity, em);
//                            if(DEBUG) System.out.println("entity " + ent.getName() +  " is specified in ManyToOne element.");
//                        }
//                    }
//                    if(o2onn != null) {
//                        String targetEntity = o2onn.getTargetEntity();
//                        if(targetEntity != null) {
//                            ent = PersistenceUtils.getEntity(targetEntity, em);
//                            if(DEBUG) System.out.println("entity " + ent.getName() +  " is specified in OneToOne element.");
//                        }
//                    }
//                    
//                    if(ent != null) {
//                        Table table = ent.getTable();
//                        if(table != null) {
//                            String catalogName = getThisOrDefaultCatalog(null); //XXX need to provide correct data
//                            String schemaName = getThisOrDefaultSchema(null);//XXX need to provide correct data
//                            String tableName = table.getName();
//                            if(tableName != null) {
//                                TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
//                                if(tableElement != null) {
//                                    ColumnElement[] columnElements = tableElement.getColumns();
//                                    for (int i = 0; i < columnElements.length; i++) {
//                                        results.add(new NNResultItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                                    }
//                                    if(DEBUG) System.out.println("added " +columnElements.length + " CC items.");
//                                }
//                            }
//                        } else {
//                            if(DEBUG) System.out.println("the found entity has not defined table!?! (probably a  bug in values defaultter).");
//                        }
//                    }
//                    
//                }
//                
//                //the @JoinTable doesn't make sense for @OneToMany
//                
//                if(m2mnn != null) {
//                    if(DEBUG) System.out.println("found ManyToMany annotation on the completed field.");
//                    //the column names in this case needs to be gotten from the surrounding @JoinTable annotation
//                    //using of the model doesn't make much sense here because once we complete @JoinColumn inside
//                    //a @JoinTable the @JoinTable must be present in the source
//                    
//                    //gettting the annotations structure from own simple parser
//                    
//                    if(DEBUG) System.out.println(nn);
//                    
//                    NNParser.NN tblNN = null;
//                    if(nn != null && nn.getName().equals("JoinTable")) { //NOI18N
//                        Map attrs = nn.getAttributes();
//                        Object val = attrs.get("table"); //NOI18N
//                        if(val != null && val instanceof NNParser.NN) {
//                            NNParser.NN tableNN = (NNParser.NN)val;
//                            if(tableNN.getName().equals("Table")) {//NOI18N
//                                tblNN = tableNN;
//                            }
//                        }
//                    }
//                    
//                    if(tblNN != null) {
//                        String catalogName = getThisOrDefaultCatalog((String)tblNN.getAttributes().get("catalog")); //XXX need to provide correct data
//                        String schemaName = getThisOrDefaultSchema((String)tblNN.getAttributes().get("schema"));//XXX need to provide correct data
//                        String tableName = (String)tblNN.getAttributes().get("name");
//                        if(tableName != null) {
//                            TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
//                            if(tableElement != null) {
//                                ColumnElement[] columnElements = tableElement.getColumns();
//                                for (int i = 0; i < columnElements.length; i++) {
//                                    results.add(new NNResultItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        
//        return results;
//    }
//    
//    private List completePersistenceUnitContext(NNCompletionQuery.Context ctx, NNParser.NN nn, NNParser.NNAttr nnattr, List<NNResultItem> results) throws SQLException {
//        String completedMember = nnattr.getName();
//        Map<String,Object> members = nn.getAttributes();
//        
//        if ("unitName".equals(completedMember)) { // NOI18N
//            PersistenceUnit[] pus = ctx.getPersistenceUnits();
//            for (PersistenceUnit pu : pus) {
//                results.add(new NNResultItem.PersistenceUnitElementItem(pu.getName(), nnattr.isValueQuoted(), nnattr.getValueOffset()));
//            }
//        }
//        
//        return results;
//    }
//    
//    private List completeManyToMany(NNCompletionQuery.Context ctx, NNParser.NN nn, NNParser.NNAttr nnattr, List<NNResultItem> results) throws SQLException {
//        String completedMember = nnattr.getName();
//        Map<String,Object> members = nn.getAttributes();
//        
//        if ("mappedBy".equals(completedMember)) { // NOI18N
//            String resolvedClassName = ctx.getCompletedMemberClassName();
//            Type type = ctx.getSyntaxSupport().getTypeFromName(resolvedClassName, false, null, false);
//            if(type != null) {
//                Entity entity = PersistenceUtils.getEntity(type.getName(), ctx.getEntityMappings());
//                if(entity != null) {
//                    //the class is entity => get all its properties
//                    ClassDefinition cdef = (ClassDefinition)type;
//                    for(Feature f : (List<Feature>)cdef.getFeatures()) {
//                        if(f instanceof Field) {
//                            if(!Modifier.isTransient(f.getModifiers())) {
//                                if(cdef.getMethod("get" + JMIClassIntrospector.capitalize(f.getName()), Collections.EMPTY_LIST, true) == null  //NOI18N
//                                        && cdef.getMethod("is" + JMIClassIntrospector.capitalize(f.getName()), Collections.EMPTY_LIST, true) == null ) { //NOI18N
//                                    //there is not getter for this field, add CC item
//                                    results.add(new NNResultItem.EntityPropertyElementItem(f.getName(), nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                                }
//                            }
//                        } else if (f instanceof Method) {
//                            if(JMIClassIntrospector.isGetter((Method)f)) {
//                                results.add(new NNResultItem.EntityPropertyElementItem(JMIClassIntrospector.getPropertyNameFromGetter((Method)f), nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                            }
//                        }
//                    }
//                    
//                }
//            }
//            
//            
//        }
//        
//        return results;
//    }
//    
//    
//    /**
//     * Returns the tables to which this class is mapped.
//     */
//    private Set/*<String>*/ getMappingEntityTableNames(JavaClass clazz) {
//        Set result = new TreeSet();
//        List/*<Annotation>*/ annotations = clazz.getAnnotations();
//        
//        for (Iterator i = annotations.iterator(); i.hasNext();) {
//            Annotation annotation = (Annotation)i.next();
//            String annotationTypeName = annotation.getType().getName();
//            
//            if ("javax.persistence.Table".equals(annotationTypeName)) { // NOI18N
//                String tableName = AnnotationUtils.getStringMemberValue(annotation, "name"); // NOI18N
//                if (tableName != null) {
//                    result.add(tableName);
//                }
//            } else if ("javax.persistence.SecondaryTable".equals(annotationTypeName)) { // NOI18N
//                String tableName = AnnotationUtils.getStringMemberValue(annotation, "name"); // NOI18N
//                if (tableName != null) {
//                    result.add(tableName);
//                }
//            } else if ("javax.persistence.SecondaryTables".equals(annotationTypeName)) { // NOI18N
//                List secondaryTableNNs = AnnotationUtils.getAnnotationsMemberValue(annotation, "value"); // NOI18N
//                for (Iterator j = secondaryTableNNs.iterator(); j.hasNext();) {
//                    Annotation secondaryTableNN = (Annotation)j.next();
//                    String tableName = AnnotationUtils.getStringMemberValue(secondaryTableNN, "name"); // NOI18N
//                }
//            }
//        }
//        
//        return result;
//    }
//    
//    private String getThisOrDefaultCatalog(String catalogName) throws SQLException {
//        assert provider != null;
//        if (catalogName != null) {
//            return catalogName;
//        } else {
//            return provider.getDefaultCatalog();
//        }
//    }
//    
//    private String getThisOrDefaultSchema(String schemaName) {
//        assert dbconn != null;
//        if (schemaName != null) {
//            return schemaName;
//        } else {
//            // XXX this may be wrong, the persistence provider would use
//            // the default connection's schema as gived by the database server
//            return dbconn.getSchema();
//        }
//    }
//    
//    private String getAnnotationTypeName(JCExpression exp) {
//        assert exp != null;
//        
//        String result = null;
//        
//        if (exp.getParameterCount() < 1) {
//            return result;
//        }
//        JCExpression variable = exp.getParameter(0);
//        if (variable.getExpID() == JCExpression.VARIABLE) {
//            //just @Table (without package specification)
//            return variable.getTokenText(0);
//        }
//        
//        if(variable.getExpID() == JCExpression.DOT) {
//            //@javax.persistence.Table (with package specification)
//            StringBuffer sb = new StringBuffer();
//            for(int i = 0; i < variable.getParameterCount(); i++) {
//                JCExpression subExp = variable.getParameter(i);
//                sb.append(subExp.getTokenText(0));
//                if(i < variable.getParameterCount() - 1) {
//                    sb.append('.');
//                }
//            }
//            return sb.toString();
//        }
//        
//        // XXX this does not count with an annotation type written like "javax.persistence.Table"
//        // should try to resolve the annotation type
//        
//        return result;
//    }
//    
//    
//    private static final class ResultItemsFilterList extends ArrayList {
//        private NNCompletionQuery.Context ctx;
//        public ResultItemsFilterList(NNCompletionQuery.Context ctx) {
//            super();
//            this.ctx = ctx;
//        }
//        
//        public boolean add(Object o) {
//            if(!(o instanceof NNResultItem)) return false;
//            
//            NNResultItem ri = (NNResultItem)o;
//            //check if the pretext corresponds to the result item text
//            try {
//                String preText = ctx.getBaseDocument().getText(ri.getSubstituteOffset(), ctx.getCompletionOffset() - ri.getSubstituteOffset());
//                if(ri.getItemText().startsWith(preText)) {
//                    return super.add(ri);
//                }
//            }catch(BadLocationException ble) {
//                //ignore
//            }
//            return false;
//        }
//    }
//    
//    private static final boolean DEBUG = Boolean.getBoolean("debug." + DBCompletionContextResolver.class.getName());
//}
