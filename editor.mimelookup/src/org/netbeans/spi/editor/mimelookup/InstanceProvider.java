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
