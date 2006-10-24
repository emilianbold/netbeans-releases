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

package org.netbeans.api.java.source.gen;

import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;

/**
 * Utility class for tree comparison.
 *
 * @author Pavel Flaska
 */
public class TreeChecker {

    /** Creates a new instance of TreeChecker */
    private TreeChecker() {
    }

    static boolean compareTrees(Tree firstTree, Tree secondTree) {
        return true;
    }
    
    static Map<Object, CharSequence[]> compareTokens(TokenHierarchy hierarchy0, TokenHierarchy hierarchy1) {
        Map result = new HashMap<Integer, String[]>();
        TokenSequence ts0 = hierarchy0.tokenSequence(JavaTokenId.language());
        TokenSequence ts1 = hierarchy1.tokenSequence(JavaTokenId.language());
        while (ts0.moveNext()) {
            if (ts1.moveNext()) {
                if (!TokenUtilities.equals(ts0.token().text(), ts1.token().text())) {
                   result.put(ts0.token().id(), new CharSequence[] { ts0.token().text(), ts1.token().text() });
                }
            }
        }
        if (result.size() > 0) {
           return result;
        } else {
           return Collections.EMPTY_MAP;
        }
    }
}
