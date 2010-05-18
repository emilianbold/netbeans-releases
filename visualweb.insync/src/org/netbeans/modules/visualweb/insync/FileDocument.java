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
package org.netbeans.modules.visualweb.insync;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.StringContent;

/**
 * A concrete swing AbstractDocument implementation to allow easy file access to SourceUnits
 * expecting an AbstractDocument. Used primarily for unit testing and other debugging.
 * @author Carl Quinn
 */
public class FileDocument extends AbstractDocument {

    /**
     *
     */
    private static final long serialVersionUID = 3833183653189988409L;

    String name;
    RootElement root = new RootElement();

    //------------------------------------------------------------------------------------- Events

    public FileDocument(String filename) throws java.io.IOException {
	super(new StringContent());
	name = filename;
	read();
    }

    /**
     * Read from the associated file into this document
     */
    public void read() throws java.io.IOException {
	File f = new File(name);
	int len = (int)f.length();
	Reader r = new FileReader(f);
	char[] filebuf = new char[len];
	r.read(filebuf);
	r.close();

	try {
	    replace(0, getLength(), new String(filebuf), null);
	}
	catch (javax.swing.text.BadLocationException e) {
	    // we know the location we passed is good...
	}
    }

    /**
     * Write to the associated file from this document
     */
    public void write() throws java.io.IOException {
	File f = new File(name);
	//int len = (int)f.length();
	Writer w = new FileWriter(f);

	try {
	    String text = getText(0, getLength());

	    w.write(text);
	    w.close();
	}
	catch (javax.swing.text.BadLocationException e) {
	    // we know the location we passed is good...
	}
    }

    class RootElement extends AbstractDocument.AbstractElement {

	/**
         *
         */
        private static final long serialVersionUID = 3545234717587551797L;

    RootElement() {
	    super(null, null);
	}

	public int getStartOffset() {
	    return 0;
	}

	public int getEndOffset() {
	    return getLength();
	}

	public boolean isLeaf() {
	    return true;
	}

	public boolean getAllowsChildren() {
	    return false;
	}

	public Enumeration children() {
	    return null;
	}

	public Element getElement(int i) {
	    return null;
	}

	public int getElementCount() {
	    return 0;
	}

	public int getElementIndex(int offset) {
	    return -1;
	}

    }

    public Element getDefaultRootElement() {
	return root;
    }

    public Element getParagraphElement(int pos) {
	return root;
    }
}
