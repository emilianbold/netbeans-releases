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

import org.apache.tools.ant.Project;

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
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.ElementCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.xml.ElementSupport;

/** A node that represents an Ant project.
 */
public class AntProjectNode extends DataNode implements ChangeListener, PropertyChangeListener {

    public AntProjectNode (DataObject obj) {
        super (obj, new AntProjectChildren ((AntProjectCookie) obj.getCookie (AntProjectCookie.class)));
        setIconBase ("/org/apache/tools/ant/module/resources/AntIcon"); // NOI18N
        AntProjectCookie cookie = (AntProjectCookie) getCookie (AntProjectCookie.class);
        cookie.addChangeListener (WeakListener.change (this, cookie));
        obj.addPropertyChangeListener (WeakListener.propertyChange (this, obj));
        RequestProcessor.postRequest (new Runnable () {
                public void run () {
                    updateDisplayName ();
                    updateElementCookie ();
                }
            }, 500); // don't even think about squeezing out folder recognizer thread...
    }

    private void updateDisplayName () {
        AntProjectCookie cookie = (AntProjectCookie) getCookie (AntProjectCookie.class);
        Element pel = cookie.getProjectElement ();
        if (pel != null) {
            String projectName = pel.getAttribute ("name"); // NOI18N
            // Set the name/display name in the IDE to the name of the project 
            setDisplayName (NbBundle.getMessage (AntProjectNode.class, "LBL_script_display_name", getName (), projectName));
        }
        Throwable exc = cookie.getParseException ();
        if (exc == null) {
            setShortDescription (getDisplayName ());
            setIconBase ("/org/apache/tools/ant/module/resources/AntIcon"); // NOI18N
        } else {
            String m = exc.getLocalizedMessage ();
            if (m == null || m.length () == 0) {
                m = exc.toString ();
                AntModule.err.annotate (exc, ErrorManager.UNKNOWN, "Strange parse error in " + ((DataObject) getCookie (DataObject.class)).getPrimaryFile (), null, null, null); // NOI18N
                AntModule.err.notify (ErrorManager.INFORMATIONAL, exc);
            }
            setShortDescription (m);
            setIconBase ("/org/apache/tools/ant/module/resources/AntIconError"); // NOI18N
        }
    }

    private void updateElementCookie () {
        AntProjectCookie main = (AntProjectCookie) getCookie (AntProjectCookie.class);
        Element projel = main.getProjectElement ();
        if (projel != null) {
            getCookieSet ().add (new ElementSupport.Instance (projel, Project.class));
        } else {
            ElementCookie cookie = (ElementCookie) getCookie (ElementCookie.class);
            if (cookie != null) {
                getCookieSet ().remove (cookie);
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

        Sheet.Set exec = new Sheet.Set ();
        exec.setName ("execution"); // NOI18N
        exec.setDisplayName (NbBundle.getMessage (AntProjectNode.class, "LBL_execution"));
        exec.setShortDescription (NbBundle.getMessage (AntProjectNode.class, "HINT_execution"));
        CompilerSupport csupp = (CompilerSupport) getCookie (CompilerSupport.class);
        if (csupp != null) csupp.addProperties (exec);
        ExecSupport xsupp = (ExecSupport) getCookie (ExecSupport.class);
        if (xsupp != null) xsupp.addProperties (exec);
        exec.remove (ExecSupport.PROP_FILE_PARAMS);
        exec.remove (ExecSupport.PROP_DEBUGGER_TYPE);
        if (csupp != null || xsupp != null) {
            sheet.put (exec);
        }

        return sheet;
    }

    private class ProjectProperty extends AntProperty {
        public ProjectProperty (String name) {
            super (name);
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

    private class ProjectTargetProperty extends ProjectProperty {
        public ProjectTargetProperty (String name) {
            super (name);
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
            return new File (getElement ().getAttribute ("basedir")); // NOI18N
        }
        public void setValue (Object o) throws IllegalArgumentException, InvocationTargetException {
            if (o == null || o.toString ().equals ("")) { // NOI18N
                try {
                    getElement ().removeAttribute ("basedir"); // NOI18N
                } catch (DOMException dome) {
                    throw new InvocationTargetException (dome);
                }
                return;
            }
            if (! (o instanceof File)) throw new IllegalArgumentException ();
            try {
                getElement ().setAttribute ("basedir", ((File) o).getPath ()); // NOI18N
            } catch (DOMException dome) {
                throw new InvocationTargetException (dome);
            }
        }
        public boolean supportsDefaultValue () {
            return true;
        }
        public void restoreDefaultValue () throws InvocationTargetException {
            setValue (null);
            /*
             try {
                 getElement ().setAttribute ("basedir", "."); // NOI18N
             } catch (DOMException dome) {
                 throw new InvocationTargetException (dome);
             }
             */
        }
    }

    private void add2Sheet (Sheet.Set props) {
        // Retrieve the document element of the Ant Project XML document from the cookie set
        Element docElem = ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        if (docElem != null) {
            ResourceBundle bundle = NbBundle.getBundle (AntProjectNode.class);
            // Create the required properties (XML attributes) of the Ant project
            Node.Property prop = new ProjectProperty ("name"); // NOI18N
            // Cannot reuse 'name' because it conflicts with the DataObject.PROP_NAME:
            prop.setName ("projectName"); // NOI18N
            prop.setDisplayName (bundle.getString ("PROP_projectName"));
            prop.setShortDescription (bundle.getString ("HINT_projectName"));
            props.put (prop);
            prop = new ProjectTargetProperty ("default"); // NOI18N
            prop.setDisplayName (bundle.getString ("PROP_default"));
            prop.setShortDescription (bundle.getString ("HINT_default"));
            props.put (prop);
            prop = new ProjectBasedirProperty (bundle.getString ("PROP_basedir"), bundle.getString ("HINT_basedir"));
            props.put (prop);
            // id prop unnecessary, since project name functions as an ID
        }
    }

    public void propertyChange (PropertyChangeEvent evt) {
        String prop = evt.getPropertyName ();
        if (prop == null || prop.equals (DataObject.PROP_NAME)) {
            updateDisplayName ();
        }
    }

    public void stateChanged (ChangeEvent ev) {
        updateDisplayName ();
        updateElementCookie ();
        firePropertyChange (null, null, null);
    }

    public SystemAction getDefaultAction () {
        return SystemAction.get (ExecuteAction.class);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
    }

    protected void createPasteTypes (Transferable t, List l) {
        AntProjectCookie proj = (AntProjectCookie) getCookie (AntProjectCookie.class);
        Element pel = proj.getProjectElement ();
        if (pel != null) {
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
               IntrospectedInfo.getDefaults ().getTypedefs ().containsKey (type) ||
               AntSettings.getDefault ().getCustomDefs ().getTypedefs ().containsKey (type);
    }

    public NewType[] getNewTypes () {
        List names = new ArrayList ();
        names.addAll (IntrospectedInfo.getDefaults ().getTypedefs ().keySet ());
        names.addAll (AntSettings.getDefault ().getCustomDefs ().getTypedefs ().keySet ());
        Collections.sort (names);
        names.add (0, "target"); // NOI18N
        names.add (1, "property"); // NOI18N
        names.add (2, "taskdef"); // NOI18N
        NewType[] types = new NewType[names.size ()];
        for (int i = 0; i < types.length; i++) {
            types[i] = new ProjectNewType ((String) names.get (i));
        }
        return types;
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
                if (name.equals ("target")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_target_name_changeme"));
                } else if (name.equals ("property")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_property_name_changeme"));
                    el2.setAttribute ("value", NbBundle.getMessage (AntProjectNode.class, "MSG_property_value_changeme"));
                } else if (name.equals ("taskdef")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_taskdef_name_changeme"));
                    el2.setAttribute ("classname", NbBundle.getMessage (AntProjectNode.class, "MSG_taskdef_classname_changeme"));
                } else {
                    el2.setAttribute ("id", NbBundle.getMessage (AntProjectNode.class, "MSG_id_changeme"));
                }
                ElementNode.appendWithIndent (el, el2);
            } catch (DOMException dome) {
                IOException ioe = new IOException ();
                AntModule.err.annotate (ioe, dome);
                throw ioe;
            }
        }
    }

}
