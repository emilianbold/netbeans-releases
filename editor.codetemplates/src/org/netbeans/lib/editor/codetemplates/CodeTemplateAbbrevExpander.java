/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;


/**
 * Abbreviation expander for code templates.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class CodeTemplateAbbrevExpander implements AbbrevExpander {
    
    public boolean expand(JTextComponent component, int abbrevStartOffset, CharSequence abbrev) {
        Document doc = component.getDocument();
        CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(doc);
        op.waitLoaded();
        CodeTemplate ct = op.findByAbbreviation(abbrev.toString());
        if (ct != null) {
            // Select the abbrev text
            component.setCaretPosition(abbrevStartOffset + abbrev.length());
            component.moveCaretPosition(abbrevStartOffset);
            ct.insert(component);
            return true;
        }
        return false;
    }

}
