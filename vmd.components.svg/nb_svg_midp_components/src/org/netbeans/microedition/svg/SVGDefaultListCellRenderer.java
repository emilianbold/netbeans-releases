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
package org.netbeans.microedition.svg;

import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;


/**
 * Suggested svg list tag :
 * <pre>
 * &lt;g ...&gt;
 *      &lt;text visibility="hidden" font-size="font_size_value"  
 *          font-family="font_family_value" x="x_arg" y="y_arg"&gt; 
 * &lt;/g&gt;
 * &lt;rect id="MAIN_ID_selection" x="x_arg" y="y_arg" stroke="black" stroke-width="1" 
 *  fill="rgb(200,200,255)" visibility="inherit"
        width="list_width" height="0"/&gt;
 * </pre>
 * Absence of inner "text" node will lead to IllegalArgumentException. 
 * Second inner tag ( rectangle ) represent selection figure on the screen.
 * @author ads
 *
 */
public class SVGDefaultListCellRenderer implements SVGListCellRenderer {
    
    private static final String HIDDEN_TEXT_SUFFIX = "_hidden_text";
    private static final String SELECTION_SUFFIX   = "_selection";
    
    private static final float ASCENT_SELECTION   = 1;
    private static final float DESCENT_SELECTION   = 2;

    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.SVGListCellRenderer#getCellRendererComponent(org.netbeans.microedition.svg.SVGList, java.lang.Object, int, boolean)
     */
    public SVGComponent getCellRendererComponent( SVGList list, Object value,
            int index, boolean isSelected )
    {
        SVGLocatableElement xmlElement = list.getElement();
        
        SVGRect rect = xmlElement.getBBox();
        float width = rect.getWidth();
        float height = rect.getHeight();
        
        //float y;
        
        /*SVGLocatableElement lastText = getLastChildElement( xmlElement, "text");
        if ( lastText != null ){
            y=lastText.getBBox().getY()+lastText.getBBox().getHeight();
        }
        else {*/
            SVGLocatableElement lastText;
            SVGLocatableElement hiddenText = (SVGLocatableElement)
            SVGComponent.getElementById( xmlElement, 
                    xmlElement.getId() + HIDDEN_TEXT_SUFFIX );
            if ( hiddenText == null ){
                throw new IllegalArgumentException("List argument " +
                		"doesn't contain hidden text for access to font" +
                		"characteristics. Unable render any value.");
            }
            rect = hiddenText.getBBox();
            myX = rect.getX();
            myY = rect.getY() ;
            myHeight = rect.getHeight();
            lastText = hiddenText;
        //}
        
        SVGLocatableElement textElement = (SVGLocatableElement) list.getForm().
            getDocument().createElementNS( SVGComponent.SVG_NS, "text");
        textElement.setFloatTrait( SVGComponent.TRAIT_X, myX );
        textElement.setFloatTrait( SVGComponent.TRAIT_Y, myY + 
                (index+1)*myHeight ) ;
        textElement.setFloatTrait( "font-size", lastText.getFloatTrait("font-size"));
        textElement.setTrait( "font-family", lastText.getTrait("font-family"));
        textElement.setTrait( SVGComponent.TRAIT_VISIBILITY, "visible");
        if ( value == null ){
            textElement.setTrait( "#text","");
        }
        else {
            textElement.setTrait("#text",  value.toString());
        }
        xmlElement.appendChild(textElement);
        
        if ( isSelected ) {
            showSelection( list , index );
        }
        
        /* 
         * TODO: currently there is no need to use returned component.
         * One only need to render ( via mechanism of SVG ) content.
         * This should be done via code above.
         * But may be later one will need to use rendered component.
         * But CTOR of SVGComponent should be changed or added 
         * new one without need of <code>elemId</code>.
         */
        return null;
    }
    
    /*private SVGLocatableElement getLastChildElement( SVGLocatableElement element,
            String tagName )
    {
        SVGLocatableElement ret = null;
        Element child = element.getFirstElementChild();
        do {
            if ( child instanceof SVGLocatableElement && 
                    tagName.equals( child.getLocalName()) 
                    && ((SVGLocatableElement)child).getId() == null)
            {
                ret = (SVGLocatableElement) child;
            }
            if ( child instanceof SVGElement ){
                child = ((SVGElement)child).getNextElementSibling();
            }
            else {
                child = null;
            }
        }
        while( child != null );
        return ret;
    }*/
    
    private void showSelection( SVGList list, int index ) {
        // TODO : modify a whole code for enabling multiple selection.
        SVGLocatableElement xmlElement = list.getElement();
        SVGLocatableElement selection = 
            (SVGLocatableElement)SVGComponent.getElementById( xmlElement,
                xmlElement.getId() + SELECTION_SUFFIX );
        selection.setFloatTrait(SVGComponent.TRAIT_Y, myY + index*myHeight 
                -ASCENT_SELECTION);
        selection.setFloatTrait( "height", myHeight +DESCENT_SELECTION);
    }

    private float myX;
    private float myY;
    private float myHeight;
    
}
