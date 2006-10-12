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

package org.netbeans.test.stub.api;

import java.util.HashMap;
import org.netbeans.modules.j2ee.persistence.editor.completion.*;

/**
 *
 * @author Andrei Badea
 */
public abstract class StubDelegate {

    private HashMap properties = new HashMap();
    
    protected void setProperty(Object key, Object value) {
        properties.put(key, value);
    }
    
    protected Object getProperty(Object key) {
        return properties.get(key);
    }
}
