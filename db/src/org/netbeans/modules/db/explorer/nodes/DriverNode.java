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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.DriverListNodeInfo;
import org.openide.util.HelpCtx;

public class DriverNode extends LeafNode implements PropertyChangeListener {

    public void setInfo(DatabaseNodeInfo info) {
        super.setInfo(info);
        DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
        if (drv != null) {
            info.put(DatabaseNodeInfo.NAME, drv.getName());
            info.put(DatabaseNodeInfo.URL, drv.getURL());
            info.put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
            info.addDriverListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        DatabaseNodeInfo info = getInfo();
        String pname = evt.getPropertyName();
        Object newval = evt.getNewValue();
        DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
        if (pname.equals(DatabaseNodeInfo.NAME)) drv.setName((String)newval);
        if (pname.equals(DatabaseNodeInfo.URL)) drv.setURL((String)newval);
        if (pname.equals(DatabaseNodeInfo.PREFIX)) drv.setDatabasePrefix((String)newval);
    }
    
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Driver"); //NOI18N
    }

    public void destroy() throws IOException {
        final DriverListNodeInfo parent = (DriverListNodeInfo) getInfo().getParent();
        getInfo().delete();
    }

    /** Help context where to find more about the paste type action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DriverNode.class);
    }

}
