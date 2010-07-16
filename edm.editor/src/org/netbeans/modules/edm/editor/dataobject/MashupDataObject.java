/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.dataobject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.sun.org.apache.xerces.internal.util.XMLChar;

import org.netbeans.modules.edm.model.EDMException;
import java.io.InputStream;
import java.util.Iterator;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.edm.editor.utils.MashupModelHelper;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.graph.components.MashupTopPanel;
import org.netbeans.modules.edm.editor.ui.view.MashupEditorTopView;
import org.netbeans.modules.edm.editor.ui.view.property.MashupResourceManager;
import org.netbeans.modules.edm.editor.widgets.EDMNodeWidget;
import org.netbeans.modules.edm.model.MashupCollaborationModel;
import org.netbeans.modules.edm.model.impl.MashupDefinitionImpl;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.model.impl.SQLConditionImpl;
import org.netbeans.modules.edm.model.impl.SQLGroupByImpl;
import org.netbeans.modules.edm.model.impl.SQLJoinOperatorImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.property.impl.PropertyViewManager;

public class MashupDataObject extends MultiDataObject {

    public static final String MASHUP_ICON_BASE_WITH_EXT =
            "org/netbeans/modules/edm/editor/resources/mashup.png"; // NOI18N
    private MashupDataEditorSupport editorSupport;
    private transient MashupCollaborationModel mModel;
    private MashupEditorTopView view;
    private MashupDataNode mNode;
    private transient AtomicReference<Lookup> myLookup =
            new AtomicReference<Lookup>();
    private transient AtomicBoolean isLookupInit = new AtomicBoolean(false);
    private MashupGraphManager manager;
    private static PropertyViewManager pvMgr;
    private transient MashupTopPanel topPanel;

    public MashupDataObject(FileObject pf, MashupDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        editorSupport = new MashupDataEditorSupport(this);
        cookies.add(editorSupport);
        manager = new MashupGraphManager(this);
    }
    public boolean newFile = false;

    @Override
    public Node createNodeDelegate() {
        if (this.mNode == null) {
            this.mNode = new MashupDataNode(this);
        }
        return this.mNode;
    }

    @Override
    public final Lookup getLookup() {
        if (myLookup.get() == null) {

            Lookup lookup;
            List<Lookup> list = new LinkedList<Lookup>();

            list.add(Lookups.fixed(new Object[]{this}));
            lookup = new ProxyLookup(list.toArray(new Lookup[list.size()]));
            myLookup.compareAndSet(null, lookup);
            isLookupInit.compareAndSet(false, true);
        }
        return myLookup.get();
    }

    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
        } else {
            getCookieSet().remove(getSaveCookie());
        }
    }

    private SaveCookie getSaveCookie() {
        return new SaveCookie() {

            public void save() throws IOException {
                getMashupDataEditorSupport().saveDocument();
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object other) {
                return getClass().equals(other.getClass());
            }
        };
    }

    public void addSaveCookie(SaveCookie cookie) {
        getCookieSet().add(cookie);
    }

    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        MashupDataObject dataObject = (MashupDataObject) super.handleCreateFromTemplate(df, name);
        String doName = dataObject.getName();
        //make sure the name is a valid NMTOKEN.
        if (!XMLChar.isValidNmtoken(doName)) {
            return dataObject;
        }

        SaveCookie sCookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
        if (sCookie != null) {
            sCookie.save();
        }
        return dataObject;
    }

    public MashupGraphManager getGraphManager() {
        if (manager == null) {
            manager = new MashupGraphManager(this);
        }
        manager.getScene().createSatelliteView();
        return manager;
    }

    public void initialize(WizardDescriptor descriptor) {
        try {
            // get the tables list from the descriptor and add to the model.            
            String url = (String) descriptor.getProperty("mashupConnection");
            DefaultTableModel tblModel = (DefaultTableModel) descriptor.getProperty("model");
            this.mModel = MashupModelHelper.getModel(getModel(), tblModel);
            establishRuntimeInputs(this.mModel, this.mModel.getSQLDefinition().getSourceTables());
            try {
                String content = this.mModel.getEDMDefinition().toXMLString("");
                editorSupport.openDocument();
                editorSupport.getDocument().remove(0, editorSupport.getDocument().getLength());
                editorSupport.getDocument().insertString(0, content, null);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
            if (view == null) {
                view = new MashupEditorTopView(mModel);
            }
            if (manager == null) {
                manager = new MashupGraphManager(this);
                manager.refreshGraph();
                manager.layoutGraph();
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public MashupCollaborationModel getModel() {
        if (this.mModel == null) {
            Element elem = null;
            MashupDefinitionImpl etlDefn = null;
            mModel = new MashupCollaborationModel(this.getName());
            try {
                elem = parseFile(this.getPrimaryFile());
            } catch (Exception ex) {
                elem = null;
            }
            if (elem != null) {
                try {
                    etlDefn = new MashupDefinitionImpl(elem, null);
                } catch (Exception ex) {
                    etlDefn = new MashupDefinitionImpl(this.getName());
                }
            } else {
                etlDefn = new MashupDefinitionImpl(this.getName());
            }
            mModel.setDefinitionContent(etlDefn);
        }
        return this.mModel;
    }

    public MashupEditorTopView getEditorView() {
        if (this.view == null) {
            view = new MashupEditorTopView(getModel());
        }
        return this.view;
    }

    public MashupDataEditorSupport getMashupDataEditorSupport() {
        return editorSupport;
    }

    public MashupTopPanel getMashupEditorTopPanel() throws Exception {
        if (this.topPanel == null) {
            this.topPanel = new MashupTopPanel();
        }
        return this.topPanel;
    }

    private Element parseFile(FileObject pf) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(pf.getInputStream());
        return document.getDocumentElement();
    }

    public void establishRuntimeInputs(MashupCollaborationModel collabModel,
            List tables) throws EDMException {
        Iterator it = tables.iterator();
        while (it.hasNext()) {
            // if table is source table and is a flat file we auto create a run
            SQLDBTable sTable = (SQLDBTable) it.next();
            SourceColumn runtimeArg = SQLObjectUtil.createRuntimeInput(sTable, collabModel.getSQLDefinition());
            if (runtimeArg != null) {
                RuntimeInput runtimeInput = (RuntimeInput) runtimeArg.getParent();

                if (runtimeInput != null) {
                    // if runtime input is not in SQL definition then add it
                    if ((collabModel.getSQLDefinition().isTableExists(runtimeInput)) == null) {
                        collabModel.addObject(runtimeInput);
                    }
                }
            }
            SQLObjectUtil.setOrgProperties(sTable);
        }
    }

    public PropertyViewManager getPropertyViewManager() {
        InputStream stream = null;
        if (pvMgr == null) {
            try {
                stream = MashupDataObject.class.getClassLoader().getResourceAsStream("org/netbeans/modules/edm/editor/dataobject/edm_properties.xml");
                pvMgr = new PropertyViewManager(stream, new MashupResourceManager());
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignore) {
                        // ignore
                    }
                }
            }
        }
        return pvMgr;
    }

    public void persistGUIInfo(java.awt.Point p, EDMNodeWidget widget, java.awt.Rectangle bounds) {
        try {
            SQLObject obj = manager.mapWidgetToObject(widget);            
            if (obj instanceof SQLJoinOperatorImpl) {                                                 
                SQLJoinOperatorImpl join = (SQLJoinOperatorImpl) obj; 
                SQLConditionImpl tbl = (SQLConditionImpl) join.getJoinCondition();                
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_X, p.x);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_Y, p.y);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_WIDTH, bounds.width);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_HEIGHT, bounds.height);
            } if (obj instanceof SQLCanvasObject) {
                SQLCanvasObject tbl = (SQLCanvasObject) obj;
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_X, p.x);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_Y, p.y);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_WIDTH, bounds.width);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_HEIGHT, bounds.height);
            } if (obj instanceof SQLGroupByImpl) {
                SQLGroupByImpl grpBy = (SQLGroupByImpl) obj;
                SQLConditionImpl tbl = (SQLConditionImpl) grpBy.getHavingCondition();
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_X, p.x);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_Y, p.y);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_WIDTH, bounds.width);
                tbl.getGUIInfo().setAttribute(GUIInfo.ATTR_HEIGHT, bounds.height);
            }            
            editorSupport.synchDocument();
            //getModel().setDirty(true);
            //setModified(true);
        } catch (Exception e) {
        }
    }
}