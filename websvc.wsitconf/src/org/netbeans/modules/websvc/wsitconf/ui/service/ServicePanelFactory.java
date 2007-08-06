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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service;

import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class ServicePanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    
    private ToolBarDesignEditor editor;
    private Node node;
    private UndoManager undoManager;
    private Project project;
    private JaxWsModel jaxwsmodel;
    
    /**
     * Creates a new instance of ServicePanelFactory
     */
    ServicePanelFactory(ToolBarDesignEditor editor, Node node, UndoManager undoManager, Project p, JaxWsModel jxwsmodel) {
        this.editor=editor;
        this.node = node;
        this.project = p;
        this.jaxwsmodel = jxwsmodel;
        this.undoManager = undoManager;
    }

    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof Binding) {
            Binding b = (Binding)key;
            return new ServicePanel((SectionView) editor.getContentView(), node, project, b, undoManager, jaxwsmodel);
        }
        if (key instanceof BindingOperation) {
            BindingOperation o = (BindingOperation)key;
            return new OperationPanel((SectionView) editor.getContentView(), node, project, o);
        }
        if (key instanceof BindingInput) {
            BindingInput i = (BindingInput)key;
            return new InputPanel((SectionView) editor.getContentView(), i, undoManager);
        }
        if (key instanceof BindingOutput) {
            BindingOutput o = (BindingOutput)key;
            return new OutputPanel((SectionView) editor.getContentView(), o, undoManager);
        }
        if (key instanceof BindingFault) {
            BindingFault f = (BindingFault)key;
            return new FaultPanel((SectionView) editor.getContentView(), f, undoManager);
        }
        return null;
    }
}
