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

package org.netbeans.modules.languages.ext;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.SyntaxContext;

/**
 *
 * @author Jan Jancura
 */
public class NBS {
    
    private static final String DOC = "org/netbeans/modules/languages/resources/NBS-Library.xml";
    
    public static List<CompletionItem> completion (Context context) {
        if (context instanceof SyntaxContext) {
            SyntaxContext sContext = (SyntaxContext) context;
            ASTPath path = sContext.getASTPath ();
            String c = "root";
            ListIterator<ASTItem> it = path.listIterator (path.size () - 1);
            boolean properties = false;
            while (it.hasPrevious ()) {
                ASTItem item =  it.previous ();
                if (item instanceof ASTToken) continue;
                ASTNode node = (ASTNode) item;
                if (node.getNT ().equals ("regularExpression"))
                    return Collections.<CompletionItem>emptyList ();
                else
                if (node.getNT ().equals ("selector"))
                    return Collections.<CompletionItem>emptyList ();
                else
                if (node.getNT ().equals ("value"))
                    properties = true;
                else
                if (node.getNT ().equals ("command")) {
                    String p = node.getTokenTypeIdentifier ("keyword");
                    if (p != null && properties) {
                        c = p;
                        break;
                    }
                }
            }
            System.out.println(c);
            return getLibrary ().getCompletionItems (c);
        }
        return Collections.<CompletionItem>emptyList ();
    }
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (DOC);
        return library;
    }
}
