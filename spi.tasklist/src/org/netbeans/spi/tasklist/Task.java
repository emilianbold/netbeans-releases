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

package org.netbeans.spi.tasklist;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.tasklist.trampoline.TaskGroupFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * A class holding the description of a single Task that will appear in TaskList's window.
 * 
 * @author S. Aubrecht
 */
public final class Task {
    
    private FileObject resource;
    private TaskGroup group;
    private String description;
    private int line;
    private ActionListener al;
    
    static {
        Accessor.DEFAULT = new AccessorImpl();
    }
    
    /**
     * Create a new Task
     * 
     * @param resource File or folder which the Task applies to, cannot be null.
     * @param groupName Name of the group this task belongs to (error, warning, todo, etc).
     * @param description A brief summary of the task (one line if possible), cannot be null.
     * @param line Line number in a text file, use negative value if line number is not applicable.
     * 
     * @return New task.
     */
    public static Task create( FileObject resource, String groupName, String description, int line ) {
        return new Task( resource, getTaskGroup( groupName ), description, line, null );
    }
    
    /**
     * Create a new Task
     * 
     * @param resource File or folder which the Task applies to, cannot be null.
     * @param groupName Name of the group this task belongs to (error, warning, todo, etc).
     * @param description A brief summary of the task (one line if possible), cannot be null.
     * @param al Task's default action, e.g. double-click or Enter key in the Task List window.
     * 
     * @return New task.
     */
    public static Task create( FileObject resource, String groupName, String description, ActionListener al ) {
        return new Task( resource, getTaskGroup( groupName ), description, -1, al );
    }
    
    /** Creates a new instance of Task */
    private Task( FileObject resource, TaskGroup group, String description, int line, ActionListener al ) {
        assert null != group;
        assert null != description;
        assert null != resource;
        
        this.resource = resource;
        this.group = group;
        this.description = description;
        this.line = line;
        this.al = al;
    }
    
    /**
     * Resource (file or folder) this taks applies to.
     * @return Resource (file or folder) this taks applies to.
     */
    FileObject getResource() {
        return resource;
    }
    
    /**
     * The group this task belongs to (error, warning, todo, etc)
     * @return The group this task belongs to (error, warning, todo, etc)
     */
    TaskGroup getGroup() {
        return group;
    }
    
    /**
     * Task description.
     * @return Task description.
     */
    String getDescription() {
        return description;
    }
    
    /**
     * One-based line number in a text file this task applies to, -1 if the line number is not applicable. 
     * @return One-based line number in a text file this task applies to, -1 if the line number is not applicable. 
     */
    int getLine() {
        return line;
    }
    
    /**
     * Action to be invoked when user double-clicks the task in the Task List window.
     * @return Task's default action or null if not available.
     */
    ActionListener getActionListener() {
        return al;
    }
    
    /**
     * Create a new TaskGroup, called from XML layer.
     */
    static TaskGroup createGroup( Map<String,String> attrs ) {
        return TaskGroupFactory.create( attrs );
    }
    
    private static Set<String> unknownTaskGroups;
    
    private static TaskGroup getTaskGroup( String groupName ) {
        TaskGroup group = TaskGroupFactory.getDefault().getGroup( groupName );
        if( null == group ) {
            if( null == unknownTaskGroups || !unknownTaskGroups.contains( groupName ) ) {
                //show only one warning that the group name is not supported
                Logger.getLogger( Task.class.getName() ).log( Level.INFO, 
                        NbBundle.getMessage( Task.class, "Err_UnknownGroupName" ), groupName ); //NOI18N
                if( null == unknownTaskGroups )
                    unknownTaskGroups = new HashSet<String>(10);
                unknownTaskGroups.add( groupName );
            }
            
            group = TaskGroupFactory.getDefault().getDefaultGroup();
        }
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        final Task test = (Task) o;

        if (this.line != test.line)
            return false;
        if (this.description != test.description && this.description != null &&
            !this.description.equals(test.description))
            return false;
        if (this.group != test.group && this.group != null &&
            !this.group.equals(test.group))
            return false;
        if (this.resource != test.resource && this.resource != null &&
            !this.resource.equals(test.resource))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 17 * hash + this.line;
        hash = 17 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 17 * hash + (this.group != null ? this.group.hashCode() : 0);
        hash = 17 * hash + (this.resource != null ? this.resource.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" ); 
        buffer.append( getResource() );
        buffer.append( ", " ); 
        buffer.append( getLine() );
        buffer.append( ", " ); 
        buffer.append( getDescription() );
        buffer.append( ", " ); 
        buffer.append( getGroup() );
        buffer.append( "]" ); 
        return buffer.toString();
    }
}
