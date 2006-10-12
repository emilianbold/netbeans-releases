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
package org.netbeans.modules.websvc.core.jaxws.bindings.model.impl;


import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponent;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsComponentFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.GlobalBindings;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsModelImpl extends AbstractDocumentModel<BindingsComponent>
        implements BindingsModel{
    
    private BindingsComponentFactory bcf;
    private GlobalBindings gb;
    
    /** Creates a new instance of BindingsModelImpl */
    /*public BindingsModelImpl(javax.swing.text.Document doc) {
         super(doc);
         bcf = new BindingsComponentFactoryImpl(this);
    }*/
    
    public BindingsModelImpl(ModelSource source){
        super(source);
        bcf = new BindingsComponentFactoryImpl(this);
    }
    
    public void setGlobalBindings(GlobalBindings gbindings){
        assert (gbindings instanceof GlobalBindingsImpl) ;
        gb = GlobalBindingsImpl.class.cast(gbindings);
    }
    
    public BindingsComponent createRootComponent(Element root) {
        if (BindingsQName.JAXWS_NS_URI.equals(root.getNamespaceURI())){
            GlobalBindingsImpl gbindings = new GlobalBindingsImpl(this, root);
            setGlobalBindings(gbindings);
        }
        return gb;
    }
    
    public BindingsComponent createComponent(BindingsComponent parent, Element element) {
        return getFactory().create(element, parent);
    }
    
    public BindingsComponent getRootComponent() {
        return gb;
    }
    
    public GlobalBindings getGlobalBindings() {
        return gb;
    }
    
    public BindingsComponentFactory getFactory() {
        return bcf;
    }
    
    protected ComponentUpdater<BindingsComponent> getComponentUpdater() {
        return null;
    }
    
}
