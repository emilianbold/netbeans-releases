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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.locator.api.DepResolverException;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.xam.locator.api.DependencyResolver;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactoryProvider;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.DocumentComponent;
import org.netbeans.modules.xml.xam.xdm.AbstractXDMModel;
import org.netbeans.modules.xml.xam.xdm.ChangedNodes;
import org.netbeans.modules.xml.xam.xdm.ComponentUpdater;
import org.netbeans.modules.xml.xam.xdm.SyncUnit;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.openide.ErrorManager;
import org.w3c.dom.Element;
import org.openide.util.Lookup;

/**
 *
 * @author rico
 * @author Nam Nguyen
 */
public class WSDLModelImpl extends AbstractXDMModel<WSDLComponent> implements WSDLModel{
    private Definitions definitions;
    private WSDLComponentFactory wcf;
    
    /** Creates a new instance of WSDLModelImpl */
    public WSDLModelImpl(javax.swing.text.Document doc) {
        super(doc);
        wcf = new WSDLComponentFactoryImpl(this);
        initializeElementRegistry();
    }
    
    public WSDLModelImpl(ModelSource source) {
        super(source);
        wcf = new WSDLComponentFactoryImpl(this);
        initializeElementRegistry();
    }
    
    public WSDLComponent createRootComponent(Element root) {
        DefinitionsImpl definitions = new DefinitionsImpl(this, root);
        setDefinitions(definitions);
        return definitions;
    }
    
    public WSDLComponent getRootComponent() {
        return definitions;
    }
    
    public WSDLComponent createComponent(WSDLComponent parent, Element element) {
        return getFactory().create(element, parent);
    }
    
    protected ComponentUpdater<WSDLComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<WSDLComponent>();
    }
    
    public WSDLComponentFactory getFactory() {
        return wcf;
    }
    
    public void setDefinitions(Definitions def){
        assert (def instanceof DefinitionsImpl) ;
        definitions = DefinitionsImpl.class.cast(def);
    }
    
    public Definitions getDefinitions(){
        return definitions;
    }

    private void initializeElementRegistry(){
        //TODO listen on changes when we have external extensions
        Lookup.Result results = Lookup.getDefault().lookup(new Lookup.Template(ElementFactoryProvider.class));
        Collection services = results.allInstances();
        Iterator iterator = services.iterator();
        while(iterator.hasNext()){
            ElementFactoryProvider service = (ElementFactoryProvider)iterator.next();
            getElementRegistry().register(service);
        }
    }
    
    ElementFactoryRegistry getElementRegistry() {
        return ElementFactoryRegistry.getDefault();
    }
    
    public List<WSDLModel> getImportedWSDLModels() {
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        Collection<Import> imports = getDefinitions().getImports();
        for (Import i:imports) {
            try {
                DependencyResolver nr = getModelSource().getResolver();
                ModelSource ms = null;
                try {
                    ms = nr.getModelSource(new URI(i.getLocation()));
                } catch(DepResolverException nse) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nse);
                    ms = nr.getModelSource(new URI(i.getNamespaceURI()));
                }
                WSDLModel m = (ms == null) ? null : WSDLModelFactory.getDefault().getModel(ms);
                if (m != null) {
                    ret.add(m);
                }
            } catch(Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return ret;
    }

    public WSDLModel findWSDLModel(String namespace) {
        WSDLModel ret = null;
        String targetNamespace = getDefinitions().getTargetNamespace();
        if (namespace == null || namespace.equals(targetNamespace)) {
            ret = this;
        } else {
            List<WSDLModel> models = getImportedWSDLModels();
            for (WSDLModel m : models) {
                ret = m.findWSDLModel(namespace);
                if (ret != null) break;
            }
        }
        return ret;
    }

    public <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type) {
        return findComponentByName(name, type, false);
    }
    
    public <T extends ReferenceableWSDLComponent> T findComponentByName(
            String name, Class<T> type, boolean global) {
        
        ReferenceableWSDLComponent found = null;
        Collection<WSDLModel> targetModels = new ArrayList<WSDLModel>();
        targetModels.add(this);
        if (global) {
            targetModels.addAll(getImportedWSDLModels());
        }
        for (WSDLModel m : targetModels) {
            FindReferencedVisitor finder = new FindReferencedVisitor(m.getDefinitions());
            found = finder.find(name, type);
            if (found != null) {
                break;
            }
        }
        return type.cast(found);
    }
    
    public Set<QName> getQNames() {
        return getElementRegistry().getKnownQNames();
    }

    public Set<String> getElementNames() {
        return getElementRegistry().getKnownElementNames();
    }
    
    protected ChangedNodes findChangedNodes(List<Node> pathToRoot) {
        ChangedNodes change = super.findChangedNodes(pathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (! (parentComponent.getModel() instanceof WSDLModel)) {
            getElementRegistry().addEmbeddedModelQNames((AbstractModel)parentComponent.getModel());
            change = super.findChangedNodes(pathToRoot);
        }
        change.setParentComponent(parentComponent);
        return change;
    }
    
    protected SyncUnit fillSyncOrder(ChangedNodes changes, SyncUnit unit) {
        unit = super.fillSyncOrder(changes, unit);
        SyncUnit reviewed = new SyncReviewVisitor().review(unit);
        return reviewed;
    }
    
    public AbstractComponent findComponent(
            AbstractComponent current,
            List<org.netbeans.modules.xml.xdm.nodes.Element> pathFromRoot, 
            int iCurrent) {
        
        if (current instanceof ExtensibilityElement.Embedder) {
            ExtensibilityElement.Embedder emb = (ExtensibilityElement.Embedder) current;
            AbstractXDMModel axm = (AbstractXDMModel) emb.getEmbeddedModel();
            AbstractComponent embedded = (AbstractComponent) axm.getRootComponent();
            return axm.findComponent(embedded, pathFromRoot, iCurrent);
        } else {
            return super.findComponent(current, pathFromRoot, iCurrent);
        }
    }
}
