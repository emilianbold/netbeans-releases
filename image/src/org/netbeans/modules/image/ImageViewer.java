/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.windows.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;


/**
 * Top component providing a viewer for images.
 * @author Petr Hamernik, Ian Formanek, Lukas Tadial
 */

public class ImageViewer extends CloneableTopComponent {

    /** Serialized version UID. */
    static final long serialVersionUID =6960127954234034486L;
    
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
        initialize(obj);
    }
    
    /** Overriden to explicitely set persistence type of ImageViewer
     * to PERSISTENCE_ONLY_OPENED */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
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
        TopComponent.NodeName.connect (this, obj.getNodeDelegate ());
        
        storedObject = obj;
        storedImage = new NBImageIcon(storedObject);
            
        // force closing panes in all workspaces, default is in current only
        setCloseOperation(TopComponent.CLOSE_EACH);
        
        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(
                    storedImage.getImage(),
                    0,
                    0,
                    (int)(getScale () * storedImage.getIconWidth ()),
                    (int)(getScale () * storedImage.getIconHeight ()),
                    0,
                    0,
                    storedImage.getIconWidth(),
                    storedImage.getIconHeight(),
                    this
                );
                
                if(showGrid) {
                    int x = (int)(getScale () * storedImage.getIconWidth ());
                    int y = (int)(getScale () * storedImage.getIconHeight ());
                    
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

        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_ImageViewer"));        
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
        toolBar.setFloatable (false);
        toolBar.setName (NbBundle.getBundle(ImageViewer.class).getString("ACSN_Toolbar"));
        toolBar.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_Toolbar"));
            JButton outButton = new JButton(SystemAction.get(ZoomOutAction.class));
            outButton.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomOut"));
            outButton.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_Out_BTN_Mnem").charAt(0));
            outButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_Out_BTN"));
            outButton.setLabel("");
        toolBar.add(outButton);       
        toolBar.addSeparator(new Dimension(2,2));
            JButton inButton = new JButton(SystemAction.get(ZoomInAction.class));
            inButton.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomIn"));
            inButton.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_In_BTN_Mnem").charAt(0));
            inButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_In_BTN"));
            inButton.setLabel("");
        toolBar.add(inButton);
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
//        SystemAction sa = SystemAction.get(CustomZoomAction.class);
//        sa.putValue (Action.SHORT_DESCRIPTION, NbBundle.getBundle(ImageViewer.class).getString("LBL_CustomZoom"));
        toolBar.add (getZoomButton ());
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
        setToolTipText(FileUtil.getFileDisplayName(fo));
    }

    /** Docks the table into the workspace if top component is valid.
     *  (Top component may become invalid after deserialization)
     */
    public void open(Workspace workspace){
        if (discard()) return;

        Workspace realWorkspace = (workspace == null)
                                  ? WindowManager.getDefault().getCurrentWorkspace()
                                  : workspace;
        dockIfNeeded(realWorkspace);
        boolean modeVisible = false;
        TopComponent[] tcArray = editorMode(realWorkspace).getTopComponents();
        for (int i = 0; i < tcArray.length; i++) {
            if (tcArray[i].isOpened(realWorkspace)) {
                modeVisible = true;
                break;
            }
        }
        if (!modeVisible) {
            openOtherEditors(realWorkspace);
        }
        super.open(workspace);
        openOnOtherWorkspaces(realWorkspace);
    }
    
    /**
     */
    protected String preferredID() {
        return getClass().getName();
    }

    private void superOpen(Workspace workspace) {
        super.open(workspace);
    }


    /** Utility method, opens this top component on all workspaces
     * where editor mode is visible and which differs from given
     * workspace.  */
    private void openOnOtherWorkspaces(Workspace workspace) {
        Workspace[] workspaces = WindowManager.getDefault().getWorkspaces();
        Mode curEditorMode = null;
        Mode tcMode = null;
        for (int i = 0; i < workspaces.length; i++) {
            // skip given workspace
            if (workspaces[i].equals(workspace)) {
                continue;
            }
            curEditorMode = workspaces[i].findMode(CloneableEditorSupport.EDITOR_MODE);
            tcMode = workspaces[i].findMode(this);
            if (
                !isOpened(workspaces[i]) &&
                curEditorMode != null &&
                (
                    tcMode == null ||
                    tcMode.equals(curEditorMode)
                )
            ) {
                // candidate for opening, but mode must be already visible
                // (= some opened top component in it)
                TopComponent[] tcArray = curEditorMode.getTopComponents();
                for (int j = 0; j < tcArray.length; j++) {
                    if (tcArray[j].isOpened(workspaces[i])) {
                        // yep, open this top component on found workspace too
                        pureOpen(this, workspaces[i]);
                        break;
                    }
                }
            }
        }
    }

    /** Utility method, opens top components which are opened
     * in editor mode on some other workspace.
     * This method should be called only if first top component is
     * being opened in editor mode on given workspace  */
    private void openOtherEditors(Workspace workspace) {
        // choose candidates for opening
        Set topComps = new HashSet(15);
        Workspace[] wsArray = WindowManager.getDefault().getWorkspaces();
        Mode curEditorMode = null;
        TopComponent[] tcArray = null;
        for (int i = 0; i < wsArray.length; i++) {
            curEditorMode = wsArray[i].findMode(CloneableEditorSupport.EDITOR_MODE);
            if (curEditorMode != null) {
                tcArray = curEditorMode.getTopComponents();
                for (int j = 0; j < tcArray.length; j++) {
                    if (tcArray[j].isOpened(wsArray[i])) {
                        topComps.add(tcArray[j]);
                    }
                }
            }
        }
        // open choosed candidates
        for (Iterator iter = topComps.iterator(); iter.hasNext(); ) {
            pureOpen((TopComponent)iter.next(), workspace);
        }
    }
        
    /** Utility method, calls super version of open if given
     * top component is of Editor type, or calls regular open otherwise.
     * The goal is to prevent from cycle open call between
     * Editor top components  */
    private void pureOpen(TopComponent tc,Workspace workspace) {
        if (tc instanceof ImageViewer) {
            ((ImageViewer)tc).dockIfNeeded(workspace);
            ((ImageViewer)tc).superOpen(workspace);
        } else {
            tc.open(workspace);
        }
    }

    /** Dock this top component to editor mode if it is not docked
     * in some mode at this time  */
    private void dockIfNeeded(Workspace workspace) {
        // dock into editor mode if possible
        Mode ourMode = workspace.findMode(this);
        if (ourMode == null) {
            editorMode(workspace).dockInto(this);
        }
    }

    private Mode editorMode(Workspace workspace) {
        Mode ourMode = workspace.findMode(this);
        if (ourMode == null) {
            ourMode = workspace.createMode(
                          CloneableEditorSupport.EDITOR_MODE, getName(),
                          CloneableEditorSupport.class.getResource(
                              "/org/openide/resources/editorMode.gif" // NOI18N
                          )
                      );
        }
        return ourMode;
    }
    
    /** Gets HelpContext. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(ImageViewer.class);
    }
        
    /** This component should be discarded if the associated environment
     *  is not valid.
     */
    private boolean discard () {
        return storedObject == null;
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
        return Utilities.loadImage("org/netbeans/modules/image/imageObject.gif"); // NOI18N
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
            (int)(getScale () * storedImage.getIconWidth ()),
            (int)(getScale () * storedImage.getIconHeight()))
        );
        panel.revalidate();
    }
    
    /** Tests new size of image. If image is smaller than  minimum
     *  size(1x1) zooming will be not performed.
     */
    private boolean isNewSizeOK() {
        if (((getScale () * storedImage.getIconWidth ()) > 1) &&
            ((getScale () * storedImage.getIconWidth ()) > 1)
        ) return true;
        return false;
    }
    
    /** Perform zoom with specific proportion.
     * @param fx numerator for scaled
     * @param fy denominator for scaled
     */
    public void customZoom(int fx, int fy) {
        double oldScale = scale;
        
        scale = (double)fx/(double)fy;
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Return zooming factor.*/
    private double getScale () {
        return scale;
    }
    
    /** Change proportion "out"*/
    private void scaleOut() {
        scale = scale / changeFactor;
    }
    
    /** Change proportion "in"*/
    private void scaleIn() {
        double oldComputedScale = getScale ();
        
        scale = changeFactor * scale;
        
        double newComputedScale = getScale();
        
        if (newComputedScale == oldComputedScale)
            // Has to increase.
            scale = newComputedScale + 1.0D;
    }
    
    /** Gets zoom button. */
    private JButton getZoomButton(final int xf, final int yf) {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(""+xf+":"+yf); // NOI18N
        if (xf < yf)
            button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomOut") + " " + xf + " : " + yf);
        else
            button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomIn") + " " + xf + " : " + yf);
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Zoom_BTN"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customZoom(xf, yf);
            }
        });
        
        return button;
    }
    
    private JButton getZoomButton() {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(NbBundle.getBundle(CustomZoomAction.class).getString("LBL_XtoY")); // NOI18N
        button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_CustomZoom"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Zoom_BTN"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                CustomZoomAction sa = (CustomZoomAction) SystemAction.get(CustomZoomAction.class);
                sa.performAction ();
            }
        });
        
        return button;
    }
    
    /** Gets grid button.*/
    private JButton getGridButton() {
        // PENDING buttons should have their own icons.
        final JButton button = new JButton(" # "); // NOI18N
        button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ShowHideGrid"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Grid_BTN"));
        button.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_Grid_BTN_Mnem").charAt(0));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showGrid = !showGrid;
                panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
            }
        });
        
        return button;
    }
    
}
