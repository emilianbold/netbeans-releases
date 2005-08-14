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

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HTMLPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class A implements ActiveEditorDrop {
    
    public static final String[] protocols = new String[] { "file", "http", "https", "ftp", "mailto" }; // NOI18N
    public static final int PROTOCOL_DEFAULT = 0;
    public static final String[] targets = new String[] { "Same Frame", "New Window", "Parent Frame", "Full Window" }; // NOI18N
    public static final int TARGET_DEFAULT = 0;
    
    private int protocolIndex = PROTOCOL_DEFAULT;
    private String url = "";
    private String text = "";
    private int targetIndex = TARGET_DEFAULT;
    private String target = "";
    
    public A() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        ACustomizer c = new ACustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HTMLPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String strProtocol = " href=\""; // NOI18N
        if (getProtocolIndex() != PROTOCOL_DEFAULT) {
            try {
                switch (getProtocolIndex()) {
                    case 1: strProtocol += "http://"; // NOI18N
                            break;
                    case 2: strProtocol += "https://"; // NOI18N
                            break;
                    case 3: strProtocol += "ftp://"; // NOI18N
                            break;
                    case 4: strProtocol += "mailto:"; // NOI18N
                }
            }
            catch (NumberFormatException nfe) {} // cannot occur
        }
        
        String strURL = "\"";
        if (getUrl().length() > 0)
            strURL = getUrl() + "\"";
        
        strProtocol += strURL;

        String strTarget = "";
        if (targetIndex != -1 && targetIndex != TARGET_DEFAULT) {
            try {
                switch (getTargetIndex()) {
                    case 1: setTarget("_blank"); // NOI18N
                            break;
                    case 2: setTarget("_parent"); // NOI18N
                            break;
                    case 3: setTarget("_top"); // NOI18N
                }
            }
            catch (NumberFormatException nfe) {}
        }
        
        if (getTarget().length() > 0)
            strTarget = " target=\"" + getTarget() + "\""; // NOI18N

        String aLink = "<a" + strProtocol + strTarget + ">" + getText() + "</a>"; // NOI18N
        
        return aLink;
    }

    public int getProtocolIndex() {
        return protocolIndex;
    }

    public void setProtocolIndex(int protocolIndex) {
        this.protocolIndex = protocolIndex;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
        
}
