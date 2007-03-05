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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.ui.tnv.scrollpane;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import org.netbeans.modules.soa.ui.tnv.api.ThumbnailPaintable;
import org.netbeans.modules.soa.ui.tnv.api.ThumbnailView;
import org.netbeans.modules.soa.ui.tnv.impl.ThumbnailViewImpl;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class ThumbnailScrollPane extends JScrollPane {
    
    private JToggleButton btnShowTNV;
    private ThumbnailView myTNView;
    
    private boolean isTnvVisible = false;
    private boolean isTnvEnabled = true;
    
    private TnvLocation myTnvLocation = TnvLocation.LOWER_RIGHT_CORNER; // default value;
    private int myTnvIndent = 0; // distance in pixels between the TNV and the Scroll pane border
    
    private transient boolean ignoreMsg = false;
    
    public ThumbnailScrollPane() {
        super();
        createContent();
    }
    
    public ThumbnailScrollPane(JComponent comp) {
        super(comp);
        createContent();
    }
    
    private void createContent() {
        this.setLayout(new ThumbnailScrollLayout());
        //
        btnShowTNV = new JToggleButton();
        setCorner(ScrollPaneConstants.LOWER_TRAILING_CORNER, btnShowTNV);
        btnShowTNV.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (!ignoreMsg) {
                    if (event.getStateChange() == ItemEvent.SELECTED) {
                        setTnvVisible(true, event.getSource());
                    }
                    if (event.getStateChange() == ItemEvent.DESELECTED) {
                        setTnvVisible(false, event.getSource());
                    }
                }
            }
        });
        //
        setTnvVisible(false, null);
        //
        ClassLoader loader = ThumbnailScrollPane.class.getClassLoader();
        URL url = loader.getResource("org/netbeans/modules/soa/ui/tnv/scrollpane/thumbnail_view.png"); // NOI18N
        if (url != null) {
            Image img =  Toolkit.getDefaultToolkit().createImage(url);
            ImageIcon icon = new ImageIcon(img);
            btnShowTNV.setIcon(icon);
        }
        String tooltip = NbBundle.getMessage(
                ThumbnailView.class, "TOOLTIP_TNV_BUTTON");
        btnShowTNV.setToolTipText(tooltip);
        btnShowTNV.setFocusable(false);
        //
        Component viewableComp = getViewport().getView();
        if (!(viewableComp instanceof ThumbnailPaintable)) {
            // Only components which implements the ThumbnailPaintable are allowed
            setTnvEnabled(false, null);
        }
    }
    
    public void setTnvVisible(boolean newValue, Object source) {
        if (newValue != isTnvVisible) {
            boolean tnvWasVisible = isTnvVisible && isTnvEnabled;
            //
            isTnvVisible = newValue;
            //
            if (isTnvVisible && isTnvEnabled) {
                getThumbnailView().getUIComponent().setVisible(true);
            } else {
                if (myTNView != null) {
                    myTNView.getUIComponent().setVisible(false);
                }
            }
            //
            //
            ignoreMsg = true;
            try {
                if (source == null || source != btnShowTNV) {
                    btnShowTNV.setSelected(isTnvVisible && isTnvEnabled);
                }
            } finally {
                ignoreMsg = false;
            }
            //
            if (tnvWasVisible != (isTnvVisible && isTnvEnabled)) {
                revalidate();
                repaint();
            }
        }
    }
    
    public void setTnvEnabled(boolean newValue, Object source) {
        if (isTnvEnabled != newValue) {
            boolean tnvWasVisible = isTnvVisible && isTnvEnabled;
            //
            isTnvEnabled = newValue;
            //
            btnShowTNV.setEnabled(isTnvEnabled);
            //
            ignoreMsg = true;
            try {
                btnShowTNV.setSelected(isTnvVisible && isTnvEnabled);
            } finally {
                ignoreMsg = false;
            }
            //
            if (myTNView != null) {
                myTNView.getUIComponent().setVisible(isTnvVisible && isTnvEnabled);
            }
            //
            if (tnvWasVisible != (isTnvVisible && isTnvEnabled)) {
                revalidate();
                repaint();
            }
        }
    }
    
    public boolean isTnvEnabled() {
        return isTnvEnabled;
    }
    
    public boolean isTnvVisible() {
        return isTnvVisible;
    }
    
    protected ThumbnailView createDefaultThumbnailView() {
        ThumbnailView tnView = new ThumbnailViewImpl();
        tnView.getUIComponent().setPreferredSize(new Dimension(100, 100));
        return tnView;
    }
    
    public synchronized void setThumbnailView(ThumbnailView newValue) {
        if (myTNView != null) {
            myTNView.setScrollPane(null);
            JComponent thViewComp = myTNView.getUIComponent();
            remove(thViewComp);
        }
        //
        myTNView = newValue;
        //
        if (myTNView != null) {
            myTNView.setScrollPane(this);
            JComponent thViewComp = myTNView.getUIComponent();
            add(thViewComp, 0);
        }
        //
        revalidate();
        repaint();
    }
    
    public synchronized ThumbnailView getThumbnailView() {
        if (myTNView == null) {
            ThumbnailView newTNView = createDefaultThumbnailView();
            setThumbnailView(newTNView);
        }
        return myTNView;
    }
    
    public boolean isOptimizedDrawingEnabled() {
        if (isTnvVisible()) {
            return false;
        } else {
            return super.isOptimizedDrawingEnabled();
        }
    }
    
    public void setTnvLocation(TnvLocation newLocation) {
        if (myTnvLocation != newLocation) {
            myTnvLocation = newLocation;
            revalidate();
            repaint();
        }
    }
    
    public TnvLocation getTnvLocation() {
        return myTnvLocation;
    }
    
    public void setTnvIndent(int newIndent) {
        if (myTnvIndent != newIndent) {
            myTnvIndent = newIndent;
            revalidate();
            repaint();
        }
    }
    
    public int getTnvIndent() {
        return myTnvIndent;
    }
    
    public void setComponentOrientation(ComponentOrientation co) {
        //
        // Set default location depend on the orientation;
        if (getTnvLocation() == null) {
            if (ComponentOrientation.RIGHT_TO_LEFT.equals(co)) {
                setTnvLocation(TnvLocation.LOWER_LEFT_CORNER);
            } else {
                setTnvLocation(TnvLocation.LOWER_RIGHT_CORNER);
            }
        }
        //
        super.setComponentOrientation(co);
    }
    
    public static enum TnvLocation {
        LOWER_LEFT_CORNER,
        LOWER_RIGHT_CORNER,
        UPPER_LEFT_CORNER,
        UPPER_RIGHT_CORNER;
    }
    
}
