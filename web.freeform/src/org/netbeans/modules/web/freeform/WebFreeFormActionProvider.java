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

package org.netbeans.modules.web.freeform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
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

/**
 * Handles providing implementations of some Web-oriented IDE-specific actions.
 *
 * @author Libor Kotouc
 */
public class WebFreeFormActionProvider implements ActionProvider {
    
    static final String NS_GENERAL = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N

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
    
    static final String PROJECT_PROPERTIES_PATH = "nbproject/project.properties";
    
    private static final String LOAD_PROPS_TARGET = "-load-props";
    private static final String CHECK_PROPS_TARGET = "-check-props";
    private static final String INIT_TARGET = "-init";
    private static final String DEBUG_TARGET = "debug-nb";
    private static final String DISPLAY_BROWSER = "debug-display-browser";

    private static final String[] projectProperties = 
            new String[] {
                        "session.name",
                        "jpda.host",
                        "jpda.address",
                        "jpda.transport",
                        "web.docbase.dir",
                        "debug.sourcepath",
                        "client.url"
            };
    
    private final Project project;
    private final AntProjectHelper helper;

    private static final String[] SUPPORTED_ACTIONS = {
        ActionProvider.COMMAND_DEBUG,
    };
    
    /** Creates a new instance of WebFreeFormActionProvider */
    public WebFreeFormActionProvider(Project aProject, AntProjectHelper aHelper) {
        project = aProject;
        helper = aHelper;
    }

    public boolean isActionEnabled(String command, org.openide.util.Lookup context) throws IllegalArgumentException {
        boolean enabled = false;
        if (command.equals(ActionProvider.COMMAND_DEBUG))
            enabled = true;
        return enabled;
    }

    public void invokeAction(String command, org.openide.util.Lookup context) throws IllegalArgumentException {
        try {
            try {
                if (command.equals(ActionProvider.COMMAND_DEBUG))
                    handleDebug();
                } catch (SAXException e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }
    
    private void handleDebug() throws IOException, SAXException {
        //allow user to confirm target generation
        if (!alert(NbBundle.getMessage(WebFreeFormActionProvider.class, "ACTION_debug"), GENERAL_SCRIPT_PATH))
            return;
        
        //let's generate a debug target
        
        //read script document
        Document script = readCustomScript(GENERAL_SCRIPT_PATH);
        if (script == null) //script doesn't exist
            script = createCustomScript();

        //append comments and target
        writeComments(script);
        writeTargets(script);
        
        //save script
        writeCustomScript(script, GENERAL_SCRIPT_PATH);
        
        //write changes to project.xml
        addBinding(ActionProvider.COMMAND_DEBUG, GENERAL_SCRIPT_PATH, DEBUG_TARGET, null, null, null, null, null);
        
        //show the result
        jumpToBinding(ActionProvider.COMMAND_DEBUG);
        jumpToBuildScript(GENERAL_SCRIPT_PATH, DEBUG_TARGET);
        
    }
    
    /**
     * Display an alert asking the user whether to really generate a target.
     * @param commandDisplayName the display name of the action to be bound
     * @param scriptPath the path that to the script that will be generated or written to
     * @return true if IDE should proceed
     */
    private boolean alert(String commandDisplayName, String scriptPath) {
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        String title = NbBundle.getMessage(WebFreeFormActionProvider.class, "TITLE_generate_target_dialog", commandDisplayName, projectDisplayName);
        String body = NbBundle.getMessage(WebFreeFormActionProvider.class, "TEXT_generate_target_dialog", commandDisplayName, scriptPath);
        NotifyDescriptor d = new NotifyDescriptor.Message(body, NotifyDescriptor.QUESTION_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton generate = new JButton(NbBundle.getMessage(WebFreeFormActionProvider.class, "LBL_generate"));
        generate.setDefaultCapable(true);
        d.setOptions(new Object[] {generate, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == generate;
    }

    /**
     * Reads a generated script if it exists, else create a skeleton.
     * @param scriptPath e.g. {@link #FILE_SCRIPT_PATH} or {@link #GENERAL_SCRIPT_PATH}
     * @return script document.
     */
    Document readCustomScript(String scriptPath) throws IOException, SAXException {

        Document script = null;
        FileObject scriptFile = helper.getProjectDirectory().getFileObject(scriptPath);
        if (scriptFile != null) {
            InputStream is = scriptFile.getInputStream();
            try {
                script = XMLUtil.parse(new InputSource(is), false, true, null, null);
            } finally {
                is.close();
            }
        }
        
        return script;
    }
    
    /**
     * Creates custom script.
     * @return script document.
     */
    Document createCustomScript() {
        //create document, set root and its attributes
        Document script = XMLUtil.createDocument("project", null, null, null); // NOI18N
        Element scriptRoot = script.getDocumentElement();
        scriptRoot.setAttribute("basedir", /* ".." times count('/', FILE_SCRIPT_PATH) */".."); // NOI18N
        String projname = ProjectUtils.getInformation(project).getDisplayName();
        scriptRoot.setAttribute("name", NbBundle.getMessage(WebFreeFormActionProvider.class, "LBL_generated_script_name", projname));

        //copy properties from project.xml to the script
        copyProperties(helper.getPrimaryConfigurationData(true), scriptRoot);
        
        return script;
    }

    /**
     * Copies all properties defined in project.xml to Ant syntax.
     * Used for generated targets which essentially copy Ant fragments from project.xml
     * (rather than the user's build.xml).
     * @param config XML of an Ant project (document element)
     * @param script target custom script
     */
    private void copyProperties(Element config, Element script) {
        // Look for <properties> in project.xml and make corresponding definitions in the Ant script.
        // See corresponding schema.
        
        Element data = helper.getPrimaryConfigurationData(true);
        Element properties = Util.findElement(data, "properties", NS_GENERAL);
        if (properties != null) {
            Iterator/*<Element>*/ propertiesIt = Util.findSubElements(properties).iterator();
            while (propertiesIt.hasNext()) {
                Element el = (Element) propertiesIt.next();
                Element nue = script.getOwnerDocument().createElement("property"); // NOI18N
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
                script.appendChild(nue);
            }
        }
    }

    /**
     * Appends the comments to script.
     * @param script Script to write to.
     */
    private void writeComments(Document script) {
        Comment comm4Edit = script.createComment(" " + NbBundle.getMessage(WebFreeFormActionProvider.class, "COMMENT_edit_target") + " ");
        Comment comm4Info = script.createComment(" " + NbBundle.getMessage(WebFreeFormActionProvider.class, "COMMENT_more_info_debug") + " ");

        Element scriptRoot = script.getDocumentElement();
        scriptRoot.appendChild(comm4Edit);
        scriptRoot.appendChild(comm4Info);
    }
    
    /**
     * Appends necessary targets to script.
     * @param script Script to write to.
     */
    private void writeTargets(Document script) {
        createLoadPropertiesTarget(script);
        createCheckPropertiesTarget(script);
        createInitTarget(script);
        createDebugTarget(script);
        createDisplayBrowserTarget(script);
    }

    /**
     * Creates target:
     *  <target name="-load-props">
     *      <property file="nbproject/project.properties"/>
     *  </target>
     * @param script Script to write to.
     */
    private void createLoadPropertiesTarget(Document script) {
        Element target = script.createElement("target");
        target.setAttribute("name", LOAD_PROPS_TARGET); // NOI18N
        Element property = script.createElement("property");
        property.setAttribute("file", PROJECT_PROPERTIES_PATH);// NOI18N
        target.appendChild(property);
        script.getDocumentElement().appendChild(target);
    }
    
    /**
     * Creates target:
     * <target name="-check-props">
     *     <fail unless="session.name"/>
     *     <fail unless="jpda.host"/>
     *     <fail unless="jpda.address"/>
     *     <fail unless="jpda.transport"/>
     *     <fail unless="web.docbase.dir"/>
     *     <fail unless="debug.sourcepath"/>
     *     <fail unless="client.url"/>
     * </target>
     * @param script Script to write to.
     */
    private void createCheckPropertiesTarget(Document script) {
        Element target = script.createElement("target");
        target.setAttribute("name", CHECK_PROPS_TARGET); // NOI18N
        Element fail;
        for (int i = 0; i < projectProperties.length; i++) {
            fail = script.createElement("fail");
            fail.setAttribute("unless", projectProperties[i]);
            target.appendChild(fail);
        }
        
        script.getDocumentElement().appendChild(target);
    }
    
    /**
     * Creates target:
     * <target name="-init" depends="-load-props, -check-props"/>
     * @param script Script to write to.
     */
    private void createInitTarget(Document script) {
        Element target = script.createElement("target");
        target.setAttribute("name", INIT_TARGET); // NOI18N
        target.setAttribute("depends", LOAD_PROPS_TARGET + ", " + CHECK_PROPS_TARGET);
        script.getDocumentElement().appendChild(target);
    }
    
    /**
     * Creates target:
     * <target name="debug-nb" depends="-init">
     *     <nbjpdaconnect name="${session.name}" host="${jpda.host}" address="${jpda.address}" transport="${jpda.transport}">
     *         <sourcepath>
     *             <path path="${web.docbase.dir}:${debug.sourcepath}"/>
     *         </sourcepath>
     *     </nbjpdaconnect>
     *     <antcall target="debug-display-browser"/>
     * </target>
     * @param script Script to write to.
     */
    private void createDebugTarget(Document script) {
        Element target = script.createElement("target");
        target.setAttribute("name", DEBUG_TARGET); // NOI18N
        target.setAttribute("depends", INIT_TARGET);
        Element nbjpdaconnect = script.createElement("nbjpdaconnect"); // NOI18N
        nbjpdaconnect.setAttribute("name", "${session.name}");
        nbjpdaconnect.setAttribute("host", "${jpda.host}");
        nbjpdaconnect.setAttribute("address", "${jpda.address}");
        nbjpdaconnect.setAttribute("transport", "${jpda.transport}");
        Element sourcepath = script.createElement("sourcepath");
        Element path = script.createElement("path");
        path.setAttribute("path", "${web.docbase.dir}:${debug.sourcepath}");
        sourcepath.appendChild(path);
        nbjpdaconnect.appendChild(sourcepath);
        target.appendChild(nbjpdaconnect);
        Element antcall = script.createElement("antcall");
        antcall.setAttribute("target", DISPLAY_BROWSER);
        target.appendChild(antcall);
        
        script.getDocumentElement().appendChild(target);
    }

    /**
     * Creates target:
     * <target name="debug-display-browser">
     *   <nbbrowse url="${client.url}"/>
     * </target>
     * @param script Script to write to.
     */
    private void createDisplayBrowserTarget(Document script) {
        Element target = script.createElement("target");
        target.setAttribute("name", DISPLAY_BROWSER); // NOI18N
        Element nbbrowse = script.createElement("nbbrowse");
        nbbrowse.setAttribute("url", "${client.url}");
        target.appendChild(nbbrowse);
        
        script.getDocumentElement().appendChild(target);
    }

    /**
     * Write a script with a new or modified document.
     * @param script Document written to the script path.
     * @param scriptPath e.g. {@link #FILE_SCRIPT_PATH} or {@link #GENERAL_SCRIPT_PATH}
     */
    void writeCustomScript(Document script, String scriptPath) throws IOException {
        FileObject scriptFile = helper.getProjectDirectory().getFileObject(scriptPath);
        if (scriptFile == null) {
            scriptFile = FileUtil.createData(helper.getProjectDirectory(), scriptPath);
        }
        FileLock lock = scriptFile.lock();
        try {
            OutputStream os = scriptFile.getOutputStream(lock);
            try {
                XMLUtil.write(script, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
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
        ideActions.appendChild(action);
        
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

        helper.putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(project);
    }

    /**
     * Jump to an action binding in the editor.
     * @param command an {@link ActionProvider} command name found in project.xml
     */
    private void jumpToBinding(String command) {
        jumpToFile(AntProjectHelper.PROJECT_XML_PATH, command, "action", "name"); // NOI18N
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
                ErrorManager.getDefault().getInstance(WebFreeFormActionProvider.class.getName()).log(
                            ErrorManager.WARNING, e + " [file=" + file + " match=" + match + " line=" + line + "]"); // NOI18N
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
    
}
