/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
