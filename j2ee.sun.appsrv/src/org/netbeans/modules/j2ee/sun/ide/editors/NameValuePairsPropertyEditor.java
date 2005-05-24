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
/*
 * ParamPropertyEditorEditor.java
 *
 * Created on January 15, 2002, 2:05 PM
 *
 * Author: Shirley Chiang
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import java.util.Set;
import java.util.ResourceBundle;
import java.util.Vector;
import java.text.MessageFormat;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel;
import org.netbeans.modules.j2ee.sun.ide.editors.ui.AbstractDDTableModel;
import org.netbeans.modules.j2ee.sun.ide.editors.ui.SortableDDTableModel;

public class NameValuePairsPropertyEditor extends java.beans.PropertyEditorSupport {

    NameValuePair[] params;
    
    static final ResourceBundle bundle = 
        ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle");

    public NameValuePairsPropertyEditor(NameValuePair[] val) {
        super();
        if (val == null) 
            params = new NameValuePair[0];
        else 
            params = val;
    }
    
    public NameValuePairsPropertyEditor(Object val) {
       super();
        if (val == null) 
            params = new NameValuePair[0];
        else 
            params = getNameValuePairs(val);
    }
    
    //Fix for bug# 5023038 - selecting the property replaces string with null
    public void setAsText(String string) throws IllegalArgumentException {
    }
    
    public String getAsText() {
        return getPaintableString();
    }
    
    protected DDTablePanel panel = null;

    static protected String[] requiredToolTips = {        
        bundle.getString("tipParamName"),          //NOI18N
        bundle.getString("tipParamValue")};         //NOI18N
        //bundle.getString("tipParamDescription")};  //NOI18N
    
    public java.awt.Component getCustomEditor() {
        ParamModel model = null;
        model = new ParamModel(params);
        
        DDTablePanel panel = new DDTablePanel(new SortableDDTableModel(model),
            requiredToolTips);
//        org.openide.util.HelpCtx.setHelpIDString(panel, "AS_RTT_NameValueEditor"); //NOI18N
        return panel;
    }
        
    public boolean isPaintable () {
        return true;
    }
    
    public void paintValue (Graphics gfx, Rectangle box) {
        String s = getPaintableString();
        FontMetrics fm = gfx.getFontMetrics ();
	gfx.drawString (s, 4, (box.height - fm.getHeight ()) / 2 + 1 + fm.getMaxAscent ());
    }
    
    protected java.lang.String getPaintableString() {
        Object [] entries = (Object[]) getValue();
        if ((entries == null) || (entries.length == 0)) {
            return  bundle.getString("TXT_Param") ;       //NOI18N
        } else if (entries.length == 1) {
            return bundle.getString("TXT_OneParam");      //NOI18N
        } else {
            return MessageFormat.format(bundle.getString("TXT_MultiParam"),  //NOI18N
            new Object [] {
                Integer.toString(entries.length)
            });
        }
     }
    
    public boolean supportsCustomEditor() {
        return true;
    }

    public void setValue(Object value) {
            if(value instanceof NameValuePair[]) {
            NameValuePair[] tmpValue = (NameValuePair[])value;
            params = new NameValuePair[tmpValue.length];
            for (int i = 0; i < tmpValue.length; i++) {
                NameValuePair param = new NameValuePair();
                param.setParamName(tmpValue[i].getParamName());
                param.setParamValue(tmpValue[i].getParamValue());
                params[i] = param;
            }
        }else
            params = getNameValuePairs(value);
    }

    public Object getValue() {
        NameValuePair[] retVal = new NameValuePair[params.length];
        for (int i = 0; i < params.length; i++) {
            NameValuePair val = new NameValuePair();
            val.setParamName(params[i].getParamName());
            val.setParamValue(params[i].getParamValue());
            retVal[i] = val;
        }
        return retVal;
    }
 
    private NameValuePair[] getNameValuePairs(Object attrVal){
        java.util.Map attributeMap = (java.util.Map)attrVal;
        Set attributeKeys = attributeMap.keySet();
        java.util.Iterator it = attributeKeys.iterator();
        NameValuePair[] pairs = new NameValuePair[attributeKeys.size()];
        int i=0;
        while(it.hasNext()){
            NameValuePair pair = new NameValuePair();
            Object key = it.next();
            pair.setParamName(key.toString());
            pair.setParamValue(attributeMap.get(key).toString());
            pairs[i] = pair;
            i++;
        }
        return pairs;
    }
    
    public class ParamModel extends AbstractDDTableModel {
        public ParamModel(NameValuePair[] rows) {
            super( rows );
        }
        
        public String getColumnName(int col) {
            if (0 == col)
                return bundle.getString("colHdrParamName");  // NOI18N
            else
                return bundle.getString("colHdrParamValue");   //NOI18N
        }
        
        public java.lang.Object getValueAt(int row, int col) {
            NameValuePair rowObj = null;
            rowObj = (NameValuePair) data.get(row); 
            
            if (null != rowObj) {
                if (0 == col)
                    return rowObj.getParamName();
                else 
                    return rowObj.getParamValue();
            }
            return null;
        }
        
        public java.util.List isValueValid(java.lang.Object obj, int param) {
            Vector errors = new Vector();
            NameValuePair edit = (NameValuePair) obj;
            String editParamName = edit.getParamName();
            String editParamValue = edit.getParamValue();
            NameValuePair row = null;
            String rowParamName = null;
            if (editParamName == null || editParamName.trim().length() == 0)
                errors.add(bundle.getString("ERR_InvalidEntry")); //NOI18N
            else if (editParamValue == null || editParamValue.trim().length() == 0)
                errors.add(bundle.getString("ERR_NoValue"));  //NOI18N
            
            for (int i = 0; i < data.size(); i++) {
                row = (NameValuePair) data.elementAt(i);
                rowParamName = row.getParamName();
                if (i != param && rowParamName.equals(editParamName))
                    errors.add(bundle.getString("ERR_DuplicateEntry")); //NOI18N
            }
            return errors;
        }
        
        public boolean isEditValid(Object obj, int index) {
            return true;
        }
        
        public java.lang.String getModelName() {
            return bundle.getString("ParamModel_modelName"); //NOI18N
        }
        
        public org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModelEditor getEditor() {
            return new NameValuePairEditor();
        }
        
        protected void setValueAt(java.lang.String str, java.lang.Object obj, int param) {
            NameValuePair inVal = (NameValuePair) obj;
            if (0 == param)
                inVal.setParamName(str);
            else 
                inVal.setParamValue(str);
        }
        
        public java.lang.Object makeNewElement() {
            NameValuePair retVal = new NameValuePair();
            
            retVal.setParamName(""); //NOI18N
            retVal.setParamValue(""); //NOI18N
            //retVal.setParamDescription("");  //NOI18N
            return retVal;
        }
        
        public java.lang.Object[] getValue() {
            return data.toArray();
        }
        
        public int getColumnCount() {
            return 2;
        }
    }
}
