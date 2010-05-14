/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.DesignView;

/**
 *
 * @author anjeleevich
 */
public abstract class TextFieldEditor extends JTextField {
    
    private DesignView designView;
    private Action applyAction;
    private Action cancelAction;
    private Listeners listeners;
    
    private boolean changedFlag = false;
    private Color normalForeground = null;
    
    private boolean xpathEditor;
    
    public TextFieldEditor(DesignView designView) {
        this(designView, true);
    }
    
    public TextFieldEditor(DesignView designView, boolean xpathEditor) {
        super(15);
        
        this.xpathEditor = xpathEditor;
        this.designView = designView;
        
        applyAction = new ApplyAction();
        cancelAction = new CancelAction();
        listeners = new Listeners();
        
        if (xpathEditor) {
            setXPath(getModelValue());
        } else {
            setText(getModelValue());
        }
        
        if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel) {
            setBorder(TextFieldBorder.INSTANCE);
        }
        
        addActionListener(applyAction);
        
        addFocusListener(listeners);
        getDocument().addDocumentListener(listeners);
        
        InputMap inputMap = getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
                CANCEL_ACTION);
        
        ActionMap actionMap = getActionMap();
        actionMap.put(CANCEL_ACTION, cancelAction);
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
        setText(xPathToText(xpath));
    }
    
    public String getXPath() {
        return textToXPath(getText());
    }
    
    public void updateContent() {
        getDocument().removeDocumentListener(listeners);
        if (xpathEditor) {
            setXPath(getModelValue());
        } else {
            setText(getModelValue());
        }
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
    
    private static final String CANCEL_ACTION = "cancel-editing-action";
    private static final Color CHANGED_FOREGROUND = Color.BLUE;
    
    private class ApplyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (xpathEditor) {
                setModelValue(getXPath());
            } else {
                setModelValue(getText());
            }
            setChanged(false);
        }
    }
    
    private class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            getDocument().removeDocumentListener(listeners);
            setXPath(getModelValue());
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

        public void focusGained(FocusEvent e) {
            activateNode();
        }

        public void focusLost(FocusEvent e) {
            if (!e.isTemporary() && changedFlag) {
                applyAction.actionPerformed(null);
            }
        }        
    }
    
    public static String xPathToText(String xpath) {
        if (xpath == null) {
            xpath = "";
        } else {
            xpath = xpath.trim();
        }
        
        if (xpath.length() == 0) {
            return xpath;
        }
        
        if (xpath.length() == 0) {
            return xpath;
        }
        
        if (xpath.length() >= 2) {
            if (xpath.startsWith(APOSTROPHE) 
                    && xpath.endsWith(APOSTROPHE)) 
            {
                return xpath.substring(1, xpath.length() - 1);
            }
            
            if (xpath.startsWith(QUOUTATION_MARK) 
                    && xpath.endsWith(QUOUTATION_MARK))
            {
                return xpath.substring(1, xpath.length() - 1);
            }
        }
        
        return EQUALS_SIGN + xpath;        
    }
    
    public static String textToXPath(String text) {
        if (text == null) {
            text = "";
        } else {
            text = text.trim();
        }
        
        if (text.length() == 0) {
            return text;
        }
        
        if (text.startsWith(EQUALS_SIGN)) {
            text = text.substring(1).trim();
        } else {
            if (text.contains(APOSTROPHE)) {
                text = QUOUTATION_MARK + text + QUOUTATION_MARK;
            } else {
                text = APOSTROPHE + text + APOSTROPHE;
            }
        }
        
        return text;        
    }

    public void activateNode() {}
    
    private static final String EQUALS_SIGN = "=";
    private static final String APOSTROPHE = "'";
    private static final String QUOUTATION_MARK = "\"";
}
