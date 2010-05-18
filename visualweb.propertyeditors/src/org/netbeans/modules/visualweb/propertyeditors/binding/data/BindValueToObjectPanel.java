/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import java.awt.BorderLayout;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetCallback;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetPanel;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BindValueToObjectPanel extends DataBindingPanel implements BindingTargetCallback {

    private static final Bundle bundle = Bundle.getBundle(BindValueToObjectPanel.class);

    protected BindingTargetPanel targetPanel;
    protected boolean initializing = true;

    private String newExpression = null;

    public BindValueToObjectPanel(BindingTargetCallback callback, DesignProperty prop) {
        super(callback, prop);
        targetPanel = new BindingTargetPanel(this);
        targetPanel.sourceContextChanged(prop.getDesignBean().getDesignContext());
        targetPanel.sourceBeanChanged(prop.getDesignBean());
        targetPanel.sourcePropertyChanged(prop);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        repaint(100);
        initializing = false;
         addComponentListener(new ComponentAdapter(){

            public void componentShown(ComponentEvent e){
                if (newExpression != null){
                    if((newExpression != null) && isShowing()){
                        bindingCallback.setNewExpressionText(newExpression);
                    }
                }
            }
        });
    }

    private void jbInit() throws Exception {
        this.setLayout(new BorderLayout());
        this.add(targetPanel, BorderLayout.CENTER);
    }

    public String getDataBindingTitle() {
        return bundle.getMessage("bindToObj"); // NOI18N
    }

    public void refresh() {
        this.validate();
        this.doLayout();
        this.repaint(100);
        bindingCallback.refresh();
    }

    public void setNewExpressionText(String newExpr) {
        newExpression = newExpr;
        bindingCallback.setNewExpressionText(newExpression);
    }
}
