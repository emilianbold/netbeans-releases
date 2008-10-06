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

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingNewType;
import org.openide.nodes.ChildFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;

/**
 * @author Ritesh Adval
 *
 * 
 */
public class BindingFolderNode extends FolderNode {

    private Definitions mDef = null;
    
    public BindingFolderNode(Definitions element, ChildFactory factory) {
        super(factory, element, Binding.class);
         mDef = element;
        this.setDisplayName(NbBundle.
                    getMessage(BindingFolderNode.class, 
                               "BINDING_FOLDER_NODE_NAME"));
        BADGE_ICON  = ImageUtilities.loadImage
        ("org/netbeans/modules/xml/wsdl/ui/view/resources/binding_badge.png");
    }

    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return new NewType[] {new BindingNewType(mDef)};
        }
        return new NewType[] {};
    }

    public static final class BindingFolderChildren extends GenericWSDLComponentChildren<Definitions> {
        public BindingFolderChildren(Definitions definitions) {
            super(definitions);
        }

        @Override
        public final Collection<Binding> getKeys() {
            Definitions def = getWSDLComponent();
            return def.getBindings();
        }
    }
    
    @Override
    public Class getType() {
        return Binding.class;
    }
    
    
}


