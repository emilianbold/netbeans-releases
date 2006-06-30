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
