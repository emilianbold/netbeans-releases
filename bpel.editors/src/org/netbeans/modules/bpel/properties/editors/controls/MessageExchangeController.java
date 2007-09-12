/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.choosers.MessageExchangeChooserPanel;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VisibilityScope;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 * Collects the common behaviour for selection of Message Exchange
 * for Recieve and Reply activities.
 *
 * @author  nk160297
 */
public class MessageExchangeController extends EditorLifeCycleAdapter
        implements Validator.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor myEditor;
    private DefaultValidator myValidator;
    private Timer inputDelayTimer;
    
    private MessageExchange myMsgEx;
    
    private JTextField fldMessageExchange;
    private JButton btnChooseMsgEx;
    
//    private ConfigurationListener myListener;
    
    public MessageExchangeController(CustomNodeEditor anEditor) {
        this.myEditor = anEditor;
        createContent();
    }
    
    public void createContent() {
        //
        btnChooseMsgEx = new JButton();
        fldMessageExchange = new JTextField();
        //
        btnChooseMsgEx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseMessageExchange();
            }
        });
        //
        ActionListener timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recalculateMsgEx();
                getValidator().revalidate(true);
            }
        };
        inputDelayTimer = new Timer(Constants.INPUT_VALIDATION_DELAY, timerListener);
        inputDelayTimer.setCoalesce(true);
        inputDelayTimer.setRepeats(false);
        //
        DocumentListener docListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void insertUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void removeUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
        };
        fldMessageExchange.getDocument().addDocumentListener(docListener);
        //
        FocusListener fl = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                inputDelayTimer.stop();
                recalculateMsgEx();
                getValidator().revalidate(true);
            }
        };
        fldMessageExchange.addFocusListener(fl);
    }
    
    private void chooseMessageExchange() {
        Lookup lookup = myEditor.getLookup();
        VisibilityScope visScope = new VisibilityScope(
                (BpelEntity)myEditor.getEditedObject(), lookup);
        Lookup contextLookup = new ExtendedLookup(lookup, visScope);
        MessageExchangeChooserPanel mExchChooserPanel =
                new MessageExchangeChooserPanel(contextLookup);
        TreeNodeChooser chooser = new TreeNodeChooser(mExchChooserPanel);
        chooser.initControls();
        if (myMsgEx != null) {
            mExchChooserPanel.setSelectedValue(myMsgEx);
        }
        String title = NbBundle.getMessage(FormBundle.class,
                "DLG_MessageExchangeChooserTitle"); // NOI18N
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(chooser, title);
        SoaDialogDisplayer.getDefault().notify( descriptor );
        if (descriptor.isOkHasPressed()) {
            MessageExchange newMExch =
                    mExchChooserPanel.getSelectedValue();
            if (newMExch != null) {
                setMessageExchange(newMExch);
            }
        }
    }
    
    private void recalculateMsgEx() {
        myMsgEx = null;
        //
        String newMsgExName = fldMessageExchange.getText();
        if (newMsgExName == null || newMsgExName.length() == 0) {
            return;
        }
        //
        Object obj = myEditor.getEditedObject();
        if (obj != null && obj instanceof BpelEntity) {
            BpelEntity modelElement = (BpelEntity)obj;
            Lookup lookup = myEditor.getLookup();
            FindHelper helper =
                    (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
            Iterator<BaseScope> itr = helper.scopeIterator(modelElement);
            while (itr.hasNext()) {
                BaseScope baseScope = itr.next();
                MessageExchangeContainer msgExCont =
                        baseScope.getMessageExchangeContainer();
                if (msgExCont != null) {
                    MessageExchange[] msgExArr = msgExCont.getMessageExchanges();
                    for (MessageExchange msgEx : msgExArr) {
                        String msgExName = msgEx.getName();
                        if (newMsgExName.equals(msgExName)) {
                            myMsgEx = msgEx;
                            return;
                        }
                    }
                }
            }
        }
    }
    
    public boolean initControls() {
        try {
            Property prop = PropertyUtils.lookForPropertyByType(
                    myEditor.getEditedNode(),
                    PropertyType.MESSAGE_EXCHANGE);
            if ( prop != null ) {
                BpelReference<MessageExchange> msgExRef =
                        (BpelReference<MessageExchange>)prop.getValue();
                if (msgExRef != null) {
                    MessageExchange msgEx = msgExRef.get();
                    setMessageExchange(msgEx);
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        //
        return true;
    }
    
    public boolean applyNewValues() {
        try {
            Object omRef = myEditor.getEditedObject();
            assert omRef instanceof ReferenceCollection;
            ReferenceCollection refColl = (ReferenceCollection)omRef;
            //
            Property prop = PropertyUtils.lookForPropertyByType(
                    myEditor.getEditedNode(),
                    PropertyType.MESSAGE_EXCHANGE);
            if ( prop != null ) {
                if (myMsgEx == null) {
                    prop.setValue(null);
                } else {
                    BpelReference<MessageExchange> newMExchRef =
                            refColl.createReference(myMsgEx, MessageExchange.class);
                    prop.setValue(newMExchRef);
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    public MessageExchange getMessageExchange() {
        return myMsgEx;
    }
    
    public void setMessageExchange(MessageExchange newValue) {
        if ((myMsgEx == null && newValue != null) ||
                (myMsgEx != null && !myMsgEx.equals(newValue))) {
            myMsgEx = newValue;
            fldMessageExchange.setText(
                    myMsgEx == null ? "" : myMsgEx.getName());
        }
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public boolean doFastValidation() {
                    String newMsgExName = fldMessageExchange.getText();
                    if (myMsgEx == null && newMsgExName != null &&
                            newMsgExName.length() != 0) {
                        addReasonKey("ERR_INCORRECT_MESSAGE_EXCHANGE"); //NOI18N
                    }
                    return isReasonsListEmpty();
                }
                
//                public boolean doDetailedValidation() {
//                    boolean isValid = true;
//                    //
//                    return isValid;
//                }
                
            };
        }
        return myValidator;
    }
    
    // ==============================================================
    // Fields accessors
    // ==============================================================
    
    public JButton getBtnChooseMsgEx() {
        return btnChooseMsgEx;
    }
    
    public JTextField getFldMessageExchange() {
        return fldMessageExchange;
    }
    
//    public void setConfigurationListener(ConfigurationListener newValue) {
//        myListener = newValue;
//    }
//
//    public interface ConfigurationListener {
//        void partnerLinkChanged();
//        void operationChanged();
//    }
    
}
