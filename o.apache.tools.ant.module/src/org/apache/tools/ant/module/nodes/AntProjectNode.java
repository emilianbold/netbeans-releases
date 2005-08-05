/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.nodes;

import java.awt.Image;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.ErrorManager;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

public final class AntProjectNode extends DataNode implements ChangeListener {
    
    public AntProjectNode (DataObject obj) {
        super(obj, Children.LEAF);
        AntProjectCookie cookie = (AntProjectCookie) obj.getCookie(AntProjectCookie.class);
        cookie.addChangeListener(WeakListeners.change(this, cookie));
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
        AntProjectCookie.ParseStatus cookie = (AntProjectCookie.ParseStatus) getCookie(AntProjectCookie.ParseStatus.class);
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
        AntProjectCookie cookie = (AntProjectCookie) getCookie(AntProjectCookie.class);
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

}
