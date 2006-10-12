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
package org.netbeans.modules.websvc.core.jaxws.bindings.model;

import java.io.IOException;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.impl.BindingsModelImpl;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsModelFactory extends AbstractModelFactory<BindingsModel>{
    
     private static final BindingsModelFactory bindingsModelFactory = new BindingsModelFactory();
    /** Creates a new instance of BindingsModelFactory */
    private BindingsModelFactory() {
    }
    
    /**
     * Gets Bindings model from given model source.  Model source should 
     * provide lookup for:
     * 1. FileObject of the model source
     * 2. DataObject represent the model
     * 3. Swing Document buffer for in-memory text of the model source
     */
     public BindingsModel getModel(ModelSource source) {
        return super.getModel(source);
    }
     
     public static BindingsModelFactory getDefault(){
        return bindingsModelFactory;
    }

    protected BindingsModel createModel(ModelSource source) {
        return new BindingsModelImpl(source);
    }
   
}
