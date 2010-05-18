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

package org.netbeans.modules.db.explorer.action;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * A registry for managing a collection of Actions defined in the layer file.
 * 
 * @author Rob Englander
 */
public class ActionRegistry implements ChangeListener {
    private static final Logger LOGGER = Logger.getLogger(ActionRegistry.class.getName());
    
    private static final String PATH = "Databases/Explorer/"; //NOI18N
    private static final String ACTIONS = "/Actions"; //NOI18N

    private final ChangeSupport changeSupport;
    private final List<Action> actions = new CopyOnWriteArrayList<Action>();
    
    private Lookup.Result lookupResult;

    /**
     * Constructor.
     * 
     * @param entryName the name of the entry folder in the layer to lookup actions.
     */
    public ActionRegistry(String entryName) {
        changeSupport = new ChangeSupport(this);
        loadActions(entryName);
    }
    
    /**
     * Loads actions from the xml layer.
     * 
     * @param entryName the name of the entry in the xml layer where the
     * actions are found.
     */
    private void loadActions(String entryName) {
        Lookup lookup = Lookups.forPath(PATH + entryName + ACTIONS);
        lookupResult = lookup.lookupResult(Object.class);
        
        initActions();

        // listen for changes and re-init the actions when the lookup changes
        lookupResult.addLookupListener(
            new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    initActions();
                    changeSupport.fireChange();
                }
            }
        );
    }

    private void initActions() {
        actions.clear();
        Collection<Object> actionList = (Collection<Object>)lookupResult.allInstances();
        
        for (Object action : actionList) {
            if (action instanceof Action) {
                actions.add((Action)action);
            } else if (action instanceof javax.swing.JSeparator) {
                actions.add(null);
            } else {
                LOGGER.log(Level.INFO, "Cannot use " + action.getClass() + " instance as DB Explorer Action"); // NOI18N
            }
        }
    }
    
    /**
     * Get the actions.  The order is based on the order that
     * was defined in the layer file.
     * 
     * @return the actions
     */
    public Collection<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void stateChanged(ChangeEvent evt) {
        changeSupport.fireChange();
    }
}
