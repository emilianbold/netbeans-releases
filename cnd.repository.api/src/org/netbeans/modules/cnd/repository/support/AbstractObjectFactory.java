
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

package org.netbeans.modules.cnd.repository.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author Sergey Grinev
 */
public abstract class AbstractObjectFactory {

    protected abstract int getHandler(Object object);
    protected abstract SelfPersistent createObject(int handler, DataInput stream) throws IOException;
    
    protected void writeSelfPersistent(SelfPersistent object, DataOutput output) throws IOException
    {
        if (object == null) {
            output.writeInt(NULL_POINTER);
        } else {
            int handler = getHandler(object);
            assert handler != NULL_POINTER;
            output.writeInt(handler);
            object.write(output);
        }
    }
    
    protected SelfPersistent readSelfPersistent(DataInput input) throws IOException
    {
        int handler = input.readInt();
        SelfPersistent object = null;
        if (handler != NULL_POINTER) {
            object = createObject(handler, input);
            assert object != null;
        }
        return object;
    }
    
    public static final int NULL_POINTER = -1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX
    public static final int LAST_INDEX = 0; 
}
