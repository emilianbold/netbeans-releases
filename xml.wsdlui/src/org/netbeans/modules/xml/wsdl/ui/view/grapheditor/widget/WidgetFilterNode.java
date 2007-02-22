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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingAndServiceNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementChildNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportSchemaNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ImportWSDLNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ServiceNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ServicePortNewType;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.NewType;

/**
 * Default filter node implementation for all widgets.
 *
 * @author  Nathan Fiedler
 */
public class WidgetFilterNode extends FilterNode {

    private AbstractWidget widget;
    
    /**
     * Creates a new instance of WidgetFilterNode.
     *
     * @param original  the original Node.
     */
    public WidgetFilterNode(Node original) {
        super(original);
    }
    
    public WidgetFilterNode(Node original, AbstractWidget widget) {
        this(original);
        this.widget = widget;
    }

    @Override
    public NewType[] getNewTypes() {
        NewType[] types = super.getNewTypes();
        List<NewType> list = new ArrayList<NewType>();
        Collections.addAll(list, types);
        updateNewTypes(list);
        return list.toArray(new NewType[list.size()]);
    }

    /**
     * Add/remove types from the given list of NewType instance. For
     * instance, the default implementation removes the BindingNewType
     * instance from the list provided by the backing Node (as well as
     * several other NewType classes). Subclasses may add or remove
     * additional types. To prevent removing the default types,
     * override without calling this superclass method.
     *
     * @param  types  list of NewType instances to be updated.
     */
    protected void updateNewTypes(List<NewType> types) {
        ListIterator<NewType> liter = types.listIterator();
        while (liter.hasNext()) {
            NewType type = liter.next();
            if (type instanceof BindingNewType ||
                    type instanceof BindingAndServiceNewType ||
                    type instanceof DocumentationNewType ||
                    type instanceof ExtensibilityElementChildNewType ||
                    type instanceof ImportSchemaNewType ||
                    type instanceof ImportWSDLNewType ||
                    type instanceof ServiceNewType ||
                    type instanceof ServicePortNewType) {
                liter.remove();
            }
        }
    }

    @Override
    public void destroy() throws IOException {
        if (widget != null) {
            widget.deleteComponent();
        }
        super.destroy();
        
    }
}
