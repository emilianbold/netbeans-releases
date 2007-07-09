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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;

/**
 *
 * @author Vitaly Bychkov
 */
public class IDEJBIGenerator extends AbstractJBIGenerator {
    private Logger logger = Logger.getLogger(IDEJBIGenerator.class.getName());    

    public IDEJBIGenerator(String srcDir, String buildDir) {
        super(srcDir, buildDir);
    }

    
    @Override
    protected <T extends ReferenceableWSDLComponent> T resolveReference(
            NamedComponentReference<T> ref) 
    {
        if (ref == null) {
            return null;
        }
        return ref.get();
    }
    
    @Override
    protected TMapModel getTMapModel() {
        File transformmapFile = getTransformmapFile();
        if (transformmapFile == null) {
            logger.log(Level.SEVERE, "Error encountered while processing transformmap file - "+transformmapFile.getAbsolutePath());
            throw new BuildException("Can't find transformation descriptor");
        }
        TMapModel tMapModel = null;
        try {
            tMapModel = IDETMapCatalogModel.getDefault().
                                            getTMapModel(transformmapFile);
        }catch (Exception ex) {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while creating Tramsformap Model ", ex);
            throw new RuntimeException("Error while creating Transformmap Model ",ex);
        }
        
        if (tMapModel == null 
                || !TMapModel.State.VALID.equals(tMapModel.getState())) 
        {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while creating Transformmap Model - "+(tMapModel == null ? " is null" : " is not valid"));
            throw new BuildException("Error while creating Transformmap Model ");
        }
        return tMapModel;
    }
    
}
