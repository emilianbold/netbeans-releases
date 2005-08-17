/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.structure.api;

/**
 * DocumentModelException is thrown from the DocumentModel's methods.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class DocumentModelException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>DocumentModelException</code> without detail message.
     */
    public DocumentModelException() {
    }
    
    /**
     * Constructs an instance of <code>DocumentModelException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DocumentModelException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>DocumentModelException</code> with the specified detail message and cause throwable.
     * @param message the detail message.
     * @param t the cause of the exception
     */
    public DocumentModelException(String message, Throwable t) {
        super(message, t);
    }
}
