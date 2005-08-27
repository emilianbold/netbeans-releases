/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.api.model;

import org.xml.sax.InputSource;
import org.netbeans.modules.xml.dtd.grammar.DTDParser;

/**
 * @author  Petr Jiricka
 */
public final class DTDUtil {
    
    /** Creates new DTDParser 
     * @param dtdOnly If true the InputSource parameter into the parse method
     *                should be a DTD document, otherwise it should be an XML
     *                document.
     * @param inputSource InputSource from which the DTD should be parsed.
     * @return GrammarQuery for this DTD
     */
    public static GrammarQuery parseDTD(boolean dtdOnly, InputSource inputSource) {
        DTDParser dtdParser = new DTDParser(dtdOnly);
        return dtdParser.parse(inputSource);
    }

        
}
