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
package com.sun.rave.web.ui.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 *  <p>	This <code>InputStream</code> looks for lines beginning with
 *	"#include '<em>filename</em>'" where filename is the name of a file to
 *	include.  It replaces the "#include" line with contents of the
 *	specified file.  Any other line beginning with '#' is illegal.</p>
 */
public class IncludeInputStream extends FilterInputStream {

    /**
     *	<p> Constructor.</p>
     */
    public IncludeInputStream(InputStream input) {
	super(input);
    }

    /**
     *	<p> This overriden method implements the include feature.</p>
     *
     *	@return	The next character.
     */
    public int read() throws IOException {
	int intChar = -1;
	if (redirStream != null) {
	    // We are already redirecting, delegate
	    intChar = redirStream.read();
	    if (intChar != -1) {
		return intChar;
	    }

	    // Found end of redirect file, stop delegating
	    redirStream = null;
	}

	// Read next character
	intChar = super.read();
	char ch = (char) intChar;

	// If we were at the end of the line, check for new line w/ #
	if (eol) {
	    // Check to see if we have a '#'
	    if (ch == '#') {
		intChar = startInclude();
	    } else {
		eol = false;
	    }
	}

	// Flag EOL if we're at the end of a line
	if ((ch == 0x0A) || (ch == 0x0D)) {
	    eol = true;
	}

	return intChar;
    }

    public int available() throws IOException {
	return 0;
    }

    public boolean markSupported() {
	return false;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
	if (bytes == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > bytes.length) || (len < 0) ||
		((off + len) > bytes.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

	int c = read();
	if (c == -1) {
	    return -1;
	}
	bytes[off] = (byte)c;

	int i = 1;
	try {
	    for (; i < len ; i++) {
		c = read();
		if (c == -1) {
		    break;
		}
		if (bytes != null) {
		    bytes[off + i] = (byte)c;
		}
	    }
	} catch (IOException ee) {
	    ee.printStackTrace();
	}
	return i;
    }

    /**
     *
     */
    private int startInclude() throws IOException {
	// We have a line beginning w/ '#', verify we have "#include"
	char ch;
	for (int count=0; count<INCLUDE_LEN; count++) {
	    // look for include
	    ch = (char) super.read();
	    if (Character.toLowerCase(ch) != INCLUDE.charAt(count)) {
		throw new RuntimeException(
			"\"#include\" expected in "
			+ "IncludeInputStream.");
	    }
	}

	// Skip whitespace...
	ch = (char) super.read();
	while ((ch == ' ') || (ch == '\t')) {
	    ch = (char) super.read();
	}

	// Skip '"' or '\''
	if ((ch == '"') || (ch == '\'')) {
	    ch = (char) super.read();
	}

	// Read the file name
	StringBuffer buf = new StringBuffer("");
	while ((ch != '"')
		&& (ch != '\'')
		&& (ch != 0x0A)
		&& (ch != 0x0D)
		&& (ch != -1)) {
	    buf.append(ch);
	    ch = (char) super.read();
	}

	// Skip ending '"' or '\'', if any
	if ((ch == '"') || (ch == '\'')) {
	    ch = (char) super.read();
	}

	// Get the file name...
	String filename = buf.toString();

	// Determine if we're in a JSF environment...
	if (FACES_CONTEXT != null) {
	    // We are... get a context root relative path...
	    filename = convertRelativePath(filename);
	}
	File file = new File(filename);
	// Make sure file exists (don't check read, let it throw an exception)
	if (file.exists()) {
	    // Open the included file
	    redirStream  = new IncludeInputStream(
		new BufferedInputStream(new FileInputStream(file)));
	} else {
	    // Check Classpath?
	    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
	    if (stream == null) {
		stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/"+filename);
	    }
	    if (stream == null) {
		throw new FileNotFoundException(filename);
	    }
	    redirStream  = new IncludeInputStream(
		    new BufferedInputStream(stream));
	}

	// Read the first character from the file to return
	return redirStream.read();
    }

    /**
     *	<p> This method converts a context-root relative path to the actual
     *	    path using the ServletContext or PortletContext.  This requires
     *	    the application to be running in a Servlet or Portlet
     *	    environment... and further requires that it be running in JSF
     *	    environment (which is used to access the Servlet or Portlet
     *	    Context).</p>
     *
     *	@param	filename    The relative filename to convert to a full path.
     *
     *	@return	The full path based on the app's context root.
     */
    protected String convertRelativePath(String filename) {
	// NOTE: This method uses reflection to avoid build/runtime
	// NOTE: dependencies on JSF, this method is only used if the
	// NOTE: FacesContext class is found in the classpath.

	// Check for the file in docroot
	Method method = null;
	Object ctx = null;
	String newFilename = null;
	try {
	    // The following should work w/ a ServletContext or PortletContext
	    // Get the FacesContext...
	    method = FACES_CONTEXT.getMethod(
		    "getCurrentInstance", (Class []) null);
	    ctx = method.invoke((Object) null, (Object []) null);

	    // Get the ExternalContext...
	    method = ctx.getClass().getMethod(
		    "getExternalContext", (Class []) null);
	    ctx = method.invoke(ctx, (Object []) null);

	    // Get actual underlying external context...
	    method = ctx.getClass().getMethod(
		    "getContext", (Class []) null);
	    ctx = method.invoke(ctx, (Object []) null);

	    // Get the real path using the ServletContext/PortletContext
	    method = ctx.getClass().getMethod(
		    "getRealPath", GET_REAL_PATH_ARGS);
	    newFilename = (String) method.invoke(ctx, new Object [] {filename});
	    if (!(new File(newFilename)).exists()) {
		// The file doesn't exist, fall back to absolute path
		newFilename = filename;
	    }
	} catch (NoSuchMethodException ex) {
	    throw new RuntimeException(ex);
	} catch (IllegalAccessException ex) {
	    throw new RuntimeException(ex);
	} catch (InvocationTargetException ex) {
	    throw new RuntimeException(ex);
	}
	return newFilename;
    }

    /**
     *	<p> Simple test case (requires a test file).</p>
     */
    public static void main(String args[]) {
	try {
	    IncludeInputStream stream =
		new IncludeInputStream(new FileInputStream(args[0]));
	    int ch = '\n';
	    while (ch != -1) {
		System.out.print((char) ch);
		ch = stream.read();
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private boolean eol = true;
    private IncludeInputStream redirStream = null;

    private static final Class [] GET_REAL_PATH_ARGS =
	    new Class[] {String.class};

    private static final String INCLUDE	    =	"include";
    private static final int	INCLUDE_LEN =	INCLUDE.length();

    private static Class FACES_CONTEXT;
    
    static {
	try {
	    FACES_CONTEXT = Class.forName("javax.faces.context.FacesContext");
	} catch (Exception ex) {
	    // Ignore, this just means we're not in a JSF environment
	    FACES_CONTEXT = null;
	}
    }
}
