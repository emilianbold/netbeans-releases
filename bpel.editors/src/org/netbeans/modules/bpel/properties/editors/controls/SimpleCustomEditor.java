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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.BorderLayout;
import java.lang.reflect.Constructor;
import javax.swing.JPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;

public class SimpleCustomEditor<T> extends AbstractCustomNodeEditor<T> {

    private static final long serialVersionUID = 1L;

    private JPanel mainPanel;
    private Class<? extends JPanel> mainPanelClass;

    public SimpleCustomEditor(BpelNode<T> node,
            Class<? extends JPanel> mainPanelClass, 
            EditingMode mode) {
        super(node);
        this.mainPanelClass = mainPanelClass;
        if (mode != null) {
            setEditingMode(mode);
        }
        createContent();
        initControls();
        subscribeListeners();
    }
    
    public SimpleCustomEditor(BpelNode<T> node, 
            JPanel mainPanel, 
            EditingMode mode) {
        super(node);
        this.mainPanel = mainPanel;
        this.mainPanelClass = mainPanel.getClass();
        //
        if (mainPanel instanceof CustomNodeEditor.Owner) {
            ((CustomNodeEditor.Owner)mainPanel).setEditor(this);
        }
        //
        if (mode != null) {
            setEditingMode(mode);
        }
        createContent();
        initControls();
        subscribeListeners();
    }
    
    public void createContent() {
        this.setLayout(new BorderLayout());
        // this.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        //
        if (mainPanel == null) {
            try {
                Constructor<? extends JPanel> constructor =
                        mainPanelClass.getConstructor(CustomNodeEditor.class);
                mainPanel = constructor.newInstance(this);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        //
        if (mainPanel != null) {
            this.add(mainPanel, BorderLayout.CENTER);
            // The accessibleContext is derived from the main child panel.
            accessibleContext = mainPanel.getAccessibleContext();
        }
        //
        SoaUtil.activateInlineMnemonics(this);
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public HelpCtx getHelpCtx() {
        if (mainPanel != null && mainPanel instanceof HelpCtx.Provider) {
            return ((HelpCtx.Provider)mainPanel).getHelpCtx();
        } else if (mainPanelClass != null) {
            return new HelpCtx(mainPanelClass);
        }
        return null;
    }

}
