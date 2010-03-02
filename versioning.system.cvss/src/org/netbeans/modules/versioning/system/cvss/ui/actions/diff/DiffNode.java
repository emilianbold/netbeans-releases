/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.lookup.Lookups;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.util.Utils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

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

    public DiffNode(Setup setup, CvsFileNode node) {
        super(Children.LEAF, getLookupFor(setup, node.getLookupObjects()));
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
        htmlDisplayName = CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(setup.getBaseFile().getName(), info, null);
        fireDisplayNameChange(htmlDisplayName, htmlDisplayName);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    public Setup getSetup() {
        return setup;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) return null;
        return new Action [0];
    }
    
    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    @SuppressWarnings("unchecked") // Adding getCookie(Class<Cookie> klass) results in name clash
    @Override
    public Cookie getCookie(Class klass) {
        FileObject fo = FileUtil.toFileObject(getSetup().getBaseFile());
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                if (fo.equals(dobj.getPrimaryFile())) {
                    return dobj.getCookie(klass);
                }
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
        return super.getCookie(klass);
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

    private static org.openide.util.Lookup getLookupFor (Setup setup, Object[] lookupObjects) {
        Object[] allLookupObjects = new Object[lookupObjects.length + 1];
        allLookupObjects[0] = setup;
        System.arraycopy(lookupObjects, 0, allLookupObjects, 1, lookupObjects.length);
        return Lookups.fixed(allLookupObjects);
    }

    private abstract class DiffNodeProperty extends PropertySupport.ReadOnly {

        protected DiffNodeProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
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
        
        private final String location;

        public LocationProperty() {
            super(COLUMN_NAME_LOCATION, String.class, COLUMN_NAME_LOCATION, COLUMN_NAME_LOCATION);
            location = Utils.getRelativePath(setup.getBaseFile());
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
            String shortPath = Utils.getRelativePath(setup.getBaseFile());
            String sortable = Integer.toString(Utils.getComparableStatus(setup.getInfo().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + DiffNode.this.getName().toUpperCase()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return setup.getInfo().getStatusText();
        }
    }
}
