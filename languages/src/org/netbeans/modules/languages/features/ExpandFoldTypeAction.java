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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;


/**
 *
 * @author Jan Jancura
 */
public class ExpandFoldTypeAction extends BaseAction {

    public ExpandFoldTypeAction (String name) {
        super (name);
        //putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("expand-all-code-block-folds"));
        //putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-expand-all-code-block-folds"));
    }

    public void actionPerformed (ActionEvent evt, JTextComponent target) {
        FoldHierarchy hierarchy = FoldHierarchy.get (target);
        // Hierarchy locking done in the utility method
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
            if (expand (hierarchy, l)) return;
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                if (expand (hierarchy, it.next ()))
                    return;
        } catch (ParseException ex) {
        }
    }

    private boolean expand (FoldHierarchy hierarchy, Language l) {
        Collection c = l.getFeatures (Language.FOLD);
        Iterator it = c.iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof Evaluator) continue;
            Map feature = (Map) o;
            Evaluator e = (Evaluator) feature.get ("expand_type_action_name");
            if (e == null) continue;
            String expand = (String) e.evaluate ();
            if (!expand.equals (getValue (NAME)))
                continue;
            e = (Evaluator) feature.get ("collapse_type_action_name");
            if (e == null) continue;
            String collapse = (String) e.evaluate ();
            List types = new ArrayList ();
            types.add (Folds.getFoldType (collapse));
            FoldUtilities.expand (hierarchy, types);
            return true;
        }
        return false;
    }
}

