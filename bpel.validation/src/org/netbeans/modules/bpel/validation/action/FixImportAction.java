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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.validation.action;

import java.awt.event.ActionEvent;
import java.util.List;

import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.validation.core.Controller;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.05.20
 */
public class FixImportAction extends IconAction {

    public FixImportAction() {
        this(i18n(FixImportAction.class, "CTL_Fix_Import_Action"), i18n(FixImportAction.class, "TLT_Fix_Import_Action")); // NOI18N
    }

    protected FixImportAction(String name, String tooltip) {
        super(name, tooltip, null);
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
        BpelModel model = (BpelModel) controller.getModel();

        if ( !checkValidationError(controller)) {
            return;
        }
//out("model: " + model);
        Process process = model.getProcess();

        if (process == null) {
            return;
        }
        Import[] imports = process.getImports();

        if (imports == null) {
            return;
        }
//out();
        for (int i = imports.length - 1; i >= 0; i--) {
//out();
//out("see: " + imports[i].getLocation());
            model.startTransaction();
            process.removeImport(i);

            if (hasValidationError(controller)) {
                model.rollbackTransaction();
//out("     ++");
            }
            else {
                model.endTransaction();
//out("     remove");
            }
        }
    }

    protected boolean hasValidationError(Controller controller) {
        org.netbeans.modules.bpel.validation.xpath.Validator.doForceXPathValidation = true;
        List<ResultItem> results = controller.validate(ValidationType.PARTIAL);
        org.netbeans.modules.bpel.validation.xpath.Validator.doForceXPathValidation = false;
        return hasValidationError(results);
    }

    private boolean hasValidationError(List<ResultItem> results) {
        for (ResultItem item : results) {
            if (item.getType() == ResultType.ERROR) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkValidationError(Controller controller) {
        if ( !hasValidationError(controller)) {
            return true;
        }
        printInformation(i18n(FixImportAction.class, "MSG_Fix_Validation_Errors")); // NOI18N
        return false;
    }
}
