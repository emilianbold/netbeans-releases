/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xslt.model;


/**
 * This interface represent elements that are children  
 * for SequenceConstructor.
 * 
 * This is common type for such elements. Each child  
 * has more specialized type and should be casted to this type.
 * 
 * Text value ( TEXT DOM Node ) could be associated with 
 * each sequence element. TEXT nodes are not XslComponents.
 * They are considered as property of the immediately preceding element.
 * F.e.
 * <p/>
 * <pre>
 * &lt;child/>
 * Text
 * </pre>
 * <p/>
 * In this case "Text" is the property of "child" Xsl component.
 *   
 * @author ads
 *
 */
public interface SequenceElement extends XslComponent {
    
    
    String TRAILING_TEXT = "trailing_text";         // NOI18N

    
    /**
     * @return text value of following TEXT node. 
     */
    String getTrailingText();
    
    /**
     * Sets following text value.
     * @param text value for substituting ( or inserting ) following sibling TEXT node. 
     */
    void setTrailingText( String text );
}
