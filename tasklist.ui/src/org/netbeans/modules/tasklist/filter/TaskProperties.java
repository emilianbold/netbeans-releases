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

package org.netbeans.modules.tasklist.filter;

import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;



/**
 * An abstract factory for creating SuggestionProperties from their id.
 */
class TaskProperties {
    public static final String PROPID_GROUP = "group"; //NOI18N
    public static final String PROPID_DESCRIPTION = "description"; //NOI18N
    public static final String PROPID_FILE = "file"; //NOI18N
    public static final String PROPID_LOCATION = "location"; //NOI18N
    
    /**
     * A factory method for properties on Suggestion.
     * @param propID one of the PROP_* constant defined in this class
     * @return a property for accessing the property
     */
    public static TaskProperty getProperty(String propID) {
        if( propID.equals(PROPID_GROUP) ) {
            return PROP_GROUP;
        } else if( propID.equals(PROPID_DESCRIPTION) ) {
            return PROP_DESCRIPTION;
        } else if( propID.equals(PROPID_FILE) ) {
            return PROP_FILE;
        } else if( propID.equals(PROPID_LOCATION) ) {
            return PROP_LOCATION;
        } else {
            throw new IllegalArgumentException("Unresolved property id " + propID); //NOI18N
        }
    }
    
    
    public static TaskProperty PROP_GROUP = new TaskProperty(PROPID_GROUP, TaskGroup.class) {
        public Object getValue(Task t) {
            return Accessor.getGroup(t);
        }
    };
    
    public static TaskProperty PROP_DESCRIPTION  = new TaskProperty(PROPID_DESCRIPTION, String.class) {
        public Object getValue(Task t) {
            return Accessor.getDescription(t);
        }
    };
    
    public static TaskProperty PROP_FILE = new TaskProperty(PROPID_FILE, String.class) {
        public Object getValue(Task t) {
            FileObject file = Accessor.getResource(t);
            if( null == file || file.isFolder() )
                return ""; //NOI18N
            return file.getNameExt();
        }
    };
    
    public static TaskProperty PROP_LOCATION = new TaskProperty(PROPID_LOCATION, String.class) {
        public Object getValue(Task t) {
            FileObject file = Accessor.getResource(t);
            if( null == file || file.isFolder() )
                return FileUtil.getFileDisplayName( file );
            return FileUtil.getFileDisplayName( file.getParent() );
        }
    };
}

