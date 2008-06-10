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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.hudson.constants.HudsonJobBuildConstants;
import org.netbeans.modules.hudson.constants.HudsonJobChangeFileConstants;
import org.netbeans.modules.hudson.constants.HudsonJobChangeItemConstants;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;

/**
 * Describes Hudson Job's Build
 *
 * @author Michal Mocnak
 */
public class HudsonJobBuild implements HudsonJobBuildConstants {
    
    public enum Result {
        SUCCESS, FAILURE
    }
    
    private final HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    private List<HudsonJobChangeItem> changes = new ArrayList<HudsonJobChangeItem>();
    
    public void putProperty(String name, Object o) {
        properties.putProperty(name, o);
    }
    
    public boolean isBuilding() {
        Boolean building = properties.getProperty(HUDSON_JOB_BUILD_BUILDING, Boolean.class);
        return building != null ? building : false;
    }
    
    public int getDuration() {
        return (int) (properties.getProperty(HUDSON_JOB_BUILD_DURATION, java.lang.Long.class) / 60000);
    }
    
    public Date getDate() {
        return new Date(properties.getProperty(HUDSON_JOB_BUILD_TIMESTAMP, Long.class));
    }
    
    public Result getResult() {
        return properties.getProperty(HUDSON_JOB_BUILD_RESULT, Result.class);
    }
    
    public Collection<HudsonJobChangeItem> getChanges() {
        return changes;
    }
    
    public void addChangeItem(HudsonJobChangeItem item) {
        changes.add(item);
    }
    
    public static class HudsonJobChangeItem implements HudsonJobChangeItemConstants {
        
        private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
        
        private List<HudsonJobChangeFile> files = new ArrayList<HudsonJobChangeFile>();
        
        public void putProperty(String name, Object o) {
            properties.putProperty(name, o);
        }
        
        public String getUser() {
            return properties.getProperty(HUDSON_JOB_CHANGE_ITEM_USER, String.class);
        }
        
        public String getMsg() {
            return properties.getProperty(HUDSON_JOB_CHANGE_ITEM_MESSAGE, String.class);
        }
        
        public Collection<HudsonJobChangeFile> getFiles() {
            return files;
        }
        
        public void addFile(HudsonJobChangeFile file) {
            files.add(file);
        }
    }
    
    public static class HudsonJobChangeFile implements HudsonJobChangeFileConstants{
        
        public enum EditType {
            add, edit, delete
        }
        
        private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
        
        public void putProperty(String name, Object o) {
            properties.putProperty(name, o);
        }
        
        public String getName() {
            return properties.getProperty(HUDSON_JOB_CHANGE_FILE_NAME, String.class);
        }
        
        public EditType getEditType() {
            return properties.getProperty(HUDSON_JOB_CHANGE_FILE_EDIT_TYPE, EditType.class);
        }
        
        public String getRevision() {
            return properties.getProperty(HUDSON_JOB_CHANGE_FILE_REVISION, String.class);
        }
        
        public String getPrevRevision() {
            return properties.getProperty(HUDSON_JOB_CHANGE_FILE_PREVIOUS_REVISION, String.class);
        }
    }
}