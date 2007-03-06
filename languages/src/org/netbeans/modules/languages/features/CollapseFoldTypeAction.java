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

package org.netbeans.modules.languages.features;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseAction;


/**
 *
 * @author Jan Jancura
 */
public class CollapseFoldTypeAction extends BaseAction {

    public CollapseFoldTypeAction (String name){
        super (name);
//            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("collapse-all-code-block-folds"));
//            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-collapse-all-code-block-folds"));
    }

    public void actionPerformed (ActionEvent evt, JTextComponent target) {
        FoldHierarchy hierarchy = FoldHierarchy.get (target);
        // Hierarchy locking done in the utility method
        List<FoldType> types = new ArrayList<FoldType> ();
        types.add (Folds.getFoldType ((String) getValue (NAME)));
        FoldUtilities.collapse (hierarchy, types);
    }
}

