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

package org.netbeans.modules.editor.options;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.util.actions.SystemAction;

/** MIME Option Node Representation.
 *  Each subClass of BaseOptions is represented via MIMEOptionNode.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class MIMEOptionNode extends BeanNode {
    
    private String name;
    
    /** Creates new OptionNode */
    public MIMEOptionNode(BaseOptions beanObject) throws IntrospectionException {
        super(beanObject);
        name = beanObject.getName();
    }
    
    /** Gets display name of all options node from bundle */
    public String getDisplayName(){
        return name;
    }
    
    // #7925
    public boolean canDestroy() {
        return false;
    }    

    // #18678
    public Node cloneNode() {
        try {
            return new MIMEOptionNode((BaseOptions)getBean());
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ie);
            return super.cloneNode();
        }
    }    
    
}
