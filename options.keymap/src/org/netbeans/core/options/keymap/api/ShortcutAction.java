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


package org.netbeans.core.options.keymap.api;


/**
 * A special object for representing the action to which the shortcut
 * is bound. As we are able to represent different kinds of actions (
 * (e.g. represented by javax.swing.Action or javax.swing.text.TextAction)
 * the instances of this interface wrap the original action and provide
 * methods contained in this interface.
 * 
 * @author David Strupl
 */
public interface ShortcutAction {

    /**
     * The display name is what the user can see when the shortcut
     * is displayed in the configuration dialogs.
     * @return the display name
     */
    public String getDisplayName ();
    
    /**
     * The ID of the shortcut action. It is the action class name or some
     * other unique identification of the action ("cut-to-clipboard" or
     * "org.openide.actions.CutAction").
     * @return 
     */
    public String getId ();
    
    /**
     * If the same action is supplied by more KeymapManagers they can "know"
     * about each other. If the action "knows" what the ID of the action
     * is coming from the other provider it can supply it by returning a non-null
     * value from this method. An example: actions coming from the editor
     * can supply the class name of the corresponding openide action, e.g.
     * org.openide.actions.CutAction.
     * @return 
     */
    public String getDelegatingActionId ();
    
    /**
     * If the action is "compound" action (delegating to different actions
     * for different keymapManagers) this method returns the instance registered
     * in the given manager. If the action is not composed of more actions
     * this method should simply return <code>this</code>.
     * 
     * @param keymapManagerName 
     * @return 
     */
    public ShortcutAction getKeymapManagerInstance(String keymapManagerName);
}

