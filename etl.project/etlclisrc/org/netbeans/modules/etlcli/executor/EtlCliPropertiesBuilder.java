/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlcli.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 *
 * @author Manish Bharani
 */
public class EtlCliPropertiesBuilder {

    private String fileloc = "./ETLProcess"; //Properties ETL CLI
    private String CLICONFIG = "etlcli.properties"; //Properties GLobal Connection
    private String CONNCONFIG = "globalconnection.properties";    //Properties cliconfig_prop = new Properties();    //Properties CONN
    private ArrayList clifilearr = new ArrayList();
    private ArrayList cliconnarr = new ArrayList();
    private HashMap<String, HashMap> connmap = new HashMap<String, HashMap>();
    private HashMap<String, HashMap> clipropmap = new HashMap<String, HashMap>();    //Tags
    private HashMap<String, String> JndiRefKeyMap = new HashMap<String, String>();
    private static final String CONNS = "conns";
    private static final String INPUTCOLS = "inputcols";
    private static EtlCliPropertiesBuilder parser = null;
    //Logger
    private static transient final Logger mLogger = Logger.getLogger(EtlCliPropertiesBuilder.class.getName());

    private EtlCliPropertiesBuilder() {
        mLogger.info("Building ETL Command Line Interface Properties ...");
    }

    public static EtlCliPropertiesBuilder getEtlCliParserInstance() {
        if (parser == null) {
            parser = new EtlCliPropertiesBuilder();
            return parser.parse();
        }
        return parser;
    }

    public EtlCliPropertiesBuilder parse() {
        loadProperties();
        createConnectionModel();
        createEtlCliModel();
        return this;
    }

    private void createConnectionModel() {
        for (int i = 0; i < cliconnarr.size(); i++) {
            String line = (String) cliconnarr.get(i);
            if (line.startsWith("GLOBALCONNECTION")) {
                int keyindex = line.indexOf(".");
                int propindex = line.indexOf("=");
                String conntag = line.substring(0, keyindex);
                String connparam = line.substring((keyindex + 1), propindex);
                if (!connmap.containsKey(conntag)) {
                    connmap.put(conntag, new HashMap<String, String>());
                }
                ((HashMap) connmap.get(conntag)).put(connparam, line.substring(propindex + 1));
            }
        }
    }

    private void createEtlCliModel() {
        for (int i = 0; i < clifilearr.size(); i++) {
            String line = (String) clifilearr.get(i);
            if (line.startsWith("Collab")) {
                int collabname_startindex = line.indexOf(".");
                int collabname_endindex = line.indexOf("-");
                String collabname = line.substring(collabname_startindex + 1, collabname_endindex);
                String otherprops_part = line.substring(collabname_endindex + 1);
                if (!clipropmap.containsKey(collabname)) {
                    HashMap propmap = new HashMap<String, List>();
                    propmap.put(CONNS, new ArrayList<PropNVpairs>());
                    propmap.put(INPUTCOLS, new ArrayList<PropNVpairs>());
                    clipropmap.put(collabname, propmap);
                }
                HashMap propmap = (HashMap) clipropmap.get(collabname);
                if ((otherprops_part.indexOf("SourceConnection") != -1) || (otherprops_part.indexOf("TargetConnection") != -1)) {
                    //Profile Connections Property
                    int conndel = otherprops_part.indexOf("=");
                    String connname = otherprops_part.substring(0, conndel);
                    String connval = otherprops_part.substring(conndel + 1);
                    JndiRefKeyMap.put(collabname + "-" + connname, connval);
                    ((List) propmap.get(CONNS)).add(new PropNVpairs(connname, connval));
                } else{
                    //Profile Input Col Propery
                    int inputcoldel = otherprops_part.indexOf("=");
                    String icname = otherprops_part.substring(0, inputcoldel);
                    String icval = otherprops_part.substring(inputcoldel + 1);
                    ((List) propmap.get(INPUTCOLS)).add(new PropNVpairs(icname, icval));
                }
            }
        }
    }

    private void loadProperties() {

        //Load Connections Propertirs
        BufferedReader cliconnin = null;
        try {
            cliconnin = new BufferedReader(new FileReader(new File(fileloc + File.separator + CONNCONFIG)));
            if (!cliconnin.ready()) {
                throw new IOException();
            }

            String line;
            while ((line = cliconnin.readLine()) != null) {
                cliconnarr.add(line);
            }
        } catch (IOException ex) {
            mLogger.severe("IO Exception : " + ex);
        } finally {
            try {
                cliconnin.close();
            } catch (IOException ex) {
                mLogger.severe("IO Exception : " + ex);
            }
        }

        //Load Etl Cli Properties
        BufferedReader cliin = null;
        try {
            cliin = new BufferedReader(new FileReader(new File(fileloc + File.separator + CLICONFIG)));
            if (!cliin.ready()) {
                throw new IOException();
            }
            String line;

            while ((line = cliin.readLine()) != null) {
                clifilearr.add(line);
            }
        } catch (IOException ex) {
            mLogger.severe("IO Exception : " + ex);
        } finally {
            try {
                cliin.close();
            } catch (IOException ex) {
                mLogger.severe("IO Exception : " + ex);
            }
        }
    }

    /*
    private void addDriversToClasspath(String jdbcdrivers) {
        StringTokenizer driverjars = new StringTokenizer(jdbcdrivers, ",");
        while (driverjars.hasMoreElements()) {
            String driverjarpath = (String) driverjars.nextElement();
            if (driverjarpath != null) {
                if ((driverjarpath.indexOf("<") != -1) || (driverjarpath.indexOf("<") != -1)) {
                    // User has not changed the default strings in the globalconnection.properties
                    mLogger.severe("Unable to load db driver supplied by the user: " + driverjarpath);
                } else {
                    // Represent driver jar path in unix style
                    driverjarpath = driverjarpath.replace("\\", "/");
                    // Try to load the driver
                    File f = new File(driverjarpath);
                    if (f.exists()) {
                        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                        Class sysclass = URLClassLoader.class;
                        try {
                            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
                            method.setAccessible(true);
                            method.invoke(sysloader, new Object[]{f.toURL()});
                            mLogger.info("Driver Loaded Successfully : " + driverjarpath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mLogger.severe("Driver jar not found on path : " + driverjarpath);
                        break;
                    }
                }
            }
        }
    }
    */
    
    class PropNVpairs {

        private String name = null;
        private String value = null;

        PropNVpairs(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getPropName() {
            return this.name;
        }

        public String getPropValue() {
            return this.value;
        }
    }

    /* ****************  Public Methods for the Modelled Data ************* */
    public Connection getCollabConnections(String connectioncode) {
        if (connectioncode != null) {
            if (connectioncode.length() > 0) {
                HashMap connparams = connmap.get(connectioncode);
                if (connparams != null) {
                    //String drvlist = (String) connparams.get("DRIVERS");
                    String drvclass = (String) connparams.get("DRIVERCLASS");
                    String url = (String) connparams.get("URL");
                    String login = (String) connparams.get("USERNAME");
                    String pw = (String) connparams.get("PASSWD");

                    try {
                        Class.forName(drvclass);
                        Connection conn = DriverManager.getConnection(url, login, pw);
                        return conn;
                    } catch (SQLException ex) {
                        mLogger.severe("SQL Exception. Error : " + ex.getMessage());
                    } catch (ClassNotFoundException ex) {
                        //mLogger.severe("[ERROR]\nClass Not found [" + drvclass + "].\n Provide abs path for  [" + drvlist + "]  driver jar(s) in globalconnection.properties. \n[Error] Unable to locate in execution path : " + ex.getMessage());
                        mLogger.severe("[ERROR]\nClass Not found [" + drvclass + "].\n[Error] Unable to locate in execution path : " + ex.getMessage());
                    }
                } else {
                    mLogger.severe("Unable to retrieve connection params for jndi ref : " + connectioncode + " " + connmap.toString());
                }
            }
        } else {
            mLogger.warning("JNDI Ref being resolved with etl cli provider is null");
        }
        return null;
    }

    public String getJndiConnectionKeyOverride(String collabname, String connname) {
        String globalconnkey = JndiRefKeyMap.get(collabname + "-" + connname);
        return globalconnkey;
    }

    public Map getInputArgsMap(String collabname) {
        Map<String, String> inputcolmap = new HashMap<String, String>();
        HashMap collabprops = (HashMap) clipropmap.get(collabname);
        if (collabprops != null) {
            List<PropNVpairs> icollist = (List) collabprops.get(INPUTCOLS);
            for (PropNVpairs elm : icollist) {
                inputcolmap.put(elm.getPropName(), elm.getPropValue());
            }
        }
        return inputcolmap;
    }

    /*
    public void loadConfiguredDriversToClasspath() {
        mLogger.info("Trying to load user supplied db drivers ... ");
        Iterator connref = connmap.values().iterator();
        while (connref.hasNext()) {
            String drvlist = (String) ((HashMap) connref.next()).get("DRIVERS");
            addDriversToClasspath(drvlist);
        }
    }
    */
}
