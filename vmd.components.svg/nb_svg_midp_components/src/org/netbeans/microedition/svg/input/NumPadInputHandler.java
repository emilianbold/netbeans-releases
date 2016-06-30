/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

/*
 * NumPadInputHandler.java
 *
 * Created on Oct 2, 2007, 10:03:40 PM
 */

package org.netbeans.microedition.svg.input;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;

import org.netbeans.microedition.svg.SVGComponent;
import org.netbeans.microedition.svg.SVGTextField;

/**
 *
 * @author Pavel
 * @author ads
 */
public class NumPadInputHandler extends TextInputHandler {
    private static final int MAX_REPEAT_TIME  = 1000;
    private static final int CARET_BLINK_TIME =  500;
    
    private static final String[] NUMKEY_MAPPING = new String[] {
       "0",
       " .,?!1@'-_():;&/%*#+<=>\"¿¡§$£¥¤",
       "abc2äàáâãåæç",
       "def3èéêë", 
       "ghi4ìíîï",
       "jkl5",
       "mno6ñöòóôõø",
       "pqrs7", 
       "tuv8üùúû",
       "wxyz9ýÿþ"
    };

    private final Thread  caretBlinkThread;
    
    private int          nPreviousKey;
    private int          nCharIndex;
    private long         nPrevPressTime;

    public NumPadInputHandler(Display display) {
        super( display );
        caretBlinkThread = new Thread() {
            public void run() {
                int     sleepTime = CARET_BLINK_TIME;
                boolean isVisible = true;
                while(true) {
                    try {
                        while(true) {
                            fireCaretVisibilityChanged(isVisible);
                            Thread.sleep(sleepTime);
                            isVisible = !isVisible;
                            sleepTime = CARET_BLINK_TIME;
                        }
                    } catch (InterruptedException e) { 
                        sleepTime = MAX_REPEAT_TIME;
                        isVisible = false;
                    }
                }
            }
        };
        
        caretBlinkThread.setPriority(Thread.MIN_PRIORITY);
        caretBlinkThread.start();
    }
    
    public boolean handleKeyPress( SVGComponent comp, int nKeyCode ) {
        if ( nKeyCode == FIRE ){
            return super.handleKeyPress(comp, nKeyCode);
        }
        
        StringBuffer aText = new StringBuffer(getText(comp));
        int nCaret = getCaretPosition(comp);

        if (nCaret == -1) {
            return false;
        }
        long nTime = System.currentTimeMillis();
        long nDiff = nTime - nPrevPressTime;
        char cChar = 0;

        if (nKeyCode >= Canvas.KEY_NUM0 && nKeyCode <= Canvas.KEY_NUM9) {
            String sKeyChars = NUMKEY_MAPPING[nKeyCode - Canvas.KEY_NUM0];

            if (nKeyCode == nPreviousKey && nDiff < MAX_REPEAT_TIME) {
                nCharIndex++;

                if (nCharIndex >= sKeyChars.length()) {
                    nCharIndex = 0;
                }
                cChar = sKeyChars.charAt(nCharIndex);
                aText.setCharAt(nCaret - 1, cChar);
            }
            else {
                resetKeyState();
                cChar = sKeyChars.charAt(0);
                aText.insert(nCaret, cChar);
                nCaret++;
            }
        }
        else {
            switch (nKeyCode) {
                case LEFT:
                    if (nCaret > 0) {
                        setCaretPosition( comp, nCaret - 1);
                        return true;
                    }
                    break;
                case RIGHT:
                    if (nCaret < aText.length()) {
                        setCaretPosition( comp,nCaret + 1);
                        return true;
                    }
                    break;
                case Canvas.FIRE:
                case BACKSPACE:
                    if (nCaret > 0) {
                        aText.deleteCharAt(--nCaret);
                        cChar = 1;
                    }
                    break;
                // TODO: special functions
                case Canvas.KEY_POUND:
                    break;

                case Canvas.KEY_STAR:
                    break;

                default:
                    if ((int) ' ' <= nKeyCode && nKeyCode <= (int) '~') {
                        resetKeyState();
                        cChar = (char) nKeyCode;
                        aText.insert(nCaret, cChar);
                        nCaret++;
                    }

                    break;
            }
        }

        nPreviousKey = nKeyCode;
        nPrevPressTime = nTime;

        if (cChar != 0) {
            setText(comp, aText.toString());
            setCaretPosition(comp, nCaret);
            caretBlinkThread.interrupt();
            return true;
        }
        return false;
    }
    
    public void handlePointerPress( PointerEvent event ) {
        event.getComponent().requestFocus();
        super.handlePointerPress(event);
    }
    
    public void handlePointerRelease( PointerEvent event ) {
        if( event.getClickCount() == 1 ){
            if ( event.getComponent() instanceof SVGTextField  ) {
                SVGTextField field = (SVGTextField) event.getComponent();
                field.setCaretPosition( field.getText().length() );
            }
        }
        super.handlePointerRelease(event);
    }

    protected void setCaretPosition( SVGComponent comp , int position){
        if ( comp instanceof SVGTextField  ) {
            SVGTextField field = (SVGTextField) comp;
            field.setCaretPosition(position);
        }
    }
    
    protected void setText( SVGComponent comp , String text){
        if ( comp instanceof SVGTextField  ) {
            SVGTextField field = (SVGTextField) comp;
            field.setText(text);
        }
    }
    
    protected int getCaretPosition(SVGComponent comp ){
        if ( comp instanceof SVGTextField  ) {
            SVGTextField field = (SVGTextField) comp;
            return field.getCaretPosition();
        }
        return -1;
    }
    
    protected String getText( SVGComponent comp ){
        if ( comp instanceof SVGTextField  ) {
            SVGTextField field = (SVGTextField) comp;
            return field.getText();
        }
        return null;
    }
    
    private void resetKeyState() {
        nPreviousKey   = 0;
        nCharIndex     = 0;
        nPrevPressTime = 0;
    }
}
