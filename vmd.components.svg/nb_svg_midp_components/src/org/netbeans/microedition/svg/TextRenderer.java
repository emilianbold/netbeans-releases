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
import org.w3c.dom.svg.SVGRect;


/**
 * 
 *
 */
class TextRenderer {
    
    TextRenderer( SVGForm form , SVGLocatableElement hiddenTextElement ){
        myForm = form;
        myElement = hiddenTextElement;
    }

    void initEmpiricalLetterWidth( SVGLocatableElement element ) {
        if ( myLetterWidth != 0 ){
            return;
        }
        String text = element.getTrait( SVGComponent.TRAIT_TEXT );
        if ( text != null && text.length() != 0 && element.getBBox() != null){
            text = text.trim();
            myLetterWidth = element.getBBox().getWidth()/text.length();
        }
    }
    
    String truncateToShownText( String text , float boundWidth ) {
        if ( boundWidth ==0 ){
            return "";
        }
        if ( myLetterWidth ==0 ){
            return text;
        }
        
        int count = Math.max( 0, (int)(boundWidth/myLetterWidth));
        
        String result = text;
        result = text.substring( 0 , Math.min( text.length(), count + 1));

        float width = getTextWidth(result);
        boolean isShort = true;
        while ( width > boundWidth && result.length() >0 ) {
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
    
    float getTextWidth(String text) {
        if ( text.endsWith(" ")) {
            return doGetTextWidth( text + "i") - doGetTextWidth("i");
        } else {
            return doGetTextWidth(text);
        }
    }
    
    boolean isEmpiricInitialized(){
        return myLetterWidth == 0;
    }
    
    float doGetTextWidth(String text) {
        float width =0;
        if (text.length() > 0) {
            final String setText = text.trim();
            getForm().invokeAndWaitSafely(new Runnable() {
                public void run() {
                    getHiddenTextElement().setTrait(SVGComponent.TRAIT_TEXT, 
                            setText);
                    /*
                     NB : be carefull with text that is used for set
                          and  resulting text. They could be different.
                          F.e. spaces and tabulations at the beggining and
                          the end are not considered in SVG rendering .
                          As result getTrait( "text" ) could differs from
                          string that was used for setTrait( "text" , string )
                     while ( !setText.equals( getHiddenTextElement().getTrait(
                            SVGComponent.TRAIT_TEXT )) )
                    {
                        try {
                            Thread.sleep(50);
                        }
                        catch (InterruptedException e) {
                        }
                    }*/
                }
            });
            SVGRect bBox = getHiddenTextElement().getBBox();
            if ( bBox != null) {
                width = bBox.getWidth();
            } else {
                //System.out.println("Error: Null BBox #1");
            }
        }
        return width;
    }
    
    SVGLocatableElement getHiddenTextElement() {
        return myElement;
    }
    
    private SVGForm getForm(){
        return myForm;
    }
    
    private       float               myLetterWidth;
    private       SVGForm             myForm;
    private       SVGLocatableElement myElement;
}
