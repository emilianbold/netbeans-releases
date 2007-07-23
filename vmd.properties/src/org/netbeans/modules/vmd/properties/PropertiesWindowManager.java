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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.properties;

import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

import javax.swing.*;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;


/**
 * @author Karol Harezlak
 */
public class PropertiesWindowManager implements ActiveViewSupport.Listener {
    
    private static PropertiesWindowManager INSTANCE;
    private static final String TC_GROUP = "vmd"; //NOI18N
    
    public static void register() {
        synchronized (PropertiesWindowManager.class) {
            if (INSTANCE != null)
                return;
            INSTANCE = new PropertiesWindowManager();
            ActiveViewSupport.getDefault().addActiveViewListener(INSTANCE);
        }
    }
    
    private PropertiesWindowManager() {
    }
    
    public void activeViewChanged(final DataEditorView deactivatedView, final DataEditorView activatedView) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TopComponentGroup tcg = WindowManager.getDefault().findTopComponentGroup(TC_GROUP);
                assert tcg != null;
                if (activatedView == null 
                    || activatedView.getKind() != DataEditorView.Kind.MODEL 
                    || activatedView.getTags().contains(PropertiesSupport.DO_NOT_OPEN_PROPERTIES_WINDOW_TAG)) {
                        tcg.close();
                        return;
                }
                tcg.open();
            }
        });
    }
    
}
