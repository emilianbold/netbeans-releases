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

package org.netbeans.modules.lexer.editorbridge;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.plaf.TextUI;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.NbEditorKit;

public class LexerEditorKit extends NbEditorKit {

    public void install(JEditorPane pane) {
        super.install(pane);

        TextUI ui = pane.getUI();
        if (ui instanceof BaseTextUI) {
            ((BaseTextUI)ui).getEditorUI().addLayer(new LexerLayer(pane), LexerLayer.VISIBILITY);
        }
    }

    /**
     * Possibly change the original coloring name.
     */
    protected String updateColoringName(String coloringName) {
        return coloringName;
    }

}

