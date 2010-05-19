/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.etl.project.anttasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.openide.util.Exceptions;

/**
 * This Class generates artifacts for command line override infreastructure
 * @author Manish Bharani
 */
public class GenerateEltCLI {

    File etlcliprop = null;
    File connprop = null;
    StringBuilder globalsb = null;
    String lns = System.getProperty("line.separator");
    //Connection Profile
    HashMap<String, DBConnectionDefinition> connprofile = new HashMap();
    HashMap<String, String> conntag = new HashMap();
    // File Handling
    BufferedWriter conn_fwriter = null;
    BufferedWriter etlcli_fwriter = null;
    //Counters
    int collabcount = 0;
    int conncount = 0;    //Delete and recreate cli build dir
    boolean initEtlcliDir = false;

    public GenerateEltCLI() {
        globalsb = new StringBuilder();
    }

    public GenerateEltCLI(boolean initEtlcliDir) {
        this();
        this.initEtlcliDir = initEtlcliDir;
    }

    private boolean setupDir(File builddir) {
        File clidir = new File(builddir.getParent() + File.separator + "ETLProcess");

        if (clidir.exists()) {
            boolean status = deleteDirectory(clidir); //Clean up dir and recreate the files
            if (status) {
                System.out.println("Cleaned up properties from previous session ...");
            }
        }
        boolean created = clidir.mkdir();
        if (!created) {
            System.out.println("Unable to create dir [" + clidir.getAbsolutePath() + "]");
            return false;
        }

        if (createArtifactFiles(clidir.getAbsolutePath())) {
            initFileAppenders(etlcliprop, connprop);
            return true;
        }
        return false;
    }

    public void processETLDef(String defname, File builddir, SQLDefinition sqldef) {
        String lineprifix = "Collab" + Integer.toString(collabcount++) + "." + defname + "-";

        //Set Up etlcli build dir if needed
        boolean canproceed = true;
        if (this.initEtlcliDir) {
            if (canproceed = setupDir(builddir)) {
                this.initEtlcliDir = false;
            }
        }

        if (canproceed) {
            printcomment("ETL-CLI Properties for Collab [ " + defname + " ]", etlcli_fwriter);

            //Profile Connections First
            List<SQLDBModel> dbmodels = sqldef.getAllDatabases();

            //Handle Model Connections
            for (SQLDBModel dbmodel : dbmodels) {
                DBConnectionDefinition conndef = dbmodel.getConnectionDefinition();
                String url = updateProfiledConnections(conndef);
                if (url != null){
                    globalsb.append(lineprifix + conndef.getName() + "=" + conntag.get(url) + lns);
                }
            }

            //Runtime Input DB Columns
            if (sqldef.getRuntimeDbModel() != null) {
                if (sqldef.getRuntimeDbModel().getRuntimeInput() != null) {

                    //Runtime input columns
                    List<DBColumn> inputdbcols = sqldef.getRuntimeDbModel().getRuntimeInput().getColumnList();
                    if (inputdbcols != null) {
                        for (DBColumn dbcol : inputdbcols) {
                            globalsb.append(lineprifix + dbcol.getName() + "=" + dbcol.getDefaultValue() + lns);
                        }
                    }
                }
            }

            //Write to File
            appendToEtlCliFile(globalsb.toString() + lns);
            globalsb.delete(0, globalsb.length()); // Init String Builder
        }

    }

    private String updateProfiledConnections(DBConnectionDefinition conndef) {
        String url = conndef.getConnectionURL();
        /* Axion Database connections (i.e file db connections) do not make it to config files */
        if (url != null) {
            if (url.indexOf("axiondb") == -1) {
                if (!connprofile.containsKey(url)) {
                    connprofile.put(url, conndef);
                    conntag.put(url, ("GLOBALCONNECTION" + Integer.toString(conncount++)));
                    return url;
                }else{
                    return url;
                }
            }
        }
        return null;
    }

    private void writeConnectionTemplete() {
        printcomment("Modify this file with Caution", conn_fwriter);
        Iterator i = connprofile.keySet().iterator();
        while (i.hasNext()) {
            String url = (String) i.next();
            DBConnectionDefinition conndef = (DBConnectionDefinition) connprofile.get(url);
            String connname = conntag.get(url);
            //globalsb.append(connname + "." + "DRIVERS=<Driver1.jar>,<Driver2.jar>" + lns);
            globalsb.append(connname + "." + "DRIVERCLASS=" + conndef.getDriverClass() + lns);
            globalsb.append(connname + "." + "DBTYPE=" + conndef.getDBType() + lns);
            globalsb.append(connname + "." + "URL=" + url + lns);
            globalsb.append(connname + "." + "USERNAME=" + conndef.getUserName() + lns);
            globalsb.append(connname + "." + "PASSWD=" + conndef.getPassword() + lns);
            globalsb.append(lns);

            appendToConnFile(globalsb.toString());
            globalsb.delete(0, globalsb.length()); // Init String Builder
        }
    }

    private boolean createArtifactFiles(String etlcliDir) {
        //Generate Collaboration commandline properties
        boolean filestat1 = false, filestat2 = false;
        etlcliprop = new File(etlcliDir + File.separator + "etlcli.properties");
        if (etlcliprop.exists()) {
            etlcliprop.delete();
            System.out.println("Deleted file : " + etlcliprop.getAbsolutePath());
        }
        try {
            etlcliprop.createNewFile();
            System.out.println("Created file : " + etlcliprop.getAbsolutePath());
            filestat1 = true;
        } catch (IOException ex) {
            System.out.println("Failed to create file : " + etlcliprop.getAbsolutePath() + ". Error : " + ex.getMessage());
        }


        //Generate Connection templete
        connprop = new File(etlcliDir + File.separator + "globalconnection.properties");
        if (connprop.exists()) {
            connprop.delete();
            System.out.println("Deleted file : " + connprop.getAbsolutePath());
        }
        try {
            connprop.createNewFile();
            System.out.println("Created file : " + connprop.getAbsolutePath());
            filestat2 = true;
        } catch (IOException ex) {
            System.out.println("Failed to create file : " + connprop.getAbsolutePath() + ". Error : " + ex.getMessage());
        }
        return (filestat1 && filestat2);
    }

    private void initFileAppenders(File etlclifile, File connfile) {
        try {
            etlcli_fwriter = new BufferedWriter(new FileWriter(etlclifile, true));
            conn_fwriter = new BufferedWriter(new FileWriter(connfile, true));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void appendToEtlCliFile(String text) {
        try {
            etlcli_fwriter.write(text);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void appendToConnFile(String text) {
        try {
            conn_fwriter.write(text);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void printcomment(String commenttext, BufferedWriter br) {
        try {
            if (br != null) {
                br.write("~*~*~*~ " + commenttext + " ~*~*~*~" + lns);
            }
        } catch (IOException ex) {
            System.out.println("[Error] Unable to write to ETL Command Line Overrides file. Error:" + ex.getMessage());
        }
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public void cleanup() {
        //Print connection Templete
        writeConnectionTemplete();

        try {
            if (etlcli_fwriter != null) {
                etlcli_fwriter.close();
            }
            if (conn_fwriter != null) {
                conn_fwriter.close();
            }
        } catch (IOException ex) {
            System.out.println("[Error] Unable to close file writers. Message :" + ex.getMessage());
        }
    }
}
