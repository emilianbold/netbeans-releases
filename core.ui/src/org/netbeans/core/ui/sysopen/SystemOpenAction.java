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

package org.netbeans.core.ui.sysopen;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Open the selected file(s) with the system default tool.
 * Available only on JDK 6+.
 * @author Jesse Glick
 */
public final class SystemOpenAction extends AbstractAction implements ContextAwareAction {
    
    public SystemOpenAction() {
        super(NbBundle.getMessage(SystemOpenAction.class, "CTL_SystemOpenAction"));
    }

    public void actionPerformed(ActionEvent e) {
        new ContextAction(Utilities.actionsGlobalContext()).actionPerformed(e);
    }

    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        
        private static interface Performer {
            void open(File f) throws IOException;
        }
        private static final Performer performer;
        static {
            Performer _performer = null;
            try {
                Class<?> desktop = Class.forName("java.awt.Desktop");
                if ((Boolean) desktop.getMethod("isDesktopSupported").invoke(null)) {
                    final Object desktopInstance = desktop.getMethod("getDesktop").invoke(null);
                    Class<?> action = Class.forName("java.awt.Desktop$Action");
                    if ((Boolean) desktop.getMethod("isSupported", action).
                            invoke(desktopInstance, action.getField("OPEN").get(null))) {
                        final Method open = desktop.getMethod("open", File.class);
                        _performer = new Performer() {
                            public void open(File f) throws IOException {
                                // XXX could try edit too?
                                try {
                                    open.invoke(desktopInstance, f);
                                } catch (InvocationTargetException x) {
                                    throw (IOException) x.getTargetException();
                                } catch (Exception x) {
                                    throw (IOException) new IOException(x.toString()).initCause(x);
                                }
                            }
                        };
                    }
                }
            } catch (ClassNotFoundException x) {
                // OK, ignore
            } catch (Exception x) {
                Logger.getLogger(SystemOpenAction.class.getName()).log(Level.WARNING, null, x);
            }
            performer = _performer;
        }

        private final Set<File> files;
        
        public ContextAction(Lookup context) {
            super(NbBundle.getMessage(SystemOpenAction.class, "CTL_SystemOpenAction"));
            files = new HashSet<File>();
            for (DataObject d : context.lookupAll(DataObject.class)) {
                File f = FileUtil.toFile(d.getPrimaryFile());
                if (f == null) {
                    files.clear();
                    break;
                }
                files.add(f);
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (performer == null) {
                return;
            }
            for (File f : files) {
                try {
                    performer.open(f);
                } catch (IOException x) {
                    Logger.getLogger(SystemOpenAction.class.getName()).log(Level.INFO, null, x);
                    // XXX or perhaps notify user of problem
                }
            }
        }

        public JMenuItem getPopupPresenter() {
            class Menu extends JMenuItem implements DynamicMenuContent {
                public Menu() {
                    super(ContextAction.this);
                }
                public JComponent[] getMenuPresenters() {
                    if (performer != null && !files.isEmpty()) {
                        if (Utilities.isWindows()) { // #144575
                            for (File f : files) {
                                if (!f.getName().contains(".")) {
                                    return new JComponent[0];
                                }
                            }
                        }
                        return new JComponent[] {this};
                    } else {
                        return new JComponent[0];
                    }
                }
                public JComponent[] synchMenuPresenters(JComponent[] items) {
                    return getMenuPresenters();
                }
            }
            return new Menu();
        }
        
    }
    
}

