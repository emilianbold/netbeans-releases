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
package org.netbeans.modules.visualweb.insync.java;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.event.DocumentEvent;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.SourceUnit;
import org.netbeans.modules.visualweb.insync.UndoManager;
import org.netbeans.modules.visualweb.insync.Unit.State;
import org.netbeans.modules.visualweb.insync.Util;

/**
 * <p>A SourceUnit that manages a Java source file.</p>
 * @author Carl Quinn
 * @version 1.0
 */
public class JavaUnit extends SourceUnit {

    URLClassLoader classLoader;
    private boolean markSourceDirty = true;
    ParserAnnotation[] errors = ParserAnnotation.EMPTY_ARRAY;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a JavaUnit from an existing source Document
     */
    public JavaUnit(FileObject fobj, URLClassLoader cl, UndoManager undoManager) {
        super(fobj, undoManager);
        //Trace.enableTraceCategory("insync.java");
    }

    /*
     * Clean up all of our big resources.
     * @see org.netbeans.modules.visualweb.insync.Unit#destroy()
     */
    public void destroy() {
        classLoader = null;
        super.destroy();
    }

    //---------------------------------------------------------------------------------------- Input

    protected void read(char[] cbuf, int len) {
        
    }
    
    /**
     * Read the errors in source
     */
    protected void readErrors() {
        ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<ParserAnnotation> parserAnnotationsList = new ArrayList<ParserAnnotation>();
                for(Diagnostic diagnostic : cinfo.getDiagnostics()) {
                    if(diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                        ParserAnnotation annotation = new ParserAnnotation(diagnostic.getMessage(Locale.getDefault()),
                                fobj, (int)diagnostic.getLineNumber(), (int)diagnostic.getColumnNumber());
                        parserAnnotationsList.add(annotation);
                    }
                }
                errors = parserAnnotationsList.toArray(ParserAnnotation.EMPTY_ARRAY);
                return null;
            }
        }, fobj);
    }

    /**
     * @param out
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream out) throws java.io.IOException {
    }
    
    /**
     * Implicit read. Read the document supplied during construction into this model.
     * 
     * @return whether or not the read affected the model.
     */
    public boolean sync() {
        //Grab the current document from our underlying editor. 
        //The property change listener from editor cookie (see SourceUnit)
        //will add UndoableEditListener to the document.
        
        //XXX We need to revisit this as the document is unnecessarily grabbed 
        //It should be probably opened in the JavaClassAdapter before write transaction 
        //to restrict being opened only during flush - Winston
        
        Util.retrieveDocument(fobj, true);
        
        // make sure it is necessary & ok to read.
        State state = getState();
        assert Trace.trace("insync", "SU.sync of " + getName() + " state:" + state );
        if (state == State.CLEAN)
            return false;
        if (state == State.MODELDIRTY) {
            assert Trace.trace("insync", "SU.sync attempt to read source into a dirty model");
            //Trace.printStackTrace();
            return false;
        }

        readErrors();
        
        if(errors.length > 0) {
            setBusted();
        } else {
            setClean();
        }
        
        return true;
    }
    

    //------------------------------------------------------------------------------------ Accessors

    /*
     * Returns the public class in the file JavaUnit represents
     */
    public JavaClass getJavaClass() {
        return JavaClass.getJavaClass(fobj);
    }


    public String getPackageName() {
       return (String)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return cinfo.getCompilationUnit().getPackageName().toString();
            }
        }, fobj);
    }


    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getErrors()
     */
    public ParserAnnotation[] getErrors() {
        if(errors == null)
            readErrors();
        return errors;
    }
    
    //------------------------------------------------------------------------------------- Mutators

    /**
     * Add a new import after the given loc
     * @param ident
     * @return
     */
    public Object/*ImportTree*/ addImport(String ident) {
        return null;
    }

    /**
     * Ensure that a given import is in the list, if not add it after loc
     * @param ident
     * @return The existing or added import
     */
    public Object/*ImportTree*/ ensureImport(String ident) {
        return null;
    }

    /**
     *
     */
    public JavaClass addClass(String name) {
        return null;
    }

    /**
     *
     */
    public void removeImport(String ident) {
    }

    /**
     *
     */
    public void removeClass(JavaClass cls) {
    }

    //-------------------------------------------------------------------------------------- Helpers


    protected void endFlush(boolean madeDirty) {
    }
    
    
    public boolean flush() {    
        return true;
    }

    public void writeTo(Writer w) throws IOException {
    }

    public void dumpTo(PrintWriter w) {
    }
    
    //----------------------------------------------------------------------------- DocumentListener

    /*
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.insertUpdate");
        undoManager.notifyBufferEdited(this);
        if(markSourceDirty) {
            setSourceDirty();
        }
    }

    /*
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.removeUpdate");
        undoManager.notifyBufferEdited(this);
        if(markSourceDirty) {
            setSourceDirty();
        }
    }
    
    protected synchronized void firstWriteLock() {
        markSourceDirty = false;
    }

    protected synchronized void lastWriteUnlock() {
        markSourceDirty = true;
    }
    
}
