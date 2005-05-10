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

    public Set/*<Project>*/ getSubprojects() {
        return new LazySubprojectsSet();
    }
    
    private Set/*<Project>*/ createSubprojects(Set/*<Project>*/ subprojects) {
        Element config = project.helper().getPrimaryConfigurationData(true);
        Element subprjsEl = Util.findElement(config, "subprojects", FreeformProjectType.NS_GENERAL); // NOI18N
        if (subprjsEl != null) {
            List/*<Element>*/ kids = Util.findSubElements(subprjsEl);
            Iterator it = kids.iterator();
            while (it.hasNext()) {
                Element prjEl = (Element) it.next();
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
                            return Collections.EMPTY_SET;
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
    private final class LazySubprojectsSet implements Set {
        
        private Set delegateTo = null;
        
        private synchronized Set getDelegateTo() {
            if (delegateTo == null) {
                delegateTo = createSubprojects(new HashSet());
            }
            
            return delegateTo;
        }
        
        public boolean contains(Object o) {
            return getDelegateTo().contains(o);
        }
        
        public boolean add(Object o) {
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
        
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }
        
        public boolean containsAll(Collection c) {
            throw new UnsupportedOperationException();
        }
        
        public Object[] toArray(Object[] a) {
            return getDelegateTo().toArray(a);
        }
        
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        public int size() {
            return getDelegateTo().size();
        }
        
        public boolean isEmpty() {
            if (delegateTo == null)
                return createSubprojects(null) == null;
            else
                return delegateTo.isEmpty();
        }
        
        public Iterator iterator() {
            return getDelegateTo().iterator();
        }
        
        public Object[] toArray() {
            return getDelegateTo().toArray();
        }
        
    }
    
}
