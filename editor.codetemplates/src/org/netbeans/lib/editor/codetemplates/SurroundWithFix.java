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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class SurroundWithFix implements Fix {
    
    private static String SURROUND_WITH = NbBundle.getMessage(SurroundWithFix.class, "TXT_SurroundWithHint_Prefix"); //NOI18N
    
    public static List getFixes(JTextComponent component) {
        List fixes = new ArrayList();
        Document doc = component.getDocument();
        CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(doc);
        op.waitLoaded();
        Collection/*<CodeTemplateFilter>*/ filters = op.getTemplateFilters(component, component.getSelectionStart());
        for (Iterator it = op.findSelectionTemplates().iterator(); it.hasNext();) {
            CodeTemplate template = (CodeTemplate)it.next();
            if (accept(template, filters))
                fixes.add(new SurroundWithFix(template, component));
        }
        return fixes;
    }
    
    private CodeTemplate template;
    private JTextComponent component;
    
    /** Creates a new instance of SurroundWithFix */
    private SurroundWithFix(CodeTemplate template, JTextComponent component) {
        this.template = template;
        this.component = component;
    }

    public String getText() {
        return SURROUND_WITH + template.getDescription();
    }

    public ChangeInfo implement() {
        template.insert(component);
        return null;
    }
    
    private static boolean accept(CodeTemplate template, Collection/*<CodeTemplateFilter>*/ filters) {
        for(Iterator it = filters.iterator(); it.hasNext();) {
            CodeTemplateFilter filter = (CodeTemplateFilter)it.next();
            if (!filter.accept(template))
                return false;
        }
        return true;
    }
}
