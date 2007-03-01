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

package org.netbeans.modules.languages.features;

import java.io.Serializable;

public class ActionCreator implements Serializable {
    
    static final long serialVersionUID = -3408200516931076212L;
    
    private static final int NAME_INDEX = 0;
    private static final int PERFORMER_INDEX = 1;
    private static final int ENABLER_INDEX = 2;
    
    private Object[] params;
    
    public ActionCreator() {
    }
    
    public ActionCreator(Object[] params) {
        this.params = params;
    }
    
    public Object readResolve() {
        return new GenericAction((String)params[NAME_INDEX],
                (String)params[PERFORMER_INDEX], (String)params[ENABLER_INDEX]);
    }
    
}
