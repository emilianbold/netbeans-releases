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

package org.netbeans.modules.debugger.ui.models;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Vector;

import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 * @author   Jan Jancura
 */
public class WatchesNodeModel implements ExtendedNodeModel {

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/watch_16.png";

    private Vector listeners = new Vector ();
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle(WatchesNodeModel.class).getString("CTL_WatchesModel_Column_Name_Name");
        if (o instanceof Watch)
            return ((Watch) o).getExpression ();
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return TreeModel.ROOT;
        if (o instanceof Watch) {
            Watch w = (Watch) o;
            return w.getExpression () + NbBundle.getBundle(WatchesNodeModel.class).getString("CTL_WatchesModel_Column_NameNoContext_Desc");
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT)
            return WATCH;
        if (node instanceof Watch)
            return WATCH;
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(Object node) throws IOException,
                                                          UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCut(Object node) throws IOException,
                                                         UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
