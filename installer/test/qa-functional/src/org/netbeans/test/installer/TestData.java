/*
 * TestData.java
 *
 * Created on 03.07.2007, 21:01:14
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.installer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mv153220
 */
public class TestData implements Serializable {

    private File installerFile = null;
    private File workDir = null;
    private File bundleFile = null;
    private Logger logger = null;
    private ClassLoader loader = null;
    private ClassLoader engineLoader = null;
    private Class installerMainClass = null;
    private Class uninstallerMainClass = null;
    private String workDirCanonicalPath = null;
    private String platformName = null;
    private String platformExt = null;
    private String installerType = null;

    public TestData(Logger logger) {
        assert logger != null;
        this.logger = logger;
        initPlatformVar();
    }

    public String getInstallerFileName() {
        return "E:/pub/Netbeans/6.0/netbeans-6.0-nightly-200707221200-standard-windows.exe";
//        return "C:/work/test/TestInstaller/netbeans-6.0-nightly-200707100000-basic-windows.exe";
    }

    public String getInstallerURL(String type) {
        String val = System.getProperty("installer.url.prefix");
        String prefix = (val == null) ? "http://bits.netbeans.org/netbeans/6.0/nightly/200707120000/bundles/netbeans-6.0-nightly-200707120000" : val;

        //val = System.getProperty("installer.url.bundle.type");
        String bundleType = (type == null) ? "basic" : type;

        return prefix + "-" + bundleType + "-" + platformName + "." + platformExt;
    }

    private void initPlatformVar() {

        if (System.getProperty("os.name").contains("Windows")) {
            platformName = "windows";
            platformExt = "exe";
        }

        if (System.getProperty("os.name").contains("Linux")) {
            platformName = "linux";
            platformExt = "sh";
        }

        if (System.getProperty("os.name").contains("Mac OS X")) {
            throw new Error("Mac OS not supported");
        }

        if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("sparc")) {
            platformName = "solaris-sparc";
            platformExt = "sh";
        }
        if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("x86")) {
            platformName = "solaris-x86";
            platformExt = "sh";
        }
    }


    public String getPlatformExt() {
        return platformExt;
    }

    public String getPaltformName() {
        return platformName;
    }
    
    public String getInstallerMainClassName() {
        return "org.netbeans.installer.Installer";
    }

    public String getUninstallerMainClassName() {
        return "org.netbeans.installer.Installer";
    }

    public Logger getLogger() {
        return logger;
    }

    public void setWorkDir(File workDir) throws IOException {
        assert workDir != null;
        this.workDir = workDir;
        workDirCanonicalPath = workDir.getCanonicalPath();
    }


    public File getTestWorkDir() {
        assert workDir != null;
        return workDir;
    }

    public String getWorkDirCanonicalPath() {
        return workDirCanonicalPath;
    }

    public File getInstallerFile() {
        return installerFile;
    }

    public void setInstallerFile(File installerFile) {
        if (canRead(installerFile)) {
            this.installerFile = installerFile;
        }
    }

    public void setBundleFile(File bundleFile) {
        if (canRead(bundleFile)) {
            this.bundleFile = bundleFile;
        }
    }

    public File getBundleFile() {
        return bundleFile;
    }

    public void setClassLoader(ClassLoader loader) {
        assert loader != null;
        this.loader = loader;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public void setEngineClassLoader(ClassLoader loader) {
        assert loader != null;
        engineLoader = loader;
    }

    public ClassLoader getEngineClassLoader() {
        return engineLoader;
    }

    public void setInstallerMainClass(Class clazz) {
        assert clazz != null;
        this.installerMainClass = clazz;
    }

    public Class getInstallerMainClass() {
        return installerMainClass;
    }

    public void setUninstallerMainClass(Class clazz) {
        assert clazz != null;
        this.uninstallerMainClass = clazz;
    }

    public Class getUninstallerMainClass() {
        return uninstallerMainClass;
    }

    private boolean canRead(File file) {
        if (file != null) {
            if (!file.canRead()) {
                java.lang.String fileName = null;
                try {
                    fileName = file.getCanonicalPath();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Can't get cannonical path");
                }
                logger.log(Level.SEVERE, "Can't read file: " + fileName);
                return false;
            }
        } else {
            logger.log(Level.SEVERE, "Bundle file name can be null");
            return false;
        }
        return true;
    }

    public String getInstallerType() {
        return installerType;
    }

    public void setInstallerType(String installerType) {
        this.installerType = installerType;
    }
}
