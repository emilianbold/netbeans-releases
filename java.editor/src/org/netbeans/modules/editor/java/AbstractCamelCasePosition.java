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

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
/* package */ abstract class AbstractCamelCasePosition extends BaseAction {

    private Action originalAction;
    
    public AbstractCamelCasePosition(String name, Action originalAction) {
        super(name);
        
        if (originalAction != null) {
            Object nameObj = originalAction.getValue(Action.NAME);
            if (nameObj instanceof String) {
                // We will be wrapping around the original action, use its name
                putValue(NAME, nameObj);
                this.originalAction = originalAction;
            }
        }
        
        String desc = getShortDescription();
        if (desc != null) {
            putValue(SHORT_DESCRIPTION, desc);
        }
    }

    public final void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if (originalAction != null && !isUsingCamelCase()) {
                if (originalAction instanceof BaseAction) {
                    ((BaseAction) originalAction).actionPerformed(evt, target);
                } else {
                    originalAction.actionPerformed(evt);
                }
            } else {
                int offset = newOffset(target);
                if (offset != -1) {
                    moveToNewOffset(target, offset);
                }
            }
        }
    }

    protected abstract int newOffset(JTextComponent textComponent);
    protected abstract void moveToNewOffset(JTextComponent textComponent, int offset);

    public String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = NbBundle.getBundle(JavaKit.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }
    
    
    
    private boolean isUsingCamelCase() {
        Preferences p = NbPreferences.forModule(AbstractCamelCasePosition.class);
        return p.getBoolean("useCamelCaseStyleNavigation", true); // NOI18N
    }
}
