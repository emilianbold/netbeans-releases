/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mobility.svgcore.view.svg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.microedition.m2g.SVGImage;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.svgcore.export.SaveElementAsImage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.composer.ScreenManager;
import org.netbeans.modules.mobility.svgcore.export.ScreenSizeHelper;
import org.netbeans.modules.mobility.svgcore.items.form.SVGComponentDrop;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.navigator.SVGNavigatorContent;
import org.netbeans.modules.mobility.svgcore.palette.SVGPaletteItemDataObject;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGToggleAction;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.FilterNode;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGLocatableElement;
import org.xml.sax.SAXException;

/**
 * Top component which displays something.
 */
public final class SVGViewTopComponent extends TopComponent implements SceneManager.SelectionListener {
    private static final long serialVersionUID = 5862679852552354L;
    
    private static final float    ZOOM_STEP           = (float) 1.1;
    private static final float    SLIDER_DEFAULT_STEP = 0.1f;
    private static final String   PREFERRED_ID        = "SVGViewTopComponent"; //NOI18N
    private static final String[] ZOOM_VALUES         = new String[]{"400%", "300%", "200%", "100%", "75%", "50%", "25%"}; //NOI18N
    private static final String   DND_PALETTE_MIME    = "x-java-openide-dataobjectdnd"; //NOI18N
    
    private final transient SVGDataObject     m_svgDataObject;
    private transient ParsingTask             parsingTask;
    private transient Lookup                  lookup = null;
    private transient JPanel                  basePanel;
    private transient UpdateThread            m_timeUpdater = null;
    
    //UI controls
    private transient JToolBar                m_toolbar;
    private transient JToolBar                animationToolbar;
    private transient JSlider                 slider;
    private transient JSpinner                currentTimeSpinner;
    private transient JComboBox               zoomComboBox;
    private transient AbstractButton          startAnimationButton;
    private transient AbstractButton          pauseAnimationButton;
    private transient AbstractButton          scaleToggleButton;
    private transient AbstractButton          showViewBoxToggleButton;
    private transient ChangeListener          changeListener;
    private transient boolean                 doScale = false;
    //decoration
    private transient ButtonMouseListener     buttonListener;
    private transient PropertyChangeListener  nameChangeL;
    //actions
    private transient ToggleScaleAction       scaleAction;
    private transient ZoomToFitAction         zoomToFitAction;
    private transient ZoomInAction            zoomInAction;
    private transient ZoomOutAction           zoomOutAction;
    private transient ToggleShowViewBoxAction showViewBoxAction;

    private final class UpdateThread extends Thread {

        public UpdateThread() {
            super("AnimatorTimeUpdater"); //NOI18N
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            PerseusController pctrl;

            try {
                while ((pctrl = getPerseusController()) != null && !isInterrupted()) {
                    if (pctrl.getAnimatorState() == PerseusController.ANIMATION_RUNNING) {
                        final float time = pctrl.getAnimatorTime();
                        final float maxTime = getSceneManager().getAnimationDuration();

                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                updateAnimationTime(time, maxTime);
                            }
                        });
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
            }
        }
    }
    
    private final transient AbstractSVGToggleAction allowEditAction = new AbstractSVGToggleAction("svg_allow_edit") { //NOI18N
        @Override
        public void actionPerformed(ActionEvent e) {
            SceneManager smgr = getSceneManager();

            if (smgr.isReadOnly()) {
                PerseusController pc = getPerseusController();
                if (pc != null && pc.isAnimatorStarted()) {
                    startAnimationAction.actionPerformed(e);
                }
                insertGraphicsAction.setEnabled(true);
                smgr.setReadOnly(false);
            } else {
                insertGraphicsAction.setEnabled(false);
                smgr.setReadOnly(true);
            }

            updateAnimationActions();
            updateDataTransferActions();
            smgr.updateActionState();
            setIsSelected(!smgr.isReadOnly());
        }
    };
    
    private final transient AbstractSVGToggleAction startAnimationAction = new AbstractSVGToggleAction("svg_anim_start") { //NOI18N
        @Override
        public void actionPerformed(ActionEvent e) {
            PerseusController pc = getPerseusController();
            if (pc != null) {
                if (!pc.isAnimatorStarted()) {
                    if (!getSceneManager().isReadOnly()) {
                        allowEditAction.actionPerformed(e);
                    }
                    pc.startAnimator();
                    if (m_timeUpdater == null) {
                        m_timeUpdater = new UpdateThread();
                        m_timeUpdater.start();
                    }
                } else {
                    pc.stopAnimator();
                    updateAnimationTime(pc.getAnimatorTime(), getSceneManager().getAnimationDuration());
                    if (m_timeUpdater != null) {
                        m_timeUpdater.interrupt();
                        m_timeUpdater = null;
                    }
                    //reload image to completely reset animations
                    updateImage();
                }
                updateAnimationActions();
            }
        }
    };
    
    private final transient AbstractSVGToggleAction pauseAnimationAction = new AbstractSVGToggleAction("svg_anim_pause", false) { //NOI18N
        @Override
        public void actionPerformed(ActionEvent e) {
            PerseusController pc = getPerseusController();

            if (pc != null) {
                if (pc.getAnimatorState() == PerseusController.ANIMATION_RUNNING) {
                    pc.pauseAnimator();
                } else {
                    pc.startAnimator();
                }
                updateAnimationActions();
            }
        }
    };
    
    private final transient AbstractSVGAction insertGraphicsAction = new AbstractSVGAction("svg_insert_graphics", false) { //NOI18N
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int r = chooser.showDialog(SwingUtilities.getWindowAncestor(SVGViewTopComponent.this), NbBundle.getMessage(SVGViewTopComponent.class, "LBL_CHOOSE_SVG_FILE")); //NOI18N
            if (r == JFileChooser.APPROVE_OPTION) {
                final File file = chooser.getSelectedFile();
                if (!file.isFile()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(SVGViewTopComponent.class, "ERROR_NotSVGFile", file), NotifyDescriptor.Message.WARNING_MESSAGE));
                    return;
                } else {
                    Thread th = new Thread("InsertGraphicsTask") { //NOI18N
                        @Override
                        public void run() {
                            try {
                                getSceneManager().setBusyState(SceneManager.OPERATION_TOKEN, true);
                                m_svgDataObject.getModel().mergeImage(file);
                            } catch (Exception ex) {
                                SceneManager.error("Insert graphics failed.", ex); //NOI18N
                            } finally {
                                getSceneManager().setBusyState(SceneManager.OPERATION_TOKEN, false);
                            }
                        }
                    };
                    th.setPriority(Thread.MIN_PRIORITY);
                    th.setDaemon(true);
                    th.start();
                }
            }
        }
    };
    
    private final transient Action m_pasteAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            Clipboard clipboard = getClipboard();

            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                    String text = (String) clipboard.getData(DataFlavor.stringFlavor);
                    final String id = m_svgDataObject.getModel().mergeImage(text, false);
                    getSceneManager().setSelection(id, true);
                } catch (Exception ex) {
                    SceneManager.error("Paste failed.", ex); //NOI18N
                }
                return;
            }
        }
    };
    
    private final transient Action m_copyAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            putElementToClipboard(false);
        }
    };
    
    private final transient Action m_cutAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            putElementToClipboard(true);
        }
    };

    public SVGViewTopComponent(SVGDataObject dObj) {
        m_svgDataObject = dObj;
        initialize();
    }
    
    Action[] getImageContextActions(){
        return new Action[]{ zoomToFitAction , scaleToggleButton.getAction()};
    }

    private SceneManager getSceneManager() {
        return m_svgDataObject.getSceneManager();
    }

    private PerseusController getPerseusController() {
        return getSceneManager().getPerseusController();
    }

    private ScreenManager getScreenManager() {
        return getSceneManager().getScreenManager();
    }

    private Lookup createLookup() {
        Lookup elementLookup = getSceneManager().getLoookup();

        ActionMap map = getActionMap();
        return Lookups.fixed(new Object[] { new FilterNode(m_svgDataObject.getNodeDelegate(), null, new ProxyLookup(new Lookup[]{new SVGElementNode(elementLookup).getLookup(), m_svgDataObject.getNodeDelegate().getLookup()})), new SVGCookie(), map});
    }

    private void initialize() {
        lookup = createLookup();
        associateLookup(lookup);

        initComponents();

        nameChangeL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) 
                        || DataObject.PROP_NAME.equals(evt.getPropertyName())
                        || DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) 
                {
                    updateName();
                }

                if (isVisible()) {
                    if (SVGDataObject.PROP_EXT_CHANGE.equals(evt.getPropertyName())) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                //externally modified, refresh image
                                SceneManager.log(Level.INFO, "Document modified, refreshing image."); //NOI18N
                                updateImage();
                            }
                        });
                    }
                }
            }
        };

        m_svgDataObject.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeL, m_svgDataObject));
        m_toolbar = createToolBar();
        animationToolbar = createAnimationBar();

        changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == slider) {
                    float currentTime = ((float) slider.getValue()) * SLIDER_DEFAULT_STEP;
                    getPerseusController().setAnimatorTime(currentTime);
                    updateAnimationTime(currentTime, getSceneManager().getAnimationDuration());
                } else if (e.getSource() == currentTimeSpinner) {
                    float currentTime = ((Float) currentTimeSpinner.getValue()).floatValue();
                    getPerseusController().setAnimatorTime(currentTime);
                    updateAnimationTime(currentTime, getSceneManager().getAnimationDuration());
                }
            }
        };
        slider.addChangeListener(changeListener);
        currentTimeSpinner.addChangeListener(changeListener);

        basePanel = new JPanel();
        basePanel.setBackground(Color.WHITE);
        add(basePanel, BorderLayout.CENTER);
        Box bottom = new Box(BoxLayout.Y_AXIS);
        bottom.add(animationToolbar);
        bottom.add(getScreenManager().getStatusBar());
        add(bottom, BorderLayout.SOUTH);
        updateName();
    }

    private void updateAnimationTime(float time, float maxTime) {
        currentTimeSpinner.removeChangeListener(changeListener);
        slider.removeChangeListener(changeListener);
        if (maxTime != -1) {
            slider.setMaximum(Math.round(maxTime / SLIDER_DEFAULT_STEP));
        }
        slider.setValue(Math.round(time / SLIDER_DEFAULT_STEP));
        time = Math.round(time * 10) / 10.0f;
        currentTimeSpinner.setValue(new Float(time));
        slider.addChangeListener(changeListener);
        currentTimeSpinner.addChangeListener(changeListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    public JComponent getToolbar() {
        return m_toolbar;
    }

    JPanel getBasePanel() {
        return basePanel;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    private void putElementToClipboard(final boolean removeObject) {
        final SVGObject[] obj = getSceneManager().getSelected();
        if (obj != null) {
            assert obj.length > 0;
            assert obj[0] != null;
            Thread th = new Thread() {

                @Override
                public void run() {
                    try {
                        String elId = obj[0].getElementId();
                        String text = getModel().getElementAsText(elId);
                        StringSelection strSet = new StringSelection(text);
                        Clipboard clipboard = getClipboard();
                        clipboard.setContents(strSet, strSet);
                        if (removeObject) {
                            getSceneManager().deleteObject(obj[0]);
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            th.setPriority(Thread.MIN_PRIORITY);
            th.setName("PutElementToClipboardThread"); //NOI18N
            th.start();
        }
    }

    @Override
    public void componentOpened() {
        getModel().setChanged(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SVGNavigatorContent.getDefault().navigate(m_svgDataObject);
            }
        });
        addSvgPanel();

        getActionMap().put(DefaultEditorKit.pasteAction, m_pasteAction);
        getActionMap().put(DefaultEditorKit.copyAction, m_copyAction);
        getActionMap().put(DefaultEditorKit.cutAction, m_cutAction);

        getSceneManager().addSelectionListener(this);
    }

    @Override
    public void componentClosed() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                SVGNavigatorContent.getDefault().navigate(null);
            }
        });
        removeSvgPanel();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        updateDataTransferActions();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    private void addSvgPanel() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread"; //NOI18N
        final LoadPanel loadPanel = new LoadPanel();
        basePanel.add(loadPanel);
        basePanel.setLayout(new BorderLayout());
    }

    public void onShow() {
        if (getModel().isChanged()) {
            basePanel.removeAll();
            updateImage();
            getModel().setChanged(false);
        }
    }

    @Override
    public void componentHidden() {
        PerseusController perseus = getPerseusController();

        if (perseus != null) {
            perseus.stopAnimator();
        }
    }

    private void removeSvgPanel() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread"; //NOI18N
        getSceneManager().resetImage();
        basePanel.removeAll();
        //TODO
/*
        if (imagePanel != null) {
        imagePanel.removeMouseListener(mouseListener);
        imagePanel.removeMouseMotionListener(mouseMotionListener);
        imagePanel = null;
        }
        if (svgAnimator != null && svgAnimator.getState() != SVGAnimatorImpl.STATE_STOPPED){
        svgAnimator.stop();
        }
        svgAnimator = null;
         */
//        svgImage = null;
        //enableComponentsInToolbar(toolbar, false);
    }

    /** Creates cloned object which uses the same underlying data object. */
    /*
    protected CloneableTopComponent createClonedObject () {
    return new SVGViewTopComponent(m_svgDataObject);
    }
     */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /** Updates the name and tooltip of this top component according to associated data object. */
    private void updateName() {
        // DataObject check
        if (! m_svgDataObject.isValid())
            return;
        // update name
        String name = m_svgDataObject.getNodeDelegate().getDisplayName();
        setName(name);
        // update display name and tooltip
        XmlMultiViewEditorSupport edSup = m_svgDataObject.getCookie(
                XmlMultiViewEditorSupport.class);
        if (edSup != null){
            edSup.updateDisplayName();
        }
    }

    private void addButtonsForActions(JToolBar toolbar, Action[] toolbarActions, GridBagConstraints constrains) {
        for (Action action : toolbarActions) {
            if (action != null) {
                initButton(toolbar, action, action instanceof AbstractSVGToggleAction);
            } else {
                toolbar.add(createToolBarSeparator(), constrains);
            }
        }
    }

    private JToolBar createToolBar() {
        final SceneManager smgr = getSceneManager();
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new GridBagLayout());
        toolbar.setFloatable(false);
        toolbar.setFocusable(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.insets = new Insets(0, 3, 0, 2);
        toolbar.add(createToolBarSeparator(), constrains);

        buttonListener = new ButtonMouseListener();

        addButtonsForActions(toolbar, smgr.getToolbarActions("svg_prev_sel", "svg_next_sel", "svg_parent_sel"), constrains); //NOI18N
        toolbar.add(createToolBarSeparator(), constrains);
        initButton(toolbar, zoomToFitAction = new ZoomToFitAction(), false);
        initCombo(toolbar, zoomComboBox = new JComboBox(ZOOM_VALUES));

        final ComboBoxEditor cbe = zoomComboBox.getEditor();
        
        zoomComboBox.setEditor( new ComboBoxEditor() {
            private String m_lastValue = ""; //NOI18N
            public Component getEditorComponent() {
                return cbe.getEditorComponent();
            }

            public void setItem(Object anObject) {
                cbe.setItem(anObject);
            }

            public Object getItem() {
                Object o = cbe.getItem();
                if ( o != null) {
                    String value = o.toString();
                    if (value != null) {
                        value = value.trim();
                        int len = value.length();
                        if ( len > 0) {
                            if (value.endsWith("%")) { //NOI18N
                                value = value.substring(0, len - 1);
                            }
                            try {
                                float floatValue = Float.parseFloat(value);
                                m_lastValue = Math.round(floatValue) + "%"; //NOI18N
                                return m_lastValue;
                            } catch( NumberFormatException e) { }
                        }
                    }
                }
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        ((JTextComponent) cbe.getEditorComponent()).setText(m_lastValue);                            
                    }
                });
                return m_lastValue;
            }

            public void selectAll() {
                cbe.selectAll();
            }

            public void addActionListener(ActionListener l) {
                cbe.addActionListener(l);
            }

            public void removeActionListener(ActionListener l) {
                cbe.removeActionListener(l);
            }            
        });
        
        zoomComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (smgr.isImageLoaded()) {
                    ScreenManager scMgr = getScreenManager();
                    if (scMgr != null) {
                        String selection = (String) zoomComboBox.getSelectedItem();
                        if (selection != null) {
                            selection = selection.trim();
                            if (selection.endsWith("%")) {
                                //NOI18N
                                selection = selection.substring(0, selection.length() - 1);
                            }
                            try {
                                float zoom = Float.parseFloat(selection) / 100;
                                if (zoom > 0 && zoom < 100) {
                                    scMgr.setZoomRatio(zoom);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        initButton(toolbar, zoomInAction = new ZoomInAction(), false);
        initButton(toolbar, zoomOutAction = new ZoomOutAction(), false);
        toolbar.add(createToolBarSeparator(), constrains);

        addButtonsForActions(toolbar, smgr.getToolbarActions("svg_toggle_tooltip", "svg_toggle_highlight"), constrains); //NOI18N
        //hoverToggleButton = initButton(toolbar, highlightAction = new ToggleHighlightAction(), true);
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.insets = new Insets(0, 3, 0, 2);
        //constrains.weighty = 1.0;
        //constrains.fill = GridBagConstraints.VERTICAL;
        toolbar.add(createToolBarSeparator(), constrains);

        scaleToggleButton = initButton(toolbar, scaleAction = new ToggleScaleAction(), true);
        showViewBoxToggleButton = initButton(toolbar, showViewBoxAction = new ToggleShowViewBoxAction(), true);

        toolbar.add(createToolBarSeparator(), constrains);

        initButton(toolbar, allowEditAction, true);
        allowEditAction.setIsSelected(!smgr.isReadOnly());
        allowEditAction.setEnabled( m_svgDataObject.getPrimaryFile().canWrite());

        toolbar.add(createToolBarSeparator(), constrains);
        initButton(toolbar, insertGraphicsAction, false);

        addButtonsForActions(toolbar, smgr.getToolbarActions("svg_delete", null, "svg_move_to_top", "svg_move_to_bottom", "svg_move_forward", "svg_move_backward"), constrains); //NOI18N
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.fill = GridBagConstraints.HORIZONTAL;
        constrains.weightx = 1.0;
        toolbar.add(new JPanel(), constrains);

        return toolbar;
    }

    private JToolBar createAnimationBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new GridBagLayout());
        toolbar.setFloatable(false);
        toolbar.setFocusable(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.insets = new Insets(0, 3, 0, 2);
        toolbar.add(Box.createHorizontalStrut(5));

        startAnimationButton = initButton(toolbar, startAnimationAction, true);
        startAnimationAction.setIsSelected(false);
        pauseAnimationButton = initButton(toolbar, pauseAnimationAction, true);
        pauseAnimationAction.setIsSelected(false);

        toolbar.add(createToolBarSeparator(), constrains);

        float currentMaximum = PerseusController.ANIMATION_DEFAULT_DURATION;
        slider = new JSlider(JSlider.HORIZONTAL, 0, (int) (currentMaximum/SLIDER_DEFAULT_STEP), 0);
        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.fill = GridBagConstraints.HORIZONTAL;
        constrains.weightx = 100.0;

        toolbar.add(slider, constrains);
        toolbar.add(Box.createHorizontalStrut(11));

        currentTimeSpinner = new JSpinner();
        currentTimeSpinner.setToolTipText(NbBundle.getMessage(SVGViewTopComponent.class, "HINT_CurrentTime")); //NOI18N
        currentTimeSpinner.setModel(new SpinnerNumberModel(new Float(0), new Float(0.0), new Float(Integer.MAX_VALUE), new Float(0.1)));
        JComponent editor = currentTimeSpinner.getEditor();
        if ( editor instanceof JSpinner.NumberEditor) {
            DecimalFormat format = ((JSpinner.NumberEditor) editor).getFormat();
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            currentTimeSpinner.setValue(new Float(0.0f));
        }
        Font font = currentTimeSpinner.getFont();
        FontMetrics fm = currentTimeSpinner.getFontMetrics(font);
        int w = fm.stringWidth("000.0"); //NOI18N
        Dimension d = currentTimeSpinner.getPreferredSize();
        d.width = w + 20;
        currentTimeSpinner.setPreferredSize(d);

        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        JLabel currentTimeLabel = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_CurrentTime")); //NOI18N
        toolbar.add(currentTimeLabel, constrains);
        toolbar.add(Box.createHorizontalStrut(4));

        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        constrains.fill = GridBagConstraints.NONE;
        constrains.weightx = 0;
        toolbar.add(currentTimeSpinner, constrains);
        currentTimeLabel.setLabelFor(currentTimeSpinner);
        toolbar.add(Box.createHorizontalStrut(4));

        constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;
        JLabel sec = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Seconds")); //NOI18N
        toolbar.add(sec, constrains);
        toolbar.add(Box.createHorizontalStrut(10));

        return toolbar;
    }

    private AbstractButton initButton(JComponent bar, Action action, boolean isToggle) {
        Border buttonBorder = UIManager.getBorder("nb.tabbutton.border"); //NOI18N
        AbstractButton button;

        if (isToggle) {
            final JToggleButton tButton = new JToggleButton(action);
            action.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (AbstractSVGToggleAction.SELECTION_STATE.equals(evt.getPropertyName())) {
                        tButton.setSelected(((Boolean) evt.getNewValue()).booleanValue());
                        tButton.repaint();
                    }
                }
            });
            Boolean state = (Boolean) action.getValue(AbstractSVGToggleAction.SELECTION_STATE);
            if (state != null) {
                tButton.setSelected(state.booleanValue());
            }
            button = tButton;
        } else {
            button = new JButton(action);
        }

        if (buttonBorder != null) {
            button.setBorder(buttonBorder);
        }
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;

        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        if (button instanceof JButton) {
            button.addMouseListener(buttonListener);
        }
        //@inherited fix of issue #69642. Focus shouldn't stay in toolbar
        button.setFocusable(false);
        bar.add(button, constrains);
        return button;
    }

    private void initCombo(JComponent bar, JComboBox comboBox) {
        GridBagConstraints constrains = new GridBagConstraints();
        constrains.anchor = GridBagConstraints.WEST;

        //@inherited fix of issue #69642. Focus shouldn't stay in toolbar
        comboBox.setFocusable(false);

        Dimension size = comboBox.getPreferredSize();
        comboBox.setPreferredSize(size);
        comboBox.setSize(size);
        comboBox.setMinimumSize(size);
        comboBox.setMaximumSize(size);

        comboBox.setEditable(true);

        bar.add(comboBox, constrains);
    }

    private void updateDataTransferActions() {
        boolean isReadOnly = getSceneManager().isReadOnly();
        boolean isSelected = getSceneManager().getSelected() != null;
        m_pasteAction.setEnabled(!isReadOnly);
        m_cutAction.setEnabled(!isReadOnly && isSelected);
        m_copyAction.setEnabled(isSelected);
    }

    private void updateAnimationActions() {
        PerseusController pc = getPerseusController();
        if (pc != null) {
            int state = pc.getAnimatorState();
            boolean isReadOnly = getSceneManager().isReadOnly();
            enableComponentsInToolbar(animationToolbar, isReadOnly && state != PerseusController.ANIMATION_NOT_AVAILABLE, startAnimationButton, pauseAnimationButton);

            startAnimationAction.setEnabled(state != PerseusController.ANIMATION_NOT_AVAILABLE);

            boolean isActive = isReadOnly && pc.isAnimatorStarted();
            startAnimationAction.setIsSelected(isActive);
            pauseAnimationAction.setEnabled(isActive);
            pauseAnimationAction.setIsSelected(state == PerseusController.ANIMATION_PAUSED);
        }
    }

    private static JSeparator createToolBarSeparator() {
        JSeparator toolBarSeparator = new JSeparator(JSeparator.VERTICAL);
        Dimension dim = new Dimension(2, 22);
        toolBarSeparator.setPreferredSize(dim);
        toolBarSeparator.setSize(dim);
        toolBarSeparator.setMinimumSize(dim);
        return toolBarSeparator;
    }

    protected void updateZoomCombo() {
        zoomComboBox.getEditor().setItem(Integer.toString((int) (getScreenManager().getZoomRatio() * 100 + 0.5)) + "%"); //NOI18N
    }

    protected synchronized void updateImage() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread"; //NOI18N
        
        disableImageContext();
        
        getSceneManager().saveSelection();

        if (parsingTask != null) {
            parsingTask.cancel();
        }
        try {
            parsingTask = new ParsingTask(m_svgDataObject, this);
            parsingTask.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    void enableImageContext(){
        for ( Action action : getImageContextActions() ){
            action.setEnabled( true );
        }
    }

    void showImage(SVGImage img) {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread"; //NOI18N
        SceneManager smgr = getSceneManager();
        basePanel.removeAll();
        smgr.setImage(img);
        smgr.restoreSelection();
        JComponent topComponent = smgr.getComposerGUI();
        topComponent.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    zoomOutAction.actionPerformed(null);
                } else {
                    zoomInAction.actionPerformed(null);
                }
            }
        });
        basePanel.add(topComponent, BorderLayout.CENTER);
        topComponent.setDropTarget( new DropTarget( topComponent, new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) {
                doDrag(dtde);
            }
            public void dragExit(DropTargetEvent dte) {
            }
            public void dragOver(DropTargetDragEvent dtde) {
                doDrag(dtde);
            }
            public void drop(DropTargetDropEvent dtde) {
                doDrop(dtde);
            }
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }
        }));

        smgr.registerPopupActions(new Action[]{
            insertGraphicsAction,
            zoomToFitAction,
            zoomInAction,
            zoomOutAction,
            scaleAction,
            showViewBoxAction,
            startAnimationAction,
            pauseAnimationAction,
            allowEditAction}, this, lookup);

        updateZoomCombo();
        updateAnimationActions();
        smgr.processEvent(SceneManager.createEvent(this, SceneManager.EVENT_IMAGE_DISPLAYED));

        SVGLocatableElement elem = getPerseusController().getViewBoxMarker();
        ScreenManager scrMgr = getScreenManager();

        if (elem == null) {
            showViewBoxAction.setEnabled(false);
            scrMgr.setShowAllArea(true);
        } else {
            showViewBoxAction.setEnabled(true);
        }
        showViewBoxToggleButton.setSelected(scrMgr.getShowAllArea());

        topComponent.requestFocus();
        //updateSelection(actualSelection);
        repaintAll();
    }
    
    private void disableImageContext(){
        for ( Action action : getImageContextActions() ){
            action.setEnabled( false );
        }
    }
    
    private void doDrag(DropTargetDragEvent dtde) {
        if ( getDroppedDataObject( dtde) != null) {
            dtde.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    private float[] getSVGPoint( DropTargetDropEvent dtde) {
        Point onTopComponent = dtde.getLocation();
        Point imageZero = getScreenManager().getAnimatorView().getLocation();
        float zoom = getScreenManager().getZoomRatio();
        
        float x = (float)(onTopComponent.getX() - imageZero.getX()) / zoom;
        float y = (float)(onTopComponent.getY() - imageZero.getY()) / zoom;
        
        return new float[]{x, y};
    }
    
    private void doDrop( DropTargetDropEvent dtde) {
        float[] point = getSVGPoint(dtde);
        DataObject dObj;
        if ( (dObj=getDroppedDataObject(dtde)) != null) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            try {
                if ( dropDataObject(dObj, point)) {
                    dtde.dropComplete(true);
                }
            } catch( Exception e) {
                SceneManager.error("Could not obtain dropped data.", e); //NOI18N
                dtde.dropComplete(false);
            }
        } else {
            dtde.rejectDrop();
        }
    }
    
    /**
     * drops data object into specified position.
     * @param dObj DataObject top drop
     * @param point svg coordinates of point wher to drop DataObject. 
     * Coordinates are expected in {x,y} format.
     * @return if drop was performed successfully
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws org.netbeans.modules.editor.structure.api.DocumentModelException
     * @throws javax.swing.text.BadLocationException
     */
    public boolean dropDataObject( DataObject dObj, float[] point) 
            throws IOException, SAXException, DocumentModelException, BadLocationException 
    {
        if ( dObj instanceof XMLDataObject) {
            Document doc = ((XMLDataObject) dObj).getDocument();

            SVGComponentDrop dropSupport = getAEDClass(doc);
            if (dropSupport != null){
                return dropSupport.handleTransfer(m_svgDataObject, point);
            } 
            
            String snippet = getSnippetBody(doc);
            if (snippet != null){
                return SVGComponentDrop.getDefault(snippet)
                        .handleTransfer(m_svgDataObject, point);
            }
            SceneManager.log(Level.SEVERE, "Nothing to drop, empty body and class!"); //NOI18N
            return true;
        } else if ( dObj instanceof SVGPaletteItemDataObject) {
            dropFile( ((SVGPaletteItemDataObject) dObj).getReferencedFile());
            return true;
        } else if (dObj instanceof SVGDataObject) {
            dropFile( FileUtil.toFile( dObj.getPrimaryFile()));
            return true;
        }
        return false;
    }
    
    private String getSnippetBody(Document doc) {
        String snippet = null;
        NodeList bodyTags = doc.getElementsByTagName("body"); //NOI18N

        if (bodyTags.getLength() > 0) {
            snippet = bodyTags.item(0).getTextContent();
        }
        return snippet;
    }
    
    private SVGComponentDrop getAEDClass(Document doc){
            String className = getClassName(doc);
            if (className != null){
                try {
                    Class nameClass = this.getClass().getClassLoader().loadClass(className);
                    if (SVGComponentDrop.class.isAssignableFrom(nameClass)) {
                        SVGComponentDrop impl = (SVGComponentDrop) nameClass.newInstance();
                        return impl;
                    } else {
                        SceneManager.log(Level.SEVERE, "className doesn't implement SVGComponentDrop!"); //NOI18N
                    }
                } catch (Exception ex) {
                    SceneManager.log(Level.SEVERE, "can't create "+className+" instance", ex); //NOI18N
                }
            }
            return null;
    }
    
    private String getClassName(Document doc){
        String name = null;
            NodeList classTags = doc.getElementsByTagName("class"); //NOI18N
            if ( classTags.getLength() > 0) {
                Node classNode = classTags.item(0);
                if (classNode.hasAttributes()){
                    NamedNodeMap attrs = classNode.getAttributes();
                    Node nameNode = attrs.getNamedItem("name");
                    if (nameNode != null){
                        name = nameNode.getNodeValue();
                    }
                }
            }
        return name;
    }
    private void dropFile(File file) throws FileNotFoundException, IOException, DocumentModelException, BadLocationException {
        if ( file != null && file.exists() && file.isFile()) {
            SceneManager.log(Level.INFO, "Dropping file " + file.getPath()); //NOI18N
            String id =m_svgDataObject.getModel().mergeImage(file);
            getSceneManager().setSelection(id, true);
        } else {
            SceneManager.log(Level.SEVERE, "Nothing to drop, file " + file + " not found"); //NOI18N
        }
    }
    
    private DataObject getDroppedDataObject( DropTargetEvent dte) {
        if ( !getSceneManager().isReadOnly()) {
            DataFlavor [] flavors = dte instanceof DropTargetDragEvent ? 
                ((DropTargetDragEvent) dte).getCurrentDataFlavors() :
                ((DropTargetDropEvent) dte).getCurrentDataFlavors();
                
            for (DataFlavor flavor : flavors) {
                if ( DND_PALETTE_MIME.equals( flavor.getSubType())) {
                    try {
                        Transferable transferable = dte instanceof DropTargetDragEvent ? 
                            ((DropTargetDragEvent) dte).getTransferable() :
                            ((DropTargetDropEvent) dte).getTransferable();
                        
                        DataObject dObj = (DataObject) transferable.getTransferData(flavor);

                        if ( dObj instanceof XMLDataObject || 
                             dObj instanceof SVGPaletteItemDataObject || 
                             dObj instanceof SVGDataObject) {
                            return dObj;
                        }
                    } catch( Exception e) {
                        SceneManager.error("Failed to get dropped data object", e); //NOI18N
                    }
                }
            }
        }
        return null;
    }

    public void selectionChanged(SVGObject[] newSelection, SVGObject[] oldSelection, boolean isReadOnly) {
        updateDataTransferActions();
    }

    private void repaintAll() {
        getScreenManager().getAnimatorView().invalidate();
        basePanel.validate();
        basePanel.repaint();
    }

    private SVGFileModel getModel() {
        return m_svgDataObject.getModel();
    }

    private static Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }
    
    /**
     * Loading panel
     */
    private static class LoadPanel extends JPanel {
        private static final long serialVersionUID = 5862679852552354L;
        LoadPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            JLabel loadingLabel = new JLabel(NbBundle.getMessage(SVGViewTopComponent.class, "MSG_Loading")); //NOI18N
            loadingLabel.setBackground(Color.WHITE);
            Font font = loadingLabel.getFont();
            loadingLabel.setFont(font.deriveFont(20.0f));
            add(loadingLabel, BorderLayout.CENTER);
        }
    }

/**
     * Simple proxy node for selected SVG Elements
     */
    private class SVGElementNode extends AbstractNode {

        SVGElementNode(Lookup lookup) {
            super(Children.LEAF, lookup);
        }

        protected Class[] cookieClasses() {
            return new Class[]{SVGObject.class};
        }

        @Override
        public Action[] getActions(boolean context) {
            return new SystemAction[]{SystemAction.get(SaveElementAsImage.class)};
        }
    }

    private class ButtonMouseListener extends MouseUtils.PopupMouseAdapter {
        @Override
        public void mouseEntered(MouseEvent evt) {
            if (evt.getSource() instanceof JButton) {
                JButton button = (JButton) evt.getSource();
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }
//            AbstractButton b = (AbstractButton)e.getComponent();
//            b.getModel().setRollover(true);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            if (evt.getSource() instanceof JButton) {
                JButton button = (JButton) evt.getSource();
                if (button.isEnabled()) {
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                }
            }
//            AbstractButton b = (AbstractButton)e.getComponent();
//            b.getModel().setRollover(false);
        }

        protected void showPopup(MouseEvent evt) {
        }
    }

    private class ToggleScaleAction extends AbstractSVGAction {
        private static final long serialVersionUID = 5862679852552354L;

        private float m_previousZoomRatio;

        ToggleScaleAction() {
            super("svg_toggle_scale"); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = getScreenManager();

            doScale = !doScale;
            if (doScale) {
                m_previousZoomRatio = smgr.getZoomRatio();

                String activeConfiguration = null;
                final FileObject primaryFile = m_svgDataObject.getPrimaryFile();
                Project p = FileOwnerQuery.getOwner(primaryFile);
                if (p != null && p instanceof J2MEProject) {
                    J2MEProject project = (J2MEProject) p;
                    activeConfiguration = project.getConfigurationHelper().getActiveConfiguration().getDisplayName();
                }
                Dimension dim = ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, activeConfiguration);
                Rectangle imgBounds = smgr.getImageBounds();

                float ratio = (float) (dim.getHeight() / imgBounds.getHeight());
                smgr.setZoomRatio(ratio * m_previousZoomRatio);
            } else {
                smgr.setZoomRatio(m_previousZoomRatio);
            }
            scaleToggleButton.setSelected(doScale);
            zoomInAction.setEnabled(!doScale);
            zoomOutAction.setEnabled(!doScale);
            zoomToFitAction.setEnabled(!doScale);
            zoomComboBox.setEnabled(!doScale);
            repaint();
        }
    }

    private class ToggleShowViewBoxAction extends AbstractSVGAction implements Presenter.Popup {
        private static final long serialVersionUID = 5862679852552354L;

        ToggleShowViewBoxAction() {
            super("svg_toggle_view"); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = getScreenManager();
            boolean b = !smgr.getShowAllArea();
            smgr.setShowAllArea(b);
            showViewBoxToggleButton.setSelected(b);
            repaint();
        }
    }

    private class ZoomToFitAction extends AbstractSVGAction {
        private static final long serialVersionUID = 5862679852552354L;

        ZoomToFitAction() {
            super("svg_zoom_fit"); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = getScreenManager();
            Rectangle imgBounds = smgr.getImageBounds();
            Rectangle panelBounds = smgr.getComponent().getBounds();

            float zoomRatio = Math.min((float) (panelBounds.width - 2 * SVGImagePanel.CROSS_SIZE) / imgBounds.width, (float) (panelBounds.height - 2 * SVGImagePanel.CROSS_SIZE) / imgBounds.height);
            smgr.setZoomRatio(zoomRatio * smgr.getZoomRatio());
            updateZoomCombo();
        }
    }

    private class ZoomInAction extends AbstractSVGAction {
        private static final long serialVersionUID = 5862679852552354L;

        ZoomInAction() {
            super("svg_zoom_in"); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = getScreenManager();
            smgr.setZoomRatio(smgr.getZoomRatio() * ZOOM_STEP);
            updateZoomCombo();
        }
    }

    private class ZoomOutAction extends AbstractSVGAction {
        private static final long serialVersionUID = 5862679852552354L;

        ZoomOutAction() {
            super("svg_zoom_out"); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            ScreenManager smgr = getScreenManager();
            smgr.setZoomRatio(smgr.getZoomRatio() / ZOOM_STEP);
            updateZoomCombo();
        }
    }

    private static void enableComponentsInToolbar(Container component, boolean enable, Component... skip) {
        main_loop:
        for (Component comp : component.getComponents()) {
            if (skip != null) {
                for (Component skipped : skip) {
                    if (skipped == comp) {
                        continue main_loop;
                    }
                }
            }
            comp.setEnabled(enable);
            enableComponentsInToolbar((Container) comp, enable);
        }
    }

    private class SVGCookie implements SelectionCookie, AnimationCookie {

        public void startAnimation(final SVGDataObject doj, final String id) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    startAnimationAction.actionPerformed(null);
                    getPerseusController().startAnimation(id);
                }
            });
        }

        public void stopAnimation(final SVGDataObject doj, final String id) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getPerseusController().stopAnimation(id);
                }
            });
        }

        public void updateSelection(final SVGDataObject doj, final String id, int startOff, boolean doubleClick) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getSceneManager().setSelection(id, false);
                }
            });
        }
    }
}