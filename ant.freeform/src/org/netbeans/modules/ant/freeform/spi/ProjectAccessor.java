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
package org.netbeans.modules.ant.freeform.spi;

import org.netbeans.modules.ant.freeform.*;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Accessor for AntProjectHelper and PropertyEvaluator, resides in project's lookup.
 * @author Jan Lahoda
 */
public final class ProjectAccessor {

    private FreeformProject p;
    
    static {
        AccessorImpl.createAccesor();
    }
    /** Creates a new instance of ProjectAccessor */
    ProjectAccessor(FreeformProject p) {
        this.p = p;
    }

    public AntProjectHelper getHelper() {
        return p.helper();
    }

    public PropertyEvaluator getEvaluator() {
        return p.evaluator();
    }
    
}
