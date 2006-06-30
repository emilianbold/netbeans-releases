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

package org.netbeans.modules.schema2beans;

import java.io.*;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author cliffwd
 * An org.xml.sax.EntityResolver that does nothing.  Good for those times
 * when you want 0 validation done, and to make sure it's not going to the
 * network for a DTD lookup.
 */
public class NullEntityResolver implements EntityResolver {
    //protected String dummyDTD = "<!ELEMENT dummy EMPTY >\n";
    protected String dummyDTD = "";
    protected byte[] buf = dummyDTD.getBytes();
    protected static NullEntityResolver theResolver = null;

    private NullEntityResolver() {
    }

    public static NullEntityResolver newInstance() {
        if (theResolver == null)
            theResolver = new NullEntityResolver();
        return theResolver;
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
    {
        //System.out.println("resolveEntity: publicId="+publicId+" systemId="+systemId);
        ByteArrayInputStream bin = new ByteArrayInputStream(buf);
        return new InputSource(bin);
    }
}
