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

/**
 *
 */
public class EngineFileGenerator {

    private FileOutputStream fos = null;
    /*
     * hold a map of db oids to connection def
     */
    private HashMap connDefs = new HashMap();
    /*
     * Holds a map of unique IDs (DB OID + port type [either "-Source" or
     * "-Target"]) to InternalDBMetadata instances which hold CME-level
     * parameters such as directory location of flatfiles and whether dynamic
     * file name resolution is in effect.
     */
    private Map internalDBConfigParams = new HashMap();
    /*
     * Holds a map of DB OIDs to corresponding connection pool names.
     *
     */
    private Map dbNamePoolNameMap = new HashMap();
    private DBConnectionDefinitionTemplate connectionDefnTemplate;
    private String collabName;
    Map dbCatalogOverrideMapMap = new HashMap();
    Map dbSchemaOverrideMapMap = new HashMap();

    public EngineFileGenerator() {
        try {
            this.connectionDefnTemplate = new DBConnectionDefinitionTemplate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEngine(File etlFile, File buildDir) throws Exception {

        String etlFileName = etlFile.getName().substring(0, etlFile.getName().indexOf(".etl"));
        String projectName = buildDir.getParentFile().getName();
        String engineFile = buildDir + "/" + projectName + "_" + etlFileName + "_engine.xml";

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        Element root = f.newDocumentBuilder().parse(etlFile).getDocumentElement();

        ETLDefinitionImpl def = new ETLDefinitionImpl();
        def.parseXML(root);
        collabName = def.getDisplayName();

        SQLDefinition sqlDefinition = def.getSQLDefinition();
        SQLDefinition defn = checkForDeploymentProperties(etlFile, etlFileName);
        if (defn != null) {
            System.out.print("Synchronizing the configuration with Design time.");
            defn = compareAndSync(sqlDefinition, defn, true);
            sqlDefinition = compareAndSync(sqlDefinition, defn, false);
            System.out.println("Synchronization completed successfully");
        }
        populateConnectionDefinitions(sqlDefinition);
        sqlDefinition.overrideCatalogNamesForDb(dbCatalogOverrideMapMap);
        sqlDefinition.overrideSchemaNamesForDb(dbSchemaOverrideMapMap);
        ETLProcessFlowGenerator flowGen = ETLProcessFlowGeneratorFactory.getCollabFlowGenerator(sqlDefinition, true);
        flowGen.setWorkingFolder(sqlDefinition.getAxiondbWorkingDirectory());
        flowGen.setInstanceDBName("instancedb");
        flowGen.setInstanceDBFolder(ETLCodegenUtil.getEngineInstanceWorkingFolder());
        //flowGen.setInstanceDBFolder(ETLCodegenUtil.getEngineInstanceWorkingFolder());
        flowGen.setMonitorDBName(def.getDisplayName());
        flowGen.setMonitorDBFolder(ETLCodegenUtil.getMonitorDBDir(def.getDisplayName(), ETLDeploymentConstants.PARAM_APP_DATAROOT));

        if (connDefs.isEmpty()) {
            // TODO change the logic to read connDefs from env, now keep it same
            // a design time
            flowGen.applyConnectionDefinitions(false);
        } else {
            flowGen.applyConnectionDefinitions(connDefs, this.dbNamePoolNameMap, internalDBConfigParams);
        }
        ETLEngine engine = flowGen.getScript();
        engine.setDisplayName(projectName+"_"+etlFileName);

        sqlDefinition.clearOverride(true, true);

        String engineContent = engine.toXMLString();

        fos = new FileOutputStream(engineFile);
        FileUtil.copy(engineContent.getBytes("UTF-8"), fos);
        fos.flush();
        fos.close();
    }

    private SQLDefinition checkForDeploymentProperties(File etlFile, String etlFileName) {
        SQLDefinition sqlDefn = null;
        String confPath = etlFile.getAbsolutePath() + "\\..\\..\\nbproject\\config\\";
        confPath = confPath + etlFileName + ".conf";
        System.out.println("Checking for configuration file for " + etlFileName + ".etl");
        File confFile = new File(confPath);
        if (confFile.exists()) {
            System.out.println("Found configuration file for " + etlFileName + ".etl");
            sqlDefn = getConfigData(confFile);
            System.out.println("Parsing the content of the configuration file for " + etlFileName + ".etl");
        } else {
            System.out.println("Configuration file not found. Using Design time configurations.");
        }
        return sqlDefn;
    }

    public SQLDefinition compareAndSync(SQLDefinition srcDefn, SQLDefinition tgtDefn, boolean isSource) {
        // sync the Models.
        Iterator it = null;
        if (isSource) {
            it = srcDefn.getSourceDatabaseModels().iterator();
        } else {
            it = srcDefn.getTargetDatabaseModels().iterator();
        }
        while (it.hasNext()) {
            boolean exists = false;
            SQLDBModel match = null;
            SQLDBModel srcModel = (SQLDBModel) it.next();
            Iterator confIterator = null;
            if (isSource) {
                confIterator = tgtDefn.getSourceDatabaseModels().iterator();
            } else {
                confIterator = tgtDefn.getTargetDatabaseModels().iterator();
            }
            while (confIterator.hasNext()) {
                SQLDBModel tgtModel = (SQLDBModel) confIterator.next();
                if (tgtModel.getModelName().equals(srcModel.getModelName())) {
                    exists = true;
                    match = tgtModel;
                    break;
                }
            }
            if (exists) {
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
        while (it.hasNext()) {
            boolean exists = false;
            SQLDBTable srcTbl = (SQLDBTable) it.next();
            Iterator confIterator = tgtModel.getTables().iterator();
            while (confIterator.hasNext()) {
                SQLDBTable tgtTbl = (SQLDBTable) confIterator.next();
                if (tgtTbl.getName().equals(srcTbl.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
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
        if (rootNode != null) {
            org.w3c.dom.Node sqlNode = rootNode.getFirstChild();
            try {
                sqlDefn = new SQLDefinitionImpl((Element) sqlNode);
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
            initMetaData(iterator);
        }

        List trgDbmodels = def.getTargetDatabaseModels();
        iterator = trgDbmodels.iterator();
        while (iterator.hasNext()) {
            initMetaData(iterator);
        }

        //System.out.println(connDefs);
        //System.out.println(dbNamePoolNameMap);
        //System.out.println(internalDBConfigParams);
        connDefs.size();
        dbNamePoolNameMap.size();
    }

    /**
     * @param iterator
     * @param string
     */
    @SuppressWarnings(value = "unchecked")
    private void initMetaData(Iterator iterator) {

        SQLDBModel element = (SQLDBModel) iterator.next();
        String oid = getSQDBModelOid(element);
        if (oid == null) {
            // support older version of DBModel
            return;
        }
        SQLDBConnectionDefinition originalConndef = (SQLDBConnectionDefinition) element
                .getConnectionDefinition();

        if (originalConndef.getDriverClass().equals("org.axiondb.jdbc.AxionDriver")) {
            SQLDBConnectionDefinition conndefTemplate = this.connectionDefnTemplate.getDBConnectionDefinition("STCDBADAPTER");
            SQLDBConnectionDefinition conndef = (SQLDBConnectionDefinition) conndefTemplate
                    .cloneObject();

            setConnectionParams(conndef);

            String key = originalConndef.getName();
            conndef.setName(key);
            connDefs.put(key, conndef);
            dbNamePoolNameMap.put(oid, key);
            // TODO all the parameters for InternalDBMetadata comes from collab
            InternalDBMetadata dbMetadata = new InternalDBMetadata("c:\\temp", false, key);
            internalDBConfigParams.put(oid, dbMetadata);
        } else {
            // jdbc connection
            SQLDBConnectionDefinition conndef = originalConndef;
            String key = originalConndef.getName();
            conndef.setName(key);
            connDefs.put(key, conndef);
            dbNamePoolNameMap.put(oid, key);
            // TODO all the parameters for InternalDBMetadata comes from collab
            //InternalDBMetadata dbMetadata = new InternalDBMetadata("c:\\temp", false, key);
            //internalDBConfigParams.put(oid, dbMetadata);
        }
    }

    /**
     * @param conndef
     */
    @SuppressWarnings(value = "unchecked")
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
        if (element.getAttribute("refKey") == null) {
            return null;
        }
        String oid = (String) element.getAttribute("refKey").getAttributeValue();
        return oid;
    }

    public static void main(String[] args) {
        String etlFile = args[0];

        File f = new File(etlFile);
        File buildDir = new File(args[1]);
        EngineFileGenerator g = new EngineFileGenerator();

        try {
            g.generateEngine(f, buildDir);
        } catch (Exception e) {
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
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration ? "yes" : "no");
            trans.transform(new DOMSource(node), new StreamResult(baos));
            ret = baos.toString(encoding);
            // mLogger.debug("ret: " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
