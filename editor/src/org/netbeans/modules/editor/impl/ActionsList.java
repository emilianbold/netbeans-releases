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
import javax.swing.Action;
import javax.swing.JSeparator;
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
    
    // XXX: use this for editor popup menu as well
    
    private final List all;
    private final List actions;

    protected ActionsList(List keys) {
        List [] lists = convert(keys == null ? Collections.emptyList() : keys);
        this.all = lists[0];
        this.actions = lists[1];
    }

    public List getAllInstances() {
        return all;
    }

    public List getActionsOnly() {
        return actions;
    }

    private static List [] convert(List keys) {
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
                        } else if (String.class.isAssignableFrom(ic.instanceClass()) ||
                            JSeparator.class.isAssignableFrom(ic.instanceClass()) ||
                            DataFolder.class.isAssignableFrom(ic.instanceClass()))
                        {
                            Object instance = ic.instanceCreate();
                            all.add(instance);
                        }
                    } catch (Exception e){
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
