package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.w3c.dom.Element;

public class OperatorComponentContainerImpl extends ComponentImpl implements OperatorComponentContainer {

    public OperatorComponentContainerImpl(IEPModel model) {
        super(model);
        setType("/IEP/Model/Plan|Operators"); //NOI18N
    }

    public OperatorComponentContainerImpl(IEPModel model, Element element) {
        super(model, element);
    }

    private static boolean isWsOperator(String componentType) {
        boolean ret = componentType.equals("/IEP/Input/StreamInput") ||
                componentType.equals("/IEP/Output/StreamOutput") ||
                componentType.equals("/IEP/Output/BatchedStreamOutput") ||
                componentType.equals("/IEP/Output/RelationOutput") ||
                componentType.equals("/IEP/Operator/InvokeService");
        return ret;
    }
    
    @Override
    public IEPComponent createChild(Element childEl) {
        IEPComponent child = null;

        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                localName = childEl.getTagName();
            }
            if (localName.equals(COMPONENT_CHILD)) {
                String type = childEl.getAttribute(OperatorComponent.TYPE_PROPERTY);

                if (isWsOperator(type)) {
                    child = new WsOperatorComponentImpl(getModel(), childEl);
                } else {
                    child = new OperatorComponentImpl(getModel(), childEl);
                }
            } else {
                child = super.createChild(childEl);
            }
        }

        return child;
    }

    @Override
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
        if (id == null) {
            return null;
        }

        OperatorComponent child = null;
        List<OperatorComponent> children = getAllOperatorComponent();
        Iterator<OperatorComponent> it = children.iterator();
        while (it.hasNext()) {
            OperatorComponent c = it.next();
            String cid = c.getString(PROP_ID);
            if (id.equals(cid)) {
                child = c;
                break;
            }
        }
        return child;
    }

    public OperatorComponent findOperator(String name) {
        if (name == null) {
            return null;
        }

        OperatorComponent child = null;
        List<OperatorComponent> children = getAllOperatorComponent();
        Iterator<OperatorComponent> it = children.iterator();
        while (it.hasNext()) {
            OperatorComponent c = it.next();
            String nameProp = c.getString(PROP_NAME);
            if (name.equals(nameProp)) {
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
        while (it.hasNext()) {
            OperatorComponent oc = it.next();

            if (!oc.equals(operator)) {
                if (oc.getInputOperatorList().contains(operator) || oc.getStaticInputList().contains(operator)) {
                    outputOperatorList.add(oc);
                }
            }
        }

        return outputOperatorList;
    }
}
