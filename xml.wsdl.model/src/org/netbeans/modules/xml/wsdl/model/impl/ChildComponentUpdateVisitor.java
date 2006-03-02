/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.xdm.ComponentUpdater;

/**
 * Visitor to add or remove a child of a WSDL component.
 * @author Nam Nguyen
 */
class ChildComponentUpdateVisitor<T extends WSDLComponent> implements WSDLVisitor, ComponentUpdater<T> {
    
    private Operation operation;
    private T parent;
    private int index;
    
    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }
    
    public void update(T target, T child, Operation operation) {
        update(target, child, -1, operation);
    }
    
    public void update(T target, T child, int index, Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.operation = operation;
        this.index = index;
        child.accept(this);
    }
    
    public void visit(Definitions child) {
        assert false; //should never happen
    }
    
    public void visit(Types child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            target.setTypes(child);
        } else {
            assert false;
        }
    }
    
    public void visit(Binding child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addBinding(child);
            } else {
                target.removeBinding(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(Message child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addMessage(child);
            } else {
                target.removeMessage(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(Service child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addService(child);
            } else {
                target.removeService(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(PortType child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addPortType(child);
            } else {
                target.removePortType(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(Import child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addImport(child);
            } else {
                target.removeImport(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(Port child) {
        if (parent instanceof Service) {
            Service target = (Service)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addPort(child);
            } else {
                target.removePort(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(BindingOperation child) {
        if (parent instanceof Binding) {
            Binding target = (Binding)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addBindingOperation(child);
            } else {
                target.removeBindingOperation(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(BindingInput child) {
        if (parent instanceof BindingOperation) {
            BindingOperation target = (BindingOperation)parent;
            target.setBindingInput(child);
        } else {
            assert false;
        }
    }
    
    public void visit(BindingOutput child) {
        if (parent instanceof BindingOperation) {
            BindingOperation target = (BindingOperation)parent;
            target.setBindingOutput(child);
        } else {
            assert false;
        }
    }
    
    public void visit(BindingFault child) {
        if (parent instanceof BindingOperation) {
            BindingOperation target = (BindingOperation)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addBindingFault(child);
            } else {
                target.removeBindingFault(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(Part child) {
        if (parent instanceof Message) {
            Message target = (Message)parent;
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                target.addPart(child);
            } else {
                target.removePart(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(Documentation doc) {
        parent.setDocumentation(doc);
    }
    
    public void visit(Output child) {
        if (parent instanceof RequestResponseOperation) {
            RequestResponseOperation target = (RequestResponseOperation)parent;
            target.setOutput(child);
        } else if (parent instanceof SolicitResponseOperation) {
            SolicitResponseOperation target = (SolicitResponseOperation)parent;
            target.setOutput(child);
        } else if (parent instanceof NotificationOperation) {
            NotificationOperation target = (NotificationOperation)parent;
            target.setOutput(child);
        } else {
            assert false;
        }
    }
    
    public void visit(Input child) {
        if (parent instanceof OneWayOperation) {
            OneWayOperation target = (OneWayOperation)parent;
            target.setInput(child);
        } else if (parent instanceof RequestResponseOperation) {
            RequestResponseOperation target = (RequestResponseOperation)parent;
            target.setInput(child);
        } else if (parent instanceof SolicitResponseOperation) {
            SolicitResponseOperation target = (SolicitResponseOperation)parent;
            target.setInput(child);
        } else {
            assert false;
        }
    }
    
    public void visit(Fault child) {
        if (parent instanceof org.netbeans.modules.xml.wsdl.model.Operation) {
            org.netbeans.modules.xml.wsdl.model.Operation target = 
                    (org.netbeans.modules.xml.wsdl.model.Operation)parent;
            if (operation.equals(Operation.ADD)) {
                target.addFault(child);
            } else {
                target.removeFault(child);
            }
        } else {
            assert false;
        }
    }
    
    private void update(org.netbeans.modules.xml.wsdl.model.Operation child) {
        if (parent instanceof PortType) {
            PortType target = (PortType)parent;
            if (operation.equals(Operation.ADD)) {
                target.addOperation(child);
            } else {
                target.removeOperation(child);
            }
        } else {
            assert false;
        }
    }
    
    public void visit(NotificationOperation child) {
        update(child);
    }
    
    public void visit(SolicitResponseOperation child) {
        update(child);
    }

    public void visit(RequestResponseOperation child) {
        update(child);
    }
    
    public void visit(OneWayOperation child) {
        update(child);
    }
    
    public void visit(ExtensibilityElement child) {
        if (parent instanceof ExtensibilityElement.UpdaterProvider) {
            ExtensibilityElement.UpdaterProvider target = (ExtensibilityElement.UpdaterProvider) parent;
            target.getComponentUpdater().update(target, child, index, operation);
        } else {
            if (operation.equals(ComponentUpdater.Operation.ADD)) {
                parent.addExtensibilityElement(child);
            } else {
                parent.removeExtensibilityElement(child);
            }
        }
    }
}
