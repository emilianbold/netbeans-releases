/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package djava.security;

import java.security.MessageDigest;
import org.netbeans.performance.Benchmark;

// Results on a P6/1200 JDK 1.4.1rc for MD5 (rough numbers; magnitude is in bytes):
//  100 - 10us
// 1000 - 45us
// For SHA-1:
//  100 - 10-15us
// 1000 - 70-75us

/**
 * Benchmark measuring SHA-1 digestion.
 *
 * @author Jesse Glick
 */
public class MessageDigestTest extends Benchmark {
    
    public static void main(String[] args) {
        simpleRun(MessageDigestTest.class);
    }
    
    public MessageDigestTest(String name) {
        super(name, new Integer[] {new Integer(100), new Integer(1000), new Integer(10000)});
    }
    
    private byte[] buf;
    private MessageDigest dig;
    protected void setUp() throws Exception {
        int magnitude = ((Integer)getArgument()).intValue();
        buf = new byte[magnitude];
        for (int i = 0; i < magnitude; i++) {
            buf[i] = (byte)i;
        }
        dig = MessageDigest.getInstance("SHA-1");
    }
    
    public void testSHA1() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            dig.reset();
            dig.digest(buf);
        }
    }
    
}
