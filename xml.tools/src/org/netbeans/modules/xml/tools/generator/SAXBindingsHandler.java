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
package org.netbeans.modules.xml.tools.generator;

import org.xml.sax.*;

public interface SAXBindingsHandler {

    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_parslet(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_SAX_bindings(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     */
    public void end_SAX_bindings() throws SAXException;
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_attbind(final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_bind(final Attributes meta) throws SAXException;
    
    /**
     * A container element end event handling method.
     */
    public void end_bind() throws SAXException;
    
}
