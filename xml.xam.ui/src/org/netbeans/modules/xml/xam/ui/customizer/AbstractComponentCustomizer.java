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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.io.IOException;
import java.util.Iterator;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

/**
 * A customizer of XAM components.
 *
 * @author  Ajit Bhate
 * @author  Nathan Fiedler
 */
public abstract class AbstractComponentCustomizer<T extends Component>
        extends AbstractCustomizer {
    private transient T component;
	private transient MessageDisplayer messageDisplayer;
    /**
     * Creates a new instance of AbstractComponentCustomizer.
     *
     * @param  component  XAM component to customize.
     */
    public AbstractComponentCustomizer(T component) {
        this.component = component;
		messageDisplayer = new MessagePanel();
		messageDisplayer.clear();
    }

    public boolean isEditable() {
        return XAMUtils.isWritable(component.getModel());
    }

    /**
     * Guarantees transaction
     */
    public void apply() throws IOException {
        Model model = component.getModel();
        // It is possible that current component is not in model,
        // or a transaction already exists, so don't start transaction.
        boolean startTransaction = model != null && !model.isIntransaction();
        if (startTransaction) {
            model.startTransaction();
        }
        try {
            applyChanges();
            setSaveEnabled(false);
            setResetEnabled(false);
        } finally {
            if (startTransaction) {
                model.endTransaction();
            }
        }
    }

    /**
     * Returns the XAM component being customized.
     *
     * @return  customized component.
     */
    protected T getModelComponent() {
        return component;
    }

    /**
     * Indicates if the given name (possibly provided by the user) is a
     * valid name for this component. Ensures that the name is unique
     * among the siblings of this component, that are of the same type.
     *
     * @param  name  name to validate.
     * @return  true if name is okay, false otherwise.
     */
    protected boolean isNameValid(String name) {
        T comp = getModelComponent();
        if (!(comp instanceof Nameable)) {
            return true;
        }
        if (name == null || name.trim().length() == 0) {
            return false;
        }
        Iterator iter = comp.getParent().getChildren().iterator();
        while (iter.hasNext()) {
            Object child = iter.next();
            if (child instanceof Nameable) {
                Nameable named = (Nameable) child;
                if (name.equals(named.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected abstract void applyChanges() throws IOException;

	protected MessageDisplayer getMessageDisplayer()
	{
		return messageDisplayer;
	}
}
