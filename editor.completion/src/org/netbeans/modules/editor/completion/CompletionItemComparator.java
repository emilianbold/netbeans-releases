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

package org.netbeans.modules.editor.completion;

import java.util.Comparator;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 * Comparator for completion items either by sort priority or by sort text.
 *
 * @author Dusan Balek, Miloslav Metelka
 */

public class CompletionItemComparator implements Comparator {

    public static final Comparator BY_PRIORITY = new CompletionItemComparator(true);

    public static final Comparator ALPHABETICAL = new CompletionItemComparator(false);
    
    private final boolean byPriority;
    
    private CompletionItemComparator(boolean byPriority) {
        this.byPriority = byPriority;
    }
    
    public static final Comparator get(int sortType) {
        if (sortType == CompletionResultSet.PRIORITY_SORT_TYPE)
            return BY_PRIORITY;
        if (sortType == CompletionResultSet.TEXT_SORT_TYPE)
            return ALPHABETICAL;
        throw new IllegalArgumentException();
    }
    
    public int compare(Object o1, Object o2) {
        assertCompletionItem(o1);
        assertCompletionItem(o2);
        if (o1 == o2)
            return 0;
        CompletionItem i1 = (CompletionItem)o1;
        CompletionItem i2 = (CompletionItem)o2;
        if (byPriority) {
            int importanceDiff = i1.getSortPriority() - i2.getSortPriority();
            if (importanceDiff != 0)
                return importanceDiff;
            int alphabeticalDiff = compareText(i1.getSortText(), i2.getSortText());
            if (alphabeticalDiff != 0)
                return alphabeticalDiff;
        } else {
            int alphabeticalDiff = compareText(i1.getSortText(), i2.getSortText());
            if (alphabeticalDiff != 0)
                return alphabeticalDiff;
            int importanceDiff = i1.getSortPriority() - i2.getSortPriority();
            if (importanceDiff != 0)
                return importanceDiff;
        }
        return -1;
    }
    
    private static void assertCompletionItem(Object o) {
        assert (o instanceof CompletionItem)
            : "Non CompletionItem instance " // NOI18N
                + o + ":" + ((o != null) ? o.getClass().getName() : "<null>") // NOI18N
                + " appeared in the code completion result list"; // NOI18N
    }
    
    private static int compareText(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = text1.charAt(i);
            char ch2 = text2.charAt(i);
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }

}
