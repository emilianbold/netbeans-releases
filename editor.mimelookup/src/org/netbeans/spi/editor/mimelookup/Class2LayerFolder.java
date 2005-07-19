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

/**
 * Provides a mapping of class to specific subfolder of the xml layer filesystem. 
 * Instances of this class should be registed to default lookup by 
 * <a href="http://openide.netbeans.org/lookup/"> META-INF/services registration</a>.
 * Using this mapping one can achieve the convenient way of using MimeLookup i.e.
 * <p>
 * <code>
 *     MimeLookup.getMimeLookup("text/x-java").lookup(FoldManager.class);
 * </code>
 * <p>
 * Using this, the registered instances of FoldManager will be retrieved from the folder with path 
 * "Editors/text/x-java/foldManager" provided that FoldManager.class is registered to
 * a subfolder "foldManager" via Class2LayerFolder registration.
 *  
 * @author Miloslav Metelka, Martin Roskanin
 */
public interface Class2LayerFolder {
    
    /**
     * Gets class of the lookuped object, i.e. FoldManager.class
     *
     * @return class of the lookuped object.
     */
    Class getClazz();
    
    /**
     * Gets layer subfolder name, where the class should be found.
     * Folder should be located in the appropriate mime type path, i.e.
     * <p>
     * <code>Editors/text/x-java/&lt;desired-layer-subfolder-name&gt;</code>
     * <br>
     * or 
     * <br>
     * <code>Editors/&lt;desired-layer-folder-name&gt;</code>
     * for mime type insensitive objects
     * @return layer folder name
     */
    String getLayerFolderName();
    
    /**
     * Get an instance provider if necessary
     * or return <code>null</code> if the default behavior
     * which returns all the collected instances as the result is desired.
     *
     * @return instance provider returning instances of {@link #getClazz()}
     *  or <code>null</code> if all the declared fileobjects should
     *  be instantiated and returned as lookup result.
     */
    InstanceProvider getInstanceProvider();
    
}
