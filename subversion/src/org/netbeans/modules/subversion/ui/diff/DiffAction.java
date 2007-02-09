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

package org.netbeans.modules.subversion.ui.diff;

import java.util.*;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.*;
import java.io.File;

import org.openide.nodes.Node;
import org.openide.util.*;

/**
 * Diff action shows local changes
 *
 * @author Petr Kuzel
 */
public class DiffAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Diff";    // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        Context ctx = SvnUtils.getCurrentContext(nodes);
        return SvnUtils.getModifiedFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE).length > 0; 
    }
    
    public static void diff(Context ctx, int type, String contextName) {
        
        DiffMainPanel panel = new DiffMainPanel(ctx, type, contextName); // spawns bacground DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); // NOI18N
        tc.open();
        tc.requestActive();        
    }

    public static void diff(File file, String rev1, String rev2) {
        DiffMainPanel panel = new DiffMainPanel(file, rev1, rev2); // spawns bacground DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();        
    }
    
    protected void performContextAction(Node[] nodes) {
        Context ctx = getContext(nodes);
        String contextName = getContextDisplayName(nodes);
        diff(ctx, Setup.DIFFTYPE_LOCAL, contextName);        
    }
   
}
