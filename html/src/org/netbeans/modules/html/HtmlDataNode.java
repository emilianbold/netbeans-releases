/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html;

import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Node that represents HTML data object.
 *
 * @author  Radim Kubacki
 */
public class HtmlDataNode extends org.openide.loaders.DataNode {
    private Sheet sheet = null;
    
    /** Creates new HtmlDataNode */
    public HtmlDataNode (DataObject dobj, Children ch) {
        super (dobj, ch);
        setShortDescription (NbBundle.getMessage(HtmlDataNode.class, "LBL_htmlNodeShortDesc"));
    }

    public Node.PropertySet[] getPropertySets() {
          if(sheet == null) {
            sheet = new Sheet();

            Node.PropertySet[] tmp = super.getPropertySets();
            for(int i = 0; i < tmp.length; i++) {
              Sheet.Set set = new Sheet.Set();
              set.setName(tmp[i].getName());
              set.setShortDescription(tmp[i].getShortDescription());
              set.setDisplayName(tmp[i].getDisplayName());
              set.setValue("helpID", HtmlDataNode.class.getName() + ".PropertySheet");// NOI18N
              set.put(tmp[i].getProperties());
              sheet.put(set);
              }
          }
          
          return sheet.toArray();
    }
}
