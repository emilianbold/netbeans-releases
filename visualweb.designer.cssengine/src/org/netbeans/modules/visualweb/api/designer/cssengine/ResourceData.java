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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.visualweb.api.designer.cssengine;


import java.net.URL;


/**
 * XXX Dummy interface to mark the style resource data.
 * <p>
 * <b><font color="red"><em>Important note: Do not provide implementation of this interface, use the provider to access it!</em></font></b>
 * </p>
 *
 * @author Peter Zavadsky
 */
public interface ResourceData {

    /** XXX Interface to provide the style resource in form of array of url strings.
     * <p>
     * <b><font color="red"><em>Important note: Do not provide implementation of this interface, use the provider to access it!</em></font></b>
     * </p>  */
    public interface UrlStringsResourceData extends ResourceData {
        public String[] getUrlStrings();
    } // End of UrlStringsResourceData.

    /** XXX Interface to provide the style resource in form of URL and URL string (relative).
     * <p>
     * <b><font color="red"><em>Important note: Do not provide implementation of this interface, use the provider to access it!</em></font></b>
     * </p>  */
    public interface UrlResourceData extends ResourceData {
        public URL getUrl();
        public String getUrlString();
    } // End of UrlResourceData.
}
