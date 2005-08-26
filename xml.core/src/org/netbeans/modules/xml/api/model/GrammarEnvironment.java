/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.api.model;

import java.util.Enumeration;
import org.openide.filesystems.FileObject;
import org.xml.sax.InputSource;

/**
 * Grammar environment provides grammar factory with a context.
 * All returned object must be treated as read-only. Grammar
 * should do the best it can do in passed (possibly partial)
 * environment.
 *
 * @author  Petr Kuzel
 */
public final class GrammarEnvironment {

    private final FileObject fileObject;
    private final InputSource inputSource;
    private final Enumeration documentChildren;
        
    /** 
     * Creates a new instance of GrammarEnvironment.
     *
     * @param documentChildren Enumeration of document level DOM nodes.
     * @param inputSource Supported document input source.
     * @param fileObject  Supported document fileObject or <code<null</code>.
     */
    public GrammarEnvironment(Enumeration documentChildren, InputSource inputSource, FileObject fileObject) {
        if (inputSource == null) throw new NullPointerException();
        if (documentChildren == null) throw new NullPointerException();
        this.inputSource = inputSource;
        this.fileObject = fileObject;
        this.documentChildren = documentChildren;
    }
    
    /**
     * There is always input source of supported document reflecting
     * current in-memory state.
     *
     * @return InputSource
     */
    public InputSource getInputSource() {
        return inputSource;
    }
    
    /**
     * If supported document exists in a form of FileObject it can
     * be retrieved. Note that data provided from file object
     * input stream may be different than current in-memory state.
     *
     * @return FileObject or null.
     */
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * Preparsed document children for fast decision based on
     * document structure.
     *
     * @return Enumeration of DOM Nodes representing document
     * children. Enumeration elements are valid only during
     * {@link GrammarQueryManager#enabled} method invocation.
     */
    public Enumeration getDocumentChildren() {
        return documentChildren;
    }
}
