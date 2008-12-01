/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom.visitor;

import org.netbeans.modules.maven.model.pom.*;
//import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
//import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a domain component.
 * 
 * @author mkleint
 */
public class ChildComponentUpdateVisitor<T extends POMComponent> //implements SCAComponentVisitor, 
        extends DefaultVisitor
        implements ComponentUpdater<T> {
    
    private Operation operation;
    private POMComponent parent;
    private int index;
    private boolean canAdd = false;
    
    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }
    
    public boolean canAdd(POMComponent target, Component child) {
        if (!(child instanceof POMComponent)) return false;
        update(target, (POMComponent) child, null);
        return canAdd;
    }
    
    public void update(POMComponent target, POMComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    
    public void update(POMComponent target, POMComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.operation = operation;
        this.index = index;
        child.accept(this);
    }
    
    /*
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
    
   
    private void update(org.netbeans.modules.xml.wsdl.model.Operation child) {
        if (parent instanceof PortType) {
            PortType target = (PortType)parent;
            if (operation == Operation.ADD) {
                addChild(target.OPERATION_PROPERTY, child);
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
    */
}
