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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.properties.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.print.api.PrintUtil;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * @author Alex Petrov (27.12.2007)
 */
public class DefineCorrelationWizard implements WizardProperties {
    private static final Dimension LEFT_DIMENSION_VALUE = new Dimension(100, 300);
    
    private WizardDescriptor wizardDescriptor;
    private BpelNode mainBpelNode;
    
    public DefineCorrelationWizard(BpelNode mainBpelNode) {
        this.mainBpelNode = mainBpelNode;
        
        WizardDescriptor.Iterator panelIterator = new WizardDescriptor.ArrayIterator(
            getWizardPanelList());
        wizardDescriptor = new WizardDescriptor(panelIterator);
        
        wizardDescriptor.putProperty(PROPERTY_AUTO_WIZARD_STYLE, true);
        wizardDescriptor.putProperty(PROPERTY_CONTENT_DISPLAYED, true);
        wizardDescriptor.putProperty(PROPERTY_CONTENT_NUMBERED, true);
        wizardDescriptor.putProperty(PROPERTY_HELP_DISPLAYED, false);
        wizardDescriptor.putProperty(PROPERTY_LEFT_DIMENSION, LEFT_DIMENSION_VALUE);
        
        wizardDescriptor.setTitle(PrintUtil.i18n(DefineCorrelationWizard.class, 
            "LBL_DefineCorrelation_Wizard_Title"));
    }
    
    public void showWizardDialog() {
        DialogDisplayer.getDefault().createDialog(wizardDescriptor).setVisible(true);
    }
    
    private List<Panel> getWizardPanelList() {
        List<Panel> panelList = new ArrayList<Panel>(2);
        panelList.add(new WizardStartPanel());
        panelList.add(new WizardFinishPanel());
        return panelList;
    }

    //========================================================================//
    public abstract class WizardAbstractPanel extends JPanel implements WizardDescriptor.Panel {
        private ChangeSupport changeSupport = new ChangeSupport(this);
        
        public abstract Component getComponent();
        protected abstract String getComponentName();
        public HelpCtx getHelp() {return null;}
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
        public void readSettings(Object settings) {}
        public void storeSettings(Object settings) {}
        
        @Override
        public boolean isValid() {return true;};
    }
    //========================================================================//
    public class WizardStartPanel extends WizardAbstractPanel {
        public WizardStartPanel() {
            super();
            setName(getComponentName());
            putClientProperty(PROPERTY_CONTENT_SELECTED_INDEX, 0);
/******???????*/ putClientProperty(PROPERTY_CONTENT_DATA, new String[] {"Start Panel", "Finish Panel"});
/******???????*/ putClientProperty(PROPERTY_ERROR_MESSAGE, "Start Panel: Error Message");

add(new JLabel((String)getClientProperty(PROPERTY_ERROR_MESSAGE)));
        }
        
        @Override
        public Component getComponent() {return this;}
        @Override
        protected String getComponentName() {return "Start Panel";}

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }
    }
    //========================================================================//
    public class WizardFinishPanel extends WizardAbstractPanel implements WizardDescriptor.FinishablePanel {
        public WizardFinishPanel() {
            super();
            setName(getComponentName());
            putClientProperty(PROPERTY_CONTENT_SELECTED_INDEX, 1);
/******???????*/ putClientProperty(PROPERTY_CONTENT_DATA, new String[] {"Start Panel", "Finish Panel"});
/******???????*/ putClientProperty(PROPERTY_ERROR_MESSAGE, "Finish Panel: Error Message");

add(new JLabel((String)getClientProperty(PROPERTY_ERROR_MESSAGE)));
        }

        @Override
        public Component getComponent() {return this;}
        @Override
        protected String getComponentName() {return "Finish Panel";}

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }
        public boolean isFinishPanel() {return true;}
    }
}

interface WizardProperties {
    String
        PROPERTY_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle", // NOI18N
        PROPERTY_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed", // NOI18N
        PROPERTY_CONTENT_NUMBERED = "WizardPanel_contentNumbered", // NOI18N
        PROPERTY_LEFT_DIMENSION = "WizardPanel_leftDimension", // NOI18N

        PROPERTY_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex", // NOI18N
        PROPERTY_CONTENT_DATA = "WizardPanel_contentData", // NOI18N
        PROPERTY_ERROR_MESSAGE = "WizardPanel_errorMessage", // NOI18N
        PROPERTY_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor", // NOI18N
        PROPERTY_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor", // NOI18N
        PROPERTY_IMAGE = "WizardPanel_image", // NOI18N
        PROPERTY_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment", // NOI18N

        PROPERTY_HELP_DISPLAYED = "WizardPanel_helpDisplayed", // NOI18N
        PROPERTY_HELP_URL = "WizardPanel_helpURL"; // NOI18N
}
    