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
package org.netbeans.microedition.svg;

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
 * 
 *
 */
public class SVGDefaultListCellRenderer extends TextRenderer 
    implements SVGListCellRenderer 
{
    
    private static final String HEIGHT      = "height";             // NOI18N
    private static final String WIDTH       = "width";              // NOI18N
    private static final String STROKE      = "stroke";             // NOI18N
    private static final String STROKE_WIDTH= "stroke-width";       // NOI18N
    private static final String FILL        = "fill";               // NOI18N
    
    
    private static final float ASCENT_SELECTION   = 2;
    private static final float DESCENT_SELECTION   = 2;
    
    SVGDefaultListCellRenderer( float height , SVGForm form , 
            SVGLocatableElement hiddenText )
    {
        super(form, hiddenText);
        myHeight = height;
        initEmpiricalLetterWidth( hiddenText );
    }

    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.SVGListCellRenderer#getCellRendererComponent(org.netbeans.microedition.svg.SVGList, java.lang.Object, int, boolean)
     */
    public SVGComponent getCellRendererComponent( SVGList list, Object value,
            int index, boolean isSelected , boolean cellHasFocus)
    {
        final SVGLocatableElement content = list.getContent();
        
        SVGLocatableElement hiddenText = list.getHiddenText();
        if (hiddenText == null) {
            throw new IllegalArgumentException("List argument "
                    + "doesn't contain hidden text for access to font"
                    + "characteristics. Unable render any value."); // NOI18N
        }
        myX = hiddenText.getFloatTrait(SVGComponent.TRAIT_X);
        myY = hiddenText.getFloatTrait(SVGComponent.TRAIT_Y);
        
        if ( list.getBoundsElement().getBBox() != null 
                && hiddenText.getBBox()!= null && myTextWidth == -1 )
        {
            myTextWidth = list.getBoundsElement().getBBox().getWidth() + 0.5f - 
                (hiddenText.getBBox().getX() - 
                        list.getBoundsElement().getBBox().getX()) * 2;
        }
        
        
        if ( isSelected ) {
            showSelection( list , index , cellHasFocus );
        }
        
        final SVGLocatableElement textElement = (SVGLocatableElement) list.getForm().
            getDocument().createElementNS( SVGComponent.SVG_NS, SVGTextField.TEXT);
        textElement.setFloatTrait( SVGComponent.TRAIT_X, myX );
        textElement.setFloatTrait( SVGComponent.TRAIT_Y, myY + index*myHeight ) ;
        textElement.setFloatTrait( SVGTextField.TRAIT_FONT_SIZE, 
                hiddenText.getFloatTrait(SVGTextField.TRAIT_FONT_SIZE));
        textElement.setTrait( SVGTextField.TRAIT_FONT_FAMILY, 
                hiddenText.getTrait(SVGTextField.TRAIT_FONT_FAMILY));
        textElement.setTrait( SVGComponent.TRAIT_VISIBILITY, 
                SVGComponent.TR_VALUE_INHERIT);
        if ( value == null ){
            textElement.setTrait( SVGComponent.TRAIT_TEXT,"");
        }
        else {
            if ( myTextWidth == -1 ){
                textElement.setTrait(SVGComponent.TRAIT_TEXT,  value.toString());
            }
            else {
                String text = truncateToShownText( value.toString(), myTextWidth );
                textElement.setTrait(SVGComponent.TRAIT_TEXT,  text);
            }
        }
        
        
        content.appendChild(textElement);
        
        return new SVGLabel( list.getForm() ,textElement );
    }
    
    private void showSelection( final SVGList list, final int index , 
            boolean focused) 
 {

        SVGLocatableElement selection = list.getSelection();

        if (selection == null) {
            throw new IllegalArgumentException("List argument " + "doesn't contain nested 'selection' element" + ". Unable render any value.");        // NOI18N
        }

        selection = createSelection(list, selection, index, focused);
        list.getCurrentSelection().appendChild(selection);

    }
    
    private SVGLocatableElement createSelection( final SVGList list, 
            SVGLocatableElement selection , int index , boolean focused)
    {
        SVGLocatableElement result = (SVGLocatableElement) list.getForm().
            getDocument().createElementNS( SVGComponent.SVG_NS, "rect");
        result.setFloatTrait( SVGComponent.TRAIT_X, 
                selection.getFloatTrait(SVGComponent.TRAIT_X) );
        result.setFloatTrait( WIDTH, selection.getFloatTrait(WIDTH ));
        result.setRGBColorTrait( FILL , selection.getRGBColorTrait(FILL));
        result.setTrait( SVGComponent.TRAIT_VISIBILITY, 
            SVGComponent.TR_VALUE_INHERIT);
        
        result.setFloatTrait(SVGComponent.TRAIT_Y, myY + (index - 1)
                * myHeight + ASCENT_SELECTION);
        result.setFloatTrait(HEIGHT, myHeight + DESCENT_SELECTION);
        
        if ( focused ){
            result.setRGBColorTrait(STROKE, selection.getRGBColorTrait(STROKE));
            result.setFloatTrait( STROKE_WIDTH, selection.getFloatTrait( STROKE_WIDTH));
        }
        
        if ( !list.isSlave() ){
            result.setTrait( SVGComponent.TRAIT_VISIBILITY, 
                    SVGComponent.TR_VALUE_VISIBLE);
        }
        return result;
    }

    private float myX;
    private float myY;
    private float myHeight;
    private float myTextWidth = -1;
    
}
