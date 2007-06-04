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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.ui.update;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author Tomas Stupka
 */    
public class FileUpdateInfo {

    /**
     * A  Added
     * D  Deleted
     * U  Updated
     * C  Conflict
     * G  Merged
     */
    private static final String KNOWN_ACTIONS = "ADUCG ";         
        
    public static int ACTION_TYPE_FILE                 = 1;
    public static int ACTION_TYPE_PROPERTY             = 2;
    
    public static int ACTION_ADDED                     = 4;
    public static int ACTION_DELETED                   = 8;
    public static int ACTION_UPDATED                   = 16;
    public static int ACTION_CONFLICTED                = 32;
    public static int ACTION_MERGED                    = 64;
    public static int ACTION_CONFLICTED_RESOLVED       = 128;
        
    public static int ACTION_LOCK_BROKEN               = 256;
        
    private final File file;    
    private final int action;
    
    private static final Pattern pattern = Pattern.compile("^([ADUCG ])([ADUCG ])([B ])  (.+)$");

    FileUpdateInfo(File file, int action) {
        this.file   = file;
        this.action = action;
    }

    public File getFile() {
        return file;
    }
    
    public int getAction() {
        return action;
    }
    
    public static FileUpdateInfo[] createFromLogMsg(String log) {
        Matcher m = pattern.matcher(log);
        if(!m.matches()) { 
            return null;
        }
                        
        String fileActionValue       = m.group(1);
        String propertyActionValue   = m.group(2);
        String broken                = m.group(3);
        String filePath              = m.group(4);   
        if( KNOWN_ACTIONS.indexOf(fileActionValue)     < 0 || 
            KNOWN_ACTIONS.indexOf(propertyActionValue) < 0 ) 
        {
            return null;
        }

        FileUpdateInfo[] fui = new FileUpdateInfo[2];
        int fileAction = parseAction(fileActionValue.charAt(0)) | (broken.equals("B") ? ACTION_LOCK_BROKEN : 0);
        int propertyAction = parseAction(propertyActionValue.charAt(0));                       
        fui[0] = fileAction != 0 ? new FileUpdateInfo(new File(filePath), fileAction | ACTION_TYPE_FILE) : null;
        fui[1] = propertyAction != 0 ? new FileUpdateInfo(new File(filePath), propertyAction | ACTION_TYPE_PROPERTY) : null;
        return fui;
    }
    
    private static int parseAction(char actionChar) {
        switch(actionChar) {
            case 'A': return ACTION_ADDED;          
            case 'D': return ACTION_DELETED;
            case 'U': return ACTION_UPDATED;
            case 'C': return ACTION_CONFLICTED;
            case 'G': return ACTION_MERGED;
        }                
        return 0;
    }
}
