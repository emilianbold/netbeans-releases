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

package org.netbeans.lib.ddl.impl;

import java.io.*;
import java.util.*;
import java.text.ParseException;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.util.PListReader;

/**
* SpecificationParser extends PListReader. It should be removed (it's functionality
* seems to be zero), but it's prepared here for future nonstandard implementations
* among the PListReader and it's use here. 
*
* @author Slavek Psenicka
*/
public class SpecificationParser extends PListReader {

    /** Constructor */
    public SpecificationParser(String file)
    throws FileNotFoundException, ParseException, IOException
    {
        super(file);
    }

    /** Constructor */
    public SpecificationParser(InputStream stream)
    throws FileNotFoundException, ParseException, IOException
    {
        super(stream);
    }
}

/*
* <<Log>>
*/	
