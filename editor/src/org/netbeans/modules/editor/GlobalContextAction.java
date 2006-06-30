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
