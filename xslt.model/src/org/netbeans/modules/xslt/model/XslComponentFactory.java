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

import org.netbeans.modules.xml.xam.dom.ComponentFactory;


/**
 * Factory for Xsl components.
 * 
 * @author ads
 *
 */
public interface XslComponentFactory extends ComponentFactory<XslComponent> {
    
    /**
     * @return instantiated "apply-templates" component. 
     */
    ApplyTemplates createApplyTemplates();
    
    /**
     * @return instantiated "attribute" component.
     */
    Attribute createAttribute();
    
    /**
     * @return instantiated "attribute-set" component.
     */
    AttributeSet createAttributeSet();
    
    /**
     * @return instantiated "call-template" component.
     */
    CallTemplate createCallTemplate();
    
    /**
     * @return instantiated "choose" component.
     */
    Choose createChoose();
    
    /**
     * @return instantiated "copy" component.
     */
    Copy createCopy();
    
    /**
     * @return instantiated "copy-of"  component.
     */
    CopyOf createCopyOf();
    
    /**
     * @return instantiated "document"  component.
     */
    Document createDocument();
    
    /**
     * @return instantiated "element" component.
     */
    Element createElement();
    
    /**
     * @return instantiated "for-each" component.
     */
    ForEach createForEach();
    
    /**
     * @return instantiated "if" component.
     */
    If createIf();
    
    /**
     * @return instantiated "import" component.
     */
    Import createImport();
    
    /**
     * @return instantiated "include" component.
     */
    Include createInclude();
    
    /**
     * @return instantiated "key" component.
     */
    Key createKey();
    
    /**
     * @return instantiated non-xslt element result compoenent.
     */
    LiteralResultElement createLiteralResultElement( String name , 
            String namespaceUri);
    
    /**
     * @return instantiated "namespace"  component.
     */
    Namespace createNamespace();
    
    /**
     * @return instantiated "number" component.
     */
    Number createNumber();
    
    /**
     * @return instantiated "otherwise" component.
     */
    Otherwise createOtherwise();
    
    /**
     * @return instantiated "output" component.
     */
    Output createOutput();
    
    /**
     * @return instantiated "param" component.
     */
    Param createParam();
    
    /**
     * @return instantiated "sequence"  component.
     */
    Sequence createSequence();
    
    /**
     * @return instantiated "sort" component.
     */
    Sort createSort();
    
    /**
     * @return instantiated "stylesheet" component.
     */
    Stylesheet createStylesheet();
    
    /**
     * @return instantiated "template" component.
     */
    Template createTemplate();
    
    /**
     * @return instantiated "text" component.
     */
    Text createText();
    
    /**
     * @return instantiated "value-of" component.
     */
    ValueOf createValueOf();
    
    /**
     * @return instantiated "variable" component.
     */
    Variable createVariable();
    
    /**
     * @return instantiated "when" component.
     */
    When createWhen();
    
    /**
     * @return instantiated "with-param" component.
     */
    WithParam createWithParam();
}
