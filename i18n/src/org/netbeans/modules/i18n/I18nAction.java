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


package org.netbeans.modules.i18n;


import javax.swing.text.StyledDocument;

import org.openide.cookies.SourceCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;


/**
* Internationalize action.
*
* @author   Petr Jiricka
*/
public class I18nAction extends CookieAction {

    /** Generated sreial version UID. */
    static final long serialVersionUID =3322896507302889271L;
    
    /** 
     * Actually performs the action.
     * @param activatedNodes Currently activated nodes.
     */
    public void performAction(final Node[] activatedNodes) {
        // Get cookie.
        final SourceCookie.Editor sec = (SourceCookie.Editor) activatedNodes[0].getCookie(SourceCookie.Editor.class);
        if (sec == null) 
            return;

        sec.open(); 
        
        // Run in separate thread.
        RequestProcessor.postRequest(new Runnable() {
            public void run() {
                DataObject obj = (DataObject)sec.getSource().getCookie(DataObject.class);
                StyledDocument doc = sec.getDocument();

                I18nManager.getDefault().internationalize(doc, obj);
            }
        });
    }

    /**
     * @return MODE_EXACTLY_ONE.
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /**
     * @return cookies which have to have the node on which the action would enable
     */
    protected Class[] cookieClasses () {
        return new Class[] {
            SourceCookie.Editor.class // show action for java source node only
        };
    }

    /** 
     * @return the action's icon 
     */
    public String getName() {
        return I18nUtil.getBundle().getString("CTL_I18nAction");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (I18nModule.class);
    }

    /** The action's icon location.
     * @return the action's icon location
     */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
    }
}
