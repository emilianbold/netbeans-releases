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

package org.netbeans.modules.compapp.casaeditor.model.casa.impl;


import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.ReferenceableCasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;


/**
 *
 * @author Nam Nguyen
 * @author rico
 */
public class GlobalReferenceImpl<T extends ReferenceableCasaComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of GlobalReferenceImpl */
    //for use by factory, create from scratch
    public GlobalReferenceImpl(
            T referenced, 
            Class<T> type, 
            CasaComponentImpl parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public GlobalReferenceImpl(
            Class<T> type, 
            CasaComponentImpl parent, 
            String refString){
        super(type, parent, refString);
    }
    
    public T get() {
        CasaComponentImpl wparent = CasaComponentImpl.class.cast(getParent());
        if (super.getReferenced() == null) {
            String localName = getLocalName();
            T target = null;
            CasaModel model = wparent.getModel();
            target = new FindReferencedVisitor<T>(model.getRootComponent()).
                    find(localName, getType());
                
            setReferenced(target);
        }
        return getReferenced();
    }
    
    public String getEffectiveNamespace() {
        
       return Constants.EMPTY_STRING; //http://java.sun.com/xml/ns/casa"; // TMP
    }
}
