#Signature file v4.1
#Version 1.13

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

CLSS public abstract interface org.netbeans.spi.editor.bracesmatching.BracesMatcher
meth public abstract int[] findMatches() throws java.lang.InterruptedException,javax.swing.text.BadLocationException
meth public abstract int[] findOrigin() throws java.lang.InterruptedException,javax.swing.text.BadLocationException

CLSS public abstract interface org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory
meth public abstract org.netbeans.spi.editor.bracesmatching.BracesMatcher createMatcher(org.netbeans.spi.editor.bracesmatching.MatcherContext)

CLSS public final org.netbeans.spi.editor.bracesmatching.MatcherContext
meth public boolean isSearchingBackward()
meth public int getLimitOffset()
meth public int getSearchLookahead()
meth public int getSearchOffset()
meth public javax.swing.text.Document getDocument()
meth public static boolean isTaskCanceled()
supr java.lang.Object
hfds backward,document,lookahead,offset
hcls SpiAccessorImpl

CLSS public final org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport
meth public !varargs static int[] findChar(javax.swing.text.Document,int,int,char[]) throws javax.swing.text.BadLocationException
meth public !varargs static org.netbeans.spi.editor.bracesmatching.BracesMatcher characterMatcher(org.netbeans.spi.editor.bracesmatching.MatcherContext,int,int,char[])
meth public static int matchChar(javax.swing.text.Document,int,int,char,char) throws javax.swing.text.BadLocationException
meth public static org.netbeans.spi.editor.bracesmatching.BracesMatcher defaultMatcher(org.netbeans.spi.editor.bracesmatching.MatcherContext,int,int)
supr java.lang.Object
hfds DEFAULT_CHARS

