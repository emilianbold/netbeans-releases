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
package org.netbeans.modules.tasklist.todo;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author pzajac
 */
public class TopComponentRegistryMock implements TopComponent.Registry {

    private Set<TopComponent> opened = Collections.emptySet() ;
    private TopComponent activated;
    
    public TopComponentRegistryMock() {
    }

    
    public Set<TopComponent> getOpened() {
        return opened;
    }
    
    public void setOpened(Set<TopComponent> opened)  {
        this.opened = new HashSet<TopComponent>(opened);
    }

    public TopComponent getActivated() {
        return activated;
    }

    public void setActivated(TopComponent activated) {
        this.activated = activated;
    }

    public Node[] getCurrentNodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node[] getActivatedNodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
