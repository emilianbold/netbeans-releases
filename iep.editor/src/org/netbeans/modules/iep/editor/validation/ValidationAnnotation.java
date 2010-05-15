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

/*
 * Created on Mar 22, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.iep.editor.validation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ValidationAnnotation extends Annotation implements PropertyChangeListener {
    
    /** The error message shown on mouseover on the pmd icon */
    private String errormessage = null;
    
    /** The annotations currently existing. */
    private static List annotations = new ArrayList();
    
    private ValidationAnnotation() {}
    
    public static final ValidationAnnotation getNewInstance() {
        ValidationAnnotation va = new ValidationAnnotation();
        annotations.add( va );
        return va;
    }
    
    public static final void clearAll() {
        Iterator iterator = annotations.iterator();
        while( iterator.hasNext() ) {
            ((Annotation)iterator.next()).detach();
        }
        annotations.clear();
    }
    
    /**
     * The annotation type.
     *
     * @return the string "wsdl-validation-annotation"
     */
    @Override
    public String getAnnotationType() {
        return "org-netbeans-modules-xml-core-error"; //NOI18N
    }
    
    
    /**
     * Sets the current errormessage
     *
     * @param message the errormessage
     */
    public void setErrorMessage( String message ) {
        errormessage = message;
    }
    
    
    /**
     * A short description of this annotation
     *
     * @return the short description
     */
    @Override
    public String getShortDescription() {
        return errormessage;
    }
    
    
    /**
     * Invoked when the user change the content on the line where the annotation is
     * attached
     *
     * @param propertyChangeEvent the event fired
     */
    public void propertyChange( PropertyChangeEvent propertyChangeEvent ) {
        Line line = ( Line )propertyChangeEvent.getSource();
        line.removePropertyChangeListener( this );
        detach();
    }
    
}

