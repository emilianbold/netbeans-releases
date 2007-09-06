/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.util.SVGConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyListener;
import java.util.Stack;
import javax.microedition.m2g.SVGImage;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGImagePanel;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGStatusBar;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author Pavel Benes
 */
public final class ScreenManager {
    private static final Image PEN_ICON            = org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/pen.png"); // NOI18N        
    private static final Color VIEWBOXBORDER_COLOR = Color.DARK_GRAY;

    private static final float MINIMUM_ZOOM = 0.01f;
    private static final float MAXIMUM_ZOOM = 100f;
    
    private final SceneManager   m_sceneMgr;
    private final SVGStatusBar   m_statusBar;
    private       JScrollPane    m_topComponent;
    private       JComponent     m_animatorView;
    private       SVGImagePanel  m_imageContainer;
    private       Cursor         m_cursor;    
    private       boolean        m_showAllArea;
    private       boolean        m_showTooltip;
    private       boolean        m_highlightObject;
    private       short          m_changeTicker = 0;
    
    ScreenManager(SceneManager sceneMgr) {
        m_sceneMgr        = sceneMgr;
        m_statusBar       = new SVGStatusBar();
        m_showAllArea     = false;
        m_showTooltip     = true;
        m_highlightObject = true;
    }
    
    void initialize() {
        PerseusController perseus = m_sceneMgr.getPerseusController();
        m_animatorView = perseus.getAnimatorGUI();
                       
        m_imageContainer = new SVGImagePanel(m_animatorView) {
            protected void paintPanel(Graphics g, int x, int y, int w, int h) {
                PerseusController perseus = m_sceneMgr.getPerseusController();
                if (perseus != null) {
                    Shape clip = g.getClip();
                    try {
                        g.setClip(x, y, w, h);
                        if (m_showAllArea) {
                            SVGLocatableElement elem = perseus.getViewBoxMarker();
                            if (elem != null) {
                                SVGRect rect = elem.getScreenBBox();
                                g.setColor( VIEWBOXBORDER_COLOR);
                                g.drawRect((int)(x + rect.getX()), (int)(y + rect.getY()),
                                           (int)(rect.getWidth()), (int)(rect.getHeight()) - 1);
                            }
                        }

                        boolean isReadOnly = m_sceneMgr.isReadOnly();
                        Stack<ComposerAction> actions = m_sceneMgr.getActiveActions();
                        if (actions != null) {
                            for (int i = actions.size()-1; i >= 0; i--) {
                                actions.get(i).paint(g, x, y, isReadOnly);
                            }
                        }

                        if (!isReadOnly) {
                            x += 1;
                            y += h - PEN_ICON.getHeight(null) - 1;
                            g.drawImage(PEN_ICON, x, y, null);
                        }
                    } finally {
                        g.setClip(clip);
                    }
                }
            }
        };
        m_topComponent = new JScrollPane(m_imageContainer);
        incrementChangeTicker();
    }
    
    public void reset() {
        m_topComponent    = null;
        m_animatorView    = null;
        m_imageContainer  = null;
        m_cursor          = null;    
    }
    
    public SVGStatusBar getStatusBar() {
        return m_statusBar;
    }
    
    public void registerMouseController( InputControlManager.MouseController mouseListener) {
        m_animatorView.addMouseListener(mouseListener);
        m_animatorView.addMouseMotionListener(mouseListener);
        m_imageContainer.addMouseListener(mouseListener);
    }

    public void registerKeyController( KeyListener keyListener) {
        m_animatorView.addKeyListener(keyListener);
    }
    
    public void registerPopupMenu( final Action [] popupActions, final Lookup lookup) {
        m_topComponent.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                JPopupMenu popup = Utilities.actionsToPopup( popupActions, lookup);                
                popup.show(m_topComponent, e.getX(), e.getY());
            }
        });

        m_animatorView.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                m_sceneMgr.popupAt(e.getX(), e.getY());
                JPopupMenu popup = Utilities.actionsToPopup( popupActions, lookup);                
                popup.show(m_animatorView, e.getX(), e.getY());
            }
        });        
    }
    
    
    public JComponent getComponent() {
        return m_topComponent;
    }

    public JComponent getAnimatorView() {
        return m_animatorView;
    }
    
    public Rectangle getImageBounds() {
        return m_animatorView.getBounds();
    }
    
    public void repaint(int x, int y, int w, int h) {
        m_animatorView.repaint(x, y, w, h);
    }
    
    public void repaint(Rectangle rect) {
        m_animatorView.repaint(rect);
    }

    public void repaint(Rectangle rect, int overlap) {
        m_animatorView.repaint(rect.x - overlap, rect.y - overlap,
                               rect.width + 2 * overlap, rect.height + 2 * overlap);
    }
    
    public void setCursor(Cursor cursor) {
        if (m_cursor != cursor) {
            m_animatorView.setCursor( cursor);
            m_cursor = cursor;
        }
    }
        
    public void setShowAllArea(boolean showAllArea) {
        if (showAllArea != m_showAllArea) {
            m_showAllArea = showAllArea;
            refresh();
            incrementChangeTicker();
        }
    }

    public boolean getShowAllArea() {
        return m_showAllArea;
    }
    
    public void setShowTooltip(boolean showTooltip) {
        m_showTooltip = showTooltip;
    }
    
    public boolean getShowTooltip() {
        return m_showTooltip;
    }

    public void setHighlightObject(boolean highlightObject) {
        m_highlightObject = highlightObject;
    }
    
    public boolean getHighlightObject() {
        return m_highlightObject;
    }
    
    public float getZoomRatio() {
        return m_sceneMgr.m_zoomRatio;
    }
    
    public void setZoomRatio(float zoomRatio) {
        if (zoomRatio < MINIMUM_ZOOM) {
            zoomRatio = MINIMUM_ZOOM;
        } else if (zoomRatio > MAXIMUM_ZOOM) {
            zoomRatio = MAXIMUM_ZOOM;
        }
        
        if ( zoomRatio != m_sceneMgr.m_zoomRatio) {
            m_sceneMgr.m_zoomRatio = zoomRatio;
            refresh();
            incrementChangeTicker();
        }
    } 
    
    public void repaint() {
        //TODO FIX: NPE when playing with window cloning
        m_animatorView.invalidate();
        m_topComponent.validate(); 
        m_animatorView.repaint();
        m_topComponent.repaint();
    }
    
    public void refresh() {
        SVGSVGElement svg        = m_sceneMgr.getPerseusController().getSVGRootElement();                
        SVGRect   viewBoxRect    = svg.getRectTrait(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
        SVGPoint  translatePoint = svg.getCurrentTranslate();
        SVGRect   rect           = null;
        Dimension size;

        // show all area in case the view box is not defined
        boolean showAll = viewBoxRect == null ? true : m_showAllArea;
        
        //System.out.println("ViewBox: " + viewBoxRect);
        //System.out.println("TotalBox: " + svg.getBBox());
        //System.out.println("TotalScreenBBox: " + svg.getScreenBBox());

        translatePoint.setX(0);
        translatePoint.setY(0);
        svg.setCurrentScale(1.0f);
        
        rect = showAll ? svg.getBBox() : viewBoxRect;
        if (rect != null) {
            size = new Dimension((int) (rect.getWidth() * m_sceneMgr.m_zoomRatio),
                                 (int) (rect.getHeight() * m_sceneMgr.m_zoomRatio));
            SVGImage svgImage = m_sceneMgr.getSVGImage();
            svgImage.setViewportWidth(size.width); 
            svgImage.setViewportHeight(size.height);        

            if ( showAll) {
                if (viewBoxRect != null) {
                    double xRatio = viewBoxRect.getWidth() / rect.getWidth();
                    double yRatio = viewBoxRect.getHeight() / rect.getHeight();
                    float  ratio  = (float) Math.max(xRatio, yRatio);
                    //System.out.println("Scale ratio: " + ratio);
                    svg.setCurrentScale( ratio);
                }

                SVGRect screenBBox = svg.getScreenBBox();
                //System.out.println("Actual TotalScreenBBox: " + screenBBox);

                translatePoint.setX(-screenBBox.getX());
                translatePoint.setY(-screenBBox.getY());            
            }
        } else {
            size = new Dimension(100, 100);
        }
        m_animatorView.setSize(size);
        repaint();
    }
    
    public short getChangeTicker() {
        return m_changeTicker;
    }
    
    public void incrementChangeTicker() {
        if (++m_changeTicker < 0) {
            m_changeTicker = 0;
        }
    }
}
