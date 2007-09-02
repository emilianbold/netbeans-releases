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
package org.netbeans.modules.ruby.elements;

import java.io.IOException;
import java.util.Set;

import javax.swing.text.Document;

import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.filesystems.FileObject;


/**
 * A program element coming from the persistent index.
 *
 * @author Tor Norbye
 */
public abstract class IndexedElement extends RubyElement {
    protected final String signature;
    protected String fileUrl;
    protected final String clz;
    protected final String fqn;
    protected final RubyIndex index;
    protected final String require;
    protected final Set<Modifier> modifiers;
    protected final String attributes;
    private int docLength = -1;
    private Document document;
    private FileObject fileObject;

    protected IndexedElement(String signature, RubyIndex index, String fileUrl, String fqn,
        String clz, String require, Set<Modifier> modifiers, String attributes) {
        this.signature = signature;
        this.index = index;
        this.fileUrl = fileUrl;
        this.fqn = fqn;
        this.require = require;
        this.modifiers = modifiers;
        this.attributes = attributes;
        // XXX Why do methods need to know their clz (since they already have fqn)
        this.clz = clz;
    }

    public String getSignature() {
        return signature;
    }

    public final String getFileUrl() {
        return fileUrl;
    }

    public final String getRequire() {
        return require;
    }

    public final String getFqn() {
        return fqn;
    }

    @Override
    public String toString() {
        return getSignature() + ":" + getFileUrl();
    }

    public final String getClz() {
        return clz;
    }

    public RubyIndex getIndex() {
        return index;
    }

    public String getIn() {
        return getClz();
    }

    public String getFilenameUrl() {
        return fileUrl;
    }

    public Document getDocument() throws IOException {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = AstUtilities.getBaseDocument(fileObject, true);
        }

        return document;
    }

    public ParserFile getFile() {
        boolean platform = false; // XXX FIND OUT WHAT IT IS!

        return new DefaultParserFile(getFileObject(), null, platform);
    }

    public FileObject getFileObject() {
        if ((fileObject == null) && (fileUrl != null)) {
            fileObject = RubyIndex.getFileObject(fileUrl);

            if (fileObject == null) {
                // Don't try again
                fileUrl = null;
            }
        }

        return fileObject;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    /** Return the length of the documentation for this class, in characters */
    public int getDocumentationLength() {
        if (docLength == -1) {
            docLength = 0;

            if (attributes != null) {
                int index = attributes.indexOf('d');

                if (index != -1) {
                    index = attributes.indexOf('(', index + 1);

                    if (index != -1) {
                        docLength = Integer.parseInt(attributes.substring(index + 1,
                                    attributes.indexOf(')', index + 1)));
                    } else {
                        // Unknown length - just use 1 to indicate positive document length
                        docLength = 1;
                    }
                }
            }
        }

        return docLength;
    }
}
