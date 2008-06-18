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

package org.netbeans.editor.ext.html;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.*;
import org.netbeans.editor.Settings;
import org.netbeans.editor.ext.*;

/**
 * Simple formatter that will break the line on the nearest previous space
 * after passing the text limit marker.
 *
 * @author Petr Nejedly
 * @version 1.00
 */
public class LineWrapFormatter extends ExtFormatter {

    int textLimit;
    Class kitClass;

    public LineWrapFormatter(Class kitClass) {
        super(kitClass);
        this.kitClass = kitClass;
	textLimit = getTextLimit();
    }
    
    /** Gets text limit int value. If the value is not found in local map, 
     *  then it is retrieving from Setting map. The default value is used if the 
     *  value from settings is also null. */
    private int getTextLimit(){
        Object localValue = getSettingValue(SettingsNames.TEXT_LIMIT_WIDTH);
        if (localValue != null && localValue instanceof Integer){
            return ((Integer)localValue).intValue();
        }else{
            synchronized (Settings.class) {
                Object settingsValue = Settings.getValue(kitClass, SettingsNames.TEXT_LIMIT_WIDTH);
                if (settingsValue != null && settingsValue instanceof Integer)
                    return ((Integer)settingsValue).intValue();
            }
        }
        
        return ((Integer)SettingsDefaults.defaultTextLimitWidth).intValue();
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
//	super.settingsChange(evt);
	String name = (evt != null) ? evt.getSettingName() : null;
	if (name == null || SettingsNames.TEXT_LIMIT_WIDTH.equals(name)) {
	    textLimit = getTextLimit();
	}
    }

    protected boolean acceptSyntax(Syntax syntax) {
	return (syntax instanceof HTMLSyntax);
    }

    public int[] getReformatBlock(JTextComponent target, String typedText) {
        BaseDocument doc = Utilities.getDocument(target);
        int dotPos = target.getCaret().getDot();

	// don't reformat for DEL/BKSPC
	if(typedText.length() == 0 || typedText.charAt(0) == 8 || 
			typedText.charAt(0) == 127) return null;
	
        if (doc != null) {
            try { 
                int rstart = Utilities.getRowStart(doc, dotPos);
                if(dotPos - rstart > textLimit) {
		    String preText = doc.getText(rstart, dotPos - rstart);
		    int lastSpace = preText.lastIndexOf(' ');
		    if(lastSpace > 0) {
			doc.remove(rstart+lastSpace, 1);
			doc.insertString(rstart+lastSpace, "\n", null); // NOI18N
		    }
                }
            }catch(BadLocationException e) {
		e.printStackTrace();                
            }
        }
	
        return null;
    }
    
    /** Returns offset of EOL for the white line */
    protected int getEOLOffset(BaseDocument bdoc, int offset) throws BadLocationException{
        return offset;
    }

    protected void initFormatLayers() { /* No layers */ }

}
