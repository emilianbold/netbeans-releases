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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.project;

import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeEvent;
import org.netbeans.spi.project.support.ant.*;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 */
public final class ExtraSourceForBinaryQueryImplementation implements SourceForBinaryQueryImplementation {

    
    public ExtraSourceForBinaryQueryImplementation() {
    }
    
     /**
     * We always return a result here, never null, otherwise we loose the ability to 
     * fire a result change when new project gets opened.
     * @return
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return new Result(binaryRoot);
    }
    
    public static SourceForBinaryQueryImplementation createProjectInstance(AntProjectHelper helper, PropertyEvaluator evaluator) {
        return new ExtraProjectSourceForBinaryQueryImpl(helper, evaluator);
    }

    private static class Result implements SourceForBinaryQuery.Result {
        
        private URL binaryRoot;
        private PropertyChangeListener listener;
        private final ChangeSupport cs = new ChangeSupport(this);
        private Map<Project, SourceForBinaryQuery.Result> cachedResults;
        private final Object LOCK = new Object();
        private ChangeListener changeListener;

        private Result(URL binaryRoot) {
            this.binaryRoot = binaryRoot;
            changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    cs.fireChange();
                }
                
            };
            listener = new PropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent evt ) {
                    if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                        synchronized (LOCK) {
                            if (cachedResults != null) {
                                Map<Project, SourceForBinaryQuery.Result> newRes = results();
                                Set<Project> oldprjs = new HashSet<Project>(cachedResults.keySet());
                                Set<Project> newprjs = new HashSet<Project>(newRes.keySet());
                                newprjs.removeAll(cachedResults.keySet());
                                oldprjs.removeAll(newRes.keySet());
                                for (Project p : oldprjs) {
                                    SourceForBinaryQuery.Result res = cachedResults.get(p);
                                    res.removeChangeListener(changeListener);
                                }
                                for (Project p : newprjs) {
                                    SourceForBinaryQuery.Result res = newRes.get(p);
                                    res.addChangeListener(changeListener);
                                }
                                cachedResults = newRes; 
                            }
                        }
                        cs.fireChange();
                    }
                }
            };
            OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(listener, OpenProjects.getDefault()));
        }

        public FileObject[] getRoots() {
            List<FileObject> fos = new ArrayList<FileObject>();
            synchronized (LOCK) {
                if (cachedResults == null) {
                    cachedResults = results();
                    for (SourceForBinaryQuery.Result result : cachedResults.values()) {
                        result.addChangeListener(changeListener);
                    }
                }
                for (SourceForBinaryQuery.Result result : cachedResults.values()) {
                    FileObject[] f = result.getRoots();
                    fos.addAll(Arrays.asList(f));
                }
            }
            return fos.toArray(new FileObject[fos.size()]);
        }
        
        private Map<Project, SourceForBinaryQuery.Result> results() {
            HashMap<Project, SourceForBinaryQuery.Result> res = new HashMap<Project,SourceForBinaryQuery.Result>();
            Project[] prj = OpenProjects.getDefault().getOpenProjects();
            for (Project project : prj) {
                //TODO ask jtulach why this lookup query fails..
                ExtraProjectSourceForBinaryQueryImpl impl = project.getLookup().lookup(ExtraProjectSourceForBinaryQueryImpl.class);
                //workaround to make it works..
                if (impl == null) {
                    for (SourceForBinaryQueryImplementation im : project.getLookup().lookupAll(SourceForBinaryQueryImplementation.class)) {
                        if (im instanceof ExtraProjectSourceForBinaryQueryImpl) {
                            impl = (ExtraProjectSourceForBinaryQueryImpl)im;
                        }
                    }   
                }
                if (impl != null) {
                    SourceForBinaryQuery.Result result = impl.findSourceRoots(binaryRoot);
                    if (result != null) {
                        res.put(project, result);
                    }
                }
            }
            return res;
        }

        public void addChangeListener(ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            cs.addChangeListener(l);
            
        }

        public void removeChangeListener(ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            cs.removeChangeListener(l);
        }
        
    }

}
