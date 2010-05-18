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
package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.faces.*;
import org.netbeans.modules.visualweb.faces.dt.std.ColumnDataBindingCustomizerAction;
import javax.faces.component.UISelectItems;

public class UISelectItemsDesignInfo implements DesignInfo {

    public Class getBeanClass() { return UISelectItems.class; }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[] {
            new ColumnDataBindingCustomizerAction(bean),
            // This action is now "hardcoded" into the designer. All
            // FacesDesignBeans will get this action. This is done such
            // that third party libraries pick it up too.
            //new BindingsCustomizerAction(bean),
        };
    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) { return true; }
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) { return true; }
    public Result beanCreatedSetup(DesignBean bean) { return null; }
    public Result beanPastedSetup(DesignBean bean) { return null; }
    public Result beanDeletedCleanup(DesignBean bean) { return null; }
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) { return false; }
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) { return null; }

    public void beanContextActivated(DesignBean bean) {}
    public void beanContextDeactivated(DesignBean bean) {}
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}
    public void beanChanged(DesignBean bean) {}
    public void propertyChanged(final DesignProperty prop, Object oldValue) {
        if ("value".equals(prop.getPropertyDescriptor().getName())) { //NOI18N

            /* MBOHM 6194849
            String vb = prop.getValueSource();
            if (vb == null || "".equals(vb)) {
                final DesignContext context = prop.getDesignBean().getDesignContext();
                final String siBeanName = prop.getDesignBean().getInstanceName();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DesignBean siBean = context.getBeanByName(siBeanName);
                        if (siBean != null) {
                            DesignProperty valProp = siBean.getProperty("value"); //NOI18N
                            String valExpr = valProp.getValueSource();
                            if (valExpr == null || "".equals(valExpr)) {
                                valExpr = HtmlDesignInfoBase.maybeSetupDefaultSelectItems(siBean,
                                    valExpr);
                                valProp.setValueSource(valExpr);
                                HtmlDesignInfoBase.maybeSetupConverter(siBean, 0);
                            }
                        }
                    }
                });
            }
            */

            DesignBean siBean = prop.getDesignBean();

            String origValExpr = prop.getValueSource();

            //create default select items, blow away existing default select items, or leave alone existing default select items
            String valExpr = HtmlDesignInfoBase.maybeSetupDefaultSelectItems(siBean, origValExpr);

            //if we created new default select items, set this si's value property to point to those default select items
            if (origValExpr == null || origValExpr.length() < 1) {
                if (valExpr != null && valExpr.length() > 0) {
                    prop.setValueSource(valExpr);   //this will cause the propertyChanged method to be called again
                    return;
                }
            }

            UISelectItemsDesignInfo.maybeSetupConverter(siBean);

        }
    }

    public static void maybeSetupConverter(DesignBean siBean) {
        Object instance = siBean.getInstance();
        if (! (instance instanceof UISelectItems)) {
            throw new IllegalArgumentException("siBean's instance was " + instance + ", which is not of type UISelectItems");
        }
        DesignProperty prop = siBean.getProperty("value");  //NOI18N
        String valExpr = prop.getValueSource();
        if (valExpr == null) {
            HtmlDesignInfoBase.maybeSetupConverter(siBean, 0);
            return;
        }
        String beginJargon = ".selectItems['";  //NOI18N
        String endJargon = "']";    //NOI18N
        int beginJargonIndex = valExpr.indexOf(beginJargon);
        if (beginJargonIndex >= 0) {
            String resultSetExpr = valExpr.substring(0,beginJargonIndex);
            resultSetExpr += "}"; //NOI18N
            DesignContext dcontext = siBean.getDesignContext();
            if (dcontext instanceof FacesDesignContext) {
                FacesDesignContext fdcontext = (FacesDesignContext)dcontext;
                FacesContext fcontext = fdcontext.getFacesContext();
                Application application = fcontext.getApplication();
                ValueBinding binding = application.createValueBinding(resultSetExpr);
                Object value = null;
                try {
                    value = binding.getValue(fcontext);
                }
                catch (EvaluationException ee) {
                    //let value be null
                }
                if (value instanceof ResultSet) {
                    ResultSet rs = (ResultSet)value;
                    int endJargonIndex = valExpr.indexOf(endJargon, beginJargonIndex);
                    if (endJargonIndex >= beginJargonIndex) {
                        String columnStr = valExpr.substring(beginJargonIndex + beginJargon.length(), endJargonIndex);

                        //could have internal commas, in say, selectItems['employee.employeeid, employee.firstname || \' , \' || employee.lastname']
                        boolean quoteOpen = false;
                        int realCommaIndex = -1;
                        String valueField = null;
                        for (int i = 0; i < columnStr.length(); i++) {
                            char c = columnStr.charAt(i);
                            if (c == '\'') {
                                quoteOpen = !quoteOpen;
                            }
                            else if (c == ',' && !quoteOpen) {
                                realCommaIndex = i;
                                String field = columnStr.substring(0, i);
                                if (field.length() > 0) {
                                    valueField = field;
                                }
                                break;
                            }
                        }
                        //if no "real" comma was found
                        if (realCommaIndex == -1) {
                            if (columnStr.length() > 0) {
                                valueField = columnStr;
                            }
                        }

                        if (valueField != null) {
                            String columnName = valueField;
                            int lastDotIndex = valueField.lastIndexOf('.');
                            if (lastDotIndex >= 0 && lastDotIndex != valueField.length() - 1) {
                                columnName = valueField.substring(lastDotIndex + 1);
                            }
                            int sqlType = Integer.MIN_VALUE;
                            try {
                                ResultSetMetaData rsmd = rs.getMetaData();
                                int cols = rsmd.getColumnCount();
                                for (int c = 1; c <= cols; c++) {
                                    String aColumnName = rsmd.getColumnName(c);
                                    if (aColumnName.equals(columnName)) {
                                        sqlType = rsmd.getColumnType(c);
                                        break;
                                    }

                                }
                            }
                            catch (SQLException sqle) {
                                ;
                            }
                            if (sqlType != Integer.MIN_VALUE) {
                                HtmlDesignInfoBase.maybeSetupConverter(siBean, sqlType);
                                return;
                            }
                        }
                    }
                }
            }

        }
        HtmlDesignInfoBase.maybeSetupConverter(siBean, 0);
    }
    public void eventChanged(DesignEvent event) {}
}
