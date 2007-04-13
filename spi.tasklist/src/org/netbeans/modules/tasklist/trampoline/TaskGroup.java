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

package org.netbeans.modules.tasklist.trampoline;

import java.awt.Image;
import java.util.List;

/**
 * A class that describes Task group, e.g. Error, Warning, TODO etc. Task groups are
 * visible to the user in Task List's window.
 * 
 * @author S. Aubrecht
 */
public final class TaskGroup implements Comparable<TaskGroup> {
    
    private String name;
    private String displayName;
    private String description;
    private Image icon;
    private int index;
    
    /** 
     * Creates a new instance of TaskGroup
     *  
     * @param name Group's id
     * @param displayName Group's display name
     * @param description Group's description (for tooltips)
     * @param icon Group's icon
     */
    public TaskGroup( String name, String displayName, String description, Image icon ) {
        assert null != name;
        assert null != displayName;
        assert null != icon;
        
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    /**
     * @return List of all available TaskGroups.
     */
    public static List<? extends TaskGroup> getGroups() {
        return TaskGroupFactory.getDefault().getGroups();
    }
    
    /**
     * @return Identification of the group.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Group's display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return Group's description (for tooltips etc)
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return Group's icon.
     */
    public Image getIcon() {
        return icon;
    }

    public int compareTo( TaskGroup otherGroup ) {
        return index - otherGroup.index;
    }
    
    void setIndex( int index ) {
        this.index = index;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        final TaskGroup test = (TaskGroup) o;

        if (this.name != test.name && this.name != null &&
            !this.name.equals(test.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
