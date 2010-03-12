/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.selectors;

import java.util.Arrays;
import java.util.MissingResourceException;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.ConnectorComparator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class SelectorPanel {

    private RepositorySelectorBuilder builder = new RepositorySelectorBuilder();
    private final String comboLabelText
            = NbBundle.getMessage(SelectorPanel.class,
                                  "SelectorPanel.connectorLabel.text"); //NOI18N

    boolean open() {
        String title = createOpenDescriptor();
        DialogDescriptor dd = builder.createDialogDescriptor(title);
        boolean ret = DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
        return ret;
    }

    boolean edit(Repository repository, String errorMessage) {
        DialogDescriptor dd = createEditDescriptor(repository, errorMessage);
        boolean ret = DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
        return ret;
    }

    private String createOpenDescriptor() throws MissingResourceException {
        String title = NbBundle.getMessage(SelectorPanel.class, "CTL_CreateTitle"); //NOI18N
        builder.setLabelText(comboLabelText);
        builder.setBugtrackingConnectorDisplayFormat("{0}"); //NOI18N
        return title;
    }

    private DialogDescriptor createEditDescriptor(Repository repository, String errorMessage) throws MissingResourceException {
        String title = NbBundle.getMessage(SelectorPanel.class, "CTL_EditTitle"); //NOI18N
        builder.setLabelVisible(false);
        builder.setComboBoxVisible(false);
        builder.setPreselectedRepository(repository);
        builder.setInitialErrorMessage(errorMessage);
        DialogDescriptor dd = builder.createDialogDescriptor(title);
        return dd;
    }

    Repository getRepository() {
        return builder.getSelectedRepository();
    }

    void setConnectors(BugtrackingConnector[] connectors) {
        Arrays.sort(connectors, new ConnectorComparator());
        builder.setBugtrackingConnectors(connectors);
    }    
}
