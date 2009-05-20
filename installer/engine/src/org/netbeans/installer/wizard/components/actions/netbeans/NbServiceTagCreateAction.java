/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.wizard.components.actions.netbeans;

import org.netbeans.modules.servicetag.ServiceTag;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.modules.reglib.NbServiceTagSupport;
import org.netbeans.modules.reglib.StatusData;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;

/**
 *
 * @author Dmitry Lipin
 */
public class NbServiceTagCreateAction extends WizardAction {
    private String source;

    public NbServiceTagCreateAction() {
        Logger parent = Logger.getLogger(this.getClass().getName()).getParent();
        Handler[] handlers = (parent == null) ? null : parent.getHandlers();
        //if(parent!=null) parent.setLevel(Level.ALL);
        if (handlers != null) {
            for (Handler h : handlers) {
                parent.removeHandler(h);
            }
        }
        parent.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                LogManager.log(record.getSourceClassName() + "." +
                        record.getSourceMethodName() + "(): " + record.getLevel());
                LogManager.log(
                        (record.getParameters() == null) ? 
                            record.getMessage() : 
                            StringUtils.format(record.getMessage(), 
                        record.getParameters()));

                if (record.getThrown() != null) {
                    LogManager.log(record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        if(SystemUtils.isMacOS()) {                                           
            System.setProperty(ALLOW_SERVICETAG_CREATION_PROPERTY,"" + false);
        }
    }

    public void execute() {
        LogManager.logEntry("... create service tags action");
        final List<Product> products = new LinkedList<Product>();
        final Registry registry = Registry.getInstance();
        products.addAll(registry.getProducts(INSTALLED_SUCCESSFULLY));
        products.addAll(registry.getProducts(INSTALLED_WITH_WARNINGS));
        source = SOURCE_NAME;

        try {
            Registry bundledRegistry = new Registry();
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);

            bundledRegistry.loadProductRegistry((bundledRegistryUri != null) ? 
                bundledRegistryUri : Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
            List<Product> bundledJdks = bundledRegistry.getProducts("jdk");
            
            if (bundledJdks.size() > 0) {
                source = StringUtils.format(SOURCE_NAME_JDK,
                        bundledJdks.get(0).getDisplayName(new Locale("")));
            }
            List<Product> bundledAppservers = bundledRegistry.getProducts("sjsas");
            
            if (bundledAppservers.size() > 0 && NetBeansUtils.getNetBeansId().equals("NBEETOOLS")) {
                source = SOURCE_NAME_JTB;
            }
        } catch (InitializationException e) {
            LogManager.log("Cannot load bundled registry", e);
        }

        for (Product product : products) {
            if (product.getUid().startsWith("nb-")) {
                createSTNetBeans(product);
            }
        }
        for (Product product : products) {
            String uid = product.getUid();
            if (uid.equals("glassfish")) {
                createSTGlassFish(product, true);
            } else if (uid.equals("glassfish-mod")) {
                createSTGlassFish(product, false);
            } else if (uid.equals("glassfish-mod-sun")) {
                createSTGlassFish(product, false);
            } else if (uid.equals("sjsas")) {
                createSTGlassFish(product, false);
            } else if (uid.equals("jdk")) {
                createSTJDK(product);
            }
        }
        LogManager.logExit("... finished service tags action");
    }
    
    private File getNetBeansLocation(Product product) {
        File nbLocation = null;

        if (product.getUid().equals("nb-base")) {
            nbLocation = product.getInstallationLocation();
        } else if (product.getUid().startsWith("nb-")) {
            List <Dependency> deps = product.getDependencyByUid("nb-base");
            if(deps.size() > 0) {
                nbLocation = Registry.getInstance().getProducts(deps.get(0)).get(0).getInstallationLocation();
            }
        }
        return nbLocation;
    }
    
    private void createSTNetBeans(Product product) {
        try {
            File nbLocation = getNetBeansLocation(product);
            LogManager.log("... netbeans location : " + nbLocation);

            if(nbLocation==null) {
                return;
            }
            File nbPlatform = nbLocation.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().startsWith("platform");
                }
            })[0];

            LogManager.log("... netbeans.home: " + nbPlatform);
            System.setProperty("netbeans.home", nbPlatform.getPath());

            Version javaV = null;
            try {
                String javaHome = NetBeansUtils.getJavaHome(nbLocation);
                LogManager.log("... netbeans_jdkhome: " + javaHome);
                if(javaHome!=null && !javaHome.equals("")) {
                    javaV = JavaUtils.getVersion(new File(javaHome));
                }
            } catch (IOException e) {
                LogManager.log(e);
            }

            if(javaV==null) {
                LogManager.log("... can`t get java version that netbeans (" + nbLocation+ ") uses, fallback to the default one");
                javaV = JavaUtils.getVersion(SystemUtils.getCurrentJavaHome());                
            }

            LogManager.log("... java.version: " + javaV);            
            String javaVersion = javaV.toJdkStyle();            
            LogManager.log("... java.version(f): " + javaVersion);

            if (product.getUid().equals("nb-base")) {
                LogManager.log("... create ST for NetBeans");
                NbServiceTagSupport.createNbServiceTag(source, javaVersion);
                setNetBeansStatus(false);
            } else if (product.getUid().equals("nb-cnd")) {
                LogManager.log("... create ST for " + product.getDisplayName());
                NbServiceTagSupport.createCndServiceTag(source, javaVersion);
            } else if (product.getUid().equals("nb-javafx")) {
                LogManager.log("... create ST for " + product.getDisplayName());
                NbServiceTagSupport.createJavaFXServiceTag(source, javaVersion);
                NbServiceTagSupport.createJavaFXSdkServiceTag(source, javaVersion);
            }
        } catch (Exception e) {
            LogManager.log(e);
        }
    }

    public static void setNetBeansStatus(boolean register) {
        StatusData sd = (register) ? 
            new StatusData(StatusData.STATUS_REGISTERED, StatusData.DEFAULT_DELAY):
            new StatusData(StatusData.STATUS_LATER, 1);

        File parent = NbServiceTagSupport.getServiceTagDirHome();
        File statusFile = new File(parent, "status.xml");
        try {
            FileUtils.mkdirs(parent);
        } catch (IOException ex) {
            LogManager.log("Error: Cannot create directory " + parent);
            return;
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(statusFile));
            sd.storeToXML(out);
        } catch (IOException ex) {
            LogManager.log("Error: Cannot save status data to " + statusFile, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LogManager.log("Error: Cannot close writer", ex);
                }
            }
        }
    }

    private void createSTGlassFish(Product gfProduct, boolean createInstallationST) {
        LogManager.log("... create ST for GlassFish/AppServer");
        File location = gfProduct.getInstallationLocation();
        File gfJavaHome = SystemUtils.getCurrentJavaHome();//default
        try {
            if(gfProduct.getVersion().getMajor() != 3) {
            gfJavaHome = GlassFishUtils.getJavaHome(location);
            }
        } catch (IOException e) {
            LogManager.log(e);
        }

        if (createInstallationST) {
            File ant = new File(location, "lib/ant/bin/ant" + (SystemUtils.isWindows() ? ".bat" : ""));
            File registryXml = new File(location, "registry.xml");
            try {
                final String[] command = {
                    ant.getAbsolutePath(),
                    (SystemUtils.isWindows() ? "" : "--noconfig"),
                    "-v",
                    "-f",
                    registryXml.getAbsolutePath(),
                    "-Dinstall.home=" + location.getAbsolutePath(),
                    "-Dsource=" + source
                };
                if(!SystemUtils.isWindows()) {
                    SystemUtils.correctFilesPermissions(ant);
                }
                SystemUtils.setEnvironmentVariable("JAVA_HOME",gfJavaHome.getPath());
                SystemUtils.executeCommand(location, command);
            } catch (IOException e) {
                LogManager.log(e);
            } catch (NativeException e) {
                LogManager.log(e);
            }
        }



        try {
            // usually netbeans is installed first, so netbeans.home should be already installed
            // if not - that means that only GF/AS was installed and NB not - then 
            // do not add ST info to NB ST and do not initialize 
            // instance_urn & product_defined_inst_id in the GF/AS ST
            ServiceTag gfST = null;
            if (System.getProperty("netbeans.home") != null) {
                // java.home system variable usually points to private jre with MacOS exception

                LogManager.log("... calling RegLib function");
                LogManager.log("...... gfJavaHome : "  + gfJavaHome);
                LogManager.log("...... gfLocation : "  + location);

                final File jreHome = new File(gfJavaHome, "jre");
                File javaHome = (SystemUtils.isMacOS() || !jreHome.exists()) ? gfJavaHome : jreHome;
                LogManager.log("...... javaHome : "    + javaHome);


                String gfVersion = null;
                if(gfProduct.getVersion().getMajor() == 2) {
                   gfVersion = gfProduct.getUid().equals("glassfish")? "v2" : "v2.1";                   
                } else {
                   gfVersion = "v3";
                }

                LogManager.log("...... gfVersion : "   + gfVersion);
                Version javaVersion = JavaUtils.getVersion(gfJavaHome);
                if(javaVersion==null) {
                    LogManager.log("...... can`t get java version from "   + gfJavaHome + ", using default one");
                    javaHome = SystemUtils.getCurrentJavaHome();
                    javaVersion = JavaUtils.getVersion(javaHome);
                    LogManager.log("...... java.home : " + javaHome);
                }

                final String  jdkVersion = javaVersion.toJdkStyle();
                LogManager.log("...... javaVersion : " + jdkVersion);
                
                gfST = NbServiceTagSupport.createGfServiceTag(source,
                        javaHome.getAbsolutePath(),
                        jdkVersion,
                        location.getAbsolutePath(),
                        gfVersion);
            }
            final String registry = "lib/registration/servicetag-registry.xml";
            final String relativeLocation = (gfProduct.getVersion().getMajor() == 3 ? "glassfish/" : "") + registry;
            File gfReg = new File(location, relativeLocation);

            if (gfReg.exists()) {
                Map<String, Object> map = new HashMap<String, Object>();
                if (gfST != null) {
                    String urn = gfST.getInstanceURN();
                    LogManager.log("... GF instanceUrn : " + urn);
                    if (urn != null && !urn.equals("")) {
                        map.put("<instance_urn/>", "<instance_urn>" + urn + "</instance_urn>");
                        map.put("<instance_urn></instance_urn>", "<instance_urn>" + urn + "</instance_urn>");
                    }
                    // specific to ST that is created by AppServer itself and stored in the installation image
                    // platform_arch and product_defined_inst_id are not set in AS install image
                    map.put("<platform_arch></platform_arch>",
                            "<platform_arch>" + gfST.getPlatformArch() + "</platform_arch>");
                    map.put("<product_defined_inst_id></product_defined_inst_id>",
                            "<product_defined_inst_id>" + gfST.getProductDefinedInstanceID() + "</product_defined_inst_id>");

                } else {
                    map.put("<platform_arch></platform_arch>",
                            "<platform_arch>" + System.getProperty("os.arch") + "</platform_arch>");
                }
                map.put("<source>Sun Java System Application Server Native Packages</source>",
                        "<source>" + source + "</source>");
                map.put("<source>GlassFish V3</source>",
                        "<source>" + source + "</source>");
                map.put("<source>Sun GlassFish Enterprise Server Native Packages</source>",
                         "<source>" + source + "</source>");
                // AppServer installation image has this incorrect vendor
                map.put("Sun Micosystems Inc.",
                        "Sun Microsystems Inc.");
                FileUtils.modifyFile(gfReg, map);
            }
        } catch (Exception e) {
            LogManager.log(e);
        }
    }

    private void createSTJDK(Product jdkProduct) {
        final String classpath = System.getProperty("java.class.path");

        if(System.getProperty("netbeans.home")!=null) {
            try {
                final File jdkHome = jdkProduct.getInstallationLocation();
                final File javaExe = JavaUtils.getExecutable(jdkHome);
                LogManager.log("... java.exe = " + javaExe);
                final String [] command = new String [] {
                    javaExe.getPath(),
                    "-cp",
                    classpath,
                    this.getClass().getName(),
                    System.getProperty("netbeans.home"),
                    source
                };
            
                SystemUtils.executeCommand(jdkHome, command);
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
    }

    public static void main(String[] args) {
        LogManager.start();
        LogManager.log("... creating JDK service tag");
        LogManager.log("... netbeans.home = " + args[0]);
        System.setProperty("netbeans.home", args[0]);
        LogManager.log("... source = " + args[1]);
        try {
            NbServiceTagSupport.createJdkServiceTag(args[1]);
        } catch (Exception e) {
            LogManager.log(e);
        }
        if (System.getProperty("java.version").startsWith("1.5")) {
            //Issue #142607 Wrong installer behaviour after uninstalling bundled jdk
            File jdkHome = new File(System.getProperty("java.home")).getParentFile();
            LogManager.log("... jdkhome = " + jdkHome);
            LogManager.log("... removing ST files that do were not created by JDK installer");
            new File(jdkHome, "jre/lib/servicetag/registration.xml").delete();
            new File(jdkHome, "jre/lib/servicetag").delete();
            for (File f : jdkHome.listFiles()) {
                if (f.getName().matches("register(_[a-zA-Z]+)*\\.html")) {
                    f.delete();
                }
            }
        }
        LogManager.log("... JDK ST created");
    }

    @Override
    public boolean canExecuteForward() {
        return Boolean.getBoolean(ALLOW_SERVICETAG_CREATION_PROPERTY);
    }

    @Override
    public WizardActionUi getWizardUi() {
        return null;
    }
    public static final String ALLOW_SERVICETAG_CREATION_PROPERTY =
            "servicetag.allow.create";//NOI18N
    public static final String SOURCE_NAME =
            ResourceUtils.getString(NbServiceTagCreateAction.class,
            "NSTCA.installer.source.name");//NOI18N
    public static final String SOURCE_NAME_JDK =
            ResourceUtils.getString(NbServiceTagCreateAction.class,
            "NSTCA.installer.source.name.jdk");//NOI18N
    public static final String SOURCE_NAME_JTB =
            ResourceUtils.getString(NbServiceTagCreateAction.class,
            "NSTCA.installer.source.name.jtb");//NOI18N
}
