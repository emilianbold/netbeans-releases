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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigModelFactory extends AbstractModelFactory<JSFConfigModel>{
    
    private static JSFConfigModelFactory modelFactory = null;
    
    private static Object instanceSyncPoint = new Object();
    
    /** Creates a new instance of JSFConfigModelFactory */
    public JSFConfigModelFactory() {
    }
    
    public static JSFConfigModelFactory getInstance(){
        if (modelFactory == null){
            synchronized(instanceSyncPoint) {
                JSFConfigModelFactory _modelFactory = modelFactory;
                if (_modelFactory == null){
                    modelFactory = new JSFConfigModelFactory();
                }
            }
        }
        return modelFactory;
    }
    
    protected JSFConfigModel createModel(ModelSource source) {
        return new JSFConfigModelImpl(source);
    }
    
    public JSFConfigModel getModel(ModelSource source) {
        return (JSFConfigModel) super.getModel(source);
    }
}
