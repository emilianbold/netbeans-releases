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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.anttasks;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>


import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.FileFilter;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.sql.Connection;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.netbeans.modules.sql.project.dbmodel.DBMetaData;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.wsdl.Definition;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * @author Administrator
 */
public class GenerateWSDL extends Task {

    private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private String dbURL = "";
    private String jndi_name = "";
    private static final String CONNECTION_FILE = "connectivityInfo.xml";
    private String engineFileName = null;
    private JFrame frame;
    DatabaseConnection dbConn = null;
    Connection conn = null;

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return mSrcDirectoryLocation;
    }

    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setSrcDirectoryLocation(String srcDirectoryLocation) {
        mSrcDirectoryLocation = srcDirectoryLocation;
    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getBuildDirectoryLocation() {
        return mBuildDirectoryLocation;
    }

    /**
     * @param buildDirectoryLocation The buildDirectoryLocation to set.
     */
    public void setBuildDirectoryLocation(String buildDirectoryLocation) {
        mBuildDirectoryLocation = buildDirectoryLocation;
        engineFileName = "sqlse_engine.xml";
    }


    public static List getFilesRecursively(File dir, FileFilter filter) {
        List ret = new ArrayList();
        if (!dir.isDirectory()) {
            return ret;
        }
        File[] fileNdirs = dir.listFiles(filter);
        for (int i = 0, I = fileNdirs.length; i < I; i++) {
            if (fileNdirs[i].isDirectory()) {
                ret.addAll(getFilesRecursively(fileNdirs[i], filter));
            } else {
                ret.add(fileNdirs[i]);
            }
        }
        return ret;
    }

    public static List getFilesRecursively(File dir, String[] extensions) {
        FileFilter filter = null;
        if (extensions[0].equals(".sql")) {
            filter = new ExtensionFilter(extensions);
        }
        return getFilesRecursively(dir, filter);
    }

    public void execute() throws BuildException {
    	File srcDir = new File(mSrcDirectoryLocation);
    	File bldDir = new File(mBuildDirectoryLocation+"/META-INF");
        
    	if (!srcDir.exists()) {
            throw new BuildException("Directory " + mSrcDirectoryLocation + " does not exit.");
        }
    	if (!bldDir.exists()) {
            throw new BuildException("Directory " + mBuildDirectoryLocation + " does not exit. Please generate the WSDL");
        }
        String srcDirPath = srcDir.getAbsolutePath();
        String bldDirPath = bldDir.getAbsolutePath();
        
        if (srcDir.isDirectory()) {
        	String extensions[] = new String[1];
        	extensions[0]=".wsdl";
        	FileFilter filter=new ExtensionFilter(extensions);
        	File[] fileNdirs = srcDir.listFiles(filter);
            if(fileNdirs.length<=0){
            	JOptionPane.showMessageDialog(frame,
                        "Please Generate the WSDL using \"Generate WSDL...\" option, before building the application.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        if (bldDir.isDirectory()) {
        	String extensions[] = new String[1];
        	extensions[0]=".xml";
        	FileFilter filter=new ExtensionFilter(extensions);
        	File[] fileNdirs = bldDir.listFiles(filter);
            if(fileNdirs.length<=0){
            	JOptionPane.showMessageDialog(frame,
                        "JBI.xml is not generated, please generate the WSDL.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    public void execute1() throws BuildException {
        Map wsdlMap = new HashMap();
        File srcDir = new File(mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new BuildException("Directory " + mSrcDirectoryLocation + " does not exit.");
        }
        try {
            String baseDir = this.getProject().getProperty("basedir");
            String projectName = baseDir.substring(baseDir.lastIndexOf(File.separator) + 1, baseDir.length());
            SQLEngineFileGenerator engineFileGen = new SQLEngineFileGenerator(mBuildDirectoryLocation + "/" + engineFileName, projectName);
            String srcDirPath = srcDir.getAbsolutePath();
            String[] ext = new String[]{".sql"};
            List sqlFiles = getFilesRecursively(srcDir, ext);
            readConnectionInfo(CONNECTION_FILE);
            getDatabaseConnection();
            if (conn == null) {
                throw new BuildException("Unable to retrieve any database connections with the url " + dbURL + "\n Please associate a Connection for the sql file in netbeans runtime tab or if it exists, connect to the external");
            }
            for (int i = 0, I = sqlFiles.size(); i < I; i++) {
                File f = (File) sqlFiles.get(i);
                if (f != null) {
                    engineFileGen.addSQLDefinition(f.getName(), dbConn);
                }
            }
            engineFileGen.persistEngineFile(jndi_name);
            //call generate wsdl in model.
            WSDLGenerator wsdlgen = new WSDLGenerator(conn,
                    sqlFiles,
                    projectName,
                    srcDirPath,
                    engineFileName);
            
            wsdlgen.setDBConnection(dbConn);

            Definition def = wsdlgen.generateWSDL();
            SQLMapWriter sqlw = new SQLMapWriter(sqlFiles, 
                                                def, 
                                                new File(mBuildDirectoryLocation).getCanonicalPath());
            sqlw.writeMap();
            JBIFileWriter fw = new JBIFileWriter(mBuildDirectoryLocation + "/META-INF/jbi.xml",
                    mBuildDirectoryLocation + "/sqlmap.xml",mBuildDirectoryLocation);
            fw.writeJBI();

        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }
    }

    private void readConnectionInfo(String fileName) throws FileNotFoundException {
        if (fileName.endsWith(".xml")) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                Document doc = factory.newDocumentBuilder().parse(new File(mSrcDirectoryLocation, CONNECTION_FILE));
                NodeList nl = doc.getDocumentElement().getElementsByTagName("database-url");
                if (nl != null) {
                    Node n = nl.item(0);
                    if (n != null) {
                        Node n2 = n.getAttributes().getNamedItem("value");
                        if (n2 != null) {
                            dbURL = n2.getNodeValue();
                            log("Using Database URL value: " + dbURL);
                        }
                    }
                }
				NodeList n3 = doc.getDocumentElement().getElementsByTagName("jndi-name");
				if (n3 != null) {
                    Node n = n3.item(0);
                    if (n != null) {
                        Node n4 = n.getAttributes().getNamedItem("value");
                        if (n4 != null) {
                            jndi_name = n4.getNodeValue();
                            log("jndi value: " + jndi_name);
                        }
                    }
                }
            } catch (SAXException e) {
                log(e.getLocalizedMessage());
            } catch (IOException e) {
                log(e.getLocalizedMessage());
            } catch (ParserConfigurationException e) {
                log(e.getLocalizedMessage());
            }
        } else {
            throw new BuildException("No File with name connectivityInfo.xml was found in the src directory");
        }
    }

    private void getDatabaseConnection() {
        DatabaseConnection[] dbConnections = ConnectionManager.getDefault().getConnections();
        if (dbConn == null) {
            for (int j = 0; j < dbConnections.length; j++) {
                if (dbConnections[j].getDatabaseURL().equalsIgnoreCase(dbURL)) {
                    dbConn = dbConnections[j];
                    conn = dbConn.getJDBCConnection();
                    break;
                }
            }
        }
        if ((dbConn != null) && (conn == null)) {
            ConnectionManager.getDefault().showConnectionDialog(dbConn);
            conn = dbConn.getJDBCConnection();
        }
    }

}
