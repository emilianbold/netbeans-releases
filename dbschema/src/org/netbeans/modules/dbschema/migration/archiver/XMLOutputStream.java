/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.migration.archiver;

import org.netbeans.modules.dbschema.migration.archiver.serializer.XMLGraphSerializer;

import java.io.OutputStream;

/**
 *
 * @author  Administrator
 * @version 
 */
public class XMLOutputStream extends java.io.DataOutputStream implements java.io.ObjectOutput
{

    private OutputStream outStream;

    /** Creates new XMLOutputStream */
    public XMLOutputStream(OutputStream out) 
    {
        super(out);
        this.outStream = out;
    }
    
    public void close() throws java.io.IOException
    {
        this.outStream.close();
    }

    public void writeObject(Object o) throws java.io.IOException
    {

        XMLGraphSerializer lSerial = new XMLGraphSerializer(this.outStream);
        lSerial.writeObject(o);
        
    }
}
