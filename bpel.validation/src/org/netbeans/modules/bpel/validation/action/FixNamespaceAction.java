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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.bpel.validation.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;

import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.bpel.model.api.BpelModel;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.09.25
 */
public final class FixNamespaceAction extends FixImportAction {

    public FixNamespaceAction() {
        super(i18n(FixNamespaceAction.class, "CTL_Fix_Namespace_Action"), i18n(FixNamespaceAction.class, "TLT_Fix_Namespace_Action")); // NOI18N
    }

    public void actionPerformed(ActionEvent event) {
        Node node = getSelectedNode();
//out();
//out("node: " + node);

        if (node == null) {
            return;
        }
        Controller controller = node.getLookup().lookup(Controller.class);

        if (controller == null) {
            return;
        }
//out("controller: " + controller);

        if ( !(controller.getModel() instanceof BpelModel)) {
            return;
        }
        if ( !checkValidationError(controller)) {
            return;
        }
        BpelModel model = (BpelModel) controller.getModel();
//out("model: " + model);

        if ( !(model.getProcess() instanceof AbstractDocumentComponent)) {
            return;
        }
        AbstractDocumentComponent process = (AbstractDocumentComponent) model.getProcess();
        Map prefixMap = process.getPrefixes();

        if (prefixMap == null) {
            return;
        }
        Iterator prefixes  = prefixMap.keySet().iterator();
//out();
        while (prefixes.hasNext()) {
            String prefix = prefixes.next().toString();
//out("see: " + prefix);

            if (isSystemPrefix(prefix)) {
                continue;
            }
//out();
//out("see: " + prefix);
            model.startTransaction();
            process.removePrefix(prefix);

            if (hasValidationError(controller)) {
                model.rollbackTransaction();
//out("     ++");
            }
            else {
                model.endTransaction();
//out("     remove");
            }
        }
        ((AbstractModel) model).getAccess().flush();
    }

    private boolean isSystemPrefix(String prefix) {
        for (String systemPrefix : SYSTEM_PREFIXES) {
            if (systemPrefix.equals(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] SYSTEM_PREFIXES = new String[] {
        "",     // NOI18N
        "xs",   // NOI18N
        "xsd" , // NOI18N
        "bpws", // NOI18N
        "sxeh", // NOI18N
        "sxed", // NOI18N
        "sxt",  // NOI18N
    };
}
