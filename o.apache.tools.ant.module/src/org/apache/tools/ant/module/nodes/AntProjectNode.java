/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick.
 */

package org.apache.tools.ant.module.nodes;

import java.awt.*;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List; // override java.awt.List
import javax.swing.event.*;

import org.w3c.dom.*;

import org.openide.*;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.nodes.Node; // override org.w3c.dom.Node
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.*;
import org.apache.tools.ant.module.xml.ElementSupport;
import org.apache.tools.ant.module.wizards.properties.*;
import org.openide.util.Utilities;

/** A node that represents an Ant project.
 */
public class AntProjectNode extends DataNode implements ChangeListener {
    
    public AntProjectNode (DataObject obj) {
        this(obj, (AntProjectCookie)obj.getCookie(AntProjectCookie.class));
    }
    private AntProjectNode(DataObject obj, AntProjectCookie cookie) {
        super(obj, new AntProjectChildren(cookie));
        cookie.addChangeListener(WeakListener.change(this, cookie));
        getCookieSet ().add (new ProjectNodeIndex (this));
    }
    
    public Node.Cookie getCookie(Class c) {
        if (c == ElementCookie.class || c == IntrospectionCookie.class) {
            AntProjectCookie main = (AntProjectCookie)getDataObject().getCookie(AntProjectCookie.class);
            Element projel = main.getProjectElement();
            if (projel != null) {
                return new ElementSupport.Introspection(projel, "org.apache.tools.ant.Project");
            }
        }
        return super.getCookie(c);
    }
    
    public Image getIcon(int type) {
        Image i = getBasicIcon();
        try {
            // #25248: annotate the build script icon
            i = getDataObject().getPrimaryFile().getFileSystem().getStatus().
                annotateIcon(i, type, getDataObject().files());
        } catch (FileStateInvalidException fsie) {
            AntModule.err.notify(ErrorManager.INFORMATIONAL, fsie);
        }
        return i;
    }
    private Image getBasicIcon() {
        AntProjectCookie.ParseStatus cookie = (AntProjectCookie.ParseStatus)getDataObject().getCookie(AntProjectCookie.ParseStatus.class);
        if (cookie.getFile() == null && cookie.getFileObject() == null) {
            // Script has been invalidated perhaps? Don't continue, we would
            // just get an NPE from the getParseException.
            return Utilities.loadImage("org/apache/tools/ant/module/resources/AntIconError.gif"); // NOI18N
        }
        if (!cookie.isParsed()) {
            // Assume for now it is not erroneous.
            return Utilities.loadImage("org/apache/tools/ant/module/resources/AntIcon.gif"); // NOI18N
        }
        Throwable exc = cookie.getParseException();
        if (exc != null) {
            return Utilities.loadImage("org/apache/tools/ant/module/resources/AntIconError.gif"); // NOI18N
        } else {
            return Utilities.loadImage("org/apache/tools/ant/module/resources/AntIcon.gif"); // NOI18N
        }
    }
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    public String getShortDescription() {
        AntProjectCookie cookie = (AntProjectCookie)getDataObject().getCookie(AntProjectCookie.class);
        if (cookie.getFile() == null && cookie.getFileObject() == null) {
            // Script has been invalidated perhaps? Don't continue, we would
            // just get an NPE from the getParseException.
            return super.getShortDescription();
        }
        Throwable exc = cookie.getParseException();
        if (exc != null) {
            String m = exc.getLocalizedMessage();
            if (m != null) {
                 return m;
            } else {
                return exc.toString();
            }
        } else {
            Element pel = cookie.getProjectElement();
            if (pel != null) {
                String projectName = pel.getAttribute("name"); // NOI18N
                if (!projectName.equals("")) { // NOI18N
                    // Set the node description in the IDE to the name of the project
                    return NbBundle.getMessage(AntProjectNode.class, "LBL_named_script_description", projectName);
                } else {
                    // No name specified, OK.
                    return NbBundle.getMessage(AntProjectNode.class, "LBL_anon_script_description");
                }
            } else {
                // ???
                return super.getShortDescription();
            }
        }
    }

    protected Sheet createSheet() {  
        Sheet sheet = super.createSheet();

        // Make sure there is a "Properties" set: // NOI18N
        Sheet.Set props = sheet.get(Sheet.PROPERTIES); // get by name, not display name
        if (props == null)  {
            props = Sheet.createPropertiesSet ();
            sheet.put(props);
        }
        add2Sheet (props);
        props.setValue("helpID", "org.apache.tools.ant.module.nodes.AntProjectNode.Properties");

        Sheet.Set exec = new Sheet.Set ();
        exec.setName ("execution"); // NOI18N
        exec.setDisplayName (NbBundle.getMessage (AntProjectNode.class, "LBL_execution"));
        exec.setShortDescription (NbBundle.getMessage (AntProjectNode.class, "HINT_execution"));
        CompilerSupport csupp = (CompilerSupport) getCookie (CompilerSupport.class);
        if (csupp != null) csupp.addProperties (exec);
        ExecutionSupport xsupp = (ExecutionSupport) getCookie (ExecutionSupport.class);
        if (xsupp != null) xsupp.addProperties (exec);
        exec.remove (ExecutionSupport.PROP_FILE_PARAMS);
        if (csupp != null || xsupp != null) {
            sheet.put (exec);
        }
        exec.setValue("helpID", "org.apache.tools.ant.module.nodes.AntProjectNode.Execution");

        return sheet;
    }

    private class ProjectNameProperty extends AntProperty {
        public ProjectNameProperty (String name, AntProjectCookie proj) {
            super (name, proj);
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
    }

    private class TargetEditor extends PropertyEditorSupport {
        public String getAsText () {
            return (String) getValue ();
        }
        public void setAsText (String v) throws IllegalArgumentException {
            setValue (v);
        }
        public String[] getTags () {
            Element proj = ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
            if (proj == null) return new String[] { getAsText () };
            NodeList nl = proj.getElementsByTagName ("target"); // NOI18N
            String[] tags = new String[nl.getLength ()];
            for (int i = 0; i < tags.length; i++) {
                tags[i] = ((Element) nl.item (i)).getAttribute ("name"); // NOI18N
            }
            return tags;
        }
    }

    private class ProjectTargetProperty extends AntProperty {
        public ProjectTargetProperty (String name, AntProjectCookie proj) {
            super (name, proj);
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
        public boolean supportsDefaultValue () {
            return false;
        }
        public void setValue (Object value) throws IllegalArgumentException, InvocationTargetException {
            if (value == null || value.equals ("")) {
                IllegalArgumentException iae = new IllegalArgumentException ("no default for " + this.getName ()); // NOI18N
                AntModule.err.annotate (iae, NbBundle.getMessage (AntProjectNode.class, "EXC_no_default_value_for_prop", this.getDisplayName ()));
                throw iae;
            }
            super.setValue (value);
        }
        public PropertyEditor getPropertyEditor () {
            return new TargetEditor ();
        }
    }

    private class ProjectBasedirProperty extends PropertySupport.ReadWrite {
        public ProjectBasedirProperty (String dname, String sdesc) {
            super ("basedir", File.class, dname, sdesc); // NOI18N
            this.setValue ("directories", Boolean.TRUE); // NOI18N
            this.setValue ("files", Boolean.FALSE); // NOI18N
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
        public Object getValue () {
            Element el = getElement ();
            if (el == null) { // #9675
                return null;
            }
            String bd = el.getAttribute("basedir"); // NOI18N
            if (bd.equals("")) return null; // NOI18N
            if (bd.equals(".")) bd = ""; // NOI18N
            return new File(bd);
        }
        public void setValue (Object o) throws IllegalArgumentException, InvocationTargetException {
            Element el = getElement ();
            if (el == null) return;
            if (o == null) {
                try {
                    el.removeAttribute ("basedir"); // NOI18N
                } catch (DOMException dome) {
                    throw new InvocationTargetException (dome);
                }
                return;
            }
            if (! (o instanceof File)) throw new IllegalArgumentException ();
            try {
                String path = ((File)o).getPath();
                if (path.equals("")) path = "."; // NOI18N
                el.setAttribute("basedir", path); // NOI18N
            } catch (DOMException dome) {
                throw new InvocationTargetException (dome);
            }
        }
        public boolean canWrite () {
            return (getElement () != null && ! isScriptReadOnly((AntProjectCookie) getCookie(AntProjectCookie.class)));
        }
        public boolean supportsDefaultValue () {
            return (getElement () != null);
        }
        public void restoreDefaultValue () throws InvocationTargetException {
            setValue (null);
        }
        public PropertyEditor getPropertyEditor() {
            // Before using File editor, set up the base directory...
            AntProjectCookie cookie = (AntProjectCookie)getCookie(AntProjectCookie.class);
            if (cookie != null) {
                Element root = cookie.getProjectElement();
                File buildscript = cookie.getFile();
                if (root != null && buildscript != null) {
                    AntModule.err.log("ProjectBasedirProperty: setting baseDir=" + buildscript.getParentFile());
                    // Controls which directory relative paths are relative to:
                    ProjectBasedirProperty.this.setValue("baseDir", buildscript.getParentFile()); // NOI18N
                }
            }
            return super.getPropertyEditor();
        }
    }

    private void add2Sheet (Sheet.Set props) {
        ResourceBundle bundle = NbBundle.getBundle (AntProjectNode.class);
        AntProjectCookie proj = (AntProjectCookie) getCookie (AntProjectCookie.class);
        
        // Create the required properties (XML attributes) of the Ant project
        Node.Property prop = new ProjectNameProperty ("name", proj); // NOI18N
        // Cannot reuse 'name' because it conflicts with the DataObject.PROP_NAME:
        prop.setName ("projectName"); // NOI18N
        prop.setDisplayName (bundle.getString ("PROP_projectName"));
        prop.setShortDescription (bundle.getString ("HINT_projectName"));
        props.put (prop);
        prop = new ProjectTargetProperty ("default", proj); // NOI18N
        prop.setDisplayName (bundle.getString ("PROP_default"));
        prop.setShortDescription (bundle.getString ("HINT_default"));
        props.put (prop);
        prop = new ProjectBasedirProperty (bundle.getString ("PROP_basedir"), bundle.getString ("HINT_basedir"));
        props.put (prop);
        // id prop unnecessary, since project name functions as an ID
        props.put (new ProjectBuildSequenceProperty(proj));
        ProjectPropertiesFileProperty ppfp = new ProjectPropertiesFileProperty ();
        props.put (ppfp);
        PropertiesChooserProperty pcp = new PropertiesChooserProperty (ppfp);
        props.put (pcp);
    }

    public void stateChanged (ChangeEvent ev) {
        fireIconChange();
        fireOpenedIconChange();
        fireShortDescriptionChange(null, null);
        fireCookieChange();
        firePropertyChange (null, null, null);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
    }

    protected void createPasteTypes (Transferable t, List l) {
        AntProjectCookie proj = (AntProjectCookie) getCookie (AntProjectCookie.class);
        Element pel = proj.getProjectElement ();
        if (pel != null && ! isScriptReadOnly (proj)) {
            ElementCookie cookie = (ElementCookie) NodeTransfer.cookie (t, NodeTransfer.COPY, ElementCookie.class);
            if (cookie != null && canPasteElement (cookie.getElement ())) {
                l.add (new ElementNode.ElementPaste (pel, cookie.getElement (), false));
            }
            cookie = (ElementCookie) NodeTransfer.cookie (t, NodeTransfer.MOVE, ElementCookie.class);
            if (cookie != null && canPasteElement (cookie.getElement ())) {
                l.add (new ElementNode.ElementPaste (pel, cookie.getElement (), true));
            }
        }
    }

    private boolean canPasteElement (Element el) {
        String type = el.getNodeName ();
        return type.equals ("target") || // NOI18N
               type.equals ("property") || // NOI18N
               type.equals ("taskdef") || // NOI18N
               type.equals ("typedef") || // NOI18N
               type.equals ("description") || // NOI18N
               IntrospectedInfo.getDefaults ().getDefs ("type").containsKey (type) || // NOI18N
               AntSettings.getDefault ().getCustomDefs ().getDefs ("type").containsKey (type); // NOI18N
    }

    public NewType[] getNewTypes () {
        if (! isScriptReadOnly ((AntProjectCookie) getCookie(AntProjectCookie.class))) {
            List names = new ArrayList ();
            names.addAll (IntrospectedInfo.getDefaults ().getDefs ("type").keySet ()); // NOI18N
            names.addAll (AntSettings.getDefault ().getCustomDefs ().getDefs ("type").keySet ()); // NOI18N
            Collections.sort (names);
            names.add (0, "target"); // NOI18N
            names.add (1, "property"); // NOI18N
            names.add (2, "taskdef"); // NOI18N
            names.add (3, "typedef"); // NOI18N
            names.add (4, "description"); // NOI18N
            // XXX in Ant 1.6, *any* task can be used here, so just add them all
            NewType[] types = new NewType[names.size ()];
            for (int i = 0; i < types.length; i++) {
                types[i] = new ProjectNewType ((String) names.get (i));
            }
            return types;
        } else {
            return new NewType[0];
        }
    }

    private class ProjectNewType extends NewType {
        private String name;
        public ProjectNewType (String name) {
            this.name = name;
        }
        public String getName () {
            return name;
        }
        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.apache.tools.ant.module.node-manip");
        }
        public void create () throws IOException {
            Element el = ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
            if (el == null) throw new IOException ();
            try {
                Element el2 = el.getOwnerDocument ().createElement (name);
                ElementNode.appendWithIndent (el, el2);
                if (name.equals ("target")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_target_name_changeme"));
                } else if (name.equals ("property")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_property_name_changeme"));
                    el2.setAttribute ("value", NbBundle.getMessage (AntProjectNode.class, "MSG_property_value_changeme"));
                } else if (name.equals ("taskdef") || name.equals("typedef")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_taskdef_name_changeme"));
                    el2.setAttribute ("classname", NbBundle.getMessage (AntProjectNode.class, "MSG_taskdef_classname_changeme"));
                } else if (name.equals("description")) { // NOI18N
                    ElementNode.appendWithIndent(el2,
                        el.getOwnerDocument().createTextNode(NbBundle.getMessage(AntProjectNode.class, "MSG_description_changeme")));
                } else {
                    // Random data type.
                    el2.setAttribute ("id", NbBundle.getMessage (AntProjectNode.class, "MSG_id_changeme"));
                }
            } catch (DOMException dome) {
                IOException ioe = new IOException ();
                AntModule.err.annotate (ioe, dome);
                throw ioe;
            }
        }
    }

    
    
    /** Returns true if the Antscript represented by the passed cookie is read-only. */
    public static boolean isScriptReadOnly(AntProjectCookie cookie) {
        if (cookie != null) {
            if (cookie.getFileObject() != null) {
                return cookie.getFileObject().isReadOnly();
            } else if (cookie.getFile() != null) {
                return ! cookie.getFile().canWrite();
            }
        }
        return true;
    }
    
    /** Property displaying the build sequence of the whole project. */
    public static class ProjectBuildSequenceProperty extends AntTargetNode.BuildSequenceProperty {
        
        /** ProjectCookie. */
        protected AntProjectCookie proj;
        
        /** Creates new ProjectBuildSequenceProperty.
         * @param elem the project Element.
         */
        public ProjectBuildSequenceProperty(AntProjectCookie proj) {
            super (proj.getProjectElement ());
            this.proj = proj;
        }
        
        /** Override getTarget of superclass to find default target. */
        public Element getTarget() {
            el = proj.getProjectElement (); // to be sure that the Element is up to date.
            if (el != null && el.getAttribute("default") != null) { // NOI18N
                return getTargetElement(el.getAttribute("default"), el); // NOI18N
            }
            return null;
        }
        
        /** Returns special String in case of missing default target. */
        public Object getValue() {
            if (proj.getProjectElement () == null) {
                return NbBundle.getMessage (AntProjectNode.class, "LBL_property_invalid_no_element");
            }
            Element el = getTarget();
            if (el == null) {
                return NbBundle.getMessage (AntProjectNode.class, "MSG_defaulttarget_missing");
            }
            return super.getValue();
        }
    }
    
    /** Index Cookie for ProjectNode. Enables ReorderAction. */
    public static class ProjectNodeIndex extends ElementNode.ElementNodeIndex {
        
        /** Creates new ProjectNodeIndex. */
        public ProjectNodeIndex(org.openide.nodes.Node indexNode) {
            super (indexNode);
        }

        /** Get the parent Node of the Elements that can be moved.*/
        protected org.w3c.dom.Node getParentNode() {
            return ((AntProjectCookie) indexNode.getCookie (AntProjectCookie.class)).getProjectElement ();
        }
    }
    
    /** Displays the Properties for the project stored in a .properties file. */
    class ProjectPropertiesFileProperty extends PropertiesFileProperty {
        ProjectPropertiesFileProperty () {
            super ( NbBundle.getMessage (AntProjectNode.class, "PROP_project_properties"),
                    NbBundle.getMessage (AntProjectNode.class, "HINT_project_properties")
                  );
        }
        /** Get the Project Element. */
        public Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
        /** Get the AntProjectCookie. */
        public AntProjectCookie getAntProjectCookie () {
            return (AntProjectCookie) getCookie (AntProjectCookie.class);
        }        
        protected void firePropertiesFilePropertyChange() {
            AntProjectNode.this.firePropertySetsChange(null, null);
        }
    }
}
