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

import java.util.Comparator;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;

/**
 * Comparator for code templates by their abbreviation name.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateComparator implements Comparator {

    // public static final Comparator BY_ABBREVIATION = new CodeTemplateComparator(true, false);

    public static final Comparator BY_ABBREVIATION_IGNORE_CASE = new CodeTemplateComparator(true, true);

    // public static final Comparator BY_PARAMETRIZED_TEXT = new CodeTemplateComparator(false, false);
    
    public static final Comparator BY_PARAMETRIZED_TEXT_IGNORE_CASE = new CodeTemplateComparator(false, true);
    
    private final boolean byAbbreviation;
    
    private final boolean ignoreCase;
    
    private CodeTemplateComparator(boolean byAbbreviation, boolean ignoreCase) {
        this.byAbbreviation = byAbbreviation;
        this.ignoreCase = ignoreCase;
    }
    
    public int compare(Object o1, Object o2) {
        CodeTemplate t1 = (CodeTemplate)o1;
        CodeTemplate t2 = (CodeTemplate)o2;
        String n1 = byAbbreviation ? t1.getAbbreviation() : t1.getParametrizedText();
        String n2 = byAbbreviation ? t2.getAbbreviation() : t2.getParametrizedText();
        return ignoreCase ? n1.compareToIgnoreCase(n2) : n1.compareTo(n2);
    }
    
}
