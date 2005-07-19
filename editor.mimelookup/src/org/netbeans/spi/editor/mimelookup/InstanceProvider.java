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

package org.netbeans.spi.editor.mimelookup;

import java.util.List;

/**
 * Provider of the instance of the given class.
 * <br>
 * The provider gets a list of files which it transfers
 * into one instance of the class for which it's declared.
 *
 * <p>
 * For example there can be an instance provider
 * of actions for the editor popup. The file object names
 * of the actions declared in the layer can be of two forms:<ul>
 *   <li><i>MyAction.instance</i> are actions instances declaration files</li>.
 *   <li><i>reformat-code</i> are editor actions names</li>.
 * </ul>
 * <br/>
 * The instance provider translates all the file objects to actions
 * which it returns as a collection in some sort of collection-like class
 * e.g.<pre>
 * interface PopupActions {
 *
 *     List<Action> getActions();
 *
 * }</pre>
 *
 */
public interface InstanceProvider {
    
    /**
     * Create an instance of the class for which this
     * instance provider is declared in {@link Class2LayerFolder}.
     *
     * @param fileObjectList non-null list of the file objects
     *  collected from the particular layer folder and possibly
     *  the inherited folders.
     * @return non-null instance of the class for which
     *  this instance provider is declared. The list of the file objects
     *  should be translated to that instance so typically the instance
     *  contains some kind of the collection.
     */
    public Object createInstance(List/*<FileObject>*/ fileObjectList);

}
