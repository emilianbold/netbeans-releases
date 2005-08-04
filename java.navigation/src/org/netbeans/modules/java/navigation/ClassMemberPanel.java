/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.navigation;

import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;


/**
 * Navigator panel impl for java sources, which shows source members structure. 
 *
 * @author Dafe Simonek
 */
public final class ClassMemberPanel implements NavigatorPanel, LookupListener {
    
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
        curData = (JavaDataObject)(((List)curContext.allInstances()).get(0));
        setNewContent(curData);
        curContext.addLookupListener(this);
    }
    
    /** Called when this panel's component is about to being hidden.
     * Right place to detach, remove listeners from data context.
     */
    public void panelDeactivated () {
        curContext.removeLookupListener(this);
        curContext = null;
        curModel.removeNotify();
        curModel = null;
        curData = null;
    }

    /** Impl of LookupListener, reacts to changes of context */
    public void resultChanged (LookupEvent ev) {
        Collection data = ((Lookup.Result)ev.getSource()).allInstances();
        if (!data.isEmpty()) {
            JavaDataObject jdo = (JavaDataObject)data.iterator().next();
            if (!jdo.equals(curData)) {
                curModel.removeNotify();
                curData = jdo;
                setNewContent(jdo);
            }
        }
    }
    
    /** Default activated Node strategy is enough for now */
    public Lookup getLookup () {
        return null;
    }
    
    /********** non public stuff **********/
    
    private void setNewContent (JavaDataObject jdo) {
        ClassMemberPanelUI ui = getPanelUI();
        curModel = new ClassMemberModel(jdo, ui);
        ui.getContent().setModel(curModel);
        ui.setFilters(curModel.getFilters());
        curModel.addNotify();
    }
    
    private ClassMemberPanelUI getPanelUI () {
        if (panelUI == null) {
            panelUI = new ClassMemberPanelUI();
        }
        return panelUI;
    }
    
}
