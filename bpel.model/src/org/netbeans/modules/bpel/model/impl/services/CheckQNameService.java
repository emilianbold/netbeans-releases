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
package org.netbeans.modules.bpel.model.impl.services;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads This visitor checks for correctness of name attribute. At least -
 *         we check for absence of spaces in names.
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class CheckQNameService extends InnerEventDispatcherAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        if (event instanceof PropertyUpdateEvent) {
            PropertyUpdateEvent ev = (PropertyUpdateEvent) event;
            if ( ev.getParent()== null || ev.getParent().getModel().inSync() ){
                return false;
            }
            Object newValue = ev.getNewValue();
            
            return newValue instanceof QName;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void preDispatch( ChangeEvent event ) throws VetoException {
        assert event instanceof PropertyUpdateEvent;

        PropertyUpdateEvent ev = (PropertyUpdateEvent) event;

        QName newValue = (QName) ev.getNewValue();
        String ns = newValue.getNamespaceURI();
        String localPart = newValue.getLocalPart();
        boolean needThrow = false;
        
        String str = "";
        try {
            new URI( ns );
        }
        catch ( URISyntaxException e ){
            needThrow = true;
            str = Utils.getResourceString(Utils.BAD_URI_VALUE, ns );
        }

        
        if (!Utils.checkNCName(localPart)) {
            needThrow = true;
            str = str + " " + Utils.getResourceString(Utils.BAD_NCNAME_VALUE,
                    localPart);
        }
        
        if ( needThrow ){
            throw new VetoException(str, event);
        }
    }
}
