/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * A view tabs ui for OS-X adapted from the view tabs UI for Metal.
 *
 * @author Tim Boudreau
 */
public final class AquaViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /**
     * *********** constants ******************
     */
    private static final int TXT_X_PAD = 5;

    private static final int ICON_X_LEFT_PAD = 5;
    private static final int ICON_X_RIGHT_PAD = 2;

    private static final int BUMP_X_PAD = 5;
    private static final int BUMP_Y_PAD = 4;
    
    /********* static fields ***********/
    
    /**
     * ******* instance fields *********
     */

    private Dimension prefSize;
    /**
     * Reusable Rectangle to optimize rectangle creation/garbage collection
     * during paints
     */
    private Rectangle tempRect = new Rectangle();

    /**
     * Should be constructed only from createUI method.
     */
    private AquaViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19); //XXX huh?
    }

    public static ComponentUI createUI(JComponent c) {
        return new AquaViewTabDisplayerUI((TabDisplayer) c);
    }

    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }
    
    protected void installPinButton() {
        //do nothing
    }    

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                21 : fm.getAscent() + 2 * fm.getDescent() + 3;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    public void paint(Graphics g, JComponent c) {
        ColorUtil.setupAntialiasing(g);
        super.paint(g, c);
        paintBottomBorder(g, c);
    }
    
    protected Font getTxtFont() {
        return getDisplayer().getFont();
    }    

    /**
     * Paints bottom "activation" line
     */
    private void paintBottomBorder(Graphics g, JComponent c) {

    }

    private Rectangle pinButtonRect = new Rectangle();
    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();

        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int textW = width;

        if (isContainsMouse()) {
            int iconWidth = 5;
            int iconHeight = 5;

            int iconY;
            int iconX;
            JButton pinButton = configurePinButton(index);
            int gap = 2;
            
            if (displayer.isShowCloseButton()) {
                if (iconHeight > height) {
                    //It's too tall, try to center it
                    iconY = -1 * ((iconHeight - height) / 2);
                } else {
                    iconY = (height / 2) - (iconHeight / 2);
                }

                iconX = x + width - (iconWidth + gap);

                if (index == getDataModel().size() - 1) {
                    iconX -= 3;
                    textW -= 3;
                }

                g.setColor(isSelected(index) && isActive() ?
                           new Color(80, 80, 123) : new Color(110, 120, 120));
                iconY -= 1; //Only if we're painting, not using a bitmap
                iconX -= 2; //Only if we're painting, not using a bitmap

                g.drawLine(iconX, iconY, iconX + iconWidth, iconY + iconHeight);
                g.drawLine(iconX, iconY + iconHeight, iconX + iconWidth, iconY);

                iconY++;
                g.drawLine(iconX, iconY, iconX + iconWidth, iconY + iconHeight);
                g.drawLine(iconX, iconY + iconHeight, iconX + iconWidth, iconY);
            } else {
                iconWidth = 0;
                iconX = x + width - gap;
                if (iconHeight > height) {
                    //It's too tall, try to center it
                    iconY = -1 * ((iconHeight - height) / 2);
                } else {
                    iconY = ((height / 2) - (iconHeight / 2)) - 1;
                }
                tempRect.x = iconX;
            }

            // pin button
            int space4Pin = pinButton != null ? pinButton.getWidth() + 1 : 0;
            if (pinButton != null && isSelected(index)) {
//                pinButton.setLocation(iconX - space4Pin, iconY - 1);
                
                //Ugly for now - really we should get rid of the button 
                //entirely
                
                // don't activate and draw pin button if tab is too narrow
                if (tempRect.x - space4Pin <= x) {
                    pinButtonRect.setBounds(0, 0, 0, 0);
                } else {
                    Icon ic = pinButton.getIcon();
                    pinButtonRect.setBounds(iconX - space4Pin, iconY - 4, 
                        ic.getIconWidth(), ic.getIconHeight());

                    if (ic != null) {
                        ic.paintIcon(displayer, g, pinButtonRect.x, pinButtonRect.y);
                    }
                }
                

            }

            textW = textW - (iconWidth + 7 + space4Pin);
        }

        if (text.length() == 0) {
            return;
        }

        int textHeight = fm.getHeight();
        int textY;
        int textX = x + 5;
        textW = textW -  5;

        if (textHeight > height) {
            textY = (-1 * ((textHeight - height) / 2)) + fm.getAscent()
                    - 1;
        } else {
            textY = (height / 2) - (textHeight / 2) + fm.getAscent();
        }

        HtmlRenderer.renderString(text, g, textX, textY, textW, height, getTxtFont(),
                          UIManager.getColor("textText"),
                          HtmlRenderer.STYLE_TRUNCATE, true);
    }
    
    //private static final JButton jb = new JButton();
    
    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {

    }

    private static final ChicletWrapper chiclet = new ChicletWrapper();

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        boolean first = index == 0;
        boolean last = index == getDataModel().size() - 1;
        int state = 0;
        if (isActive()) {
            state |= GenericGlowingChiclet.STATE_ACTIVE;
        }
        if (isSelected(index)) {
            state |= GenericGlowingChiclet.STATE_SELECTED;
        }
        if (isAttention(index)) {
            state |= GenericGlowingChiclet.STATE_ATTENTION;
        }
        
        y+=1; //align with top of editor tabs
        
        chiclet.setState(state);
        chiclet.setBounds(x, y, width, height);
        chiclet.setArcs(first ? 0.5f : 0f, last ? 0.5f : 0f,
                         first ? 0.0f : 0f, last ? 0.0f : 0f);
        chiclet.setNotch(false, false);
        g.translate (x, y);
        chiclet.draw((Graphics2D) g);
        g.translate (-x, -y);
    }


    /**
     * Computes rectangle occupied by close icon and fill values in given
     * rectangle.
     */
    private Rectangle getCloseIconRect(Rectangle rect, int index) {
        if (!displayer.isShowCloseButton()) {
            rect.x = -2;
            rect.y = -2;
            rect.width = 0;
            rect.height = 0;
            return rect;
        }
        FontMetrics fm = getTxtFontMetrics();
        String text2Paint = null;
        // setting font already here to compute string width correctly
        int width = getLayoutModel().getW(index);
        int x = getLayoutModel().getX(index);
        int height = getLayoutModel().getH(index);
        int iconWidth = 5;
        int iconHeight = 5;

        int iconY;
        if (iconHeight > height) {
            //It's too tall, try to center it
            iconY = -1 * ((iconHeight - height) / 2);
        } else {
            iconY = (height / 2) - (iconHeight / 2) - 1;
        }

        int gap = 2;

        int iconX = x + width - (iconWidth + gap);
        if (index == getDataModel().size() - 1) {
            iconX -= 3;
        }
        rect.x = iconX;
        rect.y = iconY;
        rect.width = 5;
        rect.height = 5;
        return rect;
    }

    private boolean containsMouse = false;

    private void setContainsMouse(boolean val) {
        if (val != containsMouse) {
            containsMouse = val;
            getDisplayer().repaint();
        }
    }

    private boolean isContainsMouse() {
        return containsMouse;
    }
    
    protected PinButton createPinButton() {
        // XXX - TBD - change to own specialized pin button with proper icons
        return super.createPinButton();
    }
    
    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {
        //TODO - add a method to AbstractViewTabDisplayerUI to get the close button rect and implement everything
        //on the parent class

        protected int inCloseIconRect(Point point) {
            int index = getLayoutModel().indexOfPoint(point.x, point.y);
            if (index < 0) {
                return -1;
            }
            Rectangle rect = getCloseIconRect(tempRect, index);
            rect.width += 6;
            rect.height += 6;
            rect.x -= 3;
            rect.y -= 3;
            int result = rect.contains(point) ? index : -1;
            return result;
        }
        
        protected boolean inPinButtonRect(Point p) {
            return pinButtonRect.contains(p);
        }

        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            int closeRectIdx = inCloseIconRect(p);
            tabState.setPressed(i);
            tabState.setCloseButtonContainsMouse(closeRectIdx);
            tabState.setMousePressedInCloseButton(closeRectIdx);            
            // invoke possible selection change
            if ((i != -1) && closeRectIdx == -1) {
                if (shouldPerformAction (TabDisplayer.COMMAND_SELECT, i, e)) {
                    getSelectionModel().setSelectedIndex(i);
                    tabState.setSelected(i);
                }
            }
            if (shouldReact(e) && closeRectIdx != -1) {
                setClosePressed(closeRectIdx);
                return;
            } 
            
            if (pinButton != null && inPinButtonRect (p)) {
                //XXX better to do on mouse release
                performPinAction();
            }
            if ((i != -1) && e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
                // mkleint need to consume otherwise the evil DefaultTabbedContainerUI.ForwardListener 
                // causes the menu to disappear immediately
                e.consume();
            }
            
            // update pressed state
        }

        public void mouseReleased(MouseEvent e) {
            tabState.setMousePressedInCloseButton(-1);
            tabState.setPressed(-1);
            // close button must not be active when selection change was
            // triggered by mouse press
            if (shouldReact(e)) {
                //Double check that the mouse wasn't pressed over one
                //close button and released over another
                int currClosePressed = isClosePressed();
                setClosePressed(-1);
                Point point = e.getPoint();
                if (inCloseIconRect(point) >= 0) {
                    int i = getLayoutModel().indexOfPoint(point.x, point.y);
                    if (i == currClosePressed) {
                        performAction(e);
                    } 
                    // reset rollover effect after action is complete
                    setMouseInCloseButton(point);
                }
            }
        }

        public void mouseEntered(MouseEvent me) {
            setContainsMouse(true);
            tabState.setMouseInTabsArea(true);
        }

        public void mouseExited(MouseEvent me) {
            setContainsMouse(false);
            tabState.setMouseInTabsArea(false);
        }
    } // end of OwnController

}
