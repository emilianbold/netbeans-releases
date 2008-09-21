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

/*
 * Created on May 17, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 * @author Ritesh Adval
 *
 *
 */
public class ExtensibilityElementsFolderNode extends FolderNode {

    private Definitions mDef = null;
    private Set<String> mSpecialTargetNamespaces;

    public ExtensibilityElementsFolderNode(Definitions element) {
        this(element, null);
    }

    public ExtensibilityElementsFolderNode(Definitions element, Set<String> specialTargetNamespaces) {
        super(new ExtensibilityElementChildFactory(element, specialTargetNamespaces),
                element, ExtensibilityElement.class);
        mDef = element;
        mSpecialTargetNamespaces = specialTargetNamespaces;
        this.setDisplayName(NbBundle.getMessage(ExtensibilityElementsFolderNode.class,
                "EXTENSIBILITY_ELEMENTS_FOLDER_NODE_NAME"));
    }

    @Override
    public final NewType[] getNewTypes() {
        if (isEditable()) {
            // hack for showing only partnerlink types in partner view.
            // this filters the new types to be of only which are specified in mSpecialTargetNamespaces.
            if (mSpecialTargetNamespaces != null && mSpecialTargetNamespaces.size() > 0) {
                return new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_DEFINITIONS,
                        mSpecialTargetNamespaces.toArray(new String[mSpecialTargetNamespaces.size()])).getNewTypes(mDef);
            }
            return new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_DEFINITIONS).getNewTypes(mDef);
        }
        return new NewType[]{};
    }

    @Override
    public Class getType() {
        return ExtensibilityElement.class;
    }

    public static final class ExtensibilityElementChildFactory extends ChildFactory implements Refreshable {
        private Set<String> specialTargetNS;
        private Definitions def;

        public ExtensibilityElementChildFactory(Definitions definitions, Set<String> specialTargetNamespaces) {
            super();
            specialTargetNS = specialTargetNamespaces;
            this.def = definitions;
        }

        public ExtensibilityElementChildFactory(Definitions definitions) {
            this(definitions, null);
        }

        @Override
        protected boolean createKeys(List toPopulate) {

            List<ExtensibilityElement> list = def.getExtensibilityElements();
            if (specialTargetNS == null) {
                toPopulate.addAll(list);
                return true;
            }
            List<ExtensibilityElement> finalList = new ArrayList<ExtensibilityElement>();
            if (list != null) {
                for (ExtensibilityElement element : list) {
                    if (specialTargetNS.contains(element.getQName().getNamespaceURI())) {
                        continue;
                    }
                    finalList.add(element);
                }
            }
            toPopulate.addAll(finalList);
            return true;
        }

        @Override
        protected Node createNodeForKey(Object key) {
            if (key instanceof WSDLComponent) {
                return NodesFactory.getInstance().create((WSDLComponent) key);
            }
            return null;
        }
        
        

        public void refreshChildren(boolean immediate) {
            refresh(immediate);
        }
    }
}

