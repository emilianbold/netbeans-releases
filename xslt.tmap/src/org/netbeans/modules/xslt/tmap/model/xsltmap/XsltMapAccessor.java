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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.util.Map;
import java.util.WeakHashMap;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;

/**
 * Accessor for the XsltMapModel
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltMapAccessor {
    
    private XsltMapAccessor() {
    }
    
    /**
     * If xsltMapFile is null then return null
     * If xsltMapFile couldn't be parsed or has another 
     * structure then required for the xsltmap.xml file by specification then return null
     *
     * now xsltMap model should be rebuilded every time as xsltMap file has been changed
     * @param xsltMapFile xsltmap.xml file contains neccessary xslt transforamtion use cases
     * @return XsltMapModel correspondent to the xsltmap file
     */
    public static XsltMapModel getXsltMapModel(FileObject xsltMapFile) {
        if (!isValidXsltMapFile(xsltMapFile)) {
            return null;
        }
        XsltMapModel model = new XsltMapModel(xsltMapFile);
        model.initXsltMapModel();
        if (!model.isInitModel()) {
            model = null;
        }
        
        return model;
    }
    
    public static boolean isValidXsltMapFile(FileObject xsltMapFile) {
        return xsltMapFile != null 
                && XsltMapConst.XML.equals(xsltMapFile.getExt()) 
                && XsltMapConst.XSLTMAP.equals(xsltMapFile.getName());
    }
    
    
}
