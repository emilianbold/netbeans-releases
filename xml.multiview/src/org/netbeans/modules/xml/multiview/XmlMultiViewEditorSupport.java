/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.RequestProcessor;
import org.openide.windows.*;

/**
 * XmlMultiviewEditorSupport.java
 *
 * Created on October 5, 2004, 10:46 AM
 * @author  mkuchtiak
 */
public class XmlMultiViewEditorSupport extends DataEditorSupport implements EditCookie, OpenCookie, EditorCookie.Observable, PrintCookie {
    
    private XmlMultiViewDataObject dObj;
    private int xmlMultiViewIndex;
    private static final int PARSING_DELAY = 2000;
    private static final int PARSING_INIT_DELAY = 100;
    private RequestProcessor.Task parsingDocumentTask;
    
    final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws java.io.IOException {
            XmlMultiViewEditorSupport.this.saveDocument();
            XmlMultiViewEditorSupport.this.getDataObject().setModified(false);
        }
    };
    
    /** Creates a new instance of XmlMultiviewEditorSupport */
    public XmlMultiViewEditorSupport(XmlMultiViewDataObject dObj) {
        super (dObj, new XmlEnv (dObj));
        this.dObj=dObj;

        // Set a MIME type as needed, e.g.:
        setMIMEType ("text/xml");   // NOI18
    }
    
    /** Restart the timer which starts source parsing after the specified delay.
    */
    public void restartTimer() {
        Runnable r = new Runnable() {
            public void run() {
                dObj.updateModelFromSource();
            }
	};
        if (parsingDocumentTask==null || parsingDocumentTask.isFinished() || 
            parsingDocumentTask.cancel()) {
            parsingDocumentTask = RequestProcessor.getDefault().post(r,PARSING_INIT_DELAY);                 
        } else {
            parsingDocumentTask = RequestProcessor.getDefault().post(r,PARSING_DELAY);             
        }
    } 
    /** 
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    protected boolean notifyModified () {
        if (!super.notifyModified()) 
            return false;
        addSaveCookie();
        if (!dObj.isChangedFromUI()) restartTimer();
        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified () {
        super.notifyUnmodified();

        removeSaveCookie();
    }

    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        XmlMultiViewDataObject obj = (XmlMultiViewDataObject)getDataObject();

        // Adds save cookie to the data object.
        if(obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        XmlMultiViewDataObject obj = (XmlMultiViewDataObject)getDataObject();
        
        // Remove save cookie from the data object.
        org.openide.nodes.Node.Cookie cookie = obj.getCookie(SaveCookie.class);

        if(cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }
    
    protected CloneableTopComponent createCloneableTopComponent() {
        MultiViewDescription[] customDesc = dObj.getMultiViewDesc();
        MultiViewDescription xmlDesc = new XmlMultiViewEditorSupport.XmlViewDesc (super.createCloneableTopComponent(),this);
        MultiViewDescription[] descs = new MultiViewDescription[customDesc.length+1];
        for (int i=0;i<customDesc.length;i++) descs[i]=customDesc[i];
        descs[customDesc.length]=xmlDesc;
        xmlMultiViewIndex=customDesc.length;

        CloneableTopComponent mvtc = MultiViewFactory.createCloneableMultiView(descs, descs[0],
                new CloseOperationHandler() {
                    public boolean resolveCloseOperation(CloseOperationState[] elements) {
                        if (elements != null) {
                            for (int i = 0; i < elements.length; i++) {
                                if (!elements[i].canClose()) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
        
        // #45665 - dock into editor mode if possible..
        Mode editorMode = WindowManager.getDefault().findMode(org.openide.text.CloneableEditorSupport.EDITOR_MODE);

        if (editorMode != null) {
            editorMode.dockInto(mvtc);
        }
        mvtc.setDisplayName(dObj.getDisplayName());
        mvtc.setIcon(org.openide.util.Utilities.loadImage(dObj.getIconBase()+".gif"));
        return mvtc;
    }


    /** Focuses existing component to view, or if none exists creates new.
    * The default implementation simply calls {@link #open}.
    * @see org.openide.cookies.EditCookie#edit
    */
    public void edit () {
        if (java.awt.EventQueue.isDispatchThread()) {
            openInAWT(-1);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    openInAWT(-1);
                }
            });
        }
    }
    
    /** Overrides superclass method
     */
    public void open() {
        if (java.awt.EventQueue.isDispatchThread()) {
            openInAWT(0);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    openInAWT(0);
                }
            });
        }
    }
    
    private void openInAWT(int index) {
        CloneableTopComponent mvtc = openCloneableTopComponent();
        mvtc.requestActive();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
        handler.requestActive(handler.getPerspectives()[index<0?xmlMultiViewIndex:index]);
    }
    
    /** A description of the binding between the editor support and the object.
     * Note this may be serialized as part of the window system and so
     * should be static, and use the transient modifier where needed.
     */
    private static class XmlEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1882981960507292985L;

        /** Create a new environment based on the data object.
         * @param obj the data object to edit
         */
        public XmlEnv (XmlMultiViewDataObject obj) {
            super (obj);
        }

        /** Get the file to edit.
         * @return the primary file normally
         */
        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }

        /** Lock the file to edit.
         * Should be taken from the file entry if possible, helpful during
         * e.g. deletion of the file.
         * @return a lock on the primary file normally
         * @throws IOException if the lock could not be taken
         */
        protected FileLock takeLock () throws java.io.IOException {
            return ((XmlMultiViewDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
        }

        /** Find the editor support this environment represents.
         * Note that we have to look it up, as keeping a direct
         * reference would not permit this environment to be serialized.
         * @return the editor support
         */
        public CloneableOpenSupport findCloneableOpenSupport () {
            return (XmlMultiViewEditorSupport) getDataObject ().getCookie (XmlMultiViewEditorSupport.class);
        }
    }
    
    private class XmlViewDesc implements MultiViewDescription, java.io.Serializable  {
        
        private static final long serialVersionUID = 8085725367398466167L;
        TopComponent tc;
        XmlMultiViewEditorSupport support;
        
        XmlViewDesc(TopComponent tc, XmlMultiViewEditorSupport support) {
            this.tc=tc;
            this.support=support;
        }
        
        public MultiViewElement createElement() {
            return new XmlMultiViewElement(tc, support);
        }
        
        public String getDisplayName() {
            return "XML";
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return null;
        }
        
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/xml/multiview/resources/xmlObject.gif");
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }
        
        public String preferredID() {
            return "multiview_xml";
        }
    }
}