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
package com.sun.rave.propertyeditors.domains;

import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Domain of all languages supported by the JVM. The element's value is the
 * ISO language code, e.g. <code>en</code>. The element's label is the localized
 * language display name followed by the code in parentheses, e.g. "English (en)".
 *
 */
public class LanguagesDomain extends Domain {

    private static Element[] elements;

    static {
        Locale[] locales = Locale.getAvailableLocales();
        SortedMap map = new TreeMap();
        for (int i = 0; i < locales.length; i++) {
            map.put(locales[i].getDisplayLanguage(), locales[i] );
        }
        elements = new Element[map.size()];
        Iterator iter = map.keySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Locale l = (Locale)map.get(iter.next());
            String c = l.getLanguage();
            elements[i++] = new Element(c, l.getDisplayLanguage() + " (" + c + ")");
        }
    }

    public Element[] getElements() {
        return LanguagesDomain.elements;
    }

    public String getPropertyHelpId() {
        return "projrave_ui_elements_propeditors_lang_domain_prop_ed";
    }

    public String getDisplayName() {
        return bundle.getMessage("Languages.displayName");
    }

}
