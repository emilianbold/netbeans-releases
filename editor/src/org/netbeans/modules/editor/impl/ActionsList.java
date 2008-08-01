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
import javax.swing.JSeparator;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class ActionsList {
    
    private static final Logger LOG = Logger.getLogger(ActionsList.class.getName());
    
    private final List<Object> all;
    private final List<Action> actions;

    /**
     * Create a new <code>ActionList</code> instance by calling<code>this(keys, false)</code>.
     * 
     * @param keys The list of objects to convert to <code>Action</code>s
     */
    protected ActionsList(List<FileObject> keys) {
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
    protected ActionsList(List<FileObject> keys, boolean ignoreFolders) {
        Pair p = convertImpl(keys == null ? Collections.<FileObject>emptyList() : keys, ignoreFolders);
        this.all = p.all;
        this.actions = p.actions;
    }

    public List<Object> getAllInstances() {
        return all;
    }

    public List<Action> getActionsOnly() {
        return actions;
    }

    public static List<Object> convert(List<FileObject> keys) {
        return convertImpl(keys, false).all;
    }
    
    private static class Pair {
        List<Object> all;
        List<Action> actions;
    }
    
    private static Pair convertImpl(List<FileObject> keys, boolean ignoreFolders) {
        List<Object> all = new ArrayList<Object>();
        List<Action> actions = new ArrayList<Action>();

        for (FileObject item : keys) {
            DataObject dob;
            try {
                dob = DataObject.find(item);
            } catch (DataObjectNotFoundException dnfe) {
                continue; // ignore
            }

            Object toAdd = null;
            InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
            if (ic != null) {
                try {
                    if (!isSeparator(ic)) {
                        toAdd = ic.instanceCreate();
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Can't instantiate object", e); //NOI18N
                    continue;
                }
            } else if (dob instanceof DataFolder) {
                toAdd = dob;
            } else {
                toAdd = dob.getName();
            }

            // Filter out the same succeding items
            if (all.size() > 0) {
                Object lastOne = all.get(all.size() - 1);
                if (Utilities.compareObjects(lastOne, toAdd)) {
                    continue;
                }
                if (isSeparator(lastOne) && isSeparator(toAdd)) {
                    continue;
                }
            }
            
            if (toAdd instanceof Action) {
                actions.add((Action) toAdd);
            } else if (isSeparator(toAdd)) {
                actions.add(null);
            }
            all.add(toAdd);
        }

        Pair p = new Pair();
        p.all = all.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(all);
        p.actions = actions.isEmpty() ? Collections.<Action>emptyList() : Collections.unmodifiableList(actions);
        return p;
    }
    
    private static boolean isSeparator(Object o) {
        return o == null || o instanceof JSeparator;
    }
    
    private static boolean isSeparator(InstanceCookie o) throws Exception {
        return (o instanceof InstanceCookie.Of && ((InstanceCookie.Of) o).instanceOf(JSeparator.class))
            || JSeparator.class.isAssignableFrom(o.instanceClass());
    }
}
