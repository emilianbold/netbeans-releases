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
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.beans.PropertyEditor;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.cookies.SaveCookie;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.RenameAction;

import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.*;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourceactions.RegisterAction;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor;

/** A node to represent this object.
 *
 * @author nityad
 */
public class DataSourceBeanDataNode extends DataNode implements java.beans.PropertyChangeListener{
    private DataSourceBean resource = null;
    public DataSourceBeanDataNode(SunResourceDataObject obj, DataSourceBean key) {
        this(obj, Children.LEAF, key);
    }
    
    public DataSourceBeanDataNode(SunResourceDataObject obj, Children ch, DataSourceBean key) {
        super(obj, ch);
        resource = key;
        setIconBase("org/netbeans/modules/j2ee/sun/ide/resources/ResNodeNodeIcon"); //NOI18N
        setShortDescription (NbBundle.getMessage (DataSourceBeanDataNode.class, "DSC_DataSourceNode"));//NOI18N
        
        key.addPropertyChangeListener(this);
        //getCookieSet().add(this);
        Class clazz = key.getClass ();
        try{
            createProperties(key, Utilities.getBeanInfo(clazz));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public javax.swing.Action getPreferredAction(){
        return SystemAction.get(PropertiesAction.class);
    }
    
    protected SunResourceDataObject getSunResourceDataObject() {
        return (SunResourceDataObject)getDataObject();
    }
    
    protected DataSourceBeanDataNode getDataSourceBeanDataNode(){
        return this;
    }
    
    protected DataSourceBean getDataSourceBean(){
        return resource;
    }
    
    protected SystemAction[] createActions(){
        return new SystemAction[]{
            SystemAction.get(RegisterAction.class),
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            SystemAction.get(PropertiesAction.class),
        };
   }
    
   public void propertyChange(java.beans.PropertyChangeEvent evt) {
       ResourceUtils.saveNodeToXml(getDataSourceBeanDataNode(), getDataSourceBean());
   }
   
   public HelpCtx getHelpCtx() {
        return null; // new HelpCtx ("AS_Res_DataSource");//NOI18N
   }
      
   protected void createProperties(Object bean, java.beans.BeanInfo info) {
       BeanNode.Descriptor d = BeanNode.computeProperties(bean, info);
       Node.Property p = new PropertySupport.ReadWrite(
       "extraParams", DataSourceBeanDataNode.class, //NOI18N
       NbBundle.getMessage(DataSourceBeanDataNode.class,"LBL_ExtParams"), //NOI18N
       NbBundle.getMessage(DataSourceBeanDataNode.class,"DSC_ExtParams") //NOI18N
       ) {
           public Object getValue() {
               return resource.getExtraParams();
           }
           
           public void setValue(Object val){
               if (val instanceof Object[])
                   resource.setExtraParams((Object[])val);
           }
           
           public PropertyEditor getPropertyEditor(){
               return new NameValuePairsPropertyEditor(resource.getExtraParams());
           }
       };
       
       Sheet sets = getSheet();
       Sheet.Set pset = Sheet.createPropertiesSet();
       pset.put(d.property);
       pset.put(p);
//       pset.setValue("helpID", "AS_Res_DataSource_Props"); //NOI18N
       sets.put(pset);
   }
   
    /* Example of adding Executor / Debugger / Arguments to node:
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(ExecSupport.PROP_EXECUTION);
        if (set == null) {
            set = new Sheet.Set();
            set.setName(ExecSupport.PROP_EXECUTION);
            set.setDisplayName(NbBundle.getMessage(SunResourceDataNode.class, "LBL_DataNode_exec_sheet"));
            set.setShortDescription(NbBundle.getMessage(SunResourceDataNode.class, "HINT_DataNode_exec_sheet"));
        }
        ((ExecSupport)getCookie(ExecSupport.class)).addProperties(set);
        // Maybe:
        ((CompilerSupport)getCookie(CompilerSupport.class)).addProperties(set);
        sheet.put(set);
        return sheet;
    }
     */
    
    // Don't use getDefaultAction(); just make that first in the data loader's getActions list
    
}
