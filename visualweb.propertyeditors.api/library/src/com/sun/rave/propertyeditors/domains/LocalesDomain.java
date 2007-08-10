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

import java.util.Arrays;
import java.util.Locale;

/**
 * Editable domain of all locales supported by the JVM. The element's value is the
 * locale string, e.g. <code>en_US</code>. The element's label is the locale
 * display name followed by the locale code in parentheses, e.g.
 * "US English (en_US)".
 *
 */
public class LocalesDomain extends Domain {

    private static Element[] elements;

    static {
        Locale[] locales = Locale.getAvailableLocales();
        elements = new Element[locales.length];
        for (int i = 0; i < locales.length; i++) {
            String c = locales[i].toString();            
            elements[i] = new LocaleElement(locales[i], locales[i].getDisplayName() + " (" + c + ")");                        
        }
        Arrays.sort(elements);
    }

    public LocalesDomain() {        
    }

    public Element[] getElements() {
        return LocalesDomain.elements;
    }

    public String getDisplayName() {
        return bundle.getMessage("Locales.displayName");
    } 
    
    static class LocaleElement extends Element {                
        String language;
        String country;
        String variant;
        
        LocaleElement(Locale locale, String displayName) {           
            super(locale, displayName);
            this.language = locale.getLanguage();
            this.country = locale.getCountry();
            this.variant = locale.getVariant();
        }

        public String getJavaInitializationString() {         
            return "new java.util.Locale(\"" + language + "\", \"" +
                    country + "\", \"" + variant + "\")";
        }
    }
}
