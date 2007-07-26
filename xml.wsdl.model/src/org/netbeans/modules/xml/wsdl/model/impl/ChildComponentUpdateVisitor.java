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

import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a WSDL component.
 * @author Nam Nguyen
 */
public class ChildComponentUpdateVisitor<T extends WSDLComponent> implements WSDLVisitor, ComponentUpdater<T> {
    
    private Operation operation;
    private WSDLComponent parent;
    private int index;
    private boolean canAdd = false;
    
    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }
    
    public boolean canAdd(WSDLComponent target, Component child) {
        if (!(child instanceof WSDLComponent)) return false;
        update(target, (WSDLComponent) child, null);
        return canAdd;
    }
    
    public void update(WSDLComponent target, WSDLComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    
    public void update(WSDLComponent target, WSDLComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.operation = operation;
        this.index = index;
        child.accept(this);
    }
    
    private void addChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).insertAtIndex(eventName, child, index);
    }
    
    private void removeChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).removeChild(eventName, child);
    }
    
    public void visit(Definitions child) {
        checkOperationOnUnmatchedParent();
    }

    public void visit(Types child) {
        if (parent instanceof Definitions) {
            if (operation == Operation.ADD) {
                addChild(Definitions.TYPES_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(Definitions.TYPES_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Binding child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                target.addBinding(child);
            } else if (operation == Operation.REMOVE) {
                target.removeBinding(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Message child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                target.addMessage(child);
            } else if (operation == Operation.REMOVE) {
                target.removeMessage(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Service child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                target.addService(child);
            } else if (operation == Operation.REMOVE) {
                target.removeService(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(PortType child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                target.addPortType(child);
            } else if (operation == Operation.REMOVE) {
                target.removePortType(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Import child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                target.addImport(child);
            } else if (operation == Operation.REMOVE) {
                target.removeImport(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Port child) {
        if (parent instanceof Service) {
            Service target = (Service)parent;
            if (operation == Operation.ADD) {
                target.addPort(child);
            } else if (operation == Operation.REMOVE) {
                target.removePort(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingOperation child) {
        if (parent instanceof Binding) {
            Binding target = (Binding)parent;
            if (operation == Operation.ADD) {
                target.addBindingOperation(child);
            } else if (operation == Operation.REMOVE) {
                target.removeBindingOperation(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingInput child) {
        if (parent instanceof BindingOperation) {
            if (operation == Operation.ADD) {
                addChild(BindingOperation.BINDING_INPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(BindingOperation.BINDING_INPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingOutput child) {
        if (parent instanceof BindingOperation) {
            if (operation == Operation.ADD) {
                addChild(BindingOperation.BINDING_OUTPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(BindingOperation.BINDING_OUTPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingFault child) {
        if (parent instanceof BindingOperation) {
            BindingOperation target = (BindingOperation)parent;
            if (operation == Operation.ADD) {
                target.addBindingFault(child);
            } else if (operation == Operation.REMOVE) {
                target.removeBindingFault(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Part child) {
        if (parent instanceof Message) {
            Message target = (Message)parent;
            if (operation == Operation.ADD) {
                target.addPart(child);
            } else if (operation == Operation.REMOVE) {
                target.removePart(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Documentation doc) {
        if (operation == Operation.ADD) {
            addChild(WSDLComponent.DOCUMENTATION_PROPERTY, doc);
        } else if (operation == Operation.REMOVE) {
            removeChild(WSDLComponent.DOCUMENTATION_PROPERTY, doc);
        } else if (operation == null) {
            canAdd = true;
        }
    }
    
    public void visit(Output child) {
        if (parent instanceof RequestResponseOperation || 
            parent instanceof SolicitResponseOperation ||
            parent instanceof NotificationOperation) 
        {
            if (operation == Operation.ADD) {
                addChild(org.netbeans.modules.xml.wsdl.model.Operation.OUTPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(org.netbeans.modules.xml.wsdl.model.Operation.OUTPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Input child) {
        if (parent instanceof OneWayOperation ||
            parent instanceof RequestResponseOperation ||
            parent instanceof SolicitResponseOperation) 
        {
            if (operation == Operation.ADD) {
                addChild(org.netbeans.modules.xml.wsdl.model.Operation.INPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(org.netbeans.modules.xml.wsdl.model.Operation.INPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Fault child) {
        if (parent instanceof org.netbeans.modules.xml.wsdl.model.Operation) {
            org.netbeans.modules.xml.wsdl.model.Operation target = 
                (org.netbeans.modules.xml.wsdl.model.Operation)parent;
            boolean operationWithFaults = 
                parent instanceof RequestResponseOperation || 
                parent instanceof SolicitResponseOperation;

            if (operationWithFaults && operation == Operation.ADD) {
                target.addFault(child);
            } else if (operation == Operation.REMOVE) {
                target.removeFault(child);
            } else if (operation == null) {
                canAdd = operationWithFaults;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    private void update(org.netbeans.modules.xml.wsdl.model.Operation child) {
        if (parent instanceof PortType) {
            PortType target = (PortType)parent;
            if (operation == Operation.ADD) {
                target.addOperation(child);
            } else if (operation == Operation.REMOVE) {
                target.removeOperation(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
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
            ComponentUpdater<ExtensibilityElement> updater = target.getComponentUpdater();
            if (operation != null) {
                updater.update(target, child, index, operation);
            } else {
                canAdd = false;
                if (updater instanceof ComponentUpdater.Query) {
                    canAdd = ((ComponentUpdater.Query) updater).canAdd(target, child);
                } 
            }
        } else {
            if (operation == Operation.ADD) {
                parent.addExtensibilityElement(child);
            } else if (operation == Operation.REMOVE) {
                parent.removeExtensibilityElement(child);
            } else if (operation == null) {
                canAdd = true;
                if (child instanceof ExtensibilityElement.ParentSelector) {
                    canAdd = ((ExtensibilityElement.ParentSelector)child).canBeAddedTo(parent);
                }
            }
        }
    }

    private void checkOperationOnUnmatchedParent() {
        if (operation != null) {
            // note this unmatch should be caught by validation, 
            // we don't want the UI view to go blank on invalid but still well-formed document
            //throw new IllegalArgumentException("Unmatched parent-child components"); //NO18N
        } else {
            canAdd = false;
        }
    }
}
