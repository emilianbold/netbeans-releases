/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_DATE        = "date"; // NOI18N
    static final String COLUMN_NAME_USERNAME    = "username"; // NOI18N
    static final String COLUMN_NAME_MESSAGE     = "message"; // NOI18N
        
    private RepositoryRevision.Event    event;
    private RepositoryRevision          container;
    private String                      path;

    public RevisionNode(RepositoryRevision container, SearchHistoryPanel master) {
        super(new RevisionNodeChildren(container, master), Lookups.fixed(master, container));
        this.container = container;
        this.event = null;
        this.path = null;
        int changedPaths = container.getLog().getChangedPaths().length;

        setName(container.getLog().getRevision().getNumber() +
                (changedPaths > 0 ? NbBundle.getMessage(RevisionNode.class, "LBL_NumberOfChangedPaths", changedPaths) : ""));

        if(changedPaths == 0) {
            setChildren(Children.LEAF);
        }        
        initProperties();
    }

    public RevisionNode(RepositoryRevision.Event revision, SearchHistoryPanel master) {
        super(Children.LEAF, Lookups.fixed(master, revision));
        this.path = revision.getChangedPath().getPath();
        this.event = revision;
        setName(revision.getName());
        initProperties();
    }

    RepositoryRevision.Event getRevision() {
        return event;
    }

    RepositoryRevision getContainer() {
        return container;
    }

    RepositoryRevision.Event getEvent() {
        return event;
    }

    @Override
    public String getShortDescription() {
        return path;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) return null;
        // TODO: reuse action code from SummaryView
        if (event == null) {
            return new Action [] {
                SystemAction.get(RevertModificationsAction.class)
            };
        } else {
            return new Action [] {
                new RollbackAction(),
                SystemAction.get(RevertModificationsAction.class)
            };
        }
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new DateProperty());
        ps.put(new UsernameProperty());
        ps.put(new MessageProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private abstract class CommitNodeProperty<T> extends PropertySupport.ReadOnly<T> {

        protected CommitNodeProperty(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                Subversion.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            try {
                return new RevisionPropertyEditor(getValue());
            } catch (Exception e) {
                return super.getPropertyEditor();
            }
        }
    }
    
    private class UsernameProperty extends CommitNodeProperty<String> {

        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                return container.getLog().getAuthor();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class DateProperty extends CommitNodeProperty<Object> {

        public DateProperty() {
            super(COLUMN_NAME_DATE, Object.class, COLUMN_NAME_DATE, COLUMN_NAME_DATE);
        }

        @Override
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                return container.getLog().getDate();
            } else {
                return ""; //NOI18N
            }
        }
    }

    private class MessageProperty extends CommitNodeProperty<String> {

        public MessageProperty() {
            super(COLUMN_NAME_MESSAGE, String.class, COLUMN_NAME_MESSAGE, COLUMN_NAME_MESSAGE);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            if (event == null) {
                return container.getLog().getMessage();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class RollbackAction extends AbstractAction {

        public RollbackAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackTo", // NOI18N
                    event.getLogInfoHeader().getLog().getRevision().getNumber()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SummaryView.rollback(event);
        }

        @Override
        public boolean isEnabled () {
            boolean enbl = false;
            if (event != null && event.getFile() != null) {
                enbl = true;
            }
            return enbl;
        }
    }

    private static class RevertModificationsAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            Set<RepositoryRevision.Event> events = new HashSet<RepositoryRevision.Event>();
            Set<RepositoryRevision> revisions = new HashSet<RepositoryRevision>();
            for (Node n : activatedNodes) {
                RevisionNode node = (RevisionNode) n;
                if (node.event != null) {
                    events.add(node.event);
                } else {
                    revisions.add(node.container);
                }
            }
            SearchHistoryPanel master = (SearchHistoryPanel) activatedNodes[0].getLookup().lookup(SearchHistoryPanel.class);
            SummaryView.revert(master, revisions.toArray(new RepositoryRevision[revisions.size()]), events.toArray(new RepositoryRevision.Event[events.size()]));
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackChange"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(RevertModificationsAction.class);
        }
    }
    
    private static class RevisionPropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        public RevisionPropertyEditor(Object value) {
            setValue(value);
        }

        @Override
        public void paintValue(Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            Object val = getValue();
            if (val instanceof Date) {
                val = DateFormat.getDateTimeInstance().format((Date) val);
            }
            renderer.setText(val == null ? "" : val.toString());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        @Override
        public boolean isPaintable() {
            return true;
        }
    }
}
