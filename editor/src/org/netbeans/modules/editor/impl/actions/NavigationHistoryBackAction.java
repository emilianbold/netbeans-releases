/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.impl.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class NavigationHistoryBackAction extends BaseAction implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryBackAction.class.getName());
    
    public NavigationHistoryBackAction() {
        super(BaseKit.jumpListPrevAction);
        putValue(ICON_RESOURCE_PROPERTY, "org/netbeans/modules/editor/resources/edit_previous.png"); // NOI18N

        update();
        NavigationHistory.getDefault().addPropertyChangeListener(
            WeakListeners.propertyChange(this, NavigationHistory.getDefault()));
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        NavigationHistory history = NavigationHistory.getDefault();
        if (null == history.getCurrentWaypoint()) {
            // Haven't navigated back yet
            try {
                history.markWaypoint(target, target.getCaret().getDot(), true);
            } catch (BadLocationException ble) {
                LOG.log(Level.WARNING, "Can't mark current position", ble);
            }
        }
        
        NavigationHistory.Waypoint wpt = history.navigateBack();
        if (wpt != null) {
            show(wpt);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        List<NavigationHistory.Waypoint> waypoints = NavigationHistory.getDefault().getPreviousWaypoints();
        if (!waypoints.isEmpty()) {
            NavigationHistory.Waypoint wpt = waypoints.get(waypoints.size() - 1);
            String fileName = getWaypointName(wpt);
            if (fileName != null) {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                    "NavigationHistoryBackAction_Tooltip", fileName));
            } else {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                    "NavigationHistoryBackAction_Tooltip_simple"));
            }
            setEnabled(true);
        } else {
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                "NavigationHistoryBackAction_Tooltip_simple"));
            setEnabled(false);
        }
    }

    /* package */ static void show(NavigationHistory.Waypoint wpt) {
        final URL url = wpt.getUrl();
        final int offset = wpt.getOffset();
        if (url == null || offset < 0) {
            return;
        }
        
        FileObject f = URLMapper.findFileObject(url);
        if (f != null) {
            DataObject d = null;
            
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, "Can't get DataObject for " + f, e); //NOI18N
            }
            
            if (d != null) {
                final EditorCookie editorCookie = d.getLookup().lookup(EditorCookie.class);
                final LineCookie lineCookie = d.getLookup().lookup(LineCookie.class);
                Document doc = null;
                
                if (editorCookie != null && lineCookie != null) {
                    try {
                        doc = editorCookie.openDocument();
                    } catch (IOException ioe) {
                        LOG.log(Level.WARNING, "Can't open document", ioe);
                    }
                }

                if (doc instanceof BaseDocument) {
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
                                }
                            }
                        }
                    });
                }                
            }
        }
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
}
