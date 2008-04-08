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
package org.netbeans.modules.bpel.model.impl;

import java.util.Collection;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.bpel.model.spi.NewBpelModelListener;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Lookup;


/**
 * @author ads
 */
public class BpelModelFactoryImpl extends AbstractModelFactory<BpelModel>
    implements BpelModelFactory
{

    @Override
    public BpelModel getModel(ModelSource source)
    {
        return super.getModel( source );
    }
    
    protected BpelModel createModel( ModelSource source ) {
        BpelModel newBpelModel = new BpelModelImpl( source );
        //
        // Notify all listeners about creation of the new BPEL model
        Collection<? extends NewBpelModelListener> listeners = 
                Lookup.getDefault().lookupAll(NewBpelModelListener.class);
        if (listeners != null) {
            for (NewBpelModelListener listener : listeners) {
                listener.bpelModelCreated();
            }
        }
        //
        return newBpelModel;
    }

}
