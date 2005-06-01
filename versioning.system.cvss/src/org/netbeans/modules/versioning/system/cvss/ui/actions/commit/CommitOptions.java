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

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.util.NbBundle;

import java.util.*;

/**
 * @author Maros Sandor
 */
abstract class CommitOptions {
    
    private static final ResourceBundle loc = NbBundle.getBundle(CommitOptions.class);
    
    public static final CommitOptions ADD_TEXT = new Add("CTL_CommitOption_AddAsText");
    public static final CommitOptions ADD_BINARY = new Add("CTL_CommitOption_AddAsBinary");
    public static final CommitOptions COMMIT = new Commit("CTL_CommitOption_Commit");
    public static final CommitOptions COMMIT_REMOVE = new Commit("CTL_CommitOption_CommitRemove");
    public static final CommitOptions EXCLUDE = new Commit("CTL_CommitOption_Exclude");
    
    private final String bundleKey;

    public CommitOptions(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    public String toString() {
        return loc.getString(bundleKey); 
    }
    
    static class Add extends CommitOptions {
        
        public Add(String bundleKey) {
            super(bundleKey);
        }
    }

    static class Commit extends CommitOptions {
        
        public Commit(String bundleKey) {
            super(bundleKey);
        }
    }
}

