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
package org.netbeans.modules.vmd.midpnb.screen.display;

import com.sun.perseus.awt.SVGComponent;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLocatableElement;


/**
 * Suggested svg list tag :
 * <pre>
 * &lt;g id="list" visibility="hidden" transform="translate(20,200)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=list&lt;/text>
 *
 *       &lt;g>
 *       &lt;text id="list_hidden_text" visibility="hidden">
 *           HIDDEN TEXT
 *       &lt;/text>
 *       &lt;text display="none">type=hidden_text&lt;/text>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=bound&lt;/text>
 *           &lt;rect id="list_bound" x="5.0" y="0.0" width="80" height="60" fill="white" stroke="black" stroke-width="2" visibility="inherit"/>
 *       &lt;/g>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=selection&lt;/text>
 *           &lt;rect id="list_selection" x="5" y="0" stroke="black" stroke-width="1" fill="rgb(200,200,255)" visibility="inherit" width="80" height="0"/>
 *       &lt;/g>
 *       &lt;g id="list_current_selection" >
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=current_selection&lt;/text> 
 *       &lt;/g>
 *       &lt;g  id="list_content" visibility="inherit">
 *           &lt;!-- Metadata information. Please don't edit. -->
 *           &lt;text display="none">type=content&lt;/text>
 *           &lt;/g>
 *   &lt;/g
 * </pre>
 * Absence of inner "text" node will lead to IllegalArgumentException. 
 * Rectangle ( first "rect" tag ) represents selection figure on the screen.
 * Group tag represent content that will be used as area for rendering
 * in this class. It should be present ( NPE will be thrown otherwise ).  
 * This renderer also needs selection element. 
 * 
 * @author ads
 *
 */
class SVGListCellRenderer extends TextRenderer {
    
    SVGListCellRenderer(Document document, float height, SVGLocatableElement hiddenText,
                SVGLocatableElement bounds, SVGLocatableElement content) {
        super(hiddenText);
        myDocument = document;
        myHeight = height;
        myBounds = bounds;
        myContent = content;

        initEmpiricalLetterWidth(hiddenText);
    }

    public SVGLocatableElement getCellRendererComponent( Object value,
            int index)
    {
        SVGLocatableElement hiddenText = getHiddenTextElement();
        if (hiddenText == null) {
            throw new IllegalArgumentException("List argument "
                    + "doesn't contain hidden text for access to font"
                    + "characteristics. Unable render any value."); // NOI18N
        }
        myX = hiddenText.getFloatTrait(UpdatableSVGComponentDisplayPresenter.TRAIT_X);
        myY = hiddenText.getFloatTrait(UpdatableSVGComponentDisplayPresenter.TRAIT_Y);
        
        if ( getBoundsElement().getBBox() != null 
                && hiddenText.getBBox()!= null && myTextWidth == -1 )
        {
            myTextWidth = getBoundsElement().getBBox().getWidth() + 0.5f - 
                (hiddenText.getBBox().getX() - 
                        getBoundsElement().getBBox().getX()) * 2;
        }
        
        
        final SVGLocatableElement textElement = (SVGLocatableElement) myDocument.
                createElementNS( UpdatableSVGComponentDisplayPresenter.SVG_NS, SVGListDisplayPresenter.TEXT);
        textElement.setFloatTrait( UpdatableSVGComponentDisplayPresenter.TRAIT_X, myX );
        textElement.setFloatTrait( UpdatableSVGComponentDisplayPresenter.TRAIT_Y, myY + index*myHeight ) ;
        textElement.setFloatTrait( SVGListDisplayPresenter.TRAIT_FONT_SIZE,
                hiddenText.getFloatTrait(SVGListDisplayPresenter.TRAIT_FONT_SIZE));
        textElement.setTrait( SVGListDisplayPresenter.TRAIT_FONT_FAMILY,
                hiddenText.getTrait(SVGListDisplayPresenter.TRAIT_FONT_FAMILY));
        textElement.setTrait( UpdatableSVGComponentDisplayPresenter.TRAIT_VISIBILITY,
                UpdatableSVGComponentDisplayPresenter.TR_VALUE_INHERIT);
        if ( value == null ){
            textElement.setTrait( UpdatableSVGComponentDisplayPresenter.TRAIT_TEXT,"");
        }
        else {
            if ( myTextWidth == -1 ){
                textElement.setTrait(UpdatableSVGComponentDisplayPresenter.TRAIT_TEXT,  value.toString());
            }
            else {
                String text = truncateToShownText( value.toString(), myTextWidth );
                textElement.setTrait(UpdatableSVGComponentDisplayPresenter.TRAIT_TEXT,  text);
            }
        }
        
        
        getContentElement().appendChild(textElement);
        return textElement;
    }

    SVGLocatableElement getBoundsElement() {
        return myBounds;
    }

    SVGLocatableElement getContentElement() {
        return myContent;
    }

    private float myX;
    private float myY;
    private float myHeight;
    private float myTextWidth = -1;

    Document myDocument;
    SVGLocatableElement myBounds;
    SVGLocatableElement myContent;
    
}
