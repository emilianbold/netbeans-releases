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
