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

import org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLGraphDeserializer;

import org.xml.sax.*;

import java.io.InputStream;

/**
 *
 * @author  Administrator
 * @version
 */
public class XMLInputStream extends java.io.DataInputStream implements java.io.ObjectInput
{

    private InputStream inStream;
    private ClassLoader classLoader;

    //@lars: added classloader-constructor
    /** Creates new XMLInputStream with the given classloader*/
    public XMLInputStream(InputStream in,
                          ClassLoader cl)
    {
        super(in);
        this.inStream = in;
        this.classLoader = cl;
    }

    /** Creates new XMLInputStream */
    public XMLInputStream(InputStream in)
    {
        this (in, null);
    }

    public java.lang.Object readObject() throws java.lang.ClassNotFoundException, java.io.IOException
    {

        try
        {

            XMLGraphDeserializer lSerializer = new XMLGraphDeserializer(this.classLoader);
            lSerializer.Begin();
            InputSource input = new InputSource(this.inStream);
            input.setSystemId("archiverNoID");

            lSerializer.setSource(input);

            return lSerializer.XlateObject();
        }
        catch (SAXException lError)
        {
            lError.printStackTrace();
            java.io.IOException lNewError = new java.io.IOException(lError.getMessage());
            throw lNewError;
        }
    }

}
