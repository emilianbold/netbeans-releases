/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
