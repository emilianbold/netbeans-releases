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
package org.netbeans.microedition.svg.meta;

import java.util.Hashtable;

import org.netbeans.microedition.svg.meta.ChildrenAcceptor.Visitor;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;


/**
 * This class represents meta information 
 * for SVG component.
 * This information consist of pair:
 * <code>key</code>, <code>value</code>. 
 * @author ads
 *
 */
public class MetaData extends Hashtable {
    
    private static final long serialVersionUID = 526844381412432697L;
    
    public static final String METADATA         = "text";           // NOI18N
    public static final String TRAIT_TEXT       = "#text";          // NOI18N
    public static final String DISPLAY          = "display";        // NOI18N
    public static final String NONE             = "none";           // NOI18N
    
    private static final String EQ              = "=";              // NOI18N
    

    public void loadFromElement( SVGElement element ){
        clear();
        myNestedElement = null;
        
        MetaVisitor visitor = new MetaVisitor();
        ChildrenAcceptor acceptor = new ChildrenAcceptor( visitor );
        acceptor.accept( element );
    }
    
    /**
     * In some cases ( like "rect" and "circle" ) one needs to wrap them
     * in "g" element for metadata usage . This method helps to avoid 
     * multiple navigation inside element. 
     * In described cases this method return exactly needed element
     * because it single nested not "metadata" element. 
     * @return last nested not "metadata" element
     */
    public SVGElement getNestedElement(){
        return myNestedElement;
    }
    
    private class MetaVisitor implements Visitor {

        public boolean visit( Element element ) {
            String name = element.getLocalName();
            if ( !( element instanceof SVGElement )){
                return true;
            }
            
            SVGElement svgElement = (SVGElement) element;
            
            if ( !METADATA.equals( name )){                
                myNestedElement = svgElement ;
                return true;
            }
            
            String display = svgElement.getTrait( DISPLAY );
            if ( !NONE.equals( display )){
                myNestedElement = svgElement;
                return true;
            }
            
            String content = svgElement.getTrait(TRAIT_TEXT);
            if (content == null) {
                return true;
            }
            content = content.trim();
            int indx = content.indexOf(EQ);
            if (indx == -1) {
                put(content, null);
            }
            else {
                String value = content.substring(0, indx);
                if ( indx == content.length() -1 ){
                    put( value , "" );
                }
                else {
                    put( value , content.substring( indx +1 ));
                }
            }
            return true;
        }
        
    }
    
    /**
     * This element is first not "metadata" element.
     */
    private SVGElement myNestedElement;

}
