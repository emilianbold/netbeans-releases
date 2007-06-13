/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * SvgcoreSettings.java
 * Created on June 13, 2006, 10:43 AM
 */

package org.netbeans.modules.mobility.svgcore.options;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Pavel Benes (based on initial version created by suchys)
 */
public class SvgcoreSettings {    
    
    private static SvgcoreSettings svgcoreSettings = null;
    
    private static final String PROP_EDITOR_PATH    = "editorPath"; //NOI18N    
    private static final String DEFAULT_EDITOR_PATH = "c:\\Program Files\\Ikivo\\Ikivo Animator\\IkivoAnimator.exe"; //NOI18N   
    private static final long   serialVersionUID    = 85176380568174L;   
    
    private Preferences prefs = null;
    
    private SvgcoreSettings() {
        prefs =  NbPreferences.forModule(SvgcoreSettings.class);
        final String propEditorPath = prefs.get(PROP_EDITOR_PATH,null);
        final File defaultEditorPathFile = new File(DEFAULT_EDITOR_PATH);
        if ((propEditorPath == null) && defaultEditorPathFile.exists()) {
            setExternalEditorPath(DEFAULT_EDITOR_PATH);
        }
    }
        
    
    public static synchronized SvgcoreSettings getDefault() {
        if (svgcoreSettings == null) {
            svgcoreSettings = new SvgcoreSettings();
        }
        return svgcoreSettings;
    }
    

    
    /**
     * Retrieve path to external editor executable
     * @return path to external editor executable
     */
    public String getExternalEditorPath(){
        return prefs.get(PROP_EDITOR_PATH, null);
    }
    
    /**
     * Sets path to external editor executable
     * @param String path to external editor executable
     */
    public final void setExternalEditorPath(String path){
        prefs.put(PROP_EDITOR_PATH, path);
    }    
}
