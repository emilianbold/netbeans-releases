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

package org.netbeans.modules.editor.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.awt.DropDownButtonFactory;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Vita Stejskal
 */
public final class NavigationHistoryBackAction extends TextAction implements ContextAwareAction, Presenter.Toolbar, PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryBackAction.class.getName());
    
    private final JTextComponent component;
    private final NavigationHistory.Waypoint waypoint;
    private final JPopupMenu popupMenu;
    
    public NavigationHistoryBackAction() {
        this(null, null, null);
    }

    private NavigationHistoryBackAction(JTextComponent component, NavigationHistory.Waypoint waypoint, String actionName) {
        super(BaseKit.jumpListPrevAction);
        
        this.component = component;
        this.waypoint = waypoint;
        
        if (waypoint != null) {
            putValue(NAME, actionName);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                "NavigationHistoryBackAction_Tooltip", actionName)); //NOI18N
            this.popupMenu = null;
        } else if (component != null) {
            putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/editor/resources/navigate_back_16.png"))); //NOI18N
            this.popupMenu = new JPopupMenu();
            update();
            NavigationHistory nav = NavigationHistory.getNavigations();
            nav.addPropertyChangeListener(WeakListeners.propertyChange(this, nav));
        } else {
            this.popupMenu = null;
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                "NavigationHistoryBackAction_Tooltip_simple")); //NOI18N
        }
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent c = findComponent(actionContext);
        return new NavigationHistoryBackAction(c, null, null);
    }

    public void actionPerformed(ActionEvent evt) {
        JTextComponent target = component != null ? component : getTextComponent(evt);
        NavigationHistory history = NavigationHistory.getNavigations();
        if (null == history.getCurrentWaypoint()) {
            // Haven't navigated back yet
            try {
                history.markWaypoint(target, target.getCaret().getDot(), true, false);
            } catch (BadLocationException ble) {
                LOG.log(Level.WARNING, "Can't mark current position", ble); //NOI18N
            }
        }
        
        NavigationHistory.Waypoint wpt = waypoint != null ? 
            history.navigateTo(waypoint) : history.navigateBack();
        
        if (wpt != null) {
            show(wpt);
        }
    }

    public Component getToolbarPresenter() {
        if (popupMenu != null) {
            JButton button = DropDownButtonFactory.createDropDownButton(
                (ImageIcon) getValue(SMALL_ICON), 
                popupMenu
            );
            button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            button.setAction(this);
            return button;
        } else {
            return new JButton(this);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        List<NavigationHistory.Waypoint> waypoints = NavigationHistory.getNavigations().getPreviousWaypoints();

        // Update popup menu
        if (popupMenu != null) {
            popupMenu.removeAll();

            int count = 0;
            String lastFileName = null;
            NavigationHistory.Waypoint lastWpt = null;
            
            for(int i = waypoints.size() - 1; i >= 0; i--) {
                NavigationHistory.Waypoint wpt = waypoints.get(i);
                String fileName = getWaypointName(wpt);
                
                if (fileName == null) {
                    continue;
                }
                
                if (lastFileName == null || !fileName.equals(lastFileName)) {
                    if (lastFileName != null) {
                        popupMenu.add(new NavigationHistoryBackAction(component, lastWpt, 
                            count > 1 ? lastFileName + ":" + count : lastFileName)); //NOI18N
                    }
                    lastFileName = fileName;
                    lastWpt = wpt;
                    count = 1;
                } else {
                    count++;
                }
            }
            
            if (lastFileName != null) {
                popupMenu.add(new NavigationHistoryBackAction(component, lastWpt,
                    count > 1 ? lastFileName + ":" + count : lastFileName)); //NOI18N
            }
        }
        
        // Set the short description
        if (!waypoints.isEmpty()) {
            NavigationHistory.Waypoint wpt = waypoints.get(waypoints.size() - 1);
            String fileName = getWaypointName(wpt);
            if (fileName != null) {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                    "NavigationHistoryBackAction_Tooltip", fileName)); //NOI18N
            } else {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                    "NavigationHistoryBackAction_Tooltip_simple")); //NOI18N
            }
            
            setEnabled(true);
        } else {
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                "NavigationHistoryBackAction_Tooltip_simple")); //NOI18N
            setEnabled(false);
        }
    }

    /* package */ static void show(NavigationHistory.Waypoint wpt) {
        final int offset = wpt.getOffset();
        if (offset < 0) {
            return;
        }
        
        Lookup lookup = findLookupFor(wpt);
        if (lookup != null) {
            final EditorCookie editorCookie = lookup.lookup(EditorCookie.class);
            final LineCookie lineCookie = lookup.lookup(LineCookie.class);
            Document doc = null;

            if (editorCookie != null && lineCookie != null) {
                try {
                    doc = editorCookie.openDocument();
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "Can't open document", ioe); //NOI18N
                }
            }

            if (doc instanceof BaseDocument) {
                final boolean worked[] = new boolean [1];
                final BaseDocument baseDoc = (BaseDocument) doc;
                doc.render(new Runnable() {
                    public void run() {
                        Element lineRoot = baseDoc.getParagraphElement(0).getParentElement();
                        int lineIndex = lineRoot.getElementIndex(offset);

                        if (lineIndex != -1) {
                            Element lineElement = lineRoot.getElement(lineIndex);
                            int column = offset - lineElement.getStartOffset();

                            Line line = lineCookie.getLineSet().getCurrent(lineIndex);
                            if (line != null) {
                                line.show(Line.SHOW_REUSE, column);
                                worked[0] = true;
                            }
                        }
                    }
                });
                if (worked[0]) {
                    return;
                }
            }
        }
        
        // lookup didn't work try simple navigation in the text component
        JTextComponent component = wpt.getComponent();
        if (component != null) {
            component.setCaretPosition(offset);
            component.requestFocusInWindow();
        }
    }
    
    private static Lookup findLookupFor(NavigationHistory.Waypoint wpt) {
        // try component first
        JTextComponent component = wpt.getComponent();
        if (component != null) {
            for (java.awt.Component c = component; c != null; c = c.getParent()) {
                if (c instanceof Lookup.Provider) {
                    Lookup lookup = ((Lookup.Provider)c).getLookup ();
                    if (lookup != null) {
                        return lookup;
                    }
                }
            }
        }

        // now try DataObject
        URL url = wpt.getUrl();
        FileObject f = url == null ? null : URLMapper.findFileObject(url);
        
        if (f != null) {
            try {
                return DataObject.find(f).getLookup();
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, "Can't get DataObject for " + f, e); //NOI18N
            }
        }
        
        return null;
    }
    
    /* package */ static String getWaypointName(NavigationHistory.Waypoint wpt) {
        URL url = wpt.getUrl();
        if (url != null) {
            String path = url.getPath();
            int idx = path.lastIndexOf('/'); //NOI18N
            if (idx != -1) {
                return path.substring(idx + 1);
            } else {
                return path;
            }
        } else {
            return null;
        }
    }
    
    /* package */ static JTextComponent findComponent(Lookup lookup) {
        EditorCookie ec = (EditorCookie) lookup.lookup(EditorCookie.class);
        if (ec != null) {
            JEditorPane panes[] = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
        }
        return lookup.lookup(JTextComponent.class);
    }
}
