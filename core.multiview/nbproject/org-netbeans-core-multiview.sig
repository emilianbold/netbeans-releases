#Signature file v4.1
#Version 1.22

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.core.api.multiview.MultiViewHandler
meth public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective()
meth public org.netbeans.core.api.multiview.MultiViewPerspective[] getPerspectives()
meth public void requestActive(org.netbeans.core.api.multiview.MultiViewPerspective)
meth public void requestVisible(org.netbeans.core.api.multiview.MultiViewPerspective)
supr java.lang.Object
hfds del

CLSS public final org.netbeans.core.api.multiview.MultiViewPerspective
meth public int getPersistenceType()
meth public java.awt.Image getIcon()
meth public java.lang.String getDisplayName()
meth public java.lang.String preferredID()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object
hfds description

CLSS public final org.netbeans.core.api.multiview.MultiViews
meth public static org.netbeans.core.api.multiview.MultiViewHandler findMultiViewHandler(org.openide.windows.TopComponent)
supr java.lang.Object

CLSS public abstract interface org.netbeans.core.spi.multiview.CloseOperationHandler
meth public abstract boolean resolveCloseOperation(org.netbeans.core.spi.multiview.CloseOperationState[])

CLSS public final org.netbeans.core.spi.multiview.CloseOperationState
fld public final static org.netbeans.core.spi.multiview.CloseOperationState STATE_OK
meth public boolean canClose()
meth public java.lang.String getCloseWarningID()
meth public javax.swing.Action getDiscardAction()
meth public javax.swing.Action getProceedAction()
supr java.lang.Object
hfds canClose,discardAction,id,proceedAction

CLSS public abstract interface org.netbeans.core.spi.multiview.MultiViewDescription
meth public abstract int getPersistenceType()
meth public abstract java.awt.Image getIcon()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String preferredID()
meth public abstract org.netbeans.core.spi.multiview.MultiViewElement createElement()
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract interface org.netbeans.core.spi.multiview.MultiViewElement
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.JComponent getToolbarRepresentation()
meth public abstract javax.swing.JComponent getVisualRepresentation()
meth public abstract org.netbeans.core.spi.multiview.CloseOperationState canCloseElement()
meth public abstract org.openide.awt.UndoRedo getUndoRedo()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void componentActivated()
meth public abstract void componentClosed()
meth public abstract void componentDeactivated()
meth public abstract void componentHidden()
meth public abstract void componentOpened()
meth public abstract void componentShowing()
meth public abstract void setMultiViewCallback(org.netbeans.core.spi.multiview.MultiViewElementCallback)

CLSS public final org.netbeans.core.spi.multiview.MultiViewElementCallback
meth public boolean isSelectedElement()
meth public javax.swing.Action[] createDefaultActions()
meth public org.openide.windows.TopComponent getTopComponent()
meth public void requestActive()
meth public void requestVisible()
meth public void updateTitle(java.lang.String)
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.core.spi.multiview.MultiViewFactory
fld public final static javax.swing.Action NOOP_CLOSE_ACTION
fld public final static org.netbeans.core.spi.multiview.MultiViewElement BLANK_ELEMENT
meth public static org.netbeans.core.spi.multiview.CloseOperationState createUnsafeCloseState(java.lang.String,javax.swing.Action,javax.swing.Action)
meth public static org.openide.windows.CloneableTopComponent createCloneableMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription)
meth public static org.openide.windows.CloneableTopComponent createCloneableMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription,org.netbeans.core.spi.multiview.CloseOperationHandler)
meth public static org.openide.windows.TopComponent createMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription)
meth public static org.openide.windows.TopComponent createMultiView(org.netbeans.core.spi.multiview.MultiViewDescription[],org.netbeans.core.spi.multiview.MultiViewDescription,org.netbeans.core.spi.multiview.CloseOperationHandler)
supr java.lang.Object
hcls Blank,DefaultCloseHandler,NoopAction

CLSS public abstract interface org.netbeans.core.spi.multiview.SourceViewMarker

