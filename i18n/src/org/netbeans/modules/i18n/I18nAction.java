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
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.FileOwnerQuery;


/**
 * Internationalize action. Runs "i18n session" over specified source. Finds
 * non-i19n-ized hard coded strings and offers them i18n-ize to user in step-by-step
 * manner.
 *
 * @author   Petr Jiricka
 * @see I18nManager
 */
public class I18nAction extends NodeAction {

    /** Generated sreial version UID. */
    static final long serialVersionUID =3322896507302889271L;

    public I18nAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
    
    /** 
     * Actually performs the action. Implements superclass abstract method.
     * @param activatedNodes Currently activated nodes.
     */
    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes.length != 1)
            return;

        final Node node = activatedNodes[0];
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
        if (dataObject == null)
            return;

        EditorCookie editorCookie = (EditorCookie) node.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            editorCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
            if (editorCookie == null)
                return;
        }

        editorCookie.open(); 
        I18nManager.getDefault().internationalize(dataObject);
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Overrides superclass method. Adds additional test if i18n module has registered factory
     * for this data object to be able to perform i18n action. */
    protected boolean enable(Node[] activatedNodes) {    
        if (activatedNodes.length != 1)
            return false;

        final Node node = activatedNodes[0];
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
        if (dataObject == null || dataObject.getPrimaryFile() == null)
            return false;

        EditorCookie editorCookie = (EditorCookie) node.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            editorCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
            if (editorCookie == null)
                return false;
        }

        if (!FactoryRegistry.hasFactory(dataObject.getClass()))
            return false;

	// check that the node has project
	if (FileOwnerQuery.getOwner(dataObject.getPrimaryFile()) == null)
            return false;

	return true;
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return I18nUtil.getBundle().getString("CTL_I18nAction");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_AUTOINSERT);
    }

    /** Gets the action's icon location.
     * @return the action's icon location
     */
     protected String iconResource () {
         return "org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
     }
}
