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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.openide.xml.XMLUtil;

/**
 * Provides convenience methods to write out XML elements. To be used from
 * within {@link XMLEncoder#encode}.
 * <p>
 * provides the following:
 * <ul>
 * <li> Appropriate indentation of elements.
	Amount of indentation can be configured via
	{@link XMLDocWriter#setIndentChars}
 * <li> Formatting and indentation of attribute's.
 * <li> Escaping of attribute values and content.
 * </ul>
 */

public class XMLEncoderStream {

    private String indentElement = "  ";	// NOI18N
    private int indent;
    private PrintWriter writer;
    private OutputStream os;	// just so we can close it


    XMLEncoderStream(OutputStream os, int indentChars) {
	this.os = os;
	writer = new PrintWriter(os);
	makeIndentElement(indentChars);
    }

    private void makeIndentElement(int indentChars) {
	indentElement = "";		// NOI18N
	if (indentChars > 20)
	    indentChars = 20;
	for (int i = 0; i < indentChars; i++) {
	    indentElement += " ";	// NOI18N
	}
    }


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName version="version" attr1="attr1" ...&gt;
     &lt;tagName attr1="attr1" ...&gt;
     </pre>
     * @param version If >= 0 will automatically cause the output of the
     * version attribute. This goes hand-in-hand with
     * {@link XMLDecoder#checkVersion}
     */

    public void elementOpen(String tagName,
			       int version,
			       AttrValuePair[] attrs) {
	writeIndent();
	writer.print("<" + tagName);		// NOI18N
	writeAttrs(tagName, version, attrs, true);
	writer.println(">");			// NOI18N
	indent++;
    } 


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName attr1="attr1" ...&gt;
     </pre>
     */

    public void elementOpen(String tagName, AttrValuePair[] attrs) {
	elementOpen(tagName, -1, attrs);
    }


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName version="version"&gt;
     </pre>
     * @param version If >= 0 will automatically cause the output of the
     * version attribute. This goes hand-in-hand with
     * {@link XMLDecoder#checkVersion}
     */

    public void elementOpen(String tagName, int version) {
	elementOpen(tagName, version, null);
    } 


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName&gt;
     </pre>
     */

    public void elementOpen(String tagName) {
	elementOpen(tagName, -1, null);
    } 


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;/tagName&gt;
     </pre>
     */

    public void elementClose(String tagName) {
	indent--;
	if (indent < 0)
	    indent = 0;
	writeIndent();
	writer.println("</" + tagName + ">");	// NOI18N
    } 


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName attr1="attr1" .../&gt;
     </pre>
     */

    public void element(String tagName, AttrValuePair[] attrs) {
	element(tagName, -1, attrs, null);
    }


    /**
     * .
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName&gt;content&lt;/tagName&gt;
     </pre>
     */

    public void element(String tagName, String content) {
	element(tagName, -1, null, content);
    }


    /**
     * Basic workhorse.
     * <p.
     * Puts out XML of this form:
     * <br>
     <pre>
     &lt;tagName version="version" attr1="attr1" .../&gt;
     &lt;tagName version="version" attr1="attr1" ...&gt;content&lt;/tagName&gt;
     </pre>
     * @param content If null the first form above is used.
     * @param version If >= 0 will automatically cause the output of the
     * version attribute. This goes hand-in-hand with
     * {@link XMLDecoder#checkVersion}
     */

    public void element(String tagName, int version, AttrValuePair[] attrs,
			String content) {
	writeIndent();
	writer.print("<" + tagName);	// NOI18N
	writeAttrs(tagName, version, attrs, true);
	if (content == null) {
	    writer.print("/>");		// NOI18N
	} else {
	    writer.print(">");		// NOI18N
	}

	if (content != null) {
	    writer.print(escapeContent(content));
	    writer.print("</" + tagName + ">");	// NOI18N
	}
	writer.println();
    }

    void close() throws IOException {
	writer.flush();
	writer.close();
	writer = null;
	os.close();	// SHOULD double-check if it's really our responsibility
    } 

    void println(String s) {
	writer.println(s);
    } 

    private void writeIndent(int additionalIndent) {
	for (int i = 0; i < indent; i++)
	    writer.print(indentElement);
	for (int i = 0; i < additionalIndent; i++)
	    writer.print(' ');
    }

    private void writeIndent() {
	writeIndent(0);
    }

    /**
     * Decide whether we output attributes in one line
     *	&lt;tag a1="A1" a2="A2" a3="A3"&gt;
     * </pre>
     * or "format" them.
     * <pre>
     *	&lt;tag a1="A1"
     *       a2="A2"
     *       a3="A3"&gt;
     * </pre>
     *
     * <p>
     * This decision is quick-and-dirty and will recommend wrapping if the
     * length of the unformatted form exceeds 80.
     * <br>
     * It doesn't take XML escaping into consideration.
     */
    private boolean needFormat(String tagName,
			    int version,
			    AttrValuePair[] attrs,
			    boolean format) {

	if (!format)
	    return false;

	int additionalIndent = 1 + tagName.length() + 1;
	final int max = 80;
	int width = additionalIndent;

	if (version >= 0) {
	    width += " version=\"\"".length();	// NOI18N
	    width += ("" + version).length();	// NOI18N
	}

	if (width > max)
	    return true;

	if (attrs != null) {
	    for (int ax = 0; ax < attrs.length; ax++) {
		AttrValuePair avp = attrs[ax];

		width += " ".length();	// NOI18N
		width += avp.getAttr().length();
		width += "=\"".length();	// NOI18N
		width += avp.getValue().length();
		width += "\"".length();	// NOI18N
		if (width > max)
		    return true;
	    }
	}
	return false;
    }

    private void writeAttrs(String tagName,
			    int version,
			    AttrValuePair[] attrs,
			    boolean format) {

	int awritten = 0;

	// call print on each individual chunk separately because it's
	// more efficient than concatenating Strings like this:
	// writer.print(" version=\"" + version + "\"");

	format = needFormat(tagName, version, attrs, format);

	if (version != -1) {
	    writer.print(" version=\"");	// NOI18N
	    writer.print(version);
	    writer.print("\"");			// NOI18N
	    awritten++;
	}

	if (attrs != null) {
	    int additionalIndent = 1 + tagName.length() + 1;
	    for (int ax = 0; ax < attrs.length; ax++) {
		AttrValuePair avp = attrs[ax];

		if (awritten >= 1 && format) {
		    writer.println();
		    writeIndent(additionalIndent);
		} else {
		    writer.print(" ");		// NOI18N
		}
		writer.print(avp.getAttr());
		writer.print("=\"");		// NOI18N
		writer.print(escapeAttributeValue(avp.getValue()));
		writer.print("\"");		// NOI18N
		awritten++;
	    }
	}
    }


    /**
     * For example
     * "File name is "%s"" becomes "File name is &amp;dq;%s&amp;dq;".
     */

    public static String escapeAttributeValue(String value) {
	if (value == null) {
	    return "";		// NOI18N
	} else {
	    String escapedValue = value;
	    try {
		escapedValue = XMLUtil.toAttributeValue(value);
	    } catch (java.io.CharConversionException e) {
		;
	    }
	    return escapedValue;
	}
    }

    /**
     * For example "File name is &lt;%s&gt;" becomes
     * "File name is &amp;lt;%s&amp;gt;".
     */
    public static String escapeContent(String content) {
	if (content == null) {
	    return "";		// NOI18N
	} else {
	    String escapedContent = content;
	    try {
		escapedContent = XMLUtil.toElementContent(content);
	    } catch (java.io.CharConversionException e) {
		;
	    }
	    return escapedContent;
	}
    }
}
