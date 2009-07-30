/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.subversion.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.netbeans.modules.subversion.client.cli.CommandlineClient;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * A SvnClient factory
 *
 * @author Tomas Stupka
 */
public class SvnClientFactory {

    public static final String JAVAHL_MODULE_CODE_NAME = "org.netbeans.libs.svnjavahlwin32";
    private static final String SUBVERSION_NATIVE_LIBRARY = "subversion.native.library";

    /** the only existing SvnClientFactory instance */
    private static SvnClientFactory instance;
    /** the only existing ClientAdapterFactory instance */
    private static ClientAdapterFactory factory;
    /** if an exception occured */
    private static SVNClientException exception = null;

    /** indicates that something went terribly wrong with javahl init during the previous nb session */
    private static boolean javahlCrash = false;
    private final static int JAVAHL_INIT_SUCCESS = 1;
    private final static int JAVAHL_INIT_STOP_REPORTING = 2;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.client.SvnClientFactory");

    public enum ConnectionType {
        javahl,
        cli,
        svnkit
    }

    /** Creates a new instance of SvnClientFactory */
    private SvnClientFactory() {
    }

    /**
     * Returns the only existing SvnClientFactory instance
     *
     * @return the SvnClientFactory instance
     */
    public synchronized static SvnClientFactory getInstance() {
        init();
        return instance;
    }

    /**
     * Initializes the SvnClientFactory instance
     */
    public synchronized static void init() {
        if(instance == null) {
            instance = new SvnClientFactory();
            instance.setup();
        }
    }

    /**
     * Resets the SvnClientFactory instance
     */
    public synchronized static void reset() {
        instance = null;
    }

    public static boolean isCLI() {
        init();
        assert factory != null;
        return factory.connectionType() == ConnectionType.cli;
    }

    public static boolean isJavaHl() {
        init();
        assert factory != null;
        return factory.connectionType() == ConnectionType.javahl;
    }

    public static boolean isSvnKit() {
        init();
        assert factory != null;
        return factory.connectionType() == ConnectionType.svnkit;
    }

    /**
     * Returns a SvnClient, which isn't configured in any way.
     * Knows no username, password, has no SvnProgressSupport<br/>
     * Such an instance isn't supposed to work properly when calling remote svn commands.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient() throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        return factory.createSvnClient();
    }

    /**
     *
     * Returns a SvnClient which is configured with the given <tt>username</tt>,
     * <tt>password</tt>, <tt>repositoryUrl</tt> and the <tt>support</tt>.<br>
     * In case a http proxy was given via <tt>pd</tt> an according entry for the <tt>repositoryUrl</tt>
     * will be created in the svn config file.
     * The mask <tt>handledExceptions</tt> specifies which exceptions are to be handled.
     *
     * @param repositoryUrl
     * @param support
     * @param username
     * @param password
     * @param handledExceptions
     *
     * @return the configured SvnClient
     *
     */
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, String password, int handledExceptions) throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        return factory.createSvnClient(repositoryUrl, support, username, password, handledExceptions);
    }

    /**
     * A SVNClientAdapterFactory will be setup, according to the svnClientAdapterFactory property.<br>
     * The CommandlineClientAdapterFactory is default as long no value is set for svnClientAdapterFactory.
     *
     */
    private void setup() {
        try {
            String factoryType = System.getProperty("svnClientAdapterFactory");
            // ping config file copying
            SvnConfigFiles.getInstance();

            if(factoryType == null ||
               factoryType.trim().equals("") ||
               factoryType.trim().equals("javahl"))
            {
                if(!setupJavaHl()) {
                    setupCommandline() ;
                }
            } else if(factoryType.trim().equals("svnkit")) {
                if(!setupSvnKit()) {
                    setupCommandline();
                }
            } else if(factoryType.trim().equals("commandline")) {
                setupCommandline();
            } else {
                throw new SVNClientException("Unknown factory: " + factoryType);
            }
        } catch (SVNClientException e) {
            exception = e;
        }
    }

    /**
     * Throws an exception if no SvnClientAdapter is available.
     */
    public static void checkClientAvailable() throws SVNClientException {
        init();
        if(exception != null) throw exception;
    }

    public static boolean isClientAvailable() {
        init();
        return exception == null;
    }

    public static boolean wasJavahlCrash() {
        init();
        if(javahlCrash) {
            javahlCrash = false;
            return true;
        }
        return false;
    }

    private boolean setupJavaHl () {

        String jhlInitFile = System.getProperty("netbeans.user") + "/config/svn/jhlinit";
        File initFile = new File(jhlInitFile);

        if(checkJavahlCrash(initFile)) {
            return false;
        }
        try {
            if(!initFile.exists()) initFile.createNewFile();
        } catch (IOException ex) {
            // should not happen
            LOG.log(Level.INFO, null, ex);
        }

        presetJavahl();
        try {            
            if(!SvnClientAdapterFactory.getInstance().setup(SvnClientAdapterFactory.Client.javahl)) {
               LOG.log(Level.INFO, "Could not setup subversion java bindings. Falling back on commandline.");
               return false;
            }
            if(!checkJavaHlVersion()) {
                LOG.log(Level.INFO, "Unsupported version of subversion javahl bindings. Falling back on commandline.");
                return false;
            }
        } catch (SVNClientException e) {
            LOG.log(Level.WARNING, null, e); // should not happen
        } finally {
            writeJavahlInitFlag(initFile, JAVAHL_INIT_SUCCESS);
        }
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return SvnClientAdapterFactory.getInstance().createClient();
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnClientCallback(repositoryUrl, handledExceptions);
            }
            protected ConnectionType connectionType() {
                return ConnectionType.javahl;
            }
        };
        LOG.info("running on javahl");
        return true;
    }

    private void presetJavahl() {
        if(Utilities.isUnix() && !Utilities.isMac() ) { // javahl for mac is already bundled
            presetJavahlUnix();
        } else if(Utilities.isWindows()) {
            presetJavahlWindows();
        }
    }

    private void presetJavahlUnix() {
        LOG.log(Level.FINE, "looking for svn native library...");
        String libPath = System.getProperty(SUBVERSION_NATIVE_LIBRARY);
        if (libPath != null && !libPath.trim().equals("")) {
            LOG.log(Level.FINE, "won't preset javahl due to subversion.native.library={0}", new Object[] { libPath });
            return;
        }
        String name = "libsvnjavahl-1.so";
        String[] locations = new String[] {"/usr/lib/", "/usr/lib/jni/", "/usr/local/lib/"};
        File location = null;
        for (String loc : locations) {
            File file = new File(loc, name);
            LOG.log(Level.FINE, " checking existence of {0}", new Object[] { file.getAbsolutePath() });
            if (file.exists()) {
                location = file;
                break;
            }
        }
        if(location == null) {
            location = getJavahlFromExecutablePath(name);
        }
        if(location != null) {
            System.setProperty("subversion.native.library", location.getAbsolutePath());
            LOG.log(Level.FINE, "   found javahl library. Setting subversion.native.library={0}", new Object[] { location.getAbsolutePath() });
        }
    }

    private void presetJavahlWindows() {
        String libPath = System.getProperty(SUBVERSION_NATIVE_LIBRARY);
        if (libPath != null && !libPath.trim().equals("")) {
            // the path is already set -> lets ensure we load all dependencies
            // from the same folder and let then svnClientAdapter take care for the rest
            LOG.log(Level.FINE, "preset subversion.native.library={0}", new Object[] { libPath } );
            int idx = libPath.lastIndexOf(File.separator);
            if(idx > -1) {
                libPath = libPath.substring(0, idx);
                LOG.log(Level.FINE, "loading dependencies from ", new Object[] { libPath } );
                loadJavahlDependencies(libPath);
            }
            return;
        }
                
        File location = InstalledFileLocator.getDefault().locate("modules/lib/libsvnjavahl-1.dll", JAVAHL_MODULE_CODE_NAME, false);
        if(location == null) {
            LOG.fine("could not find location for bundled javahl library");
            location = getJavahlFromExecutablePath("libsvnjavahl-1.dll");
            if(location == null) {
                return;
            }
        }
        // the library seems to be available in the netbeans install/user dir
        // => set it up so that it will used by the svnClientAdapter
        LOG.fine("libsvnjavahl-1.dll located : " + location.getAbsolutePath());
        String locationPath = location.getParentFile().getAbsolutePath();
        // svnClientAdapter workaround - we have to explicitly load the
        // libsvnjavahl-1 dependencies as svnClientAdapter  tryies to get them via loadLibrary.
        // That won't work i they aren't on java.library.path
        loadJavahlDependencies(locationPath);

        // libsvnjavahl-1 must be loaded by the svnClientAdapter to get the factory initialized
        locationPath = location.getAbsolutePath();
        LOG.log(Level.FINE, "setting subversion.native.library={0}", new Object[] { locationPath });
        System.setProperty("subversion.native.library", locationPath);
    }

    private void loadJavahlDependencies(String locationPath) {
        try { System.load(locationPath + "/libapr-1.dll"); }        catch (Throwable t) { }
        try { System.load(locationPath + "/libapriconv-1.dll"); }   catch (Throwable t) { }
        try { System.load(locationPath + "/libeay32.dll"); }        catch (Throwable t) { }
        try { System.load(locationPath + "/libdb44.dll"); }         catch (Throwable t) { }
        try { System.load(locationPath + "/ssleay32.dll"); }        catch (Throwable t) { }
        try { System.load(locationPath + "/libaprutil-1.dll"); }    catch (Throwable t) { }
        try { System.load(locationPath + "/intl3_svn.dll"); }       catch (Throwable t) { }
        try { System.load(locationPath + "/dbghelp.dll"); }         catch (Throwable t) { }
        try { System.load(locationPath + "/libsasl.dll"); }         catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_subr-1.dll"); }   catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_delta-1.dll"); }  catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_diff-1.dll"); }   catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_wc-1.dll"); }     catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_fs-1.dll"); }     catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_repos-1.dll"); }  catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_ra-1.dll"); }     catch (Throwable t) { }
        try { System.load(locationPath + "/libsvn_client-1.dll"); } catch (Throwable t) { }
    }


    private File getJavahlFromExecutablePath(String libName) {
        String executablePath = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        LOG.log(Level.FINE, "looking for svn native library in executable path={0}", new Object[] { executablePath });
        File location = new File(executablePath);
        if (location.isFile()) {
            location = location.getParentFile();
        }
        if (location != null) {
            location = new File(location.getAbsolutePath() + File.separatorChar + libName);
            if(location.exists()) {
                LOG.log(Level.FINE, "found svn native library in executable path={0}", new Object[] { location.getAbsolutePath() });
                return location;
            }
        }
        return null;
    }

    private boolean checkJavahlCrash(File initFile) {
        if(!initFile.exists()) {
            LOG.fine("trying to init javahl first time.");
            return false;
        }
        FileReader r = null;
        try {
            r = new FileReader(initFile);
            int i = r.read();
            try { r.close(); r = null; } catch(IOException e) {}
            switch(i) {
                case -1: // empty means we crashed
                    writeJavahlInitFlag(initFile, JAVAHL_INIT_STOP_REPORTING);
                    javahlCrash = true;
                    LOG.log(Level.WARNING, "It appears that subversion java bindings initialization caused trouble in a previous Netbeans session. Please report.");
                    return true;
                case JAVAHL_INIT_STOP_REPORTING:
                    LOG.fine("won't init javahl due to problem in a previous try.");
                    return true;
                case JAVAHL_INIT_SUCCESS:
                    LOG.fine("will try init javahl.");
                    return false;
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        } finally {
            try { if(r != null) r.close(); } catch (IOException ex) { }
        }
        return false;  // optimistic attitude
    }

    private void writeJavahlInitFlag(File initFile, int flag) {
        FileWriter w = null;
        try {
            w = new FileWriter(initFile);
            w.write(flag);
            w.flush();
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        } finally {
            try { if(w != null) w.close(); } catch (IOException ex) { }
        }
    }

    private boolean  setupSvnKit () {
        try {
            if(!SvnClientAdapterFactory.getInstance().setup(SvnClientAdapterFactory.Client.svnkit)) {
                LOG.log(Level.INFO, "Svnkit not available. Falling back on commandline!");
                return false;
            }
        } catch (SVNClientException ex) {
            LOG.log(Level.INFO, null, ex);
            LOG.log(Level.INFO, null, ex.getCause());
            LOG.log(Level.INFO, "Could not setup svnkit. Falling back on commandline!");
            return false;
        }
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return SvnClientAdapterFactory.getInstance().createClient();
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnClientCallback(repositoryUrl, handledExceptions);
            }
            protected ConnectionType connectionType() {
                return ConnectionType.svnkit;
            }
        };
        LOG.info("svnClientAdapter running on svnkit");
        return true;
    }

    public void setupCommandline () {
        if(!checkCLIExecutable()) return;
        
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return new CommandlineClient(); //SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnCmdLineClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return null;
            }
            protected ConnectionType connectionType() {
                return ConnectionType.cli;
            }
        };
        LOG.info("running on commandline");
    }

    private boolean checkCLIExecutable() {
        exception = null;
        SVNClientException ex = null;
        try {
            checkVersion();
        } catch (SVNClientException e) {
            ex = e;
        }
        if(ex == null) {
            // works on first shot
            LOG.fine("svn client returns correct version");
            return true;
        }
        String execPath = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(execPath != null && !execPath.trim().equals("")) {
            exception = ex;
            LOG.log(Level.WARNING, "executable binary path set to {0} yet client not available.", new Object[] { execPath });
            return false; 
        }
        if(Utilities.isUnix()) {
            LOG.fine("svn client isn't set on path yet. Will check known locations...");
            String[] locations = new String[] {"/usr/local/bin/", "/usr/bin/"};
            String name = "svn";
            for (String loc : locations) {
                File file = new File(loc, name);
                LOG.log(Level.FINE, "checking existence of {0}", new Object[] { file.getAbsolutePath() });
                if (file.exists()) {                
                    SvnModuleConfig.getDefault().setExecutableBinaryPath(loc);
                    try {
                        checkVersion();
                    } catch (SVNClientException e) {
                        ex = e;
                        continue;
                    }
                    LOG.log(Level.INFO, "found svn executable binary. Setting executable binary path to {0}", new Object[] { loc });
                    return true;
                }
            }
        }
        exception = ex;
        return false;
    }

    private void checkVersion() throws SVNClientException {
        CommandlineClient cc = new CommandlineClient();
        try {
            setConfigDir(cc);
            cc.checkSupportedVersion();
        } catch (SVNClientException e) {
            LOG.log(Level.FINE, "checking version", e);
            throw e;
        }
    }

    private boolean checkJavaHlVersion() throws SVNClientException {
        return SvnClientAdapterFactory.getInstance().isSupportedJavahlVersion();
    }

    private void setConfigDir (ISVNClientAdapter client) {
        if (client != null) {
            File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
            try {
                client.setConfigDirectory(configDir);
            } catch (SVNClientException ex) {
                // not interested, just log
                LOG.log(Level.INFO, null, ex);
            }
        }
    }

    private abstract class ClientAdapterFactory {

        abstract protected ISVNClientAdapter createAdapter();
        abstract protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions);
        abstract protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions);
        abstract protected ConnectionType connectionType();

        SvnClient createSvnClient() {
            SvnClientInvocationHandler handler = getInvocationHandler(createAdapter(), createDescriptor(null), null, -1);
            SvnClient client = createSvnClient(handler);
            setConfigDir(client);
            return client;
        }

        /**
         *
         * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>adapter</tt>,
         * <tt>support</tt> and a SvnClientDescriptor for <tt>repository</tt>.
         *
         * @param adapter
         * @param support
         * @param repository
         *
         * @return the created SvnClientInvocationHandler instance
         *
         */
        public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, String password, int handledExceptions) {
            ISVNClientAdapter adapter = createAdapter();
            SvnClientInvocationHandler handler = getInvocationHandler(adapter, createDescriptor(repositoryUrl), support, handledExceptions);
            setupAdapter(adapter, username, password, createCallback(repositoryUrl, handledExceptions));
            return createSvnClient(handler);
        }

        private SvnClientDescriptor createDescriptor(final SVNUrl repositoryUrl) {
            return new SvnClientDescriptor() {
                public SVNUrl getSvnUrl() {
                    return repositoryUrl;
                }
            };
        }

        private SvnClient createSvnClient(SvnClientInvocationHandler handler) {
            Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );
            Subversion.getInstance().cleanupFilesystem();
            try {
               return (SvnClient) proxyClass.getConstructor( new Class[] { InvocationHandler.class } ).newInstance( new Object[] { handler } );
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            }
            return null;
        }

        protected void setupAdapter(ISVNClientAdapter adapter, String username, String password, ISVNPromptUserPassword callback) {
            if(callback != null) {
                adapter.addPasswordCallback(callback);
            }
            try {
                File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
                adapter.setConfigDirectory(configDir);
                adapter.setUsername(username);
                adapter.setPassword(password);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
        }
    }
}
