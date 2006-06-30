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

package org.netbeans.modules.project.libraries;

import org.xml.sax.*;

public interface LibraryDeclarationHandler {

    /**
     * A container element start event handling method.
     * @param meta attributes
     *
     */
    public void start_volume(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     */
    public void end_volume() throws SAXException;

    /**
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     *
     */
    public void handle_type(final java.lang.String data, final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     *
     */
    public void handle_description(final java.lang.String data, final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     *
     */
    public void start_library(final Attributes meta) throws SAXException;
    
    /**
     * A container element end event handling method.
     *
     */
    public void end_library() throws SAXException;
    
    /**
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     *
     */
    public void handle_resource(final java.net.URL data, final Attributes meta) throws SAXException;

    /**
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     *
     */
    public void handle_name(final java.lang.String data, final Attributes meta) throws SAXException;

    public void handle_localizingBundle (final String data, final Attributes meta) throws SAXException;

}

