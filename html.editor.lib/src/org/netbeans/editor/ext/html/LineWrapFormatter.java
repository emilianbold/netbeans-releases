/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
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
	super.settingsChange(evt);
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
