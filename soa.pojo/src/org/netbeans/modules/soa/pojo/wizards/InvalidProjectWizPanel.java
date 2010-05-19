/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author gpatil
 */
public class InvalidProjectWizPanel implements org.openide.WizardDescriptor.Panel{
    private org.openide.WizardDescriptor.Panel orig = null;
    private WizardDescriptor wizard;
    private static ThreadLocal<Boolean> inCall = new ThreadLocal<Boolean>();

    public InvalidProjectWizPanel(org.openide.WizardDescriptor.Panel og){
        this.orig = og;
    }
    public void addChangeListener(ChangeListener arg0) {
        this.orig.addChangeListener(arg0);
    }

    public Component getComponent() {
        return this.orig.getComponent();
    }

    public HelpCtx getHelp() {
        return this.orig.getHelp();
    }

    public boolean isValid() {
        String errMsg = NbBundle.getMessage(InvalidProjectWizPanel.class, "ERR_Invalid_ProjectType");
        if (this.wizard != null){
            wizard.putProperty("WizardPanel_errorMessage", errMsg);//NOI18N
        }
        return false;
    }

    public void readSettings(Object arg0) {
        if (this.wizard == null) {
            this.wizard = (WizardDescriptor) arg0;
        }

        if (inCall.get() == null){
            try {
                inCall.set(Boolean.TRUE);
                this.orig.readSettings(arg0);
            } finally {
                inCall.remove();
            }
        } else {
            return;
        }
    }

    public void removeChangeListener(ChangeListener arg0) {
        this.orig.removeChangeListener(arg0);
    }

    public void storeSettings(Object arg0) {
        //this.orig.storeSettings(arg0);
    }

}
