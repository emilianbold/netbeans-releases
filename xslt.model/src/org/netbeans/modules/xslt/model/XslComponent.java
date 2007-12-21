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

import org.netbeans.modules.xml.xam.dom.DocumentComponent;


/**
 * @author ads
 *
 */
public interface XslComponent extends DocumentComponent<XslComponent> {

    String XSL_NAMESPACE = "http://www.w3.org/1999/XSL/Transform";

    /**
     * @return the xslt model this component belongs to.
     */
    XslModel getModel();
    
    /**
     * @return the type of the component in terms of the xslt model interfaces.
     */
    Class<? extends XslComponent> getComponentType();
    
    /**
     * @return true if the elements are from the same xsl model.
     */
    boolean fromSameModel(XslComponent other);
    
    /**
     * @param visitor apply <code>visitor</code> to this component
     */
    void accept( XslVisitor visitor );
    
    /**
     * Creates a reference to the given target Xsl component.
     * @param referenced the xsl component being referenced.
     * @param type actual type of the target
     * @return the reference.
     */
    <T extends ReferenceableXslComponent> XslReference<T> createReferenceTo(
            T referenced, Class<T> type);
}
