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


package org.netbeans.modules.jumpto.quicksearch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.spi.jumpto.quicksearch.SearchProvider;
import org.netbeans.spi.jumpto.quicksearch.SearchResult;
import org.netbeans.spi.jumpto.quicksearch.SearchResultGroup;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author  Jan Becicka
 */
public class ActionsSearchProvider implements SearchProvider {

    public String getCommandPrefix() {
        return "a";
    }

    /**
     * Returns List (ShortcutAction) of all global and editor actions.
     */
    public Set<ShortcutAction> getActions() {
        Set<ShortcutAction> actions = new HashSet<ShortcutAction>();
        for (KeymapManager m : Lookup.getDefault().lookupAll(KeymapManager.class)) {
            for (Entry<String, Set<ShortcutAction>> entry : m.getActions().entrySet()) {
                for (ShortcutAction a:entry.getValue()) {
                    actions.add(a);
                }
            }
        }
        return actions;
    }

    public SearchResultGroup evaluate(String pattern) {
        MyResult r = new MyResult(pattern);
        for (ShortcutAction a:getActions()) {
            if (a.getDisplayName().startsWith(pattern)) {
                r.addAction(a);
            }
        }
        return r;
    }
    
    private static class MyResult implements SearchResultGroup {

        private String command;
        private Collection<SearchResult> items;
        public MyResult(String command) {
            this.command = command;
            items = new ArrayList<SearchResult>();
        }

        public Iterable<? extends SearchResult> getItems() {
            return items;
        }

        public String getCategory() {
            return "Actions";
        }

        public int getSize() {
            return items.size();
        }
        
        public void addAction(ShortcutAction a) {
            items.add(new MyAction(a, this));
        }

    }
    
    private static class MyAction implements SearchResult {
        private ShortcutAction command;
        private SearchResultGroup group;
        
        public MyAction(ShortcutAction command, SearchResultGroup group) {
            this.command = command;
            this.group = group;
        }
        
        
        public void invoke() {
            Class clazz = command.getClass();
            Field f = null;
            try {
                f = clazz.getDeclaredField("action");
                f.setAccessible(true);
                Action a = (Action) f.get(command);
                a.actionPerformed(null);
            } catch (NoSuchFieldException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public String getDisplayName() {
            return command.getDisplayName();
        }

        public SearchResultGroup getResultGroup() {
            return group;
        }
    }    
}
