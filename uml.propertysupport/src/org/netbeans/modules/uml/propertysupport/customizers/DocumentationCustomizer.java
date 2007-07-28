/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.documentation.ui.DocumentationPane;
import org.netbeans.modules.uml.propertysupport.nodes.CustomPropertyEditor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
 *
 * @author Sheryl
 */
public class DocumentationCustomizer extends DocumentationPane 
        implements Customizer, EnhancedCustomPropertyEditor
{
    private IPropertyElement element;
    
    public DocumentationCustomizer()
    {
        super(false);
        setPreferredSize(new java.awt.Dimension(300, 350));
        setEnabled(true);
    }
    
    public Object getPropertyValue() throws IllegalStateException
    {
        String doc = getDocumentText();
        element.setValue(doc);
        element.save();
        return doc;
    }
    
    public void setElement(IPropertyElement element,
            IPropertyDefinition def)
    {
        this.element = element;  
        setDocumentText(element.getValue());
    }
    
    public void setPropertySupport(CustomPropertyEditor editor)
    {
    }
}
