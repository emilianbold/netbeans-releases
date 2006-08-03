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

package org.netbeans.modules.java.navigation;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.navigation.base.ModelBusyListener;
import org.netbeans.modules.java.navigation.base.NavigatorJList;
import org.netbeans.modules.java.navigation.spi.NavigatorListModel;
import org.netbeans.modules.java.navigation.spi.RelatedItemProvider.RelatedItemEvent;
import org.netbeans.modules.java.navigation.spi.RelatedItemProvider.RelatedItemListener;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;


/**
 * Navigator panel impl for java sources, which shows source members structure. 
 *
 * @author Dafe Simonek
 */
public final class ClassMemberPanel implements NavigatorPanel, LookupListener, ModelBusyListener, RelatedItemListener {
    
    /** Lookup template to search for java data objects. shared with InheritanceTreePanel */
    static final Lookup.Template JDOS = new Lookup.Template(JavaDataObject.class);
    /** UI of this navigator panel */ 
    private ClassMemberPanelUI panelUI;
    /** model actually containing content of this panel */ 
    private ClassMemberModel curModel;
    /** current context to work on */
    private Lookup.Result curContext;
    /** actual data */
    private JavaDataObject curData;
    
    public String getDisplayName () {
        return NbBundle.getBundle(ClassMemberPanel.class).getString("LBL_members"); //NOI18N
    }
    
    public String getDisplayHint () {
        // XXX - TBD
        return null;
    }
    
    public JComponent getComponent () {
        return getPanelUI();
    }
    
    /** Called when this panel's component is about to being displayed.
     * Right place to attach listeners to current navigation data context.
     *
     * @param context Lookup instance representing current context
     */
    public void panelActivated (Lookup context) {
        curContext = context.lookup(JDOS);
        Collection collection = curContext.allInstances();
        JavaDataObject jdo = null;
        if (collection != null) {
            Iterator i = collection.iterator ();
            if (i.hasNext ())
                jdo = (JavaDataObject) i.next ();
        }
        curData = jdo;
        setNewContent(curData);
        curContext.addLookupListener(this);
    }
    
    /** Called when this panel's component is about to being hidden.
     * Right place to detach, remove listeners from data context.
     */
    public void panelDeactivated () {
        if( null != curContext )
            curContext.removeLookupListener(this);
        curContext = null;
        detachFromModel(curModel);
        curModel = null;
        curData = null;
    }

    /** Impl of LookupListener, reacts to changes of context */
    public void resultChanged (LookupEvent ev) {
        Collection data = ((Lookup.Result)ev.getSource()).allInstances();
        if (!data.isEmpty()) {
            JavaDataObject jdo = (JavaDataObject)data.iterator().next();
            if (!jdo.equals(curData)) {
                detachFromModel(curModel);
                curData = jdo;
                setNewContent(jdo);
            }
        }
    }
    
    /** Default activated Node strategy is enough for now */
    public Lookup getLookup () {
        return null;
    }
    
    // ModelBusyListener impl - sets wait cursor on content during computing
    
    public void busyStart () {
        if (SwingUtilities.isEventDispatchThread()) {
            getPanelUI().setBusyState(true);
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    getPanelUI().setBusyState(true);
                }
            });
        }
    }
    
    public void busyEnd () {
        if (SwingUtilities.isEventDispatchThread()) {
            getPanelUI().setBusyState(false);
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    getPanelUI().setBusyState(false);
                }
            });
        }
    }
    
    public void newContentReady () {
        getPanelUI().newContentReady();
    }
    
    // end of ModelBusyListener implementation
    
    // RelatedItemListener impl, for selecting method currently edited in editor
    
    public void itemsChanged (RelatedItemEvent evt) {
        Object sel = evt.getNewPrimary ();
        Object oldSel = evt.getOldPrimary ();
        
        NavigatorJList list = getPanelUI().getContent();
        NavigatorListModel mdl = (NavigatorListModel)list.getModel();
        Rectangle r;
        int idx = sel == null ? -1 : mdl.indexOf ( sel );
        list.setSelectedIndex ( sel == null ? -1 : idx );
        if ( idx != -1 ) {
            list.ensureIndexIsVisible(idx);
            list.repaint ( list.getCellBounds ( idx, idx ) );
        }

        idx = oldSel == null ? -1 : mdl.indexOf ( oldSel );
        if ( idx != -1 ) {
            list.repaint ( list.getCellBounds ( idx, idx ) );
        }

        for ( Iterator i = evt.getPreviousRelatedItems ().iterator (); i.hasNext (); ) {
            idx = mdl.indexOf ( i.next () );
            if ( idx != -1 ) {
                r = list.getCellBounds ( idx, idx );
                list.repaint ( r );
            }
        }
        for ( Iterator i = evt.getRelatedItems ().iterator (); i.hasNext (); ) {
            idx = mdl.indexOf ( i.next () );
            if ( idx != -1 ) {
                r = list.getCellBounds ( idx, idx );
                list.repaint ( r );
            }
        }
    }

    public void itemsCleared (RelatedItemEvent evt) {
        getPanelUI().getContent().repaint();
    }    
    
    
    /********** non public stuff **********/
    
    private void setNewContent (JavaDataObject jdo) {
        ClassMemberPanelUI ui = getPanelUI();
        curModel = new ClassMemberModel(jdo, ui, this);
        ui.getContent().setModel(curModel);
        ui.setFilters(curModel.getFilters());
        try {
            curModel.addBusyListener(this);
        } catch (TooManyListenersException exc) {
            // listeners registered twice, probably?
            ErrorManager.getDefault().notify(exc);
        }
        curModel.addNotify();
    }
    
    private void detachFromModel (ClassMemberModel model) {
        if( null != model ) {
            model.removeBusyListener(this);
            model.removeNotify();
        }
    }
    
    private ClassMemberPanelUI getPanelUI () {
        if (panelUI == null) {
            panelUI = new ClassMemberPanelUI();
        }
        return panelUI;
    }



    
}
