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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wag.manager.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author nam
 */
public class WagSearchResults {

    public static final String WAG_HOME = System.getProperty("netbeans.user") +
            File.separator + "config" + File.separator + "WebApiGateway"; // NOI18N

    public static final String PROP_NAME = "searchResults";
    private static WagSearchResults instance;
    private SortedSet<WagSearchResult> results;
    private PropertyChangeSupport pps;

    private WagSearchResults() {
        results = new TreeSet<WagSearchResult>();
        pps = new PropertyChangeSupport(this);
    }

    public synchronized static WagSearchResults getInstance() {
        if (instance == null) {
            instance = new WagSearchResults();
        }

        return instance;
    }

    public Collection<WagSearchResult> getResults() {
        return Collections.unmodifiableSortedSet(results);
    }

    public void addResults(Collection<WagSearchResult> resultsToAdd) {
        System.out.println("adding new WagSearchResult");
        SortedSet<WagSearchResult> old = new TreeSet<WagSearchResult>(results);
        results.addAll(resultsToAdd);
        for (WagSearchResult s : results) {
            System.out.println("s = " + s);
        }
        fireChange(old, Collections.unmodifiableSortedSet(results));
    }

    public void removeresults(Collection<WagSearchResult> resultsToRemove) {
        SortedSet<WagSearchResult> old = new TreeSet<WagSearchResult>(results);
        results.removeAll(resultsToRemove);
        fireChange(old, Collections.unmodifiableSortedSet(results));
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pps.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pps.removePropertyChangeListener(l);
    }

    protected void fireChange(Object old, Object neu) {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, PROP_NAME, old, neu);
        pps.firePropertyChange(pce);
    }
}
