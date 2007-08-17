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

package org.netbeans.modules.mobility.project.ui.customizer.regex;
import org.openide.explorer.view.BeanTreeView;

import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * User: suchys
 * Date: Dec 12, 2003
 * Time: 3:57:41 PM
 */
public class CheckedTreeBeanView extends BeanTreeView {
    
    private FileObject root;
    private Pattern filter;
    private Map<String,Object> data;
    final private CheckedNodeRenderer renderer;
    final private CheckedNodeEditor editor;
    private Map<String,Object> properties;
    private String propertyName;
    
    static final Object SELECTED = new Object();
    static final Object UNSELECTED = new Object();
    static final Object MIXED = new Object();
    
    public CheckedTreeBeanView() {
        super();
        FocusListener[] fl = tree.getFocusListeners();
        for (int i = 0; i < fl.length; i++) {
            if (fl[i].getClass().getName().startsWith("org.openide")){  //NOI18N
                tree.removeFocusListener(fl[i]);
            }
        }
        
        MouseListener[] ml = tree.getMouseListeners();
        for (int i = 0; i < ml.length; i++) {
            if (ml[i].getClass().getName().startsWith("org.openide")){  //NOI18N
                tree.removeMouseListener(ml[i]);
            }
        }
        
        tree.setCellRenderer(renderer = new CheckedNodeRenderer(tree.getCellRenderer()));
        tree.setCellEditor(editor = new CheckedNodeEditor(tree));
        tree.setEditable(true);
    }
    
    public void setEditable(final boolean editable) {
        tree.setEditable(editable);
        tree.setBackground(UIManager.getDefaults().getColor(editable ?  "Tree.background" : "TextField.inactiveBackground")); //NOI18N
    }
    
    public void setSrcRoot(final FileObject root) {
        this.root = root;
    }
    
    private boolean acceptPath(final String path) {
        return path != null && (path.length()==0 || !this.filter.matcher(path).matches());
    }
    
    private synchronized Object updateState(final FileObject fo) {
        final String path = FileUtil.getRelativePath(root, fo);
        if (!acceptPath(path)) return null; // null means invalid
        Object state = data.get(path);
        final boolean forceState = state == SELECTED || state == UNSELECTED;
        final Enumeration en = fo.getChildren(forceState);
        while (en.hasMoreElements()) {
            final FileObject ch = (FileObject)en.nextElement();
            if (forceState) {
                final String cp = FileUtil.getRelativePath(root, ch);
                if (acceptPath(cp)) data.put(cp, state);
            } else {
                final Object childState = updateState(ch);
                if (childState != null) {
                    if (state == null) state = childState;
                    else if (state != childState) state = MIXED;
                }
            }
        }
        if (state == null) state = SELECTED; // if no valid children then SELECTED
        if (path.length() > 0) data.put(path, state);
        return state;
    }
    
    public Object getState(final FileObject fo) { // finds first SELECTED or UNSELECTED from root
        final String path = FileUtil.getRelativePath(root, fo);
        if (!acceptPath(path)) return null; //invalid
        return data.get(path);
    }
    
    public synchronized void setState(FileObject fo, final boolean selected) {
        final String path = FileUtil.getRelativePath(root, fo);
        if (path == null) return; // invalid file object
        data.put(path, selected ? SELECTED : UNSELECTED); // set the one
        fo = fo.getParent();
        while (fo != null && !root.equals(fo)) { // clean the path to parent
            data.remove(FileUtil.getRelativePath(root, fo));
            fo = fo.getParent();
        }
        updateState(root); // renew the path from parent
        if (properties != null && propertyName != null) properties.put(propertyName, getExcludesRegex());
    }
    
    public String getExcludesRegex() {
        final StringBuffer sb = new StringBuffer();
        addExcludes(root, sb);
        return sb.toString();
    }
    
    private void addExcludes(final FileObject fo, final StringBuffer sb) {
        final String path = FileUtil.getRelativePath(root, fo);
        if (!acceptPath(path)) return;
        final Object state = data.get(path);
        if ((path.length() > 0 && state == null) || state == SELECTED) return;
        if (state == UNSELECTED) {
            if (sb.length() > 0) sb.append(',');
            sb.append(path);
            if (fo.isFolder()) sb.append(',').append(path).append("/**");//NOI18N
        } else {
            final Enumeration en = fo.getChildren(false);
            while (en.hasMoreElements()) addExcludes((FileObject)en.nextElement(), sb);
        }
    }
    
    public synchronized void registerProperty(final Map<String,Object> properties, final String propertyName, final Pattern filter) {
        this.properties = properties;
        this.propertyName = propertyName;
        if (properties == null || propertyName == null) return;
        this.filter = filter;
        this.data = new HashMap<String,Object>();
        final String initialExcludes = (String)properties.get(propertyName);
        if (initialExcludes != null) {
            final StringTokenizer stk = new StringTokenizer(initialExcludes, ","); //NOI18N
            while (stk.hasMoreTokens()) {
                final String exclude = stk.nextToken();
                if (exclude.indexOf('*') < 0) data.put(exclude, UNSELECTED);
            }
        }
        updateState(root);
        renderer.setContentStorage(this);
        editor.setContentStorage(this);
    }
}
