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
package org.netbeans.modules.xml.api;

import java.io.*;
import javax.swing.text.*;

import org.netbeans.modules.xml.core.lib.EncodingHelper;
/**
 * XML uses inband encoding detection - this class obtains it.
 *
 * @author  Petr Jiricka
 * @version 1.0
 */
public class EncodingUtil {

    /** Detect input stream encoding.
    * The stream stays intact.
    * @return java encoding names ("UTF8", "ASCII", etc.) or null
    * if the stream is not markable or enoding cannot be detected.
    */
    public static String detectEncoding(InputStream in) throws IOException {
        return EncodingHelper.detectEncoding(in);
    }
        
    
    /** Document itself is encoded as Unicode, but in
    * the document prolog is an encoding attribute.
    * @return java encoding names ("UTF8", "ASCII", etc.) or null if no guess
    */
    public static String detectEncoding(Document doc) throws IOException {
        return EncodingHelper.detectEncoding(doc);
    }

}
