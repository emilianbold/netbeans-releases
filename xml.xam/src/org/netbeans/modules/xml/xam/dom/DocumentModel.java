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

import org.netbeans.modules.xml.xam.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Interface describing an abstract model. The model is based on a
 * document representation that represents the persistent form.
 * @author Chris Webster
 * @author Nam Nguyen
 * @author Rico Cruz
 */
public interface DocumentModel<C extends DocumentComponent<C>> extends Model<C>{
    
    /**
     * @return the DOM Document node.
     */
    Document getDocument();
    
    /**
     * Returns model root component.
     */
    C getRootComponent();
    
    /**
     * @return true if two DOM nodes have same identity.
     */
    public boolean areSameNodes(Node n1, Node n2);
    
    /**
     * Return XPath expression for the given component.
     */
    String getXPathExpression(DocumentComponent component);
    
    /**
     * Create component to be added as child of given component.
     */
    C createComponent(C parent, Element element);
    
    /**
     * Find component given a position into the Swing document.
     * @return component if found.
     */
    DocumentComponent findComponent(int position);
}
