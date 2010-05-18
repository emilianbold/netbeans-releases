/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.visualweb.propertyeditors.binding.nodes;

import org.netbeans.modules.visualweb.propertyeditors.binding.PropertyBindingPanel;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import javax.faces.model.SelectItem;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNodeFactory;
import org.netbeans.modules.visualweb.propertyeditors.binding.PropertyBindingHelper;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;

public class DataProviderTargetNodeFactory implements BindingTargetNodeFactory {

    private static final Bundle bundle = Bundle.getBundle(DataProviderTargetNodeFactory.class);

    public boolean supportsTargetClass(Class targetClass) {
        return DataProvider.class.isAssignableFrom(targetClass);
    }

    public BindingTargetNode createTargetNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
        return new DataProviderTargetNode(parent, bean, propPath, propInstance);
    }

    public class DataProviderTargetNode extends PropertyTargetNode {
        public DataProviderTargetNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
            super(parent, bean, propPath, propInstance);
        }
        public void lazyLoadCustomTargetNodes() {
            if (propInstance == null) {
                propInstance = PropertyBindingHelper.getPropInstance(bean, propPath);
            }

            if (propInstance instanceof DataProvider) {
                Thread dataProvideNodeThread = new Thread(new Runnable() {
                   public void run(){
                        final DataProvider dp = (DataProvider)propInstance;
                        try{
                            final FieldKey[] keys = dp.getFieldKeys();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run(){
                                    for (int i = 0; keys != null && i < keys.length; i++) {
                                        DataProviderTargetNode.super.add(new FieldKeyNode(DataProviderTargetNode.this, bean, propPath, dp, keys[i]));
                                    }
                                    DataProviderTargetNode.super.add(new OptionsNode(DataProviderTargetNode.this, dp, keys));
                                    DataProviderTargetNode.super.add(new SelectItemsNode(DataProviderTargetNode.this, dp, keys));
                                }
                            });
                        }catch(Exception exc){
                            ErrorManager.getDefault().notify(exc);
                        }
                   }
                });
               dataProvideNodeThread.setPriority(Thread.MIN_PRIORITY);
               dataProvideNodeThread.start();
            }
        }

        public class FieldKeyNode extends BindingTargetNode {
            protected DesignBean bean;
            protected PropertyDescriptor[] propPath;
            protected DataProvider provider;
            protected FieldKey key;
            public FieldKeyNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, DataProvider provider, FieldKey key) {
                super(parent);
                this.bean = bean;
                this.propPath = propPath;
                this.provider = provider;
                this.key = key;
            }
            public boolean lazyLoad() {
                try {
                    BeanInfo bi = Introspector.getBeanInfo(getTargetTypeClass());
                    PropertyDescriptor[] pds = bi.getPropertyDescriptors();
                    for (int i = 0; pds != null && i < pds.length; i++) {
                        if (pds[i].getReadMethod() != null) {
                            PropertyDescriptor[] newPath = new PropertyDescriptor[propPath.length + 1];
                            System.arraycopy(propPath, 0, newPath, 0, propPath.length);
                            newPath[newPath.length - 1] = pds[i];
                            super.add(_createTargetNode(this, bean, newPath, null));
                        }
                    }
                } catch (Exception x) {
//                    x.printStackTrace();
                }
                return true;
            }
            public boolean isValidBindingTarget() {
                return true;
            }
            public String getBindingExpressionPart() {
                return "value['" + key.getFieldId() + "']"; //NOI18N
            }
            public Class getTargetTypeClass() {
                return provider.getType(key);
            }
            public String getDisplayText(boolean enableNode) {
                String tn = getTargetTypeDisplayName();
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");  //NOI18N
                if (!enableNode) {
                    sb.append("<font color=\"gray\">");  //NOI18N
                }
                sb.append(bundle.getMessage("key"));  //NOI18N
                sb.append(" ");  //NOI18N
                if (enableNode) {
                    sb.append("<b>");  //NOI18N
                }
                sb.append(key.getDisplayName());
                if (enableNode) {
                    sb.append("</b>");  //NOI18N
                }
                sb.append(" &nbsp; <font size=\"-1\"><i>");  //NOI18N
                sb.append(tn);
                sb.append("</i></font>");  //NOI18N
                if (!enableNode) {
                    sb.append("</font>");  //NOI18N
                }
                sb.append("</html>");  //NOI18N
                return sb.toString();
            }
        }
        
        public class OptionsNode extends SelectItemsNode {
            public OptionsNode(BindingTargetNode parent, DataProvider provider, FieldKey[] cols) {
                super(parent, provider, cols);
            }
            public String getNodeDisplayName() {
                return bundle.getMessage("options"); // NOI18N
            }
            public String getBindingExpressionPart() {
                return "options['" + getColumnPicks() + "']";    //NOI18N
            }
        }
        
        public class SelectItemsNode extends BindingTargetNode {
            protected DataProvider provider;
            FieldKey[] cols;
            public SelectItemsNode(BindingTargetNode parent, DataProvider provider, FieldKey[] cols) {
                super(parent);
                this.provider = provider;
                this.cols = cols;
                initCustomPanel();
                displayTextEnabled = getDisplayText(true);
                displayTextDisabled = getDisplayText(false);
            }
            public String getNodeDisplayName() {
                return bundle.getMessage("selectItems"); // NOI18N
            }
            protected String displayTextEnabled = null;
            protected String displayTextDisabled = null;
            public String getDisplayText(boolean enableNode) {
                if (enableNode && displayTextEnabled != null) {
                    return displayTextEnabled;
                } else if (!enableNode && displayTextDisabled != null) {
                    return displayTextDisabled;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");    //NOI18N
                if (!enableNode) {
                    sb.append("<font color=\"gray\">");    //NOI18N
                }
                if (enableNode) {
                    sb.append("<b>");    //NOI18N
                }
                sb.append(getNodeDisplayName());
                if (enableNode) {
                    sb.append("</b>");    //NOI18N
                }
                sb.append(" &nbsp; <font size=\"-1\"><i>");    //NOI18N
                sb.append(bundle.getMessage("parenItemsForListBoxOr"));    //NOI18N
                sb.append("</i></font>");    //NOI18N
                if (!enableNode) {
                    sb.append("</font>");    //NOI18N
                }
                sb.append("</html>");    //NOI18N
                return sb.toString();
            }
            public int getChildCount() { return 0; }
            public boolean lazyLoad() { return true; }
            public Class getTargetTypeClass() {
                return SelectItem[].class;
            }
            public boolean isValidBindingTarget() {
                return true;
            }
            public String getBindingExpressionPart() {
                return "selectItems['" + getColumnPicks() + "']";    //NOI18N
            }
            String getColumnPicks() {
                StringBuffer sb = new StringBuffer();
                Object o = valueCombo.getSelectedItem();
                if (o instanceof ComboDisplayColumn) {
                    sb.append(((ComboDisplayColumn)o).fieldKey.getFieldId());
                }
                o = labelCombo.getSelectedItem();
                if (o instanceof ComboDisplayColumn) {
                    sb.append(",");    //NOI18N
                    sb.append(((ComboDisplayColumn)o).fieldKey.getFieldId());
                }
                o = descrCombo.getSelectedItem();
                if (o instanceof ComboDisplayColumn) {
                    sb.append(",");    //NOI18N
                    sb.append(((ComboDisplayColumn)o).fieldKey.getFieldId());
                }
                return sb.toString();
            }
            JPanel pickerPanel = new JPanel();
            JLabel valueLabel = new JLabel(bundle.getMessage("valueField"));   //NOI18N
            JLabel labelLabel = new JLabel(bundle.getMessage("displayField"));   //NOI18N
            JLabel descrLabel = new JLabel(bundle.getMessage("tooltipField"));  //NOI18N
            JComboBox valueCombo = new JComboBox();
            JComboBox labelCombo = new JComboBox();
            JComboBox descrCombo = new JComboBox();
            void initCustomPanel() {
                ComboDisplayColumnRenderer cdcr = new ComboDisplayColumnRenderer();
                valueCombo.setRenderer(cdcr);
                labelCombo.setRenderer(cdcr);
                descrCombo.setRenderer(cdcr);
                labelCombo.addItem(bundle.getMessage("noneBrackets"));  //NOI18N
                descrCombo.addItem(bundle.getMessage("noneBrackets"));  //NOI18N
                
                // Don't get the FieldKey again, it is a slow process
                //FieldKey[] cols = provider.getFieldKey();
                if (cols != null && cols.length > 0) {
                    for (int i = 0; i < cols.length; i++) {
                        ComboDisplayColumn col =
                                new ComboDisplayColumn(cols[i], provider.getType(cols[i]));
                        valueCombo.addItem(col);
                        labelCombo.addItem(col);
                        descrCombo.addItem(col);
                    }
                }
 
                pickerPanel.setLayout(new GridBagLayout());
                pickerPanel.add(valueLabel, new GridBagConstraints(
                        0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 2, 4), 0, 0));
                pickerPanel.add(labelLabel, new GridBagConstraints(
                        1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 2, 4), 0, 0));
                pickerPanel.add(descrLabel, new GridBagConstraints(
                        2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 2, 0), 0, 0));
                pickerPanel.add(valueCombo, new GridBagConstraints(
                        0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 4), 0, 0));
                pickerPanel.add(labelCombo, new GridBagConstraints(
                        1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 4), 0, 0));
                pickerPanel.add(descrCombo, new GridBagConstraints(
                        2, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
                valueCombo.addActionListener(updateAdapter);
                labelCombo.addActionListener(updateAdapter);
                descrCombo.addActionListener(updateAdapter);
            }
            public JComponent getCustomDisplayPanel(ActionListener updateCallback) {
                this.updateCallback = updateCallback;
                return pickerPanel;
            }
            ActionListener updateCallback = null;
            ActionListener updateAdapter = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (updateCallback != null) {
                        updateCallback.actionPerformed(e);
                    }
                }
            };
        }
        
        public class ComboDisplayColumn {
            public FieldKey fieldKey;
            public Class fieldType;
            public ComboDisplayColumn(FieldKey fieldKey, Class fieldType) {
                this.fieldKey = fieldKey;
                this.fieldType = fieldType;
            }
        }
        
        public class ComboDisplayColumnRenderer extends DefaultListCellRenderer {
            public Component getListCellRendererComponent(JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ComboDisplayColumn) {
                    ComboDisplayColumn cdc = (ComboDisplayColumn)value;
                    String tn = cdc.fieldType != null ? cdc.fieldType.getName() : "";
                    StringBuffer sb = new StringBuffer();
                    sb.append("<html><b>"); //NOI18N
                    sb.append(cdc.fieldKey.getDisplayName());
                    sb.append("</b> &nbsp; <font size=\"-1\"><i>"); //NOI18N
                    sb.append(tn);
                    sb.append("</i></font></html>"); //NOI18N
                    this.setText(sb.toString());
                } else {
                    this.setText(bundle.getMessage("noneBrackets")); //NOI18N
                }
                return this;
            }
        }
    }
}
