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



/**
 * Suggested SVG snippet :
 * <pre>
 *  &lt;g id="label" transform="translate(130,200)">
 *   &lt;text display="none">type=label&lt;/text>
 *   &lt;g>
 *        &lt;text id="label_text" x="5" y="5" stroke="black" font-size="15"  font-family="SunSansSemiBold">
 *       Label&lt;/text>
 *       &lt;text display="none">type=text&lt;/text>
 *   &lt;/g>
 *   &lt;/g>
 * </pre>
 * @author ads
 *
 */
public class SVGLabel extends SVGComponent {
    
    private static final String TEXT_SUFFIX = DASH + SVGTextField.TEXT;
    
    public SVGLabel( SVGForm form, String elemId ) {
        super(form, elemId);

        myText = (SVGLocatableElement) getElementById(getElement(),
                getElement().getId() + TEXT_SUFFIX);

        if (myText == null) {
            myText = (SVGLocatableElement) getNestedElementByMeta(getElement(), TYPE,
                    SVGTextField.TEXT);
        }
        
        verify();
    }
    
    public SVGLabel( SVGForm form, SVGLocatableElement element ) {
        super(form, element);
    }
    
    public void setLabelFor( SVGComponent component ){
        setProperty( LABEL_FOR , component );
    }
    
    public SVGComponent getLabelFor(){
        return (SVGComponent)getProperty( LABEL_FOR );
    }
    
    public void setText( String text ){
        if ( myText == null  ){
            throw new IllegalArgumentException("No nested text element found"); // NOI18N
        }
        setTraitSafely( myText , TRAIT_TEXT,  text );
    }
    
    public String getText(){
        if ( myText == null  ){
            return null;
        }
        return myText.getTrait( TRAIT_TEXT );
    }

    public synchronized boolean isFocusable() {
        if ( getLabelFor() == null ){
            return super.isFocusable();
        }
        else {
            return false;
        }
    }
    
    private void verify() {
        if ( myText == null ){
            throw new IllegalArgumentException( "Element with id= "+
                    getElement().getId() +" couldn't be Label element." +
                    		" Cannot find nested 'text' element. See javadoc" +
                    		" for SVG snippet.");
        }
    }
    
    private SVGLocatableElement myText;
}
