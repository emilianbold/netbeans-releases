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

package org.netbeans.libs.svnclientadapter.javahl;

import java.io.File;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProviders({@ServiceProvider(service=SvnClientAdapterFactory.class)})
public class JavaHlClientAdapterFactory extends SvnClientAdapterFactory {

    private static final String SUBVERSION_NATIVE_LIBRARY = "subversion.native.library";
    
    /** indicates that something went terribly wrong with javahl init during the previous nb session */
    private static boolean javahlCrash = false;
    private final static int JAVAHL_INIT_NOCRASH = 1;
    private final static int JAVAHL_INIT_STOP_REPORTING = 2;
    
    private boolean available = false;

    public JavaHlClientAdapterFactory() {
        super();
    }

    @Override
    public Client provides() {
        return Client.JAVAHL;
    }

    @Override
    public boolean isAvailable() {
        if(!available) {                    
            presetJavahl();
            try {
                JhlClientAdapterFactory.setup();
            } catch (Throwable t) {
                String jhlErorrs = JhlClientAdapterFactory.getLibraryLoadErrors();
                LOG.log(Level.INFO, t.getMessage());
                LOG.log(Level.WARNING, "{0}\n", jhlErorrs);                                             // NOI18N
                return false;
            }
            String version = getVersion();
            if(!isSupportedJavahlVersion(version)) {
                LOG.log(Level.INFO, "Unsupported version {0} of subversion javahl bindings.", version); // NOI18N
                return false;
            }
            available = JhlClientAdapterFactory.isAvailable();
        }
        return available;
    }

    @Override
    public ISVNClientAdapter createClient() {
        return JhlClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
    }

    private boolean isSupportedJavahlVersion(String version) {
        boolean retval = false;
        if (version != null) {
            version = version.toLowerCase();
            if (version.startsWith("1.7") ||                                                        // NOI18N
                version.contains("version 1.7"))                                                    // NOI18N
            {
                retval = true;
            }
        }
        return retval;
    }

    private String getVersion() {
        ISVNClientAdapter adapter = createClient();
        if (adapter != null) {
            JhlClientAdapter jhlAdapter = (JhlClientAdapter) adapter;
            String version = jhlAdapter.getNativeLibraryVersionString();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "getVersion: version {0}", version);                            // NOI18N
            }
            return version;
        }
        return null;
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
                
        File location = InstalledFileLocator.getDefault().locate("modules/lib/libsvnjavahl-1.dll", JAVAHL_WIN32_MODULE_CODE_NAME, false);
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
        Preferences prefs = org.openide.util.NbPreferences.root ().node ("org/netbeans/modules.subversion");
        
        String executablePath = prefs.get("svnExecBinary", "");
        if(executablePath == null || executablePath.trim().equals("")) {
            return null;
        }
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
}