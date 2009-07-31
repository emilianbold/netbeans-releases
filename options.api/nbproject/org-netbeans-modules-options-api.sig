#Signature file v4.0
#Version 1.12.1

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

CLSS public final org.netbeans.api.options.OptionsDisplayer
fld public final static java.lang.String ADVANCED = "Advanced"
meth public boolean open()
meth public boolean open(java.lang.String)
meth public static org.netbeans.api.options.OptionsDisplayer getDefault()
supr java.lang.Object
hfds INSTANCE,impl,log

CLSS public abstract org.netbeans.spi.options.AdvancedOption
cons public AdvancedOption()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getTooltip()
meth public abstract org.netbeans.spi.options.OptionsPanelController create()
supr java.lang.Object
hfds CONTROLLER,DISPLAYNAME,KEYWORDS,KEYWORDS_CATEGORY,TOOLTIP

CLSS public abstract org.netbeans.spi.options.OptionsCategory
cons public OptionsCategory()
meth public abstract java.lang.String getCategoryName()
meth public abstract java.lang.String getTitle()
meth public abstract org.netbeans.spi.options.OptionsPanelController create()
meth public java.lang.String getIconBase()
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds ADVANCEDOPTIONS_CATGEORY,CATEGORY_NAME,CONTROLLER,DESCRIPTION,ICON,KEYWORDS,KEYWORDS_CATEGORY,TITLE

CLSS public abstract org.netbeans.spi.options.OptionsPanelController
cons public OptionsPanelController()
fld public final static java.lang.String PROP_CHANGED = "changed"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_VALID = "valid"
meth protected void setCurrentSubcategory(java.lang.String)
meth public abstract boolean isChanged()
meth public abstract boolean isValid()
meth public abstract javax.swing.JComponent getComponent(org.openide.util.Lookup)
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void applyChanges()
meth public abstract void cancel()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void update()
meth public final static org.netbeans.spi.options.OptionsPanelController createAdvanced(java.lang.String)
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object

