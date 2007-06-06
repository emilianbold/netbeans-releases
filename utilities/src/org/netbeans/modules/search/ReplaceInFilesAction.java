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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The same as the {@link FindInFilesAction} except that this action asks for
 * a replacement string and allows to replace some or all matching substrings
 * with the given replacement string.
 *
 * @author  Marian Petras
 */
public class ReplaceInFilesAction extends FindInFilesAction {

    static final long serialVersionUID = 4554342565076372612L;
    
    @Override
    protected void initialize() {
        super.initialize();

        putProperty(REPLACING, Boolean.TRUE, false);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/find.gif";    //PENDING   //NOI18N
    }
    
    public String getName() {
        String key = SearchScopeRegistry.getDefault().hasProjectSearchScopes()
                     ? "LBL_Action_ReplaceInProjects"                   //NOI18N
                     : "LBL_Action_ReplaceInFiles";                     //NOI18N
        return NbBundle.getMessage(getClass(), key);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ReplaceInFilesAction.class);
    }

}
