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
