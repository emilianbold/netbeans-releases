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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.text.JTextComponent;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.editor.NbEditorKit.GenerateFoldPopupAction;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesGenerateFoldPopupAction extends GenerateFoldPopupAction {

    protected void addAdditionalItems (JTextComponent target, JMenu menu) {
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
            Set expands = new HashSet ();
            addFoldTypes (target, menu, l, expands);
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                addFoldTypes (target, menu, it.next (), expands);
        } catch (ParseException ex) {
        }
    }

    private void addFoldTypes (JTextComponent target, JMenu menu, Language l, Set expands) {
        Collection c = l.getFeatures (Language.FOLD);
        if (c == null) return;
        Iterator it = c.iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof Evaluator) continue;
            Map feature = (Map) o;
            Evaluator e = (Evaluator) feature.get ("expand_type_action_name");
            if (e == null) continue;
            String expand = (String) e.evaluate ();
            if (expands.contains (expand))
                continue;
            expands.add (expand);
            e = (Evaluator) feature.get ("collapse_type_action_name");
            if (e == null) continue;
            String collapse = (String) e.evaluate ();
            addAction (target, menu, expand);
            addAction (target, menu, collapse);
            setAddSeparatorBeforeNextAction (true);
        }
    }
}
    