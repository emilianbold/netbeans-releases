/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.ui.ProjectModel;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles providing implementations of some Java-oriented IDE-specific actions.
 * @author Jesse Glick
 * @see "issue #46886"
 */
final class JavaActions implements ActionProvider {
    
    static final String NS_GENERAL = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    /* Too problematic for importing <classpath> from existing <java>, since Ant would want NS on that too (oddly):
    private static final String NS_JPDA = "antlib:org.netbeans.modules.debugger.jpda.ant"; // NOI18N
     */
    
    private static final String[] ACTIONS = {
        ActionProvider.COMMAND_COMPILE_SINGLE,
        ActionProvider.COMMAND_DEBUG,
        // XXX more
    };
    
    /**
     * Script to hold file-sensitive generated targets like compile.single.
     * (Or for generated targets for debug which cannot reuse any existing target body.)
     * These pick up at least project.dir from project.xml and the entire
     * target body is fixed by the IDE, except for some strings determined
     * by information from project.xml like the classpath. The basedir
     * is set to the project directory so that properties match their
     * semantics in project.xml.
     */
    static final String FILE_SCRIPT_PATH = "nbproject/ide-file-targets.xml"; // NOI18N
    /**
     * Script to hold non-file-sensitive generated targets like debug.
     * These import the original build script and share its basedir, so that
     * properties match the semantics of build.xml.
     */
    static final String GENERAL_SCRIPT_PATH = "nbproject/ide-targets.xml"; // NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    private boolean setOutputsNotified;
    
    public JavaActions(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
    }

    public String[] getSupportedActions() {
        return ACTIONS;
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (command.equals(ActionProvider.COMMAND_COMPILE_SINGLE)) {
            return findPackageRoot(context) != null;
        } else if (command.equals(ActionProvider.COMMAND_DEBUG)) {
            return true;
        } else {
            throw new IllegalArgumentException(command);
        }
    }

    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        try {
            project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    try {
                        if (command.equals(ActionProvider.COMMAND_COMPILE_SINGLE)) {
                            handleCompileSingle(context);
                        } else if (command.equals(ActionProvider.COMMAND_DEBUG)) {
                            handleDebug();
                        } else {
                            throw new IllegalArgumentException(command);
                        }
                    } catch (SAXException e) {
                        throw (IOException) new IOException(e.toString()).initCause(e);
                    }
                }
            });
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Display an alert asking the user whether to really generate a target.
     * @param commandDisplayName the display name of the action to be bound
     * @param scriptPath the path that to the script that will be generated or written to
     * @return true if IDE should proceed
     */
    private boolean alert(String commandDisplayName, String scriptPath) {
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        String title = NbBundle.getMessage(JavaActions.class, "TITLE_generate_target_dialog", commandDisplayName, projectDisplayName);
        String body = NbBundle.getMessage(JavaActions.class, "TEXT_generate_target_dialog", commandDisplayName, scriptPath);
        NotifyDescriptor d = new NotifyDescriptor.Message(body, NotifyDescriptor.QUESTION_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton generate = new JButton(NbBundle.getMessage(JavaActions.class, "LBL_generate"));
        generate.setDefaultCapable(true);
        d.setOptions(new Object[] {generate, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == generate;
    }
    
    /**
     * Warns the user about missing project outputs setting
     * @param commandDisplayName the display name of the action to be bound
     */
    private boolean alertOutputs (String commandDisplayName) {
        JButton setOutputOption = new JButton (NbBundle.getMessage(JavaActions.class,"CTL_SetOutput"));
        setOutputOption.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(JavaActions.class,"AD_SetOutput"));
        setOutputOption.setDefaultCapable(true);
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        String title = NbBundle.getMessage(JavaActions.class, "TITLE_set_outputs_dialog", commandDisplayName, projectDisplayName);
        String body = NbBundle.getMessage(JavaActions.class,"TEXT_set_outputs_dialog");
        NotifyDescriptor d = new NotifyDescriptor.Message (body, NotifyDescriptor.QUESTION_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);        
        d.setOptions(new Object[] {setOutputOption, NotifyDescriptor.CANCEL_OPTION});
        if (DialogDisplayer.getDefault().notify (d) == setOutputOption) {
            CustomizerProvider customizerProvider = (CustomizerProvider) project.getLookup().lookup (CustomizerProvider.class);
            assert customizerProvider != null;
            customizerProvider.showCustomizer();
            return true;
        }
        return false;
    }

    /**
     * Implementation of Compile File.
     */
    private void handleCompileSingle(Lookup context) throws IOException, SAXException {
        // XXX could also try copy + mod from build.xml? but less likely to have <compile> in an accessible place...
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_compile.single"), FILE_SCRIPT_PATH)) {
            return;
        }
        Document doc = readCustomScript(FILE_SCRIPT_PATH);
        ensurePropertiesCopied(doc.getDocumentElement());
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " ");
        doc.getDocumentElement().appendChild(comm);
        comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_more_info_x.single") + " ");
        doc.getDocumentElement().appendChild(comm);
        String propertyName = "files"; // NOI18N
        AntLocation root = findPackageRoot(context);
        assert root != null : context;
        Element target = createCompileSingleTarget(doc, context, propertyName, root);
        doc.getDocumentElement().appendChild(target);
        writeCustomScript(doc, FILE_SCRIPT_PATH);
        // XXX #53622: support also folders (i.e. just files w/o ext??):
        String pattern = "\\.java$"; // NOI18N
        String targetName = target.getAttribute("name");
        addBinding(ActionProvider.COMMAND_COMPILE_SINGLE, FILE_SCRIPT_PATH, targetName, propertyName, root.virtual, pattern, "relative-path", ","); // NOI18N
        jumpToBinding(ActionProvider.COMMAND_COMPILE_SINGLE);
        jumpToBuildScript(FILE_SCRIPT_PATH, targetName);
    }
    
    Element createCompileSingleTarget(Document doc, Lookup context, String propertyName, AntLocation root) {
        String targetName = "compile-selected-files-in-" + root.physical.getNameExt(); // NOI18N
        // XXX do a uniquification check
        Element target = doc.createElement("target"); // NOI18N
        target.setAttribute("name", targetName); // NOI18N
        Element fail = doc.createElement("fail"); // NOI18N
        fail.setAttribute("unless", propertyName); // NOI18N
        fail.appendChild(doc.createTextNode(NbBundle.getMessage(JavaActions.class, "COMMENT_must_set_property", propertyName)));
        target.appendChild(fail);
        String classesDir = findClassesOutputDir(root.virtual);
        if (classesDir == null) {
            target.appendChild(doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_must_set_build_classes_dir") + " "));
            classesDir = "${build.classes.dir}"; // NOI18N
        }
        Element mkdir = doc.createElement("mkdir"); // NOI18N
        mkdir.setAttribute("dir", classesDir); // NOI18N
        target.appendChild(mkdir);
        Element javac = doc.createElement("javac"); // NOI18N
        javac.setAttribute("srcdir", root.virtual); // NOI18N
        javac.setAttribute("destdir", classesDir); // NOI18N
        javac.setAttribute("includes", "${" + propertyName + "}"); // NOI18N
        String sourceLevel = findSourceLevel(root.virtual);
        if (sourceLevel != null) {
            javac.setAttribute("source", sourceLevel); // NOI18N
        }
        String cp = findCompileClasspath(root.virtual);
        if (cp != null) {
            Element classpath = doc.createElement("classpath"); // NOI18N
            classpath.setAttribute("path", cp); // NOI18N
            javac.appendChild(classpath);
        }
        target.appendChild(javac);
        return target;
    }
    
    private void handleDebug() throws IOException, SAXException {                        
        if (!this.setOutputsNotified) {
            ProjectModel pm = ProjectModel.createModel(Util.getProjectLocation(this.helper, this.evaluator),
                FileUtil.toFile(project.getProjectDirectory()), this.evaluator, this.helper);        
            List/*<ProjectModel.CompilationUnitKey>*/ cuKeys = pm.createCompilationUnitKeys();
            assert cuKeys != null;
            boolean hasOutputs = false;
            for (Iterator it = cuKeys.iterator(); it.hasNext();) {
                ProjectModel.CompilationUnitKey ck = (ProjectModel.CompilationUnitKey) it.next();
                JavaProjectGenerator.JavaCompilationUnit cu = pm.getCompilationUnit(ck,false);
                if (cu.output != null && cu.output.size()>0) {
                    hasOutputs = true;
                    break;
                }
            }
            if (!hasOutputs) {
                alertOutputs (NbBundle.getMessage(JavaActions.class, "ACTION_debug"));            
                this.setOutputsNotified = true;
                return;
            }
        }        
        String[] bindings = findCommandBinding(ActionProvider.COMMAND_RUN);
        Element task = null;
        Element origTarget = null;
        if (bindings != null && bindings.length <= 2) {
            origTarget = findExistingBuildTarget(ActionProvider.COMMAND_RUN);
            //The origTarget may be null if the user has removed it from build.xml
            if (origTarget != null) {
                task = targetUsesTaskExactlyOnce(origTarget, "java"); // NOI18N
            }
        }
        
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_debug"), task != null ? GENERAL_SCRIPT_PATH : FILE_SCRIPT_PATH)) {
            return;
        }
        
        String generatedTargetName = "debug-nb"; // NOI18N
        String generatedScriptPath;
        Document doc;
        Element generatedTarget;
        if (task != null) {
            // We can copy the original run target with some modifications.
            generatedScriptPath = GENERAL_SCRIPT_PATH;
            doc = readCustomScript(GENERAL_SCRIPT_PATH);
            ensureImports(doc.getDocumentElement(), bindings[0]);
            generatedTarget = createDebugTargetFromTemplate(generatedTargetName, origTarget, task, doc);
        } else {
            // No info, need to generate a dummy debug target.
            generatedScriptPath = FILE_SCRIPT_PATH;
            doc = readCustomScript(FILE_SCRIPT_PATH);
            ensurePropertiesCopied(doc.getDocumentElement());
            generatedTarget = createDebugTargetFromScratch(generatedTargetName, doc);
        }
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " ");
        doc.getDocumentElement().appendChild(comm);
        comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_more_info_debug") + " ");
        doc.getDocumentElement().appendChild(comm);
        doc.getDocumentElement().appendChild(generatedTarget);
        writeCustomScript(doc, generatedScriptPath);
        addBinding(ActionProvider.COMMAND_DEBUG, generatedScriptPath, generatedTargetName, null, null, null, null, null);
        jumpToBinding(ActionProvider.COMMAND_DEBUG);
        jumpToBuildScript(generatedScriptPath, generatedTargetName);                
    }
    
    private Element createNbjpdastart(Document ownerDocument) {
        Element nbjpdastart = ownerDocument.createElement("nbjpdastart"); // NOI18N
        nbjpdastart.setAttribute("name", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
        nbjpdastart.setAttribute("addressproperty", "jpda.address"); // NOI18N
        nbjpdastart.setAttribute("transport", "dt_socket"); // NOI18N
        return nbjpdastart;
    }
    
    private static final String[] DEBUG_VM_ARGS = {
        "-Xdebug", // NOI18N
        "-Xnoagent", // NOI18N
        "-Djava.compiler=none", // NOI18N
        "-Xrunjdwp:transport=dt_socket,address=${jpda.address}", // NOI18N
    };
    private void addDebugVMArgs(Element java, Document ownerDocument) {
        //Add fork="true" if not alredy there
        NamedNodeMap attrs = java.getAttributes();
        boolean found = false;
        for (int i=0; i<attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if ("fork".equals(attr.getName())) {        //NOI18N
                String value = attr.getValue();
                if ("on".equalsIgnoreCase (value) ||    //NOI18N
                    "true".equalsIgnoreCase(value) ||   //NOI18N
                    "yes".equalsIgnoreCase(value)) {    //NOI18N
                    found = true;
                }
                break;
            }
        }
        if (!found) {
            java.setAttribute("fork", "true");  //NOI18N
        }
        for (int i = 0; i < DEBUG_VM_ARGS.length; i++) {
            Element jvmarg = ownerDocument.createElement("jvmarg"); // NOI18N
            jvmarg.setAttribute("value", DEBUG_VM_ARGS[i]); // NOI18N
            java.appendChild(jvmarg);
        }
    }
    
    Element createDebugTargetFromTemplate(String generatedTargetName, Element origTarget, Element origTask, Document ownerDocument) {
        NodeList tasks = origTarget.getChildNodes();
        int taskIndex = -1;
        for (int i = 0; i < tasks.getLength(); i++) {
            if (tasks.item(i) == origTask) {
                taskIndex = i;
                break;
            }
        }
        assert taskIndex != -1;
        Element target = (Element) ownerDocument.importNode(origTarget, true);
        Element task = (Element) target.getChildNodes().item(taskIndex);
        target.setAttribute("name", generatedTargetName); // NOI18N
        Element nbjpdastart = createNbjpdastart(ownerDocument);
        String textualCp = task.getAttribute("classpath"); // NOI18N
        if (textualCp.length() > 0) {
            Element classpath = ownerDocument.createElement("classpath"); // NOI18N
            classpath.setAttribute("path", textualCp); // NOI18N
            nbjpdastart.appendChild(classpath);
        } else {
            NodeList origClasspath = task.getElementsByTagName("classpath"); // NOI18N
            if (origClasspath.getLength() == 1) {
                Element classpath = (Element) ownerDocument.importNode(origClasspath.item(0), true);
                nbjpdastart.appendChild(classpath);
            }
        }
        target.insertBefore(nbjpdastart, task);
        addDebugVMArgs(task, ownerDocument);
        return target;
    }
    
    Element createDebugTargetFromScratch(String generatedTargetName, Document ownerDocument) {
        Element target = ownerDocument.createElement("target");
        target.setAttribute("name", generatedTargetName); // NOI18N
        Element path = ownerDocument.createElement("path"); // NOI18N
        // XXX would be better to determine runtime CP from project.xml and put it here instead (if that is possible)...
        path.setAttribute("id", "cp"); // NOI18N
        path.appendChild(ownerDocument.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_set_runtime_cp") + " "));
        target.appendChild(path);
        Element nbjpdastart = createNbjpdastart(ownerDocument);
        Element classpath = ownerDocument.createElement("classpath"); // NOI18N
        classpath.setAttribute("refid", "cp"); // NOI18N
        nbjpdastart.appendChild(classpath);
        target.appendChild(nbjpdastart);
        target.appendChild(ownerDocument.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_set_main_class") + " "));
        Element java = ownerDocument.createElement("java"); // NOI18N
        java.setAttribute("classname", "some.main.Class"); // NOI18N
        classpath = ownerDocument.createElement("classpath"); // NOI18N
        classpath.setAttribute("refid", "cp"); // NOI18N
        java.appendChild(classpath);
        addDebugVMArgs(java, ownerDocument);
        target.appendChild(java);
        return target;
    }
    
    /**
     * Read a generated script if it exists, else create a skeleton.
     * @param scriptPath e.g. {@link #FILE_SCRIPT_PATH} or {@link #GENERAL_SCRIPT_PATH}
     */
    Document readCustomScript(String scriptPath) throws IOException, SAXException {
        // XXX if there is TAX support for rewriting XML files, use that here...
        FileObject script = helper.getProjectDirectory().getFileObject(scriptPath);
        if (script != null) {
            InputStream is = script.getInputStream();
            try {
                return XMLUtil.parse(new InputSource(is), false, true, null, null);
            } finally {
                is.close();
            }
        } else {
            Document doc = XMLUtil.createDocument("project", /*XXX:"antlib:org.apache.tools.ant"*/null, null, null); // NOI18N
            Element root = doc.getDocumentElement();
            String projname = ProjectUtils.getInformation(project).getDisplayName();
            root.setAttribute("name", NbBundle.getMessage(JavaActions.class, "LBL_generated_script_name", projname));
            return doc;
        }
    }
    
    /**
     * Make sure that if the project defines ${project.dir} in project.xml that
     * a custom build script also defines this property.
     * Generally, copy any properties defined in project.xml to Ant syntax.
     * Used for generated targets which essentially copy Ant fragments from project.xml
     * (rather than the user's build.xml).
     * Also sets the basedir to the (IDE) project directory.
     * Idempotent, takes effect only once.
     * Use with {@link #FILE_SCRIPT_PATH}.
     * @param antProject XML of an Ant project (document element)
     */
    void ensurePropertiesCopied(Element antProject) {
        if (antProject.getAttribute("basedir").length() > 0) {
            // Do not do it twice to the same script.
            return;
        }
        antProject.setAttribute("basedir", /* ".." times count('/', FILE_SCRIPT_PATH) */".."); // NOI18N
        // Look for <properties> in project.xml and make corresponding definitions in the Ant script.
        Element data = helper.getPrimaryConfigurationData(true);
        Element properties = Util.findElement(data, "properties", NS_GENERAL);
        if (properties != null) {
            Iterator/*<Element>*/ propertiesIt = Util.findSubElements(properties).iterator();
            while (propertiesIt.hasNext()) {
                Element el = (Element) propertiesIt.next();
                Element nue = antProject.getOwnerDocument().createElement("property"); // NOI18N
                if (el.getLocalName().equals("property")) { // NOI18N
                    String name = el.getAttribute("name"); // NOI18N
                    assert name != null;
                    String text = Util.findText(el);
                    assert text != null;
                    nue.setAttribute("name", name);
                    nue.setAttribute("value", text);
                } else if (el.getLocalName().equals("property-file")) { // NOI18N
                    String text = Util.findText(el);
                    assert text != null;
                    nue.setAttribute("file", text);
                } else {
                    assert false : el;
                }
                antProject.appendChild(nue);
            }
        }
    }
    
    /**
     * Make sure that the custom build script imports the original build script
     * and is using the same base dir.
     * Used for generated targets which essentially copy Ant targets from build.xml.
     * Use with {@link #GENERAL_SCRIPT_PATH}.
     * Idempotent, takes effect only once.
     * @param antProject XML of an Ant project (document element)
     * @oaram origScriptPath Ant name of original build script's path
     */
    void ensureImports(Element antProject, String origScriptPath) throws IOException, SAXException {
        if (antProject.getAttribute("basedir").length() > 0) {
            // Do not do it twice to the same script.
            return;
        }
        String origScriptPathEval = evaluator.evaluate(origScriptPath);
        if (origScriptPathEval == null) {
            // Can't do anything, forget it.
            return;
        }
        String origScriptURI = helper.resolveFile(origScriptPathEval).toURI().toString();
        Document origScriptDocument = XMLUtil.parse(new InputSource(origScriptURI), false, true, null, null);
        String origBasedir = origScriptDocument.getDocumentElement().getAttribute("basedir"); // NOI18N
        if (origBasedir.length() == 0) {
            origBasedir = "."; // NOI18N
        }
        String basedir, importPath;
        File origScript = new File(origScriptPathEval);
        if (origScript.isAbsolute()) {
            // Use full path.
            importPath = origScriptPathEval;
            if (new File(origBasedir).isAbsolute()) {
                basedir = origBasedir;
            } else {
                basedir = PropertyUtils.resolveFile(origScript.getParentFile(), origBasedir).getAbsolutePath();
            }
        } else {
            // Import relative to that path.
            // Note that <import>'s path is always relative to the location of the importing script, regardless of the basedir.
            String prefix = /* ".." times count('/', FILE_SCRIPT_PATH) */"../"; // NOI18N
            importPath = prefix + origScriptPathEval;
            if (new File(origBasedir).isAbsolute()) {
                basedir = origBasedir;
            } else {
                int slash = origScriptPathEval.replace(File.separatorChar, '/').lastIndexOf('/');
                if (slash == -1) {
                    basedir = prefix + origBasedir;
                } else {
                    basedir = prefix + origScriptPathEval.substring(0, slash + 1) + origBasedir;
                }
                // Trim:
                basedir = basedir.replaceAll("/\\.$", ""); // NOI18N
            }
        }
        antProject.setAttribute("basedir", basedir); // NOI18N
        Element importEl = antProject.getOwnerDocument().createElement("import"); // NOI18N
        importEl.setAttribute("file", importPath); // NOI18N
        antProject.appendChild(importEl);
    }
    
    /**
     * Write a script with a new or modified document.
     * @param scriptPath e.g. {@link #FILE_SCRIPT_PATH} or {@link #GENERAL_SCRIPT_PATH}
     */
    void writeCustomScript(Document doc, String scriptPath) throws IOException {
        FileObject script = helper.getProjectDirectory().getFileObject(scriptPath);
        if (script == null) {
            script = FileUtil.createData(helper.getProjectDirectory(), scriptPath);
        }
        FileLock lock = script.lock();
        try {
            OutputStream os = script.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private Iterator/*<Element>*/ compilationUnitsIterator() {
        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        if (java == null) {
            return Collections.EMPTY_SET.iterator();
        }
        List/*<Element>*/ compilationUnits = Util.findSubElements(java);
        return compilationUnits.iterator();
    }
    
    /**
     * Find a Java package root in which the selection is contained.
     * @param context lookup with Java source files and/or folders and/or junk
     * @return the package root if there is one, or null if the lookup is empty, has junk, or has multiple roots
     */
    AntLocation findPackageRoot(Lookup context) {
        Iterator/*<Element>*/ cuIt = compilationUnitsIterator();
        while (cuIt.hasNext()) {
            Element compilationUnitEl = (Element) cuIt.next();
            assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
            List/*<String>*/ packageRootNames = Classpaths.findPackageRootNames(compilationUnitEl);
            Map/*<String,FileObject>*/ packageRootsByName = Classpaths.findPackageRootsByName(helper, evaluator, packageRootNames);
            Iterator/*<Map.Entry<String,FileObject>>*/ it = packageRootsByName.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry/*<String,FileObject>*/ entry = (Map.Entry) it.next();
                FileObject root = (FileObject) entry.getValue();
                if (containsSelectedJavaSources(root, context)) {
                    return new AntLocation((String) entry.getKey(), root);
                }
            }
        }
        // Couldn't find it.
        return null;
    }
    
    /**
     * Check to see if a (node-like) selection contains one or more Java sources (or folders) inside the root.
     */
    static boolean containsSelectedJavaSources(FileObject root, Lookup context) {
        Set/*<FileObject>*/ selection = new HashSet(context.lookup(new Lookup.Template(FileObject.class)).allInstances());
        Iterator/*<DataObject>*/ selectionDO = context.lookup(new Lookup.Template(DataObject.class)).allInstances().iterator();
        while (selectionDO.hasNext()) {
            DataObject dob = (DataObject) selectionDO.next();
            selection.add(dob.getPrimaryFile());
        }
        if (selection.isEmpty()) {
            return false;
        }
        Iterator/*<FileObject>*/ selIt = selection.iterator();
        while (selIt.hasNext()) {
            FileObject f = (FileObject) selIt.next();
            if (f.isData() && !f.hasExt("java")) { // NOI18N
                return false;
            }
            if (f != root && !FileUtil.isParentOf(root, f)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Represents a location as referred to by the Ant script.
     * Contains both its logical representation, and actual (current) location (or null).
     */
    static final class AntLocation {
        public final String virtual;
        public final FileObject physical;
        public AntLocation(String virtual, FileObject physical) {
            this.virtual = virtual;
            this.physical = physical;
        }
        public String toString() {
            return "AntLocation[" + virtual + "=" + physical + "]"; // NOI18N
        }
    }
    
    /**
     * Try to find the compilation unit containing a source root.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return the compilation unit owning it, or null if not found
     */
    private Element findCompilationUnit(String sources) {
        Iterator/*<Element>*/ cuIt = compilationUnitsIterator();
        while (cuIt.hasNext()) {
            Element compilationUnitEl = (Element) cuIt.next();
            Iterator/*<Element>*/ packageRoots = Util.findSubElements(compilationUnitEl).iterator();
            while (packageRoots.hasNext()) {
                Element packageRoot = (Element) packageRoots.next();
                if (packageRoot.getLocalName().equals("package-root")) { // NOI18N
                    if (Util.findText(packageRoot).equals(sources)) {
                        return compilationUnitEl;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Try to determine where classes from a given package root should be compiled to.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return an output directory (never a JAR), as a virtual name, or null if none could be found
     */
    String findClassesOutputDir(String sources) {
        Element compilationUnitEl = findCompilationUnit(sources);
        if (compilationUnitEl != null) {
            return findClassesOutputDir(compilationUnitEl);
        } else {
            return null;
        }
    }
    
    /**
     * Find output classes given a compilation unit from project.xml.
     */
    private String findClassesOutputDir(Element compilationUnitEl) {
        // Look for an appropriate <built-to>.
        Iterator/*<Element>*/ builtTos = Util.findSubElements(compilationUnitEl).iterator();
        while (builtTos.hasNext()) {
            Element builtTo = (Element) builtTos.next();
            if (builtTo.getLocalName().equals("built-to")) { // NOI18N
                String rawtext = Util.findText(builtTo);
                // Check that it is not an archive.
                String evaltext = evaluator.evaluate(rawtext);
                if (evaltext != null) {
                    File dest = helper.resolveFile(evaltext);
                    URL destU;
                    try {
                        destU = dest.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                    if (!FileUtil.isArchiveFile(destU)) {
                        // OK, dir, take it.
                        return rawtext;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Try to find the source level corresponding to a source root.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return the source level, or null if none was specified or there was no such source root
     */
    String findSourceLevel(String sources) {
        Element compilationUnitEl = findCompilationUnit(sources);
        if (compilationUnitEl != null) {
            Element sourceLevel = Util.findElement(compilationUnitEl, "source-level", JavaProjectNature.NS_JAVA_2);
            if (sourceLevel != null) {
                return Util.findText(sourceLevel);
            }
        }
        return null;
    }
    
    /**
     * Try to find the compile-time classpath corresponding to a source root.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return the classpath (in Ant form), or null if none was specified or there was no such source root
     */
    String findCompileClasspath(String sources) {
        Element compilationUnitEl = findCompilationUnit(sources);
        if (compilationUnitEl != null) {
            Iterator/*<Element>*/ classpaths = Util.findSubElements(compilationUnitEl).iterator();
            while (classpaths.hasNext()) {
                Element classpath = (Element) classpaths.next();
                if (classpath.getLocalName().equals("classpath")) { // NOI18N
                    String mode = classpath.getAttribute("mode"); // NOI18N
                    if (mode.equals("compile")) { // NOI18N
                        return Util.findText(classpath);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Add an action binding to project.xml.
     * If there is no required context, the action is also added to the context menu of the project node.
     * @param command the command name
     * @param scriptPath the path to the generated script
     * @param target the name of the target (in scriptPath)
     * @param propertyName a property name to hold the selection (or null for no context, in which case remainder should be null)
     * @param dir the raw text to use for the directory name
     * @param pattern the regular expression to match, or null
     * @param format the format to use
     * @param separator the separator to use for multiple files, or null for single file only
     */
    void addBinding(String command, String scriptPath, String target, String propertyName, String dir, String pattern, String format, String separator) throws IOException {
        // XXX cannot use FreeformProjectGenerator since that is currently not a public support SPI from ant/freeform
        // XXX should this try to find an existing binding? probably not, since it is assumed that if there was one, we would never get here to begin with
        Element data = helper.getPrimaryConfigurationData(true);
        Element ideActions = Util.findElement(data, "ide-actions", NS_GENERAL); // NOI18N
        if (ideActions == null) {
            // Probably won't happen, since generator produces it always.
            // Not trivial to just add it now, since order is significant in the schema. (FPG deals with these things.)
            return;
        }
        Document doc = data.getOwnerDocument();
        Element action = doc.createElementNS(NS_GENERAL, "action"); // NOI18N
        action.setAttribute("name", command); // NOI18N
        Element script = doc.createElementNS(NS_GENERAL, "script"); // NOI18N
        script.appendChild(doc.createTextNode(scriptPath));
        action.appendChild(script);
        Element targetEl = doc.createElementNS(NS_GENERAL, "target"); // NOI18N
        targetEl.appendChild(doc.createTextNode(target));
        action.appendChild(targetEl);
        if (propertyName != null) {
            Element context = doc.createElementNS(NS_GENERAL, "context"); // NOI18N
            Element property = doc.createElementNS(NS_GENERAL, "property"); // NOI18N
            property.appendChild(doc.createTextNode(propertyName));
            context.appendChild(property);
            Element folder = doc.createElementNS(NS_GENERAL, "folder"); // NOI18N
            folder.appendChild(doc.createTextNode(dir));
            context.appendChild(folder);
            if (pattern != null) {
                Element patternEl = doc.createElementNS(NS_GENERAL, "pattern"); // NOI18N
                patternEl.appendChild(doc.createTextNode(pattern));
                context.appendChild(patternEl);
            }
            Element formatEl = doc.createElementNS(NS_GENERAL, "format"); // NOI18N
            formatEl.appendChild(doc.createTextNode(format));
            context.appendChild(formatEl);
            Element arity = doc.createElementNS(NS_GENERAL, "arity"); // NOI18N
            if (separator != null) {
                Element separatorEl = doc.createElementNS(NS_GENERAL, "separated-files"); // NOI18N
                separatorEl.appendChild(doc.createTextNode(separator));
                arity.appendChild(separatorEl);
            } else {
                arity.appendChild(doc.createElementNS(NS_GENERAL, "one-file-only")); // NOI18N
            }
            context.appendChild(arity);
            action.appendChild(context);
        } else {
            // Add a context menu item, since it applies to the project as a whole.
            // Assume there is already a <context-menu> defined, which is quite likely.
            Element view = Util.findElement(data, "view", NS_GENERAL); // NOI18N
            if (view != null) {
                Element contextMenu = Util.findElement(view, "context-menu", NS_GENERAL); // NOI18N
                if (contextMenu != null) {
                    Element ideAction = doc.createElementNS(NS_GENERAL, "ide-action"); // NOI18N
                    ideAction.setAttribute("name", command); // NOI18N
                    contextMenu.appendChild(ideAction);
                }
            }
        }
        ideActions.appendChild(action);
        helper.putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(project);
    }

    /**
     * Jump to a target in the editor.
     * @param scriptPath the script to open
     * @param target the name of the target (in scriptPath)
     */
    private void jumpToBuildScript(String scriptPath, String target) {
        jumpToFile(scriptPath, target, "target", "name"); // NOI18N
    }
    
    /**
     * Jump to an action binding in the editor.
     * @param command an {@link ActionProvider} command name found in project.xml
     */
    private void jumpToBinding(String command) {
        jumpToFile(AntProjectHelper.PROJECT_XML_PATH, command, "action", "name"); // NOI18N
    }

    /**
     * Jump to some line in an XML file.
     * @param path project-relative path to the file
     * @param match {@see #findLine}
     * @param elementLocalName {@see #findLine}
     * @param elementAttributeName {@see #findLine}
     */
    private void jumpToFile(String path, String match, String elementLocalName, String elementAttributeName) {
        FileObject file = helper.getProjectDirectory().getFileObject(path);
        if (file == null) {
            return;
        }
        int line;
        try {
            line = findLine(file, match, elementLocalName, elementAttributeName);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        if (line == -1) {
            // Just open it.
            line = 0;
        }
        DataObject fileDO;
        try {
            fileDO = DataObject.find(file);
        } catch (DataObjectNotFoundException e) {
            throw new AssertionError(e);
        }
        LineCookie lines = (LineCookie) fileDO.getCookie(LineCookie.class);
        if (lines != null) {
            try {
                lines.getLineSet().getCurrent(line).show(Line.SHOW_GOTO);
            } catch (IndexOutOfBoundsException e) {
                // XXX reproducibly thrown if the document was already open. Why?? (file.refresh() above does not help.)
                ErrorManager.getDefault().getInstance(JavaActions.class.getName()).log(ErrorManager.WARNING, e + " [file=" + file + " match=" + match + " line=" + line + "]"); // NOI18N
                lines.getLineSet().getCurrent(0).show(Line.SHOW_GOTO);
            }
        }
    }
    
    /**
     * Find the line number of a target in an Ant script, or some other line in an XML file.
     * Able to find a certain element with a certain attribute matching a given value.
     * See also AntTargetNode.TargetOpenCookie.
     * @param file an Ant script or other XML file
     * @param match the attribute value to match (e.g. target name)
     * @param elementLocalName the (local) name of the element to look for
     * @param elementAttributeName the name of the attribute to match on
     * @return the line number (0-based), or -1 if not found
     */
    static final int findLine(FileObject file, final String match, final String elementLocalName, final String elementAttributeName) throws IOException, SAXException, ParserConfigurationException {
        InputSource in = new InputSource(file.getURL().toString());
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        final int[] line = new int[] {-1};
        class Handler extends DefaultHandler {
            private Locator locator;
            public void setDocumentLocator(Locator l) {
                locator = l;
            }
            public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                if (line[0] == -1) {
                    if (localname.equals(elementLocalName) && match.equals(attr.getValue(elementAttributeName))) { // NOI18N
                        line[0] = locator.getLineNumber() - 1;
                    }
                }
            }
        }
        parser.parse(in, new Handler());
        return line[0];
    }
    
    /**
     * Attempt to find the Ant build script target bound to a given IDE command.
     * @param command an {@link ActionProvider} command
     * @return the XML for the target if it could be found (and there was no more than one target bound), else null
     */
    Element findExistingBuildTarget(String command) throws IOException, SAXException {
        String[] binding = findCommandBinding(command);
        if (binding == null) {
            return null;
        }
        String scriptName = binding[0];
        assert scriptName != null;
        String targetName;
        if (binding.length == 1) {
            targetName = null;
        } else if (binding.length == 2) {
            targetName = binding[1];
        } else {
            // Too many bindings; we do not support this.
            return null;
        }
        String scriptPath = evaluator.evaluate(scriptName);
        if (scriptPath == null) {
            return null;
        }
        File scriptFile = helper.resolveFile(scriptPath);
        String scriptURI = scriptFile.toURI().toString();
        Document doc = XMLUtil.parse(new InputSource(scriptURI), false, true, null, null);
        if (targetName == null) {
            targetName = doc.getDocumentElement().getAttribute("default"); // NOI18N
            if (targetName == null) {
                return null;
            }
        }
        Iterator/*<Element>*/ targets = Util.findSubElements(doc.getDocumentElement()).iterator();
        while (targets.hasNext()) {
            Element target = (Element) targets.next();
            if (target.getLocalName().equals("target") && targetName.equals(target.getAttribute("name"))) { // NOI18N
                return target;
            }
        }
        return null;
    }

    /**
     * Find the target binding for some command.
     * @param command an {@link ActionProvider} command
     * @return an array of a script name (Ant syntax, never null) and zero or more target names (none means default target)
     *         or null if no binding could be found for this command
     */
    String[] findCommandBinding(String command) {
        Element data = helper.getPrimaryConfigurationData(true);
        Element ideActions = Util.findElement(data, "ide-actions", NS_GENERAL); // NOI18N
        if (ideActions == null) {
            return null;
        }
        String scriptName = "build.xml"; // NOI18N
        Iterator/*<Element>*/ actions = Util.findSubElements(ideActions).iterator();
        while (actions.hasNext()) {
            Element action = (Element) actions.next();
            assert action.getLocalName().equals("action");
            if (action.getAttribute("name").equals(command)) {
                Element script = Util.findElement(action, "script", NS_GENERAL); // NOI18N
                if (script != null) {
                    scriptName = Util.findText(script);
                }
                List/*<String>*/ scriptPlusTargetNames = new ArrayList();
                scriptPlusTargetNames.add(scriptName);
                Iterator/*<Element>*/ targets = Util.findSubElements(action).iterator();
                while (targets.hasNext()) {
                    Element target = (Element) targets.next();
                    if (target.getLocalName().equals("target")) { // NOI18N
                        scriptPlusTargetNames.add(Util.findText(target));
                    }
                }
                return (String[]) scriptPlusTargetNames.toArray(new String[scriptPlusTargetNames.size()]);
            }
        }
        return null;
    }
    
    /**
     * Check to see if a given Ant target uses a given task once (and only once).
     * @param target an Ant <code>&lt;target&gt;</code> element
     * @param taskName the (unqualified) name of an Ant task
     * @return a task element with that name, or null if there is none or more than one
     */
    Element targetUsesTaskExactlyOnce(Element target, String taskName) {
        // XXX should maybe also look for any other usage of the task in the same script in case there is none in the mentioned target
        Iterator/*<Element>*/ tasks = Util.findSubElements(target).iterator();
        Element foundTask = null;
        while (tasks.hasNext()) {
            Element task = (Element) tasks.next();
            if (task.getLocalName().equals(taskName)) {
                if (foundTask != null) {
                    // Duplicate.
                    return null;
                } else {
                    foundTask = task;
                }
            }
        }
        return foundTask;
    }

}
