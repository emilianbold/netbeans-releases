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

package org.netbeans.modules.editor;

import javax.swing.ActionMap;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 *
 * @author  Miloslav Metelka, Martin Roskanin
 */
abstract class GlobalContextAction implements LookupListener{

    private Lookup.Result result;    
    
    public GlobalContextAction() {
        result = Utilities.actionsGlobalContext ().lookup (
            new Lookup.Template (ActionMap.class)
        );
        
        result.addLookupListener(this);
    }

    protected ActionMap getContextActionMap () {
        return (ActionMap)Utilities.actionsGlobalContext ().lookup (ActionMap.class);
    }
    
    public abstract void resultChanged(org.openide.util.LookupEvent ev);
    
}
