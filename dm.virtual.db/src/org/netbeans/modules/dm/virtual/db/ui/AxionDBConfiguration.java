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
package org.netbeans.modules.dm.virtual.db.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
    private static final String FS = File.separator;

    public static AxionDBConfiguration getDefault() {
        return DEFAULT;
    }

    public String displayName() {
        return NbBundle.getMessage(AxionDBConfiguration.class, "LBL_AxionConf");
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
            // Check for virtual db driver under soa cluster.
            String netbeansHome = getNetbeansHome();
            String soa = getClusters(new File(netbeansHome), "soa");
            String nbHomeDir = netbeansHome +
                    FS + soa + FS + "modules" + FS + "ext" + FS + "dm" + FS + "virtual" + FS + "db" + FS + "axiondb.jar";
            File driver = new File(nbHomeDir);
            if (!driver.exists()) {
                String extra = getClusters(new File(netbeansHome), "extra");
                // check for virtual db driver under extra cluster.
                nbHomeDir = netbeansHome + FS + extra + FS + "modules" + FS + "ext" + FS + "dm" + FS + "virtual" + FS + "db" + FS + "axiondb.jar";
                driver = new File(nbHomeDir);
                if (!driver.exists()) {
                    nbHomeDir = "";
                }
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
            prop.store(out, NbBundle.getMessage(AxionDBConfiguration.class, "LBL_AxionConfs"));
            out.close();
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }
    }

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
        return System.getProperty("netbeans.user") + FS + "VirtualDatabases" + FS;
    }

    public void setLocation(String location) {
        location = location.trim();
        if (!location.endsWith(FS)) {
            location = location + FS;
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
            prop.store(out, NbBundle.getMessage(AxionDBConfiguration.class, "LBL_AxionConfs"));
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

    public static String getClusters(File dir, String str) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                if (children[i].trim().toLowerCase().startsWith(str)) {
                    return children[i];
                }
            }
        }
        return null;
    }

    private static String getNetbeansHome() {
        String netbeansHome = System.getProperty("netbeans.home");
        if (!netbeansHome.endsWith("netbeans")) {
            File f = new File(netbeansHome);
            netbeansHome = f.getParentFile().getAbsolutePath();
        }
        return netbeansHome;
    }

    public static File getConfigFile() {
        String nbUsrDir = null;
        String netbeansHome = null;
        nbUsrDir = System.getProperty("netbeans.user");
        netbeansHome = getNetbeansHome();
        if ((nbUsrDir.length() == 0) || (netbeansHome.length() == 0)) {
            java.util.logging.Logger.getLogger(AxionDBConfiguration.class.getName()).info(NbBundle.getMessage(AxionDBConfiguration.class, "LOG_PropertiesSet"));
            return null;
        }
        // Check for virtual db driver under soa cluster.
        String soa = getClusters(new File(netbeansHome), "soa");
        String nbHomeDir = netbeansHome + FS + soa + FS + "modules" + FS + "ext" + FS + "dm" + FS + "virtual" + FS + "db" + FS + "axiondb.jar";
        File driver = new File(nbHomeDir);
        if (!driver.exists()) {
            String extra = getClusters(new File(netbeansHome), "extra");
            // check for virtual db driver under extra cluster.
            nbHomeDir = getNetbeansHome() + FS + extra + FS + "modules" + FS + "ext" + FS + "dm" + FS + "virtual" + FS + "db" + FS + "axiondb.jar";
            driver = new File(nbHomeDir);
            if (!driver.exists()) {
                //nbHomeDir = "";
                nbHomeDir = nbUsrDir + FS +  "modules" + FS + "ext" + FS + "dm" + FS + "virtual" + FS + "db" + FS + "axiondb.jar";
                driver = new File(nbHomeDir);
            }
        }
        nbHomeDir = driver.getAbsolutePath();
        String DEFAULT_DB_LOCATION = nbUsrDir + FS + "VirtualDatabases" + FS;
        File f = new File(DEFAULT_DB_LOCATION);
        if (!f.exists()) {
            f.mkdirs();
        }
        nbUsrDir = nbUsrDir + FS + "config" + FS + "Databases" + FS + "VirtualDB";
        File conf = new File(nbUsrDir);
        if (!conf.exists()) {
            conf.mkdirs();
        }
        nbUsrDir = nbUsrDir + FS + "VirtualDBConfig.properties";
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

                    prop.store(out, NbBundle.getMessage(AxionDBConfiguration.class, "LBL_AxionDatabaseLocation"));

                    out.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ioEx) {
                    ioEx.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                conf = null;
            }
        }
        return conf;
    }

    protected static BeanNode createViewNode() throws java.beans.IntrospectionException {
        BeanNode nd = new BeanNode(AxionDBConfiguration.getDefault());
        nd.setName(NbBundle.getMessage(AxionDBConfiguration.class, "LBL_VirtualDB"));
        return nd;
    }
}
