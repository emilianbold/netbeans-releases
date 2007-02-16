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

import java.util.ResourceBundle;
import java.awt.*;
import org.netbeans.editor.ImplementationProvider;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.impl.GlyphGutterActionsProvider;
import org.netbeans.modules.editor.impl.NbEditorImplementationProvider;

/** This is NetBeans specific provider of functionality.
 * See base class for detailed comments.
 *
 * @author David Konecny
 * @since 10/2001
 * @deprecated Without any replacement.
 */
public class NbImplementationProvider extends ImplementationProvider {

    public static final String GLYPH_GUTTER_ACTIONS_FOLDER_NAME = 
        GlyphGutterActionsProvider.GLYPH_GUTTER_ACTIONS_FOLDER_NAME;
    
    private transient NbEditorImplementationProvider provider;
    
    public NbImplementationProvider() {
        provider = new NbEditorImplementationProvider();
    }
    
    /** Ask NbBundle for the resource bundle */
    public ResourceBundle getResourceBundle(String localizer) {
        return provider.getResourceBundle(localizer);
    }

    public Action[] getGlyphGutterActions(JTextComponent target) {
        return provider.getGlyphGutterActions(target);
    }
    
    public boolean activateComponent(JTextComponent c) {
        return provider.activateComponent(c);
    }
}
