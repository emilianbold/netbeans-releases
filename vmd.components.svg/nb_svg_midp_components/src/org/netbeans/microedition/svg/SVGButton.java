
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

/*
 * SVGButton.java
 *
 * Created on Oct 4, 2007, 1:56:11 PM
 */
package org.netbeans.microedition.svg;

import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRGBColor;

/**
 * Suggested SVG snippet:
 * <pre>
 * &lt;g transform="translate(130,276)" id="button_ok">
 *   &lt;!-- Metadata information. Please don't edit. -->
 *   &lt;text display="none">type=button&lt;/text>
 *
 *       &lt;rect x="-2" y="-2" rx="5" ry="5" width="80" height="30" fill="white"/>
 *       &lt;rect x="1" y="1" rx="5" ry="5" width="81" height="31" fill="rgb(0,0,128)"/>
 *   &lt;g>
 *       &lt;text display="none">type=body&lt;/text>
 *       &lt;rect id="button_ok_body" transform="matrix(1.060988,0.003826782,-0.003826782,1.060988,4.617886,1.9321077)"   
 *           x="0" y="0" rx="5" ry="5" width="80" height="30" fill="rgb(176,196,222)" stroke="rgb(255,165,0)" stroke-width="0">
 *           &lt;animate attributeName="stroke-width" attributeType="XML" begin="button_ok.focusin" dur="0.25s" fill="freeze" to="2"/>
 *           &lt;animate attributeName="stroke-width" attributeType="XML" begin="button_ok.focusout" dur="0.25s" fill="freeze" to="0"/>
 *               &lt;!-- The third and fourth animation elements are used for animate button : on press, on release -->
 *           &lt;animate id="button_ok_body_pressed" "attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(156,176,202)"/>
 *           &lt;animate id="button_ok_body_released" attributeName="fill" attributeType="XML" begin="indefinite" dur="0.25s" fill="freeze" to="rgb(176,196,222)"/>
 *       &lt;/rect>
 *   &lt;/g>
 *   &lt;g>
 *       &lt;text id="button_ok_stext" x="24" y="23" fill="black" font-size="20">
 *       OK&lt;/text>
 *       &lt;text display="none">type=shadow_text&lt;/text>
 *   &lt;/g>
 *   &lt;g>
 *   &lt;text id="button_ok_text" x="23" y="21" fill="gray" font-size="20">
 *       OK&lt;/text>
 *       &lt;text display="none">type=text&lt;/text>
 *   &lt;/g>
 *   &lt;/g>
 * </pre>
 * 
 * 
 */
public class SVGButton extends SVGAbstractButton {
    
    private       SVGRGBColor         bodyColor;

    private static final String STEXT = "stext";            // NOI18N
    private static final String STEXT_SUFFIX = DASH+STEXT;  // NOI18N
    
    public SVGButton( SVGForm form, String elemId) {
        super(form, elemId);

        myShadowText = (SVGLocatableElement) getElementById( getElement(),
                getElement().getId() + STEXT_SUFFIX);
    }
        
    public void pressButton() { 
        /*
        form.activate(this);
        if (bodyElement != null) {
            bodyColor = bodyElement.getRGBColorTrait(TRAIT_FILL);
            SVGColor color = new SVGColor(bodyColor);
            color.darken();
            //TODO Perseus API required here !!??!!
            bodyElement.setRGBColorTrait(TRAIT_FILL, color);
        }
         */
        setSelected(true);
        super.pressButton();
    }
    
    public void releaseButton() {
        /*
        if (bodyElement != null && bodyColor != null) {
            bodyElement.setRGBColorTrait(TRAIT_FILL, bodyColor);
        }
        bodyColor = null;
         */
        setSelected(false);
        super.releaseButton();
        fireActionPerformed();
    }
    
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        if ( isSelected != selected) {
            isSelected = selected;
        }
    }

    public void setText(String text){
       super.setText(text);
       if ( text == null ){
           text ="";
       }
       if ( myShadowText != null ){
            setTraitSafely(myShadowText, SVGTextField.TRAIT_TEXT, text.trim());
       }
    }
    
    private       boolean             isSelected = false;
    private       SVGLocatableElement myShadowText;
}
