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
package org.netbeans.modules.languages.features;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;


/**
 *
 * @author Jan Jancura
 */
class SemanticHighlightsLayer extends AbstractHighlightsContainer {

    private Document document;
    
    SemanticHighlightsLayer (Document document) {
        this.document = document;
    }

    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
        return new Highlights (document, startOffset, endOffset);
    }

    
    private static class Highlights implements HighlightsSequence {

        private Document            document;
        private int                 endOffset;
        private int                 startOffset1;
        private int                 endOffset1;
        private SimpleAttributeSet  attributeSet;
        private Highlighting        highlighting;
        private ASTNode             ast;
        
        
        private Highlights (Document document, int startOffset, int endOffset) {
            this.document = document;
            this.endOffset = endOffset;
            startOffset1 = startOffset;
            endOffset1 = startOffset;
            highlighting = Highlighting.getHighlighting (document);
            try {
                ast = ParserManagerImpl.get (document).getAST ();
            } catch (ParseException ex) {
                ast = ex.getASTNode ();
            }
        }
        
        public boolean moveNext () {
            if (ast == null) return false;
            attributeSet = new SimpleAttributeSet ();
            do {
                startOffset1 = endOffset1;
                if (startOffset1 >= document.getLength () - 1) return false;
                ASTPath path = ast.findPath (startOffset1);
                if (path == null) return false;
                boolean isTrailing = isTrailing (path);
                ASTNode splitNode = isTrailing ? splitNode (path) : null;
                int i, k = path.size ();
                for ( i = 0; i < k; i++) {
                    ASTItem item = path.get (i);
                    if (isTrailing && splitNode == item) {
                        break;
                    }
                    try {
                        Language language = LanguagesManager.getDefault ().getLanguage (item.getMimeType ());
                        AttributeSet as = null;
                        List<AttributeSet> colors = ColorsManager.getColors (language, path.subPath (i), document);
                        if (colors != null && !colors.isEmpty ()) {
                            for (Iterator<AttributeSet> it = colors.iterator (); it.hasNext ();) {
                                as = it.next ();
                                attributeSet.addAttributes (as);
                            }
                            endOffset1 = path.get (i).getEndOffset ();
                        }
                        as = highlighting.get (item);
                        if (as != null) {
                            attributeSet.addAttributes (as);
                            endOffset1 = path.get (i).getEndOffset ();
                        }
                    } catch (ParseException ex) {
                    }
                }
                if (endOffset1 > startOffset1)
                    return true;
                endOffset1 = path.getLeaf ().getEndOffset ();
                if (endOffset1 == startOffset1)
                    endOffset1++;
            } while (endOffset1 < endOffset);
            return false;
        }

        public int getStartOffset () {
            return startOffset1;
        }

        public int getEndOffset () {
            return endOffset1;
        }

        public AttributeSet getAttributes () {
            return attributeSet;
        }

        private static boolean isTrailing (ASTPath path) {
            try {
                if (path.size () < 2 ||
                    !(path.get (path.size () - 2) instanceof ASTNode)
                )
                    return false;
                ASTNode lastNode = (ASTNode) path.get (path.size () - 2);
                Language language = LanguagesManager.getDefault ().getLanguage (lastNode.getMimeType ());
                Set skipTokens = language.getSkipTokenTypes();
                if (!(path.getLeaf () instanceof ASTToken))
                    return false;
                ASTToken leaf = (ASTToken) path.getLeaf ();
                if (!skipTokens.contains (leaf.getType ())) 
                    return false;
                List<ASTItem> list = lastNode.getChildren ();
                for (ListIterator<ASTItem> iter = list.listIterator (list.size ()); iter.hasPrevious (); ) {
                    ASTItem item = iter.previous ();
                    if (item == leaf)
                        return true;
                    if (!(item instanceof ASTToken) || 
                        !skipTokens.contains (((ASTToken) item).getType ())
                    )
                        break;
                } // for
            } catch (ParseException ex) {
            }
            return false;
        }

        private static ASTNode splitNode (ASTPath path) {
            Iterator iter = path.listIterator ();
            Object o = iter.next();
            while (o instanceof ASTNode) {
                List children = ((ASTNode)o).getChildren ();
                o = iter.next();
                if (!(o instanceof ASTNode)) {
                    break;
                }
                if (children.get (children.size () - 1) != o) {
                    return (ASTNode) o;
                }
            } // while
            return null;
        }
    }
}
