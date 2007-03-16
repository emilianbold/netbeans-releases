/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.xml;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Drive the writing of an XML document.
 * <p>
 * While one can implement the {@link XMLEncoder} interface directly,
 * the recommended practice
 * is to define one or more specialized <code>XMLEncoder</code>s for the
 * expected top-level elements and delegate to their {@link XMLEncoder#encode}.
 */

abstract public class XMLDocWriter implements XMLEncoder {

    private int indentChars = 2;
    private XMLEncoderStream encoderStream;

    public XMLDocWriter() {
    }


    /**
     * Set number of spaces to be used for each indent level.
     */

    public void setIndentChars(int indentChars) {
	this.indentChars = indentChars;
    } 


    /**
     * Return the XML encoding string.
     * <p>
     * The typical value is "UTF-8".
     * <br>
     * The default implementation handles US/Chinese/Japanese.
     */

    protected String encoding() {
	String lang = System.getenv("LANG");	// NOI18N
	String encoding = "UTF-8";		// NOI18N
	if (lang != null) {
	    if (lang.equals("zh") ||		// NOI18N
		lang.equals("zh.GBK") ||	// NOI18N
		lang.equals("zh_CN.EUC") ||	// NOI18N
		lang.equals("zh_CN.GB18030") ||	// NOI18N
		lang.equals("zh_CN") ||		// NOI18N
		lang.equals("zh_CN.GBK")) {	// NOI18N

		encoding = "EUC-JP";		// NOI18N

	    } else if (lang.equals("ja") ||	// NOI18N
		       lang.equals("ja_JP.eucJP")) { // NOI18N

		encoding = "EUC-JP";		// NOI18N
	    } else {
		encoding = "UTF-8";		// NOI18N
	    }
	}
	return encoding;
    } 

    /**
     * Put out
     *	<?xml version="1.0" encoding="UTF-8"?>
     * (Or the correct encoding)
     */

    private void writeHeader() {
	String version = "1.0";		// NOI18N
	encoderStream.println
	("<?xml version=\"" + version + "\" encoding=\"" + encoding() + "\"?>"); // NOI18N
    } 

    /**
     * Put out 
     *	<!DOCTYPE ... >
     * LATER though ...
     */

    private void writeDoctype() {
    } 

    private void writeTail() {
    } 


    /**
     * Drive the writing of an XML document.
     * <p>
     * Will put the following to the stream:
     * <pre>
     &lt;?xml version="1.0" encoding="&lt;the-appropriate-encoding&gt;"?&gt;
     call {@link #encode}
     * </pre>
     * <p>
     * <i>the-appropriate-encoding</i> is whatever is returned by 
     * {@link #encoding}.
     */
    public void write(OutputStream os) throws IOException {
	try {
	    encoderStream = new XMLEncoderStream(os, indentChars);
	    writeHeader();
	    writeDoctype();
	    encode(encoderStream);
	} finally {
	    writeTail();
	    encoderStream.close();
	} 
    }
}
