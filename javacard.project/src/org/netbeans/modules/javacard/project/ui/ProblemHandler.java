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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.ui;

import org.openide.WizardDescriptor;

import javax.swing.event.ChangeListener;

/**
 * Interface to something that disables a dialog and
 * provides user feedback when something is wrong.
 * <p>
 * Allows us to provide one adapter for setting the problem in a
 * wizard descriptor, and our own problem-displaying component.
 * This way PlatformPropertiesPanel can be used both in a wizard
 * and as the customizer for a JavacardPlatformDataObject's node.
 *
 * @author Tim Boudreau
 */
public interface ProblemHandler {

    /**
     * Interfaces to be implemented by node customizers who want to
     * control the parent dialog's OK button state.
     */
    public static interface UI {
        void setProblemHandler (ProblemHandler handler);
        void addChangeListener (ChangeListener cl);
        void removeChangeListener (ChangeListener cl);
        String getProblem();
    }
    
    public void setProblem (String problem);
    public boolean isProblem();

    public static final class WizardAdapter implements ProblemHandler {
        private final WizardDescriptor wiz;
        public WizardAdapter (WizardDescriptor wiz) {
            this.wiz = wiz;
        }

        public void setProblem(String problem) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, problem); //NOI18N
        }

        public boolean isProblem() {
            String s = (String) wiz.getProperty(WizardDescriptor.PROP_ERROR_MESSAGE);
            return s != null && s.trim().length() > 0;
        }
    }
}
