/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.dataloader;

import java.io.IOException;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.xml.sax.InputSource;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.MultiDataObject;


public class WorklistDataObject extends MultiDataObject {
    
//    public static final String DD_MULTIVIEW_PREFIX = "dd_multiview"; // NOI18N
//    public static final String MULTIVIEW_OVERVIEW = "Overview"; // NOI18N
//    public static final String MULTIVIEW_ASSIGNMENT = "Assignment"; // NOI18N
//    public static final String MULTIVIEW_ESCALATION = "Escalation"; // NOI18N
//    public static final String MULTIVIEW_TIMEOUT = "Timeout"; // NOI18N
//    public static final String MULTIVIEW_NOTIFICATION = "Notification"; // NOI18N
    
     /** Property name for documentDTD property */
//    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N
//    public static final String HELP_ID_PREFIX_OVERVIEW="dd_multiview_overview_"; //NOI18N
//    public static final String HELP_ID_PREFIX_ASSIGNMENT="worklist_multiview_assignment_"; //NOI18N
//    public static final String HELP_ID_PREFIX_FILTERS="dd_multiview_filters_"; //NOI18N
//    public static final String HELP_ID_PREFIX_PAGES="dd_multiview_pages_"; //NOI18N
//    public static final String HELP_ID_PREFIX_REFERENCES="dd_multiview_references_"; //NOI18N
//    public static final String HELP_ID_PREFIX_SECURITY="dd_multiview_security_"; //NOI18N

//    private TaskSelectionModel taskSelectionModel;
//            
//    private WLMModel model;
    
//    private WorklistEditorSupport editorSupport;
    
    public WorklistDataObject(FileObject pf, WorklistDataLoader loader) 
            throws DataObjectExistsException 
    {
        super(pf, loader);
        
        CookieSet set = getCookieSet();
        
        // editor support defines MIME type understood by EditorKits registry
        set.add(new WorklistEditorSupport(this));

        // Add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is));
    }

    public WLMModel getModel() {
        WorklistEditorSupport support = getWlmEditorSupport();
        return (support == null) ? null : support.getModel();
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new WorklistDataNode(this);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected void handleDelete() throws IOException {
        if (isModified()) {
            setModified(false);
        }
        getWlmEditorSupport().getEnv().unmarkModified();
        super.handleDelete();
    }    
    
    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        //TODO:make sure we save file before moving This is what jave move does.
        //It also launch move refactoring dialog which we should be doing
        //as well
        if(isModified()) {
            SaveCookie sCookie = this.getCookie(SaveCookie.class);
            if(sCookie != null) {
                sCookie.save();
            }
        }

        return super.handleMove(df);
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
                getWlmEditorSupport().saveDocument();
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object other) {
                return other != null && getClass().equals(other.getClass());
            }
        };
    }    

    public WorklistEditorSupport getWlmEditorSupport() {
        return getCookie(WorklistEditorSupport.class);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    ////////////////////////////////////
    ////////////////////////////////////
    ////////////////////////////////////
    
//    @Override
//    public Lookup getLookup() {
//        if (myLookup.get() == null) {
//            Lookup superLookup = super.getLookup();
//            //
//            Lookup[] lookupArr = new Lookup[] {
//                Lookups.fixed(new Object[] {
////                    new SearchProvider(this),
////                    XmlFileEncodingQueryImpl.singleton()}),
//                superLookup};
//            //
//            Lookup newLookup = new ProxyLookup(lookupArr);
//            myLookup.compareAndSet(null, newLookup);
//        }
//        return myLookup.get();
//    }    
    

    
//    private transient AtomicReference<Lookup> myLookup = 
//        new AtomicReference<Lookup>();    
    
    private static final long serialVersionUID = 6338889116068357651L;
    
    /*public @Override Lookup getLookup() {
        return getCookieSet().getLookup();
    }*/

//    protected String getPrefixMark() {
//        return "<tasks";
//    }
    
//    protected MultiViewDescription[] getMultiViewDescriptions() {
//        return new MultiViewDescription[] {
//            new DesignerMultiViewDescription(this)
//        };
//    }
    
//     protected DesignMultiViewDesc[] getMultiViewDesc() {
//         if(taskSelectionModel == null) {
//             taskSelectionModel = new TaskSelectionModel();
//         }
//         
//        return new DesignMultiViewDesc[] {
//            new DesignerMultiViewDescription(this),
//            new WlmMapperMultiviewElementDesc(this)
////            new WLMView(this,MULTIVIEW_OVERVIEW),
////            new WLMView(this,MULTIVIEW_ASSIGNMENT),
////            new WLMView(this,MULTIVIEW_ESCALATION),
////            new WLMView(this,MULTIVIEW_TIMEOUT)
//            //new WLMView(this,MULTIVIEW_NOTIFICATION),
//            //new WLMView(this, MULTIVIEW_SECURITY)
//            //new DDView(this,"Security")
//        };
//    }
     
//    private class WLMView extends DesignMultiViewDesc implements Serializable {
//        private static final long serialVersionUID = -4224134594154669985L;
//        private String name;
//
//        WLMView() {}
//
//        WLMView(WorklistDataObject dObj,String name) {
//            super(dObj, name);
//            this.name=name;
//        }
//
//        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
//            WorklistDataObject dObj = (WorklistDataObject)getDataObject();
//            if (name.equals(MULTIVIEW_OVERVIEW)) {
//                return new OverviewMultiViewElement(dObj, 0, taskSelectionModel);
//            } else if (name.equals(MULTIVIEW_ASSIGNMENT)) {
//                return new AssignmentMultiViewElement(dObj,1, taskSelectionModel);
//            } else if (name.equals(MULTIVIEW_ESCALATION)) {
//                return new EscalationMultiViewElement(dObj,2, taskSelectionModel);
//            } else if(name.equals(MULTIVIEW_TIMEOUT)) {
//                return new TimeoutMultiViewElement(dObj,3, taskSelectionModel);
//            } else if(name.equals(MULTIVIEW_NOTIFICATION)) {
//                return new NotificationMultiViewElement(dObj,4, taskSelectionModel);
//            } 
//            return null; 
//        }
//
//        public HelpCtx getHelpCtx() {
//            if (name.equals(MULTIVIEW_OVERVIEW)) {
//                //return new HelpCtx(HELP_ID_PREFIX_OVERVIEW+"overviewNode"); //NOI18N
//            } else if (name.equals(MULTIVIEW_ASSIGNMENT)) {
//                return new HelpCtx(HELP_ID_PREFIX_ASSIGNMENT+"assignmentNode"); //NOI18N
//            } else if (name.equals(MULTIVIEW_ESCALATION)) {
//                //return new HelpCtx(HELP_ID_PREFIX_FILTERS+"filtersNode"); //NOI18N
//            } else if(name.equals(MULTIVIEW_TIMEOUT)) {
//                //return new HelpCtx(HELP_ID_PREFIX_OVERVIEW+"overviewNode"); //NOI18N
//            } else if(name.equals(MULTIVIEW_NOTIFICATION)) {
//                //return new HelpCtx(HELP_ID_PREFIX_REFERENCES+"references"); //NOI18N
//            }
//            return null;
//        }
//
//        public java.awt.Image getIcon() {
//            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"); //NOI18N
//        }
//
//        public String preferredID() {
//            return DD_MULTIVIEW_PREFIX+name;
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WorklistDataObject.class,"TTL_"+name);
//        }
//        
//         
//    }
    
//    public WLMModel getModel() {
//            if(model == null) {
//                ModelSource modelSource = Utilities.getModelSource(this.getPrimaryFile(), true);
//                if(modelSource != null) {
//                    model = WLMModelFactory.getDefault().getModel(modelSource);
//                }
//            }
//            return model;
//    }
    
//    public TaskSelectionModel getTaskSelectionModel() {
//        return this.taskSelectionModel;
//    }
    
//    public boolean isDocumentParseable() {
//        return getModel().getState() == WLMModel.State.VALID;
//    }
}
