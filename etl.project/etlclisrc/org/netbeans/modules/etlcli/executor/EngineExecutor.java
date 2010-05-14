/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * The contents of this file are subject to the terms of the Common 
 * Development and Distribution License ("CDDL")(the "License"). You 
 * may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://open-dm-mi.dev.java.net/cddl.html
 * or open-dm-mi/bootstrap/legal/license.txt. See the License for the 
 * specific language governing permissions and limitations under the  
 * License.  
 *
 * When distributing the Covered Code, include this CDDL Header Notice 
 * in each file and include the License file at
 * open-dm-mi/bootstrap/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the 
 * fields enclosed by brackets [] replaced by your own identifying 
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.netbeans.modules.etlcli.executor;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLEngineContext;
import com.sun.etl.engine.ETLEngineExecEvent;
import com.sun.etl.engine.ETLEngineListener;
import com.sun.etl.engine.ETLEngineLogEvent;
import com.sun.etl.engine.impl.ETLEngineImpl;
import com.sun.etl.jdbc.DBConnectionParameters;
import com.sun.etl.utils.RuntimeAttribute;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author admin
 */
public class EngineExecutor {

    /**
     * Logger instance
     */
    private static transient final Logger mLogger = Logger.getLogger("ETLEngineInvoker");
    private static String TRGT_SCHEMA = null;
    private static String TRGT_DB_CONN = null;
    private static String TRGT_DB_LOGIN = null;
    private static String TRGT_DB_PW = null;
    public static String fs = System.getProperty("file.separator"); //File Separator
    private static String ENGINE_FILE = "C:" + fs + "ETL" + fs + "ETLEngineInvoker" + fs + "engine.xml";
    private static Connection trgtconn = null;
    private static int dbtype = 0;
    private ETLEngine engine = null;
    private ETLEngineListener listener = null;
    static LogManager logManager = null;

    static {
        System.setProperty("JAVACAPS_DATAINTEGRATOR_DISABLE_MONITOR", "true");
    }
    public EngineExecutor() {       
    }

    public void startinvoke(String[] args) {
        confiugreLogger();
        String engineFileName = "";

        if (args.length == 1) {
            ENGINE_FILE = args[0];
        } else if (args.length >= 5) {
            TRGT_SCHEMA = args[0];
            TRGT_DB_CONN = args[1];
            TRGT_DB_LOGIN = args[2];
            TRGT_DB_PW = args[3];
            ENGINE_FILE = args[4];
        }


        //Parse the etl cli propertirs
        EtlCliPropertiesBuilder cliparser = EtlCliPropertiesBuilder.getEtlCliParserInstance();

        //Trying to load user supplied drivers to classpath
        //cliparser.loadConfiguredDriversToClasspath();

        File engine_file = new File(ENGINE_FILE);

        ArrayList<String> arrFiles = new ArrayList<String>();
        try {
            BufferedReader input = new BufferedReader(new FileReader(engine_file));
            String fileName = "";
            while (fileName != null) {
                fileName = input.readLine();
                if (fileName != null && fileName.trim().length() > 0) {
                    arrFiles.add(fileName);
                }
            }
            input.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        if (args.length == 1) {
            //Execute Engine file only
            mLogger.info("Start executing ETL Engine File :: " + ENGINE_FILE);
            EngineExecutor invoker = new EngineExecutor();
            invoker.executeEngine(engine_file, cliparser);
            mLogger.info("End executing ETL Engine File :: " + ENGINE_FILE);

        } else {
        }
    }

    private void executeEngine(File enginefile, EtlCliPropertiesBuilder parser) {
        try {
            System.out.println("Setting Engine context...");
            engine = new ETLEngineImpl();
            ETLEngineContext context = new ETLEngineContext();
            System.out.println("Parsing Engine File...");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new FileInputStream(enginefile));
            engine.setContext(context);
            engine.setRunningOnAppServer(true);
            engine.parseXML(doc.getDocumentElement());
            System.out.println("Initializing engine listener...");
            listener = new ETLEngineListenerImpl();

            String collabname = getCollabNameFromEngine(engine);
            //Set JNDI Connection refernces for connection override
            setJndiNamesForConnProvider(engine, collabname, parser);
            //Override Input args into engine
            setInputArgumentOverrides(engine, collabname, parser);

            System.out.println("Transferring control to ETL Engine ...");
            engine.exec(listener);
            synchronized (listener) {
                listener.wait();
            }

        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }

    private void setInputArgumentOverrides(ETLEngine etlEngine, String etlcollabname, EtlCliPropertiesBuilder parser) {
        Map<String, RuntimeAttribute> engineAttrMap = etlEngine.getInputAttrMap();
        Map localinputArgsMap = parser.getInputArgsMap(etlcollabname);

        Iterator i = localinputArgsMap.keySet().iterator();
        while (i.hasNext()) {
            String localkey = (String) i.next();
            if (localkey != null) {
                RuntimeAttribute engineRA = engineAttrMap.get(localkey);
                String localval = (String) localinputArgsMap.get(localkey);
                if (engineRA != null && localval != null && !localval.equals("")) {
                    Object o = convertInputArgument(engineRA, localval);
                    engineRA.setAttributeValue(o);
                }
            }
        }
    }

    private void setJndiNamesForConnProvider(ETLEngine etlEngine, String etlcollabname, EtlCliPropertiesBuilder parser) {
        String PARAM_APP_DATA_ROOT = "APP_DATAROOT";
        ETLEngineContext context = etlEngine.getContext();

        List connDefs = etlEngine.getConnectionDefList();
        Iterator itr = connDefs.iterator();
        while (itr.hasNext()) {
            DBConnectionParameters conn = (DBConnectionParameters) itr.next();
            if (conn.getConnectionURL().indexOf(PARAM_APP_DATA_ROOT) != -1) {
                String url = conn.getConnectionURL();
                String newUrl = null;
                //String workingDirectory = (row == null) ? "" : (String) row.get("DBWorkingDir");
                String workingDirectory = (getCWD() + File.separator + "logs" + File.separator + "exec").replace('\\', '/');
                if (workingDirectory == null || "".equalsIgnoreCase(workingDirectory)) {
                    newUrl = url;
                } else {
                    newUrl = url.replaceAll(PARAM_APP_DATA_ROOT, workingDirectory);
                }

                conn.setConnectionURL(newUrl);
            } else {
                String connName = conn.getName();
                String jndiResName = parser.getJndiConnectionKeyOverride(etlcollabname, connName);
                try {
                    mLogger.fine("resolved jndiResName is = " + jndiResName);
                    conn.setJNDIPath(jndiResName);
                } catch (javax.management.openmbean.InvalidKeyException e) {
                    mLogger.fine("no mapping exist for connName" + connName);
                }
            }
        }
    }

    private class ETLEngineListenerImpl implements ETLEngineListener {

        public void executionPerformed(ETLEngineExecEvent event) {
            if ((event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) || (event.getStatus() == ETLEngine.STATUS_COLLAB_EXCEPTION)) {
                engine.stopETLEngine();
                Timestamp endTime = engine.getContext().getStatistics().getCollabFinishTime();
                Timestamp startTime = engine.getContext().getStatistics().getCollabStartTime();
                int rowsExtracted = 0;
                int rowsRejected = 0;
                int rowsInserted = 0;
                Iterator<String> it = engine.getContext().getStatistics().getKnownTableNames().iterator();
                System.out.println("");
                System.out.println("Tables processed:");
                int i = 1;
                while (it.hasNext()) {
                    String tblName = it.next();
                    System.out.println("");
                    System.out.println("\t" + i++ + "." + tblName);
                    System.out.println("\t\tRows Inserted: " + engine.getContext().getStatistics().getRowsInsertedCount(tblName));
                    rowsExtracted += engine.getContext().getStatistics().getRowsExtractedCount(tblName);
                    rowsRejected += engine.getContext().getStatistics().getRowsRejectedCount(tblName);
                    rowsInserted += engine.getContext().getStatistics().getRowsInsertedCount(tblName);
                }
                System.out.println("");
                System.out.println("Total Rows Inserted for " + (i - 1) + " table(s) :" + rowsInserted);
                System.out.println("");
                System.out.println("Start time:" + startTime.toString());
                System.out.println("End time:" + endTime.toString());
                System.out.println("");
                long diff = endTime.getTime() - startTime.getTime();
                float timeTaken = diff / 1000;
                System.out.println("Total time taken:" + String.valueOf(timeTaken) + " seconds");
                System.out.println("");
                System.out.println("Exiting ETL Engine...");
                synchronized (listener) {
                    listener.notifyAll();
                }
            }
        }

        public void updateOutputMessage(ETLEngineLogEvent event) {
        }
    }

    private Object convertInputArgument(RuntimeAttribute ra, String value) {
        Object result = null;

        switch (ra.getJdbcType()) {

            case Types.BOOLEAN:
                result = Boolean.valueOf(value);
                break;
            case Types.INTEGER:
                result = Integer.valueOf(value);
                break;
            case Types.DECIMAL:
                result = BigDecimal.valueOf(Double.valueOf(value).doubleValue());
                break;
            case Types.DOUBLE:
                result = Double.valueOf(value);
                break;
            case Types.FLOAT:
                result = Float.valueOf(value);
                break;
            case Types.DATE:
                result = Date.valueOf(value);
            case Types.TIME:
                result = Time.valueOf(value);
            case Types.TIMESTAMP:
                result = Timestamp.valueOf(value);
                break;
            case Types.CHAR:
            case Types.VARCHAR:
                result = value;
                break;

            default:
                result = value;

        }
        return result;
    }

    public void confiugreLogger() {
        System.out.println("Configuring the logger ...");
        InputStream ins = null;
        try {
            //LogManager logManager;
            String config = "config/logger.properties";
            File f = new File("./logs");

            if (f.exists() && f.isDirectory()) {
            } else {
                f.mkdir();
            }
            if (logManager == null) {
                logManager = LogManager.getLogManager();
                //ins = new FileInputStream(config);
                ins = this.getClass().getClassLoader().getResourceAsStream("config/logger.properties");
                logManager.readConfiguration(ins);
            }
        } catch (IOException ex) {
            mLogger.severe(ex.getLocalizedMessage());
        } catch (SecurityException ex) {
            mLogger.severe(ex.getLocalizedMessage());
        } finally {
            try {
                if (ins != null) {
                    ins.close();
                }
            } catch (IOException ex) {
                mLogger.severe(ex.getLocalizedMessage());
            }
        }
    }

    private String getCollabNameFromEngine(ETLEngine engine) {
        //FIXME . This will fail if collabname and project name will have an underscore
        String engdisplayName = engine.getDisplayName();
        if (engdisplayName != null) {
            String collabname = engdisplayName.substring(engdisplayName.indexOf("_") + 1);
            return collabname;
        }
        return null;
    }
    //Current Working dir
    private String getCWD() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException ex) {
        }
        return null;
    }

    public static void main(String[] args) {
        EngineExecutor engineinvoker = new EngineExecutor();
        engineinvoker.startinvoke(args);

    }
}
