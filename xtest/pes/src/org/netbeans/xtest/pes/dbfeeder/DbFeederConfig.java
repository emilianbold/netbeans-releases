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

/*
 * DbFeederConfig.java
 *
 * Created on September 30, 2002, 2:36 PM
 */

package org.netbeans.xtest.pes.dbfeeder;

import org.netbeans.xtest.xmlserializer.*;
import java.sql.*;
import java.io.*;
import org.netbeans.xtest.pes.*;
import org.netbeans.xtest.pe.*;
import org.netbeans.xtest.util.SerializeDOM;
import java.util.logging.Level;
import org.w3c.dom.Document;

/**
 *
 * @author  mb115822
 */
public class DbFeederConfig implements XMLSerializable {

    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(DbFeederConfig.class);
    static {
        try {
            // load global registry
            GlobalMappingRegistry.registerClassForElementName("PESdbFeederConfig", DbFeederConfig.class);
            // register this class
            classMappingRegistry.registerSimpleField("loggingLevel",ClassMappingRegistry.ATTRIBUTE,"loggingLevel");
            classMappingRegistry.registerSimpleField("deleteAge",ClassMappingRegistry.ATTRIBUTE,"deleteAge");
            classMappingRegistry.registerSimpleField("workDirs",ClassMappingRegistry.ELEMENT,"workdirs");
            classMappingRegistry.registerSimpleField("mail",ClassMappingRegistry.ELEMENT,"mail");
            classMappingRegistry.registerSimpleField("databaseConnection",ClassMappingRegistry.ELEMENT,"database");
        } catch (MappingException me) {
            me.printStackTrace();
            classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }        
    
    // empty constructor - required by XMLSerializer
    public DbFeederConfig() {}
    

    private String loggingLevel;
    
    public Level getLoggingLevel() {
        return Level.parse(loggingLevel);
    }  
    
    private int deleteAge;
    
    /** Returns deleteAge attribute from db-feeder config. It says how many
     * builds should be kept in database. Builds beyond deleteAge threshold
     * will be deleted.
     */
    public int getDeleteAge() {
        if(deleteAge == 0) {
            // if deleteAge not defined set it to max to not delete anything
            deleteAge = Integer.MAX_VALUE;
        }
        return deleteAge;
    }
    
    private WorkingDirectories workDirs;
    
    public WorkingDirectories getWorkDirs() {
        return workDirs;
    }

    
    
    private DatabaseConnection databaseConnection;
    
    public Connection getDatabaseConnection() throws SQLException {
        return databaseConnection.getDatabaseConnection();
    }    
    
    private Mail mail;
    
    public PESMailer getPESMailer() {
        return mail.getPESMailer();
    }
    
    public static DbFeederConfig loadCondfig(File configFile) throws XMLSerializeException {        
        if (!configFile.isFile()) {
            throw new XMLSerializeException("Cannot load config from file "+configFile.getPath()+", file does not exist");
        }
        try {
            Document doc = SerializeDOM.parseFile(configFile);
            XMLSerializable xmlObject = XMLSerializer.getXMLSerializable(doc);
            if (xmlObject instanceof DbFeederConfig) {
                DbFeederConfig config = (DbFeederConfig)xmlObject;
                return config;
            }
        } catch (IOException ioe) {
            throw new XMLSerializeException("IOException caught when loading config file:"+configFile.getPath(),ioe);
        }
        // xmlobject is not of required type
        throw new XMLSerializeException("Loaded xml document is not PESdbFeederConfig");
    }
    
    public void setLoggingLevels() {
        PESLogger.setConsoleLoggingLevel(getLoggingLevel());
        mail.registerLogHandler();
    }

   
    
    // public inner classes    
    
    // mail class
    public static class Mail implements XMLSerializable {
        
        private String smtpHost = "localhost";
        private String from;
        private String to;
        private String loggingLevel = "SEVERE";
        
        
        
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(DbFeederConfig.Mail.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("smtpHost",ClassMappingRegistry.ELEMENT,"smtpHost");
                classMappingRegistry.registerSimpleField("from",ClassMappingRegistry.ELEMENT,"from");
                classMappingRegistry.registerSimpleField("to",ClassMappingRegistry.ELEMENT,"to");
                classMappingRegistry.registerSimpleField("loggingLevel",ClassMappingRegistry.ATTRIBUTE,"loggingLevel");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        
        private PESMailer mailer;
        
        public PESMailer getPESMailer() {
            if (mailer == null) {
                mailer  = new PESMailer();
                mailer.setSMTPHost(smtpHost);
                mailer.setFromAddress(from);
                mailer.setToAddress(to);
            }
            return mailer;
        }
        
        public boolean registerLogHandler() {
            return PESLogger.addEmailLogger(loggingLevel, getPESMailer());
        }
        
    }
    
    
    // database connection class
    public static class DatabaseConnection implements XMLSerializable {
        
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(DbFeederConfig.DatabaseConnection.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("jdbcUsername",ClassMappingRegistry.ELEMENT,"username");
                classMappingRegistry.registerSimpleField("jdbcPassword",ClassMappingRegistry.ELEMENT,"password");
                classMappingRegistry.registerSimpleField("jdbcDriverClass",ClassMappingRegistry.ELEMENT,"driverClass");
                classMappingRegistry.registerSimpleField("jdbcURL",ClassMappingRegistry.ELEMENT,"URL");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        
        private String jdbcUsername;
        private String jdbcPassword;
        private String jdbcDriverClass;
        private String jdbcURL;
        
        public Connection dbConnection = null;
        
        public Connection getDatabaseConnection() throws SQLException {
            if (dbConnection == null) {
                try {
                    Class.forName(jdbcDriverClass);
                } catch (ClassNotFoundException cnfe) {
                    throw new SQLException("Cannot find class "+jdbcDriverClass+" cauught ClassNotFoundException:"+cnfe.getMessage());
                }
                dbConnection = DriverManager.getConnection(jdbcURL,jdbcUsername,jdbcPassword);
                dbConnection.setAutoCommit(false);
            } else if (dbConnection.isClosed()) {
                dbConnection =  DriverManager.getConnection(jdbcURL,jdbcUsername,jdbcPassword);
                dbConnection.setAutoCommit(false);                
            }
            return dbConnection;
        }
    }
    
    
    // working directories class
     public static class WorkingDirectories implements XMLSerializable {
        
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(DbFeederConfig.WorkingDirectories.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("incoming",ClassMappingRegistry.ELEMENT,"incoming");
                classMappingRegistry.registerSimpleField("invalid",ClassMappingRegistry.ELEMENT,"invalid");
                classMappingRegistry.registerSimpleField("work",ClassMappingRegistry.ELEMENT,"work");
                classMappingRegistry.registerSimpleField("crash",ClassMappingRegistry.ELEMENT,"crash");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        
        // fields are not 
        private String incoming;
        private String work;
        private String invalid;
        private String crash;  
    
        public File getWork() throws IOException {
            return createOrGetDirectory(work, "work");
        }
        
        public File getIncoming() throws IOException {
            return createOrGetDirectory(incoming, "incoming");
        }
        
        public File getInvalid() throws IOException {
            return createOrGetDirectory(invalid, "invalid ");
        }
        
        public File getCrash() throws IOException {
            return createOrGetDirectory(crash, "crash");
        }
        
        
        private static File createOrGetDirectory(String dirName, String dirLabel) throws IOException {
            if (dirName == null) {
                throw new IOException("Cannot create directory "+dirLabel+", directory location is not specified ");
            }
            File dirFile = new File(dirName);
            if (!dirFile.exists()) {
                boolean result = dirFile.mkdirs();
                if (result == true) {
                    return dirFile;
                } else {
                    throw new IOException("Cannot create "+dirLabel+" dir at: "+dirName);
                }
            }
            if (!dirFile.isDirectory()) {
                throw new IOException(dirLabel+" dir is not a directory"+dirName);
            }
            if (!dirFile.canWrite()) {
                throw new IOException(dirLabel+" dir is not a writable directory"+dirName);
            }
            // return it
            return dirFile;
        }
     }
}
