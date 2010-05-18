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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.web.ui.component.*;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableDesignHelper;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;

/**
 * DesignInfo for the <code>TableColumn</code> component. The following behavior is
 * implemented:
 * <ul>
 * <li>Upon component creation, pre-populate with one Static Text.</li>
 * </ul>
 *
 * @author Winston Prakash
 */
public class TableColumnDesignInfo extends AbstractDesignInfo {

    private static final String WIDTH_PROPERTY = "width";

    public TableColumnDesignInfo() {
        super(TableColumn.class);
    }

    public Result beanCreatedSetup(DesignBean tableColumnBean) {
        int colNo = tableColumnBean.getBeanParent().getChildBeanCount();
        String columnHeaderText = DesignMessageUtil.getMessage(TableColumnDesignInfo.class, "tableColumn.headerText", new Object[]{String.valueOf(colNo)});
        DesignProperty headerTextProperty = tableColumnBean.getProperty("headerText"); //NOI18N
        headerTextProperty.setValue(columnHeaderText);
        DesignProperty widthProperty = tableColumnBean.getProperty("width"); //NOI18N
        widthProperty.setValue(String.valueOf(200));
        DesignContext context = tableColumnBean.getDesignContext();
        if (context.canCreateBean(StaticText.class.getName(), tableColumnBean, null)) {
            DesignBean staticTextBean = context.createBean(StaticText.class.getName(), tableColumnBean, null);
            DesignProperty textProperty = staticTextBean.getProperty("text"); //NOI18N
            textProperty.setValue(staticTextBean.getBeanInfo().getBeanDescriptor().getDisplayName());
        }
        return Result.SUCCESS;
    }

    /** {@inheritDoc} */
    public Result beanDeletedCleanup(DesignBean bean) {
        // Adjust table width if table column width is et in pixels
        int oldColumnWidth = -1;
        Object oldValue = bean.getProperty(WIDTH_PROPERTY).getValue();
        if (oldValue != null) {
            String oldColumnWidthStr = (String) oldValue;
            if (oldColumnWidthStr.indexOf("%") == -1) {
                //NOI18N
                try {
                    oldColumnWidth = Integer.parseInt(oldColumnWidthStr);
                } catch (Exception exc) {
                }
            }
        }
        if (bean.getBeanParent() != null) {
            TableDesignHelper.adjustTableWidth(bean.getBeanParent().getBeanParent(), oldColumnWidth, 0);
        }
        return Result.SUCCESS;
    }

    /**
     * {@inheritDoc}
     * Accept only StaticText, Button or Field as Child
     */
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if (childClass.isAssignableFrom(StaticText.class) || childClass.isAssignableFrom(Button.class) || childClass.isAssignableFrom(TextField.class) || childClass.isAssignableFrom(TextArea.class) || childClass.isAssignableFrom(StaticText.class) || childClass.isAssignableFrom(Label.class) || childClass.isAssignableFrom(DropDown.class) || childClass.isAssignableFrom(Hyperlink.class) || childClass.isAssignableFrom(ImageHyperlink.class) || childClass.isAssignableFrom(Checkbox.class) || childClass.isAssignableFrom(RadioButton.class) || childClass.isAssignableFrom(ImageComponent.class) || childClass.isAssignableFrom(PanelGroup.class) || childClass.isAssignableFrom(Message.class)) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * Accept only TableRowGroup as Parent
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class parentClass) {
        return parentBean.getInstance().getClass().isAssignableFrom(TableRowGroup.class);
    }

    /**
     * Accept only Reult Set (may be not required in future) or  TableDataProvider as links
     *
     * {@inheritDoc}
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    /**
     * TBD - remove the earlier child and add the source bean as child
     *
     * {@inheritDoc}
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        System.out.println(targetBean);
        System.out.println(sourceBean);
        return Result.SUCCESS;
    }

    /**
     * Modify the width of the table if the column width changes
     */
    public void propertyChanged(DesignProperty property, Object oldValue) {
        String propertyName = property.getPropertyDescriptor().getName();
        if (propertyName.equals(WIDTH_PROPERTY)) {
            String columnWidth = (String) property.getValue();
            if (columnWidth != null) {
                // If not a percentage, units are in pixels.
                // Ajust the table width only if the column width is specified in pixles
                if (columnWidth.indexOf("%") == -1) {
                    TableDesignHelper.adjustTableWidth(property.getDesignBean().getBeanParent());
                }
            }
        }
    }
}
