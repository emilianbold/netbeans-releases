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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Describes Hudson Job's Build
 * 
 * @author Michal Mocnak
 */
public class HudsonJobBuild {
    
    public enum Result {
        SUCCESS, FAILURE
    }
    
    private boolean building;
    private long duration;
    private long timestamp;
    private Result result;
    
    private List<HudsonJobChangeItem> changes = new ArrayList<HudsonJobChangeItem>();
    
    public HudsonJobBuild() {}
    
    public boolean isBuilding() {
        return building;
    }
    
    public void setBuilding(boolean building) {
        this.building = building;
    }
    
    public int getDuration() {
        return (int) (duration / 60000);
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public Date getDate() {
        return new Date(timestamp);
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Result getResult() {
        return result;
    }
    
    public void setResult(Result result) {
        this.result = result;
    }
    
    public Collection<HudsonJobChangeItem> getChanges() {
        return changes;
    }
    
    public void addChangeItem(HudsonJobChangeItem item) {
        changes.add(item);
    }
    
    public static class HudsonJobChangeItem {
        
        private String user;
        private String msg;
        
        private List<HudsonJobChangeFile> files = new ArrayList<HudsonJobChangeFile>();
        
        public String getUser() {
            return user;
        }
        
        public void setUser(String user) {
            this.user = user;
        }
        
        public String getMsg() {
            return msg;
        }
        
        public void setMsg(String msg) {
            this.msg = msg;
        }
        
        public Collection<HudsonJobChangeFile> getFiles() {
            return files;
        }
        
        public void addFile(HudsonJobChangeFile file) {
            files.add(file);
        }
    }
    
    public static class HudsonJobChangeFile {
        
        public enum EditType {
            add, edit, delete
        }
        
        private String name;
        private EditType editType;
        private String revision;
        private String prevRevision;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public EditType getEditType() {
            return editType;
        }
        
        public void setEditType(EditType editType) {
            this.editType = editType;
        }
        
        public String getRevision() {
            return revision;
        }
        
        public void setRevision(String revision) {
            this.revision = revision;
        }
        
        public String getPrevRevision() {
            return prevRevision;
        }
        
        public void setPrevRevision(String prevRevision) {
            this.prevRevision = prevRevision;
        }
    }
}