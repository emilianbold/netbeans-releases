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
package org.netbeans.api.xml.cookies;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import org.openide.nodes.Node;

/**
 * Transform this object by XSL Transformation.
 * <p>
 * It should be gracefully served by all data objects and explorer nodes
 * representing XML documents.
 *
 * @author     Libor Kramolis
 * @see        javax.xml.transform.Transformer
 */
public interface TransformableCookie extends Node.Cookie {
    
    /**
     * Transform this object by XSL Transformation.
     *
     * @param transformSource source of transformation.
     * @param outputResult result of transformation.
     * @param observer optional notifier (<code>null</code> allowed)
     *                 giving judgement details via {@link XMLProcessorDetail}s.
     * @throws TransformerException if an unrecoverable error occurs during the course of the transformation
     */
    public void transform (Source transformSource, Result outputResult, CookieObserver observer) throws TransformerException;
    
}
