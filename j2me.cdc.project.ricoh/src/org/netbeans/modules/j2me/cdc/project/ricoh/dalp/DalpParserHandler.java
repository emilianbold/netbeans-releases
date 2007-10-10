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

package org.netbeans.modules.j2me.cdc.project.ricoh.dalp;

import org.xml.sax.*;

public interface DalpParserHandler {

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_application_ver(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_jar(final Attributes meta) throws SAXException;

    /**
     * 
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_dsdk(final Attributes meta) throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_vendor(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_install(final Attributes meta) throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_encode_file(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_dalp(final Attributes meta) throws SAXException;

    /**
     * 
     * A container element end event handling method.
     */
    public void end_dalp() throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_title(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_resources(final Attributes meta) throws SAXException;

    /**
     * 
     * A container element end event handling method.
     */
    public void end_resources() throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_telephone(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_offline_allowed(final Attributes meta) throws SAXException;

    /**
     * 
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_application_desc(final Attributes meta) throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_fax(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_all_permissions(final Attributes meta) throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_e_mail(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_product_id(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_information(final Attributes meta) throws SAXException;

    /**
     * 
     * A container element end event handling method.
     */
    public void end_information() throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_icon(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * A data element event handling method.
     * @param data value or null 
     * @param meta attributes
     */
    public void handle_description(final java.lang.String data, final Attributes meta) throws SAXException;

    /**
     * 
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_security(final Attributes meta) throws SAXException;

    /**
     * 
     * A container element end event handling method.
     */
    public void end_security() throws SAXException;

    public void handle_argument(final java.lang.String data, Attributes attrs) throws SAXException;
}
