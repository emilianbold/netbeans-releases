/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.php.dbgp.annotations;

import org.openide.text.Annotatable;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class CallStackAnnotation extends DebuggerAnnotation {
    
    static final String CALL_STACK_ANNOTATION_TYPE = "CallSite";  // NOI18N   
    
    private static final String CALL_STACK_LINE     = "ANTN_CALLSITE";

    public CallStackAnnotation( Annotatable annotatable ) {
        super(annotatable);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.annotations.DebuggerAnnotation#getAnnotationType()
     */
    @Override
    public String getAnnotationType()
    {
        return CALL_STACK_ANNOTATION_TYPE;
    }
    
    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getShortDescription()
     */
    @Override
    public String getShortDescription()
    {
        return NbBundle.getBundle(DebuggerAnnotation.class).
            getString( CALL_STACK_LINE );
    }

}
