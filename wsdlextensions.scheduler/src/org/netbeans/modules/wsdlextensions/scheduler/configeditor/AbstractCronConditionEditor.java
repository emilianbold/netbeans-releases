/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.netbeans.modules.wsdlextensions.scheduler.model.CronConstants;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author sunsoabi_edwong
 */
public abstract class AbstractCronConditionEditor extends JPanel
        implements CronConditionEditor, CronConstants {

    protected PropertyChangeSupport pcs;
    protected AbstractTriggerPanel mainPanel;
    protected boolean suppressNotification;
    protected Map<AbstractButton, JComponent[]> radioComponents;
    protected CronField cronField;
    protected List<AbstractButton> conditionTracker =
            new ArrayList<AbstractButton>();
    
    protected final String MSG_ASK_ENTER_MULTIPLE_ENTRIES;
    protected static final String ERR_LT2_ENTRIES =
            NbBundle.getMessage(AbstractCronConditionEditor.class,
                    "ERR_LT2_ENTRIES");                                 //NOI18N
    
    public AbstractCronConditionEditor(AbstractTriggerPanel mainPanel,
            CronField cronField) {
        super();
        
        this.mainPanel = mainPanel;
        this.cronField = cronField;
        MSG_ASK_ENTER_MULTIPLE_ENTRIES = NbBundle.getMessage(
                AbstractCronConditionEditor.class,
                "MSG_ASK_ENTER_MULTIPLE_ENTRIES",                       //NOI18N
                cronField.getFieldName());
        radioComponents = new HashMap<AbstractButton, JComponent[]>();
    }
    
    void addRadioComponents(AbstractButton rb, JComponent[] comps) {
        radioComponents.put(rb, comps);
    }
    
    JComponent[] getRadioComponents(AbstractButton rd) {
        return radioComponents.get(rd);
    }
    
    void smartSetEnabled(JComponent comp, boolean enabled) {
        if (comp.isEnabled() != enabled) {
            comp.setEnabled(enabled);
        }
    }
    
    void setRadioButtonEnabled(AbstractButton rad, boolean enabled) {
        JComponent[] comps = getRadioComponents(rad);
        if (comps != null) {
            for (JComponent c : comps) {
                smartSetEnabled(c, enabled);
            }
        }
    }
    
    AbstractButton getSelectedFieldCondition(ButtonGroup bgp) {
        Object[] selObjs = bgp.getSelection().getSelectedObjects();
        if ((selObjs != null) && (selObjs.length > 0)
                && (selObjs[0] instanceof AbstractButton)) {
            return (AbstractButton) selObjs[0];
        }
        return null;
    }
    
    void trackConditionSelection(AbstractButton selBtn) {
        conditionTracker.add(0, selBtn);
        if (conditionTracker.size() == 3) {
            conditionTracker.remove(2);
        }
    }
    
    AbstractButton getPreviousSelectedCondition() {
        if (conditionTracker.size() == 2) {
            return conditionTracker.get(1);
        }
        return null;
    }
    
    void updateRadioButtonsEnabling(ButtonGroup bgp) {
        Enumeration<AbstractButton> buttons = bgp.getElements();
        AbstractButton selBtn = null;
        while (buttons.hasMoreElements()) {
            AbstractButton btn = buttons.nextElement();
            setRadioButtonEnabled(btn, btn.isSelected());
            if (btn.isSelected()) {
                selBtn = btn;
            }
        }
        if (selBtn != null) {
            JComponent[] comps = getRadioComponents(selBtn);
            if (comps != null) {
                for (JComponent c : comps) {
                    if (c instanceof JTextField) {
                        JTextField tf = (JTextField) c;
                        if (Utils.isEmpty(tf.getText())) {
                            askEnterMultipleEntries(tf);
                        } else if (tf.getText().equals(
                                MSG_ASK_ENTER_MULTIPLE_ENTRIES)) {
                            tf.selectAll();
                            tf.requestFocusInWindow();
                        }
                        break;
                    }
                }
            }
            trackConditionSelection(selBtn);
            fireConditionPropertyChange(selBtn);
        }
    }
    
    private void askEnterMultipleEntries(JTextField tf) {
        tf.setText(MSG_ASK_ENTER_MULTIPLE_ENTRIES);
        tf.selectAll();
        tf.requestFocusInWindow();
    }
    
    public void addConditionPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void removeConditionPropertyChangeListener(
            PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
    public void fireConditionPropertyChange(Object value) {
        if (!suppressNotification) {
            pcs.firePropertyChange(cronField.name(), null, value);
        }
    }
    
    void updateDescription(String titleKey, String descKey) {
        mainPanel.updateDescription(AbstractCronConditionEditor.class,
                titleKey, descKey);
    }
    
    void registerFocusGained(JSpinner spinner,
            final String titleKey, final String descKey) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
                .addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent evt) {
                        updateDescription(titleKey, descKey);
                    }
                });
    }
    
    void validateMultipleEntries(String entries, Component label) {
        StringTokenizer st = null;
        if (!Utils.isEmpty(entries)) {
            st = new StringTokenizer(entries, LAX_DELIM);
        }
        if ((null == st) || (st.countTokens() < 2)) {
            throw new SchedulerArgumentException(ERR_LT2_ENTRIES, label);
        }
        while (st.hasMoreTokens()) {
            try {
                parseInt(true, st.nextToken());
            } catch (SchedulerArgumentException sae) {
                throw new SchedulerArgumentException(sae.getMessage(), label);
            }
        }
    }
    
    abstract int parseInt(boolean user, String str);
}
