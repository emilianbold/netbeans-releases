/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.xam.AbstractModelFactory;

/**
 *
 * @author rico
 */
public class WSDLModelFactory extends AbstractModelFactory<WSDLModel> {
    
    private static final WSDLModelFactory wsdlModelFactory = new WSDLModelFactory();
    
    private Map<Document, WeakReference<WSDLModel>> cachedModels =
            new WeakHashMap<Document,WeakReference<WSDLModel>>();

    public static WSDLModelFactory getDefault(){
        return wsdlModelFactory;
    }
    
    /** Creates a new instance of WSDLModelFactory */
    private WSDLModelFactory() {
    }

    protected WSDLModel createModel(ModelSource source) {
        return new WSDLModelImpl(source);
    }
}
