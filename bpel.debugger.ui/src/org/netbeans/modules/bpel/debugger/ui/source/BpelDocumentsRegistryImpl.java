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

package org.netbeans.modules.bpel.debugger.ui.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelDocumentsRegistry;

/**
 *
 * @author Alexander Zgursky
 */
/* XXX would need a public no-arg constructor
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.debugger.api.BpelDocumentsRegistry.class)
*/
public class BpelDocumentsRegistryImpl implements BpelDocumentsRegistry {
    
    private List<Listener> myListeners = new LinkedList<Listener>();
    
    /** Creates a new instance of BpelDocumentsRegistry */
    private BpelDocumentsRegistryImpl() {
    }
    
    public void addListener(Listener listener) {
        myListeners.add(listener);
    }
    
    public void removeListener(Listener listener) {
        myListeners.remove(listener);
    }

    public void addPath(String path) {
//        System.out.println("Path is added: " + path);
    }

    public void removePath(String path) {
//        System.out.println("Path is removed: " + path);
    }

    public String[] getPaths() {
        return null;
    }

    public QName getQNameByPath(String path) {
        return null;
    }

    public String[] getPathsByQName(QName qName) {
        return null;
    }
}
