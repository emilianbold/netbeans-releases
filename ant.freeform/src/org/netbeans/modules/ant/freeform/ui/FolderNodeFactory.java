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


package org.netbeans.modules.ant.freeform.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;


/**
 *
 * @author mkleint
 */
public class FolderNodeFactory implements NodeFactory {
    
    /** Creates a new instance of FolderNodeFactory */
    public FolderNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        FreeformProject project = p.getLookup().lookup(FreeformProject.class);
        assert project != null;
        return new RootChildren(project);
    }

    
    private static final class RootChildren implements NodeList<Element>, AntProjectListener {
        
        private final FreeformProject p;
        private List<Element> keys = new ArrayList<Element>();
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        public RootChildren(FreeformProject p) {
            this.p = p;
        }
        
        public void addNotify() {
            updateKeys(false);
            p.helper().addAntProjectListener(this);
            // XXX should probably listen to project.evaluator also?
        }
        
        public void removeNotify() {
            keys = null;
            p.helper().removeAntProjectListener(this);
        }
        
        private void updateKeys(boolean fromListener) {
            Element genldata = p.helper().getPrimaryConfigurationData(true);
            Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
            if (viewEl != null) {
                Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
                keys = Util.findSubElements(itemsEl);
                if (fromListener) {
                    // #50328 - post setKeys to different thread to prevent deadlocks
                    RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                fireChange();
                            }
                        });
                } 
            } else {
                if (fromListener) {
                    keys = Collections.<Element>emptyList();
                    // #58491 - post setKeys to different thread to prevent deadlocks
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                }
            }
        }
        

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateKeys(true);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
        public List<Element> keys() {
            return keys;
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            List<ChangeListener> list = new ArrayList<ChangeListener>();
            list.addAll(listeners);
            for (ChangeListener ls : listeners) {
                ls.stateChanged(new ChangeEvent(this));
            }
        }

        public Node node(Element itemEl) {
            
            Element locationEl = Util.findElement(itemEl, "location", FreeformProjectType.NS_GENERAL); // NOI18N
            String location = Util.findText(locationEl);
            String locationEval = p.evaluator().evaluate(location);
            if (locationEval == null) {
                return null;
            }
            FileObject file = p.helper().resolveFileObject(locationEval);
            if (file == null) {
                // Not there... skip this node.
                return null;
            }
            String label;
            Element labelEl = Util.findElement(itemEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
            if (labelEl != null) {
                label = Util.findText(labelEl);
            } else {
                label = null;
            }
            if (itemEl.getLocalName().equals("source-folder")) { // NOI18N
                if (!file.isFolder()) {
                    // Just a file. Skip it.
                    return null;
                }
                String style = itemEl.getAttribute("style"); // NOI18N
                for (ProjectNature nature : Lookup.getDefault().lookupAll(ProjectNature.class)) {
                    if (nature.getSourceFolderViewStyles().contains(style)) {
                        return nature.createSourceFolderView(p, file, style, location, label);
                    }
                }
                // fall back to tree display
                // assert style.equals(STYLE_TREE);
                DataObject fileDO;
                try {
                    fileDO = DataObject.find(file);
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
                return new ViewItemNode((DataFolder) fileDO, location, label);
            } else {
                assert itemEl.getLocalName().equals("source-file") : itemEl; // NOI18N
                    DataObject fileDO;
                    try {
                        fileDO = DataObject.find(file);
                    } catch (DataObjectNotFoundException e) {
                        throw new AssertionError(e);
                    }
                return new ViewItemNode(fileDO.getNodeDelegate(), location, label);
            }
        }
    }
    
    
    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            Object[] listeners = ell.getListenerList();     
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {             
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }
    
     private static final class ViewItemNode extends FilterNode {
        
        private final String name;
        
        private final String displayName;
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
       
        public ViewItemNode(Node orig, String name, String displayName) {
            super(orig);
            this.name = name;
            this.displayName = displayName;
        }
        
        public ViewItemNode(DataFolder folder, String name, String displayName) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
            this.name = name;
            this.displayName = displayName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            if (displayName != null) {
                return displayName;
            } else {
                // #50425: show original name incl. annotations
                return super.getDisplayName();
            }
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
    }    
}