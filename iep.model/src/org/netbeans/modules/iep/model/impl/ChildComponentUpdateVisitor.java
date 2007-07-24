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

package org.netbeans.modules.iep.model.impl;


import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a WSDL component.
 * 
 * @author Nam Nguyen
 */
public class ChildComponentUpdateVisitor<T extends IEPComponent> implements
		IEPVisitor, ComponentUpdater<T> {

	private Operation operation;

	private IEPComponent parent;

	private int index;

	private boolean canAdd = false;

	/**
	 * Creates a new instance of ChildComponentUpdateVisitor
	 */
	public ChildComponentUpdateVisitor() {
	}

	public boolean canAdd(IEPComponent target, Component child) {
		if (!(child instanceof IEPComponent))
			return false;
		update(target, (IEPComponent) child, null);
		return canAdd;
	}

	public void update(IEPComponent target, IEPComponent child,
			Operation operation) {
		update(target, child, -1, operation);
	}

	public void update(IEPComponent target, IEPComponent child, int index,
			Operation operation) {
		assert target != null;
		assert child != null;

		this.parent = target;
		this.operation = operation;
		this.index = index;
		child.accept(this);
	}

	@SuppressWarnings("unchecked")
	private void addChild(String eventName, DocumentComponent child) {
		((AbstractComponent) parent).insertAtIndex(eventName, child, index);
	}

	@SuppressWarnings("unchecked")
	private void removeChild(String eventName, DocumentComponent child) {
		((AbstractComponent) parent).removeChild(eventName, child);
	}

	private void checkOperationOnUnmatchedParent() {
		if (operation != null) {
			// note this unmatch should be caught by validation,
			// we don't want the UI view to go blank on invalid but still
			// well-formed document
			// throw new IllegalArgumentException("Unmatched parent-child
			// components"); //NO18N
		} else {
			canAdd = false;
		}
	}

    public void visitComponent(org.netbeans.modules.iep.model.Component component) {
        if (parent instanceof Component) {
            if (operation == Operation.ADD) {
                    addChild(org.netbeans.modules.iep.model.Component.COMPONENT_CHILD, component);
            } else if (operation == Operation.REMOVE) {
                    removeChild(org.netbeans.modules.iep.model.Component.COMPONENT_CHILD, component);
            } else if (operation == null) {
                    canAdd = true;
            }
        }
    }

    public void visitProperty(Property property) {
        if (parent instanceof Component) {
            if (operation == Operation.ADD) {
                    addChild(org.netbeans.modules.iep.model.Component.COMPONENT_CHILD, property);
            } else if (operation == Operation.REMOVE) {
                    removeChild(org.netbeans.modules.iep.model.Component.COMPONENT_CHILD, property);
            } else if (operation == null) {
                    canAdd = true;
            }
        }
    }
    
    
        
}
