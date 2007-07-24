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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.text.JTextComponent;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.editor.NbEditorKit.GenerateFoldPopupAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesGenerateFoldPopupAction extends GenerateFoldPopupAction {

    public static final String EXPAND_PREFIX = "Expand:";
    public static final String COLLAPSE_PREFIX = "Collapse:";
    
    protected void addAdditionalItems (JTextComponent target, JMenu menu) {
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            Set expands = new HashSet ();
            addFoldTypes (target, menu, l, expands);
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                addFoldTypes (target, menu, it.next (), expands);
        } catch (ParseException ex) {
        }
    }

    private void addFoldTypes (JTextComponent target, JMenu menu, Language l, Set expands) {
        List<Feature> features = l.getFeatures (LanguagesFoldManager.FOLD);
        Iterator<Feature> it = features.iterator ();
        while (it.hasNext ()) {
            Feature fold = it.next ();
            String expand = l.localize((String) fold.getValue ("expand_type_action_name"));
            if (expand == null) continue;
            if (expands.contains (expand))
                continue;
            expands.add (expand);
            String collapse = l.localize((String) fold.getValue ("collapse_type_action_name"));
            if (collapse == null) continue;
            addAction (target, menu, EXPAND_PREFIX + expand);
            addAction (target, menu, COLLAPSE_PREFIX + collapse);
            setAddSeparatorBeforeNextAction (true);
        }
    }
}
    