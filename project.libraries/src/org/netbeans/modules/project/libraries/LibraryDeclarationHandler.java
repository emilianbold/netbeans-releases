/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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

