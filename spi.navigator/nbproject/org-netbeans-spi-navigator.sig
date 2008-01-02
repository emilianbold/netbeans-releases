#API master signature file
#Version 1.5.1
CLSS public final org.netbeans.spi.navigator.NavigatorHandler
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public static void org.netbeans.spi.navigator.NavigatorHandler.activatePanel(org.netbeans.spi.navigator.NavigatorPanel)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.navigator.NavigatorLookupHint
meth public abstract java.lang.String org.netbeans.spi.navigator.NavigatorLookupHint.getContentType()
supr null
CLSS public abstract interface org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy
fld  constant public static final int org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY
meth public abstract int org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy.getPanelsPolicy()
supr null
CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanel
meth public abstract java.lang.String org.netbeans.spi.navigator.NavigatorPanel.getDisplayHint()
meth public abstract java.lang.String org.netbeans.spi.navigator.NavigatorPanel.getDisplayName()
meth public abstract javax.swing.JComponent org.netbeans.spi.navigator.NavigatorPanel.getComponent()
meth public abstract org.openide.util.Lookup org.netbeans.spi.navigator.NavigatorPanel.getLookup()
meth public abstract void org.netbeans.spi.navigator.NavigatorPanel.panelActivated(org.openide.util.Lookup)
meth public abstract void org.netbeans.spi.navigator.NavigatorPanel.panelDeactivated()
supr null
CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanelWithUndo
intf org.netbeans.spi.navigator.NavigatorPanel
meth public abstract java.lang.String org.netbeans.spi.navigator.NavigatorPanel.getDisplayHint()
meth public abstract java.lang.String org.netbeans.spi.navigator.NavigatorPanel.getDisplayName()
meth public abstract javax.swing.JComponent org.netbeans.spi.navigator.NavigatorPanel.getComponent()
meth public abstract org.openide.awt.UndoRedo org.netbeans.spi.navigator.NavigatorPanelWithUndo.getUndoRedo()
meth public abstract org.openide.util.Lookup org.netbeans.spi.navigator.NavigatorPanel.getLookup()
meth public abstract void org.netbeans.spi.navigator.NavigatorPanel.panelActivated(org.openide.util.Lookup)
meth public abstract void org.netbeans.spi.navigator.NavigatorPanel.panelDeactivated()
supr null
