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

package org.netbeans.modules.java.hints.jackpot.refactoring;

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
public class ReplaceConstructorRefactoringUI implements RefactoringUI {

    private final TreePathHandle constructor;
    private String factoryName;
    private ReplaceConstructorWithFactory panel;
    private String name;

    public ReplaceConstructorRefactoringUI(TreePathHandle constructor, String name) {
        this.constructor = constructor;
        this.name = name;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ReplaceConstructorRefactoringUI.class, "ReplaceConstructorName");    
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ReplaceConstructorRefactoringUI.class, "ReplaceConstructorDescription", name ,factoryName);    
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
                panel.initialize();
            }
            @Override
            public Component getComponent() {
                if (panel == null) {
                    panel = new ReplaceConstructorWithFactory(parent);
                }
                return panel;
            }
        };
    }

    @Override
    public Problem setParameters() {
        this.factoryName = panel.getFactoryName();
        return null;
    }

    @Override
    public Problem checkParameters() {
        String factoryName = this.factoryName != null ? this.factoryName : panel.getFactoryName();
        
        if (factoryName == null || factoryName.length() == 0) {
            return new Problem(true, "No factory method name specified.");
        }
        if (!SourceVersion.isIdentifier(factoryName)) {
            return new Problem(true, factoryName + " is not an identifier.");
        }
        return null;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return new ReplaceConstructorRefactoring(constructor, factoryName);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
