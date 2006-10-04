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
 */   
/*
 * SvgcoreSettings.java
 *
 * Created on June 13, 2006, 10:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.options;

import java.io.File;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 *
 * @author suchys
 */
public class SvgcoreSettings extends SystemOption {
    
    public static final String PROP_EDITOR_PATH = "editorPath"; //NOI18N
    
    private static final String DEFAULT_EDITOR_PATH = "c:\\Program Files\\Ikivo\\Ikivo Animator\\IkivoAnimator.exe"; //NOI18N
   
    private static final long serialVersionUID = 85176380568174L;
    
    protected void initialize(){
        super.initialize();
        if (new File(DEFAULT_EDITOR_PATH).exists()){
            setExternalEditorPath(DEFAULT_EDITOR_PATH);
        }
    }

    public static SvgcoreSettings getDefault(){
        return (SvgcoreSettings) findObject(SvgcoreSettings.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage(SvgcoreSettings.class, "LBL_SvgSettings"); //NOI18N
    }
    
    /**
     * Retrive path to external editor executable
     * @return path to external editor executable
     */
    public String getExternalEditorPath(){
        return (String) getProperty(PROP_EDITOR_PATH);
    }
    
    /**
     * Sets path to external editor executable
     * @param String path to external editor executable
     */
    public void setExternalEditorPath(String path){
        putProperty(PROP_EDITOR_PATH, path);
    }    
}
