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

package org.netbeans.modules.bpel.debugger.spi;

import java.beans.PropertyChangeListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;

/**
 * @author Alexander Zgursky
 */
public interface EditorContext {

    /**
     * Opens given file (url) in the editor and navigates to the given position.
     *
     * @param url full path to the source file to show
     * @param xpath xpath of the bpel element to navigate to
     *
     * @return true if succeeded to show the source or false otherwise
     */
    boolean showSource(String url, String xpath, String view);
    
    /**
     * Annotates the bpel element identified by the given xpath in the given
     * file (url) with the given annotation type.
     *
     * @param url full path to the source file to add annotation for
     * @param xpath annotation position
     * @param annotationType annotation type
     *
     * @return a reference to the created annotation object. This object should
     *         be supplied as a parameter to subsequent
     *         {@link #removeAnnotation} call
     */
    Object addAnnotation(String url, String xpath, int lineNumber, AnnotationType annotationType);

    /**
     * Removes the given annotation.
     *
     * @param annotation a reference to the annotation object that is returned
     *                   from {@link #annotate} method
     */
    void removeAnnotation(Object annotation);
    
    boolean isAttached(Object annotation);
    
    boolean isValid(Object annotation);
    
    AnnotationType getAnnotationType(Object annotation);
    
    String getXpath(Object annotation);
    
    int getLineNumber(Object annotation);
    
    QName getProcessQName(String url);
    
    QName getCurrentProcessQName();
    
    /**
     * Returns the more appropriate line number for the
     * given the url and line number.
     */
    int translateBreakpointLine(String url, int lineNumber);
    
    public void addAnnotationListener(Object annotation, PropertyChangeListener l);
    
    public void removeAnnotationListener(Object annotation, PropertyChangeListener l);
}
