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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.Workspace;


/**
 * Top component providing a viewer for images.
 * @author Petr Hamernik, Ian Formanek, Lukas Tadial
 */

public class ImageViewer extends CloneableTopComponent {
    
    /** <code>ImageDataObject</code> which image is viewed. */
    private ImageDataObject storedObject;
    
    /** Viewed image is serializable. */
    private NBImageIcon storedImage;
    
    /** Component showing image. */
    private JPanel panel;
    
    /** Height to width image factor. */
    private float factor;
    
    /** Numerator for scale. */
    private int scale_x = 1;
    
    /** Denominator for scale. */
    private int scale_y = 1;
    
    /** Listens for name changes. */
    private PropertyChangeListener nameChangeL;
    
    /** Icon. */
    private static Image icon = null;
    
    /** Serialized version UID. */
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
    
    /** Reloads icon. */
    protected void reloadIcon(NBImageIcon icon) {
        // Reset values.
        storedImage = icon;
        
        factor = (float)storedImage.getIconHeight() / storedImage.getIconWidth(); // y/x
        
        resizePanel();
        panel.repaint();
    }
    
    /** Initializes member variables and set listener for name changes on DataObject. */
    private void initialize (ImageDataObject obj) {
        storedObject = obj;
        storedImage = new NBImageIcon(storedObject);
        
        factor = (float)storedImage.getIconHeight() / storedImage.getIconWidth(); // y/x
        
        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                g.drawImage(
                storedImage.getImage(),
                0,
                0,
                (int)(storedImage.getIconWidth() * getScale()),
                (int)((storedImage.getIconWidth() * getScale()) * factor),
                0,
                0,
                storedImage.getIconWidth(),
                storedImage.getIconHeight(),
                this
                );
                
            }
            
            /** Calculates factor of image when fully loaded. Overrides superclass method. */
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
                if ((infoflags & (FRAMEBITS|ALLBITS)) != 0) {
                    factor = (float)h/w;
                }
                return (infoflags & (ALLBITS|ABORT)) == 0;
            }
        };
        
        storedImage.setImageObserver(panel);
        panel.setPreferredSize(new Dimension(storedImage.getIconWidth(), storedImage.getIconHeight() ));
        JScrollPane scroll = new JScrollPane(panel);
        
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.NORTH);
        
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

    /** Creates toolbar. */
    private JToolBar createToolBar() {
        // Ddefinition of toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.add(new ZoomOutAction());
        toolBar.add(new ZoomInAction());
        toolBar.addSeparator();
        toolBar.add(getZoomButton(1,1));
        toolBar.addSeparator();
        toolBar.add(getZoomButton(1,3));
        toolBar.add(getZoomButton(1,5));
        toolBar.add(getZoomButton(1,7));
        toolBar.addSeparator();
        toolBar.add(getZoomButton(3,1));
        toolBar.add(getZoomButton(5,1));
        toolBar.add(getZoomButton(7,1));
        toolBar.addSeparator();
        toolBar.add(new CustomZoomAction());
        
        return toolBar;
    }
    
    /** Updates the name and tooltip of this top component according to associated data object. */
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
            Mode editorMode = realW.findMode(CloneableEditorSupport.EDITOR_MODE);
            if (editorMode != null) editorMode.dockInto(this);
        }
        super.open (w);
    }
    
    /** Gets HelpContext. */
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
    
    /** Creates cloned object which uses the same underlying data object. */
    protected CloneableTopComponent createClonedObject () {
        return new ImageViewer(storedObject);
    }
    
    /** Overrides superclass method. Gets actions for this top component. */
    public SystemAction[] getSystemActions() {
        SystemAction[] oldValue = super.getSystemActions();
        return SystemAction.linkActions(new SystemAction[] {
            SystemAction.get(ZoomInAction.class),
            SystemAction.get(ZoomOutAction.class),
            SystemAction.get(CustomZoomAction.class),
            null},
            oldValue);
    }
    
    /** Overrides superclass method. Gets <code>Icon</code>. */
    public Image getIcon () {
        if (icon == null)
            icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/netbeans/modules/image/imageObject.gif")); // NOI18N
        return icon;
    }
    
    /** Draws zoom in scaled image. */
    public void zoomIn() {
        scaleIn();
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Draws zoom out scaled image. */
    public void zoomOut() {
        if (isNewSizeOK()) { // You can't still make picture smaller, but bigger why not?            
            scaleOut();
            resizePanel();
            panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
        } // Show dialog ? I thing no.
    }
    
    /** Resizes panel. */
    private void resizePanel() {
        panel.setPreferredSize(new Dimension(
            (int)(storedImage.getIconWidth() * getScale()),
            (int)((storedImage.getIconWidth() * getScale())*factor))
        );
        panel.revalidate();
    }
    
    /** Tests new size of image. If image is smaller than  minimum
     *  size(1x1) zooming will be not performed.
     */
    private boolean isNewSizeOK() {
        scaleOut();
        if ((storedImage.getIconWidth() * getScale()) > 1
        && ((storedImage.getIconWidth() * getScale()) * factor) > 1) {
            scaleIn();
            return true;
        }
        scaleIn();
        return false;
    }
    
    /** Perform zoom with specific proportion.
     * @param fx numerator for scaled
     * @param fy denominator for scaled
     */    
    public void customZoom(int fx, int fy) {
        scale_x = fx;
        scale_y = fy;
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Return zooming factor.*/
    private float getScale() {
        return scale_x/(float)scale_y;
    }
    
    /** Change proportion "out"*/
    private void scaleOut() {
        if (scale_x > 1) {
            scale_x--;
            return;
        }
        scale_y++;
    }
    
    /** Change proportion "in"*/
    private void scaleIn() {
        if (scale_y > 1) {
            scale_y--;
            return;
        }
        scale_x++;
    }
    
    /** Gets zoom button. */
    private JButton getZoomButton(final int xf, final int yf) {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(""+xf+":"+yf); // NOI18N
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customZoom(xf, yf);
            }
        });
        
        return button;
    }

}
