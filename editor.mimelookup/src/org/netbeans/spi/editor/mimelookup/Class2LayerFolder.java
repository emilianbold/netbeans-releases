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
 * Provides further clasification of objects registered for a mime type. This
 * interface is a hook into the implementation of the default
 * <code>MimeDataProvider</code> that can be used to further specify where to
 * look for instances of a certain class and how to create them.
 *
 * <p>The implementations of this interface should be registered among the services
 * in the default lookup, for details look at
 * <a href="http://openide.netbeans.org/lookup/index.html"> META-INF/services registration</a>.
 *
 * <p>The default <code>MimeDataProvider</code> allowes to register instances
 * in a hierarchy of folders on the system filesystem (modules XML layers). The
 * hierarchy starts with the Editors/ folder and then contains subfolders for
 * each mime type that has some registered objects. So, for example there might
 * be settings registered for the mime type identified by the following mime
 * path 'text/x-jsp/text/x-java' and they would be located in the folder
 * 'Editors/text/x-jsp/text/x-java' on the system filesystem. For more details
 * on the implementation of the default <code>MimeDataProvider</code> please see
 * the <a href="package-summary.html#defaultMimeDataProvider"/>SPI package</a> description.
 *
 * <p>The implementations of this interface are used for determining the structure
 * under the folder belonging to a given mime type. This interface allows to 
 * tell the default <code>MimeDataProvider</code> that instances of a certain
 * class are registered in a specific subfolder rather then under the folder
 * belonging to the mime type. When looking up instances of such a class the
 * default <code>MimeDataProvider</code> will not look in the mime type's folder,
 * but in its subfolder, which name it will obtain by calling the implementation
 * of this interface.
 *
 * <p>Therefore, for example instances of the <code>FolderManager</code> class
 * can be registered in the 'Editors/text/x-java/foldManager' folder and they
 * will be properly retrieved when calling
 * <code>MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FolderManager.class);</code>.
 *
 * @author Miloslav Metelka, Martin Roskanin, Vita Stejskal
 */
public interface Class2LayerFolder {
    
    /**
     * Gets the class of the instances that are registered under the special
     * subfolder.
     *
     * @return The class which this object provides an additional information for.
     */
    Class getClazz();
    
    /**
     * Gets the name of the subfolder where the instances are registered. The
     * subfolder should be located be located under the folder belonging to
     * the appropriate mime type, i.e.
     * <code>Editors/text/x-java/&lt;desired-layer-subfolder-name&gt;</code>.
     *
     * @return The mime type subfolder name.
     */
    String getLayerFolderName();
    
    /**
     * Gets the <code>InstanceProvider</code> that should be used for creating
     * the registered instances. This method can return <code>null</code> if
     * there is no speacial <code>InstanceProvider</code> needed and the instances
     * can be created in the standard way.
     *
     * @return The <code>InstanceProvider</code> capable of createing instances
     * of the {@link #getClazz()} class. Can return <code>null</code>.
     */
    InstanceProvider getInstanceProvider();
    
}
