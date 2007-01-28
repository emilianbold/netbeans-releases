/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import java.sql.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.faces.*;
import org.netbeans.modules.visualweb.faces.dt.std.RowDataBindingCustomizerAction;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.UISelectItem;

public class UISelectItemDesignInfo implements DesignInfo {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(UISelectItemDesignInfo.class);

    public Class getBeanClass() { return UISelectItem.class; }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) { return true; }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) { return true; }

    public Result beanCreatedSetup(DesignBean bean) { return null; }

    public Result beanPastedSetup(DesignBean bean) { return null; }

    public Result beanDeletedCleanup(DesignBean bean) { return null; }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[] {
            new RowDataBindingCustomizerAction(bean),
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
            try {
                ResultSet rs = (ResultSet)sourceBean.getInstance();
                if (rs != null) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rsmd.getColumnCount() >= 1) {
                        String table = rsmd.getTableName(1);
                        String column = rsmd.getColumnName(1);
                        String outerName = ((FacesDesignContext)sourceBean.getDesignContext()).getReferenceName();
                        String valueRef = "#{" + outerName + "." + sourceBean.getInstanceName() + ".rowData." + column + "}";   //NOI18N
                        String dtvalue = "[" + table + "." + column + "]";   //NOI18N
                        targetBean.getProperty("value").setValue(valueRef);   //NOI18N
                        targetBean.getDesignContext().setContextData(targetBean.getInstanceName() + ".databind", dtvalue);   //NOI18N
                        //System.err.println("Linking target:" + targetBean + " src:" + sourceBean + " valueRef:" + valueRef);
                    }
                }
            }
            catch (Exception x) {
                System.err.println(bundle.getMessage("getMetaDataException")); //NOI18N
                x.printStackTrace();
            }
        }
        else if (sourceBean.getInstance() instanceof ResultSet) {
            try {
                ResultSet rs = (ResultSet)sourceBean.getInstance();
                ResultSetMetaData rsmd = rs.getMetaData();
                if (rsmd.getColumnCount() >= 1) {
                    String table = rsmd.getTableName(1);
                    String column = rsmd.getColumnName(1);
                    String outerName = ((FacesDesignContext)sourceBean.getDesignContext()).getReferenceName();
                    String valueRef = "#{" + outerName + "." + sourceBean.getInstanceName() + ".currentRow['" + column + "']}";   //NOI18N
                    targetBean.getProperty("value").setValue(valueRef);   //NOI18N
//                    String dtvalue = "[" + table + "." + column + "]";
//                    targetBean.getDesignContext().setContextData(targetBean.getInstanceName() + ".databind", dtvalue);
                    //System.err.println("Linking target:" + targetBean + " src:" + sourceBean + " valueRef:" + valueRef);
                }
            }
            catch (Exception x) {
                System.err.println(bundle.getMessage("getMetaDataException")); //NOI18N
                x.printStackTrace();
            }
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
