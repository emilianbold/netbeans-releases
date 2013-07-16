/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.options;

import java.awt.EventQueue;
import javax.swing.JComponent;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.annotations.UserAnnotations;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    displayName="#LBL_AnnotationsOptions", // NOI18N
//    toolTip="#LBL_AnnotationsOptionsTooltip",
    id=PhpAnnotationsPanelController.ID,
    location=UiUtils.OPTIONS_PATH,
    position=155
)
public class PhpAnnotationsPanelController extends BaseOptionsPanelController {

    public static final String ID = "Annotations"; // NOI18N

    // @GuardedBy(EDT)
    private PhpAnnotationsPanel panel = null;


    @Override
    protected boolean validateComponent() {
        assert EventQueue.isDispatchThread();
        // noop
        return true;
    }

    @Override
    protected void updateInternal() {
        assert EventQueue.isDispatchThread();
        panel.setAnnotations(UserAnnotations.getInstance().getAnnotations());
        panel.setResolveDeprecatedElements(PhpOptions.getInstance().isAnnotationsResolveDeprecatedElements());
    }

    @Override
    protected void applyChangesInternal() {
        assert EventQueue.isDispatchThread();
        UserAnnotations.getInstance().setAnnotations(panel.getAnnotations());
        PhpOptions.getInstance().setAnnotationsResolveDeprecatedElements(panel.isResolveDeprecatedElements());
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        if (panel == null) {
            panel = new PhpAnnotationsPanel();
            panel.addChangeListener(this);
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.options.PhpAnnotationsPanelController"); // NOI18N
    }

}
