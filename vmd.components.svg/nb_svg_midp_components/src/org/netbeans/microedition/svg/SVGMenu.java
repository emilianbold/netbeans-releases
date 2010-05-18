/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * SvgMenuAnotherStyle.java
 *
 * Created on April 10, 2006, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.microedition.svg;

import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;

/**
 * Screen representing a menu component. SVG animation can contain named elements, 
 * which are being focused while the users moves up/down. In the case SVG image
 * contains animation on the focus event, the user can see the animated transition
 * between the named elements.
 *
 * <p/> By default this component starts the animation automatically and runs in 
 * full screen mode.
 *
 * @author breh
 */
public class SVGMenu extends SVGAnimatorWrapper {
    
    
    private static final String FOCUSINANIM_PREFFIX = "focusInAnim_";
    private static final String FOCUSOUTANIM_PREFIX = "focusOutAnim_";
    
    /**
     * Command fired when the user chooses a menu element.
     */
    public static final Command SELECT_COMMAND = new Command("SELECT_COMMAND",Command.ITEM,0); // NOI18N
    
    /**
     * value of selected index when no menu item is selected
     */
    public static final int SELECTED_INDEX_NONE = -1;
    
    
    private static final int NO_FOCUS = -1;
    
    private Vector menuElements;
    private Document svgDocument;
    private int currentFocus = NO_FOCUS;
    private Display display;
    
    
    // menu element structure
    private static class MenuElement {
        SVGElement focusableElement;
        int gameActionId;
        SVGAnimationElement focusInAnimation;
        SVGAnimationElement focusOutAnimation;
    }
    
    /**
     * Creates a new instance of SVGMenu. It requires SVGImage to be displayed as
     * menu and display.
     * 
     * <p/> Please note, supplied SVGImage shouldn't be reused in other SVGAnimator.
     */
    public SVGMenu(SVGImage svgImage, Display display) {
        super(svgImage, display);
        this.svgDocument = svgImage.getDocument();
        this.menuElements = new Vector();
        this.display = display;
        setResetAnimationWhenStopped(false); // animation cannot be reset automatically        
        setSVGEventListener(new SvgMenuEventListener()); // set menu listener        
        setFullScreenMode(true); // menu is usually full screen
    }
    
    
    /**
     * Adds a menu element to the menu component. The menu element is identified
     * by SVG element ID, so when the menu element is being selected, the
     * element with given ID is being focused.
     */
    public void addMenuElement(String svgElementId) {
        this.addMenuElement(svgElementId,Canvas.FIRE);
    }
    
    
    /**
     * Gets menu element id for given index.
     * @return string element ID
     * @throws IndexOutOfBoundException when a wrong index is used
     */
    public String getMenuElementID(int index) throws IndexOutOfBoundsException {
       if ((index < 0) || (index >= menuElements.size())) throw new IndexOutOfBoundsException("Wrong index for menu element: "+index);
       return ((MenuElement)menuElements.elementAt(index)).focusableElement.getId();
    }
    
    
    /**
     * Implementation of addding the menu element
     */
    private void addMenuElement(String svgElementId, int gameActionId) {
        MenuElement element = new MenuElement();
        element.focusableElement = (SVGElement) svgDocument.getElementById(svgElementId);
        element.gameActionId = gameActionId;
        // now try to find animation elements in by our patterns (Nokia S40 workaround)
        // focus.in animation replacement
        Element animElement = svgDocument.getElementById(FOCUSINANIM_PREFFIX + svgElementId);
        if (animElement instanceof SVGAnimationElement) {
            element.focusInAnimation = (SVGAnimationElement)animElement;
        }
        // focus.out animation replacement
        animElement = svgDocument.getElementById(FOCUSOUTANIM_PREFIX + svgElementId);
        if (animElement instanceof SVGAnimationElement) {
            element.focusOutAnimation = (SVGAnimationElement)animElement;
        }        
        menuElements.addElement(element);
        resetFocus();
    }
    
    /**
     * Gets index of selected menu element
     */
    public int getSelectedIndex() {
        return currentFocus;
    }
    
    
    // we should also think about removeMenuElement, but this is not necessary for Visual Designer
    
    
    private void focusOn(int currentFocus, int nextFocus) {
        //System.out.println("current="+currentFocus+", next="+nextFocus);
        MenuElement currentElement = getMenuElementAt(currentFocus);
        MenuElement nextElement = getMenuElementAt(nextFocus);
        if (currentElement != null) {
            // if it has focus out animation replacement
            if (currentElement.focusInAnimation != null) {
                stopAnimation(currentElement.focusInAnimation);
                //System.out.println("Stopping focusIn replacement for current");
            }
            if (currentElement.focusOutAnimation != null) {
                startAnimation(currentElement.focusOutAnimation);
                //System.out.println("Dispatching focusOut replacement for current");
            }
        }
        if (nextElement != null) {            
            if (nextElement.focusOutAnimation != null) {
                stopAnimation(nextElement.focusOutAnimation);
                //System.out.println("Stopping focusOut replacement for cunextrrent");
            }
            if (nextElement.focusInAnimation != null) {
                // if it has focus in animation replacement
                // remove current focus
                getSVGImage().focusOn(null); 
                // start focus animation replacement
                startAnimation(nextElement.focusInAnimation);
                //System.out.println("Dispatching focusIn replacement for next");
            } else {
                // else use delvering focus on element
                getSVGImage().focusOn(nextElement.focusableElement);
                //System.out.println("Dispatching regular focusIn");
            }
        } else {
            // we have a problem houston -- remove focus
            getSVGImage().focusOn(null);
            //System.out.println("Dispatching regular focusIn on null");
        }
    }
    
    
    
    private MenuElement getMenuElementAt(int index) {
        if ((index >= 0) && (index < menuElements.size())) {
            return (MenuElement)menuElements.elementAt(index);
        } else {
            return null;
        }
    }
    
    
    private void startAnimation(final SVGAnimationElement svgAnimationElement) {
        Runnable runnable = new Runnable() {
           public void run() {
               svgAnimationElement.beginElementAt(0f);
           }
        };
        invokeLaterSafely(runnable);
    }
    
    private void stopAnimation(final SVGAnimationElement svgAnimationElement) {
        Runnable runnable = new Runnable() {
           public void run() {
               svgAnimationElement.endElementAt(0f);
           }
        };
        invokeLaterSafely(runnable);
    }
    
    
    /**
     * focus on the first element if applicable;
     */
    public void resetFocus() {
        focusOn(currentFocus,0);
        currentFocus = 0;
    }
    
    
    /**
     * By default, focusNext simply moves the focus on the
     * next focusable element in the focus ring.
     */
    public void focusNext() {
        int nextFocus = currentFocus;
        if (nextFocus + 1 != menuElements.size()) {
            nextFocus++;
        } else {
            nextFocus = 0;
        }
        focusOn(currentFocus,nextFocus);
        currentFocus = nextFocus;
    }
    
    /**
     * By default, focusNext simply moves the focus on the
     * previous focusable element in the focus ring.
     */
    public void focusPrev() {
        int nextFocus = currentFocus;
        if (nextFocus == 0) {
            nextFocus = menuElements.size();
        }
        nextFocus--;
        focusOn(currentFocus,nextFocus);
        currentFocus = nextFocus;
    }
    
    
    
    
    // Listen to MIDP key events
    // - UP/LEFT    -> Focus on previous item.
    // - DOWN/RIGHT -> Focus on next item.
    private class SvgMenuEventListener implements  SVGEventListener {
        public void keyPressed(int keyCode) {
            int gameAction = getSvgCanvas().getGameAction(keyCode);
            if (gameAction == Canvas.UP
                    ||
                    gameAction == Canvas.LEFT) {
                getAnimator().invokeLater(new Runnable() {
                    public void run() {
                        focusPrev();
                    }
                });
            } else if (gameAction == Canvas.DOWN
                    ||
                    gameAction == Canvas.RIGHT) {
                getAnimator().invokeLater(new Runnable() {
                    public void run() {
                        focusNext();
                    }
                });
            }
            
            MenuElement currentMenuElement = getMenuElementAt(currentFocus);
            if (currentMenuElement != null) {
                if (gameAction == currentMenuElement.gameActionId){
                    // fire the action
                    fireSelectCommandAction();
                }
                // else do nothing !!!                
            } else {
                //System.out.println("SvgMenuEventListener.keyPressed: Houston we have a problem : currentMenuElement == null !!!");
            }
        }
        
        
        private void fireSelectCommandAction() {
            final CommandListener commandListener = SVGMenu.this.getCommandListener();
            if (commandListener != null) {
                commandListener.commandAction(SVGMenu.SELECT_COMMAND,SVGMenu.this);
            }
        }
        
        public void keyReleased(int keyCode) {
        }
        
        public void pointerPressed(int x, int y) {
        }
        
        public void pointerReleased(int x, int y) {
        }
        
        public void hideNotify() {
        }
        
        public void showNotify() {
        }
        
        public void sizeChanged(int width, int height) {
        }
    }
    
   
}
