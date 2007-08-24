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

package org.netbeans.modules.web.jsf.api.facesmodel;

/**
 * The "supported-locale" element allows authors to declare
 * which locales are supported in this application instance.
 * @author Petr Pisl
 */
public interface SupportedLocale extends JSFConfigComponent {

    /**
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @return value of the supported locale
     */
    String getLocale();

    /**
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param locale new value for the supported locale
     */
    void setLocale(String locale);
}