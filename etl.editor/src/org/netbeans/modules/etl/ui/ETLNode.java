/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.etl.ui;

import org.netbeans.modules.etl.ui.view.cookies.ExecuteTestCookie;
import org.netbeans.modules.etl.ui.view.cookies.SelectTablesCookie;
import org.openide.ErrorManager;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;


/** A node to represent this object. */
public class ETLNode extends DataNode {
    
    private ETLDataObject dObj;
    
    public ETLNode(ETLDataObject obj) {
        this(obj, Children.LEAF);
        this.dObj = obj;
    }
    
    private ETLNode(DataObject obj, Children ch) {
        super(obj, ch);
        setIconBaseWithExtension("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
        init();
    }
    
    private void init() {
        CookieSet cs = getCookieSet();
        ETLDataObject dataObj = (ETLDataObject) getDataObject();
        //cs.add(new ETLEditorShowCookie(element));
        cs.add(new ExecuteTestCookie());
        cs.add(new SelectTablesCookie());
    }
    
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.createPropertiesSet();        
        try {
            Property nameProp = new PropertySupport.Reflection(this.dObj, String.class,
                    "getName", null);
            Property execProp = new PropertySupport.Reflection(this.dObj.getETLDefinition().getSQLDefinition(), 
                    String.class, "getExecutionStrategyStr", null);
            
            nameProp.setName("Collaboration Name");
            execProp.setName("Execution Strategy");
            set.put(nameProp);
            set.put(execProp);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        sheet.put(set);
        return sheet;
    }
}
