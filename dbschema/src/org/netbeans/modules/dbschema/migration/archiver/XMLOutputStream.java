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
