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

package org.netbeans.modules.palette;

import org.openide.ErrorManager;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Libor Kotouc
 */
class ActiveEditorDropProvider implements InstanceContent.Convertor {
    
    private static ActiveEditorDropProvider instance = new ActiveEditorDropProvider();
    
    /** Creates a new instance of ActiveEditorDropProvider */
    private ActiveEditorDropProvider() {
    }
    
    static ActiveEditorDropProvider getInstance() {
        return instance;
    }
    
    public Class type(Object obj) {
        //able to convert String instances only
        if (obj instanceof String)
            return ActiveEditorDrop.class;
        
        return null;
        
    }

    public String id(Object obj) {
        return obj.toString();
    }

    public String displayName(Object obj) {
        return ((Class)obj).getName();
    }

    public Object convert(Object obj) {
        Object drop = null;
        if (obj instanceof String)
            drop = getActiveEditorDrop((String)obj);

        return drop;
    }
    
    private ActiveEditorDrop getActiveEditorDrop(String instanceName) {

        ActiveEditorDrop drop = null;

        if (instanceName != null && instanceName.trim().length() > 0) {//we should try to instantiate item drop
            try {
                ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
                if (loader == null)
                    loader = getClass ().getClassLoader ();
                Class instanceClass = loader.loadClass (instanceName);
                drop = (ActiveEditorDrop)instanceClass.newInstance();
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        return drop;
    }
    
}
