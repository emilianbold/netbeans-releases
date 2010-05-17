/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.IOException;
import java.io.Writer;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import com.sun.faces.util.HtmlUtils;

/**
 * PrettyJspWriter provides a pretty-formated JSP writer needed for JSP generation
 *
 * @author Robert Brewin, Carl Quinn
 * @version 1.0
 */
public class PrettyJspWriter extends ResponseWriter {

    String encoding = "ISO-8859-1";

    /**
     * Used to track the current indentation amount
     */
    private int currentIndent = 0;

    /**
     * The indent amount
     */
    private int indentAmount  = 4;

    /**
     * Flag which indicates that we have an open element tag
     */
    private boolean closeStart;

    private boolean isEmptyElement;

    /** True when we shouldn't be escaping output (basically,
     * inside of <script> and <style> elements).
     */
    private boolean dontEscape;

    /**
     * Holds the <code>Writer</code> we will be emitting to
     */
    private ResettableStringWriter writer;

    private char[] buffer = new char[1028];
    private char[] charHolder = new char[1];

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct the PrettyJspWriter
     */
    public PrettyJspWriter() {
    }

    /**
     * Construct the PrettyJspWriter
     */
    public PrettyJspWriter(ResettableStringWriter outputWriter) {
        this.writer = outputWriter;
    }

    /**
     * Set the output writer
     *
     * @param outputWriter -- the new <code>Writer</code> to use for emission
     */
    public void setOutputWriter(ResettableStringWriter outputWriter) {
        this.writer = outputWriter;
    }

    //---------------------------------------------------------------------------------- Indentation

    /**
     * Get the current indentation amount
     *
     * @return the indent amount
     */
    public int getCurrentIndent() {
        return currentIndent;
    }

    /**
     * Set the amount to indent
     *
     * @param indentAmount
     */
    public void setIndentAmount(int indentAmount) {
        this.indentAmount = indentAmount;
    }

    /**
     * Increment the current indent
     */
    public void indent() {
        currentIndent += indentAmount;
    }

    /**
     * Decrement the current indent
     */
    public void outdent() {
        currentIndent -= indentAmount;
        if (currentIndent < 0)
            currentIndent = 0;
    }

    /**
     * Indent within the current stream
     *
     * @throws IOException
     */
    private void writeIndent() throws IOException {
        for (int i = 0; i < currentIndent; i++)
            writer.write(' ');
    }

    //--------------------------------------------------------------------------------- zzz

    /**
     * @return the content type, such as "text/html" for this
     * ResponseWriter.
     *
     */
    public String getContentType() {
        return "text/html";
    }

    /**
     * @return the character encoding, such as "ISO-8859-1" for this
     * ResponseWriter.  Please see <a
     * href="http://www.iana.org/assignments/character-sets">the
     * IANA</a> for a list of character encodings.
     *
     */
    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * <p>Write whatever text should begin a response.</p>
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void startDocument() throws IOException {
        currentIndent = 0;
    }

    /**
     * <p>Write whatever text should end a response.  If there is an open
     * element that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void endDocument() throws IOException {
        //!CQ could error-check indent here...
        currentIndent = 0;
        writer.flush();
    }

    /**
     * Flush the stream.  If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination.  Then, if that destination is another character or
     * byte stream, flush it.  Thus one flush() invocation will flush all the
     * buffers in a chain of Writers and OutputStreams.
     *
     * @exception  java.io.IOException  If an I/O error occurs
     */
    public void flush() throws IOException {
        closeStartIfNecessary();
        writer.flush();
    }

    /**
     * <p>Write the start of an element, up to and including the
     * element name.  Once this method has been called, clients can
     * call <code>writeAttribute()</code> or <code>writeURIAttribute()</code>
     * method to add attributes and corresponding values.  The starting
     * element will be closed (that is, the trailing '>' character added)
     * on any subsequent call to <code>startElement()</code>,
     * <code>writeComment()</code>,
     * <code>writeText()</code>, <code>endElement()</code>, or
     * <code>endDocument()</code>.</p>
     *
     * @param name Name of the element to be started
     *
     * @param componentForElement May be <code>null</code>.  If
     * non-<code>null</code>, must be the UIComponent instance to which
     * this element corresponds.
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>name</code>
     *  is <code>null</code>
     */
    public void startElement(String name, UIComponent componentForElement) throws IOException {
        
        char firstChar = name.charAt(0);
        if (firstChar == 's' || firstChar == 'S' && 
                (name.equalsIgnoreCase("script") || name.equalsIgnoreCase("style")))
                dontEscape = true;

        closeStartIfNecessary();
        writeIndent();
        writer.write("<" + name);
        if (componentForElement != null) {
            ValueBinding vb = componentForElement.getValueBinding("binding");
            if (vb != null)
                writeAttribute("design-id", vb.getExpressionString(), null);
        }
        closeStart = true;
        isEmptyElement = true;  // empty until proven otherwise
        indent();
    }
    
    /**
     * This method automatically closes a previous element (if not
     * already closed).
     */
    private void closeStartIfNecessary() throws IOException {
        if (closeStart) {
            writer.write(">\n");
            closeStart = false;
        }
    }

    /**
     * <p>Write the end of an element, after closing any open element
     * created by a call to <code>startElement()</code>.
     *
     * @param name Name of the element to be ended
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception java.lang.NullPointerException if <code>name</code>
     *  is <code>null</code>
     */
    public void endElement(String name) throws IOException {
        // always turn escaping back on once an element ends
        dontEscape = false;

        // See if we need to close the start of the last element
        if (closeStart) {
            //boolean isEmptyElement = HtmlUtils.isEmptyElement(name);
            if (isEmptyElement) {
                writer.write(" />");
                closeStart = false;
                outdent();
                return;
            }

            writer.write(">\n");
            closeStart = false;
        }

        outdent();

        writeIndent();
        writer.write("</");
        writer.write(name);
        writer.write(">\n");
    }

    /**
     * <p>Force the closing of the start tag currently being
     * written, if it has not already been closed.</p>
     *
     * @param component the {@link UIComponent} (if any) to which this
     * tag corresponds.
     *
     * @exception IOException if an input/output error occurs
     *
    public void closeStartTag(UIComponent component) throws IOException {
        if (closeStart) {
            writer.write(">\n");
            closeStart = false;
        }
    }*/

    /**
     * <p>Write an attribute name and corresponding value (after converting
     * that text to a String if necessary), after escaping it properly.
     * This method may only be called after a call to
     * <code>startElement()</code>, and before the opened element has been
     * closed.</p>
     *
     * @param name Attribute name to be added
     *
     * @param value Attribute value to be added
     *
     * @param componentPropertyName May be <code>null</code>.  If
     * non-<code>null</code>, this must be the name of the property on
     * the {@link UIComponent} passed in to a previous call to {@link
     * #startElement} to which this attribute corresponds.
     *
     * @exception IllegalStateException if this method is called when there
     *  is no currently open element
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>name</code> is
     * <code>null</code>
     */
    public void writeAttribute(String name, Object value, String componentPropertyName) throws IOException {

        writer.write(" ");
        writer.write(name);
        writer.write("=\"");
        
        // write the attribute value
        HtmlUtils.writeAttribute(writer, buffer, value.toString());

        writer.write("\"");
    }

    /**
     * <p>Write a URI attribute name and corresponding value (after converting
     * that text to a String if necessary), after encoding it properly
     * (for example, '%' encoded for HTML).
     * This method may only be called after a call to
     * <code>startElement()</code>, and before the opened element has been
     * closed.</p>
     *
     * @param name Attribute name to be added
     *
     * @param value Attribute value to be added
     *
     * @param componentPropertyName May be <code>null</code>.  If
     * non-<code>null</code>, this must be the name of the property on
     * the {@link UIComponent} passed in to a previous call to {@link
     * #startElement} to which this attribute corresponds.
     *
     * @exception IllegalStateException if this method is called when there
     *  is no currently open element
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>name</code> is
     * <code>null</code>
     */
    public void writeURIAttribute(String name, Object value, String componentPropertyName)
        throws IOException {
        //!CQ TODO: could throw exception or similar check if elementOpen is false

        writer.write(" ");
        writer.write(name);
        writer.write("=\"");

        String stringValue = value.toString();

        // Javascript URLs should not be URL-encoded
        if (stringValue.startsWith("javascript:")) {
            HtmlUtils.writeAttribute(writer, buffer, stringValue);
        } else {
            HtmlUtils.writeURL(writer, stringValue, encoding, null);
        }

        writer.write("\"");
    }

    /**
     * <p>Write a comment containing the specified text, after converting
     * that text to a String if necessary.  If there is an open element
     * that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * @param comment Text content of the comment
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception java.lang.NullPointerException if <code>comment</code>
     *  is <code>null</code>
     */
    public void writeComment(Object comment) throws IOException {
        closeStartIfNecessary();
        writer.write("<!-- ");
        writer.write(comment.toString());
        writer.write(" -->");
    }

    /**
     * <p>Write an object (after converting it to a String, if necessary),
     * after escaping it properly.  If there is an open element
     * that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * <p>All angle bracket occurrences in the argument must be escaped
     * using the &amp;gt; &amp;lt; syntax.</p>
     *
     * @param text Text to be written
     *
     * @param componentPropertyName May be <code>null</code>.  If
     * non-<code>null</code>, this is the name of the property in the
     * associated component to which this piece of text applies.
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>text</code>
     *  is <code>null</code>
     */
    public void writeText(Object text, String componentPropertyName) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writeIndent();
        if (dontEscape) {
            writer.write(text.toString());
        } else {
            HtmlUtils.writeText(writer, buffer, text.toString());
        }
    }

    /**
     * <p>Write a single character, after escaping it properly.  If there
     * is an open element that has been created by a call to
     * <code>startElement()</code>, that element will be closed first.</p>
     *
     * @param text Text to be written
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void writeText(char text) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writeIndent();
        if (dontEscape) {
            writer.write(text);
        } else {
            charHolder[0] = text;
            HtmlUtils.writeText(writer, buffer, charHolder);
        }
    }

    /**
     * <p>Write text from a character array, after escaping it properly
     * for this method.  If there is an open element that has been
     * created by a call to <code>startElement()</code>, that element
     * will be closed first.</p>
     *
     * @param text Text to be written
     * @param off Starting offset (zero-relative)
     * @param len Number of characters to be written
     *
     * @exception java.lang.IndexOutOfBoundsException if the calculated starting or
     *  ending position is outside the bounds of the character array
     * @exception java.io.IOException if an input/output error occurs
     * @exception java.lang.NullPointerException if <code>text</code>
     *  is <code>null</code>
     */
    public void writeText(char[] text, int off, int len) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writeIndent();
        if (dontEscape) {
            writer.write(text);
        } else {
            HtmlUtils.writeText(writer, buffer, text);
        }
    }

    /**
     * Creates a new instance of this ResponseWriter, using a different Writer.
     */
    public ResponseWriter cloneWithWriter(Writer writer) {
        // How do we handle this? We need the writer to be resettable!
        throw new RuntimeException("cloneWithWriter not supported by the Rave container!");
        //return new PrettyJspWriter(writer);
    }

    //--------------------------------------------------------------------------------------- Writer

    /**
     * Close the stream, flushing it first. Once a stream has been closed, further write() or
     * flush() invocations will cause an IOException to be thrown. Closing a previously-closed
     * stream, however, has no effect.
     * 
     * @exception java.io.IOException If an I/O error occurs
     */
    public void close() throws IOException {
        closeStartIfNecessary();
        writer.close();
    }

    public void write(char cbuf) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writer.write(cbuf);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writer.write(cbuf, off, len);
    }

    public void write(int c) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writer.write(c);
    }

    public void write(String str) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writer.write(str);
    }

    public void write(String str, int off, int len) throws IOException {
        isEmptyElement = false;
        closeStartIfNecessary();
        writer.write(str, off, len);
    }

    /*
     * Returns the index of the next character to be written to the stream, or put another way, the
     * number of characters since the writer was created.
     * 
     * @returns character count (not including reset content) since writer was created.
     */
    public int getPosition() {
        return writer.getPosition();
    }

    /**
     * Truncates the written content back to a particular position/index specified. <b>Note</b>:
     * Should only be used to reset back to a position outside of a tag, since it will clear the
     * "closeStart" flag.
     * 
     * @param position The position to jump back to.
     */
    public void reset(int position) {
        writer.reset(position);
        closeStart = false;
    }
}
