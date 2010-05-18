/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.io.IOException;
import java.lang.ref.WeakReference;
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

    private WeakReference<AbstractWidget> widgetWeakRef;
    
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
        this.widgetWeakRef = new WeakReference<AbstractWidget>(widget);
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
        if (widgetWeakRef != null && widgetWeakRef.get() != null) {
            widgetWeakRef.get().deleteComponent();
        }

        super.destroy();
        
    }
}
