/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.loaders.InstanceDataObject;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.beaninfo.editors.ListImageEditor;

import java.awt.Image;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.ref.WeakReference;

/** Filters nodes under the session node (displayed in Options dialog), adds special
 * properties to Nodes of particular settings to show/edit positions wher the
 * setting is defined on DefaultFileSystem.
 *
 * @author  Vitezslav Stejskal
 */
public final class SettingChildren extends FilterNode.Children {
    /** Name of Node.Property indicating if setting is defined on Project layer */
    public static final String PROP_INDICATOR = "Indicator"; // NOI18N
    /** Name of Node.Property showing status of Project layer according to the setting */
    public static final String PROP_LAYER_PROJECT = "Project-Layer"; // NOI18N
    /** Name of Node.Property showing status of Session layer according to the setting */
    public static final String PROP_LAYER_SESSION = "Session-Layer"; // NOI18N
    /** Name of Node.Property showing status of Modules layer according to the setting */
    public static final String PROP_LAYER_MODULES = "Modules-Layer"; // NOI18N

    public SettingChildren (Node original) {
        super (original);
    }
    
    protected Node copyNode (Node node) {
        InstanceDataObject ido = (InstanceDataObject) node.getCookie (InstanceDataObject.class);
        return ido != null ? new SettingFilterNode (node) : new FilterNode (node, new SettingChildren (node));
    }

    /** Property allowing display/manipulation of setting status for one specific layer. */
    public static class FileStateProperty extends PropertySupport {
        private FileObject primaryFile = null;
        private int layer;

        public FileStateProperty (String name) {
            this (null, 0, name, true);
        }

        public FileStateProperty (FileObject primaryFile, int layer, String name, boolean readonly) {
            super (name, Integer.class,
                NbBundle.getMessage (FileStateProperty.class, "LBL_FSP_" + name), // NOI18N
                NbBundle.getMessage (FileStateProperty.class, "LBL_FSP_Desc_" + name), // NOI18N
                true, !readonly);

            this.primaryFile = primaryFile;
            this.layer = layer;

            setValue (ListImageEditor.PROP_VALUES, new Integer [] {
                new Integer (FileStateManager.FSTATE_DEFINED),
                new Integer (FileStateManager.FSTATE_IGNORED),
                new Integer (FileStateManager.FSTATE_INHERITED),
                new Integer (FileStateManager.FSTATE_UNDEFINED)
            });

            setValue (ListImageEditor.PROP_IMAGES, new Image [] {
                Utilities.loadImage ("/org/netbeans/core/resources/setting-defined.gif"), // NOI18N
                Utilities.loadImage ("/org/netbeans/core/resources/setting-ignored.gif"), // NOI18N
                Utilities.loadImage ("/org/netbeans/core/resources/setting-inherited.gif"), // NOI18N
                Utilities.loadImage ("/org/netbeans/core/resources/empty.gif") // NOI18N
            });
        }

        public boolean canWrite () {
            if (!super.canWrite ())
                return false;
            
            Integer val = null;
            try {
                val = (Integer) getValue ();
            } catch (Exception e) {
                // ignore it, will be handled later
            }
            return val != null && val.intValue () != FileStateManager.FSTATE_DEFINED;
        }

        public Object getValue () throws IllegalAccessException, InvocationTargetException {
            FileStateManager fsm = FileStateManager.getDefault ();
            return new Integer (fsm.getFileState (primaryFile, layer));
        }

        public void setValue (Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            FileStateManager fsm = FileStateManager.getDefault ();
            int os = fsm.getFileState (primaryFile, layer);
            int ns = ((Integer) val).intValue ();
            
            if (os == ns)
                return;
            
            try {
                switch (ns) {
                    case FileStateManager.FSTATE_DEFINED:
                        boolean above = false;
                        boolean go = true;

                        for (int i = 0; i < layer; i++) {
                            int state = fsm.getFileState (primaryFile, i);
                            if (state == FileStateManager.FSTATE_DEFINED) {
                                // warn user, that above defined files will be removed

                                NotifyDescriptor nd = new NotifyDescriptor.Confirmation (
                                    NbBundle.getMessage (SettingChildren.class, "MSG_ask_remove_above_defined_files"), // NOI18N
                                    NotifyDescriptor.YES_NO_OPTION);

                                Object answer = TopManager.getDefault ().notify (nd);
                                if (answer.equals (NotifyDescriptor.NO_OPTION))
                                    go = false;

                                break;
                            }
                        }

                        if (go)
                            fsm.define (primaryFile, layer);

                        break;

                    case FileStateManager.FSTATE_UNDEFINED:
                        fsm.delete (primaryFile, layer);
                        break;

                    default:
                        throw new IllegalArgumentException ("Required file state change isn't allowed. NewState=" + ns); // NOI18N
                }
            } catch (java.io.IOException e) {
                TopManager.getDefault ().notifyException (e);
            }
        }

        public PropertyEditor getPropertyEditor () {
            return new FileStateEditor ();
        }
    }

    /** Read-only property indicating if specific setting is defined on Project layer. */
    public static class IndicatorProperty extends PropertySupport {
        private FileObject primaryFile = null;

        public IndicatorProperty () {
            this (null);
        }

        public IndicatorProperty (FileObject primaryFile) {
            super (PROP_INDICATOR, Integer.class,
                NbBundle.getMessage (IndicatorProperty.class, "LBL_IndicatorProperty_Name"), // NOI18N
                NbBundle.getMessage (IndicatorProperty.class, "LBL_IndicatorProperty_Description"), // NOI18N
                true, false);

            this.primaryFile = primaryFile;

            setValue (ListImageEditor.PROP_VALUES, new Integer [] {
                new Integer (0),
                new Integer (1)
            });

            setValue (ListImageEditor.PROP_IMAGES, new Image [] {
                Utilities.loadImage ("/org/netbeans/core/resources/empty.gif"), // NOI18N
                Utilities.loadImage ("/org/netbeans/core/resources/project.gif") // NOI18N
            });
        }

        public Object getValue () throws IllegalAccessException, InvocationTargetException {
            FileStateManager fsm = FileStateManager.getDefault ();
            if (FileStateManager.FSTATE_DEFINED == fsm.getFileState (primaryFile, 
                    FileStateManager.LAYER_PROJECT)) {
                return new Integer (1);
            }

            return new Integer (0);
        }

        public void setValue (Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            throw new IllegalAccessException ();
        }

        public PropertyEditor getPropertyEditor () {
            return new ListImageEditor ();
        }
    }

    /** Filter node used for adding special status related properties to setting nodes. */
    private static final class SettingFilterNode extends FilterNode {
        private Sheet sheet = null;
        private FSL weakL = null;
        
        public SettingFilterNode (Node original) {
            super (original);

            FileObject pf = ((InstanceDataObject) getCookie (InstanceDataObject.class)).getPrimaryFile ();
            weakL = new FSL (this);
            FileStateManager.getDefault ().addFileStatusListener (weakL, pf);
        }
        
        public PropertySet[] getPropertySets () {
            if (sheet == null) {
                sheet = cloneSheet (super.getPropertySets ());
            }
            return sheet.toArray ();
        }

        protected NodeListener createNodeListener () {
            return new NA (this);
        }
        
        private Sheet cloneSheet (PropertySet orig []) {
            Sheet s = new Sheet ();
            for (int i = 0; i < orig.length; i++) {
                Sheet.Set ss = new Sheet.Set ();
                ss.put (orig[i].getProperties ());
                ss.setName (orig[i].getName ());
                ss.setDisplayName (orig[i].getDisplayName ());
                ss.setShortDescription (orig[i].getShortDescription ());
                ss.setHidden (orig[i].isHidden ());
                ss.setExpert (orig[i].isExpert ());
                ss.setPreferred (orig[i].isPreferred ());
                s.put (ss);
            }

            Sheet.Set hidden = new Sheet.Set ();
            hidden.setName ("DFS layout"); // NOI18N
            hidden.setHidden (!Boolean.getBoolean ("netbeans.options.sheet"));
            s.put (hidden);

            FileObject pf = ((InstanceDataObject) getCookie (InstanceDataObject.class)).getPrimaryFile ();
            
            hidden.put (new IndicatorProperty (pf));
            hidden.put (new FileStateProperty (pf, FileStateManager.LAYER_PROJECT, PROP_LAYER_PROJECT, false));
            hidden.put (new FileStateProperty (pf, FileStateManager.LAYER_SESSION, PROP_LAYER_SESSION, false));
            hidden.put (new FileStateProperty (pf, FileStateManager.LAYER_MODULES, PROP_LAYER_MODULES, false));

            return s;
        }

        private static class NA extends NodeAdapter {
            public NA (SettingFilterNode sfn) {
                super (sfn);
            }
            
            protected void propertyChange (FilterNode fn, PropertyChangeEvent ev) {
                if (Node.PROP_PROPERTY_SETS.equals (ev.getPropertyName ())) {
                    ((SettingFilterNode)fn).sheet = null;
                }
                super.propertyChange (fn, ev);
            }
        }
        
        private static class FSL implements FileStateManager.FileStatusListener {
            WeakReference node = null;
            public FSL (SettingFilterNode sfn) {
                node = new WeakReference (sfn);
            }
            public void fileStatusChanged (FileObject mfo) {
                SettingFilterNode n = (SettingFilterNode) node.get ();
                if (n == null) {
                    FileStateManager.getDefault ().removeFileStatusListener (this, null);
                    return;
                }
                
                n.firePropertyChange (PROP_LAYER_PROJECT, null, null);
                n.firePropertyChange (PROP_LAYER_SESSION, null, null);
                n.firePropertyChange (PROP_LAYER_MODULES, null, null);
                n.firePropertyChange (PROP_INDICATOR, null, null);
            }
        }
    }
}
