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
import org.openide.util.Utilities;

/** A node that represents an Ant project.
 */
public class AntProjectNode extends DataNode implements ChangeListener {
    
    public AntProjectNode (DataObject obj) {
        this(obj, (AntProjectCookie)obj.getCookie(AntProjectCookie.class));
    }
    private AntProjectNode(DataObject obj, AntProjectCookie cookie) {
        super(obj, new AntProjectChildren(cookie));
        cookie.addChangeListener(WeakListeners.change(this, cookie));
        setValue("propertiesHelpID", "org.apache.tools.ant.module.nodes.AntProjectNode.propertysheet"); // NOI18N
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

        Sheet.Set props = new Sheet.Set();
        props.setName("project"); // NOI18N
        props.setDisplayName(NbBundle.getMessage(AntProjectNode.class, "LBL_proj_sheet"));
        props.setShortDescription(NbBundle.getMessage(AntProjectNode.class, "HINT_proj_sheet"));
        props.setValue("helpID", "org.apache.tools.ant.module.nodes.AntProjectNode.Properties");
        add2Sheet (props);
        sheet.put(props);

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

    private class ProjectTargetProperty extends AntProperty {
        public ProjectTargetProperty (String name, AntProjectCookie proj) {
            super (name, proj);
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
    }

    private class ProjectBasedirProperty extends PropertySupport.ReadOnly {
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
    }

    public void stateChanged (ChangeEvent ev) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                fireIconChange();
                fireOpenedIconChange();
                fireShortDescriptionChange(null, null);
                fireCookieChange();
                firePropertyChange(null, null, null);
            }
        });
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
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
    
}
