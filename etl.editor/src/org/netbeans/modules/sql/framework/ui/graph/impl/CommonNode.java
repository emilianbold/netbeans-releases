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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class CommonNode extends DataNode {
    
    private ResourceBundle resBundle = null;

    /** Creates a new instance of LocalizedNode */
    public CommonNode(DataObject obj, Children children) {
        super(obj, children);

        try {
            resBundle = NbBundle.getBundle((String) obj.getPrimaryFile().getAttribute("SystemFileSystem.localizingBundle"));

        } catch (MissingResourceException ex) {
            //@TODO Log this exception
        }
    }

    protected String getLocalizedValue(String key) {
        if (resBundle == null || key == null) {
            return key;
        }

        try {
            key = resBundle.getString(key);
        } catch (MissingResourceException ex) {
            //@TODO Log this exception
        }

        return key;
    }

    public SystemAction[] getActions() {
        return null;
    }

    
    public String toString() {
        return this.getDisplayName();
    }
}
