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

import java.sql.Connection;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class StubTest extends NbTestCase {
    
    public StubTest(String name) {
        super(name);
    }
    
    public void testDefault() {
        Primitives p = (Primitives)Stub.create(new Class[] { Primitives.class });
        
        assertEquals(System.identityHashCode(p), p.hashCode());
        
        assertTrue(p.equals(p));
        assertFalse(p.equals(new Object()));
        
        assertEquals((byte)0, p.getByte());
        assertEquals((short)0, p.getShort());
        assertEquals(0, p.getInteger());
        assertEquals(0L, p.getLong());
        assertEquals(Float.floatToRawIntBits(0), Float.floatToRawIntBits(p.getFloat()));
        assertEquals(Double.doubleToRawLongBits(0.0), Double.doubleToRawLongBits(p.getDouble()));
        assertEquals('\0', p.getCharacter());
        assertEquals(false, p.getBoolean());
    }
    
    private static interface Primitives {
        
        public byte getByte();
        
        public short getShort();
        
        public int getInteger();
        
        public long getLong();
        
        public float getFloat();
        
        public double getDouble();
        
        public char getCharacter();
        
        public boolean getBoolean();
    }
}
