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

package org.netbeans.modules.ant.freeform;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 * Manages list of subprojects.
 * @author Jesse Glick
 * @see "#46713"
 */
final class Subprojects implements SubprojectProvider {
    
    private final FreeformProject project;
    
    public Subprojects(FreeformProject project) {
        this.project = project;
    }

    public Set<? extends Project> getSubprojects() {
        return new LazySubprojectsSet();
    }
    
    /**Analyzes subprojects element.
     * Works in two modes:
     * 1. Lazy mode, detects only if the set of subprojects would be empty.
     *    Enabled if the provided subprojects parameter is null.
     *    The subprojects set is empty if createSubprojects(null) == null.
     *    Used by the LazySubprojectsSet, see #58639. Works as fast as possible.
     * 2. Full mode, creates the set of projects.
     *    Enabled if the subprojects parameter is not-null.
     *    The provided instance of Set is filled by the projects and returned.
     *
     * This method never allocates a new set.
     */
    private Set<Project> createSubprojects(Set<Project> subprojects) {
        Element config = project.getPrimaryConfigurationData();
        Element subprjsEl = Util.findElement(config, "subprojects", FreeformProjectType.NS_GENERAL); // NOI18N
        if (subprjsEl != null) {
            for (Element prjEl : Util.findSubElements(subprjsEl)) {
                assert prjEl.getLocalName().equals("project") : "Bad element " + prjEl + " in <subprojects> for " + project;
                String rawtext = Util.findText(prjEl);
                assert rawtext != null : "Need text content for <project> in " + project;
                String evaltext = project.evaluator().evaluate(rawtext);
                if (evaltext == null) {
                    continue;
                }
                FileObject subprjDir = project.helper().resolveFileObject(evaltext);
                if (subprjDir == null) {
                    continue;
                }
                try {
                    Project p = ProjectManager.getDefault().findProject(subprjDir);
                    if (p != null) {
                        if (subprojects == null)
                            return Collections.emptySet();
                        else
                            subprojects.add(p);
                    }
                } catch (IOException e) {
                    org.netbeans.modules.ant.freeform.Util.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }

        return subprojects;
    }

    public void addChangeListener(ChangeListener listener) {
        // XXX
    }

    public void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
    /**Fix for #58639: the subprojects should be loaded lazily, so invoking the popup menu
     * with "Open Required Projects" is fast.
     */
    private final class LazySubprojectsSet implements Set<Project> {
        
        private Set<Project> delegateTo = null;
        
        private synchronized Set<Project> getDelegateTo() {
            if (delegateTo == null) {
                delegateTo = createSubprojects(new HashSet<Project>());
            }
            
            return delegateTo;
        }
        
        public boolean contains(Object o) {
            return getDelegateTo().contains(o);
        }
        
        public boolean add(Project p) {
            throw new UnsupportedOperationException();
        }
        
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }
        
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }
        
        public boolean addAll(Collection<? extends Project> c) {
            throw new UnsupportedOperationException();
        }
        
        public boolean containsAll(Collection c) {
            return getDelegateTo().containsAll(c);
        }
        
        public <T> T[] toArray(T[] a) {
            return getDelegateTo().toArray(a);
        }
        
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        public int size() {
            return getDelegateTo().size();
        }
        
        public synchronized boolean isEmpty() {
            if (delegateTo == null) {
                return createSubprojects(null) == null;
            } else {
                return delegateTo.isEmpty();
            }
        }
        
        public Iterator<Project> iterator() {
            return getDelegateTo().iterator();
        }
        
        public Object[] toArray() {
            return getDelegateTo().toArray();
        }

        public int hashCode() {
            return getDelegateTo().hashCode();
        }

        public boolean equals(Object obj) {
            return getDelegateTo().equals(obj);
        }

        public String toString() {
            return getDelegateTo().toString();
        }
        
    }
    
}
