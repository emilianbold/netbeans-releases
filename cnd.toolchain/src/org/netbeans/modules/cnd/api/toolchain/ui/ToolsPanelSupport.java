/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.toolchain.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.toolchain.ui.options.HostToolsPanelModel;
import org.netbeans.modules.cnd.toolchain.ui.options.ToolsCacheManagerImpl;
import org.netbeans.modules.cnd.toolchain.ui.options.ToolsPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.WeakSet;

/**
 *
 * @author Alexander Simon
 */
public class ToolsPanelSupport {
    private static CompilerSet currentCompilerSet;
    private static final ToolsCacheManagerImpl cacheManager = (ToolsCacheManagerImpl) ToolsCacheManager.createInstance();
    // component.getClientProperty(OK_LISTENER_KEY) can have vetoable listener (VetoableChangeListener)
    public final static String OK_LISTENER_KEY = "okVetoableListener"; // NOI18N
    // component.getClientProperty(OK_LISTENER_KEY) can have selected toolchain name (String)
    public final static String SELECTED_TOOLCHAIN_KEY = "selectedToolchain"; // NOI18N
    public static ToolsCacheManager getToolsCacheManager() {
        return cacheManager;
    }

    public static boolean isUnsupportedMake(String name) {
        name = ToolUtils.getBaseName(name);
        return name.toLowerCase().equals("mingw32-make.exe"); // NOI18N
    }

    private static Set<ChangeListener> listenerChanged = new WeakSet<ChangeListener>();

    public static void addCompilerSetChangeListener(ChangeListener l) {
        listenerChanged.add(l);
    }

    public static void removeCompilerSetChangeListener(ChangeListener l) {
        listenerChanged.remove(l);
    }

    public static void fireCompilerSetChange(CompilerSet  set) {
        ChangeEvent ev = new ChangeEvent(set);
        currentCompilerSet = set;
        for (ChangeListener l : listenerChanged) {
            l.stateChanged(ev);
        }
    }

    private final static Set<ChangeListener> listenerModified = new WeakSet<ChangeListener>();

    public static void addCompilerSetModifiedListener(ChangeListener l) {
        synchronized (listenerModified) {
            listenerModified.add(l);
        }
    }

    public static void removeCompilerSetModifiedListener(ChangeListener l) {
        synchronized (listenerModified) {
            listenerModified.remove(l);
        }
    }

    public static void fireCompilerSetModified(CompilerSet set) {
        ChangeEvent ev = new ChangeEvent(set);
        synchronized (listenerModified) {
            for (ChangeListener l : listenerModified) {
                l.stateChanged(ev);
            }
        }
    }

    public static CompilerSet getCurrentCompilerSet() {
        return currentCompilerSet;
    }

    private static final Set<IsChangedListener> listenerIsChanged = new WeakSet<IsChangedListener>();

    public static void addIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.add(l);
        }
    }

    public static void removeIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.remove(l);
        }
    }

    public static boolean isChangedInOtherPanels() {
        boolean isChanged = false;
        synchronized (listenerIsChanged) {
            for (IsChangedListener l : listenerIsChanged) {
                if (l.isChanged()) {
                    isChanged = true;
                    break;
                }
            }
        }
        return isChanged;
    }

    /**
     * returns toolchain manager component to be embedded in other containers
     * @param env execution environment for which manager is created
     * @return toolchain manager component for specified execution environmen
     *  reference to listener to be used by containers to notify about OK is in component
     *  property OK_LISTENER_KEY (VetoableChangeListener)
     *  client can find selected toolchain after OK in property
     *  SELECTED_TOOLCHAIN_KEY (String name of toolchain)
     */
    public static JComponent getToolsPanelComonent(ExecutionEnvironment env) {
        HostToolsPanelModel model = new HostToolsPanelModel(env);
        final ToolsPanel tp = new ToolsPanel(model);
        tp.update();
        VetoableChangeListener okL = new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                tp.applyChanges();
                tp.putClientProperty(SELECTED_TOOLCHAIN_KEY, tp.getSelectedToolchain());
            }
        };
        tp.putClientProperty(OK_LISTENER_KEY, okL); // NOI18N
        return tp;
    }
    
    private ToolsPanelSupport() {
    }
}
