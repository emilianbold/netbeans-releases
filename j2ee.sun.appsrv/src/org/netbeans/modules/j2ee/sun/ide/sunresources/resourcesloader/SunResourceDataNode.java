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
package org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/** A node to represent this object.
 *
 * @author nityad
 */
public class SunResourceDataNode extends DataNode {
    
    public SunResourceDataNode(SunResourceDataObject obj) {
        this(obj, Children.LEAF);
    }
    
    public SunResourceDataNode(SunResourceDataObject obj, Children ch) {
        super(obj, ch);
        setIconBase("org/netbeans/modules/j2ee/sun/ide/resources/ResNodeNodeIcon"); //NOI18N
    }
    
    protected SunResourceDataObject getSunResourceDataObject() {
        return (SunResourceDataObject)getDataObject();
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
