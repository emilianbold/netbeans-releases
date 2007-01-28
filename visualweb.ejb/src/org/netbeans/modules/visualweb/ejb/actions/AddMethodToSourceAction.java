/*
 * AddMethodToSourceAction.java
 *
 * Created on August 23, 2004, 5:52 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import java.awt.Container;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author  cao
 */
public class AddMethodToSourceAction  extends NodeAction {
    
    /** Creates a new instance of AddMethodToSourceAction */
    public AddMethodToSourceAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        // Enable this action only if the Java and JSP (??) source editors are open
        
        TopComponent jspView = findJspView();
        if( jspView != null && findJavaJspPane( jspView ) != null )
            return true;
        else
            return false;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( AddMethodToSourceAction.class, "ADD_METHOD_TO_SOURCE" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        
        System.out.println( ".......AddMethodToSourceAction.performAction()" );
        TopComponent jspView = findJspView();
        if( jspView == null )
        {
            System.out.println( "......no JSP view" );
            return;
        }
        
        JEditorPane javaJspEditor = (JEditorPane)findJavaJspPane( jspView );
        if( javaJspEditor == null )
        {
            System.out.println( "......no javaJspEditor" );
            return;
        }
        
        // get the position from the editor
        int pos = javaJspEditor.getCaretPosition();
        
        // get the document and insert the code clip string
        Document document = javaJspEditor.getDocument();
        try {
            document.insertString( pos, "pretend there is method inserted", null );
        } catch (BadLocationException ble) {
        }
        
        ((JEditorPane)javaJspEditor).setDocument( document );
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
    /** Find Java/Jsp editor
     */
    private TopComponent findJspView() {
        // Search through modes, then topcomponents
        Set modes = WindowManager.getDefault().getModes();
        Iterator it2 = modes.iterator();
        while (it2.hasNext()) {
            Mode m = (Mode)it2.next();
            TopComponent[] tcs = m.getTopComponents();
            if (tcs != null) {
                for (int j = 0; j < tcs.length; j++) {
                    // FIXME I think this won't work properly now, there is a multiview component employed.
                    // Usage of this entire method looks very suspicious.
                    if (tcs[j] instanceof org.openide.text.CloneableEditor) {
                        // XXX - return the first editor, I actually want the one
                        //       which was last selected. I also need to make sure
                        //       the component is showing
                        return tcs[j];
                    }
                }
            }
        }
        
        return null;
    }
    
    /** Fish for a Java/Jsp within a container hierarchy
     */
    private JComponent findJavaJspPane(Container c) {
        if (c == null)
            return null;

        int n = c.getComponentCount();
        for (int i = 0; i < n; i++) {
            java.awt.Component child = c.getComponent(i);
            
            if (child instanceof javax.swing.JEditorPane) { 
                // XXX - I wa hoping for a JEditorPane so I can check the content type (e.g. text/x-java)
                return (JComponent)child;
            } else if (child instanceof Container) {
                JComponent result = findJavaJspPane((Container)child);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
    
}
