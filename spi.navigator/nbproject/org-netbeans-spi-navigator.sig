#Signature file v4.0
#Version 1.8.1

CLSS public java.lang.Object
cons public Object()
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

CLSS public final org.netbeans.spi.navigator.NavigatorHandler
meth public static void activatePanel(org.netbeans.spi.navigator.NavigatorPanel)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorLookupHint
meth public abstract java.lang.String getContentType()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy
fld public final static int LOOKUP_HINTS_ONLY = 1
meth public abstract int getPanelsPolicy()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanel
meth public abstract java.lang.String getDisplayHint()
meth public abstract java.lang.String getDisplayName()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void panelActivated(org.openide.util.Lookup)
meth public abstract void panelDeactivated()

CLSS public abstract interface org.netbeans.spi.navigator.NavigatorPanelWithUndo
intf org.netbeans.spi.navigator.NavigatorPanel
meth public abstract org.openide.awt.UndoRedo getUndoRedo()

