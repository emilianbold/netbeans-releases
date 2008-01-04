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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.ruby.platform;

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents one Ruby platform, i.e. installation of a Ruby interpreter.
 */
public final class RubyPlatform {

    private static final Logger LOGGER = Logger.getLogger(RubyPlatform.class.getName());
    
    private static final String KEY_RUBY = "ruby"; //NOI18N
    public static final String DEFAULT_RUBY_RELEASE = "1.8"; // NOI18N

    /** Version number of the rubystubs */
    private static final String RUBYSTUBS_VERSION = "1.8.6-p110"; // NOI18N

    private final String id;
    private final String interpreter;
    private final String label;
    private File home;
    private String homeUrl;
    private String rubylib;
    private FileObject libFO;
    private GemManager gemManager;
    private FileObject stubsFO;
    private boolean indexInitialized;
    
    private PropertyChangeSupport pcs;

    private RubyPlatform(final String id, final String interpreterPath) {
        this(id, interpreterPath, new File(interpreterPath).getName());
    }

    public RubyPlatform(final String id, final String interpreterPath, final String label) {
        this.id = id;
        this.interpreter = interpreterPath;
        this.label = label;
    }

    public static RubyPlatform platformFor(final Project project) {
        RubyPlatformProvider rpp = project.getLookup().lookup(RubyPlatformProvider.class);
        return rpp == null ? null : rpp.getPlatform();
    }

    /** Utility method. */
    public static GemManager gemManagerFor(final Project project) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        return platform == null ? null : platform.getGemManager();
    }

    /**
     * Checks whether project has a valid platform and in turn whether the
     * platform has a valid Rake installed.
     *
     * @param warn whether to show warning message to the user if ther is no
     *        valid Rake installed
     */
    public static boolean hasValidRake(final Project project, final boolean warn) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (platform == null) {
            if (warn) {
                showWarning(project);
            }
            return false;
        }
        return platform.isValidRuby(warn) && platform.getGemManager().isValidRake(warn);
    }
    
    public String getID() {
        return id;
    }

    public String getInterpreter(boolean cannonical) {
        String result = interpreter;
        if (cannonical) {
            try {
                result = new File(interpreter).getCanonicalFile().getAbsolutePath();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot get canonical path", e);
            }
        }
        
        updateIndexRoots();
        
        return result;
    }

    public String getInterpreter() {
        updateIndexRoots();
        
        return interpreter;
    }

    public File getInterpreterFile() {
        updateIndexRoots();
        
        return new File(interpreter);
    }

    public File getHome() {
        return getHome(true);
    }

    public File getHome(boolean canonical) {
        if (home == null) {
            try {
                String rp = getInterpreter(canonical);
                if (rp == null) {
                    return null;
                }
                File r = new File(rp);

                // Handle bogus paths like "/" which cannot possibly point to a valid ruby installation
                File p = r.getParentFile();
                if (p == null) {
                    return null;
                }

                p = p.getParentFile();
                if (p == null) {
                    return null;
                }

                home = p.getCanonicalFile();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }
        return home;
    }

    public String getHomeUrl() {
        if (homeUrl == null) {
            try {
                File r = getHome();
                if (r != null) {
                    homeUrl = r.toURI().toURL().toExternalForm();
                }
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            }
        }

        return homeUrl;
    }

    /** Return the lib directory for this interprerter. */
    public String getLib() {
        File home = getHome();
        if (home == null) {
            return null;
        }
        File lib = new File(home, "lib"); // NOI18N

        if (lib.exists() && new File(lib, "ruby").exists()) { // NOI18N
            try {
                return lib.getCanonicalPath();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        throw new AssertionError("'lib/ruby' cannot be resolved for '" + interpreter + "' interpreter");
    }

    public FileObject getLibFO() {
        if (libFO == null) {
            String lib = getLib();

            if (lib != null) {
                libFO = FileUtil.toFileObject(new File(lib));
            }
        }

        return libFO;
    }

    /**
     * Find the Ruby-specific library directory for the chosen Ruby. This is
     * usually something like /foo/bar/lib/1.8/ (where Ruby was
     * /foo/bar/bin/ruby) but it tries to work with other versions of Ruby as
     * well (such as 1.9).
     */
    public String getLibDir() {
        if (rubylib == null) {
            File home = getHome();
            assert home != null : "home not null";

//            if (home == null) {
//                String jrubyLib = getJRubyLib();
//                if (jrubyLib != null) {
//                    return jrubyLib + File.separator + "ruby" + File.separator + // NOI18N
//                        DEFAULT_RUBY_RELEASE;
//                } else {
//                    return null;
//                }
//            }

            File lib = new File(home, "lib" + File.separator + "ruby"); // NOI18N

            if (!lib.exists()) {
                return null;
            }

            File f = new File(lib, DEFAULT_RUBY_RELEASE); // NOI18N

            if (f.exists()) {
                rubylib = f.getAbsolutePath();
            } else {
                // Search for a numbered directory
                File[] children = lib.listFiles();

                for (File c : children) {
                    if (!c.isDirectory()) {
                        continue;
                    }

                    String name = c.getName();

                    if (name.matches("\\d+\\.\\d+")) { // NOI18N
                        rubylib = c.getAbsolutePath();

                        break;
                    }
                }
            }

            assert rubylib != null : "rubylib not null";
//            if (rubylib == null) {
//                rubylib = getJRubyLib();
//            }
        }

        return rubylib;
    }

    /** Return the site_ruby directory for the current ruby installation. Not cached. */
    public String getRubyLibSiteDir() {
        String sitedir = null;
        File home = getHome();
        assert home != null : "home not null";

//        if (home == null) {
//            File siteDir =
//                    new File(getJRubyLib() + File.separator + "ruby" + File.separator + "site_ruby" + // NOI18N
//                    File.separator + DEFAULT_RUBY_RELEASE);
//
//            if (siteDir.exists()) {
//                return siteDir.getAbsolutePath();
//            } else {
//                return null;
//            }
//        }

        File lib =
                new File(home, "lib" + File.separator + "ruby" + File.separator + "site_ruby"); // NOI18N

        if (!lib.exists()) {
            return null;
        }

        File f = new File(lib, DEFAULT_RUBY_RELEASE); // NOI18N

        if (f.exists()) {
            sitedir = f.getAbsolutePath();
        } else {
            // Search for a numbered directory
            File[] children = lib.listFiles();

            for (File c : children) {
                if (!c.isDirectory()) {
                    continue;
                }

                String name = c.getName();

                if (name.matches("\\d+\\.\\d+")) { // NOI18N
                    sitedir = c.getAbsolutePath();

                    break;
                }
            }

            if ((sitedir == null) && (children.length > 0)) {
                sitedir = children[0].getAbsolutePath();
            }
        }

        return sitedir;
    }

    public boolean isValidRuby(boolean warn) {
        String rp = getBinDir();
        boolean valid = false;
        if (rp != null) {
            File file = new File(rp);
            valid = file.exists() && getHome() != null;
        }

        if (warn && !valid) {
            showWarning(this);
        }

        return valid;
    }

    private static void showWarning(final RubyPlatform platform) {
        String msg = NbBundle.getMessage(RubyInstallation.class, "InvalidRubyPlatform", platform.getLabel());
        JButton closeButton = getCloseButton();

        Object[] options = new Object[]{closeButton};
        showDialog(msg, options);
    }
    
    private static void showWarning(final Project project) {
        String msg =
                NbBundle.getMessage(RubyInstallation.class, "InvalidRubyPlatformForProject",
                ProjectUtils.getInformation(project).getDisplayName());
        JButton closeButton = getCloseButton();

        CustomizerProvider customizer = project.getLookup().lookup(CustomizerProvider.class);
        Object[] options;
        JButton propertiesButton =
                new JButton(NbBundle.getMessage(RubyInstallation.class, "Properties"));
        if (customizer != null) {
            options = new Object[]{propertiesButton, closeButton};
        } else {
            options = new Object[]{closeButton};
            
        }
        if (showDialog(msg, options) == propertiesButton) {
            customizer.showCustomizer();
        }
    }

    private static Object showDialog(String msg, Object[] options) {
        DialogDescriptor descriptor =
                new DialogDescriptor(msg,
                NbBundle.getMessage(RubyInstallation.class, "MissingRuby"), true, options,
                options[0],
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RubyInstallation.class), null);
        descriptor.setMessageType(NotifyDescriptor.Message.ERROR_MESSAGE);
        descriptor.setModal(true);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return descriptor.getValue();
    }

    private static JButton getCloseButton() {
        JButton closeButton =
                new JButton(NbBundle.getMessage(RubyInstallation.class, "CTL_Close"));
        closeButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(RubyInstallation.class, "AD_Close"));
        return closeButton;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDefault() {
        return interpreter.equals(RubyPlatformManager.getDefaultPlatform().getInterpreter());
    }

    public boolean isJRuby() {
        return RubyInstallation.isJRuby(interpreter);
    }

    public boolean isValid() {
        return new File(interpreter).isFile();
    }

    public GemManager getGemManager() {
        if (gemManager == null) {
            gemManager = new GemManager(this);
        }
        return gemManager;
    }

    public String getBinDir() {
        return getBinDir(true);
    }

    public String getBinDir(boolean canonical) {
        String rubybin = null;
        String r = getInterpreter(canonical);

        if (r != null) {
            rubybin = new File(r).getParent();
        }
        return rubybin;
    }

//    public String getRuby() {
//        return getRuby(true);
//    }
//
//    public String getRuby(boolean canonical) {
//        if (ruby == null) {
//            // Test and preindexing hook
//            ruby = System.getProperty("ruby.interpreter");
//
//            if (ruby == null) { // Usually the case
//                ruby = Util.getPreferences().get(KEY_RUBY, null);
//
//                if (ruby == null) {
//                    ruby = chooseRuby();
//                    if (ruby != null) {
//                        Util.getPreferences().put(KEY_RUBY, ruby);
//                    }
//                }
//
//                if (ruby == null || ruby.equals("jruby")) { // NOI18N
//                    ruby = getJRuby();
//                }
//            }
//
//            // Let RepositoryUpdater and friends know where they can root preindexing
//            // This should be done in a cleaner way.
//            if (ruby != null) {
//                org.netbeans.modules.gsfret.source.usages.Index.setPreindexRootUrl(getRubyHomeUrl());
//            }
//        }
//
//        if (ruby != null && canonical) {
//            try {
//                return new File(ruby).getCanonicalFile().getAbsolutePath();
//            } catch (IOException e) {
//                LOGGER.log(Level.WARNING, "Cannot get canonical path", e);
//            }
//        }
//        return ruby;
//    }

//    private String chooseRuby() {
//        // Check the path to see if we find any other Ruby installations
//        String path = System.getenv("PATH"); // NOI18N
//        if (path == null) {
//            path = System.getenv("Path"); // NOI18N
//        }
//        
//        if (path != null) {
//            final Set<String> rubies = new TreeSet<String>();
//            Set<String> dirs = new TreeSet<String>(Arrays.asList(path.split(File.pathSeparator)));
//            for (String dir : dirs) {
//                File f = null;
//                if (Utilities.isWindows()) {
//                    f = new File(dir, "ruby.exe"); // NOI18N
//                } else {
//                    f = new File(dir, "ruby"); // NOI18N
//                    // Don't include /usr/bin/ruby on the Mac - it's no good
//                    // Source: http://developer.apple.com/tools/rubyonrails.html
//                    //   "The version of Ruby that shipped on Mac OS X Tiger prior to 
//                    //    v10.4.6 did not work well with Rails.   If you're running 
//                    //    an earlier version of Tiger, you'll need to either upgrade 
//                    //    to 10.4.6 or upgrade your copy of Ruby to version 1.8.4 or 
//                    //    later using the open source distribution."
//                    if (Utilities.isMac() && "/usr/bin/ruby".equals(f.getPath())) { // NOI18N
//                        String version = System.getProperty("os.version"); // NOI18N
//                        if (version == null || version.startsWith("10.4")) { // Only a problem on Tiger // NOI18N
//                            continue;
//                        }
//                    }
//                }
//                if (f.exists()) {
//                    try {
//                        rubies.add(f.getCanonicalPath());
//                    } catch (IOException e) {
//                        LOGGER.log(Level.WARNING, "Cannot resolve cannonical path for: " + f, e);
//                        rubies.add(f.getPath());
//                    }
//                }
//            }
//            
//            if (rubies.size() > 0) {
//                if (SwingUtilities.isEventDispatchThread()) {
//                    return askForRuby(rubies);
//                } else {
//                    SwingUtilities.invokeLater(new Runnable() {
//                        public void run() {
//                            String chosen = askForRuby(rubies);
//                            if (chosen != null && !chosen.equals("jruby") && !chosen.equals(ruby)) { // NOI18N
//                                setRuby(chosen);
//                            }
//                        }
//                    });
//                }
//                
//            } else {
//                // No rubies found - just default to using the bundled JRuby
//                return "jruby"; // NOI18N
//            }
//        }
//            
//        return "jruby"; // NOI18N
//    }

//    private String askForRuby(final Set<String> rubies) {
//        // Ruby found in the path -- offer to use it
//        String jrubyLabel = NbBundle.getMessage(RubyInstallation.class, "JRubyBundled");
//        String nativeRubyLabel = NbBundle.getMessage(RubyInstallation.class, "NativeRuby") + " "; 
//
//        List<String> displayList = new ArrayList<String>();
//        String jruby = getJRuby();
//        if (jruby != null) {
//            displayList.add(jrubyLabel);
//        }
//        for (String r : rubies) {
//            displayList.add(nativeRubyLabel + r);
//        }
//
//        ChooseRubyPanel panel = new ChooseRubyPanel(displayList);
//
//        DialogDescriptor descriptor = new DialogDescriptor(panel,
//                NbBundle.getMessage(RubyInstallation.class, "ChooseRuby"), true,
//                DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
//                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RubyInstallation.class), null);
//        descriptor.setMessageType(NotifyDescriptor.Message.INFORMATION_MESSAGE);
//
//        Dialog dlg = null;
//        descriptor.setModal(true);
//
//        try {
//            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
//            dlg.setVisible(true);
//        } finally {
//            if (dlg != null) {
//                dlg.dispose();
//            }
//        }
//
//        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) {
//            return "jruby"; // NOI18N
//        }
//        String displayItem = panel.getChosenInterpreter();
//        if (displayItem == null) {
//            // Force user to choose
//            displayRubyOptions();
//        } else {
//            if (displayItem.equals(jrubyLabel)) {
//                return "jruby"; // NOI18N
//            } else {
//                assert displayItem.startsWith(nativeRubyLabel);
//                String path = displayItem.substring(nativeRubyLabel.length());
//                
//                try {
//                    path = new File(path).getCanonicalPath();
//                } catch (IOException ioe) {
//                    Exceptions.printStackTrace(ioe);
//                }
//
//                return path;
//            }
//        }
//
//        return "jruby"; // NOI18N
//    }
    
    public FileObject getRubyStubs() {
        if (stubsFO == null) {
            // Core classes: Stubs generated for the "builtin" Ruby libraries.
            File clusterFile = InstalledFileLocator.getDefault().locate(
                    "modules/org-netbeans-modules-ruby-project.jar", null, false);

            if (clusterFile != null) {
                File rubyStubs =
                    new File(clusterFile.getParentFile().getParentFile().getAbsoluteFile(),
                        // JRUBY_RELEASEDIR + File.separator + 
                    "rubystubs" + File.separator + RUBYSTUBS_VERSION); // NOI18N
                assert rubyStubs.exists() && rubyStubs.isDirectory();
                stubsFO = FileUtil.toFileObject(rubyStubs);
            } else {
                // During test?
                String r = getInterpreter();
                if (r != null) {
                    FileObject fo = FileUtil.toFileObject(new File(r));
                    if (fo != null) {
                        stubsFO = fo.getParent().getParent().getParent().getFileObject("rubystubs/" + RUBYSTUBS_VERSION); // NOI18N
                    }
                }
            }
        }

        return stubsFO;
    }

    /*
     * Return non-null iff the given file is a "system" file - e.g. it's part of
     * the Ruby 1.8 libraries, or the Java support libraries, or the user's
     * rubygems, or something under $GEM_HOME...
     *
     * @return The actual root the in the system the file is under.
     */
    public FileObject getSystemRoot(FileObject file) {
        // See if the file is under the Ruby libraries
        FileObject rubyLibFo = getLibFO();
        FileObject rubyStubs = getRubyStubs();
        FileObject gemHome = gemManager != null ? gemManager.getRubyLibGemDirFo() : null;

        //        FileObject jar = FileUtil.getArchiveFile(file);
        //        if (jar != null) {
        //            file = jar;
        //        }

        while (file != null) {
            if (file == rubyLibFo || file == rubyStubs || file == gemHome) {
                return file;
            }

            file = file.getParent();
        }

        return null;
    }

//    public void setRuby(String ruby) {
//        File rubyF = new File(ruby);
//        ruby = rubyF.getAbsolutePath();
//        if (!ruby.equals(getRuby())) {
//            Util.getPreferences().put(KEY_RUBY, ruby);
//        }
//        if (!FileUtil.normalizeFile(rubyF).equals(FileUtil.normalizeFile(new File(getRuby())))) {
//            // reset only in case it is not a link to the same file
//            this.ruby = ruby;
//            // Recompute lazily:
//            this.rubylibFo = null;
//            this.rubyStubsFo = null;
//            this.jrubyJavaSupport = null;
//            //this.irb = null;
//            if (isValidRuby(false)) {
//                recomputeRoots();
//            }
//        }
//    }


    private void updateIndexRoots() {
        if (!indexInitialized) {
            indexInitialized = true;
            // HACK, fix soon
            // Let RepositoryUpdater and friends know where they can root preindexing
            // This should be done in a cleaner way.
            //org.netbeans.modules.gsfret.source.usages.Index.setPreindexRootUrl(getHomeUrl());

            org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(FileUtil.toFileObject(getHome(true)));

            String gemDir = getGemManager().getGemDir(true);
            if (gemDir != null) {
                FileObject gemFo = FileUtil.toFileObject(new File(gemDir));
                org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(gemFo);
            }

        }
    }
    
    /**
     * The gems installed have changed, or the installed ruby has changed etc. --
     * force a recomputation of the installed classpath roots.
     */
    public void recomputeRoots() {
        updateIndexRoots();

        // Ensure that source cache is wiped and classpaths recomputed for existing files
        Source.clearSourceCache();
        
        if (pcs != null) {
            pcs.firePropertyChange("roots", null, null); // NOI18N
        }

        //        // Force ClassIndex registration
        //        // Dummy class path provider just to trigger recomputation
        //        ClassPathProviderImpl cpProvider = new ClassPathProviderImpl(null, null, null, null);
        //        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.BOOT);
        //        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cps);
        //        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cps);

        // Possibly clean up index from old ruby root as well?
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }

        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(listener);
        }
    }
}
