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


package org.netbeans.modules.visualweb.project.jsfloader;


import org.netbeans.modules.visualweb.palette.api.CodeClipDragAndDropHandler;
import org.netbeans.modules.visualweb.palette.api.CodeClipPaletteActions;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.spi.designer.jsf.DesignerJsfServiceProvider;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInput;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.PrintCookie;
import org.openide.loaders.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.text.DataEditorSupport.Env;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

// <multiview>
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
// </multiview>

/**
 * Editor support for JSF java data objects. The one which provides
 * JSF multiiew component.
 *
 * @author Peter Zavadsky
 */
public final class JsfJavaEditorSupport extends DataEditorSupport implements EditorCookie.Observable, CloseCookie, PrintCookie {

// <multiview>
    private static final String MV_ID_DESIGNER = "designer"; // NOI18N
    private static final String MV_ID_JSP      = "jsp"; // NOI18N
    private static final String MV_ID_JAVA     = "java"; // NOI18N

    private static final int ELEMENT_INDEX_DESIGNER = 0;
    private static final int ELEMENT_INDEX_JSP      = 1;
    private static final int ELEMENT_INDEX_JAVA     = 2;

    private static final String ICON_PATH_JSF_JSP = "org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.gif"; // NOI18N

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.visualweb.project.jsfloader.JsfJavaEditorSupport");
    
    /** XXX Last created multiview, to be remembered so we can use its ref to update all clones.
     * The problem is that allEditors in EditorSupport is not the one plays the role, but the delegated one. */
    private CloneableTopComponent lastMultiView;
    
    
    /** Constructor. */
    JsfJavaEditorSupport(JsfJavaDataObject obj) {
        super(obj, new Environment(obj)); 
    }
    
    
    public void openDesigner() {
        //Bugfix #10688 open() is now run in AWT thread
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                CloneableTopComponent editor = doOpenDesigner();
                MultiViewHandler handler = MultiViews.findMultiViewHandler(editor);
                handler.requestActive(handler.getPerspectives()[ELEMENT_INDEX_DESIGNER]);
            }
        });
    }
    
    CloneableTopComponent doOpenDesigner() {
        if(!SwingUtilities.isEventDispatchThread()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Assertion failed. WindowsAPI is required to be called from AWT thread only, see " // NOI18N
                    + "http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/threading.html")); // NOI18N
        }
        
        CloneableTopComponent editor = openCloneableTopComponent();
        editor.requestActive();
        return editor;
    }
    
    /** Overriding to get java source opened as default, to match the original behaviour. */
    public void open() {
        //Bugfix #10688 open() is now run in AWT thread
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                CloneableTopComponent editor = openCloneableTopComponent();
                editor.requestActive();
                viewJavaSource(editor);
            }
        });
    }
    
    protected void openJsp() {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                CloneableTopComponent editor = openCloneableTopComponent();
                editor.requestActive();
                viewJspSource(editor);
            }
        });
    }  
    
    protected void notifyClosed() {
        super.notifyClosed();
        lastMultiView = null;
    }
    
    /** Overrides superclass method. Adds updating of display name.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified */
    protected boolean notifyModified() {
        boolean oldValue = isModified();
        boolean ret = super.notifyModified();
        
        if(ret) {
            // XXX Hacks adding save cookie, if there was only the part of jsp save cookie present.
            JsfJavaDataObject obj = (JsfJavaDataObject)getDataObject();
            if(obj.getPureCookie(SaveCookie.class) == null) {
                obj.addSaveCookie(new Save());
                obj.setModified(true);
            }
        }
        
        if(oldValue != ret) {
            updateMultiViewDisplayName();
        }
        
        return ret;
    }
    
    // XXX See above
    private class Save implements SaveCookie {
        public void save() throws IOException {
            saveDocument();
            getDataObject().setModified(false);
        }
    }
    
    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        
        // Code cloned from super
        // Correct way would be to override removeSaveCookie in JsfJavaDataObject,
        // BUT removeSaveCookie in JavaDataObject is pacage visible only, so had to
        // do this hack
        JsfJavaDataObject obj = (JsfJavaDataObject) getDataObject();
        SaveCookie save = (SaveCookie) obj.getPureCookie(SaveCookie.class);
        if (save != null) {
            obj.removeSaveCookie(save);
            obj.setModified(false);
        }
        updateMultiViewDisplayName();
    }
    
    /** @Override */
    protected void updateTitles() {
        // XXX #6486899 Hack to by pass dangerous recursion, see the issue.
        // The real problem is in insync,
        // which builds the model immediatelly when just creation of the instance is needed.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JsfJavaEditorSupport.super.updateTitles();
            }
        });
    }

    
    /** XXX Heavy hack. Replaces canClose method, used in CloseHandler. */
    private static boolean canCloseAll(JsfJspEditorSupport jsfJspEditorSupport, JsfJavaEditorSupport jsfJavaEditorSupport) {
        // 1st part taken from DataEditorSupport.
        if(jsfJavaEditorSupport.env().isModified() && jsfJavaEditorSupport.isEnvReadOnly()) {
            Object result = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(DataObject.class, "MSG_FileReadOnlyClosing",
//                        new Object[] {((Env)env).getFileImpl().getNameExt()}),
                    new Object[] {jsfJavaEditorSupport.getDataObject().getPrimaryFile().getNameExt()}),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
                    ));
            
            return result == NotifyDescriptor.OK_OPTION;
        }
        
        // 2nd part taken from CloneableEditorSupport.
        boolean jspModified = jsfJspEditorSupport.env().isModified();
        boolean javaModified = jsfJavaEditorSupport.env().isModified();
        if(jspModified || javaModified) {
            String msg = jsfJavaEditorSupport.messageSave();
            
            java.util.ResourceBundle bundle = NbBundle.getBundle(CloneableEditorSupport.class);
            
            JButton saveOption = new JButton(bundle.getString("CTL_Save")); // NOI18N
            saveOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Save")); // NOI18N
            saveOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Save")); // NOI18N
            JButton discardOption = new JButton(bundle.getString("CTL_Discard")); // NOI18N
            discardOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Discard")); // NOI18N
            discardOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Discard")); // NOI18N
            discardOption.setMnemonic(bundle.getString("CTL_Discard_Mnemonic").charAt(0)); // NOI18N
            
            NotifyDescriptor nd = new NotifyDescriptor(
                    msg,
                    bundle.getString("LBL_SaveFile_Title"),
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION},
                    saveOption
                    );
            
            Object ret = DialogDisplayer.getDefault().notify(nd);
            
            if (NotifyDescriptor.CANCEL_OPTION.equals(ret)
            || NotifyDescriptor.CLOSED_OPTION.equals(ret)) {
                return false;
            }
            
            if(saveOption.equals(ret)) {
                if(jspModified) {
                    try {
                        jsfJspEditorSupport.saveDocument();
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        return false;
                    }
                }
                if(javaModified) {
                    try {
                        jsfJavaEditorSupport.saveDocument();
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private Env env() {
        return (Env)env;
    }
    /** Indicates whether the <code>Env</code> is read only. */
    private boolean isEnvReadOnly() {
        CloneableEditorSupport.Env env = env();
//        return env instanceof Env && ((Env)env).getFileImpl().isReadOnly();
        return env instanceof Env && getDataObject().getPrimaryFile().isReadOnly();
    }
    
    // XXX PROBLEM NB didn't solve the issues we asked for(NB #59046, #59043), that
    // way there is no real support for multiviews containing two or more editors
    // which is our case, and also there seems to be no way to hack it (like this overriding).
//    /** XXX Overriding superclass to select the correct tab in multiview. */
//    protected Pane openAt(final org.openide.text.PositionRef pos, final int column) {
//        Pane pane = super.openAt(pos, column);
//        if(pane instanceof TopComponent) {
//            viewJavaSource((TopComponent)pane);
//        }
//        return pane;
//    }
    
    
// <multiview>
    
    /** Gets called if jsp editor is opened first via EditCookie. */
    protected CloneableEditorSupport.Pane createPane() {
        JsfJavaDataObject jsfJavaDataObject = (JsfJavaDataObject)getDataObject();
        JsfJspDataObject jsfJspDataObject = Utils.findCorrespondingJsfJspDataObject(jsfJavaDataObject.getPrimaryFile(), false);
        
        if(jsfJspDataObject == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Can't find jsp data object for " + getDataObject())); // NOI18N
            return super.createPane();
        }
        
        MultiViewDescription[] descs = new MultiViewDescription[] {
            new DesignerDesc(jsfJspDataObject),
            new JspDesc(jsfJspDataObject),
            new JavaDesc(jsfJavaDataObject)
        };
        
        CloneableTopComponent mvtc =
                MultiViewFactory.createCloneableMultiView(
                descs,
                descs[ELEMENT_INDEX_DESIGNER],
                new CloseHandler(jsfJavaDataObject.getPrimaryFile()));
        
        lastMultiView = mvtc;
        
        // Update display name and tooltip.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateMultiViewDisplayName();
                updateMultiViewToolTip();
            }
        });
        
        // #45665 - dock into editor mode if possible..
        Mode editorMode = WindowManager.getDefault().findMode(EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(mvtc);
        }
        return (CloneableEditorSupport.Pane)mvtc;
    }
    
    void updateMultiViewDisplayName() {
        if(SwingUtilities.isEventDispatchThread()) {
            doUpdateMultiViewDisplayName();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doUpdateMultiViewDisplayName();
                }
            });
        }
    }
    
    private void doUpdateMultiViewDisplayName() {
        FileObject jspFileObject = Utils.findJspForJava(getDataObject().getPrimaryFile());
        if(jspFileObject == null || !jspFileObject.isValid()) {
            return;
        }
        
        String displayName;
        JsfJspEditorSupport jsfJspEditorSupport = Utils.findCorrespondingJsfJspEditorSupport(getDataObject().getPrimaryFile(), true);
        if(jsfJspEditorSupport == null) {
            // # 6248373 Probably deleted already.
            return;
        } else {
            displayName = jsfJspEditorSupport.messageName();
        }
        
        Enumeration en = getMultiViews();
        while(en.hasMoreElements()) {
            TopComponent tc = (TopComponent)en.nextElement();
            tc.setDisplayName(displayName);
        }
    }
    
    void updateMultiViewToolTip() {
        if(SwingUtilities.isEventDispatchThread()) {
            doUpdateMultiViewToolTip();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doUpdateMultiViewToolTip();
                }
            });
        }
    }
    
    private void doUpdateMultiViewToolTip() {
        String toolTip;
        JsfJspEditorSupport jsfJspEditorSupport = Utils.findCorrespondingJsfJspEditorSupport(getDataObject().getPrimaryFile(), true);
        if(jsfJspEditorSupport == null) {
            return;
        } else {
            toolTip = jsfJspEditorSupport.messageToolTip();
        }
        
        Enumeration en = getMultiViews();
        while(en.hasMoreElements()) {
            TopComponent tc = (TopComponent)en.nextElement();
            tc.setToolTipText(toolTip);
        }
    }
    
    /** Gets currently associated multivies, helper method. */
    private Enumeration getMultiViews() {
        CloneableTopComponent ctc = lastMultiView;
        if(ctc == null) {
            return Collections.enumeration(Collections.EMPTY_SET);
        } else {
            return ctc.getReference().getComponents();
        }
    }
    
// Helper view methods
    private void viewJspSource(final TopComponent jspEditor) {
        if(SwingUtilities.isEventDispatchThread()) {
            doViewJspSource(jspEditor);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doViewJspSource(jspEditor);
                }
            });
        }
    }
    
    private void doViewJspSource(TopComponent jspEditor) {
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(jspEditor);
        if(mvh != null) {
            MultiViewPerspective[] mvps = mvh.getPerspectives();
            for(int i = 0; i < mvps.length; i++) {
                if(MV_ID_JSP.equals(mvps[i].preferredID())) {
                    mvh.requestActive(mvps[i]);
                    break;
                }
            }
        }
    }
    
    private void viewJavaSource(final TopComponent javaEditor) {
        if(SwingUtilities.isEventDispatchThread()) {
            doViewJavaSource(javaEditor);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doViewJavaSource(javaEditor);
                }
            });
        }
    }
    
    private void doViewJavaSource(TopComponent editor) {
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(editor);
        if(mvh != null) {
            MultiViewPerspective[] mvps = mvh.getPerspectives();
            for(int i = 0; i < mvps.length; i++) {
                if(MV_ID_JAVA.equals(mvps[i].preferredID())) {
                    mvh.requestActive(mvps[i]);
                    break;
                }
            }
        }
    }
    
    private void setLastMultiViewFromChild(Component childComponent) {
        if (childComponent == null) {
            return;
        }
        
        CloneableTopComponent ctc = (CloneableTopComponent)SwingUtilities.getAncestorOfClass(CloneableTopComponent.class, childComponent);
        if (ctc != null) {
            lastMultiView = ctc;
        }
    }
    
// End of helper view methods.
    
    
// <multiview>
    /** A descriptor for the designer editor as an element in multiview. */
    private static class DesignerDesc implements MultiViewDescription, Serializable {
        
        private static final long serialVersionUID =-3126744316624172415L;
        
        private JsfJspDataObject jsfJspDataObject;
        
        
        public DesignerDesc(JsfJspDataObject jsfJspDataObject) {
            this.jsfJspDataObject = jsfJspDataObject;
        }
        
        
        public MultiViewElement createElement() {
//            final MultiViewElement multiViewElement =  DesignerServiceHack.getDefault().
//                    getMultiViewElementForDataObject(jsfJspDataObject);
            final MultiViewElement multiViewElement = DesignerJsfServiceProvider.getDesignerJsfService().createDesignerMultiViewElement(jsfJspDataObject);
            
            if(multiViewElement != null) {
                // XXX #6357375 Setting the last multiview, because after deserialization,
                // there seems to be no other way how to get it.
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JsfJavaEditorSupport jsfJavaEditorSupport = Utils.findCorrespondingJsfJavaEditorSupport(jsfJspDataObject.getPrimaryFile(), false);
                        if (jsfJavaEditorSupport != null) {
                            jsfJavaEditorSupport.setLastMultiViewFromChild(multiViewElement.getVisualRepresentation());
                            // #6462651
                            jsfJavaEditorSupport.updateMultiViewDisplayName();
                            jsfJavaEditorSupport.updateMultiViewToolTip();
                        }
                    }
                });
                
                return multiViewElement;
            }
//                WebForm webForm = jsfJspDataObject.getWebForm();
//                webForm.createTopComponent(jsfJspEditorSupport);
//                return webForm.getMultiViewElement();
            return MultiViewFactory.BLANK_ELEMENT;
        }
        
        public String preferredID() {
            return MV_ID_DESIGNER;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        public java.awt.Image getIcon() {
            return Utilities.loadImage(ICON_PATH_JSF_JSP);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(JsfJavaEditorSupport.class, "CTL_DesignerTabCaption"); // NOI18N
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }
    }
    
    /** A descriptor for the jsp editor as an element in multiview. */
    private static class JspDesc implements MultiViewDescription, Serializable {
        
        private static final long serialVersionUID =-3126744316624172415L;
        
        private JsfJspDataObject jsfJspDataObject;
        
        
        public JspDesc(JsfJspDataObject jsfJspDataObject) {
            this.jsfJspDataObject = jsfJspDataObject;
        }
        
        public MultiViewElement createElement() {
            JsfJspEditorSupport jsfJspEditorSupport = (JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class);
            if(jsfJspEditorSupport != null) {
                jsfJspEditorSupport.prepareDocument();
                final MultiViewElement multiViewElement = jsfJspEditorSupport.createMultiViewElement();
                
                // XXX #6357375 Setting the last multiview, because after deserialization,
                // there seems to be no other way how to get it.
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JsfJavaEditorSupport jsfJavaEditorSupport = Utils.findCorrespondingJsfJavaEditorSupport(jsfJspDataObject.getPrimaryFile(), false);
                        if (jsfJavaEditorSupport != null) {
                            jsfJavaEditorSupport.setLastMultiViewFromChild((Component)multiViewElement);
                            // #6462651.
                            jsfJavaEditorSupport.updateMultiViewDisplayName();
                            jsfJavaEditorSupport.updateMultiViewToolTip();
                        }
                    }
                });
                
                return multiViewElement;
            }
            return MultiViewFactory.BLANK_ELEMENT;
        }
        
        public String preferredID() {
            return MV_ID_JSP;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        public java.awt.Image getIcon() {
            return Utilities.loadImage(ICON_PATH_JSF_JSP);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(JsfJavaEditorSupport.class, "CTL_JspTabCaption"); // NOI18N
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
        
    }
    
    /** A descriptor for the java editor as an element in multiview. */
    private static class JavaDesc implements MultiViewDescription, Serializable {
        
        private static final long serialVersionUID =-3126744316624172415L;

        private JsfJavaDataObject jsfJavaDataObject;
        
        
        public JavaDesc(JsfJavaDataObject jsfJavaDataObject) {
            this.jsfJavaDataObject = jsfJavaDataObject;
        }
        
        public MultiViewElement createElement() {
            DataEditorSupport javaEditor = (DataEditorSupport)jsfJavaDataObject.getCookie(DataEditorSupport.class);
            if(javaEditor != null) {
                javaEditor.prepareDocument();
                final MultiViewElement multiViewElement = new JavaEditorTopComponent(javaEditor);
                
                // XXX #6357375 Setting the last multiview, because after deserialization,
                // there seems to be no other way how to get it.
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JsfJavaEditorSupport jsfJavaEditorSupport = (JsfJavaEditorSupport)jsfJavaDataObject.getCookie(JsfJavaEditorSupport.class);
                        if (jsfJavaEditorSupport != null) {
                            jsfJavaEditorSupport.setLastMultiViewFromChild((Component)multiViewElement);
                            // #6462651.
                            jsfJavaEditorSupport.updateMultiViewDisplayName();
                            jsfJavaEditorSupport.updateMultiViewToolTip();
                        }
                    }
                });
                
                return multiViewElement;
            }
            return MultiViewFactory.BLANK_ELEMENT;
        }
        
        public String preferredID() {
            return MV_ID_JAVA;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        public java.awt.Image getIcon() {
            return Utilities.loadImage(ICON_PATH_JSF_JSP);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(JsfJavaEditorSupport.class, "CTL_JavaTabCaption"); // NOI18N
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
        
    }
    
    private static class JavaEditorTopComponent extends CloneableEditor
            implements MultiViewElement, CloneableEditorSupport.Pane {
        private static final long serialVersionUID =-3126744316624172415L;
        private static final NavigatorLookupHint NAVIGATOR_HINT =
                new NavigatorLookupHint() {
            public String getContentType() {
                return "text/x-java"; // NOI18N
            }
        };
        
        private transient JComponent toolbar;
        
        private transient MultiViewElementCallback multiViewObserver;
        
        private PaletteController javaPaletteController;
        
        
        JavaEditorTopComponent() {
            super();
        }
        
        JavaEditorTopComponent(CloneableEditorSupport ces) {
            super(ces);
            initialize();
        }
        
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            //required to do this to make sure cloneableEditorSupport is deserialized.
            super.readExternal(in);
            initialize();
        }
        
        private void initialize() {
            DataObject jsfJavaDataObject = ((JsfJavaEditorSupport)cloneableEditorSupport()).getDataObject();
            if(jsfJavaDataObject != null) {
                setActivatedNodes(new Node[] {jsfJavaDataObject.getNodeDelegate()});
            }
            
            initializePalette();
        }

        // XXX PaletteController
        private void initializePalette() {
            

            JsfJavaEditorSupport javaES = (JsfJavaEditorSupport)cloneableEditorSupport();
            FileObject primaryFile = javaES.getDataObject().getPrimaryFile();
            Project project = FileOwnerQuery.getOwner(primaryFile);
            
            String paletteDirectory;
            if ( JsfProjectUtils.JAVA_EE_5.equals(JsfProjectUtils.getJ2eePlatformVersion(project))) {
                paletteDirectory = "CreatorJavaPalette5";
            } else {
                //Later to be renamed with a 1.4
                paletteDirectory = "CreatorJavaPalette";
            }    
            
//            JsfJavaEditorSupport javaES = (JsfJavaEditorSupport)cloneableEditorSupport();
//            JsfJspEditorSupport jes = Utils.findCorrespondingJsfJspEditorSupport(javaES.getDataObject().getPrimaryFile(), true);
//            JsfJspEditorSupport jes = Utils.findCorrespondingJsfJspEditorSupport(javaES.getDataObject().getPrimaryFile(), true);

//            String paletteFolderName = "CreatorJavaPalette";
            PaletteController controller;
            try {
                controller = PaletteFactory.createPalette(paletteDirectory, new CodeClipPaletteActions(paletteDirectory, this), null, new CodeClipDragAndDropHandler()); // NOI18N
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                controller = null;
//                System.out.println("Java Palette is null." );
            }
            javaPaletteController = controller;
            return;
            
        }
        
        
        public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                JEditorPane pane = getEditorPane();
                if (pane != null) {
                    Document doc = pane.getDocument();
                    if (doc instanceof NbDocument.CustomToolbar) {
                        toolbar = ((NbDocument.CustomToolbar)doc).createToolbar(pane);
                    }
                }
                if (toolbar == null) {
                    // attempt to create own toolbar??
                    toolbar = new JPanel();
                }
            }
            return toolbar;
        }
        
        public JComponent getVisualRepresentation() {
            return this;
        }
        
        public void componentDeactivated() {
            super.componentDeactivated();
        }
        
        public void componentActivated() {
            super.componentActivated();
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            multiViewObserver = callback;
            
//            // needed for deserialization...
//// XXX This smells really badly, is this supposed to be the 'typical' way to
//// deserialize the needed stuff? (comes from FormEditorSupport)
//            JsfJavaEditorSupport jsfJavaEditorSupport = (JsfJavaEditorSupport)jsfJavaDataObject.getCookie(JsfJavaEditorSupport.class);
//            if(jsfJavaEditorSupport != null) {
//                // this is used (or misused?) to obtain the deserialized
//                // multiview topcomponent and set it to JsfJavaEditorSupport
//                jsfJavaEditorSupport.setMultiView((CloneableTopComponent)multiViewElementCallback.getTopComponent());
//            }
            
        }
        
        public void requestVisible() {
            if (multiViewObserver != null)
                multiViewObserver.requestVisible();
            else
                super.requestVisible();
        }
        
        public void requestActive() {
            if (multiViewObserver != null)
                multiViewObserver.requestActive();
            else
                super.requestActive();
        }
        
        public void componentClosed() {
            super.componentClosed();
        }
        
        public void componentShowing() {
            super.componentShowing();
        }
        
        public void componentHidden() {
            super.componentHidden();
        }
        
        public void componentOpened() {
            super.componentOpened();
        }
        
        public void updateName() {
            super.updateName();
        }
        
        protected boolean closeLast() {
            return true;
        }
        
        public CloseOperationState canCloseElement() {
            // if this is not the last cloned java editor component, closing is OK
            if (!isLastView(multiViewObserver.getTopComponent()))
                return CloseOperationState.STATE_OK;
            
            // return a placeholder state - to be sure our CloseHandler is called
            return MultiViewFactory.createUnsafeCloseState(
                    "ID_CLOSING_JAVA", // dummy ID // NOI18N
                    MultiViewFactory.NOOP_CLOSE_ACTION,
                    MultiViewFactory.NOOP_CLOSE_ACTION);
        }
        
        private static boolean isLastView(TopComponent tc) {
            if (!(tc instanceof CloneableTopComponent))
                return false;
            
            boolean oneOrLess = true;
            Enumeration en = ((CloneableTopComponent)tc).getReference().getComponents();
            if (en.hasMoreElements()) {
                en.nextElement();
                if (en.hasMoreElements())
                    oneOrLess = false;
            }
            return oneOrLess;
        }
        
        public Action[] getActions() {
            // need to delegate to multiview's actions because of the way editor
            // constructs actions : NbEditorKit.NbBuildPopupMenuAction
            return multiViewObserver != null ?
                multiViewObserver.createDefaultActions() : super.getActions();
        }
        
        protected boolean isActiveTC() {
            TopComponent selected = getRegistry().getActivated();
            
            if (selected == null)
                return false;
            if (selected == this)
                return true;
            
            MultiViewHandler handler = MultiViews.findMultiViewHandler(selected);
            if (handler != null
                    && MV_ID_JAVA.equals(handler.getSelectedPerspective()
                    .preferredID()))
                return true;
            
            return false;
        }
        
        // XXX Overrides superclass, to fake the display name of the JSF editor,
        // in order to prevent overriding of the multiview display name based on multiview SPI impl.
        // see NB #57035
        public String getDisplayName() {
            JsfJavaEditorSupport javaES = (JsfJavaEditorSupport)cloneableEditorSupport();
            JsfJspEditorSupport jes = Utils.findCorrespondingJsfJspEditorSupport(javaES.getDataObject().getPrimaryFile(), true);
            if(jes == null) {
                return null;
            } else {
                return jes.messageName();
            }
        }
        
        private WeakReference lookupWRef = new WeakReference(null);
        
        /** Adds <code>NavigatorLookupHint</code> into the original lookup,
         * for the navigator. */
        public Lookup getLookup() {
            Lookup lookup = (Lookup)lookupWRef.get();
            
            if (lookup == null) {
                Lookup superLookup = super.getLookup();
                if (javaPaletteController == null) {
                    lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.singleton(NAVIGATOR_HINT)});
                } else {
                    DataObject dObj = ((JsfJavaEditorSupport)cloneableEditorSupport()).getDataObject();
                    lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(NAVIGATOR_HINT, javaPaletteController)});
                }
                lookupWRef = new WeakReference(lookup);
            }
            return lookup;
        }
    }
    
// XXX This is a bit strange class? What is a reason of that?
    /** Implementation of CloseOperationHandler for multiview. Ensures both form
     * and java editor are correctly closed, data saved, etc. Holds a reference
     * to jsf DataObject only - to be serializable with the multiview
     * TopComponent without problems.
     */
    private static class CloseHandler implements CloseOperationHandler, Serializable {
        private static final long serialVersionUID =-3126744315424172415L;
        
// XXX During deserialization of TopComponent, there might not be recognized the
// JsfJavaDataObject, because the project infra may not be inited yet, thus instead
// of the JsfJavaDataObject it would fall back to JspDataObject.
// Using trick, to operate over the primary file only.
//        private JsfJavaDataObject jsfJavaDataObject;
        private FileObject primaryJsfFileObject;
        
        private CloseHandler() {
        }
        
        public CloseHandler(FileObject primaryJsfFileObject) {
            this.primaryJsfFileObject = primaryJsfFileObject;
        }
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            if(!primaryJsfFileObject.isValid()) {
                // It's not valid, probably deleted already, close it.
                return true;
            }
            
            DataObject dobj;
            try {
                dobj = DataObject.find(primaryJsfFileObject);
            } catch(DataObjectNotFoundException dnfe) {
// EAT: The notification of the close is processed AFTER the file is actually deleted, so this error should not
// be an error ?
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
                return true;
            }
            JsfJavaDataObject jsfJavaDataObject;
            if(dobj instanceof JsfJavaDataObject) {
                jsfJavaDataObject = (JsfJavaDataObject)dobj;
            } else {
//              EAT: The notification of the close is processed AFTER the file is actually deleted, so this error should not
//              be an error ?
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
//                    "JsfJavaDataObject was not found for primary file=" + primaryJsfFileObject + ", instead was found=" + dobj)); // NOI18N
                return true;
            }
            JsfJavaEditorSupport jsfJavaEditorSupport = (JsfJavaEditorSupport)jsfJavaDataObject.getCookie(JsfJavaEditorSupport.class);
            
            JsfJspDataObject jsfJspDataObject;
            JsfJspEditorSupport jsfJspEditorSupport;
            jsfJspDataObject = Utils.findCorrespondingJsfJspDataObject(jsfJavaDataObject.getPrimaryFile(), true);
            if(jsfJspDataObject == null) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                //    new IllegalStateException("Can't find jsp data object for " + jsfJavaDataObject)); // NOI18N
                jsfJspEditorSupport = null;
            } else {
                jsfJspEditorSupport = (JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class);
            }
            
            boolean ret;
            if(jsfJavaEditorSupport != null && jsfJspEditorSupport != null) {
                ret = canCloseAll(jsfJspEditorSupport, jsfJavaEditorSupport);
            } else {
                // Some problem occured, log it?
                ret = true;
            }
            
            // #6338212 We have to call notifyClose only when the last component
            // is about to be closed, otherwise the webform (performance stuff), gets cleared.
            // Also that is the correct handling of the notifyClose API.
            boolean closingLast;
            if(jsfJavaEditorSupport != null) {
                int i = 0;
                for (Enumeration en = jsfJavaEditorSupport.getMultiViews(); en.hasMoreElements(); ) {
                    en.nextElement();
                    i++;
                }
                if (i > 1) {
                    closingLast = false;
                } else {
                    closingLast = true;
                }
            } else {
                closingLast = true;
            }
            
            // XXX #6182333 Closing document. Otherwise it is not handled, because of the
            // strange impl via original CloneableTopComponent and CloneableEditor.
            if(ret && closingLast) {
                if (jsfJspEditorSupport != null)
                    jsfJspEditorSupport.notifyClosed();
                if (jsfJavaEditorSupport != null)
                    jsfJavaEditorSupport.notifyClosed();
            }
            
            return ret;
        }
    }
// </multiview>
    
    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = 3035543168452715818L;
        
        /** Constructor. */
        public Environment(JsfJavaDataObject obj) {
            super(obj);
        }
        
        
        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            return getFile().lock();
        }
    } // End of nested Environment class.
}

