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

package org.netbeans.modules.xsl.api;

import org.w3c.dom.Node;
import java.awt.Component;
import org.openide.loaders.DataObject;

/**
 * The <code>XPathCustomizer</code> is an interface to enable editing an XPath in a source document.
 * By implementing the interface any editor can launch the <code>XPathCustomizer</code> when an XPath needs
 * to be customized.
  */
public interface XSLCustomizer {	
    /**
     * Returns the <code>Component</code> used to edit the XPath.
     * @param node the <code>Node</code> instance to be customized.
     * @param dataObject the <code>DataObject</code> representing the XSL document.
	 *            <code>ScenarioCookie</code> can be fetched from this DataObject using
	 *            <code>dataObject.getCookie(ScenarioCookie.class)</code>.
     * @return the <code>Component</code> used to edit the XPath.
     */
    public Component getCustomizer(Node node, DataObject dataObject);

    /**
     * Indicates if this customizer can return a customizing component for this
     * node.
     * @param node the <code>Node</code> instance to be customized.
     * @return true if a customizer is supported for this node, otherwise false.
     */
    public boolean hasCustomizer(Node node);
}
