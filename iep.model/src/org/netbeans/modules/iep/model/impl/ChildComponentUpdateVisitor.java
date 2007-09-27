/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
