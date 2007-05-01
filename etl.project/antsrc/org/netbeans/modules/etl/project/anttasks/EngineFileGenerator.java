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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.project.anttasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.netbeans.modules.etl.codegen.DBConnectionDefinitionTemplate;
import org.netbeans.modules.etl.codegen.ETLCodegenUtil;
import org.netbeans.modules.etl.codegen.ETLProcessFlowGenerator;
import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.impl.InternalDBMetadata;
import com.sun.etl.engine.ETLEngine;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.utils.ETLDeploymentConstants;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.impl.SQLDefinitionImpl;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import com.sun.sql.framework.utils.StringUtil;
import com.sun.sql.framework.utils.XmlUtil;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * 
 */
public class EngineFileGenerator {
    
    private FileOutputStream fos = null;
        /*
         * hold a map of otd oids to connection def
         */
    private HashMap connDefs = new HashMap();
        /*
         * Holds a map of unique IDs (OTD OID + port type [either "-Source" or
         * "-Target"]) to InternalDBMetadata instances which hold CME-level
         * parameters such as directory location of flatfiles and whether dynamic
         * file name resolution is in effect.
         */
    private Map internalDBConfigParams = new HashMap();
    
        /*
         * Holds a map of OTD OIDs to corresponding connection pool names.
         *
         */
    private Map otdNamePoolNameMap = new HashMap();
    
        /*
         *
         */
    private DBConnectionDefinitionTemplate connectionDefnTemplate;
    
        /*
         *
         */
    private String collabName;
    
        /*
         * TODO fill up from env
         */
    Map otdCatalogOverrideMapMap = new HashMap();
        /*
         * TODO fill up from env
         */
    Map otdSchemaOverrideMapMap = new HashMap();
    
    public EngineFileGenerator() {
        try {
            this.connectionDefnTemplate = new DBConnectionDefinitionTemplate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void generateEngine(File etlFile, File buildDir) throws Exception {
        
        String etlFileName = etlFile.getName().substring(0, etlFile.getName().indexOf(".etl"));
        String engineFile = buildDir + "/" + etlFileName + "_engine.xml";
        
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        Element root = f.newDocumentBuilder().parse(etlFile).getDocumentElement();
        
        ETLDefinitionImpl def = new ETLDefinitionImpl();
        def.parseXML(root);
        collabName = def.getDisplayName();
        
        SQLDefinition sqlDefinition = def.getSQLDefinition();
        SQLDefinition defn = checkForDeploymentProperties(etlFile, etlFileName);
        if(defn != null) {
            System.out.print("Synchronizing the configuration with Design time.");
            defn = compareAndSync(sqlDefinition, defn, true);
            sqlDefinition = compareAndSync(sqlDefinition, defn, false);            
            System.out.println("Synchronization completed successfully");
        }
        populateConnectionDefinitions(sqlDefinition);
        sqlDefinition.overrideCatalogNamesForOtd(otdCatalogOverrideMapMap);
        sqlDefinition.overrideSchemaNamesForOtd(otdSchemaOverrideMapMap);
        ETLProcessFlowGenerator flowGen = ETLProcessFlowGeneratorFactory.getCollabFlowGenerator(
                sqlDefinition, true);
        flowGen.setWorkingFolder(ETLDeploymentConstants.PARAM_APP_DATAROOT);
        flowGen.setInstanceDBName(ETLDeploymentConstants.PARAM_INSTANCE_DB_NAME);
        flowGen.setInstanceDBFolder(ETLCodegenUtil.getEngineInstanceWorkingFolder());
        flowGen.setMonitorDBName(def.getDisplayName());
        flowGen.setMonitorDBFolder(ETLCodegenUtil.getMonitorDBDir(def.getDisplayName(),
                ETLDeploymentConstants.PARAM_APP_DATAROOT));
        
        if (connDefs.isEmpty()) {
            // TODO change the logic to read connDefs from env, now keep it same
            // a design time
            flowGen.applyConnectionDefinitions(false);
        } else {
            flowGen.applyConnectionDefinitions(connDefs, this.otdNamePoolNameMap,
                    internalDBConfigParams);
        }
        ETLEngine engine = flowGen.getScript();
        
        sqlDefinition.clearOverride(true, true);
        
        String engineContent = engine.toXMLString();
        
        fos = new FileOutputStream(engineFile);
        FileUtil.copy(engineContent.getBytes("UTF-8"), fos);
        
    }
    
    private SQLDefinition checkForDeploymentProperties(File etlFile, String etlFileName) {
        SQLDefinition sqlDefn = null;
        String confPath = etlFile.getAbsolutePath() + "\\..\\..\\nbproject\\config\\";
        confPath = confPath + etlFileName +".conf";
        System.out.println("Checking for configuration file for "+ etlFileName +".etl");
        File confFile = new File(confPath);
        if(confFile.exists()) {
            System.out.println("Found configuration file for " + etlFileName +".etl");
            sqlDefn = getConfigData(confFile);
            System.out.println("Parsing the content of the configuration file for " + etlFileName +".etl");
        } else {
            System.out.println("Configuration file not found. Using Design time configurations.");
        }
        return sqlDefn;
    }
    
    public SQLDefinition compareAndSync(SQLDefinition srcDefn, SQLDefinition tgtDefn, boolean isSource) {
        // sync the Models.
        Iterator it = null;
        if(isSource) {
            it = srcDefn.getSourceDatabaseModels().iterator();
        } else {
            it = srcDefn.getTargetDatabaseModels().iterator();
        }
        while(it.hasNext()) {
            boolean exists = false;
            SQLDBModel match = null;
            SQLDBModel srcModel = (SQLDBModel)it.next();
            Iterator confIterator = null;
            if(isSource) {
                confIterator = tgtDefn.getSourceDatabaseModels().iterator();
            } else {
                confIterator = tgtDefn.getTargetDatabaseModels().iterator();
            }
            while(confIterator.hasNext()) {
                SQLDBModel tgtModel = (SQLDBModel)confIterator.next();
                if(tgtModel.getModelName().equals(srcModel.getModelName())) {
                    exists = true;
                    match = tgtModel;
                    break;
                }
            }
            if(exists) {
                try {
                    tgtDefn.removeObject(match);
                    match = syncTables(srcModel, match);
                    tgtDefn.addObject(match);
                } catch (BaseException ex) {
                    // ignore
                }
            } else {
                try {
                    tgtDefn.addObject(srcModel);
                } catch (BaseException ex) {
                    // ignore
                }
            }
        }
        return tgtDefn;
    }
    
    private SQLDBModel syncTables(SQLDBModel srcModel, SQLDBModel tgtModel) {
        Iterator it = srcModel.getTables().iterator();
        while(it.hasNext()) {
            boolean exists = false;
            SQLDBTable srcTbl = (SQLDBTable)it.next();
            Iterator confIterator = tgtModel.getTables().iterator();
            while(confIterator.hasNext()) {
                SQLDBTable tgtTbl = (SQLDBTable)confIterator.next();
                if(tgtTbl.getName().equals(srcTbl.getName())) {
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                tgtModel.addTable(srcTbl);
            }
        }
        return tgtModel;
    }
    
    public SQLDefinition getConfigData(File configFile) {
        org.w3c.dom.Node rootNode = null;
        SQLDefinition sqlDefn = null;
        try {
            Element element = XmlUtil.loadXMLFile(new BufferedReader(new FileReader(configFile)));
            rootNode = (org.w3c.dom.Node) element;
        } catch (Exception ex) {
            //ignore
        }
        if(rootNode != null) {
            org.w3c.dom.Node sqlNode = rootNode.getFirstChild();
            try {
                sqlDefn = new SQLDefinitionImpl((Element)sqlNode);
            } catch (Exception ex) {
                sqlDefn = null;
            }
        }
        return sqlDefn;
    }
    
    private void populateConnectionDefinitions(SQLDefinition def) {
        List srcDbmodels = def.getSourceDatabaseModels();
        Iterator iterator = srcDbmodels.iterator();
        while (iterator.hasNext()) {
            initMetaData(iterator, "source");
            
        }
        
        List trgDbmodels = def.getTargetDatabaseModels();
        iterator = trgDbmodels.iterator();
        while (iterator.hasNext()) {
            initMetaData(iterator, "target");
        }
        
        //System.out.println(connDefs);
        //System.out.println(otdNamePoolNameMap);
        //System.out.println(internalDBConfigParams);
        
        connDefs.size();
        otdNamePoolNameMap.size();
    }
    
    /**
     * @param iterator
     * @param string
     */
    @SuppressWarnings("unchecked")
    private void initMetaData(Iterator iterator, String dbtable) {
        
        SQLDBModel element = (SQLDBModel) iterator.next();
        String oid = getSQDBModelOid(element);
        if (oid == null) {// support older version of DBModel
            return;
        }
        SQLDBConnectionDefinition originalConndef = (SQLDBConnectionDefinition) element
                .getConnectionDefinition();
        
        if (originalConndef.getDriverClass().equals("org.axiondb.jdbc.AxionDriver")) {
            SQLDBConnectionDefinition conndefTemplate = this.connectionDefnTemplate
                    .getDBConnectionDefinition("STCDBADAPTER");
            SQLDBConnectionDefinition conndef = (SQLDBConnectionDefinition) conndefTemplate
                    .cloneObject();
            
            setConnectionParams(conndef);
            
            String key = originalConndef.getName() + "-" + dbtable;
            conndef.setName(key);
            connDefs.put(key, conndef);
            otdNamePoolNameMap.put(oid, key);
            // TODO all the parameters for InternalDBMetadata comes from collab
            // env
            InternalDBMetadata dbMetadata = new InternalDBMetadata("c:\\temp", false, key);
            internalDBConfigParams.put(oid, dbMetadata);
        } else { // jdbc connection
            
            SQLDBConnectionDefinition conndef =originalConndef ;
            
            
            String key = originalConndef.getName() + "-" + dbtable;
            conndef.setName(key);
            connDefs.put(key, conndef);
            otdNamePoolNameMap.put(oid, key);
            // TODO all the parameters for InternalDBMetadata comes from collab
            // env
            //InternalDBMetadata dbMetadata = new InternalDBMetadata("c:\\temp", false, key);
            //internalDBConfigParams.put(oid, dbMetadata);
        }
        
    }
    
    /**
     * @param conndef
     */
    @SuppressWarnings("unchecked")
    private void setConnectionParams(SQLDBConnectionDefinition conndef) {
        String metadataDir = ETLCodegenUtil.getEngineInstanceWorkingFolder();
        Map connectionParams = new HashMap();
        connectionParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, collabName);
        connectionParams.put(DBConnectionDefinitionTemplate.KEY_METADATA_DIR, metadataDir);
        
        conndef.setConnectionURL(StringUtil.replace(conndef.getConnectionURL(), connectionParams));
    }
    
    /**
     * @param element
     * @return
     */
    private String getSQDBModelOid(SQLDBModel element) {
        if (element.getAttribute("refKey") == null)
            return null;
        String oid = (String) element.getAttribute("refKey").getAttributeValue();
        return oid;
    }
    
    public static void main(String[] args) {
        String etlFile = "test/xxx.etl";
        
        File f = new File(etlFile);
        File buildDir = new File("test");
        EngineFileGenerator g = new EngineFileGenerator();
        
        try {
            g.generateEngine(f, buildDir);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static String toXml(Node node, String encoding, boolean omitXMLDeclaration) {
        String ret = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.ENCODING, encoding);
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration ? "yes"
                    : "no");
            trans.transform(new DOMSource(node), new StreamResult(baos));
            ret = baos.toString(encoding);
            // mLogger.debug("ret: " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
}
