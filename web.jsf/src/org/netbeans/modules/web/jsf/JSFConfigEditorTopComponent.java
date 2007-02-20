/*
 * JSFConfigEditorTopComponent.java
 *
 * Created on February 7, 2007, 5:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author petr
 */
public class JSFConfigEditorTopComponent extends TopComponent{
    
    private JComponent view;

    public JSFConfigEditorTopComponent (JSFConfigEditorContext context, Lookup lookup, JComponent view) {
        this.view = view;
        setLayout (new BorderLayout ());
        setFocusable (true);
        add (view, BorderLayout.CENTER);
        // TODO the try shouldn't be here
        try{
            Node node = DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
            setActivatedNodes (new Node[] { node });
        }
        catch (Exception e){}
//        lookup = new ProxyLookup (lookup, node.getLookup (), Lookups.singleton (getActionMap ()));
//        associateLookup (lookup);
    }

    public void requestFocus () {
        super.requestFocus ();
        view.requestFocus ();
    }

    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        return view.requestFocusInWindow ();
    }
    
}
