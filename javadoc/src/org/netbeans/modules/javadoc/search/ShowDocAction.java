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

/*
 * ShowDocAction.java
 *
 * Created on 3. leden 2001, 11:23
 */

package org.netbeans.modules.javadoc.search;

import java.awt.Rectangle;
import java.awt.BorderLayout;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.src.ClassElement;
import org.openide.cookies.SourceCookie;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;

import org.netbeans.modules.java.JavaDataObject;

/**
 *  On selected node try to find generated and mounted documentation
 * @author  Petr Suchomel
 * @version 1.0
 */
public class ShowDocAction extends CookieAction {

    static final long serialVersionUID =3578357584245478L;
    
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle( ShowDocAction.class ).getString ("CTL_SHOWDOC_MenuItem");   //NOI18N
    }

    /** Cookie classes contains one class returned by cookie () method.
    */
    protected final Class[] cookieClasses () {
        return new Class[] { JavaDataObject.class };
    }

    /** All must be DataFolders or JavaDataObjects
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ShowDocAction.class);
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( Node[] nodes ) {
        IndexSearch indexSearch = IndexSearch.getDefault();
                
        if( nodes.length == 1 && nodes[0] != null ) {
            String toFind = nodes[0].getName();
            if (toFind != null)
                indexSearch.setTextToFind( toFind );
        }
        indexSearch.open ();
        indexSearch.requestFocus();        
    }

    protected String iconResource(){
        return "/org/netbeans/modules/javadoc/resources/showjavadoc.gif"; //NOI18N
    }
}
