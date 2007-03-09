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

package org.netbeans.modules.compapp.casaeditor.model.jbi;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 *
 * @author jqian
 */
public interface JBIComponent extends DocumentComponent<JBIComponent> {
    
    public static final String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement";     // NOI18N
    
    JBIModel getModel();
    
    void accept(JBIVisitor visitor);
        
    void addExtensibilityElement(ExtensibilityElement ee);
    void removeExtensibilityElement(ExtensibilityElement ee);
    List<ExtensibilityElement> getExtensibilityElements();
    
    <T extends ExtensibilityElement> List<T> getExtensibilityElements(Class<T> type);
        
    String getAnyAttribute(QName qname);
    void setAnyAttribute(QName qname, String value);
}
