/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mashup.db.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.common.utils.MigrationUtils;
import org.openide.nodes.BeanNode;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class AxionDBConfiguration {

    private static final AxionDBConfiguration DEFAULT = new AxionDBConfiguration();
    public static final String PROP_DB_LOC = "DBLocation";
    public static final String PROP_DRIVER_LOC = "DriverLocation";
    public static final String PROP_LOC = "location";
    public static final String PROP_DRV_LOC = "driver";
    private static transient final Logger mLogger = Logger.getLogger(FlatfileResulSetPanel.class.getName());
    //private static transient final Localizer mLoc = Localizer.get();
    public static AxionDBConfiguration getDefault() {
        return DEFAULT;
    }

    public String displayName() {
        /*String nbBundle4 = mLoc.t("BUND253: MashupDB Configuration");
        return nbBundle4.substring(15);*/
        return "MashupDB Configuration";
    }

    protected final String putProperty(String key, String value, boolean notify) {
        System.setProperty(key, value);
        return System.getProperty(key);
    }

    protected final String getProperty(String key) {
        return System.getProperty(key);
    }

    public String getDriver() {
        File conf = getConfigFile();
        if (conf != null) {
            Properties prop = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(conf);
                prop.load(in);
            } catch (FileNotFoundException ex) {
                //ignore
            } catch (IOException ioEx) {
                //ignore
            }
            String drv = prop.getProperty(PROP_DRIVER_LOC);
            if (drv != null) {
                return drv;
            }
        } else {
            // Check for mashup driver under soa cluster.
            String nbHomeDir = System.getProperty("netbeans.home") + File.separator +
                    ".." + File.separator + "soa2" + File.separator + "modules" + File.separator + "ext" + File.separator + "etl" + File.separator + "axiondb.jar";
            File driver = new File(nbHomeDir);
            if (!driver.exists()) {

                // check for mashup driver under extra cluster.
                nbHomeDir = System.getProperty("netbeans.home") + File.separator + ".." + File.separator + "extra" + File.separator + "modules" + File.separator + "ext" + File.separator + "etl" + File.separator + "axiondb.jar";
                driver = new File(nbHomeDir);
                if (!driver.exists()) {
                    nbHomeDir = "";
                }
                //java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info("***** AxionDBConfiguration Axiond driver location "+nbHomeDir);
                return nbHomeDir;
            }
        }
        return "";
    }

    public void setDriver(String driver) {
        driver = driver.trim();
        File conf = getConfigFile();
        try {
            FileInputStream in = new FileInputStream(conf);
            Properties oldProp = new Properties();
            oldProp.load(in);
            String dbLoc = oldProp.getProperty(PROP_DB_LOC);
            in.close();
            FileOutputStream out = new FileOutputStream(conf);
            Properties prop = new Properties();
            prop.setProperty(PROP_DRIVER_LOC, driver);
            prop.setProperty(PROP_DB_LOC, dbLoc);
            String nbBundle3 = "MashupDB Configurations";//mLoc.t("BUND254: MashupDB Configurations");
            prop.store(out, nbBundle3);//nbBundle3.substring(15));
            out.close();
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }
    }

    /**
     * Returns the AXION location or an empty string if the AXION location
     * is not set. Never returns null.
     * @return dbLocation
     */
    public String getLocation() {
        File conf = getConfigFile();
        if (conf != null) {
            Properties prop = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(conf);
                prop.load(in);
            } catch (FileNotFoundException ex) {
                //ignore
            } catch (IOException ioEx) {
                //ignore
            }
            return prop.getProperty(PROP_DB_LOC);
        }
        return System.getProperty("netbeans.user") + File.separator + "MashupDatabases" + File.separator;
    }

    /**
     * Sets the AXION location.
     *
     * @param location the AXION location. A null value is valid and
     *        will be returned by getLocation() as an empty
     *        string (meaning "not set"). An empty string is valid
     *        and has the meaning "set to the default location".
     */
    public void setLocation(String location) {
        location = location.trim();
        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }
        File conf = getConfigFile();
        try {
            FileInputStream in = new FileInputStream(conf);
            Properties oldProp = new Properties();
            oldProp.load(in);
            String drv = oldProp.getProperty(PROP_DRIVER_LOC);
            in.close();
            FileOutputStream out = new FileOutputStream(conf);
            Properties prop = new Properties();
            prop.setProperty(PROP_DB_LOC, location);
            prop.setProperty(PROP_DRIVER_LOC, drv);
            String nbBundle3 = "MashupDB Configurations";//mLoc.t("BUND254: MashupDB Configurations");
            prop.store(out, nbBundle3);//nbBundle3.substring(15));
            out.close();
            File db = new File(location);
            if (!db.exists()) {
                db.mkdir();
            }
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }
    }

    public static File getConfigFile() {
        String nbUsrDir = null;
        String netbeansHome = null;
        if (MigrationUtils.isCmdLine) {
            nbUsrDir = System.getProperty("user.dir") + File.separator + ".." + File.separator + "usrdir";
            netbeansHome = System.getProperty("user.dir") + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "netbeans" + File.separator + "bin";
        } else {
            nbUsrDir = System.getProperty("netbeans.user");
            netbeansHome = System.getProperty("netbeans.home");
        }
        if( (nbUsrDir.length() == 0) || (netbeansHome.length() == 0) ) {
            java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info("netbeans.user or netbeans.home properties not set.");
            return null;
        }
        // Check for mashup driver under soa cluster.
        String nbHomeDir = netbeansHome + File.separator +
                ".." + File.separator + "soa2" + File.separator + "modules" + File.separator + "ext" + File.separator + "etl" + File.separator + "axiondb.jar";
        File driver = new File(nbHomeDir);
        if (!driver.exists()) {
            // check for mashup driver under extra cluster.
            nbHomeDir = netbeansHome + File.separator + ".." + File.separator + "extra" + File.separator + "modules" + File.separator + "ext" + File.separator + "etl" + File.separator + "axiondb.jar";
            driver = new File(nbHomeDir);
            if (!driver.exists()) {
                nbHomeDir = "";
            }/* else {
                nbHomeDir = driver.getAbsolutePath();
           }*/
        }
        nbHomeDir = driver.getAbsolutePath();        
        String DEFAULT_DB_LOCATION = nbUsrDir + File.separator + "MashupDatabases" + File.separator;        
        nbUsrDir = nbUsrDir + File.separator + "config" + File.separator + "Databases" + File.separator + "MashupDB";
        File conf = new File(nbUsrDir);
        //java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info("**************** conf.exists() =  " + conf.exists());
        if (!conf.exists()) {
            conf.mkdirs();
        }
        nbUsrDir = nbUsrDir + File.separator + "MashupDBConfig.properties";
        conf = new File(nbUsrDir);
        if (!conf.exists()) {
            try {
                conf.createNewFile();
                Properties prop = new Properties();
                prop.setProperty(PROP_DB_LOC, DEFAULT_DB_LOCATION);
                prop.setProperty(PROP_DRIVER_LOC, nbHomeDir);
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(conf);
                    String nbBundle2 = "Mashup Database Location";//mLoc.t("BUND255: Mashup Database Location");

                    prop.store(out, nbBundle2);//nbBundle2.substring(15));

                    out.close();
                } catch (FileNotFoundException ex) {
                   java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info("****************FileNotFoundException =  " + ex.getMessage());
                } catch (IOException ioEx) {
                   java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info("****************IOException =  " + ioEx.getMessage());
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info("****************IOException =  " + ex.getMessage());
                conf = null;
            }
        }
        return conf;
    }

    protected static BeanNode createViewNode() throws java.beans.IntrospectionException {
        BeanNode nd = new BeanNode(AxionDBConfiguration.getDefault());
        String nbBundle1 = "Mashup Database";//mLoc.t("BUND256: Mashup Database");
        nd.setName(nbBundle1);//nbBundle1.substring(15));
        return nd;
    }
}
