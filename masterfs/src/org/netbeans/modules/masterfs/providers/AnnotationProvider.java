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

package org.netbeans.modules.masterfs.providers;

import java.io.IOException;

/** Can provide status and actions for FileObjects. Register it 
 * in META-INF/services/org.netbeans.modules.masterfs.providers.AnnotationProvider
 * file.
 *
 * @author Jaroslav Tulach
 */
public abstract class AnnotationProvider extends Object {
    /** listeners */
    private org.openide.filesystems.FileStatusListener listener;
    /** lock for modification of listeners */
    private static Object LOCK = new Object ();
    
    
    /** Annotate the name of a file cluster.
    * @param name the name suggested by default
    * @param files an immutable set of {@link FileObject}s belonging to this filesystem
    * @return the annotated name or null if this provider does not know how to annotate these files
    */
    public abstract String annotateName (String name, java.util.Set files);

    /** Annotate the icon of a file cluster.
     * <p>Please do <em>not</em> modify the original; create a derivative icon image,
     * using a weak-reference cache if necessary.
    * @param icon the icon suggested by default
    * @param iconType an icon type from {@link java.beans.BeanInfo}
    * @param files an immutable set of {@link FileObject}s belonging to this filesystem
    * @return the annotated icon or null if some other provider shall anotate the icon
    */
    public abstract java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files);
    
    /** Annotate a name such that the returned value contains HTML markup.
     * The return value less the html content should typically be the same 
     * as the return value from <code>annotateName()</code>.  This is used,
     * for example, by VCS filesystems to de&euml;phasize the status information
     * included in the file name by using a light grey font color. 
     * <p>
     * For consistency with <code>Node.getHtmlDisplayName()</code>, 
     * filesystems that proxy other filesystems (and so must implement
     * this interface to supply HTML annotations) should return null if
     * the filesystem they proxy does not provide an implementation of
     * HTMLStatus.
     *
     * @see org.openide.awt.HtmlRenderer
     * @see <a href="@org-openide-loaders@/org/openide/loaders/DataNode.html#getHtmlDisplayName()"><code>DataNode.getHtmlDisplayName()</code></a>
     * @see org.openide.nodes.Node#getHtmlDisplayName
     **/
    public abstract String annotateNameHtml (String name, java.util.Set files);

    /** Provides actions that should be added to given set of files.
     * @return null or array of actions for these files.
     */
    public abstract javax.swing.Action[] actions (java.util.Set files);
    
    //
    // Listener support
    //
    

    /** Registers FileStatusListener to receive events.
    * The implementation registers the listener only when getStatus () is 
    * overriden to return a special value.
    *
    * @param listener The listener to register.
    */
    public final void addFileStatusListener (
        org.openide.filesystems.FileStatusListener listener
    ) throws java.util.TooManyListenersException {
        synchronized (LOCK) {
            if (this.listener != null) {
                throw new java.util.TooManyListenersException ();
            }
            this.listener = listener;
        }
    }

    /** Removes FileStatusListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public final void removeFileStatusListener (
        org.openide.filesystems.FileStatusListener listener
    ) {
        synchronized (LOCK) {
            if (this.listener == listener) {
                this.listener = null;
            }
        }
    }

    /** Notifies all registered listeners about change of status of some files.
    *
    * @param event The event to be fired
    */
    protected final void fireFileStatusChanged(org.openide.filesystems.FileStatusEvent event) {
        org.openide.filesystems.FileStatusListener l;
        synchronized (LOCK) {
            l = this.listener;
        }
        if (l != null) {
            l.annotationChanged (event);
        }
    }    
    
    public abstract InterceptionListener getInterceptionListener();
}
