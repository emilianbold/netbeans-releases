/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

/** EjbRefTableModel - table model for EJB references and EJB Local References
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class EjbRefTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbRefName"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbRefType"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbInterfaceType"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbHome"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbInterface"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_EjbLink"),
            NbBundle.getMessage(EjbRefTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		CommonDDBean ref = getEjbRef(row);
                if (ref instanceof EjbRef) {
                    EjbRef param = (EjbRef)ref;
                    if (column == 0) param.setEjbRefName((String)value);
                    else if (column == 1) param.setEjbRefType((String)value);
                    else if (column == 3) param.setHome((String)value);
                    else if (column == 4) param.setRemote((String)value);
                    else if (column == 5) param.setEjbLink((String)value);
                    else if (column == 6) param.setDescription((String)value);
                } else {
                    EjbLocalRef param = (EjbLocalRef)ref;
                    if (column == 0) param.setEjbRefName((String)value);
                    else if (column == 1) param.setEjbRefType((String)value);
                    else if (column == 3) param.setLocalHome((String)value);
                    else if (column == 4) param.setLocal((String)value);
                    else if (column == 5) param.setEjbLink((String)value);
                    else if (column == 6) param.setDescription((String)value);
                }
	}


	public Object getValueAt(int row, int column)
	{
                CommonDDBean ref = getEjbRef(row);
                if (ref instanceof EjbRef) {
                    EjbRef param = (EjbRef)ref;
                    if (column == 0) return param.getEjbRefName();
                    else if (column == 1) return param.getEjbRefType();
                    else if (column == 2) return "Remote"; //NOI18N
                    else if (column == 3) return param.getHome();
                    else if (column == 4) return param.getRemote();
                    else if (column == 5) return param.getEjbLink();
                    else {
                        String desc = param.getDefaultDescription();
                        return desc==null?null:desc.trim();
                    }
                } else {
                    EjbLocalRef param = (EjbLocalRef)ref;
                    if (column == 0) return param.getEjbRefName();
                    else if (column == 1) return param.getEjbRefType();
                    else if (column == 2) return "Local"; //NOI18N
                    else if (column == 3) return param.getLocalHome();
                    else if (column == 4) return param.getLocal();
                    else if (column == 5) return param.getEjbLink();
                    else {
                        String desc = param.getDefaultDescription();
                        return desc==null?null:desc.trim();
                    }
                }

	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                CommonDDBean param=null;
                WebApp webApp = (WebApp)getParent();
                String interfaceType = (String)values[2];
                if ("Remote".equals(interfaceType)) {
                    param=webApp.createBean("EjbRef"); //NOI18N
                    ((EjbRef)param).setEjbRefName((String)values[0]);
                    ((EjbRef)param).setEjbRefType((String)values[1]);
                    ((EjbRef)param).setHome((String)values[3]);
                    ((EjbRef)param).setRemote((String)values[4]);
                    String link = (String)values[5];
                    if (link.length()>0) ((EjbRef)param).setEjbLink(link);
                    String desc = (String)values[6];
                    if (desc.length()>0) ((EjbRef)param).setDescription(desc);
                    int row = webApp.sizeEjbRef();
                    webApp.addEjbRef((EjbRef)param);
                    getChildren().add(row,param);
                    fireTableRowsInserted(row, row);
                } else {
                    param=webApp.createBean("EjbLocalRef"); //NOI18N
                    ((EjbLocalRef)param).setEjbRefName((String)values[0]);
                    ((EjbLocalRef)param).setEjbRefType((String)values[1]);
                    ((EjbLocalRef)param).setLocalHome((String)values[3]);
                    ((EjbLocalRef)param).setLocal((String)values[4]);
                    String link = (String)values[5];
                    if (link.length()>0) ((EjbLocalRef)param).setEjbLink(link);
                    String desc = (String)values[6];
                    if (desc.length()>0) ((EjbLocalRef)param).setDescription(desc);
                    webApp.addEjbLocalRef((EjbLocalRef)param);
                    getChildren().add(param);
                    int row = getRowCount() - 1;
                    fireTableRowsInserted(row, row);
                }
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
            //try {
                CommonDDBean ref = getEjbRef(row);
                WebApp webApp = (WebApp)getParent();
                String interfaceType = (String)values[2];
                if (ref instanceof EjbRef) {
                    EjbRef param = (EjbRef)ref;
                    //if ("Remote".equals(interfaceType)) { //NOI18N
                        param.setEjbRefName((String)values[0]);
                        param.setEjbRefType((String)values[1]);
                        param.setHome((String)values[3]);
                        param.setRemote((String)values[4]);
                        String ejbLink = (String)values[5];
                        param.setEjbLink(ejbLink.length()>0?ejbLink:null);
                        String desc = (String)values[6];
                        param.setDescription(desc.length()>0?desc:null);
                        fireTableRowsUpdated(row,row);
                    /*} else {
                        webApp.removeEjbRef(param);
                        getChildren().remove(row);
                        //fireTableRowsDeleted(row, row);
                        EjbLocalRef localRef = (EjbLocalRef)webApp.createBean("EjbLocalRef"); //NOI18N
                        localRef.setEjbRefName((String)values[0]);
                        localRef.setEjbRefType((String)values[1]);
                        localRef.setLocalHome((String)values[3]);
                        localRef.setLocal((String)values[4]);
                        localRef.setEjbLink((String)values[5]);
                        String desc = (String)values[6];
                        localRef.setDescription(desc.length()>0?desc:null);
                        webApp.addEjbLocalRef(localRef);
                        getChildren().add(row,param);
                        //fireTableRowsInserted(row, row);
                        fireTableRowsUpdated(row,row);
                    }*/
                } else {
                    EjbLocalRef param = (EjbLocalRef)ref;
                    //if ("Local".equals(interfaceType)) { //NOI18N
                        param.setEjbRefName((String)values[0]);
                        param.setEjbRefType((String)values[1]);
                        param.setLocalHome((String)values[3]);
                        param.setLocal((String)values[4]);
                        String ejbLink = (String)values[5];
                        param.setEjbLink(ejbLink.length()>0?ejbLink:null);
                        String desc = (String)values[6];
                        param.setDescription(desc.length()>0?desc:null);
                        fireTableRowsUpdated(row,row);
                    /*} else {
                        webApp.removeEjbLocalRef(param);
                        getChildren().remove(row);
                        //fireTableRowsDeleted(row, row);
                        EjbRef remoteRef = (EjbRef)webApp.createBean("EjbRef"); //NOI18N
                        remoteRef.setEjbRefName((String)values[0]);
                        remoteRef.setEjbRefType((String)values[1]);
                        remoteRef.setHome((String)values[3]);
                        remoteRef.setRemote((String)values[4]);
                        remoteRef.setEjbLink((String)values[5]);
                        String desc = (String)values[6];
                        remoteRef.setDescription(desc.length()>0?desc:null);
                        webApp.addEjbRef(remoteRef);
                        getChildren().add(row,param);
                        //fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                        fireTableRowsUpdated(row,row);
                    }*/
                }    
            //} catch (ClassNotFoundException ex) {}
	}
        
	public void removeRow(int row)
	{
            WebApp webApp = (WebApp)getParent();
            CommonDDBean ref = getEjbRef(row);
            if (ref instanceof EjbRef) {
                webApp.removeEjbRef((EjbRef)ref);
            } else {
                webApp.removeEjbLocalRef((EjbLocalRef)ref);
            }
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
        
        CommonDDBean getEjbRef(int row) {
            return (CommonDDBean)getChildren().get(row);
        }
}