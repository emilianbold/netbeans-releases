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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
     * @return true if the return value of toString() method changes after setting the value.
     */
    public boolean enableUnderline(boolean underlineEnabled) {
        boolean change = (underlineEnabled() != underlineEnabled) && !noDecorationEnabled();
        this.underlineEnabled = underlineEnabled;
        return change;
    }

    public boolean underlineEnabled() {
        return underlineEnabled;
    }

    /**
     * Enable/disable the overlining of text.
     * @return true if the return value of toString() method changes after setting the value.
     */
    public boolean enableOverline(boolean overlineEnabled) {
        boolean change = (overlineEnabled() != overlineEnabled) && !noDecorationEnabled();
        this.overlineEnabled = overlineEnabled;
        return change;
    }

    public boolean overlineEnabled() {
        return overlineEnabled;
    }

    /**
     * Enable/disable the line through effect of text.
     * @return true if the return value of toString() method changes after setting the value.
     */
    public boolean enableLineThrough(boolean lineThroughEnabled) {
        boolean change = (lineThroughEnabled() != lineThroughEnabled) && !noDecorationEnabled();
        this.lineThroughEnabled = lineThroughEnabled;
        return change;
    }

    public boolean lineThroughEnabled() {
        return lineThroughEnabled;
    }

    /**
     * Enable/disable text blinking.
     * @return true if the return value of toString() method changes after setting the value.
     */
    public boolean enableBlink(boolean blinkEnabled) {
        boolean change = (blinkEnabled() != blinkEnabled) && !noDecorationEnabled();
        this.blinkEnabled = blinkEnabled;
        return change;
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
