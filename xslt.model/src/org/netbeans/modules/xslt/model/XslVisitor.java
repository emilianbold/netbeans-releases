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
 * @author ads
 *
 */
public interface XslVisitor {
    
    /**
     * Visit "apply-templates" element.
     * @param applyTemplates visited element 
     */
    void visit( ApplyTemplates applyTemplates );
    
    /**
     * Visit "attribute" element.
     * @param attribute visited element
     */
    void visit( Attribute attribute);
    
    /**
     * Visit "attribute-set" element.
     * @param attributeSet visited element
     */
    void visit( AttributeSet attributeSet );
    

    /**
     * Visit "call-template" element.
     * @param callTemplate visited element
     */
    void visit( CallTemplate callTemplate );
    
    /**
     * Visit "choose" element.
     * @param choose visited element
     */
    void visit( Choose choose );
    
    /**
     * Visit "copy" element.
     * @param copy visited element
     */
    void visit( Copy copy ); 
            
    /**
     * Visit "copy-of" element.
     * @param copyOf visited element
     */
    void visit( CopyOf copyOf );
    
    /**
     * Visit "document" element.
     * @param document visited element
     */
    void visit( Document document );
    
    /**
     * Visit "element" element.
     * @param element visited element
     */
    void visit( Element element );
    
    /**
     * Visit "for-each" element.
     * @param forEach visited element
     */
    void visit( ForEach forEach );
    
    /**
     * Visit "if" element.
     * @param iff visited element
     */
    void visit( If iff );
    
    /**
     * Visit "import" element.
     * @param impt visited element
     */
    void visit( Import impt );
    
    /**
     * Visit "include" element.
     * @param include visited element
     */
    void visit( Include include );
    
    /**
     * Visit "key" element.
     * @param key visited element
     */
    void visit( Key key );

    /**
     * Visit not xslt result element.
     * @param element visited element
     */
    void visit( LiteralResultElement element );
    
    /**
     * Visit "namespace" element.
     * @param namespace visited element
     */
    void visit( Namespace namespace );
    
    /**
     * Visit "number" element.
     * @param number visited element
     */
    void visit( Number number ); 
            
    /**
     * Visit "otherwise" element.
     * @param otherwise visited element
     */
    void visit( Otherwise otherwise );
    
    /**
     * Visit "output" element.
     * @param output visited element
     */
    void visit( Output output );
    
    /**
     * Visit "param" element.
     * @param param visited element
     */
    void visit( Param param );
    
    /**
     * Visit "sequence" element.
     * @param sequence visited element
     */
    void visit( Sequence sequence );
    
    /**
     * Visit "sort" element.
     * @param sort visited element
     */
    void visit( Sort sort );
    
    /**
     * Visit "stylesheet" element.
     * @param stylesheet visited element
     */
    void visit( Stylesheet stylesheet );
    
    /**
     * Visit "template" element.
     * @param template visited element
     */
    void visit( Template template );

    /**
     * Visit "text" element.
     * @param text visited element
     */
    void visit( Text text );
    
    /**
     * Visitor "value-of" element.
     * @param valueOf visited element
     */
    void visit( ValueOf valueOf );
    
    /**
     * Visit "variable" element.
     * @param variable visited element
     */
    void visit( Variable variable );
    
    /**
     * Visit "when" element.
     * @param when visited element
     */
    void visit( When when );

    /**
     * Visit "apply-imports" element. 
     * @param impt visited element
     */
    void visit( ApplyImports impt );
    
    void visit(TypeSpec typeSpec);
    void visit(UseAttributesSetsSpec useAttributesSetsSpec);
    void visit(UseCharacterMapsSpec useCharacterMapsSpec);
    void visit(WithParam withParam);
}
