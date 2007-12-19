/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.languages.php;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;


/**
 * This implementation is not needed actually.
 * The same functionality could be got via DefaultsColors.xml file.
 * It is enabled now in layer.xml but file DefaultsColors.xml doesn't contain
 * any coloring actions. One can write this file in appropriate way 
 * for getting different background for HTML block and PHP block.
 * 
 * I keep current implementation of this highlighting for future purpose:
 * we can change dynamically background for PHP and HTML.    
 * 
 * @author ads
 *
 */
public class HighlightFactory implements HighlightsLayerFactory {
    
    private static final String LAYER_ID    = "php-block-layer";        // NOI18N       
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.highlighting.HighlightsLayerFactory#createLayers(org.netbeans.spi.editor.highlighting.HighlightsLayerFactory.Context)
     */
    public HighlightsLayer[] createLayers( Context ctx ) {
        Document doc = ctx.getDocument();
        HighlightsLayer layer = HighlightsLayer.create( LAYER_ID, 
                ZOrder.SYNTAX_RACK, false, new PhpBlockContaniner( doc ) );
        return new HighlightsLayer[] { layer };
    }
    
}
/**
 *  TODO : need review . Check org.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting
 */ 
class PhpBlockContaniner implements HighlightsContainer {
    
    //private static final Color BACKGROUND_COLOR = new Color( 0xfd, 0xf2, 0xc4);
    private static final Color BACKGROUND_COLOR = new Color( 0xd8, 0xee, 0xd2);
    
    PhpBlockContaniner( Document doc ) {
        myDocument = doc;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.highlighting.HighlightsContainer#addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
     */
    public void addHighlightsChangeListener( HighlightsChangeListener arg0 ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.highlighting.HighlightsContainer#getHighlights(int, int)
     */
    public HighlightsSequence getHighlights( int start, int end ) {
        return new PhpBlockHighlightsSequence( start , end );
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.highlighting.HighlightsContainer#removeHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)
     */
    public void removeHighlightsChangeListener( HighlightsChangeListener arg0 ) {
        // TODO Auto-generated method stub
        
    }
    
    private class PhpBlockHighlightsSequence implements HighlightsSequence {

        private static final int INIT_STATE              = 0;
        
        private static final int DELIMETER_STATE         = 1;
        
        private static final int DELIMETER1_STATE        = 2;
        
        
        PhpBlockHighlightsSequence( int start , int end){
            myStart = start;
            myEnd = end;
            TokenHierarchy hierarchy = TokenHierarchy.get( myDocument );
            mySequence = hierarchy.tokenSequence();
            mySequence.move( myStart );
            myState = INIT_STATE;
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.editor.highlighting.HighlightsSequence#getAttributes()
         */
        public AttributeSet getAttributes() {
            Token token = mySequence.token();
            
            if ( token.id() == PhpTokenId.PHP ) {
                SimpleAttributeSet set = new SimpleAttributeSet();
                // TODO : color should not be hardcoded. It should be set via options.
                set.addAttribute( StyleConstants.Background, 
                        BACKGROUND_COLOR);
                set.addAttribute( HighlightsContainer.ATTR_EXTENDS_EOL, true );
                return set;
            }
            else if ( isDelimeter(token) ) {
                SimpleAttributeSet set = new SimpleAttributeSet();
                // TODO : color should not be hardcoded. It should be set via options.
                set.addAttribute( StyleConstants.Background, 
                        BACKGROUND_COLOR);
                set.addAttribute( StyleConstants.Bold, true );
                return set;
            }
            return SimpleAttributeSet.EMPTY;
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.editor.highlighting.HighlightsSequence#getEndOffset()
         */
        public int getEndOffset() {
            return mySequence.offset() + mySequence.token().length();
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.editor.highlighting.HighlightsSequence#getStartOffset()
         */
        public int getStartOffset() {
            return mySequence.offset();
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.editor.highlighting.HighlightsSequence#moveNext()
         */
        public boolean moveNext() {
            return mySequence.moveNext() && mySequence.offset()<myEnd;
        }
        
        private boolean isDelimeter( Token token ) {
            boolean ret = false;
            if ( token.id() == PhpTokenId.DELIMITER ) {
                if ( myState == INIT_STATE ) {
                    ret = true;
                    myState = DELIMETER_STATE;
                }
                else if ( myState == DELIMETER_STATE ) {
                    ret = true;
                    myState  = INIT_STATE;
                }
            }
            else if ( token.id() == PhpTokenId.DELIMITER1 || 
                    token.id() == PhpTokenId.DELIMITER2 ) 
            {
                if ( myState == INIT_STATE ) {
                    ret = true;
                    myState = DELIMETER1_STATE;
                }
            }
            else if ( token.id() == PhpTokenId.DELIMITER_END ) 
            {
                if ( myState == DELIMETER1_STATE ) {
                    ret = true;
                    myState = INIT_STATE;
                }
            }
            return ret;
        }
        
        private int myStart;
        
        private int myEnd;
        
        private int myState;
        
        private TokenSequence mySequence;
        
    }
    
    private Document myDocument;
    
}

