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
package org.netbeans.modules.bpel.validation.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
//import org.openide.text.DataEditorSupport;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.core.helper.api.CoreUtil;
import org.netbeans.modules.bpel.core.util.BPELValidationController;
import org.netbeans.modules.bpel.validation.util.QuickFix;
import org.netbeans.modules.bpel.validation.util.ResultItem;
import org.netbeans.modules.bpel.validation.util.Util;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.12.03
 */
public final class QuickFixAction extends IconAction {

  public QuickFixAction() {
    super(
      i18n(QuickFixAction.class, "CTL_Quick_Fix_Action"), // NOI18N
      i18n(QuickFixAction.class, "TLT_Quick_Fix_Action"), // NOI18N
      icon(Util.class, "quickfix") // NOI18N
    );
  }

  public void actionPerformed(ActionEvent event) {
    Node node = getSelectedNode();
    BpelModel model = getBpelModel(node);
    List<ResultItem> items = getResultItems(model);

    if (items == null) {
      items = new ArrayList<ResultItem>();
    }
//out("SIZE: " + items.size());
    InputOutput io = IOProvider.getDefault().getIO(i18n(QuickFixAction.class, "LBL_Quick_Fix_Window"), false); // NOI18N

    try {
      io.getOut().reset();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    io.select();

    if (items.size() == 0) {
      io.getOut().println(i18n(QuickFixAction.class, "MSG_Nothing_to_do")); // NOI18N
      return;
    }
    io.getOut().println(i18n(QuickFixAction.class, "MSG_Quick_Fix_started")); // NOI18N
    QuickFix quickFix;

    for (ResultItem item : items) {
      quickFix = item.getQuickFix();

      if (quickFix == null) {
        continue;
      }
      quickFix.doFix();
      io.getOut().println();
      io.getOut().println("Error: " + quickFix.getFixDescription());
    }
    io.getOut().println();

//1 Error(s),  0 Warning(s).

    io.getOut().print(i18n(QuickFixAction.class,"MSG_Quick_Fix_finished")); // NOI18N
//    io.select();
  }

  private BpelModel getBpelModel(Node node) {
    DataObject data = getDataObject(node);

    if (data == null) {
      return null;
    }
    return CoreUtil.getBpelModel(data);
  }

// to do r?
//  private List<ResultItem> getResultItems(Node node) {
////out("MODE: " + node);
//    if (myValidationController == null) {
//      BPELDataEditorSupport support = (BPELDataEditorSupport) node.getLookup().lookup(DataEditorSupport.class);
//      myValidationController = support.getValidationController();
//    }
//    if (myValidationController == null) {
////out("CONTROLLER is NULL");
//      return null;
//    }
//    return myValidationController.getResultItems();
//  }

  private List<ResultItem> getResultItems(BpelModel model) {
    if (model == null) {
      return null;
    }
    Validation validation = new Validation();
    validation.validate(model, ValidationType.COMPLETE);

    List<org.netbeans.modules.xml.xam.spi.Validator.ResultItem> items = validation.getValidationResult();
    List<ResultItem> resultItems = new ArrayList<ResultItem>();

    for (org.netbeans.modules.xml.xam.spi.Validator.ResultItem item : items) {
      if (item instanceof ResultItem) {
        resultItems.add((ResultItem) item);
      }
    }
    return resultItems;
  }

  private BPELValidationController myValidationController;
}
