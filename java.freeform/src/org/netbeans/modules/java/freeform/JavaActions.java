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
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// XXX might work better to look for existing <javac> etc. from build.xml & copy w/ mods...

/**
 * Handles providing implementations of some Java-oriented IDE-specific actions.
 * @author Jesse Glick
 * @see "issue #46886"
 */
final class JavaActions implements ActionProvider {
    
    private static final String NS_GENERAL = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    
    private static final String[] ACTIONS = {
        ActionProvider.COMMAND_COMPILE_SINGLE,
        // XXX more
    };
    
    static final String SCRIPT_PATH = "nbproject/ide-targets.xml"; // NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    
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
     * @return true if IDE should proceed
     */
    private boolean alert(String commandDisplayName) {
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        String title = NbBundle.getMessage(JavaActions.class, "TITLE_generate_target_dialog", commandDisplayName, projectDisplayName);
        String body = NbBundle.getMessage(JavaActions.class, "TEXT_generate_target_dialog", commandDisplayName, SCRIPT_PATH);
        NotifyDescriptor d = new NotifyDescriptor.Message(body, NotifyDescriptor.QUESTION_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton generate = new JButton(NbBundle.getMessage(JavaActions.class, "LBL_generate"));
        generate.setDefaultCapable(true);
        d.setOptions(new Object[] {generate, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == generate;
    }
    
    private void handleCompileSingle(Lookup context) throws IOException, SAXException {
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_compile.single"))) {
            return;
        }
        Document doc = readCustomScript();
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " ");
        doc.getDocumentElement().appendChild(comm);
        String propertyName = "files"; // NOI18N
        AntLocation root = findPackageRoot(context);
        assert root != null : context;
        Element target = createCompileSingleTarget(doc, context, propertyName, root);
        doc.getDocumentElement().appendChild(target);
        writeCustomScript(doc);
        // XXX support also folders (i.e. just files w/o ext??):
        String pattern = "\\.java$"; // NOI18N
        String targetName = target.getAttribute("name");
        addBinding(ActionProvider.COMMAND_COMPILE_SINGLE, targetName, propertyName, root.virtual, pattern, "relative-path", ","); // NOI18N
        jumpTo(targetName);
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
        if (classesDir != null) {
            Element mkdir = doc.createElement("mkdir"); // NOI18N
            mkdir.setAttribute("dir", classesDir); // NOI18N
            target.appendChild(mkdir);
        }
        Element javac = doc.createElement("javac"); // NOI18N
        javac.setAttribute("srcdir", root.virtual); // NOI18N
        if (classesDir != null) {
            javac.setAttribute("destdir", classesDir); // NOI18N
        }
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
    
    /**
     * Read {@link #SCRIPT_PATH} if it exists, else create a skeleton.
     */
    Document readCustomScript() throws IOException, SAXException {
        // XXX if there is TAX support for rewriting XML files, use that here...
        FileObject script = helper.getProjectDirectory().getFileObject(SCRIPT_PATH);
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
            root.setAttribute("basedir", /* ".." times count('/', SCRIPT_PATH) */".."); // NOI18N
            String projname = ProjectUtils.getInformation(project).getDisplayName();
            root.setAttribute("name", NbBundle.getMessage(JavaActions.class, "LBL_generated_script_name", projname));
            String projectDir = evaluator.getProperty("project.dir"); // NOI18N
            if (projectDir != null) {
                // Need to define this in the script, too, or else external source roots will not work.
                Element property = doc.createElement("property"); // NOI18N
                property.setAttribute("name", "project.dir"); // NOI18N
                property.setAttribute("location", projectDir); // NOI18N
                root.appendChild(property);
            }
            return doc;
        }
    }
    
    /**
     * Write {@link #SCRIPT_PATH} with a new or modified document.
     */
    void writeCustomScript(Document doc) throws IOException {
        FileObject script = helper.getProjectDirectory().getFileObject(SCRIPT_PATH);
        if (script == null) {
            script = FileUtil.createData(helper.getProjectDirectory(), SCRIPT_PATH);
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
        // XXX should this also hold a Element compilationUnit (for source packages)? in a subclass? or similar info?
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
     * @param command the command name
     * @param target the name of the target (in {@link #SCRIPT_PATH})
     * @param propertyName a property name to hold the selection
     * @param dir the raw text to use for the directory name
     * @param pattern the regular expression to match, or null
     * @param format the format to use
     * @param separator the separator to use for multiple files, or null for single file only
     */
    void addBinding(String command, String target, String propertyName, String dir, String pattern, String format, String separator) throws IOException {
        // XXX cannot use FreeformProjectGenerator since that is currently not a public support SPI from ant/freeform
        // XXX should this try to find an existing binding? probably not, since it is assumed that if there was one, we would never get here to begin with
        Element data = helper.getPrimaryConfigurationData(true);
        Element ideActions = Util.findElement(data, "ide-actions", NS_GENERAL); // NOI18N
        if (ideActions == null) {
            // XXX probably won't happen, since generator produces it always
            return;
        }
        Document doc = data.getOwnerDocument();
        Element action = doc.createElementNS(NS_GENERAL, "action"); // NOI18N
        action.setAttribute("name", command); // NOI18N
        Element script = doc.createElementNS(NS_GENERAL, "script"); // NOI18N
        script.appendChild(doc.createTextNode(SCRIPT_PATH));
        action.appendChild(script);
        Element targetEl = doc.createElementNS(NS_GENERAL, "target"); // NOI18N
        targetEl.appendChild(doc.createTextNode(target));
        action.appendChild(targetEl);
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
        ideActions.appendChild(action);
        helper.putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(project);
    }

    /**
     * Jump to a target in the editor.
     * @param target the name of the target (in {@link #SCRIPT_PATH})
     */
    private void jumpTo(String target) {
        FileObject script = helper.getProjectDirectory().getFileObject(SCRIPT_PATH);
        assert script != null;
        int line;
        try {
            line = findLine(script, target);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        if (line == -1) {
            return;
        }
        DataObject scriptDO;
        try {
            scriptDO = DataObject.find(script);
        } catch (DataObjectNotFoundException e) {
            throw new AssertionError(e);
        }
        LineCookie lines = (LineCookie) scriptDO.getCookie(LineCookie.class);
        if (lines != null) {
            lines.getLineSet().getCurrent(line).show(Line.SHOW_GOTO);
        }
    }
    
    /**
     * Find the line number of a target in an Ant script.
     * See also AntTargetNode.TargetOpenCookie.
     * @return the line number (0-based), or -1 if not found
     */
    static int findLine(FileObject script, final String target) throws IOException, SAXException, ParserConfigurationException {
        InputSource in = new InputSource(script.getURL().toString());
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        final int[] line = new int[] {-1};
        class Handler extends DefaultHandler {
            private Locator locator;
            public void setDocumentLocator(Locator l) {
                locator = l;
            }
            public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                if (line[0] == -1) {
                    if (qname.equals("target") && target.equals(attr.getValue("name"))) { // NOI18N
                        line[0] = locator.getLineNumber() - 1;
                    }
                }
            }
        }
        parser.parse(in, new Handler());
        return line[0];
    }

}
