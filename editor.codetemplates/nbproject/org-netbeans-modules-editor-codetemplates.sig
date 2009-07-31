#Signature file v4.0
#Version 1.11.1

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

CLSS public final org.netbeans.lib.editor.codetemplates.api.CodeTemplate
meth public java.lang.String getAbbreviation()
meth public java.lang.String getDescription()
meth public java.lang.String getParametrizedText()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getContexts()
meth public void insert(javax.swing.text.JTextComponent)
supr java.lang.Object
hfds abbreviation,contexts,description,managerOperation,mimePath,parametrizedText,singleLineText

CLSS public final org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager
meth public boolean isLoaded()
meth public java.util.Collection<? extends org.netbeans.lib.editor.codetemplates.api.CodeTemplate> getCodeTemplates()
meth public org.netbeans.lib.editor.codetemplates.api.CodeTemplate createTemporary(java.lang.String)
meth public static org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager get(javax.swing.text.Document)
meth public void registerLoadedListener(javax.swing.event.ChangeListener)
meth public void waitLoaded()
supr java.lang.Object
hfds operation
hcls ApiAccessor

CLSS public abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter
innr public abstract interface static Factory
meth public abstract boolean accept(org.netbeans.lib.editor.codetemplates.api.CodeTemplate)

CLSS public abstract interface static org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter$Factory
meth public abstract org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter createFilter(javax.swing.text.JTextComponent,int)

CLSS public final org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest
meth public boolean isInserted()
meth public boolean isReleased()
meth public int getInsertTextOffset()
meth public java.lang.String getInsertText()
meth public java.lang.String getParametrizedText()
meth public java.util.List<? extends org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter> getAllParameters()
meth public java.util.List<? extends org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter> getMasterParameters()
meth public javax.swing.text.JTextComponent getComponent()
meth public org.netbeans.lib.editor.codetemplates.api.CodeTemplate getCodeTemplate()
meth public org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter getMasterParameter(java.lang.String)
meth public void setParametrizedText(java.lang.String)
supr java.lang.Object
hfds handler
hcls SpiAccessor

CLSS public final org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter
fld public final static java.lang.String COMPLETION_INVOKE_HINT_NAME = "completionInvoke"
fld public final static java.lang.String CURSOR_PARAMETER_NAME = "cursor"
fld public final static java.lang.String DEFAULT_VALUE_HINT_NAME = "default"
fld public final static java.lang.String EDITABLE_HINT_NAME = "editable"
fld public final static java.lang.String LINE_HINT_NAME = "line"
fld public final static java.lang.String SELECTION_PARAMETER_NAME = "selection"
meth public boolean isEditable()
meth public boolean isSlave()
meth public boolean isUserModified()
meth public int getInsertTextOffset()
meth public int getParametrizedTextEndOffset()
meth public int getParametrizedTextStartOffset()
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public java.util.Collection<? extends org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter> getSlaves()
meth public java.util.Map<java.lang.String,java.lang.String> getHints()
meth public org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter getMaster()
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds impl

CLSS public abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor
meth public abstract void parameterValueChanged(org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter,boolean)
meth public abstract void release()
meth public abstract void updateDefaultValues()

CLSS public abstract interface org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory
meth public abstract org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor createProcessor(org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest)

