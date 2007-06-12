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

import java.io.DataOutput;
import java.io.IOException;

/**
 * interface for object which supports writing own data in stream
 * usually such object has also constructor for creating from stream
 * i.e.  
 * class Clazz implements SelfPersistent {
 *      public Clazz(DataInput input) throws IOException {
 *          field = input.read();
 *      }
 *      public void write (DataOutput output) throws IOException {
 *          output.write(field);
 *      }
 * }
 *
 * @author Sergey Grinev
 */
public interface SelfPersistent {
        public void write (DataOutput output) throws IOException;
}
