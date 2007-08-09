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

package org.netbeans.modules.mobility.project;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.cldcplatform.PlatformConvertor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.Utils;
import org.netbeans.modules.mobility.project.deployment.CopyDeploymentPlugin;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.regex.Pattern;
import java.beans.PropertyVetoException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.util.NbBundle;
import org.netbeans.modules.mobility.project.ui.customizer.MIDletScanner;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.ConfigurationTemplateDescriptor;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 * Create a fresh J2MEProject from scratch.
 * Currently does not permit much to be specified - feel free to add more parameters
 * as needed.
 * @author Jesse Glick, Adam Sotona, David Kaspar
 */
public class J2MEProjectGenerator {
    
    public static final Pattern IMPORT_EXCLUDES = Pattern.compile("^(.*/)?(([^/]*~)|(#[^/]*#)|(\\.#[^/]*)|(%[^/]*%)|(\\._[^/]*)|(CVS)|(CVS/.*)|(\\.cvsignore)|(SCCS)|(SCCS/.*)|(vssver\\.scc)|(\\.svn)|(\\.svn/.*)|(\\.DS_Store)|([^/]*\\.class)|([^/]*\\.adContent)|([^/]*\\.jad)|([^/]*\\.nbattrs))$");//NOI18N
    public static final Pattern IMPORT_SRC_EXCLUDES = Pattern.compile("^(.*/)?(([^/]*~)|(#[^/]*#)|(\\.#[^/]*)|(%[^/]*%)|(\\._[^/]*)|(CVS)|(CVS/.*)|(\\.cvsignore)|(SCCS)|(SCCS/.*)|(vssver\\.scc)|(\\.svn)|(\\.svn/.*)|(\\.DS_Store)|([^/]*\\.class)|([^/]*\\.adContent)|([^/]*\\.jad)|([^/]*\\.jar)|([^/]*\\.zip)|([^/]*\\.nbattrs))$");//NOI18N
    public static final String DEFAULT_ENCODING = "UTF-8"; // NO I18N
    public static final String TRUE = "true"; //NOI18N
    public static final String FALSE = "false"; //NOI18N
    public static final String EMPTY = ""; //NOI18N
    private static final HashSet<String> KNOWN_ATTRIBUTES = new HashSet<String>(Arrays.asList(new String[] {
        "MIDlet-Name", "MIDlet-Vendor", "MIDlet-Version", "MIDlet-Icon", "MIDlet-Description", "MIDlet-Info-URL", //NOI18N
        "MIDlet-Data-Size", //NOI18N
        "MIDlet-Install-Notify", "MIDlet-Delete-Notify", "MIDlet-Delete-Confirm", //NOI18N
        "MicroEdition-Configuration", "MicroEdition-Profile", //NOI18N
    }));
    private static final String PRIVATE_PREFIX = "private."; //NOI18N
    private static final String SRC = "src";
    private static final String NAME = "name";
    private static final String MIDLET = "MIDlet-";
    
    private J2MEProjectGenerator() {
        //Just to avoid accessor class creation
    }
    
    public static AntProjectHelper createProjectFromSources(final File projectLocation, final String name, final PlatformSelectionPanel.PlatformDescription platform, final String sourcesLocation, final String jadLocation) throws IOException {
        return createProject(projectLocation, name, platform, new ProjectGeneratorCallback() {
            public void doPostGeneration(Project project, AntProjectHelper helper, @SuppressWarnings("unused") FileObject projectLocation, @SuppressWarnings("unused") File projectLocationFile, @SuppressWarnings("unused") ArrayList configurations) throws IOException {
                setSourceRoot(helper, getReferenceHelper(project), sourcesLocation);
                loadJadManifest(helper, new File(jadLocation));
                fillMissingMIDlets(project, helper);
            }
        });
    }
    
    public static AntProjectHelper createProjectFromSuite(final File projectLocation, final String name, final PlatformSelectionPanel.PlatformDescription platform, final String suite, final String sources) throws IOException {
        return createProject(projectLocation, name, platform, new ProjectGeneratorCallback() {
            public void doPostGeneration(Project project, AntProjectHelper helper, @SuppressWarnings("unused") FileObject projectLocation, @SuppressWarnings("unused") File projectLocationFile, @SuppressWarnings("unused") ArrayList configurations) throws IOException {
                setSourceRoot(helper, getReferenceHelper(project), sources);
                loadSettingFromSuite(helper, new File(suite));
                fillMissingMIDlets(project, helper);
            }
        });
    }
    
    public static AntProjectHelper createProjectFromWtkProject(final File projectLocation, final String name, final PlatformSelectionPanel.PlatformDescription platform, final String appLocation) throws IOException {
        return createProject(projectLocation, name, platform, new ProjectGeneratorCallback() {
            public void doPostGeneration(Project project, AntProjectHelper helper, @SuppressWarnings("unused") FileObject projectLocation, File projectLocationFile, @SuppressWarnings("unused") ArrayList configurations) throws IOException {
                final ReferenceHelper refHelper = getReferenceHelper(project);
                setSourceRoot(helper, refHelper, new File(appLocation, SRC).getAbsolutePath()); //NOI18N
                final File jad = findWtkJadFile(appLocation);
                final File mf = findWtkManifestFile(appLocation);
                loadJadAndManifest(helper, jad, mf);
                loadWTKProperties(helper, projectLocationFile);
                final File[] files = new File(appLocation, "lib").listFiles(); //NOI18N
                File[] libs = null;
                if (files == null) {
                    libs = new File[1];
                } else {
                    libs = new File[files.length + 1];
                    System.arraycopy(files, 0, libs, 1, files.length);
                }
                libs[0] = new File(appLocation, "res"); //NOI18N
                loadLibraries(helper, refHelper, libs);
                fillMissingMIDlets(project, helper);
            }
        });
    }
    
    public static AntProjectHelper duplicateProject(final J2MEProject oldProject, final File _projectLocation, final String name, final boolean copySources) throws IOException {
        final FileObject projectLocation = createProjectLocation(_projectLocation);
        final AntProjectHelper h = ProjectGenerator.createProject(projectLocation, J2MEProjectType.TYPE);
        final AntProjectHelper oldHelper = oldProject.getLookup().lookup(AntProjectHelper.class);
        final Element data = h.getPrimaryConfigurationData(true);
        final Document doc = data.getOwnerDocument();
        final Element nameEl = doc.createElementNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, NAME); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        final Element minant = doc.createElementNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        final EditableProperties ep = oldHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).cloneProperties();
        final EditableProperties ep2 = oldHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).cloneProperties();
        ep.setProperty(DefaultPropertiesDescriptor.SRC_DIR, SRC);  //NOI18N
        ep.setProperty(NAME, name);  //NOI18N
        final File projDir = FileUtil.toFile(projectLocation);
        assert projDir != null : "FileUtil.toFile convertion failed for: " + projectLocation;  //NOI18N
        for ( final String key : (Set<String>)ep.keySet() ) {
            if (key.startsWith("file.reference.") || key.startsWith("project.")) {//NOI18N
                final String pValue = ep.getProperty(key);
                if (pValue.indexOf("${") < 0) { //NOI18N
                    final File f = oldHelper.resolveFile(pValue);
                    String newPath;
                    if (CollocationQuery.areCollocated(projDir, f) && (newPath = PropertyUtils.relativizeFile(projDir, f)) != null) {
                        // Fine, using a relative path to subproject and store it back.
                        ep.put(key, newPath);
                    } else {
                        // Use an absolute path, remove the reference form project.properties and store it into private.properties.
                        ep.remove(key);
                        ep2.put(key, f.getAbsolutePath());
                    }
                }
            }
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep2);
        final Project prj = ProjectManager.getDefault().findProject(projectLocation);
        final ProjectConfigurationsHelper oldCfgHlp = oldProject.getLookup().lookup(ProjectConfigurationsHelper.class);
        final ProjectConfiguration cfgs[] = oldCfgHlp.getConfigurations().toArray(new ProjectConfiguration[0]);
        final ProjectConfigurationsHelper cfgHlp = prj.getLookup().lookup(ProjectConfigurationsHelper.class);
        for (int a = 0; a < cfgs.length; a++) {
            if (!oldCfgHlp.getDefaultConfiguration().equals(cfgs[a])) cfgHlp.addConfiguration(cfgs[a].getDisplayName());
        }
        final ReferenceHelper oldRefHelper = oldProject.getLookup().lookup(ReferenceHelper.class);
        final ReferenceHelper refHelper = prj.getLookup().lookup(ReferenceHelper.class);
        final ReferenceHelper.RawReference rawRef[] = oldRefHelper.getRawReferences();
        if (rawRef != null) for (int i=0; i<rawRef.length; i++) {
            refHelper.addRawReference(rawRef[i]);
        }
        if (prj instanceof J2MEProject)
            ((J2MEProject) prj).hookNewProjectCreated();
        final FileObject src = projectLocation.createFolder(SRC); // NOI18N
        if (copySources) copyJavaFolder(oldHelper.resolveFile(oldHelper.getStandardPropertyEvaluator().getProperty(DefaultPropertiesDescriptor.SRC_DIR)), FileUtil.toFile(src), IMPORT_SRC_EXCLUDES);
        refreshProject(projectLocation, src);
        ProjectManager.getDefault().saveProject(prj);
        return h;
    }
    
    public static AntProjectHelper createNewProject(final File projectLocation, final String name, final PlatformSelectionPanel.PlatformDescription platform, final Collection<DataObject> createHelloMIDlet, final Set<ConfigurationTemplateDescriptor> cfgTemplates) throws IOException {
        return createProject(projectLocation, name, platform, new ProjectGeneratorCallback() {
            public void doPostGeneration(Project project, AntProjectHelper helper, FileObject projectLocation, @SuppressWarnings("unused") File projectLocationFile, ArrayList<String> configurations) throws IOException {
                final FileObject src = projectLocation.createFolder(SRC); // NOI18N
                if (createHelloMIDlet != null) {
                    FileObject hello = src.createFolder("hello"); // NOI18N
                    if (hello == null)
                        hello = src;
                    final DataFolder helloFolder = DataFolder.findFolder(hello);
                    final FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
                    FileObject foTemplate = dfs.findResource("Templates/MIDP/HelloMIDlet.java"); //NOI18N
                    if (foTemplate == null) foTemplate = dfs.findResource("Templates/MIDP/Midlet.java"); //NOI18N
                    try {
                        if (foTemplate != null) {
                            final DataObject template = DataObject.find(foTemplate);
                            if (template != null) {
                                // Remove ".java" suffix
                                String name=template.getName();
                                if (name.endsWith(".java")) {
                                    name=name.substring(0,name.length()-5);
                                }
                                DataObject fromTemplate = template.createFromTemplate (helloFolder);
                                try {
                                    fromTemplate.setValid (false);
                                } catch (PropertyVetoException e) {
                                    e.printStackTrace (); // TODO
                                }
                                fromTemplate = DataObject.find (fromTemplate.getPrimaryFile ());
                                createHelloMIDlet.add(fromTemplate);
                                addMIDletProperty(project, helper, name, hello != src ? "hello."+name : name, ""); // NOI18N
                            }
                        }
                    } catch (DataObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (cfgTemplates != null) {
                    final EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    final EditableProperties proj = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    for (ConfigurationTemplateDescriptor desc : cfgTemplates) {
                        String cfgName = desc.getCfgName();
                        String prefix = J2MEProjectProperties.CONFIG_PREFIX + cfgName + '.'; 
                        if (!configurations.contains(cfgName)) {
                            configurations.add(cfgName);
                            Map<String, String> p = desc.getPrivateProperties();
                            if (p != null) for(Map.Entry<String, String> en : p.entrySet()) {
                                if (!priv.containsKey(en.getKey())) priv.put(en.getKey(), en.getValue());
                            }
                            p = desc.getProjectGlobalProperties();
                            if (p != null) for(Map.Entry<String, String> en : p.entrySet()) {
                                if (!proj.containsKey(en.getKey())) proj.put(en.getKey(), en.getValue());
                            }
                            p = desc.getProjectConfigurationProperties();
                            if (p != null) for(Map.Entry<String, String> en : p.entrySet()) {
                                proj.put(prefix + en.getKey(), en.getValue());
                            }
                        }
                    }
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, proj);
                }
                refreshProject(projectLocation, src);
            }
        });
    }
    
    public static AntProjectHelper createProjectFromTemplate(final FileObject template, final File projectLocation, final String name, final PlatformSelectionPanel.PlatformDescription platform) throws IOException {
        return createProject(projectLocation, name, platform, new ProjectGeneratorCallback() {
            public void doPostGeneration(@SuppressWarnings("unused") Project project, AntProjectHelper helper, FileObject projectLocation, File projectLocationFile, ArrayList<String> configurations) throws IOException {
                FileObject src = null;
                if (template.getExt().endsWith("zip")) {  //NOI18N
                    unzip(template.getInputStream(), projectLocationFile);
                    final File jadFile =  Utils.findAnyFile(projectLocationFile.listFiles(), "jad"); // NOI18N
                    if (jadFile != null) {
                        loadJadManifest(helper, jadFile);
                        jadFile.delete();
                    }
                    final File metaInfDir = new File(projectLocationFile, "META-INF");  //NOI18N
                    final File projectPropertiesFile = new File(metaInfDir, "project.properties");  //NOI18N
                    if (projectPropertiesFile.exists()  && projectPropertiesFile.canRead()) {
                        final Properties props = new Properties();
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(projectPropertiesFile);
                            props.load(fis);
                            final Enumeration e = props.keys();
                            while (e.hasMoreElements()) {
                                String name = (String) e.nextElement();
                                if (! name.startsWith(J2MEProjectProperties.CONFIG_PREFIX))
                                    continue;
                                name = name.substring(J2MEProjectProperties.CONFIG_PREFIX.length());
                                final int i = name.indexOf('.');  //NOI18N
                                if (i >= 0)
                                    name = name.substring(0, i);
                                if (! configurations.contains(name))
                                    configurations.add(name);
                            }
                            setProperties(helper, props);
                        } finally {
                            if (fis != null) try { fis.close(); } catch (IOException e) {}
                        }
                        deleteAll(metaInfDir);
                    }
                    projectLocation.refresh(false);
                } else {
                    src = projectLocation.createFolder(SRC); // NOI18N
                }
                refreshProject(projectLocation, src);
            }
        });
    }
    
    public static interface ProjectGeneratorCallback {
        
        public void doPostGeneration(Project project, AntProjectHelper helper, FileObject projectLocation, File projectLocationFile, ArrayList<String> configurations) throws IOException;
        
    }
    
    /**
     * Create a new empty J2ME project.
     * @param _projectLocation the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @param callback project generation callback
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final File _projectLocation, final String name, final PlatformSelectionPanel.PlatformDescription platform, final ProjectGeneratorCallback callback) throws IOException {
        final FileObject projectLocation = createProjectLocation(_projectLocation);
        final AntProjectHelper h = ProjectGenerator.createProject(projectLocation, J2MEProjectType.TYPE);
        final Element data = h.getPrimaryConfigurationData(true);
        final Document doc = data.getOwnerDocument();
        final Element nameEl = doc.createElementNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, NAME); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        final Element minant = doc.createElementNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties priv = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        for (ProjectPropertiesDescriptor p : Lookup.getDefault().lookup(new Lookup.Template<ProjectPropertiesDescriptor>(ProjectPropertiesDescriptor.class)).allInstances() ) {
            for (PropertyDescriptor d : p.getPropertyDescriptors()) {
                if (d.getDefaultValue() != null) {
                    (d.isShared() ? ep : priv).setProperty(d.getName(), d.getDefaultValue());
                }
            }
        }
        
        ep.setProperty(DefaultPropertiesDescriptor.BUILD_ROOT_DIR, "build");  //NOI18N
        ep.setProperty(DefaultPropertiesDescriptor.DIST_ROOT_DIR, "dist");  //NOI18N
        ep.setProperty(NAME, name);  //NOI18N
        ep.setProperty("preprocessed.dir", "${build.dir}/preprocessed");  //NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/compiled");  //NOI18N
        ep.setProperty("obfuscator.srcjar", "${build.dir}/before-obfuscation.jar");  //NOI18N
        ep.setProperty("obfuscator.destjar", "${build.dir}/obfuscated.jar");  //NOI18N
        ep.setProperty("obfuscated.classes.dir", "${build.dir}/obfuscated");  //NOI18N
        ep.setProperty("preverify.classes.dir", "${build.dir}/preverified");  //NOI18N
        final String usablePropertyName = PropertyUtils.getUsablePropertyName(name);
        ep.setProperty(DefaultPropertiesDescriptor.DIST_JAR, usablePropertyName + ".jar");  //NOI18N
        ep.setProperty(DefaultPropertiesDescriptor.DIST_JAD, usablePropertyName + ".jad");  //NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/doc");  //NOI18N
        ep.setProperty(CopyDeploymentPlugin.PROP_TARGET, "deploy"); //NOI18N
        ep.setProperty(DefaultPropertiesDescriptor.JAVAC_ENCODING, FileEncodingQuery.getDefaultEncoding().name());
        
        final HashMap<String,String> manifestOthers = new HashMap<String,String>();
        manifestOthers.put("MIDlet-Name", name);  //NOI18N
        manifestOthers.put("MIDlet-Vendor", "Vendor");  //NOI18N
        manifestOthers.put("MIDlet-Version", "1.0");  //NOI18N
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_OTHERS, manifestOthers);
        
        if (platform != null) {
            ep.putAll(PlatformConvertor.extractPlatformProperties("", platform.platform, platform.device, platform.configuration, platform.profile)); //NOI18N
        } else {
            ep.putAll(PlatformConvertor.extractPlatformProperties("", findPlatform(null), null, null, null)); //NOI18N
        }
        
        priv.setProperty("netbeans.user",  System.getProperty("netbeans.user"));  //NOI18N
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
        final File projectLocationFile = FileUtil.toFile(projectLocation);
        assert projectLocationFile != null : "FileUtil.toFile convertion failed for: " + projectLocation;  //NOI18N
        final ArrayList<String> configurations = new ArrayList<String>();
        final Project prj = ProjectManager.getDefault().findProject(projectLocation);
        if (callback != null)
            callback.doPostGeneration(prj, h, projectLocation, projectLocationFile, configurations);
        if (! configurations.isEmpty()) {
            final ProjectConfigurationsHelper confs = prj.getLookup().lookup(ProjectConfigurationsHelper.class);
            if (confs != null) {
                for (int a = 0; a < configurations.size(); a++) {
                    final String conf = configurations.get(a);
                    confs.addConfiguration(conf);
                }
            }
        }
        if (prj instanceof J2MEProject)
            ((J2MEProject) prj).hookNewProjectCreated();
        ProjectManager.getDefault().saveProject(prj);
        return h;
    }
    
    protected static void loadJadManifest(final AntProjectHelper helper, final File jadManifest) throws IOException {
        final Map<String,String> map = new HashMap<String,String>();
        loadJadManifest(map, jadManifest);
        loadPropertiesFromMap(helper, map, map);
    }
    
    public static void loadJadManifest(final Map<String,String> map, final File jadManifest) throws IOException {
        if (! jadManifest.exists()  ||  ! jadManifest.isFile()  ||  ! jadManifest.canRead())
            return;
        String ext = jadManifest.getName();
        final int index = ext.lastIndexOf('.');
        if (index >= 0)
            ext = ext.substring(index + 1);
        if ("jad".equals(ext.toLowerCase()))  //NOI18N
            loadJad(map, jadManifest);
        else
            loadManifest(map, jadManifest);
    }
    
    protected static void loadJadAndManifest(final AntProjectHelper helper, final File jad, final File manifest) throws IOException {
        final Map<String,String> jadMap = new HashMap<String,String>(), manifestMap = new HashMap<String,String>();
        if (jad != null)
            loadJad(jadMap, jad);
        if (manifest != null)
            loadManifest(manifestMap, manifest);
        loadPropertiesFromMap(helper, jadMap.isEmpty() ? manifestMap : jadMap, manifestMap.isEmpty() ? jadMap : manifestMap);
    }
    
    public static void loadJadAndManifest(final Map<String,String> map, final File jad, final File manifest) throws IOException {
        if (jad != null)
            loadJad(map, jad);
        if (manifest != null)
            loadManifest(map, manifest);
    }
    
    private static void removeInvalidProperties(final Map<String,String> map) {
        map.remove("MIDlet-Jar-RSA-SHA1");  //NOI18N
        map.remove("MIDlet-Jar-Size");  //NOI18N
        map.remove("MIDlet-Jar-URL");  //NOI18N
        map.remove("MicroEdition-Configuration");  //NOI18N
        map.remove("MicroEdition-Profile");  //NOI18N
        final String CERTIFICATE = "MIDlet-Certificate-{0}-{1}";  //NOI18N
        for (int a = 1; ; a ++) {
            int b = 1;
            for (;; b ++) {
                if (map.remove(MessageFormat.format(CERTIFICATE, new Object[] {Integer.toString(a), Integer.toString(b)})) == null)
                    break;
            }
            if (b <= 1)
                break;
        }
    }
    
	protected static void setProperties(final AntProjectHelper helper, final Map map) {
        final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.putAll(map);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
    private static void loadPropertiesFromMap(final AntProjectHelper helper, final Map<String,String> jadMap, final Map<String,String> manifestMap) throws IOException {
        final HashMap<String,String> map = new HashMap<String,String>(jadMap);
        map.putAll(manifestMap);
        removeInvalidProperties(map);
        final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final HashMap<String,String> midlets = new HashMap<String,String>();
        final HashMap<String,String> apipermissions = new HashMap<String,String>();
        final HashMap<String,String> pushregistry = new HashMap<String,String>();
        final HashMap<String,String> others = (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(ep.getProperty(DefaultPropertiesDescriptor.MANIFEST_OTHERS), null, null);
        final HashMap<String,String> manifest = new HashMap<String,String>();
        final HashMap<String,String> jad = new HashMap<String,String>();
        for ( final String key : map.keySet() ) {
            if (key == null)
                continue;
            final String value = map.get(key);
            if (value == null)
                continue;
            if ("MIDlet-Permissions".equalsIgnoreCase(key))  //NOI18N
                apipermissions.put(key, value);
            else if ("MIDlet-Permissions-Opt".equalsIgnoreCase(key))  //NOI18N
                apipermissions.put(key, value);
            else if (isNumberedProperty(key, MIDLET))  //NOI18N
                midlets.put(key, value);
            else if (isNumberedProperty(key, "MIDlet-Push-"))  //NOI18N
                pushregistry.put(key, value);
            else if (!jadMap.containsKey(key) && !KNOWN_ATTRIBUTES.contains(key))
                manifest.put(key, value);
            else if (!manifestMap.containsKey(key) && !KNOWN_ATTRIBUTES.contains(key))
                jad.put(key, value);
            else
                others.put(key, value);
        }
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_MIDLETS, midlets);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_APIPERMISSIONS, apipermissions);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_PUSHREGISTRY, pushregistry);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_OTHERS, others);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_JAD, jad);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_MANIFEST, manifest);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
    private static boolean isNumberedProperty(final String key, final String prefix) {
        if (! key.startsWith(prefix))
            return false;
        try {
            Integer.parseInt(key.substring(prefix.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static void storeManifestProperties(final EditableProperties ep, final String name, final HashMap<String,String> map)  {
        ep.setProperty(name, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map, null, null));
    }
    
    private static void loadJad(final Map<String,String> map, final File jad) throws IOException {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(jad), DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            br = new BufferedReader(new FileReader(jad));
        }
        try {
            for (;;) {
                final String readLine = br.readLine();
                if (readLine == null)
                    break;
                if ("".equals(readLine))  //NOI18N
                    continue;
                final int colon = readLine.indexOf(':');
                if (colon < 0)
                    continue;
                map.put(readLine.substring(0, colon), readLine.substring(colon + 1).trim());
            }
        } finally {
            br.close();
        }
    }
    
    private static void loadManifest(final Map<String,String> map, final File manifest) throws IOException {
        final FileInputStream fis = new FileInputStream(manifest);
        try {
            final Manifest m = new Manifest(fis);
            final Iterator it = m.getEntries().values().iterator();
            while (it.hasNext()) {
                putAllAsStrings(map, (Attributes)it.next());
            }
            putAllAsStrings(map, m.getMainAttributes());
            map.remove("Manifest-Version"); //NOI18N
            map.remove("Created-By"); //NOI18N
        } finally {
            fis.close();
        }
    }
    
    private static void putAllAsStrings(final Map<String,String> map, final Attributes attrs) {
        for ( final Map.Entry en : attrs.entrySet() ) {
            map.put(en.getKey().toString(), (String)en.getValue());
        }
    }
    
    protected static void loadSettingFromSuite(final AntProjectHelper helper, final File suite) throws IOException {
        final Document doc = getDocumentForSuite(suite);
        loadSettingFromSuite(helper, doc);
    }
    
    public static Document getDocumentForSuite(final File suite) throws IOException {
        if (suite == null  ||  ! suite.exists()  ||  ! suite.isFile()  ||  ! suite.canRead())
            return null;
        final InputStream is = new FileInputStream(suite);
        Document doc = null;
        try {
            doc = XMLUtil.parse(new InputSource(is), false, false, new ErrorHandler() {
                public void error(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }
                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }
                public void warning(@SuppressWarnings("unused") SAXParseException e) {
                }
            }, null);
        } catch (SAXException e) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(J2MEProjectGenerator.class, "MSG_ProjectGen_CannotParseSuite")));  //NOI18N
            return null;
        } finally {
            try { is.close(); } catch (IOException e) {}
        }
        return doc;
    }
    
    private static void loadSettingFromSuite(final AntProjectHelper helper, final Document doc) throws IOException {
        if (doc == null)
            return;
        
        final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final HashMap<String,String> midlets = new HashMap<String,String>();
        final HashMap<String,String> pushregistry = new HashMap<String,String>();
        final HashSet<String> permission = new HashSet<String>();
        final HashSet<String> permissionOpt = new HashSet<String>();
        final HashMap<String,String> apipermissions = new HashMap<String,String>();
        final HashMap<String,String> others = (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(ep.getProperty(DefaultPropertiesDescriptor.MANIFEST_OTHERS), null, null);
        Profile configuration = null, profile = null;
        
        final Node root = doc.getFirstChild();
        if ("2.0".equals(getAttributeValue(root, "version"))) {  //NOI18N
            Node node = root.getFirstChild();
            while (node != null) {
                if ("attribute".equalsIgnoreCase(node.getNodeName())) {  //NOI18N
                    Node attributeNode = node.getFirstChild();
                    while (attributeNode != null) {
                        if ("midlet".equalsIgnoreCase(attributeNode.getNodeName())) {  //NOI18N
                            final String order = getAttributeValue(attributeNode, "order");  //NOI18N
                            final String clazz = getAttributeValue(attributeNode, "class");  //NOI18N
                            final String icon = getAttributeValue(attributeNode, "icon");  //NOI18N
                            final String name = getAttributeValue(attributeNode, NAME);  //NOI18N
                            if (order != null  &&  clazz != null  &&  icon != null  &&  name != null)
                                midlets.put(MIDLET + order, name + ", " + icon + ", " + clazz);  //NOI18N
                        } else if ("permission".equalsIgnoreCase(attributeNode.getNodeName())) {  //NOI18N
                            final String name = getAttributeValue(attributeNode, NAME);  //NOI18N
                            if (name != null)
                                permission.add(name);
                        } else if ("permission-opt".equalsIgnoreCase(attributeNode.getNodeName())) {  //NOI18N
                            final String name = getAttributeValue(attributeNode, NAME);  //NOI18N
                            if (name != null)
                                permissionOpt.add(name);
                        } else if ("required".equalsIgnoreCase(attributeNode.getNodeName())  ||  //NOI18N
                                "optional".equalsIgnoreCase(attributeNode.getNodeName())  ||  //NOI18N
                                "user".equalsIgnoreCase(attributeNode.getNodeName())  ||  //NOI18N
                                "security".equalsIgnoreCase(attributeNode.getNodeName())) {  //NOI18N
                            final String name = getAttributeValue(attributeNode, NAME);  //NOI18N
                            final String value = getAttributeValue(attributeNode, "value");  //NOI18N
                            if (name != null  &&  value != null)
                                others.put(name, value);
                        } else if ("push".equalsIgnoreCase(attributeNode.getNodeName())) {  //NOI18N
                            final String protocol = getAttributeValue(attributeNode, "protocol");  //NOI18N
                            final String host = getAttributeValue(attributeNode, "host");  //NOI18N
                            final String clazz = getAttributeValue(attributeNode, "class");  //NOI18N
                            final String order = getAttributeValue(attributeNode, "order");  //NOI18N
                            if (protocol != null  &&  host != null  &&  clazz != null  &&  order != null)
                                midlets.put("MIDlet-Push-" + order, protocol + ", " + clazz + ", " + host);  //NOI18N
                        }
                        attributeNode = attributeNode.getNextSibling();
                    }
                } else if ("Configuration".equalsIgnoreCase(node.getNodeName())) {  //NOI18N
                    configuration = name2profile(getAttributeValue(node, "value"));  //NOI18N
                } else if ("Profile".equalsIgnoreCase(node.getNodeName())) {  //NOI18N
                    profile = name2profile(getAttributeValue(node, "value"));  //NOI18N
                }
                node = node.getNextSibling();
            }
        }
        
        if (! permission.isEmpty())
            apipermissions.put("MIDlet-Permissions", hashMapToCommaSeparatedString(permission));  //NOI18N
        if (! permissionOpt.isEmpty())
            apipermissions.put("MIDlet-Permissions-Opt", hashMapToCommaSeparatedString(permissionOpt));  //NOI18N
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_MIDLETS, midlets);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_APIPERMISSIONS, apipermissions);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_PUSHREGISTRY, pushregistry);
        removeInvalidProperties(others);
        storeManifestProperties(ep, DefaultPropertiesDescriptor.MANIFEST_OTHERS, others);
        if (configuration != null || profile != null) {
            Profile profiles[] = new Profile[configuration == null || profile == null ? 1 : 2];
            profiles[0] = configuration;
            if (profile != null) profiles[profiles.length - 1] = profile;
            final J2MEPlatform platform = findPlatform(profiles);
            if (platform != null)
                ep.putAll(PlatformConvertor.extractPlatformProperties("", platform, null, configuration.toString(), profile.toString())); //NOI18N
        }
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
    private static String hashMapToCommaSeparatedString(final HashSet<String> set) {
        final StringBuffer res = new StringBuffer();
        boolean first = true;
        for ( final String o : set ) { 
            if (o == null)
                continue;
            res.append(o);
            if (! first)
                res.append(", ");  //NOI18N
            else
                first = false;
        }
        return res.toString();
    }
    
    public static String getAttributeValue(Node node, final String attr) {
        try {
            if (node == null)
                return null;
            node = node.getAttributes().getNamedItem(attr);
            if (node == null)
                return null;
            return node.getNodeValue();
        } catch (DOMException e) {
            return null;
        }
    }
    
    public static File findWtkJadFile(final String appLocation) {
        if (appLocation == null)
            return null;
        File jad = null;
        final File bin = new File(appLocation, "bin");  //NOI18N
        if (bin.exists()  &&  bin.isDirectory()  &&  bin.canRead()) {
            final File[] files = bin.listFiles();
            jad = Utils.findSubFile(files, new File(appLocation).getName() + ".jad");  //NOI18N
            if (jad == null)
                jad = Utils.findAnyFile(files, "jad");  //NOI18N
        }
        return jad;
    }
    
    public static File findWtkManifestFile(final String appLocation) {
        if (appLocation == null)
            return null;
        File mf = null;
        final File bin = new File(appLocation, "bin");  //NOI18N
        if (bin.exists()  &&  bin.isDirectory()  &&  bin.canRead()) {
            final File[] files = bin.listFiles();
            mf = Utils.findSubFile(files, "manifest.mf");  //NOI18N
            if (mf == null)
                mf = Utils.findAnyFile(files, "mf");  //NOI18N
        }
        return mf;
    }
    
	protected static void loadWTKProperties(final AntProjectHelper helper, final File wtkProject) throws IOException {
        final File propFile = new File(wtkProject, "project.properties");  //NOI18N
        if (! propFile.exists()  ||  ! propFile.isFile())
            return;
        final Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propFile);
            props.load(fis);
        } finally {
            if (fis != null) try { fis.close(); } catch (IOException e) {}
        }
        final Profile configuration = wtkName2profile(props.getProperty("configuration"), "CLDC");  //NOI18N
        if (configuration != null) {
            final J2MEPlatform found = findPlatform(new Profile[] { configuration });
            if (found != null) {
                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.putAll(PlatformConvertor.extractPlatformProperties("", found, null, configuration.toString(), null)); //NOI18N
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }
        }
    }
    
    private static Profile wtkName2profile(String profile, final String prefix) {
        if (profile == null)
            return null;
        if (profile.startsWith(prefix)) {
            profile = profile.substring(prefix.length());
            if (profile.startsWith("-"))  //NOI18N
                profile = profile.substring(1);
            profile = prefix + "-" + profile;  //NOI18N
        }
        return name2profile(profile);
    }
    
    private static Profile name2profile(final String name) {
        if (name == null)
            return null;
        final int i = name.indexOf('-');
        try {
            if (i >= 0)
                return new Profile(name.substring(0, i), new SpecificationVersion(name.substring(i + 1)));
            return new Profile(name, new SpecificationVersion("1.0"));  //NOI18N
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static J2MEPlatform findPlatform(final Profile[] profiles) {
        JavaPlatform p[];
        if (profiles != null)
            p = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null, profiles));
        else
            p = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null));
        
        for (int i=0; i<p.length; i++) {
            if (p[i] instanceof J2MEPlatform) {
                return (J2MEPlatform) p[i];
            }
        }
        return null;
    }
    
    private static FileObject createProjectLocation(File dir) throws IOException {
        dir = dir.getCanonicalFile();
        File rootF = dir;
        while (rootF != null && !(rootF = rootF.getParentFile()).exists());
        if (rootF == null) throw new IOException("Cannot find existing parent from "+dir.toString()); //NOI18N
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(rootF));
        assert dirFO != null : "FileObject for " + rootF + "does not exist !";  //NOI18N
        if (rootF != dir) {
            String relName = dir.getAbsolutePath().substring(rootF.getAbsolutePath().length());
            if (relName.startsWith(File.separator)) relName = relName.substring(1);
            dirFO = FileUtil.createFolder(dirFO, relName);
        }
        dirFO.refresh(false); // workaround for #5037460
        assert dirFO.isFolder() : "Not really a dir: " + dir;  //NOI18N
        //        assert dirFO.getChildren().length == 0 : "Dir must have been empty: " + dir;
        File parent = FileUtil.toFile(dirFO);
        if (parent != null) parent = parent.getParentFile();
        if (parent != null) ProjectChooser.setProjectsFolder(parent);
        return dirFO;
    }
    
    private static void copyJavaFolder(final File source, final File target, final Pattern filter) throws IOException {
        copyJavaFolder(source, target, target, filter);
    }
    
    private static void copyJavaFolder(final File source, final File targetRoot, final File target, final Pattern filter) throws IOException {
        if (source == null  ||  targetRoot == null  ||  target  == null)
            return;
        if (isParent(source, target))
            return;
        final File[] files = source.listFiles();
        if (files != null) for (int a = 0; a < files.length; a ++) {
            final File file = files[a];
            if (filter.matcher(file.getAbsolutePath().replace('\\', '/')).matches())
                continue;
            if (file.isDirectory()) {
                final File subdir = new File(target, file.getName());
                subdir.mkdirs();
                copyJavaFolder(file, targetRoot, subdir, filter);
            } else {
                File targetFile = new File(target, file.getName());
                if (file.getName().toLowerCase().endsWith(".java")) {  //NOI18N
                    String classPackage = null;
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(file));
                        for (;;) {
                            final String line = br.readLine();
                            if (line == null)
                                break;
                            
                            int pos = 0;
                            while (pos < line.length()  &&  line.charAt(pos) == ' ')
                                pos ++;
                            if (! line.startsWith("package", pos))  //NOI18N
                                continue;
                            pos += "package".length();  //NOI18N
                            final int tmppos = pos;
                            while (pos < line.length()  &&  line.charAt(pos) == ' ')
                                pos ++;
                            if (tmppos == pos)
                                continue;
                            int end = pos;
                            while (end < line.length()  &&  (Character.isJavaIdentifierPart(line.charAt(end))  ||  line.charAt(end) == '.'))
                                end ++;
                            if (end == pos)
                                continue;
                            if (end < line.length()  &&  ! Character.isWhitespace(line.charAt(end))  &&  line.charAt(end) != ';')
                                continue;
                            classPackage = line.substring(pos, end);
                            break;
                        }
                    } catch (IOException e) {
                    } finally {
                        if (br != null) try { br.close(); } catch (IOException e) {}
                    }
                    if (classPackage != null  &&  ! "".equals(classPackage)) {  //NOI18N
                        final File dir = new File(targetRoot, classPackage.replace('.', File.separatorChar));
                        dir.mkdirs();
                        targetFile = new File(dir, file.getName());
                    }
                }
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(file);
                    fos = new FileOutputStream(targetFile);
                    FileUtil.copy(fis, fos);
                } finally {
                    if (fis != null) try { fis.close(); } catch (IOException e) {}
                    if (fos != null) try { fos.close(); } catch (IOException e) {}
                }
            }
        }
    }
    
    private static boolean isParent(final File source, File target) {
        while (target != null) {
            if (source.equals(target))
                return true;
            target = target.getParentFile();
        }
        return false;
    }
    
    protected static void unzip(final InputStream source, final File targetFolder) throws IOException {
        //installation
        final ZipInputStream zip=new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                final File f = new File(targetFolder, ent.getName());
                if (ent.isDirectory()) {
                    f.mkdirs();
                } else {
                    f.getParentFile().mkdirs();
                    final FileOutputStream out = new FileOutputStream(f);
                    try {
                        FileUtil.copy(zip, out);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }
    
    protected static void deleteAll(final File directory) {
        final File[] files = directory.listFiles();
        for (int a = 0; a < files.length; a++) {
            final File file = files[a];
            if (file.isDirectory())
                deleteAll(file);
            else
                file.delete();
        }
        directory.delete();
    }
    
    public static void loadLibraries(final AntProjectHelper h, final ReferenceHelper refHelper, final File[] files) {
        StringBuffer libs = null;
        if (files != null) for (int a = 0; a < files.length; a ++) {
            if (files[a] == null  ||  ! files[a].exists())
                continue;
            final String name = files[a].getName().toLowerCase();
            if (files[a].isFile() && !name.endsWith(".jar")  &&  !name.endsWith(".zip"))  //NOI18N
                continue;
            if (libs != null)
                libs.append(File.pathSeparatorChar);
            else
                libs = new StringBuffer();
            libs.append(refHelper.createForeignFileReference(FileUtil.normalizeFile(files[a]), "anyfile")); //NOI18N
        }
        if (libs == null)
            return;
        final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(DefaultPropertiesDescriptor.LIBS_CLASSPATH, libs.toString());
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }

    public static void copyMIDletProperty(final Project project, final AntProjectHelper h, String sourceClass, String targetClass) throws IOException {
        if (sourceClass == null  ||  targetClass == null)
            return;

        final ProjectConfigurationsHelper confHelper = project.getLookup().lookup(ProjectConfigurationsHelper.class);
        final ProjectConfiguration[] confs = confHelper.getConfigurations().toArray(new ProjectConfiguration[0]);
        final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        final String defaultValue = ep.getProperty(DefaultPropertiesDescriptor.MANIFEST_MIDLETS);
        HashMap<String,String> map = defaultValue != null ? (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue, null, null) : new HashMap<String,String>();

        copyMIDletProperty (map, sourceClass, targetClass);

        ep.put(DefaultPropertiesDescriptor.MANIFEST_MIDLETS, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map, null, null));

        for (int i = 0; i < confs.length; i++) {
            final ProjectConfiguration conf = confs[i];
            final String confName = conf.getDisplayName();
            final String propertyName = VisualPropertySupport.translatePropertyName(confName, DefaultPropertiesDescriptor.MANIFEST_MIDLETS, false);
            if (propertyName == null)
                continue;
            final String propertyValue = ep.getProperty(propertyName);
            if (propertyValue == null)
                continue;
            map = (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue, null, null);

            copyMIDletProperty (map, sourceClass, targetClass);

            ep.put(propertyName, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map, null, null));
        }

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }

    private static void copyMIDletProperty (HashMap<String, String> map, String sourceClass, String targetClass) {
        String newMIDlet = null;
        int a = 1;
        for (;;) {
            String value = map.get (MIDLET + a);// NOI18N
            if (value == null)
                break;
            int index = value.lastIndexOf (',');
            if (index >= 0  &&  sourceClass.equals (value.substring (index + 1).trim ()))
                newMIDlet = value.substring (0, index + 1) + targetClass;
            a++;
        }
        if (newMIDlet != null)
            map.put(MIDLET + a, newMIDlet);  //NOI18N
    }

    public static void addMIDletProperty(final Project project, final AntProjectHelper h, String name, String clazz, String icon) throws IOException {
        if (name == null)
            name = ""; // NOI18N
        if (clazz == null)
            clazz = ""; // NOI18N
        if (icon == null)
            icon = ""; // NOI18N
        
        final ProjectConfigurationsHelper confHelper = project.getLookup().lookup(ProjectConfigurationsHelper.class);
        final ProjectConfiguration[] confs = confHelper.getConfigurations().toArray(new ProjectConfiguration[0]);
        final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        final String defaultValue = ep.getProperty(DefaultPropertiesDescriptor.MANIFEST_MIDLETS);
        HashMap<String,String> map = defaultValue != null ? (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue, null, null) : new HashMap<String,String>();
        addMIDletProperty(map, name, clazz, icon);
        ep.put(DefaultPropertiesDescriptor.MANIFEST_MIDLETS, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map, null, null));
        
        for (int i = 0; i < confs.length; i++) {
            final ProjectConfiguration conf = confs[i];
            final String confName = conf.getDisplayName();
            final String propertyName = VisualPropertySupport.translatePropertyName(confName, DefaultPropertiesDescriptor.MANIFEST_MIDLETS, false);
            if (propertyName == null)
                continue;
            final String propertyValue = ep.getProperty(propertyName);
            if (propertyValue == null)
                continue;
            map = (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(defaultValue, null, null);
            addMIDletProperty(map, name, clazz, icon);
            ep.put(propertyName, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(map, null, null));
        }
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }
    
    private static void addMIDletProperty(final Map<String,String> map, final String name, final String clazz, final String icon) {
        int a = 1;
        while (map.containsKey(MIDLET + a))  //NOI18N
            a++;
        map.put(MIDLET + a, name + ", " + icon + ", " + clazz);  //NOI18N
    }
    
    public static void refreshProject(final FileObject projectLocation, final FileObject srcLocation) {
        if (projectLocation != null)
            projectLocation.refresh(false);
        if (srcLocation != null)
            srcLocation.refresh();
    }
    
    protected static void fillMissingMIDlets(final Project project, final AntProjectHelper helper) {
        final ReferenceHelper refHelper = project.getLookup().lookup(ReferenceHelper.class);
        final ProjectConfigurationsHelper confHelper = project.getLookup().lookup(ProjectConfigurationsHelper.class);
        final MIDletScanner scanner = MIDletScanner.getDefault(new J2MEProjectProperties(project, helper, refHelper, confHelper));
        final DefaultComboBoxModel midlets = new DefaultComboBoxModel();
        scanner.scan(midlets, null, null,
                     new ChangeListener() {

                         public void stateChanged(@SuppressWarnings("unused") ChangeEvent e) {
                             final MIDletsCacheHelper mHelper = project.getLookup().lookup(MIDletsCacheHelper.class);

                             for (int i = 0; i < midlets.getSize(); i++)
                                 try {
                                     final String midlet = (String) midlets.getElementAt(i);

                                     if (!mHelper.contains(midlet))
                                         addMIDletProperty(project, helper,
                                                           midlet.substring(midlet.lastIndexOf('.') +
                                                                            1),
                                                           midlet, null);
                                 }
                                 catch (IOException ioe) {
                                 }
                             if (project instanceof J2MEProject)
                                 ((J2MEProject) project).hookNewProjectCreated();
                             try {
                                 ProjectManager.getDefault().saveProject(project);
                             }
                             catch (IOException ioe) {
                             }
                         }
                     });
    }
    
    protected static void setSourceRoot(final AntProjectHelper helper, final ReferenceHelper refHelper, final String srcRoot) {
        final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(DefaultPropertiesDescriptor.SRC_DIR, refHelper.createForeignFileReference(helper.resolveFile(srcRoot), "anyfile")); //NOI18N
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        refreshProject(helper.getProjectDirectory(), helper.resolveFileObject(srcRoot));
    }
    
    protected static ReferenceHelper getReferenceHelper(final Project p) {
        return p.getLookup().lookup(ReferenceHelper.class);
    }
}
