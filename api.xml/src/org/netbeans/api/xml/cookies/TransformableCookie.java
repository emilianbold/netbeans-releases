/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.xml.cookies;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import org.openide.nodes.Node;

/**
 * Transform this object by XSL Transformation.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing XML documents.
 *
 * @author Libor Kramolis
 * @deprecated XML tools API candidate
 */
public interface TransformableCookie extends Node.Cookie {
    
    /**
     * Transform this object by XSL Transformation.
     *
     * @param transformSource source of transformation.
     * @param outputResult result of transformation.
     * @param observer optional notifier (<code>null</code> allowed)
     *                 giving judgement details via {@link XMLProcessorMessage}s.
     */
    public void transform (Source transformSource, Result outputResult, CookieObserver observer)
        throws IOException, TransformerConfigurationException, TransformerException;
    
}
