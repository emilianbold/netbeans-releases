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

package org.netbeans.api.gsf;

import org.openide.filesystems.FileObject;

/**
 * This represents an error registered for the current java source,
 * possibly with associated fix proposals.
 *
 * @todo Add a getArgs() method etc. such that error messages can be parameterized; see javax.tools.DiagnosticMessage
 *
 * @author Tor Norbye
 */
public interface Error  {
    /**
     * Provide a short user-visible (and therefore localized) description of this error
     */
    String getDisplayName();

    /**
     * Provide a full sentence description of this item, suitable for display in a tooltip
     * for example
     */
    String getDescription();

    /**
     * Return a unique id/key for this error, such as "compiler.err.abstract.cant.be.instantiated".
     * This key is used for error hints providers.
     */
    String getKey();
    
    ///** 
    // * Get the fixes associated with this error 
    // */
    //Collection<Fix> getFixes();
    //
    ///** 
    // * Register a fix proposal for this error 
    // */
    //void addFix(Fix fix);
    
    /**
     * Get the file object associated with this error, if any
     */
    FileObject getFile();

    /**
     * Get the position of the error
     * @todo Switch away from Position and just use integer offsets?
     */
    Position getStartPosition();
    
    /**
     * Get the end position of the error, or null if unknown
     * @todo Switch away from Position and just use integer offsets?
     */
    Position getEndPosition();
    
    /**
     *  Get the severity of this error
     */
    Severity getSeverity();
    
    /**
     * Return optional parameters for this message. The parameters may
     * provide the specific unknown symbol name for an unknown symbol error,
     * etc.
     */
    Object[] getParameters();

    /**
     * Set optional parameters for this message.
     *
     * @see #getParameters
     *
     * @param parameters The array of parameters
     */
    void setParameters(Object[] parameters);
}
