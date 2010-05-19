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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.DesignView;

/**
 *
 * @author anjeleevich
 */
public abstract class TextAreaEditor extends JTextArea {

    private JScrollPane scrollPane;

    private DesignView designView;
    private Action applyAction;
    private Action cancelAction;
    private Listeners listeners;
    
    private boolean changedFlag = false;
    private Color normalForeground = null;
    
    public TextAreaEditor(DesignView designView, int rows, int columns) {
        super(rows, columns);
        this.designView = designView;
        scrollPane = new JScrollPane(this);
        
        applyAction = new ApplyAction();
        cancelAction = new CancelAction();
        listeners = new Listeners();
        
        setXPath(getModelValue());
        
        addFocusListener(listeners);
        getDocument().addDocumentListener(listeners);
        
        InputMap inputMap = getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
                CANCEL_ACTION);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 
                KeyEvent.CTRL_DOWN_MASK), APPLY_ACTION);
        
        ActionMap actionMap = getActionMap();
        actionMap.put(CANCEL_ACTION, cancelAction);
        actionMap.put(APPLY_ACTION, applyAction);
    }
    
    public DesignView getDesignView() {
        return designView;
    }
    
    public WLMModel getModel() {
        return designView.getModel();
    }
    
    public TTask getTask() {
        WLMModel model = getModel();
        if (model == null) {
            return null;
        }
        
        return model.getTask();
    }    
    
    public void setXPath(String xpath) {
        setText(TextFieldEditor.xPathToText(xpath));
    }
    
    public String getXPath() {
        return TextFieldEditor.textToXPath(getText());
    }
    
    public JComponent getView() {
        return scrollPane;
    }
    
    public void updateContent() {
        getDocument().removeDocumentListener(listeners);
        setXPath(getModelValue());
        getDocument().addDocumentListener(listeners);
        setChanged(false);
    }
    
    private void setChanged(boolean changedFlag) {
        if (this.changedFlag != changedFlag) {
            this.changedFlag = changedFlag;
            
            if (changedFlag) {
                if (normalForeground == null) {
                    normalForeground = getForeground();
                }
                setForeground(CHANGED_FOREGROUND);
            } else {
                if (normalForeground != null) {
                    setForeground(normalForeground);
                }
            }
        }
    }    
    
    public abstract String getModelValue();
    public abstract void setModelValue(String value);
    
    private static final String APPLY_ACTION = "apply-changes-action";
    private static final String CANCEL_ACTION = "cancel-editing-action";
    private static final Color CHANGED_FOREGROUND = Color.BLUE;
    
    private class ApplyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            setModelValue(getXPath());
            setChanged(false);
        }
    }
    
    private class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            getDocument().removeDocumentListener(listeners);
            setText(getModelValue());
            getDocument().addDocumentListener(listeners);
            setChanged(false);
        }
    }    
    
    private class Listeners implements DocumentListener, FocusListener {
        public void insertUpdate(DocumentEvent e) {
            setChanged(true);
        }

        public void removeUpdate(DocumentEvent e) {
            setChanged(true);
        }

        public void changedUpdate(DocumentEvent e) {
            setChanged(true);
        }

        public void focusGained(FocusEvent e) {}

        public void focusLost(FocusEvent e) {
            if (!e.isTemporary() && changedFlag) {
                applyAction.actionPerformed(null);
            }
        }        
    }    
}
