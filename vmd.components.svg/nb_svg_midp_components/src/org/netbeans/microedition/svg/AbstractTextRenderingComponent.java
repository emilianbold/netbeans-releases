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
 * @author ads
 *
 */
public abstract class AbstractTextRenderingComponent extends SVGComponent {

    public AbstractTextRenderingComponent( SVGForm form, String elemId ) {
        super(form, elemId);
    }
    
    public AbstractTextRenderingComponent( SVGForm form, 
            SVGLocatableElement element) 
    {
        super(form, element);
    }

    protected abstract SVGLocatableElement getHiddenTextElement();
    
    protected void initEmpiricalLetterWidth( SVGLocatableElement element ) {
        if ( myLetterWidth != 0 ){
            return;
        }
        String text = element.getTrait( TRAIT_TEXT );
        if ( text != null && text.length() != 0 && element.getBBox() != null){
            myLetterWidth = element.getBBox().getWidth()/text.length();
        }
    }
    
    protected String truncateToShownText( String text , float boundWidth ) {
        int count = (int)(boundWidth/myLetterWidth);
        String result = text;
        result = text.substring( 0 , Math.min( text.length(), count + 1));

        float width = getTextWidth(result);
        boolean isShort = true;
        while ( width > boundWidth) {
            result = result.substring(0, result.length() - 1);
            width = getTextWidth(result);
            isShort = false;
        }
        
        String tmpText = result;
        while ( isShort && width <= boundWidth && count <= text.length() ){
            result = tmpText;
            tmpText = text.substring( 0, count );
            count ++;
            width = getTextWidth(tmpText);
        }
        if ( count == text.length() ){
            result = tmpText;
        }
        return result;
    }
    
    protected float getTextWidth(String text) {
        if ( text.endsWith(" ")) {
            return doGetTextWidth( text + "i") - doGetTextWidth("i");
        } else {
            return doGetTextWidth(text);
        }
    }
    
    protected boolean isEmpiricInitialized(){
        return myLetterWidth == 0;
    }
    
    private float doGetTextWidth(String text) {
        float width = 0;
        if (text.length() > 0) {
            setTraitSafely( getHiddenTextElement() , TRAIT_TEXT, text);
            SVGRect bBox = getHiddenTextElement().getBBox();
            if ( bBox != null) {
                width = bBox.getWidth();
            } else {
                //System.out.println("Error: Null BBox #1");
            }
        }
        return width;
    }
    
    private       float               myLetterWidth;
}
