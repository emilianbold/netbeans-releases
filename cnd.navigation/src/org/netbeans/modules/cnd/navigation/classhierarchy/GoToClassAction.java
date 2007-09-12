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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;

/**
 * @author Alexander Simon
 */
public class GoToClassAction extends AbstractAction {
    
    private CsmOffsetable csmObject;
    private Action delegate;
    
    public GoToClassAction(CsmOffsetable csmObject, Action delegate) {
        this.csmObject = csmObject;
        this.delegate = delegate;
        putValue(Action.NAME, NbBundle.getMessage(GoToClassAction.class, "LBL_GoToClass")); //NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        CsmUtilities.openSource(csmObject);
        if (delegate != null){
            delegate.actionPerformed(e);
        }
    }
}
