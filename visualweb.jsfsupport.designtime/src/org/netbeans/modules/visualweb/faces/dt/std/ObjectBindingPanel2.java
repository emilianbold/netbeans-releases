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
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.netbeans.modules.visualweb.faces.dt.binding.BindingCallback;
import org.netbeans.modules.visualweb.faces.dt.binding.TargetPanel;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignProperty;

public class ObjectBindingPanel2 extends JPanel implements BindingCallback {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(ObjectBindingPanel2.class);

    protected DesignProperty prop;
    protected ValueBindingPanel vrp;
    protected TargetPanel targetPanel = null;
    protected boolean initializing = true;

    public ObjectBindingPanel2(ValueBindingPanel vrp, DesignProperty prop) {
        this.vrp = vrp;
        this.prop = prop;
        targetPanel = new TargetPanel(this);
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
    }

    private void jbInit() throws Exception {
        this.setLayout(new BorderLayout());
        this.add(targetPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        vrp.repaint(100);
    }

    public void setNewExpressionText(String newExpr) {
        vrp.setValueBinding(newExpr);
    }
}
