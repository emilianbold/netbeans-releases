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

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "locale-config" element allows the app developer to
 * declare the supported locales for this application.
 *
 * @author Petr Pisl
 */
public interface LocaleConfig extends JSFConfigComponent {

    /**
     * Property name of &lt;default-locale&gt; element.
     */
    public static final String DEFAULT_LOCALE = JSFConfigQNames.DEFAULT_LOCALE.getLocalName();
    /**
     * Property name of &lt;supported-locale&gt; element.
     */
    public static final String SUPPORTED_LOCALE = JSFConfigQNames.SUPPORTED_LOCALE.getLocalName();

    /**
     * The "default-locale" element declares the default locale
     * for this application instance.
     *
     * @return the default locale
     */
    DefaultLocale getDefaultLocale();

    /**
     * The "default-locale" element declares the default locale
     * for this application instance.
     *
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param locale the default locale
     */
    void setDefaultLocale(DefaultLocale locale);

    /**
     * The "supported-locale" element allows authors to declare
     * which locales are supported in this application instance.
     *
     * @return a list of supported locales
     */
    List<SupportedLocale> getSupportedLocales();

    /**
     * The "supported-locale" element allows authors to declare
     * which locales are supported in this application instance.
     *
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param locale the supported locale
     */
    void addSupportedLocales(SupportedLocale locale);

    /**
     * The "supported-locale" element allows authors to declare
     * which locales are supported in this application instance.
     *
     * It must be specified as :language:[_:country:[_:variant:]]
     * without the colons, for example "ja_JP_SJIS".  The
     * separators between the segments may be '-' or '_'.
     * @param index where will be putted
     * @param locale the supported locale
     */
    void addSupportedLocales(int index, SupportedLocale locale);
}