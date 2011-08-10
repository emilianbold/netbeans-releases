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

package org.netbeans.spi.project.support;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * Factory for lookup capable of merging content from registered 
 * {@link org.netbeans.spi.project.LookupProvider} instances.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public final class LookupProviderSupport {
    
    private LookupProviderSupport() {
    }
    
    /**
     * Creates a project lookup instance that combines the content from multiple sources. 
     * A convenience factory method for implementors of Project.
     * 
     * @param baseLookup initial, base content of the project lookup created by the project owner
     * @param folderPath the path in the System Filesystem that is used as root for lookup composition, as for {@link Lookups#forPath}.
     *        The content of the folder is assumed to be {@link LookupProvider} instances.
     * @return a lookup to be used in project
     */ 
    public static Lookup createCompositeLookup(Lookup baseLookup, String folderPath) {
        return new DelegatingLookupImpl(baseLookup, Lookups.forPath(folderPath), folderPath);
    }
    
    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link org.netbeans.api.project.Sources} instances in the project lookup. 
     * Allows to compose the {@link org.netbeans.api.project.Sources}
     * content from multiple sources.
     * @return instance to include in project lookup
     */
    public static LookupMerger<Sources> createSourcesMerger() {
        return new SourcesMerger();
    }
    
    private static class SourcesMerger implements LookupMerger<Sources> {
        public @Override Class<Sources> getMergeableClass() {
            return Sources.class;
        }

        public @Override Sources merge(Lookup lookup) {
            return new SourcesImpl(lookup);
        }
    }
    
    private static class SourcesImpl implements Sources, ChangeListener, LookupListener {
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private Lookup.Result<Sources> delegates;
        private Collection<Sources> currentDelegates = new ArrayList<Sources>();
        
        public SourcesImpl(Lookup lookup) {
            if (currentDelegates.size() > 0) {
                for (Sources old : currentDelegates) {
                    old.removeChangeListener(this);
                }
                currentDelegates.clear();
            }
            if (delegates != null) {
                delegates.removeLookupListener(this);
            }
            Lookup.Result<Sources> srcs = lookup.lookupResult(Sources.class);
            for (Sources ns : srcs.allInstances()) {
                assert ns != this;
                ns.addChangeListener(this);
                currentDelegates.add(ns);
            }
            srcs.addLookupListener(this);
            delegates = srcs;
            changeSupport.fireChange();
        }

        public @Override SourceGroup[] getSourceGroups(String type) {
            assert delegates != null;
            Collection<SourceGroup> result = new ArrayList<SourceGroup>();
            for (Sources ns : delegates.allInstances()) {
                SourceGroup[] sourceGroups = ns.getSourceGroups(type);
                if (sourceGroups != null) {
                    for (SourceGroup sourceGroup : sourceGroups) {
                        if (sourceGroup == null) {
                            Exceptions.printStackTrace(new NullPointerException(ns + " returns null source group!"));
                        } else {
                            result.add(sourceGroup);
                        }
                    }
                }
            }
            return result.toArray(new SourceGroup[result.size()]);
        }

        public @Override void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public @Override void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        public @Override void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

        public @Override void resultChanged(LookupEvent ev) {
            if (currentDelegates.size() > 0) {
                for (Sources old : currentDelegates) {
                    old.removeChangeListener(this);
                }
                currentDelegates.clear();
            }
            for (Sources ns : delegates.allInstances()) {
                ns.addChangeListener(this);
                currentDelegates.add(ns);
            }
            changeSupport.fireChange();
        }
    }
    
}
