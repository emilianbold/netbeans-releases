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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.projects.jbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;

/**
 * 
 * @author jqian
 */
public class JbiSubprojectProvider implements SubprojectProvider {
    
    private SubprojectProvider realProvider;
    
    private List<ChangeListener> listenerList = new ArrayList<ChangeListener>();
    
    public JbiSubprojectProvider(SubprojectProvider realProvider) {
        this.realProvider = realProvider;
    }
    
    public Set<? extends Project> getSubprojects() {
        return realProvider.getSubprojects();
    }
    
    public void addChangeListener(ChangeListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }    
    
    public void removeChangeListener(ChangeListener listener) {
       listenerList.remove(listener);
    }
    
    public void subprojectAdded(Project subproject) {
        fireStateChanged(subproject);
    }
    
    public void subprojectRemoved(Project subproject) {
        fireStateChanged(subproject);
    }
    
    private void fireStateChanged(Project subproject) {
        for (ChangeListener listener : listenerList) {
            listener.stateChanged(new ChangeEvent(subproject));
        }
    }
    
}
