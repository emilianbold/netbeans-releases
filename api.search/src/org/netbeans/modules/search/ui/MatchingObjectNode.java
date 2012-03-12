/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.search.MatchingObject;
import org.openide.cookies.EditCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jhavlin
 */
public class MatchingObjectNode extends AbstractNode {

    private MatchingObject matchingObject;
    private Node original;
    private boolean valid = true;

    public MatchingObjectNode(Node original,
            org.openide.nodes.Children children,
            MatchingObject matchingObject, final boolean replacing) {
        this(original, children, matchingObject,
                new ReplaceCheckableNode(matchingObject, replacing));
    }

    private MatchingObjectNode(Node original,
            org.openide.nodes.Children children,
            final MatchingObject matchingObject,
            ReplaceCheckableNode checkableNode) {
        super(children, Lookups.fixed(matchingObject, checkableNode));
        this.matchingObject = matchingObject;
        if (matchingObject.isObjectValid()) {
            this.original = original;
            setValidOriginal();
        } else {
            setInvalidOriginal();
        }
        matchingObject.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!matchingObject.isObjectValid() && valid) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setInvalidOriginal();
                        }
                    });
                } else {
                    fireIconChange();
                    ResultsOutlineSupport.toggleParentSelected(
                            MatchingObjectNode.this);
                }
            }
        });
        original.addNodeListener(new OrigNodeListener());
    }

    @Override
    public Image getIcon(int type) {
        if (valid) {
            return original.getIcon(type);
        } else {
            return ImageUtilities.loadImage(
                    "org/netbeans/modules/search/res/invalid.png");     //NOI18N
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        return original.getHtmlDisplayName();
    }

    @Override
    public String getDisplayName() {
        return original.getDisplayName();
    }

    @Override
    public Action getPreferredAction() {
        return new OpenNodeAction();
    }

    private void setValidOriginal() {
        fireIconChange();
        fireDisplayNameChange(null, null);
    }

    private void setInvalidOriginal() {
        valid = false;
        original = new AbstractNode(Children.LEAF);
        original.setDisplayName(matchingObject.getFileObject().getNameExt());
        fireIconChange();
        fireDisplayNameChange(matchingObject.getDataObject().getName(),
                matchingObject.getFileObject().getNameExt());
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public PropertySet[] getPropertySets() {

        PropertySet[] sets = new PropertySet[1];
        PropertySet set = new PropertySet("default", "default properties",
                "Default Properties") {

            @Override
            public Property<?>[] getProperties() {
                Property[] properties = new Property[]{
                    new SizeProperty(),
                    new LastModifiedProperty(),
                    new DetailsCountProperty(),
                    new PathProperty()
                };
                return properties;
            }
        };

        sets[0] = set;
        return sets;
    }

    private class SizeProperty extends Property<Long> {

        public SizeProperty() {
            super(Long.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Long getValue() throws IllegalAccessException, InvocationTargetException {
            return matchingObject.getFileObject().getSize();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Long val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            return "size";
        }
    }

    private class LastModifiedProperty extends Property<Date> {

        public LastModifiedProperty() {
            super(Date.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Date getValue() throws IllegalAccessException,
                InvocationTargetException {
            return matchingObject.getFileObject().lastModified();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Date val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "lastModified";                                      //NOI18N
        }
    }

    private class DetailsCountProperty extends Property<Integer> {

        public DetailsCountProperty() {
            super(Integer.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Integer getValue() throws IllegalAccessException,
                InvocationTargetException {
            return matchingObject.getDetailsCount();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Integer val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();                  //NOI18N
        }

        @Override
        public String getName() {
            return "detailsCount";                                      //NOI18N
        }
    }

     private class PathProperty extends Property<String> {

        public PathProperty() {
            super(String.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public String getValue() throws IllegalAccessException,
                InvocationTargetException {
            return matchingObject.getFileObject().getPath();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();                  //NOI18N
        }

        @Override
        public String getName() {
            return "path";                                              //NOI18N
        }
    }

    private class OrigNodeListener implements NodeListener {

        public OrigNodeListener() {
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setInvalidOriginal();
                    original.removeNodeListener(OrigNodeListener.this);
                }
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setValidOriginal();
        }
    }

    private class OpenNodeAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            EditCookie editCookie = original.getLookup().lookup(
                    EditCookie.class);
            if (editCookie != null) {
                editCookie.edit();
            }
        }
    }
}
