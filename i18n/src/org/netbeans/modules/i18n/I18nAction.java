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


import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;


/**
 * Internationalize action. Runs "i18n session" over specified source. Finds
 * non-i19n-ized hard coded strings and offers them i18n-ize to user in step-by-step
 * manner.
 *
 * @author   Petr Jiricka
 * @see I18nManager
 */
public class I18nAction extends CookieAction {

    /** Generated sreial version UID. */
    static final long serialVersionUID =3322896507302889271L;

    
    /** 
     * Actually performs the action.
     * @param activatedNodes Currently activated nodes.
     */
    public void performAction(final Node[] activatedNodes) {
        // Gets editor cookie.
        EditorCookie editorCookie = (EditorCookie)activatedNodes[0].getCookie(EditorCookie.class);
        
        if(editorCookie == null) 
            return;

        editorCookie.open(); 
        
        final DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
        
        // Run in separate thread.
        RequestProcessor.postRequest(new Runnable() {
            public void run() {
                I18nManager.getDefault().internationalize(dataObject);
            }
        });
    }

    /** Implements superclass abstract method.
     * @return MODE_EXACTLY_ONE.
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Implemenst superclass abstract method.
     * @return <code>EditorCookie<code>.class 
     * #see org.openide.cookies.EditorCookie */
    protected Class[] cookieClasses () {
        return new Class[] {
            EditorCookie.class // Has documents.
        };
    }

    /** Overrides superclass method. Adds additional test if i18n module has registered factory
     * for this data object to be able to perform i18n action. */
    protected boolean enable(Node[] activatedNodes) {    
        if(!super.enable(activatedNodes))
            return false;
        
        DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
        
        if(dataObject == null)
            return false;
        
        return FactoryRegistry.hasFactory(dataObject.getClass().getName());
    }
    
    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return I18nUtil.getBundle().getString("CTL_I18nAction");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (I18nModule.class);
    }

    /** Gets the action's icon location.
     * @return the action's icon location
     */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
    }
}
