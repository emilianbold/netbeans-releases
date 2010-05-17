/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;


/**
 * Logic implementation is copied from
 * main/vmd.components.svg/nb_svg_midp_components/src/org.netbeans.microedition.svg.SVGListCellRenderer.java
 * @author akorostelev
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

    public void clearContent(){
        Node node = getContentElement().getFirstElementChild();
        while (node != null) {
            Element next = null;
            if (node instanceof SVGElement) {
                next = ((SVGElement) node).getNextElementSibling();
            }
            if (!SVGListDisplayPresenter.METADATA_METADATA.equals(node.getLocalName())) {
                getContentElement().removeChild(node);
            }
            else if (node instanceof SVGElement) {
                String display = ((SVGElement) node).getTrait(SVGListDisplayPresenter.METADATA_DISPLAY);
                if (!SVGListDisplayPresenter.METADATA_NONE.equals(display)) {
                    final Node forRemove = node;
                    getContentElement().removeChild(forRemove);
                }
            }
            node = next;
        }
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
