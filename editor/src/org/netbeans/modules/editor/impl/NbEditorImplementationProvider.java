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

package org.netbeans.modules.editor.impl;

import java.util.ResourceBundle;
import java.awt.*;
import java.util.List;
import java.util.List;
import org.openide.util.NbBundle;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.EditorImplementationProvider;
import org.openide.windows.TopComponent;

/** 
 * 
 * @author Vita Stejskal
 */
public final class NbEditorImplementationProvider implements EditorImplementationProvider {

    private static final Action [] NO_ACTIONS = new Action[0];
    
    public NbEditorImplementationProvider() {
        
    }
    
    /** Ask NbBundle for the resource bundle */
    public ResourceBundle getResourceBundle(String localizer) {
        return NbBundle.getBundle(localizer);
    }
    
    public Action[] getGlyphGutterActions(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target);
        if (mimeType != null) {
            List actions = GlyphGutterActionsProvider.getGlyphGutterActions(mimeType);
            return (Action []) actions.toArray(new Action [actions.size()]);
        } else {
            return NO_ACTIONS;
        }
    }
    
    public boolean activateComponent(JTextComponent c) {
        Container container = SwingUtilities.getAncestorOfClass(TopComponent.class, c);
        if (container != null) {
            ((TopComponent)container).requestActive();
            return true;
        }
        return false;
    }
}
