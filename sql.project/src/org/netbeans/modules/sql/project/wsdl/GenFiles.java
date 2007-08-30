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

package org.netbeans.modules.sql.project.wsdl;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.wsdl.Definition;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GenFiles extends CallableSystemAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NAME = "Name";
	private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private String mBaseDirectoryLocation;
    private String dbURL = "";
	private String jndi_name = "";
    private static final String CONNECTION_FILE = "connectivityInfo.xml";
    private String engineFileName = "sqlse_engine.xml";
    private JFrame frame;
    DatabaseConnection dbConn = null;
    Connection conn = null;
    Project project =null;
    
    public GenFiles() {
    
    }
    
    public void setProject(Project project) {
 		this.project = project;
 	}

/*	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.listener = listener;
	}

	public Object getValue(String key) {
		return null;
	}

	public boolean isEnabled() {
		return false;
	}

	public void putValue(String key, Object value) {
		
	}*/
	
	public String getName(){
		//return NbBundle.getMessage(ActionImpl.class, "CTL_Connect");
		return "Generate WSDL...";
	}

	/*public void removePropertyChangeListener(PropertyChangeListener listener) {
		if(this.listener.equals(listener)){
		this.listener = null;
		
		
		}*/
	 /**
     * Default implementation of getHelpCtx().
     *
     * @return   The HelpCtx value
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     * Action to perform
     */
    public void performAction() {
    	
    }
    
    /**
     * Override to perform action synchronous
     */
    public boolean asynchronous() {
        return false;
    }
   	
		
	

	public void setEnabled(boolean b) {
		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			execute();
		} catch (Exception e1) {
			//e1.printStackTrace();
		}
	}
	
	
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
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setBaseDirectoryLocation(String baseDirectoryLocation) {
        mBaseDirectoryLocation = baseDirectoryLocation;
    }
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

    public void execute() throws Exception {
    	Map wsdlMap = new HashMap();
        
        try {
        	String baseDir = project.getProjectDirectory().getPath();
			String OS = System.getProperty("os.name").toLowerCase();
                if(OS.indexOf("windows") > -1){
                    //do nothing
                }else{
                    baseDir='/'+baseDir;
                }
        	mBuildDirectoryLocation = baseDir+"/build";
        	mSrcDirectoryLocation = baseDir+"/src";
        	File srcDir = new File(mSrcDirectoryLocation);
            if (!srcDir.exists()) {
                throw new Exception("Directory " + mSrcDirectoryLocation + " does not exit.");
            }
        	//String baseDir = mBaseDirectoryLocation;
            int length = baseDir.length();
            String projectName = baseDir.substring(baseDir.lastIndexOf(File.separator) + 1, baseDir.length());
            if(projectName.length() == length){
            	projectName = baseDir.substring(baseDir.lastIndexOf("/") + 1, baseDir.length());	
            }
            boolean b=(new File(mBuildDirectoryLocation+"/META-INF")).mkdirs();
            SQLEngineFileGenerator engineFileGen = new SQLEngineFileGenerator(mBuildDirectoryLocation + "/" + engineFileName, projectName);
            String srcDirPath = srcDir.getAbsolutePath();
            String[] ext = new String[]{".sql"};
            List sqlFiles = getFilesRecursively(srcDir, ext);
            readConnectionInfo(CONNECTION_FILE);
            getDatabaseConnection();
            if (conn == null) {
                throw new Exception("Unable to retrieve any database connections with the url " + dbURL + "\n Please associate a Connection for the sql file in netbeans runtime tab or if it exists, connect to the external");
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
            project.getProjectDirectory().refresh();
            JOptionPane.showMessageDialog(frame,
                    "WSDL Generated.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void readConnectionInfo(String fileName) throws Exception {
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
                            //log("Using Database URL value: " + dbURL);
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
                            //log("jndi value: " + jndi_name);
                        }
                    }
                }
            } catch (SAXException e) {
                //log(e.getLocalizedMessage());
            } catch (IOException e) {
                //log(e.getLocalizedMessage());
            } catch (ParserConfigurationException e) {
                //log(e.getLocalizedMessage());
            }
        } else {
            throw new Exception("No File with name connectivityInfo.xml was found in the src directory");
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
