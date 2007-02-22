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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Sergey Grinev
 */
public final class MacroAndIncludeObjectFactory {
    
    /** Never create a new instance of MacroAndIncludeObjectFactory */
    private MacroAndIncludeObjectFactory() {};
    
    public static void write(CsmMacro macro, DataOutput output) throws IOException
    {
        assert( macro != null && output != null );
        // there is only one impl, so we don't need a handler here
        assert( macro instanceof MacroImpl);
        ((SelfPersistent)macro).write(output);
    }
    
//    public static CsmMacro read(DataInput input)
    
}
