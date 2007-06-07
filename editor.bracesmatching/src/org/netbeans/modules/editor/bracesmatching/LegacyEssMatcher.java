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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bracesmatching;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Vita Stejskal
 */
public final class LegacyEssMatcher implements BracesMatcher, BracesMatcherFactory {

    private final MatcherContext context;
    private final ExtSyntaxSupport ess;
    
    private int [] block;
    
    public LegacyEssMatcher() {
        this(null, null);
    }

    private LegacyEssMatcher(MatcherContext context, ExtSyntaxSupport ess) {
        this.context = context;
        this.ess = ess;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int[] findOrigin() throws BadLocationException {
        int offset;
        
        if (context.isSearchingBackward()) {
            offset = context.getSearchOffset() - 1;
        } else {
            offset = context.getSearchOffset();
        }
        
        block = ess.findMatchingBlock(offset, false);
        return block == null ? null : new int [] { offset, offset };
    }

    public int[] findMatches() throws InterruptedException {
        return block;
    }

    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    public BracesMatcher createMatcher(MatcherContext context) {
        Document d = context.getDocument();
        
        if (d instanceof BaseDocument) {
            SyntaxSupport ss = ((BaseDocument) d).getSyntaxSupport();
            if (ss instanceof ExtSyntaxSupport && ss.getClass() != ExtSyntaxSupport.class) {
                return new LegacyEssMatcher(context, (ExtSyntaxSupport) ss);
            }
        }
        
        return null;
    }

}
