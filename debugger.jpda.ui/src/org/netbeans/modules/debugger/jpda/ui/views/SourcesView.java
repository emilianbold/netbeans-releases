/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.views;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.netbeans.modules.debugger.jpda.ui.Utils;
import org.netbeans.spi.viewmodel.Models;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

public class SourcesView extends TopComponent {
    
    private transient JComponent tree;
    private transient ViewModelListener viewModelListener;
    
    
    public SourcesView () {
        setIcon (Utils.getIcon (
            "org/netbeans/modules/debugger/jpda/resources/root"
        ).getImage ());
    }

    protected String preferredID() {
        return this.getClass().getName();
    }

    protected void componentShowing () {
        super.componentShowing ();
        if (viewModelListener != null)
            return;
        if (tree == null) {
            setLayout (new BorderLayout ());
            tree = Models.createView 
                (null, null, null, null, new ArrayList ());
            tree.setName ("SourcesView");
            add (tree, "Center");  //NOI18N
        }
        if (viewModelListener != null)
            throw new InternalError ();
        viewModelListener = new ViewModelListener (
            "SourcesView",
            tree
        );
    }
    
    protected void componentHidden () {
        super.componentHidden ();
        viewModelListener.destroy ();
        viewModelListener = null;
    }
    
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
    
    public String getName () {
        return NbBundle.getMessage (SourcesView.class, "CTL_Sourcess_view");
    }
    
//    private void update () {
//        list.setListData (getData ());
//    }
//    
//    private Object[] getData () {
//        DebuggerEngine engine = DebuggerManager.getDebuggerManager ().
//            getCurrentEngine ();
//        if (engine == null) return new Object[0];
//        ClassPath classPath = (ClassPath) engine.lookupFirst (ClassPath.class);
//        if (classPath == null) return new Object[0];
//        List entries = classPath.entries ();
//        int i, k = entries.size ();
//        String[] list = new String [k];
//        for (i = 0; i < k; i++) {
//            ClassPath.Entry e = (ClassPath.Entry) entries.get (i);
//            list [i] = e.getRoot ().getPath ();
//            Project p = FileOwnerQuery.getOwner (e.getRoot ());
//            if (p != null) {
//                ProjectInformation pi = (ProjectInformation) p.getLookup ().
//                    lookup (ProjectInformation.class);
//                list [i] = "Project " + pi.getDisplayName () + " sources (" + list [i] + ")";
//            } else 
//                list [i] = "JDK Sources (" + list [i] + ")";
//        }
//        return list;
//    }
//    
//    public void propertyChange (java.beans.PropertyChangeEvent evt) {
//        if (evt.getPropertyName () != DebuggerManager.PROP_CURRENT_ENGINE)
//            return;
//        update ();
//    }
//    
//    public void breakpointAdded (org.netbeans.api.debugger.Breakpoint breakpoint) {}
//    public void breakpointRemoved (org.netbeans.api.debugger.Breakpoint breakpoint) {}
//    public org.netbeans.api.debugger.Breakpoint[] initBreakpoints () 
//        {return new org.netbeans.api.debugger.Breakpoint[0];}
//    public void initWatches () {}
//    public void sessionAdded (org.netbeans.api.debugger.Session session) {}
//    public void sessionRemoved (org.netbeans.api.debugger.Session session) {}
//    public void watchAdded (org.netbeans.api.debugger.Watch watch) {}
//    public void watchRemoved (org.netbeans.api.debugger.Watch watch) {}
//    
//    class SourceCellRenderer extends JCheckBox implements ListCellRenderer {
//
//        public Component getListCellRendererComponent (
//            JList list,
//            Object value,
//            int index,
//            boolean isSelected,
//            boolean cellHasFocus
//        ) {
//            String s = value.toString ();
//            setText (s);
//            setSelected (true);
//            setIcon (SourcesView.getIcon ("org/netbeans/modules/debugger/jpda/resources/root"));
//            if (isSelected) {
//                setBackground (list.getSelectionBackground ());
//                setForeground (list.getSelectionForeground ());
//            } else {
//                setBackground (list.getBackground ());
//                setForeground (list.getForeground ());
//            }
//            setEnabled (list.isEnabled ());
//            setFont (list.getFont ());
//            setOpaque (true);
//            return this;
//        }
//    }
}
