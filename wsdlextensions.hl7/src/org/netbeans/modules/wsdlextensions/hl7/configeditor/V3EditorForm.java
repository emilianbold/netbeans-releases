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

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.V3Panel;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class V3EditorForm extends V3Panel implements Form{

    private final Model model;
        
    public V3EditorForm(Model model){
        super();
        this.model = model;
        init();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //mHostTextField.requestFocusInWindow();
            }
        });
        
    }
    
    private void init() {
        
    }
    /**
     * Signal for the form to reread its data model into its view, in effect
     * discarding uncommitted changes made thru the view.
     */
    public void refresh() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("refresh()", new Runnable() {
                public void run() {
                    refresh();
                }
            });
            return;
        }


    /*    mHostTextField.setText(model.getHost());
        mPortTextField.setText(model.getPort());
        mQueueManagerTextField.setText(model.getQueueManager());
        mQueueTextField.setText(model.getQueue());
        mChannelTextField.setText(model.getChannel()); */

    }

    /**
     * Signal for the form to update its data model with uncommitted changes
     * made thru its view.
     */
    public void commit() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("commit()", new Runnable() {
                public void run() {
                    commit();
                }
            });
            return;
        }

        /*
        model.setHost(mHostTextField.getText());
        model.setPort(mPortTextField.getText());
        model.setQueueManager(mQueueManagerTextField.getText());
        model.setQueue(mQueueTextField.getText());
        model.setChannel(mChannelTextField.getText());
        */
    }

    /**
     * Populate the form's internal data model with the information provided. If
     * the supplied model is not a type that is recognized or meaningful, it is
     * disregarded.
     *
     * @param model A supported FormModel instance.
     */
    public void loadModel(FormModel model) {

    }

    /**
     * Returns the form's own data model.
     *
     * @return Form data model
     */
    public FormModel getModel() {
        return model;
    }
    
    /**
     * The Swing component that represents the form's visual representation.
     *
     * @return The form's view.
     */
    public JComponent getComponent() {
       return this;
    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {
        String getLocation();
        void setLocation(String location);
        
        String getTransportProtocol();
        void setTransportProtocol(String transportProtocol);
        
        String getUse();
        void setUse(String use);
        
        String getEncodingStyle();
        void setEncodingStyle(String encodingStyle);

        String getLLPType();
        void setLLPType(String llpType);
        
        String getStartBlockCharacter();
        void setStartBlockCharacter(String startBlockChar);
        
        String getEndBlockCharacter();
        void setEndBlockCharacter(String endBlockChar);
        
        String getEndDataCharacter();
        void setEndDataCharacter(String endDataChar);
        
        boolean isHLLPChecksumEnabled();
        void setHLLPChecksumEnabled(boolean enabled);
        
        int getMllpv2RetriesCountOnNak();
        void setMllpv2RetriesCountOnNak(int count);
        
        int getMllpv2RetryInterval();
        void setMllpv2RetryInterval(int interval);
        
        int getMllpv2TimeToWaitForAckNak();
        void setMllpv2TimeToWaitForAckNak(int duration);
    }    
    

}
