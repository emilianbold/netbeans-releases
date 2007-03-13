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

/*
 * TextDecorationData.java
 *
 * Created on October 21, 2004, 1:49 PM
 */

package org.netbeans.modules.css.visual.model;

import java.util.StringTokenizer;

/**
 * Data structure for the Text Decoration data
 * @author  Winston Prakash
 * @version 1.0
 */
public class TextDecorationData {

    private boolean noDecorationEnabled = false;
    private boolean underlineEnabled = false;
    private boolean overlineEnabled = false;
    private boolean lineThroughEnabled = false;
    private boolean blinkEnabled = false;

    /** Creates a new instance of TextDecorationData */
    public TextDecorationData() {
    }

    public void setDecoration(String decorationStr){
        StringTokenizer st = new StringTokenizer(decorationStr);
        enableUnderline(false);
        enableOverline(false);
        enableLineThrough(false);
        enableBlink(false);
        enableNoDecoration(false);
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            if(token.trim().equals("underline")){ //NOI18N
                enableUnderline(true);
            }
            if(token.trim().equals("overline")){ //NOI18N
                enableOverline(true);
            }
            if(token.trim().equals("line-through")){ //NOI18N
                enableLineThrough(true);
            }
            if(token.trim().equals("blink")){ //NOI18N
                enableBlink(true);
            }
            if(token.trim().equals("none")){ //NOI18N
                enableNoDecoration(true);
            }
        }
    }

    /**
     * Enable/disable the underling of text.
     */
    public void enableUnderline(boolean underlineEnabled) {
        this.underlineEnabled = underlineEnabled;
    }

    public boolean underlineEnabled() {
        return underlineEnabled;
    }

    /**
     * Enable/disable the overlining of text.
     */
    public void enableOverline(boolean overlineEnabled) {
        this.overlineEnabled = overlineEnabled;
    }

    public boolean overlineEnabled() {
        return overlineEnabled;
    }

    /**
     * Enable/disable the line through effect of text.
     */
    public void enableLineThrough(boolean lineThroughEnabled) {
        this.lineThroughEnabled = lineThroughEnabled;
    }

    public boolean lineThroughEnabled() {
        return lineThroughEnabled;
    }

    /**
     * Enable/disable text blinking.
     */
    public void enableBlink(boolean blinkEnabled) {
        this.blinkEnabled = blinkEnabled;
    }

    public boolean blinkEnabled() {
        return blinkEnabled;
    }

    /**
     * Enable/disable text blinking.
     */
    public void enableNoDecoration(boolean noDecorationEnabled) {
        this.noDecorationEnabled = noDecorationEnabled;
        if(noDecorationEnabled){
            enableUnderline(false);
            enableOverline(false);
            enableLineThrough(false);
            enableBlink(false);
        }
    }

    public boolean noDecorationEnabled() {
        return noDecorationEnabled;
    }

    public String toString(){
        String textDecoration="";
        if(noDecorationEnabled){
            return "none"; //NOI18N
        }
        if(underlineEnabled){
            textDecoration += " underline"; //NOI18N
        }
        if(overlineEnabled){
            textDecoration += " overline"; //NOI18N
        }
        if(lineThroughEnabled){
            textDecoration += " line-through"; //NOI18N
        }
        if(blinkEnabled){
            textDecoration += " blink"; //NOI18N
        }

        return textDecoration;
    }


}
