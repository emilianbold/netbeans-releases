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



package org.netbeans.modules.uml.common;

/**
 * @author KevinM
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IETSystemPrinter
{
	/**
	 * Flush the stream.  This is done by writing any buffered output bytes to
	 * the underlying output stream and then flushing that stream.
	 *
	 * @see        java.io.OutputStream#flush()
	 */
	public void flush();

	/**
	 * Close the stream.  This is done by flushing the stream and then closing
	 * the underlying output stream.
	 *
	 * @see        java.io.OutputStream#close()
	 */
	public void close();
	
	/**
	 * Flush the stream and check its error state.  The internal error state
	 * is set to <code>true</code> when the underlying output stream throws an
	 * <code>IOException</code> other than <code>InterruptedIOException</code>,
	 * and when the <code>setError</code> method is invoked.  If an operation
	 * on the underlying output stream throws an
	 * <code>InterruptedIOException</code>, then the <code>PrintStream</code>
	 * converts the exception back into an interrupt by doing:
	 * <pre>
	 *     Thread.currentThread().interrupt();
	 * </pre>
	 * or the equivalent.
	 *
	 * @return True if and only if this stream has encountered an
	 *         <code>IOException</code> other than
	 *         <code>InterruptedIOException</code>, or the
	 *         <code>setError</code> method has been invoked
	 */
	public boolean checkError();


	/*
	 * Exception-catching, synchronized output operations,
	 * which also implement the write() methods of OutputStream
	 */

	/**
	 * Write the specified byte to this stream.  If the byte is a newline and
	 * automatic flushing is enabled then the <code>flush</code> method will be
	 * invoked.
	 *
	 * <p> Note that the byte is written as given; to write a character that
	 * will be translated according to the platform's default character
	 * encoding, use the <code>print(char)</code> or <code>println(char)</code>
	 * methods.
	 *
	 * @param  b  The byte to be written
	 * @see #print(char)
	 * @see #println(char)
	 */
	public void write(int b);

	/**
	 * Write <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this stream.  If automatic flushing is
	 * enabled then the <code>flush</code> method will be invoked.
	 *
	 * <p> Note that the bytes will be written as given; to write characters
	 * that will be translated according to the platform's default character
	 * encoding, use the <code>print(char)</code> or <code>println(char)</code>
	 * methods.
	 *
	 * @param  buf   A byte array
	 * @param  off   Offset from which to start taking bytes
	 * @param  len   Number of bytes to write
	 */
	public void write(byte buf[], int off, int len);

	/* Methods that do not terminate lines */

	/**
	 * Print a boolean value.  The string produced by <code>{@link
	 * java.lang.String#valueOf(boolean)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      b   The <code>boolean</code> to be printed
	 */
	public void print(boolean b);
	
	/**
	 * Print a character.  The character is translated into one or more bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      c   The <code>char</code> to be printed
	 */
	public void print(char c);

	/**
	 * Print an integer.  The string produced by <code>{@link
	 * java.lang.String#valueOf(int)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      i   The <code>int</code> to be printed
	 * @see        java.lang.Integer#toString(int)
	 */
	public void print(int i);
	/**
	 * Print a long integer.  The string produced by <code>{@link
	 * java.lang.String#valueOf(long)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      l   The <code>long</code> to be printed
	 * @see        java.lang.Long#toString(long)
	 */
	public void print(long l);

	/**
	 * Print a floating-point number.  The string produced by <code>{@link
	 * java.lang.String#valueOf(float)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      f   The <code>float</code> to be printed
	 * @see        java.lang.Float#toString(float)
	 */
	public void print(float f);
	/**
	 * Print a double-precision floating-point number.  The string produced by
	 * <code>{@link java.lang.String#valueOf(double)}</code> is translated into
	 * bytes according to the platform's default character encoding, and these
	 * bytes are written in exactly the manner of the <code>{@link
	 * #write(int)}</code> method.
	 *
	 * @param      d   The <code>double</code> to be printed
	 * @see        java.lang.Double#toString(double)
	 */
	public void print(double d);
	/**
	 * Print an array of characters.  The characters are converted into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      s   The array of chars to be printed
	 * 
	 * @throws  NullPointerException  If <code>s</code> is <code>null</code>
	 */
	public void print(char s[]);

	/**
	 * Print a string.  If the argument is <code>null</code> then the string
	 * <code>"null"</code> is printed.  Otherwise, the string's characters are
	 * converted into bytes according to the platform's default character
	 * encoding, and these bytes are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      s   The <code>String</code> to be printed
	 */
	public void print(String s);

	/**
	 * Print an object.  The string produced by the <code>{@link
	 * java.lang.String#valueOf(Object)}</code> method is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the
	 * <code>{@link #write(int)}</code> method.
	 *
	 * @param      obj   The <code>Object</code> to be printed
	 * @see        java.lang.Object#toString()
	 */
	public void print(Object obj);

	/* Methods that do terminate lines */

	/**
	 * Terminate the current line by writing the line separator string.  The
	 * line separator string is defined by the system property
	 * <code>line.separator</code>, and is not necessarily a single newline
	 * character (<code>'\n'</code>).
	 */
	public void println();
	/**
	 * Print a boolean and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(boolean)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>boolean</code> to be printed
	 */
	public void println(boolean x);

	/**
	 * Print a character and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(char)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>char</code> to be printed.
	 */
	public void println(char x);
	
	/**
	 * Print an integer and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(int)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>int</code> to be printed.
	 */
	public void println(int x);

	/**
	 * Print a long and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(long)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  a The <code>long</code> to be printed.
	 */
	public void println(long x);

	/**
	 * Print a float and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(float)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>float</code> to be printed.
	 */
	public void println(float x);

	/**
	 * Print a double and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(double)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>double</code> to be printed.
	 */
	public void println(double x);

	/**
	 * Print an array of characters and then terminate the line.  This method
	 * behaves as though it invokes <code>{@link #print(char[])}</code> and
	 * then <code>{@link #println()}</code>.
	 *
	 * @param x  an array of chars to print.
	 */
	public void println(char x[]);

	/**
	 * Print a String and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(String)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>String</code> to be printed.
	 */
	public void println(String x);

	/**
	 * Print an Object and then terminate the line.  This method behaves as
	 * though it invokes <code>{@link #print(Object)}</code> and then
	 * <code>{@link #println()}</code>.
	 *
	 * @param x  The <code>Object</code> to be printed.
	 */
	public void println(Object x);
}
