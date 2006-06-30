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
