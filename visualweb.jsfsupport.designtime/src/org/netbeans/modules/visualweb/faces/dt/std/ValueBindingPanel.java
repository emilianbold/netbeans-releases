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
package org.netbeans.modules.visualweb.faces.dt.std;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.faces.component.UIData;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectMany;
import javax.faces.component.html.HtmlDataTable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.netbeans.modules.visualweb.faces.dt.std.table.HtmlDataTableState;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

public class ValueBindingPanel extends JPanel implements EnhancedCustomPropertyEditor {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(ValueBindingPanel.class);

    protected DesignProperty liveProperty;
    protected boolean isValueProperty;
    protected FacesDesignProperty facesDesignProperty;
    protected JTabbedPane tabs = new JTabbedPane();

    protected ObjectBindingPanel2 objectPanel = null; //new ObjectBindingPanel(this);
    protected DataModelBindingPanel dmPanel = null; //new DataModelBindingPanel(this);
    protected RowDataBindingPanel rowDataPanel = null; //new RowDataBindingPanel(this);
    protected ColumnDataBindingPanel columnDataPanel = null; //new ColumnDataBindingPanel(this);

    protected JTextField valueTextField = new JTextField();
    protected GridBagLayout gridBagLayout1 = new GridBagLayout();
    protected JLabel valueLabel = new JLabel();
    protected ValueBindingPropertyEditor vbpe;

    public ValueBindingPanel(ValueBindingPropertyEditor vbpe, DesignProperty lp) {
        this.vbpe = vbpe;
        this.liveProperty = lp;
        // This type of code REALLY sucks, but I'm stuck with it for now :( TODO
        isValueProperty = "value".equals(liveProperty.getPropertyDescriptor().getName());
        this.facesDesignProperty = lp instanceof FacesDesignProperty ? (FacesDesignProperty)lp : null;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object bean = lp.getDesignBean().getInstance();

        // find the current value
        String currentBinding = lp.getValueSource();
        valueTextField.setText(currentBinding);

        if (isValueProperty && bean instanceof HtmlDataTable) {
            dmPanel = new DataModelBindingPanel(this, null, lp);
            tabs.add(dmPanel, bundle.getMessage("bindToDb")); //NOI18N
        } else if (isValueProperty && bean instanceof UISelectItems) {
            columnDataPanel = new ColumnDataBindingPanel(this, null, lp);
            tabs.add(columnDataPanel, bundle.getMessage("bindDiplayToDb")); //NOI18N
        } else if (!isValueProperty || !(bean instanceof UISelectMany)) {
            rowDataPanel = new RowDataBindingPanel(this, null, lp);
            tabs.add(rowDataPanel, bundle.getMessage("bindToDb")); //NOI18N
        }

        objectPanel = new ObjectBindingPanel2(this, lp);
        tabs.add(objectPanel, bundle.getMessage("bindToObj")); //NOI18N
        if (!isValueProperty) {
            tabs.setSelectedComponent(objectPanel);
        }

        initializing = false;
    }

    public Object getPropertyValue() throws IllegalStateException {
        String vb = getValueBinding();

        // special handling for UISelectItems
        DesignBean bean = liveProperty.getDesignBean();
        if (bean.getInstance() instanceof UISelectItems && isValueProperty) { //NOI18N

            // if we're blanking out the value binding for 'value'
            // replug-in the default items (or nuke them if not)
        	//MBOHM for 6194849, no harm in calling this line here, and would be more risky to change
            vb = HtmlDesignInfoBase.maybeSetupDefaultSelectItems(bean, vb);
            // check and see if we need a converter
            //MBOHM 6194849 //HtmlDesignInfoBase.maybeSetupConverter(bean, selectItemsValueType);
        }
        // special handling for UIData bound to a rowset
        else if (bean.getInstance() instanceof UIData &&
            "value".equals(liveProperty.getPropertyDescriptor().getName())) {

            vb = HtmlDataTableState.maybeGenerateRowsetColumns(bean, vb);
        }

        if (vb != null && vb.length() == 0) {
            vb = null;
        }
        return vb;
    }

    protected int selectItemsValueType = 0;
    public void setSelectItemsValueType(int sqlType) {
        this.selectItemsValueType = sqlType;
    }

    protected boolean initializing = true;

    public void setValueBinding(String valueBinding) {
        if (initializing) {
            return;
        }
        valueTextField.setText(valueBinding);
    }

    public String getValueBinding() {
        return valueTextField.getText();
    }

    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        valueLabel.setText(bundle.getMessage("currValSetting")); //NOI18N
        this.add(valueLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
        this.add(valueTextField, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 8, 4, 8), 0, 0));
        this.add(tabs, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
//        valueTextField.getDocument().addUndoableEditListener(new UndoableEditListener() {
//            public void undoableEditHappened(UndoableEditEvent e) {
//                if (initializing)
//                    return;
//                firePropertyChange(null, null, null);
//                if (vbpe != null) {
//                    vbpe.setAsText(valueTextField.getText());
//                }
//            }
//        });
    }
}
