#API master signature file
#Version 1.5.1
CLSS public static abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter$Factory
meth public abstract org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter$Factory.createFilter(javax.swing.text.JTextComponent,int)
supr null
CLSS public final org.netbeans.lib.editor.codetemplates.api.CodeTemplate
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.lib.editor.codetemplates.api.CodeTemplate.getAbbreviation()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.api.CodeTemplate.getDescription()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.api.CodeTemplate.getParametrizedText()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.api.CodeTemplate.toString()
meth public java.util.List org.netbeans.lib.editor.codetemplates.api.CodeTemplate.getContexts()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.lib.editor.codetemplates.api.CodeTemplate.insert(javax.swing.text.JTextComponent)
supr java.lang.Object
CLSS public final org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager.isLoaded()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Collection org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager.getCodeTemplates()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.lib.editor.codetemplates.api.CodeTemplate org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager.createTemporary(java.lang.String)
meth public static org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager.get(javax.swing.text.Document)
meth public void org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager.registerLoadedListener(javax.swing.event.ChangeListener)
meth public void org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager.waitLoaded()
supr java.lang.Object
CLSS public abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter
innr public static abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter$Factory
meth public abstract boolean org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter.accept(org.netbeans.lib.editor.codetemplates.api.CodeTemplate)
supr null
CLSS public final org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.isInserted()
meth public boolean org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.isReleased()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getInsertTextOffset()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getInsertText()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getParametrizedText()
meth public java.util.List org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getAllParameters()
meth public java.util.List org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getMasterParameters()
meth public javax.swing.text.JTextComponent org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getComponent()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.lib.editor.codetemplates.api.CodeTemplate org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getCodeTemplate()
meth public org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.getMasterParameter(java.lang.String)
meth public void org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest.setParametrizedText(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter
fld  constant public static final java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.COMPLETION_INVOKE_HINT_NAME
fld  constant public static final java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.CURSOR_PARAMETER_NAME
fld  constant public static final java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.DEFAULT_VALUE_HINT_NAME
fld  constant public static final java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.EDITABLE_HINT_NAME
fld  constant public static final java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.LINE_HINT_NAME
fld  constant public static final java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.SELECTION_PARAMETER_NAME
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.isEditable()
meth public boolean org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.isSlave()
meth public boolean org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.isUserModified()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getInsertTextOffset()
meth public int org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getParametrizedTextEndOffset()
meth public int org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getParametrizedTextStartOffset()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getName()
meth public java.lang.String org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getValue()
meth public java.util.Collection org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getSlaves()
meth public java.util.Map org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getHints()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.getMaster()
meth public void org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter.setValue(java.lang.String)
supr java.lang.Object
CLSS public abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor
meth public abstract void org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor.parameterValueChanged(org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter,boolean)
meth public abstract void org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor.release()
meth public abstract void org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor.updateDefaultValues()
supr null
CLSS public abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory
meth public abstract org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory.createProcessor(org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest)
supr null
