/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.etl.ui.view.cookies;

import java.util.List;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.wizards.ETLTableSelectionWizard;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Cookie for exposing access to a dialog box for selecting tables to participate in an
 * eTL collaboration.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SelectTablesCookie implements Node.Cookie {

    private static final boolean DEBUG = false;
    private static final String LOG_CATEGORY = SelectTablesCookie.class.getName();
    private ETLDataObject dataObj;
    private static transient final Logger mLogger = Logger.getLogger(SelectTablesCookie.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Creates a new instance of SelectTablesCookie associated with the given
     * ProjectElement.
     *
     * @param pElement the associated project element
     */
    public SelectTablesCookie() {
    }

    /**
     * Displays the table selection wizard for the current data object.
     */
    public void showDialog() {
        try {
            dataObj = DataObjectProvider.getProvider().getActiveDataObject();
            ETLCollaborationModel collabModel = DataObjectProvider.getProvider().getActiveDataObject().getModel();
            ETLDefinitionImpl def = collabModel.getETLDefinition();
            ETLTableSelectionWizard wizard = new ETLTableSelectionWizard(def);
            DataObjectHelper.setDefaultCursor();

            if (wizard.show()) {
                List sources = wizard.getSelectedSourceModels();
                List targets = wizard.getSelectedDestinationModels();

                // Update definition object.
                DataObjectHelper helper = new DataObjectHelper(dataObj);
                helper.updateTableSelections(dataObj, sources, targets);

                if (DEBUG) {
                    mLogger.infoNoloc(mLoc.t("EDIT019: Selected source tables:{0}", sources));
                    mLogger.infoNoloc(mLoc.t("EDIT020: Selected target tables:{0}", targets));
                    mLogger.infoNoloc(mLoc.t("EDIT021: New state of ETL Definition:{0}", def.toXMLString("")));
                }
                dataObj.getETLEditorSupport().synchDocument();
            }
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message("Table selection failed.", NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
}
