/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * The contents of this file are subject to the terms of the Common 
 * Development and Distribution License ("CDDL")(the "License"). You 
 * may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html
 * or mural/license.txt. See the License for the specific language 
 * governing permissions and limitations under the License.  
 *
 * When distributing Covered Code, include this CDDL Header Notice 
 * in each file and include the License file at mural/license.txt.
 * If applicable, add the following below the CDDL Header, with the 
 * fields enclosed by brackets [] replaced by your own identifying 
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.netbeans.modules.etl.project;

import java.util.regex.Pattern;
import net.java.hulp.i18n.LocalizationSupport;

/**
 *
 * @author rtam
 */
public class Localizer extends LocalizationSupport {
    
    private static final String DEFAULT_PATTERN = "([A-Z][A-Z][A-Z][A-Z]\\d\\d\\d)(: )(.*)";

    private static final String DEFAULT_PREFIX = "DM-DI-";
    private static final String DEFAULT_BUNDLENAME = "msgs";
    private static Localizer instance = null; 
    
    public Localizer(Pattern idpattern, String prefix, String bundlename) {
        super(idpattern, prefix, bundlename);
    }
    
    /**  Returns an instance of Localizer.
     * 
     * @return a Localizer instance.
     * 
     */
    public static Localizer get() {
        if (instance == null) {
            Pattern pattern = Pattern.compile(DEFAULT_PATTERN);
            instance = new Localizer(pattern, DEFAULT_PREFIX, DEFAULT_BUNDLENAME);

        }
        return instance;
    } 
    public static String parse(String str) {
        String s = "DM-DI-PRSR001: ";
        if (str.contains(s)) {
            str = str.replace(s, "");
        }
        return str;
    }
    /* public static Localizer get(String prefix, String bundlename) {
       
            Pattern pattern = Pattern.compile(DEFAULT_PATTERN);
           Localizer instanceLocal = new Localizer(pattern, prefix, bundlename);

       
        return instanceLocal;
    } */
}
