/*
 * Copyright 2002 by Dimon-hugbunadarhus ehf.,
 * Laugavegur 178, 105 Reykjavik, ICELAND
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Dimon-hugbunadarhus ehf. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Dimon.
 *
 * File information:
 * REVIEW: DRAFT
 * $Id$
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
