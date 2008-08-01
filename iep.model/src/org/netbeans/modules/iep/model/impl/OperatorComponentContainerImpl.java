package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.InvokeStreamOperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.TableInputOperatorComponent;
import org.w3c.dom.Element;

public class OperatorComponentContainerImpl extends ComponentImpl implements OperatorComponentContainer {

    public OperatorComponentContainerImpl(IEPModel model) {
        super(model);
        setType("/IEP/Model/Plan|Operators"); //NOI18N
    }

    public OperatorComponentContainerImpl(IEPModel model, Element element) {
        super(model, element);
        setType("/IEP/Model/Plan|Operators"); //NOI18N
    }
    
     public IEPComponent createChild(Element childEl) {
            IEPComponent child = null;
            
            if (childEl != null) {
                String localName = childEl.getLocalName();
                if (localName == null || localName.length() == 0) {
                        localName = childEl.getTagName();
                }
                if (localName.equals(COMPONENT_CHILD)) {
                     String type = childEl.getAttribute(OperatorComponent.TYPE_PROPERTY);
                     
                     if(type.endsWith("TableInput")) {
                        child = new TableInputOperatorComponentImpl(getModel(), childEl);
                     } else if(type.endsWith("Input")) {
                         child = new InputOperatorComponentImpl(getModel(), childEl);
                     } else if(type.endsWith("Output")) {
                         child = new OutputOperatorComponentImpl(getModel(), childEl);
                     } else if(type.endsWith("InvokeStream")) {
                         child = new InvokeStreamOperatorComponentImpl(getModel(), childEl);
                     } else if  (type.endsWith("ExternalTablePollingStream")) {
                         child = new ExternalTablePollingStreamOperatorComponentImpl(getModel(), childEl);
                     } else if  (type.endsWith("ReplayStream")) {
                         child = new ReplayStreamOperatorComponentImpl(getModel(), childEl);
                     } else {
                        child = new OperatorComponentImpl(getModel(), childEl);
                     }
                } else {
                    child = super.createChild(childEl);
                }
            }
            
            return child;
        }
    
    public void accept(IEPVisitor visitor) {
        visitor.visitOperatorComponentContainer(this);
    }
     
    public void addOperatorComponent(OperatorComponent operator) {
        addChildComponent(operator);
    }

    public List<OperatorComponent> getAllOperatorComponent() {
        return getChildren(OperatorComponent.class);
    }

    public void removeOperatorComponent(OperatorComponent operator) {
        removeChildComponent(operator);
    }

    public OperatorComponent findChildComponent(String id) {
        if(id == null) {
            return null;
        }
        
        OperatorComponent child = null;
        List<OperatorComponent> children = getAllOperatorComponent();
        Iterator<OperatorComponent> it = children.iterator();
        while(it.hasNext()) {
            OperatorComponent c = it.next();
            String cid = c.getId();
            if(id.equals(cid)) {
                child = c;
                break;
            }
        }
        
        return child;
        
    }
    
    public OperatorComponent findOperator(String name) {
        if(name == null) {
            return null;
        }
        
        OperatorComponent child = null;
        List<OperatorComponent> children = getAllOperatorComponent();
        Iterator<OperatorComponent> it = children.iterator();
        while(it.hasNext()) {
            OperatorComponent c = it.next();
            String nameProp = c.getDisplayName();
            if(name.equals(nameProp)) {
                child = c;
                break;
            }
        }
        
        return child;
        
        
    }
    
    public List<OperatorComponent> findOutputOperator(OperatorComponent operator) {
        List<OperatorComponent> outputOperatorList = new ArrayList<OperatorComponent>();
        
        List<OperatorComponent> allOps = getAllOperatorComponent();
        Iterator<OperatorComponent> it = allOps.iterator();
        while(it.hasNext()) {
            OperatorComponent oc = it.next();
            
            if(!oc.equals(operator)) {
                if(oc.getInputOperatorList().contains(operator)) {
                    outputOperatorList.add(oc);
                }
            }
        }
        
        return outputOperatorList;
    }

    public List<InvokeStreamOperatorComponent> getInvokeStreamOperatorComponent() {
        List<InvokeStreamOperatorComponent> invokeOps = new ArrayList<InvokeStreamOperatorComponent>();
        List<OperatorComponent> ops =  getAllOperatorComponent();
        Iterator<OperatorComponent> it = ops.iterator();
        while(it.hasNext()) {
            OperatorComponent op = it.next();
            
            if(op instanceof InvokeStreamOperatorComponent) {
                invokeOps.add((InvokeStreamOperatorComponent) op);
            }
        }
        
        return invokeOps;
        
    }
        
    public List<TableInputOperatorComponent> getTableInputOperatorComponent() {
        List<TableInputOperatorComponent> tableInputOps = new ArrayList<TableInputOperatorComponent>();
        List<OperatorComponent> ops =  getAllOperatorComponent();
        Iterator<OperatorComponent> it = ops.iterator();
        while(it.hasNext()) {
            OperatorComponent op = it.next();
            
            if(op instanceof TableInputOperatorComponent) {
                tableInputOps.add((TableInputOperatorComponent) op);
            }
        }
        
        return tableInputOps;
        
    }
}
