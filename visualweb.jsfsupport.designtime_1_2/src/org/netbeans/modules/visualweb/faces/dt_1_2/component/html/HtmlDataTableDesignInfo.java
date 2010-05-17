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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import java.sql.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.faces.*;
import com.sun.rave.faces.data.DefaultTableDataModel;
import com.sun.rave.faces.data.RowSetDataModel;
import org.netbeans.modules.visualweb.faces.dt.std.DataModelBindingCustomizerAction;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;

public class HtmlDataTableDesignInfo implements DesignInfo {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlDataTableDesignInfo.class);

    public Class getBeanClass() { return HtmlDataTable.class; }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if (childClass == null) {	//defensive
            return true;
        }
        if (UIColumn.class.isAssignableFrom(childClass) ||
                javax.faces.component.html.HtmlPanelGroup.class.isAssignableFrom(childClass) ||
                javax.faces.component.html.HtmlPanelGrid.class.isAssignableFrom(childClass)) {
            return true;
        }
        return false;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        try {
            DesignContext context = bean.getDesignContext();
            FacesDesignContext fcontext = null;
            if (context instanceof FacesDesignContext) {
                fcontext = (FacesDesignContext)context;
            }

            /* Controversial.... now that we have our own default stylesheet
               with alternating even/row colors, this just adds visual
               clutter. See the "rowClasses" property setting below.
            // set the border to 1
            DesignProperty borderProp = bean.getProperty("border"); //NOI18N
            if (borderProp != null) {
                borderProp.setValue(new Integer(1));
            }
            */

            // create and setup a default datamodel
            DesignBean dm = context.createBean(DefaultTableDataModel.class.getName(), null, null);
            dm.setInstanceName(bean.getInstanceName() + "Model", true); //NOI18N
            bean.getProperty("var").setValue("currentRow"); //NOI18N

            bean.getProperty("headerClass").setValue("list-header"); //NOI18N
            bean.getProperty("rowClasses").setValue("list-row-even,list-row-odd"); //NOI18N

            if (fcontext != null) {
                String ref = fcontext.getBindingExpr(dm);
                bean.getProperty("value").setValueSource(ref); //NOI18N
            }
            else {
                String outerName = context.getDisplayName();
                bean.getProperty("value").setValueSource("#{" + outerName + "." + //NOI18N
                    dm.getInstanceName() + "}"); //NOI18N
            }

            // create the three initial default columns
            for (int i = 1; i <= 3; i++) {
                DesignBean col = context.createBean(UIColumn.class.getName(), bean, null);
                if (col != null) {
                    DesignBean input = context.createBean(HtmlOutputText.class.getName(), col, null);
                    if (input != null) {
                        input.getProperty("value").setValueSource( //NOI18N
                            "#{currentRow['COLUMN" + i + "']}"); //NOI18N
                    }
                    DesignBean header = ((FacesDesignContext)context).createFacet("header", HtmlOutputText.class.getName(), col); //NOI18N
                    if (header != null) {
                        //header.getProperty("value").setValue("column" + i); //NOI18N
                        header.getProperty("value").setValue(bundle.getMessage("Table_Column") + i); //NOI18N
                    }
                }
            }
        }
        catch (Exception x) {
            x.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public Result beanPastedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        DesignContext context = bean.getDesignContext();

        DesignBean dm = context.getBeanByName(bean.getInstanceName() + "Model"); //NOI18N
        if (dm != null && (dm.getInstance() instanceof DefaultTableDataModel ||
                           dm.getInstance() instanceof RowSetDataModel)) {
            context.deleteBean(dm);
        }

        return Result.SUCCESS;
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[] {
            new DataModelBindingCustomizerAction(bean),
            new HtmlDataTableCustomizerAction(bean),
            new HtmlDataTableAddColumnAction(bean),
            // This action is now "hardcoded" into the designer. All
            // FacesDesignBeans will get this action. This is done such
            // that third party libraries pick it up too.
            //new BindingsCustomizerAction(bean),
        };
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return HtmlDesignInfoBase.isResultSetClass(sourceClass) ;
        // return ResultSet.class.isAssignableFrom(sourceClass);
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        // if cachedRowSetDataProvider, switch the sourceBean to
        // it's rowset.
        if ( HtmlDesignInfoBase.isCachedRowSetDP( sourceBean.getInstance() ) ) {
            DesignBean db = HtmlDesignInfoBase.findCachedRowSetBean( sourceBean ) ;
            if ( db != null ) {
                sourceBean = db ;
            }
        }
        if (sourceBean.getInstance() instanceof ResultSet) {

            HtmlDataTableState ts = new HtmlDataTableState(targetBean);
            ts.varName = HtmlDataTableState.DEFAULT_VAR_NAME;
            ts.setSourceBean(sourceBean);
            ts.refreshColumnInfo();
            ts.saveState();
        }
        return Result.SUCCESS;
    }

    public void beanContextActivated(DesignBean bean) {}
    public void beanContextDeactivated(DesignBean bean) {}
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}
    public void beanChanged(DesignBean bean) {}
    public void propertyChanged(DesignProperty prop, Object oldValue) {}
    public void eventChanged(DesignEvent event) {}
}
