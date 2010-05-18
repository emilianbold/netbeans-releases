/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.vmd.api.model;

import org.openide.util.Utilities;
import org.openide.util.TopologicalSortException;

import java.util.*;

/**
 * @author David Kaspar
 */
final class PresenterEventManager implements PresenterEvent {

    private ArrayList<DependencyItem> dependencies;

    private HashSet<PresenterListener> changed;

    private List<PresenterListener> topology;
    private Map<PresenterListener, HashSet<PresenterListener>> dependencyMap;

    PresenterEventManager () {
        dependencies = new ArrayList<DependencyItem> ();
    }

    void firePresenterChanged (PresenterListener listener) {
        changed.add (listener);
    }

    void addPresenterListener (DesignComponent component, Class<? extends Presenter> presenterClass, PresenterListener listener) {
        dependencies.add (new DependencyItem (component, presenterClass, listener));
        topology = null;
    }

    void removePresenterListener (DesignComponent component, Class<? extends Presenter> presenterClass, PresenterListener listener) {
        dependencies.remove (new DependencyItem (component, presenterClass, listener));
        topology = null;
    }

    void removeAllPresenterListeners (PresenterListener listener) {
        for (Iterator<DependencyItem> i = dependencies.iterator (); i.hasNext ();) {
            DependencyItem di = i.next ();
            if (di.listener == listener) {
                i.remove ();
                topology = null;
            }
        }
    }

    void prepare (boolean forceUpdateTopology) {
        changed = new HashSet<PresenterListener> (100);
        if (forceUpdateTopology)
            topology = null;
    }

    void execute () {
        if (topology == null)
            createTopology ();
        List<PresenterListener> _topology = this.topology;
        Map<PresenterListener, HashSet<PresenterListener>> _dependencyMap = this.dependencyMap;

        HashSet<PresenterListener> marked = new HashSet<PresenterListener> (100);
        for (PresenterListener presenterListener : changed) {
            if (_topology.contains (presenterListener))
                marked.add (presenterListener);
            else
                presenterListener.presenterChanged (this);
        }

        for (PresenterListener presenterListener : _topology) {
            if (marked.contains (presenterListener))
                presenterListener.presenterChanged (this);
            if (changed.contains (presenterListener)) {
                HashSet<PresenterListener> set = _dependencyMap.get (presenterListener);
                if (set == null)
                    continue;
                for (PresenterListener listener : set)
                    marked.add (listener);
            }
        }
    }

    private void createTopology () {
        HashSet<PresenterListener> unsortedSet = new HashSet<PresenterListener> ();
        HashMap<PresenterListener, HashSet<PresenterListener>> _dependencyMap = new HashMap<PresenterListener, HashSet<PresenterListener>> ();

        for (DependencyItem item : dependencies)
            item.setupTopology (unsortedSet, _dependencyMap);

        try {
            this.topology = Utilities.topologicalSort (unsortedSet, _dependencyMap);
            this.dependencyMap = _dependencyMap;
        } catch (TopologicalSortException e) {
            Debug.warning (e);
            System.err.println ("TopologicalSortException: Topological Sets:" + Arrays.toString (e.topologicalSets ())); // NOI18N
            System.err.println ("TopologicalSortException: Unsortable Set:" + Arrays.toString (e.unsortableSets ())); // NOI18N
            this.topology = Collections.emptyList ();
            this.dependencyMap = Collections.emptyMap ();
        }
    }

    public boolean isPresenterChanged (DesignComponent component, Class<? extends Presenter> presenterClass) {
        assert component != null  &&  presenterClass != null;
        for (Presenter presenter : component.getPresenters (presenterClass))
            if (presenter instanceof DynamicPresenter)
                if (changed.contains (((DynamicPresenter) presenter).getPresenterListener ()))
                    return true;
        return false;
    }

    private class DependencyItem {

        private DesignComponent component;
        private Class<? extends Presenter> presenterClass;
        private PresenterListener listener;

        public DependencyItem (DesignComponent component, Class<? extends Presenter> presenterClass, PresenterListener listener) {
            assert component != null  && presenterClass != null  &&  listener != null;
            this.component = component;
            this.presenterClass = presenterClass;
            this.listener = listener;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o)
                return true;
            if (o == null  ||  getClass () != o.getClass ())
                return false;
            final DependencyItem di = (DependencyItem) o;

            if (component != di.component)
                return false;
            if (! presenterClass.equals (di.presenterClass))
                return false;
            return listener == di.listener;
        }

        @Override
        public int hashCode () {
            int result;
            result = (component != null ? component.hashCode () : 0);
            result = 29 * result + (presenterClass != null ? presenterClass.hashCode () : 0);
            result = 29 * result + (listener != null ? listener.hashCode () : 0);
            return result;
        }

        private void setupTopology (HashSet<PresenterListener> unsortedSet, HashMap<PresenterListener,HashSet<PresenterListener>> dependencyMap) {
            Presenter presenter = component.getPresenter (presenterClass);
            if (! (presenter instanceof DynamicPresenter))
                return;
            PresenterListener presenterListener = ((DynamicPresenter) presenter).getPresenterListener ();
            if (presenterListener == listener)
                return;

            unsortedSet.add (presenterListener);
            unsortedSet.add (listener);

            HashSet<PresenterListener> set = dependencyMap.get (presenterListener);
            if (set == null) {
                set = new HashSet<PresenterListener> ();
                dependencyMap.put (presenterListener, set);
            }
            set.add (listener);
        }

    }

}
