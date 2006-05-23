/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                Class desktop = Class.forName("java.awt.Desktop");
                if ((Boolean) desktop.getMethod("isDesktopSupported").invoke(null)) {
                    final Object desktopInstance = desktop.getMethod("getDesktop").invoke(null);
                    Class action = Class.forName("java.awt.Desktop$Action");
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

