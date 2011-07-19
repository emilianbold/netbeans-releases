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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.test.refactoring.operators;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class ParametersPanelOperator extends JDialogOperator {

    public ParametersPanelOperator(String name) {
        super(name);
    }
    
    private JButtonOperator back;
    private JButtonOperator preview;
    private JButtonOperator refactor;
    private JCheckBoxOperator openInNewTab;
    
    public JButtonOperator getBack() {
        if(back==null) {
            back = new JButtonOperator(this, "< Back");            
        }
        return back;
    }
    
    public JButtonOperator getPreview() {
        if(preview==null) {
            preview = new JButtonOperator(this, "Preview");            
        }
        return preview;
    }
    
    public JButtonOperator getRefactor() {
        if(refactor==null) {
            refactor = new JButtonOperator(this, "Refactor");            
        }
        return refactor;
    }

    public JCheckBoxOperator getOpenInNewTab() {
        if(openInNewTab == null) {
            openInNewTab = new JCheckBoxOperator(this, "Open In New Tab");
        }
        return openInNewTab;
    }
    
    public FindUsagesClassOperator getFindUsagesClassOperator() {
        return null;
        //return new FindUsagesClassOperator()
    }

    protected String getBungleText(String bundlePath, String bundleKey) {
        String bundleText = java.util.ResourceBundle.getBundle(bundlePath).getString(bundleKey);
        int index = bundleText.indexOf('&');
        if (index == -1) {
            return bundleText;
        }
        return bundleText.substring(0, index) + bundleText.substring(index + 1);
    }

}
