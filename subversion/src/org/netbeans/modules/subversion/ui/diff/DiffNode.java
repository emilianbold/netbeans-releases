/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.subversion.ui.diff;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.lookup.Lookups;
import org.openide.ErrorManager;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class DiffNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME = "name";
    static final String COLUMN_NAME_STATUS = "status";
    static final String COLUMN_NAME_LOCATION = "location";
        
    private final Setup     setup;
    private String          htmlDisplayName;

    public DiffNode(Setup setup) {
        super(Children.LEAF, Lookups.singleton(setup));
        this.setup = setup;
        setName(setup.getBaseFile().getName());
        initProperties();
        refreshHtmlDisplayName();
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = setup.getInfo(); 
        int status = info.getStatus();
        // Special treatment: Mergeable status should be annotated as Conflict in Versioning view according to UI spec
        if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            status = FileInformation.STATUS_VERSIONED_CONFLICT;
        }
        htmlDisplayName = Subversion.getInstance().getAnnotator().annotateNameHtml(setup.getBaseFile().getName(), info, null);
        fireDisplayNameChange(htmlDisplayName, htmlDisplayName);
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    public Setup getSetup() {
        return setup;
    }

    public Action[] getActions(boolean context) {
        if (context) return null;
        return new Action [0];
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new NameProperty());
        ps.put(new LocationProperty());
        ps.put(new StatusProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private abstract class DiffNodeProperty extends PropertySupport.ReadOnly {

        protected DiffNodeProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }
    }

    private class NameProperty extends DiffNodeProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, COLUMN_NAME_NAME, COLUMN_NAME_NAME);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return DiffNode.this.getName();
        }
    }
    
    private class LocationProperty extends DiffNodeProperty {
        
        private String location;

        public LocationProperty() {
            super(COLUMN_NAME_LOCATION, String.class, COLUMN_NAME_LOCATION, COLUMN_NAME_LOCATION);
            try {
                location = SvnUtils.getRelativePath(setup.getBaseFile());
            } catch (SVNClientException e) {
                location = "";
            }
            setValue("sortkey", location + "\t" + DiffNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return location;
        }
    }
    
    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N

    private class StatusProperty extends DiffNodeProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, String.class, COLUMN_NAME_STATUS, COLUMN_NAME_STATUS);
            String shortPath = null;
            try {
                shortPath = SvnUtils.getRelativePath(setup.getBaseFile());
            } catch (SVNClientException e) {
                shortPath = "";
            }
            String sortable = Integer.toString(SvnUtils.getComparableStatus(setup.getInfo().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + DiffNode.this.getName().toUpperCase()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return setup.getInfo().getStatusText();
        }
    }
}
