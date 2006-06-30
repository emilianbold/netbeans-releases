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
