/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
