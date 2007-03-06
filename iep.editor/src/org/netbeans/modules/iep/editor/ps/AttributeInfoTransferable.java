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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.ps;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AttributeInfoDataFlavor.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class AttributeInfoTransferable implements Transferable {
    List tagList;
    DataFlavor flavors[];
    
    public AttributeInfoTransferable(Object objs[]) {
        tagList = null;
        flavors = (new DataFlavor[] {
            AttributeInfoDataFlavor.ATTRIBUTE_INFO_FLAVOR
        });
        tagList = new ArrayList(Arrays.asList(objs));
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Arrays.asList(flavors).contains(flavor);
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if(flavor.equals(AttributeInfoDataFlavor.ATTRIBUTE_INFO_FLAVOR)) {
            return tagList;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }
    
}