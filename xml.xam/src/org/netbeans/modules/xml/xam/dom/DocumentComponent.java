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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.dom;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.xam.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A component in model.
 * 
 */
public interface DocumentComponent<C extends DocumentComponent> extends Component<C> {
    public static final String TEXT_CONTENT_PROPERTY = "textContent";

    /**
     * Returns the DOM element corresponding to this component.
     */
    Element getPeer();

    /**
     * @return string value of the given attribute.
     */
    String getAttribute(Attribute attribute);
    
    /**
     * Sets the attribute value.
     * @param eventPropertyName name property change event to fire.
     * @param attribute the attribute to set value for.
     * @param value for the attribute.
     */
    void setAttribute(String eventPropertyName, Attribute attribute, Object value);
    
    /**
     * Returns true if the component is part of the document model.
     */
    boolean isInDocumentModel();
    
    /**
     * Returns the position of this component in the schema document,
     * expressed as an offset from the start of the document.
     * @return the position of this component in the document
     */
    int findPosition();

    /**
     * Returns true if the node referenced by this component is n.
     */
    boolean referencesSameNode(Node n);

    /**
     * Returns child component backed by given element node.
     */
    public C findChildComponent(Element e);

    /**
     * Returns position of the attribute by the given name, or -1 if not found.
     */
    public int findAttributePosition(String attributeName);
}
