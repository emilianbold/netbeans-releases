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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
