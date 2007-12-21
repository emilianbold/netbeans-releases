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
package org.netbeans.modules.xslt.core.util;

import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xslt.model.spi.XslModelFactory;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.util.Lookup;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class Util {
    
    public static XslModel getXslModel(FileObject xslFo) {
        XslModel model = null;
        if (xslFo != null) {
            ModelSource modelSource = Utilities.getModelSource(xslFo, true);
            model = XslModelFactory.XslModelFactoryAccess.getFactory().getModel(modelSource);
        }
        
        return model;
    }

    public static FileObject getFileObjectByModel(Model model){
        if (model != null){
            ModelSource src = model.getModelSource();
            if (src != null){
                Lookup lookup = src.getLookup();
                if (lookup != null){
                    return lookup.lookup(FileObject.class);
                }
            }
        }
        return null;
    }
}
