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

package org.netbeans.modules.image;

import java.awt.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.windows.*;

/** Top component providing a viewer for images.
*
* @author Petr Hamernik, Ian Formanek
*/
public class ImageViewer extends CloneableTopComponent {
    private static final int MINIMUM_WIDTH = 200;
    private static final int MINIMUM_HEIGHT = 150;

    private static final int DEFAULT_BORDER_WIDTH = 40;
    private static final int DEFAULT_BORDER_HEIGHT = 40;

    private ImageDataObject storedObject;
    private JLabel label;

    /** Listens for name changes
    */
    private PropertyChangeListener nameChangeL; 

    static final long serialVersionUID =6960127954234034486L;

    /** Default constructor. Must be here, used during de-externalization */
    public ImageViewer () {
        super();
    }

    /** Create a new image viewer.
    * @param obj the data object holding the image
    */
    public ImageViewer(ImageDataObject obj) {
        super(obj);
        initialize(obj);
    }

    /** Private constructor, used for cloning
    */
    private ImageViewer(ImageDataObject obj, JLabel label) {
        super(obj);
        Icon icon = label.getIcon();
        this.label = (icon != null) ? new JLabel(icon) : new JLabel();
        initialize(obj);
    }

    /** Reloads icon, call from event-dispaching thread only ! 
    */
    protected void reloadIcon(Icon icon) {
        label.setIcon(icon);
        this.repaint();
    }

    /** Initializes member variables and set listener for name changes on DataObject
    */
    private void initialize (ImageDataObject obj) {
        storedObject = obj;
        if(label == null) // when using deserialization by cloning
              label = new JLabel( new NBImageIcon(obj));
        JScrollPane scroll = new JScrollPane(label);
        setLayout(new BorderLayout());
        add(scroll, "Center"); // NOI18N

        nameChangeL = new PropertyChangeListener() {
                          public void propertyChange(PropertyChangeEvent evt) {
                              if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) ||
                                      DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                                  updateName();
                              }
                          }
                      };
        obj.addPropertyChangeListener(WeakListener.propertyChange(nameChangeL, obj));
    }
    
    /** Updates the name and tooltip of this top component according to associated data object.
    */
    private void updateName () {
        // update name
        String name = storedObject.getNodeDelegate().getDisplayName();
        setName(name);
        // update tooltip
        FileObject fo = storedObject.getPrimaryFile();
        StringBuffer fullName = new StringBuffer(fo.getPackageName('.'));
        String extension = fo.getExt();
        if (extension.length() > 0) {
            fullName.append(" ["); // NOI18N
            fullName.append(extension);
            fullName.append(']');
        }
        setToolTipText(fullName.toString());
    }



    /** Show the component on given workspace. If given workspace is
    * not active, component will be shown only after given workspace
    * will become visible.
    * Note that this method only makes it visible, but does not
    * give it focus.
    * @param workspace Workspace on which component should be opened.
    * @see #requestFocus
    */
    public void open (Workspace w) {
        Workspace realW = (w == null)
                          ? org.openide.TopManager.getDefault().getWindowManager().getCurrentWorkspace()
                          : w;
        Mode viewerMode = realW.findMode(this);
        if (viewerMode == null) {
            Mode editorMode = realW.findMode(EditorSupport.EDITOR_MODE);
            if (editorMode != null) editorMode.dockInto(this);
        }
        super.open (w);
    }

    /** Overrides getPreferredSize.
    */
    public Dimension getPreferredSize () {
        Dimension pref = label.getPreferredSize ();
        return new Dimension (Math.max (DEFAULT_BORDER_WIDTH + pref.width, MINIMUM_WIDTH), Math.max (DEFAULT_BORDER_HEIGHT + pref.height, MINIMUM_HEIGHT));
    }

    /** Gets HelpContext.
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(ImageViewer.class);
    }

    /** Serialize this top component. Serializes its data object in addition
    * to common superclass behaviour.
    * @param out the stream to serialize to
    */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        super.writeExternal(out);
        out.writeObject(storedObject);
    }

    /** Deserialize this top component.
    * Reads its data object and initializes itself in addition
    * to common superclass behaviour.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        storedObject = (ImageDataObject)in.readObject();
        // to reset the listener for FileObject changes
        ((ImageOpenSupport)storedObject.getCookie(ImageOpenSupport.class)).prepareViewer(); 
        initialize(storedObject);
    }

    /** Creates cloned object which uses the same underlying data object.
    */
    protected CloneableTopComponent createClonedObject () {
        return new ImageViewer(storedObject, label);
    }

}

/*
 * Log
 *  15   Gandalf   1.14        3/8/00   David Simonek   bugfix - repaired 
 *       serialization of this top component
 *  14   Gandalf   1.13        1/5/00   Ian Formanek    NOI18N
 *  13   Gandalf   1.12        11/27/99 Patrik Knakal   
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/9/99  Ian Formanek    Fixed bug 4309 - opening
 *       an image file throws exception
 *  10   Gandalf   1.9         10/8/99  Ian Formanek    Removed debug printlns
 *  9    Gandalf   1.8         9/13/99  Ian Formanek    Fixed bug 3671 - Image 
 *       Viewer  window is opened much to small.
 *  8    Gandalf   1.7         7/20/99  Jesse Glick     Context help (window 
 *       system changes).
 *  7    Gandalf   1.6         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  4    Gandalf   1.3         1/7/99   Jaroslav Tulach Uses OpenSupport
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
