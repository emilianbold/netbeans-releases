/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.pdf;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/** Simple node to represent a PDF file.
 * @author Jesse Glick
 */
public class PDFDataNode extends DataNode {

    public PDFDataNode (PDFDataObject obj) {
        this (obj, Children.LEAF);
    }

    public PDFDataNode (PDFDataObject obj, Children ch) {
        super (obj, ch);
        setIconBase ("/org/netbeans/modules/pdf/PDFDataIcon");
    }

}
