/*
 * NodePopupMenuProvider.java
 *
 * Created on February 2, 2007, 6:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author joelle
 */
public class PageFlowPopupProvider implements PopupMenuProvider {
    
    PageFlowScene graphScene;
    
    private JMenuItem miAddWebPage;
    private JPopupMenu graphPopup;
    
    private String addPage = NbBundle.getMessage(GraphPopupProvider.class, "MSG_AddPage");
    
    /**
     * Creates a Popup for any right click on Page Flow Editor
     * @param graphScene The related PageFlow Scene.
     */
    public PageFlowPopupProvider(PageFlowScene graphScene) {
        
        this.graphScene = graphScene;
        initialize();
    }
    
    
    // <actions from layers>
    private static final String PATH_PAGEFLOW_ACTIONS = "PageFlowEditor/PopupActions"; // NOI18N
//        private static final String PATH_PAGEFLOW_ACTIONS = "PageFlowEditor/application/x-pageflow/Popup"; // NOI18N
    private void initialize() {
        graphPopup = Utilities.actionsToPopup(
                SystemFileSystemSupport.getActions(PATH_PAGEFLOW_ACTIONS),
                getLookup());
        //        graphPopup = new JPopupMenu("Transition Menu");
        //
        //        graphPopup = new JPopupMenu();
        //        miAddWebPage = new JMenuItem(addPage);
        //        miAddWebPage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK));
        //        miAddWebPage.addActionListener(this);
        //        graphPopup.add(miAddWebPage);
    }
    
    
    
    public JPopupMenu getPopupMenu(Widget widget, Point point){
        return graphPopup;
    }
    
    
    /** Weak reference to the lookup. */
    private WeakReference lookupWRef = new WeakReference(null);
    
    /** Adds <code>NavigatorLookupHint</code> into the original lookup,
     * for the navigator. */
    private Lookup getLookup() {
        Lookup lookup = (Lookup)lookupWRef.get();
        
        if (lookup == null) {
            InstanceContent ic = new InstanceContent();
            //                ic.add(firstObject);
            ic.add(graphScene);
            lookup = new AbstractLookup(ic);
            lookupWRef = new WeakReference(lookup);
        }
        
        return lookup;
    }
    
}
