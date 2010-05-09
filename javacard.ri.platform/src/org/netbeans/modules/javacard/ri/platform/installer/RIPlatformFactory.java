/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.ri.platform.installer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.ri.platform.MergeProperties;
import org.netbeans.modules.javacard.ri.platform.RIPlatform;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.propdos.AntStyleResolvingProperties;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Class which takes a Java Card RI platform directory and creates a
 * Java Platform in the IDE for it, initializes default device, etc.
 *
 * @author Tim Boudreau
 */
public final class RIPlatformFactory implements Mutex.ExceptionAction<FileObject> {

    private final EditableProperties platformProps;
    private final EditableProperties deviceSettings;
    private final FileObject baseDir;
    private final ProgressHandle h;
    private final String displayName;
    private FileObject platformFile;
    private final FileObject platformsFolder = FileUtil.getConfigFile(
            CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER); //NOI18N
    private final EditableProperties globalProps = PropertyUtils.getGlobalProperties();
    public static final SpecificationVersion MINIMUM_SUPPORTED_VERSION =
            new SpecificationVersion("3.0.2"); //NOI18N

    /**
     * Create a platform factory which can create a Java Card platform the IDE
     * can recognize.
     * @param info A PlatformInfo object created by the wizard
     * @param deviceSettings User-entered settings which override defaults
     * for the default device.
     * @param baseDir The directory on disk - the root of the SDK
     * @param h A progress handle, or null
     * @param displayName The display name the created platform should get
     * in the UI
     */
    RIPlatformFactory(PlatformInfo info, EditableProperties deviceSettings, FileObject baseDir, ProgressHandle h, String displayName) {
        this(info.writeTo(new EditableProperties(true)), deviceSettings, baseDir, h, displayName);
    }

    /**
     * Create a platform factory which can create a Java Card platform the
     * IDE can recognize.
     *
     * @param platformProps Properties read from a platform.properties file in
     * the root of an RI install or RI-like SDK
     * @param deviceSettings User-entered settings, such as from a wizard,
     * which override the default settings (memory size, etc.) for the default
     * device for this card.  May be null.
     * @param baseDir The directory of the Java Card platform.  May not be null.
     * @param h A ProgressHandle if this task is being run from the new
     * platform wizard, or null if not
     * @param displayName The display name for this platform.  May not be null.
     */
    public RIPlatformFactory(EditableProperties platformProps, EditableProperties deviceSettings, FileObject baseDir, ProgressHandle h, String displayName) { //public for unit tests
        Parameters.notNull("platformProps", platformProps); //NOI18N
        Parameters.notNull("baseDir", baseDir); //NOI18N
        Parameters.notNull("displayName", displayName); //NOI18N
        this.platformProps = platformProps;
        this.baseDir = baseDir;
        this.h = h;
        this.displayName = displayName;
        assert platformsFolder != null;
        this.deviceSettings = deviceSettings;
        platformProps.put (JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME,
                displayName);
    }

    /**
     * Create a platform factory which can create a Java Card platform the
     * IDE can recognize.
     *
     * @param baseDir The directory of the SDK install.  This directory must
     * contain a file called platform.properties in properties format, with
     * a number of required keys/values.
     * @param displayName The display name the resulting platform should get
     * in NetBeans UI
     * @throws IOException if something goes wrong reading the platform file
     */
    RIPlatformFactory (FileObject baseDir, String displayName) throws IOException {
        this (readPlatformProperties(baseDir), null, baseDir, null, displayName);
    }

    public static boolean canInstall (String javacardVersion) {
        SpecificationVersion v = new SpecificationVersion(javacardVersion);
        return v.compareTo(MINIMUM_SUPPORTED_VERSION) >= 0;
    }

    public static boolean canInstall (Map<? extends Object, ? extends Object> m) {
        Object o = m.get(JavacardPlatformKeyNames.PLATFORM_JAVACARD_VERSION);
        return o == null ? false : canInstall(o.toString());
    }

    public FileObject createPlatform() throws IOException {
        try {
            try {
                ProjectManager.mutex().writeAccess(this);
                return platformFile;
            } catch (MutexException ex) {
                IOException ioe = new IOException("Exception creating " + //NOI18N
                        "platform in " + baseDir.getPath()); //NOI18N
                ioe.initCause(ex);
                throw ioe;
            } finally {
                if (h != null) {
                    h.finish();
                }
            }
        } catch (IOException ioe) {
            if (platformFile != null && platformFile.isValid()) {
                platformFile.delete();
            }
            throw ioe;
        }
    }

    public FileObject run() throws Exception {
        FileUtil.getConfigRoot().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                platformFile = create();
            }
        });
        return platformFile;
    }

    private FileObject create() throws IOException {
        progress();
        if (!canInstall(platformProps)) {
            throw new IOException (NbBundle.getMessage(RIPlatformFactory.class,
                    "ERR_TOO_OLD", //NOI18N
                    platformProps.get(JavacardPlatformKeyNames.PLATFORM_JAVACARD_VERSION),
                    MINIMUM_SUPPORTED_VERSION)); //NOI18N
        }
        //Add properties omitted from the JC 3.0.2 platform.properties, if
        //not present
        //XXX check if is RI? or if not wrapper?
        addMissingPropertiesJC302();
        //Translate UNIX-style relative paths into platform-specific absolute
        //paths usable by Ant
        translatePaths(FileUtil.toFile(baseDir), platformProps);
        hackAntTasksJarFor302();
        //Get the file name.  The first one installed is always called
        //javacard_default
        String filename = getPlatformFileName();
        progress();
        //Write the file name into the file, as an optimization so we don't
        //have to iterate all platform DataObjects to find the one for this
        //JavacardPlatform
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_ID, filename);

        progress();
        //Create the file
        FileObject fo = platformFile = platformsFolder.createData(filename,
                JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION); //NOI18N
        progress();
        //Create the folder for eprom files and write its path into the platform props
        createAndStoreEepromFolder(filename);
        progress();
        if (!platformProps.containsKey(JavacardPlatformKeyNames.PLATFORM_RI_HOME) && JavacardPlatformKeyNames.PLATFORM_KIND_RI.equals(platformProps.get(JavacardPlatformKeyNames.PLATFORM_KIND))) {
            File f = FileUtil.toFile(baseDir);
            String path = f == null ? /* unit tests */ baseDir.getPath() : f.getAbsolutePath();
            platformProps.put (JavacardPlatformKeyNames.PLATFORM_RI_HOME, path);
        }

        //If this platform wrappers the RI, create a properties that merges
        //values from that.  append.* and prepend.* will be resolved before
        //save;  ${}-delimited properties will be preserved
        ObservableProperties workingProps = maybeLoadDefaultPlatformProps();
        //We're done, store the platform properties to disk
        save(workingProps, fo);
        progress();
        //Try to keep a usable value for the default RI.  Some platform
        //implementations will be wrappers for it and need a valid path to it
        if (JavacardPlatformKeyNames.PLATFORM_KIND_RI.equals(platformProps.getProperty(JavacardPlatformKeyNames.PLATFORM_KIND))) {
            storeGlobalJavacardRIPath(fo);
        }
        //Make a new folder to store device/card configuration info
        FileObject cardsFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(filename, true);
        progress();
        String ext = platformProps.getProperty(JavacardPlatformKeyNames.PLATFORM_DEVICE_FILE_EXTENSION);
        //Get the file name for our device file
        String deviceFileName = findDeviceFileName (cardsFolder, ext);
        progress();
        //Get our device properties, as follows:
        // - Look for prototype.javacard.device.* in platform.properties, and
        //   if present, copy them minus "prototype." into the device props
        // - If no prototypes, use ServerTemplate.jcard, the fallback
        // - Overwrite any properties with user-entered values from the
        //   wizard, stored in this.deviceSettings
        EditableProperties deviceProps = loadDeviceProperties(deviceFileName);
        progress();
        //Create the device file and write it to disk
        FileObject deviceFile = cardsFolder.createData(deviceFileName, ext);
        save (deviceProps, deviceFile);
        progress();
        File f = FileUtil.toFile (fo);
        String path = f == null /* unit test */ ? "NONE" : f.getAbsolutePath(); //NOI18N
        //Set a property for this platform in $USERDIR/build.properties
        globalProps.setProperty(JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX
                + filename, path); //NOI18N
        //e.g. jcplatform.platform_default.devicepath
        //Set a global device path for this platform too
        String globalPlatformPointerProperty = JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX
                + filename + JCConstants.GLOBAL_PROPERTIES_DEVICE_FOLDER_PATH_KEY_SUFFIX;

        f = FileUtil.toFile (cardsFolder);
        path = f == null /* unit test */ ? "NONE" : f.getAbsolutePath(); //NOI18N
        //And save that to the global properties
        globalProps.setProperty(globalPlatformPointerProperty, path);
        //Store the global properties to disk and we're done
        PropertyUtils.putGlobalProperties(globalProps);
        progress();
        return fo;
    }

    private int step;
    private static final String JC_302_DEBUG_PROXY_CP = 
            "lib/api_connected.jar:" + //NOI18N
            "lib/api.jar:" + //NOI18N
            "lib/romizer.jar:" + //NOI18N
            "lib/tools.jar:" + //NOI18N
            "lib/asm-all-3.1.jar:" + //NOI18N
            "lib/bcel-5.2.jar:" + //NOI18N
            "lib/commons-logging-1.1.jar:" + //NOI18N
            "lib/commons-httpclient-3.0.jar:" + //NOI18N
            "lib/commons-codec-1.3.jar:" + //NOI18N
            "lib/commons-cli-1.0.jar:" + //NOI18N
            "lib/ant-contrib-1.0b3.jar"; //NOI18N
    /**
     * Add necessary properties missing from the Java Card 3.0.2 platform.properties -
     * will be corrected in next release.
     */
    private final void addMissingPropertiesJC302() {
        if (!platformProps.containsKey(JavacardPlatformKeyNames.PLATFORM_DEBUG_PROXY)) {
            FileObject fo = baseDir.getFileObject("bin/debugproxy.bat");
            if (fo == null) {
                fo = baseDir.getFileObject("bin/debugproxy");
            }
            if (fo == null) {
                fo = baseDir.getFileObject("bin/debugproxy.sh");
            }
            if (fo != null) {
                File file = FileUtil.toFile(fo);
                String path = file == null ? fo.getPath() /* unit tests */ : file.getAbsolutePath();
                platformProps.put (JavacardPlatformKeyNames.PLATFORM_DEBUG_PROXY, path);
            }
        }
        if (!platformProps.containsKey(JavacardPlatformKeyNames.PLATFORM_DEBUG_PROXY_CLASSPATH)) {
            platformProps.put(JavacardPlatformKeyNames.PLATFORM_DEBUG_PROXY_CLASSPATH, JC_302_DEBUG_PROXY_CP);
        }
    }

    public static final String ANT_TASKS_302_JAR_PATH = "javacard302/anttasks.jar"; //NOI18N
    public boolean antTasksUpdated; //unit tests
    private final void hackAntTasksJarFor302() {
        //DELETE THIS METHOD ONCE 3.0.2 NO LONGER SUPPORTED

        //For JavaCard 3.0.2 only, we bundle our own copy of the Java Card Ant tasks.
        //This method will find all path-like properties and replace any references
        //to lib/nbtasks.jar with references to the copy of the file we are bundling

        //Note that this method must be called *after* translatePaths() - it expects
        //platform-specific, not platform-independent path properties
        String ver = platformProps.getProperty(JavacardPlatformKeyNames.PLATFORM_RI_VERSION);
        boolean upgrade = ver != null && "3.0.2".equals(ver.trim()) && !platformProps.containsKey(JavacardPlatformKeyNames.PLATFORM_302_ANT_TASKS_UPDATED);
        Logger log = Logger.getLogger(RIPlatformFactory.class.getName());
        if (upgrade) {
            File antTasksJar = InstalledFileLocator.getDefault().locate(ANT_TASKS_302_JAR_PATH, "org.netbeans.modules.javacard.ri.platform", false); //NOI18N
            if (antTasksJar != null) {
                antTasksUpdated = true;
                platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_302_ANT_TASKS_UPDATED, "" + Boolean.TRUE);
                for (String key : JavacardPlatformKeyNames.getPathPropertyNames(platformProps)) {
                    String path = platformProps.getProperty(key);
                    if (path != null) {
                        String nue = upgradePath (path, antTasksJar, log);
                        if (nue != null && !nue.equals(path)) {
                            log.log (Level.INFO, "Upgrade {0} from {1} to {2}", new Object[]{key, path, nue});
                            platformProps.setProperty (key, nue);
                        }
                    }
                }
            } else {
                log.log(Level.WARNING,
                        "Could not upgrade Ant tasks for {0} because " + //NOI18N
                        "Ant tasks JAR is missing",  //NOI18N
                        new Object[] { this.baseDir.getPath() });
            }
        }
    }

    final String upgradePath (String path, File antTasksJar, Logger log) {
        if (path != null && path.length() > 0) {
            FileObject origTasksJar = baseDir.getFileObject("lib/nbtasks.jar");  //NOI18N
            if (origTasksJar != null) {
                File f = FileUtil.toFile (origTasksJar);
                if (f != null) {
                    String abs = f.getAbsolutePath();
                    if (path.indexOf(abs) < 0) {
                        return path;
                    }
                    String antTasksPath = antTasksJar.getAbsolutePath();
                    Pattern p = Pattern.compile (File.pathSeparator, Pattern.LITERAL);
                    String[] components = p.split(path);
                    StringBuilder nue = new StringBuilder();
                    for (String pth : components) {
                        boolean match = abs.equals(pth);
                        if (match) {
                            pth = antTasksPath;
                        }
                        if (nue.length() > 0) {
                            nue.append (File.pathSeparator);
                        }
                        nue.append(pth);
                    }
                    return nue.toString();
                } else {
                    log.log (Level.WARNING, "lib/nbtasks.jar exists but not a " + //NOI18N
                            "regular file: {0}", origTasksJar.getPath()); //NOI18N
                }
            } else {
                log.log(Level.WARNING, "lib/nbtasks.jar missing under {0}", new Object[] { baseDir.getPath() });
            }
        }
        return path;
    }

    private void progress() {
        if (h != null) {
            if (step == 0) {
                h.setInitialDelay(20);
                h.setDisplayName(NbBundle.getMessage(JavacardPlatformWizardIterator.class,
                        "PROGRESS_CREATING_PLATFORM")); //NOI18N
                h.start(14);
            }
            step++;
            h.progress(step);
        }
    }

    private String getPlatformFileName() {
        boolean hasOtherPlatforms = false;
        for (FileObject fo : platformsFolder.getChildren()) {
            hasOtherPlatforms = JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(fo.getExt());
            if (hasOtherPlatforms) {
                break;
            }
        }
        final String fname = !hasOtherPlatforms
                ? JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME
                : displayName.replace(' ', '_'); //NOI18N
        final String filename = FileUtil.findFreeFileName(platformsFolder, fname,
                JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
        return filename;
    }

    static void save(EditableProperties p, final FileObject to) throws IOException {
        FileLock lock = to.lock();
        OutputStream out = to.getOutputStream(lock);
        try {
            p.store(out); //NOI18N
        } finally {
            lock.releaseLock();
            out.close();
        }
    }

    static void save(ObservableProperties p, final FileObject to) throws IOException {
        FileLock lock = to.lock();
        OutputStream out = to.getOutputStream(lock);
        try {
            p.store(out, null); //NOI18N
        } finally {
            lock.releaseLock();
            out.close();
        }
    }

    private void storeGlobalJavacardRIPath(FileObject fo) {
        String val = globalProps.getProperty(JCConstants.GLOBAL_BUILD_PROPERTIES_RI_PROPERTIES_PATH_KEY);
        if (val != null) {
            File f = new File(val);
            if (!f.exists() || !f.isDirectory()) {
                val = null;
            }
        }
        if (val != null) {
            File file = FileUtil.toFile(fo);
            globalProps.setProperty(JCConstants.GLOBAL_BUILD_PROPERTIES_RI_PROPERTIES_PATH_KEY, file.getAbsolutePath());
        }
    }

    private boolean addPrototypeValues(EditableProperties deviceProps) {
        return addPrototypeValues(deviceProps, platformProps);
    }

    static boolean addPrototypeValues(EditableProperties deviceProps, EditableProperties platformProps) {
        boolean result = false;
        for (Map.Entry<String, String> e : platformProps.entrySet()) {
            String key = e.getKey();
            if (key.startsWith(JCConstants.PLATFORM_FILE_DEVICE_PROTOTYPE_PREFIX)) {
                result = true;
                key = key.substring(JCConstants.PLATFORM_FILE_DEVICE_PROTOTYPE_PREFIX.length());
                deviceProps.put(key, e.getValue());
            }
        }
        return result;
    }

    private void translatePaths(File dir, EditableProperties props) {
        Set<String> translatablePaths = JavacardPlatformKeyNames.getPathPropertyNames(props);
        Set<String> copy = new HashSet<String>(translatablePaths);
        for (String s : copy) {
            translatablePaths.add(MergeProperties.APPEND_PREFIX + s);
            translatablePaths.add(MergeProperties.PREPEND_PREFIX + s);
        }
        for (String key : new ArrayList<String>(props.keySet())) {
            String val = props.getProperty(key);
            if (translatablePaths.contains(key)) {
                String xlated = translatePath(dir, val);
                props.put(key, xlated);
            }
        }
    }

    static String translatePath(File dir, String val) {
        if (val.startsWith("${")) { //NOI18N
            //If it starts with an inline property, assume this is
            //something that resolves to a parent dir, don't assume
            //we need to add one
            val = val.replace(':', File.pathSeparatorChar);
            val = val.replace('/', File.separatorChar);
            return val;
        }
        if ("".equals(val) || val == null) { //NOI18N
            return ""; //NOI18N
        }
        if (".".equals(val) || "./".equals(val)) { //NOI18N
            return dir.getAbsolutePath();
        }
        if (val.startsWith("./")) { //NOI18N
            val = val.substring(2);
        }
        if (File.separatorChar != '/' && val.indexOf("/") >= 0) { //NOI18N
            val = val.replace('/', File.separatorChar); //NOI18N
        }
        if (val.indexOf(':') >= 0) { //NOI18N
            String[] paths = val.split(":"); //NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                if (sb.length() > 0) {
                    sb.append(File.pathSeparatorChar);
                }
                sb.append(translatePath(dir, path));
            }
            return sb.toString();
        }
        File nue = new File(dir, val);
        String result = nue.getAbsolutePath();
        return result;
    }

    private EditableProperties loadDeviceProperties(String deviceFileName) throws IOException {
        EditableProperties deviceProps = new EditableProperties(true);
        //Use template only if non prototype.* keys defined
        if (!addPrototypeValues(deviceProps)) {
            FileObject deviceTemplate = FileUtil.getConfigFile(CommonSystemFilesystemPaths.SFS_DEVICE_TEMPLATE_PATH);
            if (deviceTemplate != null) {
                InputStream in = new BufferedInputStream(deviceTemplate.getInputStream());
                try {
                    deviceProps.load(in);
                } finally {
                    in.close();
                }
            } else {
                Logger.getLogger(RIPlatformFactory.class.getName()).log(Level.WARNING,
                        "Could not find device template " + deviceFileName + //NOI18N
                        " in SFS"); //NOI18N
            }
        }
        progress();
        deviceProps.put(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME, deviceFileName);
        if (this.deviceSettings != null) {
            deviceProps.putAll(this.deviceSettings);
        }
        return deviceProps;
    }

    private String findDeviceFileName(FileObject cardsFolder, String ext) {
        //Now create a default device template
        String deviceFileName = JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME;
        progress();
        deviceFileName = FileUtil.findFreeFileName(cardsFolder, deviceFileName,
                ext);
        return deviceFileName;
    }

    private void createAndStoreEepromFolder(String filename) {
        FileObject eepromFolder = Utils.sfsFolderForDeviceEepromsForPlatformNamed(filename, true);
        File f = FileUtil.toFile(eepromFolder);
        if (f != null) { //unit test
            platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_EEPROM_FOLDER,
                FileUtil.toFile(eepromFolder).getAbsolutePath());
        }
    }

    private static void read (EditableProperties to, FileObject from) throws IOException {
        BufferedInputStream in = new BufferedInputStream(from.getInputStream());
        try {
            to.load(in);
        } finally {
            in.close();
        }
    }

    private static EditableProperties readPlatformProperties(FileObject baseDir) throws IOException {
        EditableProperties result = new EditableProperties(true);
        FileObject platformDefinition = baseDir.getFileObject(JCConstants.PLATFORM_PROPERTIES_PATH);
        if (platformDefinition == null) {
            throw new IOException ("No platform definition at path " + //NOI18N
                    JCConstants.PLATFORM_PROPERTIES_PATH + " in " + //NOI18N
                    baseDir.getPath());
        }
        read (result, platformDefinition);
        return result;
    }

    private ObservableProperties maybeLoadDefaultPlatformProps() throws IOException {
        AntStyleResolvingProperties p = new AntStyleResolvingProperties(true);
        p.putAll(this.platformProps);
        String riWrapperProp = platformProps.getProperty(JavacardPlatformKeyNames.PLATFORM_IS_RI_WRAPPER);
        if (riWrapperProp != null && "true".equals(riWrapperProp) || "yes".equals(riWrapperProp)) { //NOI18N
            DataObject defPlatform = RIPlatform.findDefaultPlatform(null);
            if (defPlatform == null) {
                throw new IOException("No copy of the Java Card RI installed"); //NOI18N
            }
            File defPlatformFile = FileUtil.toFile(defPlatform.getPrimaryFile());
            JavacardPlatform defPform = defPlatform.getLookup().lookup(RIPlatform.class);
            if (defPform == null && !Boolean.getBoolean("PlatformDataObjectTest")) { //NOI18N
                throw new IOException ("Default Platform is not an instance of RIPlatform"); //NOI18N
            } else if (Boolean.getBoolean("PlatformDataObjectTest")) { //XXX get this out of here
                Iterable<DataObject> i = Utils.findAllRegisteredJavacardPlatformDataObjects();
                if (i.iterator().hasNext()) {
                    for (DataObject d : i) {
                        if (d.getLookup().lookup(JavacardPlatform.class) != null) {
                            defPform = d.getLookup().lookup(JavacardPlatform.class);
                        }
                    }
                }
            }
            p.put(JavacardPlatformKeyNames.PLATFORM_RI_PROPERTIES_PATH, defPlatformFile.getAbsolutePath());
            if (defPform instanceof RIPlatform) {
                p.put(JavacardPlatformKeyNames.PLATFORM_RI_HOME, ((RIPlatform) defPform).getHome().getAbsolutePath());
            }
            
            PropertiesAdapter adap = defPlatform.getLookup().lookup(PropertiesAdapter.class);
            assert adap != null : "No properties adapter from default javacard platform"; //NOI18N
            p = new MergeProperties(adap.asProperties(), p);
        }
        return p;
    }

    public static RIPlatform createPlatform (ObservableProperties p, DataObject caller) throws IOException {
//        String riWrapperProp = p.getProperty(JavacardPlatformKeyNames.PLATFORM_IS_RI_WRAPPER);
//        if (riWrapperProp != null && "true".equals(riWrapperProp) || "yes".equals(riWrapperProp)) { //NOI18N
//            System.err.println("Creating a wrapper platform for " + p.getProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME));
//            DataObject defPlatform = RIPlatform.findDefaultPlatform(caller);
//            if (defPlatform == null) {
//                throw new IOException("No copy of the Java Card RI installed"); //NOI18N
//            }
//            PropertiesAdapter adap = defPlatform.getLookup().lookup(PropertiesAdapter.class);
//            assert adap != null : "No properties adapter from default javacard platform"; //NOI18N
//            p = new MergeProperties(adap.asProperties(), p);
//        }
        return new RIPlatform(p);
    }
}
