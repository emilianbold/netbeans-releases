/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.parser;

import java.io.IOException;

import org.xml.sax.InputSource;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeDocumentRoot;

import org.netbeans.modules.xml.tax.parser.ParsingSupport;

/**
 * 
 *
 * @author  Petr Kuzel
 * @version 
 */
public class XMLParsingSupport extends ParsingSupport {

    /**
     * Parse XML document and return TreeDocument instance ot null.
     */
    public TreeDocumentRoot parse(InputSource in) throws IOException, TreeException {
//        if (url == null)
//	    url = getPrimaryFile().getURL();
        TreeStreamSource treeBuilder = new TreeStreamSource (TreeDocument.class, in, null);
        return (TreeDocument)treeBuilder.getBuilder().buildDocument();
        
    }
}
