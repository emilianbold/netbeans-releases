/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class ForEach implements ActiveEditorDrop {
    
    private String variable = "";
    private String collection = "";
    private boolean fixed = false;
    private String begin = "";
    private String end = "";
    private String step = "";
    
    
    
    public ForEach() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        ForEachCustomizer c = new ForEachCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String strVariable = " var=\"" + variable + "\""; // NOI18N
        
        String strCollection = " items=\"" + collection + "\""; // NOI18N
        
        String strBegin = "", strEnd = "", strStep = "";
        if (fixed) {
            if (begin.length() > 0)
                strBegin = " begin=\"" + begin + "\""; // NOI18N
            if (end.length() > 0)
                strEnd = " end=\"" + end + "\""; // NOI18N
            if (step.length() > 0)
                strStep = " step=\"" + step + "\""; // NOI18N
        }
        
        String fe =  "<c:forEach" + strVariable + strCollection + strBegin + strEnd + strStep + ">\n" + // NOI18N
                     "</c:forEach>";// NOI18N
        
        return fe;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

   
}
