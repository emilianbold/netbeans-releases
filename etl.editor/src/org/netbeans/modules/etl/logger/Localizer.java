/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)I18N.java
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */
package org.netbeans.modules.etl.logger;

import java.util.regex.Pattern;

import net.java.hulp.i18n.LocalizationSupport;

/**
 * Internationalization utility for XsltSE.
 * @author Kevan Simpson
 */
public class Localizer extends LocalizationSupport {

    private static final String DEFAULT_PATTERN = "([A-Z][A-Z][A-Z][A-Z]\\d\\d\\d)(: )(.*)";
    private static final String DEFAULT_PREFIX = "DM-DI-";
    private static final String DEFAULT_BUNDLENAME = "msgs";
    private static Localizer instance = null;
    private static final Localizer mI18n = new Localizer();

    public Localizer(Pattern idpattern, String prefix, String bundlename) {
        super(idpattern, prefix, bundlename);
    }

    protected Localizer() {
        super(Pattern.compile("([A-Z][A-Z][A-Z][A-Z]\\d\\d\\d)(: )(.*)", Pattern.DOTALL),
                "DM-DI-", "msgs");
    }

    public static String loc(String message, Object... params) {
        return mI18n.t(message, params);
    }

    public static Localizer get() {
        if (mI18n == null) {
            Pattern pattern = Pattern.compile(DEFAULT_PATTERN);
            instance = new Localizer(pattern, DEFAULT_PREFIX, DEFAULT_BUNDLENAME);
        }
        return mI18n;
    }

    /**
     * Tests the specified string for null-ness and emptiness (zero non-whitespace characters).
     * @param str The test string.
     * @return <code>true</code> if the string is <code>null</code> or zero-length when trimmed.
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() == 0);
    }
}
