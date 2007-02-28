/*
 * ActionsList.java
 *
 * Created on February 16, 2007, 2:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vita Stejskal
 */
public class ActionsList {
    
    private static final Logger LOG = Logger.getLogger(ActionsList.class.getName());
    
    private final List all;
    private final List actions;

    /**
     * Create a new <code>ActionList</code> instance by calling<code>this(keys, false)</code>.
     * 
     * @param keys The list of objects to convert to <code>Action</code>s
     */
    protected ActionsList(List keys) {
        this(keys, false);
    }
    
    /**
     * Create a new <code>ActionList</code> instance. The <code>ActionList</code>
     * converts a list of objects (keys) to the list of <code>Action</code>s
     * or other instances that can potentially be used in actions based UI such
     * as popup menus, toolbars, etc. The other instances can be anything, but
     * usually they are things like <code>JSeparator</code>, <code>DataFolder</code>
     * or plain <code>String</code> with the name of an editor action.
     * 
     * @param keys The list of objects to convert to <code>Action</code>s
     * @param ignoreFolders <code>true</code> if the conversion should skipp folders
     */
    protected ActionsList(List keys, boolean ignoreFolders) {
        List [] lists = convertImpl(keys == null ? Collections.emptyList() : keys, ignoreFolders);
        this.all = lists[0];
        this.actions = lists[1];
    }

    public List getAllInstances() {
        return all;
    }

    public List getActionsOnly() {
        return actions;
    }

    public static List convert(List keys) {
        List [] lists = convertImpl(keys, false);
        return lists[0];
    }
    
    private static List [] convertImpl(List keys, boolean ignoreFolders) {
        List all = new ArrayList();
        List actions = new ArrayList();

        for (int i = 0; i < keys.size(); i++){
            Object item = keys.get(i);
            DataObject dob = null;

            if (item instanceof DataObject) {
                dob = (DataObject) item;
            } else if (item instanceof FileObject) {
                try {
                    dob = DataObject.find((FileObject) item);
                } catch (DataObjectNotFoundException dnfe) {
                    // ignore
                }
            }

            if (dob != null) {
                InstanceCookie ic = (InstanceCookie) dob.getLookup().lookup(InstanceCookie.class);
                if (ic != null){
                    try{
                        if (Action.class.isAssignableFrom(ic.instanceClass()) ||
                            SystemAction.class.isAssignableFrom(ic.instanceClass()))
                        {
                            Object instance = ic.instanceCreate();
                            all.add(instance);
                            actions.add(instance);
                        } else if (!DataFolder.class.isAssignableFrom(ic.instanceClass()) || !ignoreFolders) {
                            Object instance = ic.instanceCreate();
                            all.add(instance);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Can't instantiate object", e);
                    }
                } else if (dob instanceof DataFolder) {
                    all.add(dob);
                } else {
                    all.add(dob.getName());
                }
            } else {
                all.add(item);
                if (item instanceof Action || item instanceof SystemAction) {
                    actions.add(item);
                }
            }
        }

        return new List [] { 
            all.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(all), 
            actions.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(actions), 
        };
    }
}
