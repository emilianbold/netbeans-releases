/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
