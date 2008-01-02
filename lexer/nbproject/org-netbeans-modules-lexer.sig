#API master signature file
#Version 1.25.1
CLSS public static abstract org.netbeans.lib.editor.util.AbstractCharSequence$StringLike
cons public StringLike()
innr public static abstract org.netbeans.lib.editor.util.AbstractCharSequence$StringLike
intf java.lang.CharSequence
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract char org.netbeans.lib.editor.util.AbstractCharSequence.charAt(int)
meth public abstract int org.netbeans.lib.editor.util.AbstractCharSequence.length()
meth public boolean org.netbeans.lib.editor.util.AbstractCharSequence$StringLike.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.lib.editor.util.AbstractCharSequence$StringLike.hashCode()
meth public java.lang.CharSequence org.netbeans.lib.editor.util.AbstractCharSequence$StringLike.subSequence(int,int)
meth public java.lang.String org.netbeans.lib.editor.util.AbstractCharSequence.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr org.netbeans.lib.editor.util.AbstractCharSequence
CLSS public final org.netbeans.api.lexer.InputAttributes
cons public InputAttributes()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.api.lexer.InputAttributes.getValue(org.netbeans.api.lexer.LanguagePath,java.lang.Object)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.api.lexer.InputAttributes.setValue(org.netbeans.api.lexer.Language,java.lang.Object,java.lang.Object,boolean)
meth public void org.netbeans.api.lexer.InputAttributes.setValue(org.netbeans.api.lexer.LanguagePath,java.lang.Object,java.lang.Object,boolean)
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.Language
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean org.netbeans.api.lexer.Language.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.lexer.Language.hashCode()
meth public int org.netbeans.api.lexer.Language.maxOrdinal()
meth public java.lang.String org.netbeans.api.lexer.Language.dumpInfo()
meth public java.lang.String org.netbeans.api.lexer.Language.mimeType()
meth public java.lang.String org.netbeans.api.lexer.Language.toString()
meth public java.util.List org.netbeans.api.lexer.Language.nonPrimaryTokenCategories(org.netbeans.api.lexer.TokenId)
meth public java.util.List org.netbeans.api.lexer.Language.tokenCategories(org.netbeans.api.lexer.TokenId)
meth public java.util.Set org.netbeans.api.lexer.Language.merge(java.util.Collection,java.util.Collection)
meth public java.util.Set org.netbeans.api.lexer.Language.tokenCategories()
meth public java.util.Set org.netbeans.api.lexer.Language.tokenCategoryMembers(java.lang.String)
meth public java.util.Set org.netbeans.api.lexer.Language.tokenIds()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public org.netbeans.api.lexer.TokenId org.netbeans.api.lexer.Language.tokenId(int)
meth public org.netbeans.api.lexer.TokenId org.netbeans.api.lexer.Language.tokenId(java.lang.String)
meth public org.netbeans.api.lexer.TokenId org.netbeans.api.lexer.Language.validTokenId(int)
meth public org.netbeans.api.lexer.TokenId org.netbeans.api.lexer.Language.validTokenId(java.lang.String)
meth public static org.netbeans.api.lexer.Language org.netbeans.api.lexer.Language.find(java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.LanguagePath
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.lexer.LanguagePath.endsWith(org.netbeans.api.lexer.LanguagePath)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.lexer.LanguagePath.size()
meth public java.lang.String org.netbeans.api.lexer.LanguagePath.mimePath()
meth public java.lang.String org.netbeans.api.lexer.LanguagePath.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.Language org.netbeans.api.lexer.LanguagePath.innerLanguage()
meth public org.netbeans.api.lexer.Language org.netbeans.api.lexer.LanguagePath.language(int)
meth public org.netbeans.api.lexer.Language org.netbeans.api.lexer.LanguagePath.topLanguage()
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.embedded(org.netbeans.api.lexer.Language)
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.embedded(org.netbeans.api.lexer.LanguagePath)
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.parent()
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.subPath(int)
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.subPath(int,int)
meth public static org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.get(org.netbeans.api.lexer.Language)
meth public static org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.LanguagePath.get(org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.Language)
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.PartType
fld  public static final org.netbeans.api.lexer.PartType org.netbeans.api.lexer.PartType.COMPLETE
fld  public static final org.netbeans.api.lexer.PartType org.netbeans.api.lexer.PartType.END
fld  public static final org.netbeans.api.lexer.PartType org.netbeans.api.lexer.PartType.MIDDLE
fld  public static final org.netbeans.api.lexer.PartType org.netbeans.api.lexer.PartType.START
intf java.io.Serializable
intf java.lang.Comparable
meth protected final java.lang.Object java.lang.Enum.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public final boolean java.lang.Enum.equals(java.lang.Object)
meth public final int java.lang.Enum.compareTo(java.lang.Enum)
meth public final int java.lang.Enum.hashCode()
meth public final int java.lang.Enum.ordinal()
meth public final java.lang.Class java.lang.Enum.getDeclaringClass()
meth public final java.lang.String java.lang.Enum.name()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Enum.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static final [Lorg.netbeans.api.lexer.PartType; org.netbeans.api.lexer.PartType.values()
meth public static java.lang.Enum java.lang.Enum.valueOf(java.lang.Class,java.lang.String)
meth public static org.netbeans.api.lexer.PartType org.netbeans.api.lexer.PartType.valueOf(java.lang.String)
meth public volatile int java.lang.Enum.compareTo(java.lang.Object)
supr java.lang.Enum
CLSS public abstract org.netbeans.api.lexer.Token
cons protected Token()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract boolean org.netbeans.api.lexer.Token.hasProperties()
meth public abstract boolean org.netbeans.api.lexer.Token.isCustomText()
meth public abstract boolean org.netbeans.api.lexer.Token.isFlyweight()
meth public abstract int org.netbeans.api.lexer.Token.length()
meth public abstract int org.netbeans.api.lexer.Token.offset(org.netbeans.api.lexer.TokenHierarchy)
meth public abstract java.lang.CharSequence org.netbeans.api.lexer.Token.text()
meth public abstract java.lang.Object org.netbeans.api.lexer.Token.getProperty(java.lang.Object)
meth public abstract org.netbeans.api.lexer.PartType org.netbeans.api.lexer.Token.partType()
meth public abstract org.netbeans.api.lexer.TokenId org.netbeans.api.lexer.Token.id()
meth public final boolean org.netbeans.api.lexer.Token.equals(java.lang.Object)
meth public final int org.netbeans.api.lexer.Token.hashCode()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.TokenChange
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.lexer.TokenChange.isBoundsChange()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.lexer.TokenChange.addedTokenCount()
meth public int org.netbeans.api.lexer.TokenChange.embeddedChangeCount()
meth public int org.netbeans.api.lexer.TokenChange.index()
meth public int org.netbeans.api.lexer.TokenChange.offset()
meth public int org.netbeans.api.lexer.TokenChange.removedTokenCount()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.Language org.netbeans.api.lexer.TokenChange.language()
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.TokenChange.languagePath()
meth public org.netbeans.api.lexer.TokenChange org.netbeans.api.lexer.TokenChange.embeddedChange(int)
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenChange.currentTokenSequence()
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenChange.removedTokenSequence()
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.TokenHierarchy
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.lexer.TokenHierarchy.isActive()
meth public boolean org.netbeans.api.lexer.TokenHierarchy.isMutable()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.api.lexer.TokenHierarchy.inputSource()
meth public java.lang.String org.netbeans.api.lexer.TokenHierarchy.toString()
meth public java.util.List org.netbeans.api.lexer.TokenHierarchy.embeddedTokenSequences(int,boolean)
meth public java.util.List org.netbeans.api.lexer.TokenHierarchy.tokenSequenceList(org.netbeans.api.lexer.LanguagePath,int,int)
meth public java.util.Set org.netbeans.api.lexer.TokenHierarchy.languagePaths()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenHierarchy.tokenSequence()
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenHierarchy.tokenSequence(org.netbeans.api.lexer.Language)
meth public static org.netbeans.api.lexer.TokenHierarchy org.netbeans.api.lexer.TokenHierarchy.create(java.io.Reader,org.netbeans.api.lexer.Language,java.util.Set,org.netbeans.api.lexer.InputAttributes)
meth public static org.netbeans.api.lexer.TokenHierarchy org.netbeans.api.lexer.TokenHierarchy.create(java.lang.CharSequence,boolean,org.netbeans.api.lexer.Language,java.util.Set,org.netbeans.api.lexer.InputAttributes)
meth public static org.netbeans.api.lexer.TokenHierarchy org.netbeans.api.lexer.TokenHierarchy.create(java.lang.CharSequence,org.netbeans.api.lexer.Language)
meth public static org.netbeans.api.lexer.TokenHierarchy org.netbeans.api.lexer.TokenHierarchy.get(javax.swing.text.Document)
meth public void org.netbeans.api.lexer.TokenHierarchy.addTokenHierarchyListener(org.netbeans.api.lexer.TokenHierarchyListener)
meth public void org.netbeans.api.lexer.TokenHierarchy.removeTokenHierarchyListener(org.netbeans.api.lexer.TokenHierarchyListener)
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.TokenHierarchyEvent
fld  protected transient java.lang.Object java.util.EventObject.source
intf java.io.Serializable
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.lexer.TokenHierarchyEvent.affectedEndOffset()
meth public int org.netbeans.api.lexer.TokenHierarchyEvent.affectedStartOffset()
meth public int org.netbeans.api.lexer.TokenHierarchyEvent.insertedLength()
meth public int org.netbeans.api.lexer.TokenHierarchyEvent.modificationOffset()
meth public int org.netbeans.api.lexer.TokenHierarchyEvent.removedLength()
meth public java.lang.Object java.util.EventObject.getSource()
meth public java.lang.String java.util.EventObject.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.TokenChange org.netbeans.api.lexer.TokenHierarchyEvent.tokenChange()
meth public org.netbeans.api.lexer.TokenChange org.netbeans.api.lexer.TokenHierarchyEvent.tokenChange(org.netbeans.api.lexer.Language)
meth public org.netbeans.api.lexer.TokenHierarchy org.netbeans.api.lexer.TokenHierarchyEvent.tokenHierarchy()
meth public org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEvent.type()
supr java.util.EventObject
CLSS public final org.netbeans.api.lexer.TokenHierarchyEventType
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.ACTIVITY
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.EMBEDDING_CREATED
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.EMBEDDING_REMOVED
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.LANGUAGE_PATHS
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.MODIFICATION
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.REBUILD
fld  public static final org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.RELEX
intf java.io.Serializable
intf java.lang.Comparable
meth protected final java.lang.Object java.lang.Enum.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public final boolean java.lang.Enum.equals(java.lang.Object)
meth public final int java.lang.Enum.compareTo(java.lang.Enum)
meth public final int java.lang.Enum.hashCode()
meth public final int java.lang.Enum.ordinal()
meth public final java.lang.Class java.lang.Enum.getDeclaringClass()
meth public final java.lang.String java.lang.Enum.name()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Enum.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static final [Lorg.netbeans.api.lexer.TokenHierarchyEventType; org.netbeans.api.lexer.TokenHierarchyEventType.values()
meth public static java.lang.Enum java.lang.Enum.valueOf(java.lang.Class,java.lang.String)
meth public static org.netbeans.api.lexer.TokenHierarchyEventType org.netbeans.api.lexer.TokenHierarchyEventType.valueOf(java.lang.String)
meth public volatile int java.lang.Enum.compareTo(java.lang.Object)
supr java.lang.Enum
CLSS public abstract interface org.netbeans.api.lexer.TokenHierarchyListener
intf java.util.EventListener
meth public abstract void org.netbeans.api.lexer.TokenHierarchyListener.tokenHierarchyChanged(org.netbeans.api.lexer.TokenHierarchyEvent)
supr null
CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int org.netbeans.api.lexer.TokenId.ordinal()
meth public abstract java.lang.String org.netbeans.api.lexer.TokenId.name()
meth public abstract java.lang.String org.netbeans.api.lexer.TokenId.primaryCategory()
supr null
CLSS public final org.netbeans.api.lexer.TokenSequence
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.lexer.TokenSequence.createEmbedding(org.netbeans.api.lexer.Language,int,int)
meth public boolean org.netbeans.api.lexer.TokenSequence.createEmbedding(org.netbeans.api.lexer.Language,int,int,boolean)
meth public boolean org.netbeans.api.lexer.TokenSequence.isEmpty()
meth public boolean org.netbeans.api.lexer.TokenSequence.isValid()
meth public boolean org.netbeans.api.lexer.TokenSequence.moveNext()
meth public boolean org.netbeans.api.lexer.TokenSequence.movePrevious()
meth public boolean org.netbeans.api.lexer.TokenSequence.removeEmbedding(org.netbeans.api.lexer.Language)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.api.lexer.TokenSequence.index()
meth public int org.netbeans.api.lexer.TokenSequence.move(int)
meth public int org.netbeans.api.lexer.TokenSequence.moveIndex(int)
meth public int org.netbeans.api.lexer.TokenSequence.offset()
meth public int org.netbeans.api.lexer.TokenSequence.tokenCount()
meth public java.lang.String org.netbeans.api.lexer.TokenSequence.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.Language org.netbeans.api.lexer.TokenSequence.language()
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.api.lexer.TokenSequence.languagePath()
meth public org.netbeans.api.lexer.Token org.netbeans.api.lexer.TokenSequence.offsetToken()
meth public org.netbeans.api.lexer.Token org.netbeans.api.lexer.TokenSequence.token()
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenSequence.embedded()
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenSequence.embedded(org.netbeans.api.lexer.Language)
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenSequence.subSequence(int)
meth public org.netbeans.api.lexer.TokenSequence org.netbeans.api.lexer.TokenSequence.subSequence(int,int)
meth public void org.netbeans.api.lexer.TokenSequence.moveEnd()
meth public void org.netbeans.api.lexer.TokenSequence.moveStart()
supr java.lang.Object
CLSS public final org.netbeans.api.lexer.TokenUtilities
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
meth public static boolean org.netbeans.api.lexer.TokenUtilities.endsWith(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean org.netbeans.api.lexer.TokenUtilities.equals(java.lang.CharSequence,java.lang.Object)
meth public static boolean org.netbeans.api.lexer.TokenUtilities.startsWith(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean org.netbeans.api.lexer.TokenUtilities.textEquals(java.lang.CharSequence,java.lang.CharSequence)
meth public static int org.netbeans.api.lexer.TokenUtilities.indexOf(java.lang.CharSequence,int)
meth public static int org.netbeans.api.lexer.TokenUtilities.indexOf(java.lang.CharSequence,int,int)
meth public static int org.netbeans.api.lexer.TokenUtilities.indexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int org.netbeans.api.lexer.TokenUtilities.indexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static int org.netbeans.api.lexer.TokenUtilities.lastIndexOf(java.lang.CharSequence,int)
meth public static int org.netbeans.api.lexer.TokenUtilities.lastIndexOf(java.lang.CharSequence,int,int)
meth public static int org.netbeans.api.lexer.TokenUtilities.lastIndexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int org.netbeans.api.lexer.TokenUtilities.lastIndexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static java.lang.CharSequence org.netbeans.api.lexer.TokenUtilities.trim(java.lang.CharSequence)
meth public static java.lang.String org.netbeans.api.lexer.TokenUtilities.debugText(java.lang.CharSequence)
supr java.lang.Object
CLSS public final org.netbeans.spi.lexer.EmbeddingPresence
fld  public static final org.netbeans.spi.lexer.EmbeddingPresence org.netbeans.spi.lexer.EmbeddingPresence.ALWAYS_QUERY
fld  public static final org.netbeans.spi.lexer.EmbeddingPresence org.netbeans.spi.lexer.EmbeddingPresence.CACHED_FIRST_QUERY
fld  public static final org.netbeans.spi.lexer.EmbeddingPresence org.netbeans.spi.lexer.EmbeddingPresence.NONE
intf java.io.Serializable
intf java.lang.Comparable
meth protected final java.lang.Object java.lang.Enum.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public final boolean java.lang.Enum.equals(java.lang.Object)
meth public final int java.lang.Enum.compareTo(java.lang.Enum)
meth public final int java.lang.Enum.hashCode()
meth public final int java.lang.Enum.ordinal()
meth public final java.lang.Class java.lang.Enum.getDeclaringClass()
meth public final java.lang.String java.lang.Enum.name()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Enum.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static final [Lorg.netbeans.spi.lexer.EmbeddingPresence; org.netbeans.spi.lexer.EmbeddingPresence.values()
meth public static java.lang.Enum java.lang.Enum.valueOf(java.lang.Class,java.lang.String)
meth public static org.netbeans.spi.lexer.EmbeddingPresence org.netbeans.spi.lexer.EmbeddingPresence.valueOf(java.lang.String)
meth public volatile int java.lang.Enum.compareTo(java.lang.Object)
supr java.lang.Enum
CLSS public final org.netbeans.spi.lexer.LanguageEmbedding
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.lexer.LanguageEmbedding.joinSections()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.lexer.LanguageEmbedding.endSkipLength()
meth public int org.netbeans.spi.lexer.LanguageEmbedding.startSkipLength()
meth public java.lang.String org.netbeans.spi.lexer.LanguageEmbedding.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.Language org.netbeans.spi.lexer.LanguageEmbedding.language()
meth public static org.netbeans.spi.lexer.LanguageEmbedding org.netbeans.spi.lexer.LanguageEmbedding.create(org.netbeans.api.lexer.Language,int,int)
meth public static org.netbeans.spi.lexer.LanguageEmbedding org.netbeans.spi.lexer.LanguageEmbedding.create(org.netbeans.api.lexer.Language,int,int,boolean)
supr java.lang.Object
CLSS public abstract org.netbeans.spi.lexer.LanguageHierarchy
cons public LanguageHierarchy()
meth protected abstract java.lang.String org.netbeans.spi.lexer.LanguageHierarchy.mimeType()
meth protected abstract java.util.Collection org.netbeans.spi.lexer.LanguageHierarchy.createTokenIds()
meth protected abstract org.netbeans.spi.lexer.Lexer org.netbeans.spi.lexer.LanguageHierarchy.createLexer(org.netbeans.spi.lexer.LexerRestartInfo)
meth protected boolean org.netbeans.spi.lexer.LanguageHierarchy.isRetainTokenText(org.netbeans.api.lexer.TokenId)
meth protected java.util.Map org.netbeans.spi.lexer.LanguageHierarchy.createTokenCategories()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected org.netbeans.spi.lexer.EmbeddingPresence org.netbeans.spi.lexer.LanguageHierarchy.embeddingPresence(org.netbeans.api.lexer.TokenId)
meth protected org.netbeans.spi.lexer.LanguageEmbedding org.netbeans.spi.lexer.LanguageHierarchy.embedding(org.netbeans.api.lexer.Token,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
meth protected org.netbeans.spi.lexer.TokenValidator org.netbeans.spi.lexer.LanguageHierarchy.createTokenValidator(org.netbeans.api.lexer.TokenId)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public final boolean org.netbeans.spi.lexer.LanguageHierarchy.equals(java.lang.Object)
meth public final int org.netbeans.spi.lexer.LanguageHierarchy.hashCode()
meth public final org.netbeans.api.lexer.Language org.netbeans.spi.lexer.LanguageHierarchy.language()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String org.netbeans.spi.lexer.LanguageHierarchy.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public static org.netbeans.api.lexer.TokenId org.netbeans.spi.lexer.LanguageHierarchy.newId(java.lang.String,int)
meth public static org.netbeans.api.lexer.TokenId org.netbeans.spi.lexer.LanguageHierarchy.newId(java.lang.String,int,java.lang.String)
supr java.lang.Object
CLSS public abstract org.netbeans.spi.lexer.LanguageProvider
cons protected LanguageProvider()
fld  constant public static final java.lang.String org.netbeans.spi.lexer.LanguageProvider.PROP_EMBEDDED_LANGUAGE
fld  constant public static final java.lang.String org.netbeans.spi.lexer.LanguageProvider.PROP_LANGUAGE
meth protected final void org.netbeans.spi.lexer.LanguageProvider.firePropertyChange(java.lang.String)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract org.netbeans.api.lexer.Language org.netbeans.spi.lexer.LanguageProvider.findLanguage(java.lang.String)
meth public abstract org.netbeans.spi.lexer.LanguageEmbedding org.netbeans.spi.lexer.LanguageProvider.findLanguageEmbedding(org.netbeans.api.lexer.Token,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.netbeans.spi.lexer.LanguageProvider.addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void org.netbeans.spi.lexer.LanguageProvider.removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.lexer.Lexer
meth public abstract java.lang.Object org.netbeans.spi.lexer.Lexer.state()
meth public abstract org.netbeans.api.lexer.Token org.netbeans.spi.lexer.Lexer.nextToken()
meth public abstract void org.netbeans.spi.lexer.Lexer.release()
supr null
CLSS public final org.netbeans.spi.lexer.LexerInput
fld  constant public static final int org.netbeans.spi.lexer.LexerInput.EOF
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.lexer.LexerInput.consumeNewline()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.lexer.LexerInput.read()
meth public int org.netbeans.spi.lexer.LexerInput.readLength()
meth public int org.netbeans.spi.lexer.LexerInput.readLengthEOF()
meth public java.lang.CharSequence org.netbeans.spi.lexer.LexerInput.readText()
meth public java.lang.CharSequence org.netbeans.spi.lexer.LexerInput.readText(int,int)
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.netbeans.spi.lexer.LexerInput.backup(int)
supr java.lang.Object
CLSS public final org.netbeans.spi.lexer.LexerRestartInfo
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.spi.lexer.LexerRestartInfo.getAttributeValue(java.lang.Object)
meth public java.lang.Object org.netbeans.spi.lexer.LexerRestartInfo.state()
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.netbeans.api.lexer.InputAttributes org.netbeans.spi.lexer.LexerRestartInfo.inputAttributes()
meth public org.netbeans.api.lexer.LanguagePath org.netbeans.spi.lexer.LexerRestartInfo.languagePath()
meth public org.netbeans.spi.lexer.LexerInput org.netbeans.spi.lexer.LexerRestartInfo.input()
meth public org.netbeans.spi.lexer.TokenFactory org.netbeans.spi.lexer.LexerRestartInfo.tokenFactory()
supr java.lang.Object
CLSS public abstract org.netbeans.spi.lexer.MutableTextInput
cons public MutableTextInput()
meth protected abstract boolean org.netbeans.spi.lexer.MutableTextInput.isReadLocked()
meth protected abstract boolean org.netbeans.spi.lexer.MutableTextInput.isWriteLocked()
meth protected abstract java.lang.CharSequence org.netbeans.spi.lexer.MutableTextInput.text()
meth protected abstract java.lang.Object org.netbeans.spi.lexer.MutableTextInput.inputSource()
meth protected abstract org.netbeans.api.lexer.InputAttributes org.netbeans.spi.lexer.MutableTextInput.inputAttributes()
meth protected abstract org.netbeans.api.lexer.Language org.netbeans.spi.lexer.MutableTextInput.language()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final org.netbeans.spi.lexer.TokenHierarchyControl org.netbeans.spi.lexer.MutableTextInput.tokenHierarchyControl()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public final org.netbeans.spi.lexer.TokenFactory
fld  public static final org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.SKIP_TOKEN
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
meth public org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.createCustomTextToken(org.netbeans.api.lexer.TokenId,java.lang.CharSequence,int,org.netbeans.api.lexer.PartType)
meth public org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.createPropertyToken(org.netbeans.api.lexer.TokenId,int,org.netbeans.spi.lexer.TokenPropertyProvider,org.netbeans.api.lexer.PartType)
meth public org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.createToken(org.netbeans.api.lexer.TokenId)
meth public org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.createToken(org.netbeans.api.lexer.TokenId,int)
meth public org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.createToken(org.netbeans.api.lexer.TokenId,int,org.netbeans.api.lexer.PartType)
meth public org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenFactory.getFlyweightToken(org.netbeans.api.lexer.TokenId,java.lang.String)
supr java.lang.Object
CLSS public final org.netbeans.spi.lexer.TokenHierarchyControl
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.lexer.TokenHierarchyControl.isActive()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public synchronized org.netbeans.api.lexer.TokenHierarchy org.netbeans.spi.lexer.TokenHierarchyControl.tokenHierarchy()
meth public void org.netbeans.spi.lexer.TokenHierarchyControl.rebuild()
meth public void org.netbeans.spi.lexer.TokenHierarchyControl.setActive(boolean)
meth public void org.netbeans.spi.lexer.TokenHierarchyControl.textModified(int,int,java.lang.CharSequence,int)
supr java.lang.Object
CLSS public abstract interface org.netbeans.spi.lexer.TokenPropertyProvider
meth public abstract java.lang.Object org.netbeans.spi.lexer.TokenPropertyProvider.getValue(org.netbeans.api.lexer.Token,java.lang.Object)
supr null
CLSS public abstract interface org.netbeans.spi.lexer.TokenValidator
meth public abstract org.netbeans.api.lexer.Token org.netbeans.spi.lexer.TokenValidator.validateToken(org.netbeans.api.lexer.Token,org.netbeans.spi.lexer.TokenFactory,java.lang.CharSequence,int,int,java.lang.CharSequence,int,java.lang.CharSequence)
supr null
