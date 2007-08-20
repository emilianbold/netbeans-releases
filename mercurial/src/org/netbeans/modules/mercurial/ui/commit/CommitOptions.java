/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.commit;

import org.openide.util.NbBundle;

/**
 * @author Maros Sandor
 */
abstract class CommitOptions {

    public static final CommitOptions COMMIT = new Commit("CTL_CommitOption_Commit"); // NOI18N
    public static final CommitOptions COMMIT_REMOVE = new Commit("CTL_CommitOption_CommitRemove"); // NOI18N
    public static final CommitOptions EXCLUDE = new Commit("CTL_CommitOption_Exclude"); // NOI18N
    
    private final String bundleKey;

    public CommitOptions(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    public String toString() {
        return NbBundle.getMessage(CommitOptions.class, bundleKey);
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

