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


package org.netbeans.core.windows;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.Set;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Instance of this class keeps list of (weak references to) recently activated TopComponents.
 *
 * @author  Marek Slama
 */
final class RecentViewList implements PropertyChangeListener {

    private static RecentViewList instance;
    
    /** List of TopComponents (weak references are used). First is most recently
     * activated. */
    private List<Reference<TopComponent>> tcWeakList = new ArrayList<Reference<TopComponent>>(20);
    
    public RecentViewList (WindowManager wm) {
        // Starts listening on Registry to be notified about activated TopComponent.
        wm.getRegistry().addPropertyChangeListener(this);
    }

    
    /** Used to get array for view and for persistence */
    public TopComponent [] getTopComponents() {
        List<TopComponent> tcList = new ArrayList<TopComponent>(tcWeakList.size());
        clean();
        for (int i = 0; i < tcWeakList.size(); i++) {
            Reference<TopComponent> w = tcWeakList.get(i);
            TopComponent tc = w.get();
            if ((tc != null) && tc.isOpened()) {
                tcList.add(tc);
            }
        }
        return tcList.toArray(new TopComponent[tcList.size()]);
    }
    
    /** Used to set initial values from persistence */
    public void setTopComponents(TopComponent [] tcs) {
        tcWeakList.clear();
        for (int i = 0; i < tcs.length; i++) {
            Reference<TopComponent> wr = new WeakReference<TopComponent>(tcs[i]);
            tcWeakList.add(wr);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) evt.getNewValue();
            if (tc != null) {
                //Update list
                clean();
                Reference<TopComponent> w = find(tc);
                if (w != null) {
                    //Rearrange, put to first place
                    tcWeakList.remove(w);
                    tcWeakList.add(0,w);
                } else {
                    Reference<TopComponent> wr = new WeakReference<TopComponent>(tc);
                    tcWeakList.add(0,wr);
                }
                // #69486: ensure all components are listed
                fillList(TopComponent.getRegistry().getOpened());
            }
        }
    }
    
    /** Clean gc'ed TopComponents from list */
    private void clean () {
        int i = 0;
        while (i < tcWeakList.size()) {
            WeakReference w = (WeakReference) tcWeakList.get(i);
            TopComponent tc = (TopComponent) w.get();
            //TopComponent was gc'ed
            if (tc == null) {
                tcWeakList.remove(w);
            } else {
                i++;
            }
        }
    }
    
    /** Returns weak reference to given TopComponent if present.
     * Otherwise returns null. */
    private Reference<TopComponent> find (TopComponent tc) {
        for (int i = 0; i < tcWeakList.size(); i++) {
            Reference<TopComponent> w = tcWeakList.get(i);
            TopComponent c = w.get();
            if (tc == c) {
                return w;
            }
        }
        return null;
    }

    /** Fills list of weak references with TCs that are in given
     * input list but are not yet contained in list of weak references.
     */ 
    private void fillList(Set<TopComponent> openedTCs) {
        Reference<TopComponent> wr;
        for (TopComponent curTC: openedTCs) {
            if (find(curTC) == null) {
                if (tcWeakList.size() > 1) {
                    wr = new WeakReference<TopComponent>(curTC);
                    tcWeakList.add(1,wr);
                } else {
                    wr = new WeakReference<TopComponent>(curTC);
                    tcWeakList.add(wr);
                }
            }
        }
    }
    
}
