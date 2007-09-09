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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import junit.framework.Assert;

/**
 *
 * @author Jan Lahoda
 */
public class TestUtilities {

    private TestUtilities() {
    }
    
    public static String detectOffsets(String source, int[] positionOrSpan) {
        //for now, the position/span delimiter is '|', without possibility of escaping:
        String[] split = source.split("\\|");
        
        Assert.assertTrue("incorrect number of position markers (|)", positionOrSpan.length == split.length - 1);
        
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int offset = 0;
        
        for (String s : split) {
            sb.append(s);
            if (index < positionOrSpan.length)
                positionOrSpan[index++] = (offset += s.length());
        }
        
        return sb.toString();
    }

}
