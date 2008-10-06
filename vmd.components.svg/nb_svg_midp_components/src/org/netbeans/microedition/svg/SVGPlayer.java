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

/*
 * SVGPlayer.java
 *
 * Created on June 15, 2007
 *
 */

package org.netbeans.microedition.svg;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Ticker;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 *
 * This class encapsulates SVGAnimator and exposes some of its setters,
 * so it can be used as a component in NetBeans Visual Designer. 
 * It also adds many utility methods which can be used
 * to easily manipulate the underlying document of the animated SVG image.
 *
 * <p>
 * Even though this class extends Canvas, the Canvas is utilized only
 * for forwarding command actions from Canvas created by SVGAnimator.
 * @author breh 
 */
public class SVGPlayer extends Canvas implements CommandListener { // need to use canvas, because of 
                                                                            // forwarding command actions
    
    
    /**
     * Anchor point constants.
     */
    public static final int TOP_LEFT = 0, TOP = 1, TOP_RIGHT = 2, LEFT = 3, CENTER = 4,
            RIGHT = 5, BOTTOM_LEFT = 6, BOTTOM = 7, BOTTOM_RIGHT = 8;
    
    /**
     * Animator is stopped (i.e. when started again, the animation
     * will start from the beginning) 
     */
    public static final int STOPPED = 0;
    
    /**
     * Animator is paused (i.e. when started again, the animation will 
     * continue from the paused state)
     */
    public static final int PAUSED = 1;
    
    /**
     * Animator is running animation
     */    
    public static final int PLAYING = 2;
    
    // svg image to be animated
    private final SVGImage svgImage;
    // the actual canvas created by the animator
    private final Canvas animatorCanvas;
    // SVG animator
    private final SVGAnimator animator;
    // display
    private final Display display;
    
    // command listener used for forwarding actions
    private CommandListener commandListener;
    // user's SVGEventListener 
    private SVGEventListener userSvgEventListener;
    // a user's SVGEventListener to be called in the update thread.
    private SVGEventListener safeSvgEventListener;
    
    // boolean property for starting animation automatically
    private boolean startAnimation;
    // boolean property determing whether animation should be reset when stopped
    // (e.g. when the displayable with animation is dismissed)
    private boolean resetAnimationWhenStopped;
    // property hodling animator state
    private int animatorState = STOPPED;
    // flag determining whether animation needs reset before startinh again (used with compo
    private boolean animationNeedsReset;
    
    // current document update thread, if null, there is no update thread
    private Thread documentUpdateThread;
        
    // The associated document instance.
    private final Document doc;    
    
    // The associated root svg element
    private final SVGSVGElement svg;    
    
    // reset the animation runnable
    private Runnable resetAnimationRunnable = new Runnable() {
            public void run() {
                SVGSVGElement svg = (SVGSVGElement)getSVGImage().getDocument().getDocumentElement();
                svg.setCurrentTime(0f);
            }
    };
    
    
    /** 
     * Creates a new instance of SvgAnimatorHelper. It requires SVGImage to be animated
     * and display.
     *
     * <p/> Please note, supplied SVGImage shouldn't be reused in other SVGAnimator.
     */
    public SVGPlayer(SVGImage svgImage, Display display) throws IllegalArgumentException {
        if (svgImage == null) {
            throw new IllegalArgumentException("svgImage parameter cannot be null");
        }        
        if (display == null) {
            throw new IllegalArgumentException("display parameter cannot be null");
        }
        this.animatorState = STOPPED;
        this.svgImage = svgImage;
        this.display = display;
        this.animator = SVGAnimator.createAnimator(svgImage);
        this.animatorCanvas = (Canvas)animator.getTargetComponent();
        // this sets the size of the image,but we should be somehow able 
        // to track sizeChanged event !!! - this is not possible so far !!!!
        this.svgImage.setViewportWidth(this.animatorCanvas.getWidth());
        this.svgImage.setViewportHeight(this.animatorCanvas.getHeight());
        // sets the command listener to be this component
        this.animatorCanvas.setCommandListener(this);
        // set the svg listener
        this.animator.setSVGEventListener(new WrapperSvgEventListener());
        this.setStartAnimationImmediately(true);
        this.setResetAnimationWhenStopped(true);
        
        // get document and root svg element
        this.doc = svgImage.getDocument();
        this.svg = (SVGSVGElement) doc.getDocumentElement();
    }
    
    
    
    /**
     * Gets Canvas which was created by the SVGAnimator
     */
    public Canvas getSvgCanvas() {
        return animatorCanvas;
    }
    
    public Document getDocument() {
        return doc;
    }
    
    
    /**
     * Gets user's SVGEventListener
     * @return an instance of the user's SVGEventListener or null if there was no user SVGEvenetListener set.
     */
    public SVGEventListener getSVGEventListener() {
        return userSvgEventListener;
    }
    
    /**
     * Sets a user's SVGEventListener to the SVGAnimator
     * @param svgEventListener user SVGEventListener or null if no listener should be set
     */
    public void setSVGEventListener(SVGEventListener svgEventListener) {
        this.userSvgEventListener = svgEventListener;
    }
    
    
    /**
     * Gets user's "Safe" SVGEventListener which is being called in the 
     * document update thread.
     * @return an instance of the user's SVGEventListener being called in the document update thread, 
     * or null if there was no user SVGEvenetListener set.
     */
    public SVGEventListener getSafeSVGEventListener() {
        return userSvgEventListener;
    }    
    
    
    /**
     * Sets a user's SVGEventListener to be called in the document update
     * thread.
     * @param safeSvgEventListener user SVGEventListener or null if no listener should be set
     */
    public void setSafeSVGEventListener(SVGEventListener safeSvgEventListener) {
        this.safeSvgEventListener = safeSvgEventListener;
    }    
    
    
    /**
     * Sets time increment for the animation. Proxy call for
     * SVGAnimator.setTimeIncrement()
     * @param timeIncrement the minimal time that should ellapse between frame rendering. In seconds. Should be greater than zero. 
     * @throws java.lang.IllegalArgumentException - if timeIncrement is less than or equal to zero.
     */
    public void setTimeIncrement(float timeIncrement) {
        animator.setTimeIncrement(timeIncrement);
    }
    
    /**
     * Gets time increment of the animation. Proxy call of SVGAnimator.getTimeIncrement().
     * @return time increment in seconds. 
     */
    public float getTimeIncrement() {
        return animator.getTimeIncrement();
    }
   
    /**
     * Pauses the animation. Proxy call for SVGAnimator.pause()
     */
    public synchronized void pause() {
        getAnimator().pause();
        animatorState = PAUSED;
    }
    
    /**
     * Stops the animation. Proxy call for SVGAnimator.stop()
     */
    public synchronized void stop() {
        getAnimator().stop();
        documentUpdateThread = null;
        animatorState = STOPPED;
    }
    
    
    /**
     * Starts the animation. Proxy call for SVGAnimator.play()
     */
    public synchronized void play()  {
        getAnimator().play();
        // get current document update thread
        try {
            getAnimator().invokeAndWait(new Runnable() {
                public void run() {
                    documentUpdateThread = Thread.currentThread();
                }
            });
        } catch (InterruptedException ex) {
            // this should not happen
        }
        animatorState = PLAYING;
    }
    
    /**
     * reset the animation, so it starts again from the beginning. Can be used in 
     * either when stopped/paused or playing state.
     */
    public void reset() {
        if (animatorState == STOPPED) {
            resetAnimationRunnable.run();
        } else {
            // if playing or paused the reset needs tobe running in update thread
            animator.invokeLater(resetAnimationRunnable);
        }
    }    
    
    /**
     * Returns state of the animation
     * @return STOPPED for stopped state, PAUSED for paused state and PLAYING when
     * the animation is running.
     */
    public synchronized int getAnimatorState() {
        return animatorState;
    }
    
    /**
     * Proxy call for SVGAnimator.invokeAndWait() method.
     * @param runnable Runnable to be passed to SVGAnimator.invokeAndWait(runnable) method.
     * @throws java.lang.InterruptedException 
     */
    public void invokeAndWait(Runnable runnable) throws InterruptedException {
        getAnimator().invokeAndWait(runnable);
    }
    
    /**
     * Proxy call for SVGAnimator.invokeLater() method.
     * @param runnable Runnable to be passed to SVGAnimator.invokeLater(runnable) method.
     */
    public void invokeLater(Runnable runnable) {
        getAnimator().invokeLater(runnable);
    }
    
   /**
     * Gets title from the animation canvas (not <i>this</i> canvas)
     * @return title string
     */
   public String getTitle() {
       return (animatorCanvas != null) ? animatorCanvas.getTitle() : null; 
   }      
   
    /**
     * Sets title to the animation canvas (not <i>this</i> canvas)
     * @param s title String
     */
    public void setTitle(String s) {
        animatorCanvas.setTitle(s);
    }

    /**
     * Adds command to the animation canvas (not <i>this</i> canvas)
     */
    public void addCommand(Command cmd) {
        animatorCanvas.addCommand(cmd);
    }

    /**
     * Removes command from the animation canvas (not <i>this</i> canvas)
     */
    public void removeCommand(Command cmd) {
        animatorCanvas.removeCommand(cmd);
    }

    /** 
     * Gets ticker from the animation canvas (not <i>this</i> canvas)
     */
    public Ticker getTicker() {
        return (animatorCanvas != null) ? animatorCanvas.getTicker() : null; 
    }
    
    /**
     * Sets ticker to the animation canvas (not <i>this</i> canvas)
     */
    public void setTicker(Ticker ticker) {
        animatorCanvas.setTicker(ticker);
    }

    /**
     * sets fullscreen mode the animation canvas (not <i>this</i> canvas)
     */
    public void setFullScreenMode(boolean mode) {
        animatorCanvas.setFullScreenMode(mode);
        // need to change also the size of the image !!!
        // this is now done in wrapperSvgEventListener
        // the listener in this case does not work - perhaps a bug ????
        this.svgImage.setViewportWidth(this.animatorCanvas.getWidth());
        this.svgImage.setViewportHeight(this.animatorCanvas.getHeight());
    }
    
    /**
     * When set to true, the animation starts immediatelly when 
     * the canvas with animation displayed on the screen
     */
    public void setStartAnimationImmediately(boolean startAnimation) {
        this.startAnimation = startAnimation;
    }
    
    /**
     * Should be animation reset whhen stopped
     **/
    public boolean isResetAnimationWhenStopped() {
        return resetAnimationWhenStopped;
    }
    
    /**
     * Sets 
     */
    public void setResetAnimationWhenStopped(boolean reset) {
        this.resetAnimationWhenStopped = reset;
    }    
    
    
    /**
     * Gets command listener assigned to this displayable. Can be
     * used by children classes
     */
    protected final CommandListener getCommandListener() {
        return this.commandListener;
    }
    
    /**
     * Sets command listener to this displayable
     */
    public void setCommandListener(CommandListener commandListener) {
        //super.setCommandListener(this);
        this.commandListener = commandListener;
    }    
    
    /**
     * Gets Display
     **/
    protected Display getDisplay() {
        return display;
    }
    
    /**
     * Gets SVGAnimator created in this player. Please use wisely :-)
     **/
    protected SVGAnimator getAnimator() {
        return animator;
    }
        
    /**
     * Gets SVGImage used to create this SVGPlayer.
     *
     * @return SVGImage used to create this object
     */
    public final SVGImage getSVGImage() {
        return svgImage;
    }
    
    
    
    
    /**
     * Gets SVGElement from the SVGImage used for this SVGPlayer.
     * 
     * @param id an id of the svg element to be obtained
     * @return SVGElement corresponding to the given id, or null if there is
     * no such element or the element is not of SVGElement instance
     *
     * @throws IllegalArgumentException if the supplied id is null
     */
    public SVGElement getSVGElementById(String id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id parameter cannot be null");
        }
        Element element = doc.getElementById(id);
        if (element instanceof SVGElement) {
            return (SVGElement)element;
        } else {
            return null;
        }
    }
    
    
    /**
     * Gets SVGLocatableElement from the SVGImage used for this SVGPlayer.
     * 
     * @param id an id of the svg element to be obtained
     * @return SVGLocatableElement corresponding to the given id, or null if there is
     * no such element or the element is not of SVGLocatableElement instance
     *
     * @throws IllegalArgumentException if the supplied id is null
     */
    public SVGLocatableElement getSVGLocatableElementById(String id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id parameter cannot be null");
        }
        Element element = doc.getElementById(id);
        if (element instanceof SVGLocatableElement) {
            return (SVGLocatableElement)element;
        } else {
            return null;
        }
    }    
    
    
    
    /**
     * Gets SVGAnimationElement from the SVGImage used for this SVGPlayer.
     * 
     * @param id an id of the animation element to be obtained
     * @return SVGAnimationElement corresponding to the given id, or null if there is
     * no such element or the element is not of SVGAnimationElement instance
     *
     * @throws IllegalArgumentException if the supplied id is null
     */
    public SVGAnimationElement getSVGAnimationElementById(String id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id parameter cannot be null");
        }
        Element element = doc.getElementById(id);
        if (element instanceof SVGAnimationElement) {
            return (SVGAnimationElement)element;
        } else {
            return null;
        } 
    }
        
    
    
    
    // checks whether the the current thread is a document update thread
    private boolean isRunningInUpdateThread() {
        return (Thread.currentThread() == documentUpdateThread);
    }
    
    
    /**
     * Schedule the input Runnable for execution in the update thread\
     * at a later time. In the case the animator is in stopped mode, the method 
     * invokes the runnable in a separate thread.
     *
     * @param runnable a runnable scheduled for invokation in update thread
     */
    public void invokeLaterSafely(Runnable runnable) {
        if (runnable != null) {
            if (getAnimatorState() == STOPPED) {
                new Thread(runnable).start();
            } else {
                getAnimator().invokeLater(runnable);
            }
        }
    }
    
    
    /**
     * Invokes the input Runnable in the document update thread and returns after 
     * the Runnable has completed. In the case SVGAnimator is in stopped mode or
     * if the method is being called directly from the document update thread,
     * the method invokes run() method on runnable directly.
     *
     * @param runnable a runnable scheduled for invokation in update thread
     */
    public void invokeAndWaitSafely(Runnable runnable) {
        if (runnable != null) {
            // if the method is already running in an update thread or animator is
            // stopped, the runnable is run direclty, otherwise it is run using
            // SVGAnimator.ivokeAndWait() method
            if (isRunningInUpdateThread() || (getAnimatorState() == STOPPED)) {
                runnable.run();
            } else {
                try {
                    getAnimator().invokeAndWait(runnable);
                } catch (InterruptedException ex) {
                    // ignore the interrupted exception
                }
            }
        }
    }
    
    
    
    // setting and manipulation utility methods
    
    
    
    
    
    /**
     * Sets the desired trait on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "display"
     * @param traitValue the value of the trait to set, e.g., "none"
     */
    public void setTraitSafely(final String id, final String traitName, final String traitValue) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                setTrait(id,traitName,traitValue);
            }
        });
    }
    
    
    /**
     * Sets the desired trait on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     * <p><em>Note:</em>This method needs to be called from the document update thread.</p>
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "display"
     * @param traitValue the value of the trait to set, e.g., "none"
     */
    public void setTrait(final String id, final String traitName, final String traitValue) {        
        SVGElement elt = getSVGElementById(id);                
        if (elt != null) {
            elt.setTrait(traitName, traitValue);
        }
    }

    
    /**
     * Sets the desired trait on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "display"
     * @param traitValue the value of the trait to set, e.g., "none"
     */
     public void setFloatTraitSafely(final String id, final String traitName, final float traitValue) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                setFloatTrait(id,traitName,traitValue);
            }
        });
    }
    
    
    /**
     * Sets the desired trait on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "display"
     * @param traitValue the value of the trait to set, e.g., "none"
     */
    public void setFloatTrait(final String id, final String traitName, final float traitValue) {        
        SVGElement elt = getSVGElementById(id);
        
        if (elt != null) {
            elt.setFloatTrait(traitName, traitValue);
        }
    }    
    
    
    /**
     * Sets the desired RGB value on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "stroke" or "fill"
     * @param rgb the color value as an int in the following format 0xXXRRGGBB.
     *        The high order byte is ignored. For example, 0xFFFF0000 specifies
     *        red.
     */
    public void setRGBTraitSafely(final String id, final String traitName, final int rgb) {    
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                setRGBTrait(id,traitName,rgb);
            }
        });    
    }
    
    
    /**
     * Sets the desired RGB value on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "stroke" or "fill"
     * @param rgb the color value as an int in the following format 0xXXRRGGBB.
     *        The high order byte is ignored. For example, 0xFFFF0000 specifies
     *        red.
     */
    public void setRGBTrait(final String id, final String traitName, final int rgb) {
        SVGElement elt =  getSVGElementById(id);
        
        if (elt != null) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8)  & 0xFF;
            int b = (rgb)       & 0xFF;
            
            SVGRGBColor svgRGB = svg.createSVGRGBColor(r, g, b);
            elt.setRGBColorTrait(traitName, svgRGB);
        }
    }
    
    
    /**
     * Sets the desired RGB value on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "fill"
     * @param r the desired red component value, in the 0-255 interval
     * @param g the desired green component value, in the 0-255 interval
     * @param b the desired blue component value, in the 0-255 interval
     */
    public void setRGBTraitSafely(final String id, final String traitName, final int r, final int g, final int b) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                setRGBTrait(id,traitName,r,g,b);
            }
        });
    }  
    
    
    /**
     * Sets the desired RGB value on the element with the specified identifier. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param traitName the name of the trait to change, e.g, "fill"
     * @param r the desired red component value, in the 0-255 interval
     * @param g the desired green component value, in the 0-255 interval
     * @param b the desired blue component value, in the 0-255 interval
     */
    public void setRGBTrait(final String id, final String traitName, final int r, final int g, final int b) {
        SVGElement elt =  getSVGElementById(id);
        
        if (elt != null) {
            SVGRGBColor rgb = svg.createSVGRGBColor(r, g, b);
            elt.setRGBColorTrait(traitName, rgb);
        }
    }    
    
    
    
    
    /**
     * Translates the element with the specified id by the given amount, in
     * user space. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param tx the desired translation along the x-axis
     * @param ty the desired translation along the y-axis
     */
    public void translateSafely(final String id, final float tx, final float ty) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                translate(id,tx,ty);
            }
        });
    }
    
    /**
     * Translates the element with the specified id by the given amount, in
     * user space. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param tx the desired translation along the x-axis
     * @param ty the desired translation along the y-axis
     */
    public void translate(final String id, final float tx, final float ty) {
        translate(getSVGElementById(id),tx,ty);
    }
    
    /**
     * Translates the element with the specified id by the given amount, in
     * user space.
     *
     * @param svgElement the element whose trait value should be changed. If null,
     * the operation is not performed.
     * @param tx the desired translation along the x-axis
     * @param ty the desired translation along the y-axis
     */
    public void translate(final SVGElement svgElement, final float tx, final float ty) {        
        if (svgElement != null) {
            SVGMatrix txf = svgElement.getMatrixTrait("transform");
            if (txf == null) {
                txf = svg.createSVGMatrixComponents(1, 0, 0, 1, 0, 0);
            }
            txf.mTranslate(tx, ty);
            svgElement.setMatrixTrait("transform", txf);
        }
    }
    
    
    
    
    /**
     * Translates the element with the specified id by the given amount, in
     * the screen coordinate space. If the
     * element with given ID is not an instance of SVGLocatableElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param tx the desired translation along the x-axis
     * @param ty the desired translation along the y-axis
     */
    public void screenTranslateSafely(final String id, final float tx, final float ty) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                screenTranslate(id,tx,ty);
            }
        });
    }
    
    
    
    
    /**
     * Translates the element with the specified id by the given amount, in
     * the screen coordinate space.  If the
     * element with given ID is not an instance of SVGLocatableElement, the operation is
     * not performed.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param tx the desired translation along the x-axis
     * @param ty the desired translation along the y-axis
     */
    public void screenTranslate(final String id, final float tx, final float ty) {
        screenTranslate(getSVGLocatableElementById(id),tx,ty);
    }
    
    
    /**
     * Translates the element with the specified id by the given amount, in
     * the screen coordinate space.
     *
     * @param svgLocatableElement the element whose trait value should be changed. 
     * If null, the operation is not performed.
     * @param tx the desired translation along the x-axis
     * @param ty the desired translation along the y-axis
     */
    public void screenTranslate(final SVGLocatableElement svgLocatableElement, final float tx, final float ty) {
        if (svgLocatableElement != null) {
            SVGMatrix txf = svgLocatableElement.getMatrixTrait("transform");
            if (txf == null) {
                txf = svg.createSVGMatrixComponents(1, 0, 0, 1, 0, 0);
            }
            
            // user space -> screen
            SVGMatrix screenTxf = svgLocatableElement.getScreenCTM();
            
            // screen -> user space
            SVGMatrix screenTxfInv = screenTxf.inverse();
            
            txf = txf.mMultiply(screenTxfInv);
            
            // Now, txf brings us to the screen coordinate system.
            // We concatenate the tranlation in that space.
            txf.mTranslate(tx, ty);
            
            // Now, concatenate the transform back to the user space.
            txf.mMultiply(screenTxf);
            
            svgLocatableElement.setMatrixTrait("transform", txf);
        }
    }    
    
    
    /**
     * Returns the screen bounding box for the desired element.
     *
     * @param id the id of the element whose bounding box is retrieved. 
     * @return found screen bounding box or null if the the element with given
     * was not found or does not correspond to an instance SVGLocatableElement.
     */
    public SVGRect getScreenBBox(final String id) {        
        SVGLocatableElement elt = getSVGLocatableElementById(id);
        if (elt != null) {
            return elt.getScreenBBox();
        }
        return null;
    }
    
    
    
    /**
     * Scales the element with the specified id by the given factor along the
     * x and y axis. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param sx the desired scale factor along the x-axis
     * @param sy the desired scale factor along the y-axis
     */
    public void scaleSafely(final String id, final float sx, final float sy) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                scale(id,sx,sy);
            }
        });
    }    
    
    
    /**
     * Scales the element with the specified id by the given factor along the
     * x and y axis. If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     *
     * @param id the id of the element whose trait value should be changed.
     * @param sx the desired scale factor along the x-axis
     * @param sy the desired scale factor along the y-axis
     */
    public void scale(final String id, final float sx, final float sy) {
        scale(getSVGElementById(id),sx,sy);
    }
    
    
    /**
     * Scales the element with the specified id by the given factor along the
     * x and y axis. 
     *
     * @param svgElement element whose trait value should be changed. If null, the
     * operation is not performed.
     * @param sx the desired scale factor along the x-axis
     * @param sy the desired scale factor along the y-axis
     */
    public void scale(final SVGElement svgElement, final float sx, final float sy) {        
        if (svgElement != null) {
            SVGMatrix txf = svgElement.getMatrixTrait("transform");
            if (txf == null) {
                txf = svg.createSVGMatrixComponents(1, 0, 0, 1, 0, 0);
            }
            
            SVGMatrix scale = svg.createSVGMatrixComponents(sx, 0, 0, sy, 0, 0);
            txf.mMultiply(scale);
            svgElement.setMatrixTrait("transform", txf);
        }
    }    
    
    /**
     * Scales the element with the given id about the desired anchor point.
     * If the
     * element with given ID is not an instance of SVGLocatableElement, the operation is
     * not performed.
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param sx the desired scale factor along the x-axis
     * @param sy the desired scale factor along the y-axis
     * @param anchor one of TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT,
     *        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT.
     */
    public void scaleAboutSafely(final String id, final float sx, final float sy,
            final int anchor) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                scaleAbout(id,sx,sy,anchor);
            }
        });
    }        
        
    
    /**
     * Scales the element with the given id about the desired anchor point.
     * If the
     * element with given ID is not an instance of SVGLocatableElement, the operation is
     * not performed.
     *
     * @param id the id of the element whose trait value should be changed.
     * @param sx the desired scale factor along the x-axis
     * @param sy the desired scale factor along the y-axis
     * @param anchor one of TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT,
     *        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT.
     */
    public void scaleAbout(final String id, final float sx, final float sy,
            final int anchor) {        
        scaleAbout(getSVGLocatableElementById(id),sx,sy,anchor);
    }
    
    
    /**
     * Scales the element with the given id about the desired anchor point.
     *
     * @param svgLocatableElement element whose trait value should be changed. If null
     * the operation is not performed.
     * @param sx the desired scale factor along the x-axis
     * @param sy the desired scale factor along the y-axis
     * @param anchor one of TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT,
     *        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT.
     */
    public void scaleAbout(final SVGLocatableElement svgLocatableElement, final float sx, final float sy,
            final int anchor) {
        
        if (svgLocatableElement != null) {
            SVGMatrix txf = svgLocatableElement.getMatrixTrait("transform");
            if (txf == null) {
                txf = svg.createSVGMatrixComponents(1, 0, 0, 1, 0, 0);
            }
            
            // Get the element's bounding box to compute the anchor
            // point translation.
            SVGRect bbox = svgLocatableElement.getBBox();
            float[] translate = computeAnchorTranslate(bbox, anchor);
            
            SVGMatrix scale = svg.createSVGMatrixComponents(sx, 0, 0, sy, 0, 0);
            txf.mTranslate(-translate[0], -translate[1]);
            txf.mMultiply(scale);
            txf.mTranslate(translate[0], translate[1]);
            svgLocatableElement.setMatrixTrait("transform", txf);
        }
    }    
    
    
    
    /**
     * Rotates the element with the given id by the given angle.
     *
     * If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param id the id of the element which should be rotated.
     * @param angle the rotation angle, in degrees
     */
    public void rotateSafely(final String id, final float angle) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                rotate(id,angle);
            }
        });        
    }    
    
    
    /**
     * Rotates the element with the given id by the given angle.
     *
     * If the
     * element with given ID is not an instance of SVGElement, the operation is
     * not performed.
     *
     * @param id the id of the element which should be rotated.
     * @param angle the rotation angle, in degrees
     */
    public void rotate(final String id, final float angle) {
        rotate(getSVGElementById(id),angle);
    }
    
    
    /**
     * Rotates the element with the given id by the given angle.
     *
     * @param svgElement the element which should be rotated. If null, the 
     * operation is not performed.
     * @param angle the rotation angle, in degrees
     */
    public void rotate(final SVGElement svgElement, final float angle) {        
        if (svgElement != null) {
            SVGMatrix txf = svgElement.getMatrixTrait("transform");
            if (txf == null) {
                txf = svg.createSVGMatrixComponents(1, 0, 0, 1, 0, 0);
            }
            
            txf.mRotate(angle);
            svgElement.setMatrixTrait("transform", txf);
        }
    }    
    
    
    
    /**
     * Rotates the element with the given id by the given angle about
     * the desired anchor point.
     * 
     * If the
     * element with given ID is not an instance of SVGLocatableElement, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     *
     * @param id the id of the element which should be rotated.
     * @param angle the rotation angle, in degrees
     * @param anchor the reference point about which to rotate the element.
     */
    public void rotateAboutSafely(final String id,
            final float angle,
            final int anchor) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                rotateAbout(id,angle,anchor);
            }
        }); 
    }    
    
    
    /**
     * Rotates the element with the given id by the given angle about
     * the desired anchor point.
     * 
     * If the
     * element with given ID is not an instance of SVGLocatableElement, the operation is
     * not performed.
     *
     * @param id the id of the element which should be rotated.
     * @param angle the rotation angle, in degrees
     * @param anchor the reference point about which to rotate the element.
     */
    public void rotateAbout(final String id,
            final float angle,
            final int anchor) {
        rotateAbout(getSVGLocatableElementById(id),angle,anchor);
    }
    
    
    /**
     * Rotates the element with the given id by the given angle about
     * the desired anchor point.
     *
     * @param svgLocatableElement the element which should be rotated. If null the
     * operation is not performed.
     * @param angle the rotation angle, in degrees
     * @param anchor the reference point about which to rotate the element.
     */
    public void rotateAbout(final SVGLocatableElement svgLocatableElement,
            final float angle,
            final int anchor) {
        
        if (svgLocatableElement != null) {
            SVGMatrix txf = svgLocatableElement.getMatrixTrait("transform");
            if (txf == null) {
                txf = svg.createSVGMatrixComponents(1, 0, 0, 1, 0, 0);
            }
            
            // Get the element's bounding box to compute the anchor
            // point translation.
            SVGRect bbox = svgLocatableElement.getBBox();
            float[] translate = computeAnchorTranslate(bbox, anchor);
            
            txf.mTranslate(-translate[0], -translate[1]);
            txf.mRotate(angle);
            txf.mTranslate(translate[0], translate[1]);
            
            svgLocatableElement.setMatrixTrait("transform", txf);
        }
    }    
    
    
    
    
    /**
     * Computes the translation needed to center the given rectangle about the
     * desired anchor position.
     *
     * @param bbox the object's bounding box, used to compute the translation.
     * @param anchor the desired anchor position for the translation. For example,
     * if anchor is "BOTTOM_RIGHT", the returned translation will move the
     * bottom right of the bounding box to the coordinate system's origin.
     *
     * @return the translation. A float array of size two, with the x-axis translation
     * at index 0 and the y-axis translation at index 1.
     */
    protected float[] computeAnchorTranslate(SVGRect bbox, int anchor) {
        float[] translate = {0, 0};
        switch (anchor) {
            case TOP_LEFT:
            case LEFT:
            case BOTTOM_LEFT:
                translate[0] = -bbox.getX();
                break;
            case TOP:
            case CENTER:
            case BOTTOM:
                translate[0] = -bbox.getX() - bbox.getWidth() / 2;
                break;
            case TOP_RIGHT:
            case RIGHT:
            case BOTTOM_RIGHT:
            default:
                translate[0] = -bbox.getX() - bbox.getWidth();
                break;
        }
        
        switch (anchor) {
            case TOP_LEFT:
            case TOP:
            case TOP_RIGHT:
                translate[1] = -bbox.getY();
                break;
            case LEFT:
            case CENTER:
            case RIGHT:
                translate[1] = -bbox.getY() - bbox.getHeight() / 2;
                break;
            case BOTTOM_LEFT:
            case BOTTOM:
            case BOTTOM_RIGHT:
            default:
                translate[1] = -bbox.getY() - bbox.getHeight();
                break;
        }
        
        return translate;
    }    
    
    
    
    
    /// animation utility methods
    
    /**
     * Starts immediately animation on the given animation element. If the given
     * id does not correspond to a valid animation element, the operation is
     * not performed.
     *
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param elementId id of the animation element on which the animation should be started
     *
     */      
    public void startAnimationSafely(final String elementId) {
        invokeAndWaitSafely(new Runnable() {
            public void run() {
                startAnimation(elementId);
            }
        });
    }    

    
    
    /**
     * Starts immediately animation on the given animation element. If the given
     * id does not correspond to a valid animation element, the operation is
     * not performed.
     *
     * @param elementId id of the animation element on which the animation should be started
     *
     * @throws IllegalArgumentException if the elementID does not correspond to a valid SVGAnimationElement
     */      
    public void startAnimation(String elementId) {
        startAnimation(elementId,0f);
    }
    
    

    
    /**
     * Starts immediately animation on the given animation element. If the given
     * id does not correspond to a valid animation element, the operation is
     * not performed.
     *
     * @param elementId id of the animation element on which the animation should be started
     * @param delay offset in seconds at which the animation should be started
     *
     */        
    public void startAnimation(String elementId, float delay) {
        SVGAnimationElement animationElement = getSVGAnimationElementById(elementId);
        if (animationElement != null) {
            animationElement.beginElementAt(delay);
        }
    }
    
    
    /**
     * Stops animation on the given animation element. If the given
     * id does not correspond to a valid animation element, the operation is
     * not performed.
     * 
     * <p>
     * This method uses "safe" approach - i.e. when it runs in document
     * update thread it runs directly in the thread, otherwise it is
     * scheduled using SVGAnimator.invokeAndWait method.
     *
     * @param elementId id of the animation element on which the animation should be stopped
     */
    public void stopAnimationSafely(final String elementId) {
        invokeLaterSafely(new Runnable() {
            public void run() {
                stopAnimationSafely(elementId);
            }
        });
    }

    
    /**
     * Stops animation on the given animation element. If the given
     * id does not correspond to a valid animation element, the operation is
     * not performed.
     *
     * @param elementId id of the animation element on which the animation should be stopped
     
     */      
    public void stopAnimation(String elementId) {
        stopAnimation(elementId,0f);
    }    
    
    /**
     * Stops animation on the given animation element, with given delay. 
     * Stops animation on the given animation element. If the given
     * id does not correspond to a valid animation element, the operation is
     * not performed.
     *
     * @param elementId id of the animation element on which the animation should be stopped
     * @param delay offset in seconds at which the animation should be stopped
     *
     */    
    public void stopAnimation(String elementId, float delay) {
        SVGAnimationElement animationElement = getSVGAnimationElementById(elementId);
        if (animationElement != null) {
            animationElement.endElementAt(delay);
        }        
    }
    
    
    
    /**
     * Dummy paint method - does nothing, because all the rendering is done
     * by the canvas obtained from SVGAnimator
     */
    protected void paint(Graphics graphics) {
        // dummy - should never be called
    }

    /*
    protected void showNotify() {
        //System.out.println("Current display:"+display.getCurrent());
            getDisplay().callSerially(new Runnable() {
                public void run() {
                    getDisplay().setCurrent(animatorCanvas);
                }
            });       
    }*/
    
    
    /**
     * Implementation of CommandListener.commandAction() which forwards
     * command action from Canvas created by SVGAnimator
     * to the CommandListener assigned to this component
     */
    public void commandAction(Command command, Displayable displayable) {
        if ((displayable == animatorCanvas) && (commandListener != null)) {
            commandListener.commandAction(command,this);
        }
    }

    /**
     * Wrapper for SvgEventListener - allows also userSvgEventListener to listen here
     */
    private class WrapperSvgEventListener implements  SVGEventListener {
        
        public void keyPressed(final int i) {
            if (userSvgEventListener != null) {
                userSvgEventListener.keyPressed(i);
            }
            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.keyPressed(i);
                    }
                });
            }
        }

        public void keyReleased(final int i) {
            if (userSvgEventListener != null) {
                userSvgEventListener.keyReleased(i);
            }
            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.keyReleased(i);
                    }
                });
            }            
        }

        public void pointerPressed(final int x, final int y) {
            if (userSvgEventListener != null) {
                userSvgEventListener.pointerPressed(x,y);
            }
            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.pointerPressed(x, y);
                    }
                });
            }            
        }

        public void pointerReleased(final int x, final int y) {
            if (userSvgEventListener != null) {
                userSvgEventListener.pointerReleased(x,y);
            }
            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.pointerPressed(x, y);
                    }
                });
            }            
        }

        public void hideNotify() {
            // should schedule animation reset !!!
            animationNeedsReset = true;
            // stop the animmation if necessary            
            if (animatorState != STOPPED) {
                // need to call play serially - otherwise this might cause a deadlock on the device
                /*getDisplay().callSerially(new Runnable() {
                    public void run() {
                 **/
                        stop();
                /*                    }
                });*/
            }
            // call the user's listener'
            if (userSvgEventListener != null) {
                userSvgEventListener.hideNotify();
            }
            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.hideNotify();
                    }
                });
            }            
        }

        public void showNotify() {
            // stop the animmation if necessary
            if (resetAnimationWhenStopped && animationNeedsReset) {
                reset();
            }
            if ((startAnimation) && (animatorState != PLAYING)){
                // need to call play serially - otherwise this might cause a deadlock on the device
                getDisplay().callSerially(new Runnable() {
                    public void run() {
                        play();
                    }
                });
            }
            // call the user's listeners
            if (userSvgEventListener != null) {
                userSvgEventListener.showNotify();
            }

            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.showNotify();
                    }
                });
            }            
            
        }

        public void sizeChanged(final int x, final int y) {
            // resize the image automatically? only when the user does not supply its own listener
            svgImage.setViewportWidth(x);
            svgImage.setViewportHeight(y);
            if (userSvgEventListener != null) {
                userSvgEventListener.sizeChanged(x,y);
            }
            
            if (safeSvgEventListener != null) {
                invokeAndWaitSafely(new Runnable() {
                    public void run() {
                        safeSvgEventListener.sizeChanged(x, y);
                    }
                });
            }            
        }
        
    }

    
}
