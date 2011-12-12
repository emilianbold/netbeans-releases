/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jackpot30.refactoring.invertboolean;

import java.awt.Component;
import javax.lang.model.SourceVersion;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class InvertBooleanRefactoringUI implements RefactoringUI {

    private final TreePathHandle path;
    private String name;
    private InvertBooleanRefactoringPanel panel;

    public InvertBooleanRefactoringUI(TreePathHandle path, String name) {
        this.path = path;
        this.name = name;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(InvertBooleanRefactoringUI.class, "InvertBooleanName");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(InvertBooleanRefactoringUI.class, "InvertBooleanDescription", name);
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(final ChangeListener parent) {
        return new CustomRefactoringPanel() {
            @Override
            public void initialize() {
                panel.initialize(name);
            }
            @Override
            public Component getComponent() {
                if (panel == null) {
                    panel = new InvertBooleanRefactoringPanel(parent);
                }
                return panel;
            }
        };
    }

    @Override
    public Problem setParameters() {
        this.name = panel.getName();
        return null;
    }

    @Override
    public Problem checkParameters() {
        String name = this.name != null ? this.name : panel.getName();
        
        if (name == null || name.length() == 0) {
            return new Problem(true, "No factory method name specified.");
        }
        if (!SourceVersion.isIdentifier(name)) {
            return new Problem(true, name + " is not an identifier.");
        }
        return null;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return new InvertBooleanRefactoring(path, name);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
