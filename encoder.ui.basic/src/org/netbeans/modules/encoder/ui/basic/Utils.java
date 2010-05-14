/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.ui.basic;

import java.awt.Component;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Miscellaneous utility methods
 *
 * @author Jun Xu
 */
public class Utils {

    private static final ResourceBundle _bundle =
            java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/encoder/ui/basic/Bundle"); //NOI18N
    private static final String OUTPUT_PANE_NAME =
            _bundle.getString("utils.encoding_action_io_pane_title"); //NOI18N
    
    public static InputOutput getEncodingIO() {
        return IOProvider.getDefault().getIO(OUTPUT_PANE_NAME, false);
    }
    
    public static void notify(Object obj) {
        notify(obj, false, null, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void notify(Object obj, boolean popupMsgBox,
            Component topComp, int messageType) {
        Level logLevel;
        if (messageType == JOptionPane.ERROR_MESSAGE) {
            logLevel = Level.SEVERE;
        } else if (messageType == JOptionPane.WARNING_MESSAGE) {
            logLevel = Level.WARNING;
        } else {
            if (obj instanceof Throwable) {
                logLevel = Level.SEVERE;
            } else {
                logLevel = Level.INFO;
            }
        }
        if (obj instanceof Throwable) {
            Logger.getLogger(Utils.class.getName()).log(
                    logLevel, null, (Throwable) obj);
        } else {
            Logger.getLogger(Utils.class.getName()).log(
                    logLevel, null, obj);
        }        
        if (popupMsgBox) {
            
            class NarrowOptionPane extends JOptionPane {
                
                int maxCharactersPerLineCount;
                
                NarrowOptionPane(int maxCharactersPerLineCount) {
                    this.maxCharactersPerLineCount = maxCharactersPerLineCount;
                }
                
                @Override
                public int getMaxCharactersPerLineCount() {
                    return maxCharactersPerLineCount;
                }
            }
            JOptionPane pane = new NarrowOptionPane(100);
            pane.setMessage(obj);
            pane.setMessageType(messageType);
            JDialog dialog = pane.createDialog(topComp, ""); //NOI18N
            dialog.setVisible(true);
        }
    }

    /**
     * Gets the list of all supported character sets plus some extra
     * coding methods that are supported by encoders.
     */
    public static String[] getCharsetNames(boolean toIncludeExtra) {
        List<String> nameList = new ArrayList<String>();
        if (toIncludeExtra) {
            nameList.add("BASE64");
        }
        Map<String, Charset> charsets = Charset.availableCharsets();
        for (String name : charsets.keySet()) {
            nameList.add(name);
        }
        return nameList.toArray(new String[0]);
    }
}
