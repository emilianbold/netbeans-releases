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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 * This class helps Bpel project to obtain the Bpel model given a
 * BPEL File URI
 * @author Sreenivasan Genipudi
 */
public class IDEBPELCatalogModel {

    static IDEBPELCatalogModel singletonCatMod = null;

    /**
     * Constructor
     */
    public IDEBPELCatalogModel() {
    }

    /**
     * Gets the instance of this class internal API
     * @return current class instance
     */
    public static IDEBPELCatalogModel getDefault(){
        if (singletonCatMod == null){
            singletonCatMod = new IDEBPELCatalogModel	();
        }
        return singletonCatMod;
    }



    /**
     * Creates BPEL Model from BPEL URI
     * @param locationURI
     * @throws java.lang.Exception
     * @return
     */
     public BpelModel getBPELModel(File file) throws Exception {
             //convert file to FileObject
             ModelSource source = org.netbeans.modules.xml.retriever.catalog.Utilities.createModelSource(FileUtil.toFileObject(file), true);

             BpelModelFactory factory = (BpelModelFactory) Lookup.getDefault()
                     .lookup(BpelModelFactory.class);

             BpelModel model = factory.getModel(source);
             model.sync();
             return model;
         }

}
