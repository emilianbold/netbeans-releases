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

/**
 * Suggested SVG snippet:
 * <pre>
 * &lt;g transform="translate(20,190)">
 *       &lt;rect x="0" y="0" rx="5" ry="5" width="200" height="60" fill="none" stroke="#85a6cf" stroke-width="2"/>
 *       &lt;g id="radio_male" transform="translate(7,5)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=radio&lt;/text>
 *
 *           &lt;rect x="0" y="0" rx="5" ry="5" width="185" height="24" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_male.focusin" fill="freeze" to="visible"/>
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_male.focusout" fill="freeze" to="hidden"/>
 *           &lt;/rect>
 *           &lt;circle id="radio_male_dot" cx="11" cy="12" r="8" fill="white" stroke="black" stroke-width="2"/>
 *       &lt;g>
 *           &lt;text display="none">type=dot&lt;/text>
 *           &lt;circle cx="11" cy="12" r="4" fill="black" visibility="hidden"/>
 *       &lt;g    
 *          &lt;text id="radio_male_text" x="24" y="17" stroke="gray" font-size="15">
 *          Male&lt;/text>
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=text&lt;/text>
 *       &lt;/g>
 *
 *       &lt;g> id="radio_female" transform="translate(7,33)">
 *       &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=radio&lt;/text>
 *
 *           &lt;rect x="0" y="0" rx="5" ry="5" width="185" height="24" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_female.focusin" fill="freeze" to="visible"/>
 *               &lt;set attributeName="visibility" attributeType="XML" begin="radio_female.focusout" fill="freeze" to="hidden"/>
 *           &lt;/rect>
 *           &lt;circle transform="translate(11, 12)" cx="0" cy="0" r="8" fill="white" stroke="black" stroke-width="2"/>
 *       &lt;g>
 *           &lt;!-- Metadata information. Please don't edit. -->
 *       &lt;text display="none">type=dot&lt;/text>
 *
 *       &lt;circle id="radio_female_dot" cx="11" cy="12" r="4" fill="black" visibility="hidden"/>
 *       &lt;g>
 *          &lt;text id="radio_female_text" x="24" y="17" stroke="gray" font-size="15">
 *          Female&lt;/text>
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=text&lt;/text>
 *       &lt/g>
 *   &lt;/g>
 * </pre>
 *
 * @author Pavel Benes
 * @author ads
 */
public class SVGRadioButton extends SVGAbstractButton {
    
    private static final String DOT         = "dot";           // NOI18N
    private static final String DOT_SUFFIX  = DASH+DOT;        // NOI18N 
    
    public SVGRadioButton( SVGForm form, String elemId) {
        super(form, elemId);
        initNestedElements();
        
        //isSelected = form.registerRadioButton(this);
        updateTrait();
    }

    /**
     * 
     */
    private void initNestedElements() {
        if ( getElement().getId() != null ){
            myDotElement = (SVGLocatableElement) getElementById( getElement(), 
                    getElement().getId() + DOT_SUFFIX );
        }
        if ( myDotElement == null ){
            myDotElement = (SVGLocatableElement) getNestedElementByMeta( getElement(), 
                    TYPE, DOT );
        }
        
    }
    
    public void setSelected( boolean selected) {
        if ( isSelected != selected) {
            isSelected = selected;
            updateTrait();
            fireActionPerformed();
        }
    }
    
    public boolean isSelected() {
        return isSelected;
    }
         
    public void pressButton() { 
        if ( !isSelected) {
            form.activate(this);
            setSelected( true);
        }
    }
    
    private void updateTrait() {
        setTraitSafely(myDotElement , TRAIT_VISIBILITY, 
                isSelected ? TR_VALUE_VISIBLE : TR_VALUE_HIDDEN );
    }
    
    private SVGLocatableElement myDotElement;
    private       boolean             isSelected;
}
