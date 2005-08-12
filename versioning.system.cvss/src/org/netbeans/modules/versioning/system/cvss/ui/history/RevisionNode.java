/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME        = "name";
    static final String COLUMN_NAME_REVISION    = "revision";
    static final String COLUMN_NAME_DATE        = "date";
    static final String COLUMN_NAME_USERNAME    = "username";
    static final String COLUMN_NAME_MESSAGE     = "message";
    static final String COLUMN_NAME_PATH        = "path";
        
    private LogInformation.Revision                 revision;
    private SearchHistoryPanel.ResultsContainer     container;

    public RevisionNode(SearchHistoryPanel.ResultsContainer container) {
        super(new RevisionNodeChildren(container), Lookups.singleton(container));
        this.container = container;
        this.revision = null;
        initProperties();
    }

    public RevisionNode(LogInformation.Revision revision) {
        super(Children.LEAF, Lookups.fixed(new Object [] { revision }));
        this.revision = revision;
        initProperties();
    }

    LogInformation.Revision getRevision() {
        return revision;
    }

    SearchHistoryPanel.ResultsContainer getContainer() {
        return container;
    }

    public String getName() {
        LogInformation.Revision rev = revision != null ? revision : (LogInformation.Revision) container.getRevisions().get(0);
        return rev.getLogInfoHeader().getFile().getName();
    }

    public Action[] getActions(boolean context) {
        if (context) return null;
        return new Action [] {
            new FindAssociateChangesAction()
        };
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new RevisionProperty());
        ps.put(new DateProperty());
        ps.put(new UsernameProperty());
        ps.put(new MessageProperty());
        ps.put(new PathProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private abstract class CommitNodeProperty extends PropertySupport.ReadOnly {

        protected CommitNodeProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                return "<error>";
            }
        }
    }
    
    private class RevisionProperty extends CommitNodeProperty {

        public RevisionProperty() {
            super(COLUMN_NAME_REVISION, String.class, COLUMN_NAME_REVISION, COLUMN_NAME_REVISION);
        }

        public Object getValue() {
            if (revision != null) {
                return revision.getNumber();
            } else {
                List revs = container.getRevisions();
                LogInformation.Revision newest = (LogInformation.Revision) revs.get(0);
                LogInformation.Revision eldest = (LogInformation.Revision) revs.get(revs.size() - 1);
                return newest.getNumber() + " - " + eldest.getNumber();
            }
        }
    }

    private class PathProperty extends CommitNodeProperty {

        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, COLUMN_NAME_PATH, COLUMN_NAME_PATH);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "";
        }
    }
    
    private class UsernameProperty extends CommitNodeProperty {

        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getAuthor();
            } else {
                return "";
            }
        }
    }

    private class DateProperty extends CommitNodeProperty {

        public DateProperty() {
            super(COLUMN_NAME_DATE, String.class, COLUMN_NAME_DATE, COLUMN_NAME_DATE);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getDateString();
            } else {
                return "";
            }
        }
    }

    private class MessageProperty extends CommitNodeProperty {

        public MessageProperty() {
            super(COLUMN_NAME_MESSAGE, String.class, COLUMN_NAME_MESSAGE, COLUMN_NAME_MESSAGE);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getMessage();
            } else {
                return "";
            }
        }
    }

    private class FindAssociateChangesAction extends AbstractAction {

        public FindAssociateChangesAction() {
            super(NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindAssociateChanges"));
        }

        public void actionPerformed(ActionEvent e) {
            File file = revision.getLogInfoHeader().getFile();
            SearchHistoryAction.openSearch(NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), revision.getNumber()), 
                                           revision.getMessage().trim(), revision.getAuthor(), revision.getDate());
        }
    }
}
