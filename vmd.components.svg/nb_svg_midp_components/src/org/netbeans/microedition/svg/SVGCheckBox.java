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
 * Suggested SVG snippet :
 * <pre>
 * &lt;g id="checkbox_online" transform="translate(130,220)">
 *      &lt;!-- Metadata information. Please don't edit. -->
 *      &lt;text display="none">type=checkbox&lt;/text>
 *
 *      &lt;rect x="0" y="0" rx="5" ry="5" width="200" height="30" fill="none" stroke="rgb(255,165,0)" stroke-width="2" visibility="hidden">
 *          &lt;set attributeName="visibility" attributeType="XML" begin="checkbox_online.focusin" fill="freeze" to="visible"/>
 *          &lt;set attributeName="visibility" attributeType="XML" begin="checkbox_online.focusout" fill="freeze" to="hidden"/>
 *      &lt;/rect>
 *      &lt;rect x="7.4" y="5" width="20" height="20" fill="white" stroke="black" stroke-width="2"/>
 *      &lt;g>
 *          &lt;path id="checkbox_online_mark" transform="translate(12.5,15.5)"  d="M 0,0 5,5 5,-12" fill="none" stroke="black" stroke-width="2"/>
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=mark&lt;/text>    
 *      &lt;/g>
 *      &lt;g>
 *          &lt;text id="checkbox_online_title" x="33.8" y="21.6" stroke="gray" font-size="15">
 *          Online&lt;/text>
 *          &lt;!-- Metadata information. Please don't edit. -->
 *          &lt;text display="none">type=text&lt;/text>
 *      &lt;/g>
 *      
 *  &lt;/g
 * </pre>
 * @author Pavel Benes
 * @author ads
 */
public class SVGCheckBox extends SVGAbstractButton {
    
    private static final String MARK        = "mark";              // NOI18N
    private static final String TEXT        = "text";              // NOI18N
    private static final String TITLE       = "title";             // NOI18N
    
    private static final String MARK_SUFFIX = DASH+ MARK;
    private static final String TITLE_SUFFIX= DASH+ TITLE;
    
    public SVGCheckBox( SVGForm form, String elemId) {
        super(form, elemId);
        initNestedElements();
        verify();
        setTraitSafely( myMarkElement , TRAIT_VISIBILITY, TR_VALUE_HIDDEN );
        isSelected = false;
    }

    public void setSelected( boolean selected) {
        if ( isSelected != selected) {
            setTraitSafely( myMarkElement , TRAIT_VISIBILITY, selected ? 
                    TR_VALUE_VISIBLE : TR_VALUE_HIDDEN);
            isSelected = selected;
            fireActionPerformed();
        }
    }
    
    public boolean isSelected() {
        return isSelected;
    }
         
    public void pressButton() { 
        form.activate(this);
        setSelected( !isSelected);
    }
    
    public String getText(){
        return myTextElement.getTrait( TRAIT_TEXT );
    }
    
    public void setText( String text ){
        setTraitSafely( myTextElement, TRAIT_TEXT, text );
    }
    
    private void initNestedElements() {
        if (getElement().getId() != null) {
            myMarkElement = (SVGLocatableElement) getElementById(
                    getElement(), getElement().getId() + MARK_SUFFIX );
            myTextElement = (SVGLocatableElement) getElementById( getElement(),
                    getElement().getId() + TITLE_SUFFIX );
        }
        
        if ( myMarkElement == null ){
            myMarkElement = (SVGLocatableElement) getNestedElementByMeta(
                    getElement(), TYPE , MARK );
        }
        
        if ( myTextElement == null ){
            myTextElement = (SVGLocatableElement) getNestedElementByMeta(
                    getElement(), TYPE , TEXT );
        }
    }
    
    private void verify() {
        /*
         *  Should we check meta information f.e. type of component here
         *  for preventing creation based on incorrect element ? 
         */
        // TODO : check type of element.
        
        if ( myMarkElement == null || myTextElement == null ){
            throw new IllegalArgumentException("Element with id=" +
                    getElement().getId()+" couldn't be used for Checkbox." +
                            " It doesn't have nested 'mark' or 'text' elements." +
                            "See javadoc for SVG snippet format");
        }
    }
    
    private SVGLocatableElement myMarkElement;
    private SVGLocatableElement myTextElement;
    private       boolean       isSelected;
}
