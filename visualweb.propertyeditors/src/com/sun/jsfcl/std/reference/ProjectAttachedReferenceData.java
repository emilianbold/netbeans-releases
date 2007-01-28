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
package com.sun.jsfcl.std.reference;

import com.sun.rave.designtime.DesignProject;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProjectAttachedReferenceData extends ReferenceData {
    public static final String PROJECT_DATA_KEY_PREFIX = "referenceData-"; //NOI18N

    protected Object versionMarker;
    // Even though I am based on a project, and not on a live one, the only
    // way I have of accessing project level data is through a live project
    protected DesignProject liveProject;

    /**
     * @param manager
     * @param definer
     */
    public ProjectAttachedReferenceData(ReferenceDataManager manager, ReferenceDataDefiner definer,
        String name, DesignProject liveProject) {

        super(manager, definer, name);
        this.liveProject = liveProject;
    }

    public void add(ReferenceDataItem item) {

        items.add(item);
        versionMarker = null;
        refreshProjectData();
    }

    /* (non-Javadoc)
     * @see com.sun.jsfcl.std.reference.ReferenceData#addToItems(java.util.List)
     */
    protected void defineItems() {
        String key = getDataKey();
        String projectData = (String) getProject().getProjectData(key);
        getDefiner().addProjectItems(projectData, items);
    }

    protected String getDataKey() {
        return PROJECT_DATA_KEY_PREFIX + getName();
    }

    public DesignProject getProject() {

        return liveProject;
    }

    public String getProjectDataKey() {

        return PROJECT_DATA_KEY_PREFIX + getName();
    }

    public Object getVersionMarker() {

        if (versionMarker == null) {
            versionMarker = new Object();
        }
        return versionMarker;
    }

    protected void refreshProjectData() {
        String key = getDataKey();
        String projectData = toString();
        getProject().setProjectData(key, projectData);
    }

    public void remove(ReferenceDataItem item) {

        items.remove(item);
        versionMarker = null;
        refreshProjectData();
    }

    public String toString() {

        return getDefiner().getProjectItemsString(getItems());
    }

}
