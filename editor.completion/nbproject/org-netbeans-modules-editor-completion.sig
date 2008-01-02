#API master signature file
#Version 1.9.1
CLSS public final org.netbeans.api.editor.completion.Completion
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
meth public static org.netbeans.api.editor.completion.Completion org.netbeans.api.editor.completion.Completion.get()
meth public void org.netbeans.api.editor.completion.Completion.hideAll()
meth public void org.netbeans.api.editor.completion.Completion.hideCompletion()
meth public void org.netbeans.api.editor.completion.Completion.hideDocumentation()
meth public void org.netbeans.api.editor.completion.Completion.hideToolTip()
meth public void org.netbeans.api.editor.completion.Completion.showCompletion()
meth public void org.netbeans.api.editor.completion.Completion.showDocumentation()
meth public void org.netbeans.api.editor.completion.Completion.showToolTip()
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionDocumentation
meth public abstract java.lang.String org.netbeans.spi.editor.completion.CompletionDocumentation.getText()
meth public abstract java.net.URL org.netbeans.spi.editor.completion.CompletionDocumentation.getURL()
meth public abstract javax.swing.Action org.netbeans.spi.editor.completion.CompletionDocumentation.getGotoSourceAction()
meth public abstract org.netbeans.spi.editor.completion.CompletionDocumentation org.netbeans.spi.editor.completion.CompletionDocumentation.resolveLink(java.lang.String)
supr null
CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionItem
meth public abstract boolean org.netbeans.spi.editor.completion.CompletionItem.instantSubstitution(javax.swing.text.JTextComponent)
meth public abstract int org.netbeans.spi.editor.completion.CompletionItem.getPreferredWidth(java.awt.Graphics,java.awt.Font)
meth public abstract int org.netbeans.spi.editor.completion.CompletionItem.getSortPriority()
meth public abstract java.lang.CharSequence org.netbeans.spi.editor.completion.CompletionItem.getInsertPrefix()
meth public abstract java.lang.CharSequence org.netbeans.spi.editor.completion.CompletionItem.getSortText()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask org.netbeans.spi.editor.completion.CompletionItem.createDocumentationTask()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask org.netbeans.spi.editor.completion.CompletionItem.createToolTipTask()
meth public abstract void org.netbeans.spi.editor.completion.CompletionItem.defaultAction(javax.swing.text.JTextComponent)
meth public abstract void org.netbeans.spi.editor.completion.CompletionItem.processKeyEvent(java.awt.event.KeyEvent)
meth public abstract void org.netbeans.spi.editor.completion.CompletionItem.render(java.awt.Graphics,java.awt.Font,java.awt.Color,java.awt.Color,int,int,boolean)
supr null
CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionProvider
fld  constant public static final int org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_ALL_QUERY_TYPE
fld  constant public static final int org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE
fld  constant public static final int org.netbeans.spi.editor.completion.CompletionProvider.DOCUMENTATION_QUERY_TYPE
fld  constant public static final int org.netbeans.spi.editor.completion.CompletionProvider.TOOLTIP_QUERY_TYPE
meth public abstract int org.netbeans.spi.editor.completion.CompletionProvider.getAutoQueryTypes(javax.swing.text.JTextComponent,java.lang.String)
meth public abstract org.netbeans.spi.editor.completion.CompletionTask org.netbeans.spi.editor.completion.CompletionProvider.createTask(int,javax.swing.text.JTextComponent)
supr null
CLSS public final org.netbeans.spi.editor.completion.CompletionResultSet
fld  constant public static final int org.netbeans.spi.editor.completion.CompletionResultSet.PRIORITY_SORT_TYPE
fld  constant public static final int org.netbeans.spi.editor.completion.CompletionResultSet.TEXT_SORT_TYPE
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.editor.completion.CompletionResultSet.addAllItems(java.util.Collection)
meth public boolean org.netbeans.spi.editor.completion.CompletionResultSet.addItem(org.netbeans.spi.editor.completion.CompletionItem)
meth public boolean org.netbeans.spi.editor.completion.CompletionResultSet.isFinished()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.editor.completion.CompletionResultSet.getSortType()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.estimateItems(int,int)
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.finish()
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.setAnchorOffset(int)
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.setDocumentation(org.netbeans.spi.editor.completion.CompletionDocumentation)
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.setHasAdditionalItems(boolean)
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.setTitle(java.lang.String)
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.setToolTip(javax.swing.JToolTip)
meth public void org.netbeans.spi.editor.completion.CompletionResultSet.setWaitText(java.lang.String)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionTask
meth public abstract void org.netbeans.spi.editor.completion.CompletionTask.cancel()
meth public abstract void org.netbeans.spi.editor.completion.CompletionTask.query(org.netbeans.spi.editor.completion.CompletionResultSet)
meth public abstract void org.netbeans.spi.editor.completion.CompletionTask.refresh(org.netbeans.spi.editor.completion.CompletionResultSet)
supr null
CLSS public abstract interface org.netbeans.spi.editor.completion.LazyCompletionItem
intf org.netbeans.spi.editor.completion.CompletionItem
meth public abstract boolean org.netbeans.spi.editor.completion.CompletionItem.instantSubstitution(javax.swing.text.JTextComponent)
meth public abstract boolean org.netbeans.spi.editor.completion.LazyCompletionItem.accept()
meth public abstract int org.netbeans.spi.editor.completion.CompletionItem.getPreferredWidth(java.awt.Graphics,java.awt.Font)
meth public abstract int org.netbeans.spi.editor.completion.CompletionItem.getSortPriority()
meth public abstract java.lang.CharSequence org.netbeans.spi.editor.completion.CompletionItem.getInsertPrefix()
meth public abstract java.lang.CharSequence org.netbeans.spi.editor.completion.CompletionItem.getSortText()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask org.netbeans.spi.editor.completion.CompletionItem.createDocumentationTask()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask org.netbeans.spi.editor.completion.CompletionItem.createToolTipTask()
meth public abstract void org.netbeans.spi.editor.completion.CompletionItem.defaultAction(javax.swing.text.JTextComponent)
meth public abstract void org.netbeans.spi.editor.completion.CompletionItem.processKeyEvent(java.awt.event.KeyEvent)
meth public abstract void org.netbeans.spi.editor.completion.CompletionItem.render(java.awt.Graphics,java.awt.Font,java.awt.Color,java.awt.Color,int,int,boolean)
supr null
CLSS public abstract org.netbeans.spi.editor.completion.support.AsyncCompletionQuery
cons public AsyncCompletionQuery()
meth protected abstract void org.netbeans.spi.editor.completion.support.AsyncCompletionQuery.query(org.netbeans.spi.editor.completion.CompletionResultSet,javax.swing.text.Document,int)
meth protected boolean org.netbeans.spi.editor.completion.support.AsyncCompletionQuery.canFilter(javax.swing.text.JTextComponent)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth protected void org.netbeans.spi.editor.completion.support.AsyncCompletionQuery.filter(org.netbeans.spi.editor.completion.CompletionResultSet)
meth protected void org.netbeans.spi.editor.completion.support.AsyncCompletionQuery.preQueryUpdate(javax.swing.text.JTextComponent)
meth protected void org.netbeans.spi.editor.completion.support.AsyncCompletionQuery.prepareQuery(javax.swing.text.JTextComponent)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final boolean org.netbeans.spi.editor.completion.support.AsyncCompletionQuery.isTaskCancelled()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public final org.netbeans.spi.editor.completion.support.AsyncCompletionTask
cons public AsyncCompletionTask(org.netbeans.spi.editor.completion.support.AsyncCompletionQuery)
cons public AsyncCompletionTask(org.netbeans.spi.editor.completion.support.AsyncCompletionQuery,javax.swing.text.JTextComponent)
intf java.lang.Runnable
intf org.netbeans.spi.editor.completion.CompletionTask
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.spi.editor.completion.support.AsyncCompletionTask.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.editor.completion.support.AsyncCompletionTask.cancel()
meth public void org.netbeans.spi.editor.completion.support.AsyncCompletionTask.query(org.netbeans.spi.editor.completion.CompletionResultSet)
meth public void org.netbeans.spi.editor.completion.support.AsyncCompletionTask.refresh(org.netbeans.spi.editor.completion.CompletionResultSet)
meth public void org.netbeans.spi.editor.completion.support.AsyncCompletionTask.run()
supr java.lang.Object
CLSS public final org.netbeans.spi.editor.completion.support.CompletionUtilities
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
meth public static int org.netbeans.spi.editor.completion.support.CompletionUtilities.getPreferredWidth(java.lang.String,java.lang.String,java.awt.Graphics,java.awt.Font)
meth public static void org.netbeans.spi.editor.completion.support.CompletionUtilities.renderHtml(javax.swing.ImageIcon,java.lang.String,java.lang.String,java.awt.Graphics,java.awt.Font,java.awt.Color,int,int,boolean)
supr java.lang.Object
