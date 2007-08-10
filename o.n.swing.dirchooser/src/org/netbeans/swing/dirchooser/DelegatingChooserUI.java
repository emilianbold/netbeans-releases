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
package org.netbeans.swing.dirchooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.metal.MetalFileChooserUI;

/** Placeholder ComponentUI that just delegates to other FileChooserUIs
 * based on what selection mode is set in JFileChooser.
 *
 * @author Dafe Simonek
 */
public class DelegatingChooserUI extends ComponentUI {
    
    private static boolean firstTime = true;

    public static ComponentUI createUI(JComponent c) {
        JFileChooser fc = (JFileChooser)c;
        Class<? extends FileChooserUI> chooser = getCurChooser(fc);
        ComponentUI compUI;
        try {
            Method createUIMethod = chooser.getMethod("createUI", JComponent.class);
            compUI = (ComponentUI) createUIMethod.invoke(null, fc);
        } catch (Exception exc) {
            Logger.getLogger(DelegatingChooserUI.class.getName()).log(Level.FINE,
                    "Could not instantiate custom chooser, fallbacking to Metal", exc);
            compUI = MetalFileChooserUI.createUI(c);
        }
        
        // listen to sel mode changes and select correct chooser by invoking
        // filechooser.updateUI() which triggers this createUI again 
        if (firstTime) {
            fc.addPropertyChangeListener(
                    JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY,
                    new PropertyChangeListener () {
                        public void propertyChange(PropertyChangeEvent evt) {
                            JFileChooser fileChooser = (JFileChooser)evt.getSource();
                            fileChooser.updateUI();
                        }
                    }
            );
        }
        
        return compUI;
    }

    /** Returns dirchooser for DIRECTORIES_ONLY, default filechooser for other
     * selection modes.
     */
    private static Class<? extends FileChooserUI> getCurChooser (JFileChooser fc) {
        if (fc.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
            return DirectoryChooserUI.class;
        }
        return Module.getOrigChooser();
    }

}
