/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.Collection;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

class NbClipboard extends ExClipboard implements LookupListener
{
    private static NbClipboard nbClipboard;

    private Clipboard systemClipboard;
    private Convertor[] convertors;
    private Lookup.Result result;
    
    private NbClipboard() {
        super("NBClipboard");   // NOI18N
        systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        result = Lookup.getDefault().lookup(new Lookup.Template(ExClipboard.Convertor.class));
        result.addLookupListener(this);
        resultChanged(null);
    }

    static synchronized NbClipboard getDefault() {
        if (nbClipboard == null)
            nbClipboard = new NbClipboard();
        return nbClipboard;
    }
    
    protected synchronized Convertor[] getConvertors () {
        return convertors;
    }

    public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
        systemClipboard.setContents(contents, owner);
        fireClipboardChange();
    }

    public synchronized Transferable getContents(Object requestor) {
        return systemClipboard.getContents(requestor);
    }
    
    public synchronized void resultChanged(LookupEvent ev) {
        Collection c = result.allInstances();
        Convertor[] temp = new Convertor[c.size()];
        convertors = (Convertor[]) c.toArray(temp);
    }
}
