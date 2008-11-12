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
package org.netbeans.microedition.svg.input;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.netbeans.microedition.svg.SVGComponent;
import org.netbeans.microedition.svg.SVGTextField;


/**
 * @author ads
 *
 */
public class TextInputHandler extends InputHandler implements CommandListener {

    private static final String OK = "OK";          // NOI18N

    public TextInputHandler( Display display ){
        myDisplay = display;
    }
    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.input.InputHandler#handleKeyPress(org.netbeans.microedition.svg.SVGComponent, int)
     */
    public boolean handleKeyPress( SVGComponent comp, int keyCode ) {
        if ( keyCode == FIRE ){
            showTextBox(comp);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.microedition.svg.input.InputHandler#handleKeyRelease(org.netbeans.microedition.svg.SVGComponent, int)
     */
    public boolean handleKeyRelease( SVGComponent comp, int keyCode ) {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command cmd, Displayable disp) {
        TextBox lcduiText = (TextBox) disp;
        final String text = lcduiText.getString();
        final int    pos  = lcduiText.getCaretPosition();
            
        getDisplay().setCurrent(myPreviousDisp);
        myPreviousDisp = null;

        getDisplay().callSerially( new Runnable() {
            public void run() {
                myCurrentTextField.setText(text);
                myCurrentTextField.setCaretPosition(pos);
                myCurrentTextField = null;
            }
        });
    }
    
    protected Display getDisplay() {
        return myDisplay;
    }
    
    protected void showTextBox( SVGComponent comp ) {
        if ( !(comp instanceof SVGTextField )){
            return;
        }
        SVGTextField svgField = (SVGTextField) comp;
        myCurrentTextField = svgField;
        TextBox lcduiText = new TextBox( svgField.getTitle(), svgField.getText(), 100, TextField.ANY);
        lcduiText.addCommand(new Command( OK, Command.OK, 0));
        lcduiText.setCommandListener(this);
        
        myPreviousDisp = getDisplay().getCurrent();
        getDisplay().setCurrent(lcduiText);
    }
    
    public void handlePointerRelease( PointerEvent event ) {
        if( event.getClickCount() > 1 ){
            showTextBox( event.getComponent() );
        }
        super.handlePointerRelease(event);
    }
    
    private final Display myDisplay;
    private SVGTextField myCurrentTextField = null;
    private Displayable  myPreviousDisp = null;

}
