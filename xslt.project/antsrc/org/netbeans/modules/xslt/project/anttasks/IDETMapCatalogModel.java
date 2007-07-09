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
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.spi.TMapModelFactory;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Vitaly Bychkov
 *
 */
public class IDETMapCatalogModel {

    static IDETMapCatalogModel singletonCatMod = null;

    /**
     * Constructor
     */
    public IDETMapCatalogModel() {
    }

    /**
     * Gets the instance of this class internal API
     * @return current class instance
     */
    public static IDETMapCatalogModel getDefault(){
        if (singletonCatMod == null){
            singletonCatMod = new IDETMapCatalogModel();
        }
        return singletonCatMod;
    }



    /**
     * Creates TMap Model from transformation descriptor
     * @param File - transformation descriptor
     * @throws java.lang.Exception
     * @return Transformation Model
     */
     public TMapModel getTMapModel(File file) throws Exception {
             //convert file to FileObject
             ModelSource source = org.netbeans.modules.xml.retriever.catalog.Utilities.createModelSource(FileUtil.toFileObject(file), true);

             TMapModelFactory factory = 
                     TMapModelFactory.TMapModelFactoryAccess.getFactory();

             TMapModel model = factory.getModel(source);
             model.sync();
             return model;
         }

}
