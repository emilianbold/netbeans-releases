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
import java.awt.Color;
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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
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

    /** Serialized version UID. */
    static final long serialVersionUID =6960127954234034486L;
    
    /** Icon of top component. */
    private static Image icon = null;
    
    /** <code>ImageDataObject</code> which image is viewed. */
    private ImageDataObject storedObject;
    
    /** Viewed image. */
    private NBImageIcon storedImage;
    
    /** Component showing image. */
    private JPanel panel;
    
    /** Scale of image. */
    private double scale = 1.0D;
    
    /** On/off grid. */
    private boolean showGrid = false;
    
    /** Increase/decrease factor. */
    private final double changeFactor = Math.sqrt(2.0D);
    
    /** Grid color. */
    private final Color gridColor = Color.black;
    
    /** Listens for name changes. */
    private PropertyChangeListener nameChangeL;
    
    
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
        
        resizePanel();
        panel.repaint();
    }
    
    /** Initializes member variables and set listener for name changes on DataObject. */
    private void initialize(ImageDataObject obj) {
        storedObject = obj;
        storedImage = new NBImageIcon(storedObject);
        
        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                g.drawImage(
                    storedImage.getImage(),
                    0,
                    0,
                    (int)(storedImage.getIconWidth() * getScale()),
                    (int)(storedImage.getIconHeight() * getScale()),
                    0,
                    0,
                    storedImage.getIconWidth(),
                    storedImage.getIconHeight(),
                    this
                );
                
                if(showGrid) {
                    int x = (int)(storedImage.getIconWidth() * getScale());
                    int y = (int)(storedImage.getIconHeight() * getScale());
                    
                    double gridDistance = getScale();
                    
                    if(gridDistance < 2) 
                        // Disable painting of grid if no image pixels would be visible.
                        return;
                    
                    g.setColor(gridColor);
                    
                    double actualDistance = gridDistance;
                    for(int i = (int)actualDistance; i < x ;actualDistance += gridDistance, i = (int)actualDistance) {
                        g.drawLine(i,0,i,(y-1));
                    }

                    actualDistance = gridDistance;
                    for(int j = (int)actualDistance; j < y; actualDistance += gridDistance, j = (int)actualDistance) {
                        g.drawLine(0,j,(x-1),j);
                    }
                }
                
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
        // Definition of toolbar.
        JToolBar toolBar = new JToolBar();
        
        toolBar.add(SystemAction.get(ZoomOutAction.class));
        toolBar.addSeparator(new Dimension(2,2));
        toolBar.add(SystemAction.get(ZoomInAction.class));
        toolBar.addSeparator(new Dimension(11,2));
        toolBar.add(getZoomButton(1,1));
        toolBar.addSeparator(new Dimension(11,2));
        toolBar.add(getZoomButton(1,3));
        toolBar.addSeparator(new Dimension(2,2));
        toolBar.add(getZoomButton(1,5));
        toolBar.addSeparator(new Dimension(2,2));
        toolBar.add(getZoomButton(1,7));
        toolBar.addSeparator(new Dimension(11,2));
        toolBar.add(getZoomButton(3,1));
        toolBar.addSeparator(new Dimension(2,2));
        toolBar.add(getZoomButton(5,1));
        toolBar.addSeparator(new Dimension(2,2));
        toolBar.add(getZoomButton(7,1));
        toolBar.addSeparator(new Dimension(11,2));
        toolBar.add(SystemAction.get(CustomZoomAction.class));
        toolBar.addSeparator(new Dimension(11,2));
        toolBar.add(getGridButton());
        
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
        double oldScale = scale;
        
        scaleOut();
        
         // You can't still make picture smaller, but bigger why not?
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Resizes panel. */
    private void resizePanel() {
        panel.setPreferredSize(new Dimension(
            (int)(storedImage.getIconWidth() * getScale()),
            (int)(storedImage.getIconWidth() * getScale()))
        );
        panel.revalidate();
    }
    
    /** Tests new size of image. If image is smaller than  minimum
     *  size(1x1) zooming will be not performed.
     */
    private boolean isNewSizeOK() {
        if ((storedImage.getIconWidth() * getScale()) > 1
        && (storedImage.getIconWidth() * getScale()) > 1) {
            return true;
        }
        
        return false;
    }
    
    /** Perform zoom with specific proportion.
     * @param fx numerator for scaled
     * @param fy denominator for scaled
     */
    public void customZoom(int fx, int fy) {
        double oldScale = scale;
        
        scale = (double)fx/fy;
        
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Return zooming factor.*/
    private double getScale() {
        if(scale > 1.0D)
            scale = Math.floor(scale);
        
        return scale;
    }
    
    /** Change proportion "out"*/
    private void scaleOut() {
        scale = scale / changeFactor;
    }
    
    /** Change proportion "in"*/
    private void scaleIn() {
        double oldComputedScale = getScale();
        
        scale = changeFactor * scale;
        
        double newComputedScale = getScale();
        
        if(newComputedScale == oldComputedScale)
            // Has to increase.
            scale = newComputedScale + 1.0D;
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
    
    /** Gets grid button.*/
    private JButton getGridButton() {
        // PENDING buttons should have their own icons.
        final JButton button = new JButton(" # "); // NOI18N

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showGrid = !showGrid;
                panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
            }
        });
        
        return button;
    }
    
}
