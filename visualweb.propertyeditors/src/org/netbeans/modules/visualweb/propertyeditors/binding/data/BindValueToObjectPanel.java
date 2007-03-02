/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
