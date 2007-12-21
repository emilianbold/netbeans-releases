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
package org.netbeans.modules.bpel.core.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;

import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;

/**
 * @author ads
 *
 */
class WeakTriggerListener implements PropertyChangeListener, ComponentListener {
    
    WeakTriggerListener( ExternalModelsValidationTrigger trigger ) {
        myTrigger = new WeakReference<ExternalModelsValidationTrigger>( trigger );
        myQueue = new ReferenceQueue<ExternalModelsValidationTrigger>();
    }

    public void propertyChange( PropertyChangeEvent evt ) {
        handle();
    }

    public void childrenAdded( ComponentEvent evt ) {
        handle();
    }

    public void childrenDeleted( ComponentEvent evt ) {
        handle();
    }

    public void valueChanged( ComponentEvent evt ) {
        handle();
    }
    
    private ExternalModelsValidationTrigger getTrigger() {
        return myTrigger.get();
    }
    
    private void handle() {
        Reference<? extends ExternalModelsValidationTrigger> ref = null;
        while( (ref = myQueue.poll())!= null ) {
            if ( ref == myTrigger ) {
                removeModelListener();
            }
        }
        if ( getTrigger()!= null ) {
            getTrigger().changeHappened();
        }
    }
    
    private void removeModelListener() {
        List<SchemaModel> schemaModels = SchemaModelFactory.getDefault().getModels();
        for( Model model : schemaModels ) {
            removeModelListener(model);
        }
        List<WSDLModel> wsdlModels = WSDLModelFactory.getDefault().getModels();
        for( Model model : wsdlModels ) {
            removeModelListener(model);
        }
    }

    private void removeModelListener( Model model ) {
        if ( model != null ) {
            model.removeComponentListener( this );
            model.removePropertyChangeListener( this );
        }
    }
    
    private WeakReference<ExternalModelsValidationTrigger> myTrigger;
    private ReferenceQueue<ExternalModelsValidationTrigger> myQueue;
}
