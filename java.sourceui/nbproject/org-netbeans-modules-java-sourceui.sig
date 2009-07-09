#Signature file v4.0
#Version 1.7.1

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

CLSS public final org.netbeans.api.java.source.ui.DialogBinding
meth public static org.netbeans.api.java.source.JavaSource bindComponentToFile(org.openide.filesystems.FileObject,int,int,javax.swing.text.JTextComponent)
supr java.lang.Object

CLSS public final org.netbeans.api.java.source.ui.ElementHeaders
fld public final static java.lang.String ANNOTATIONS = "%annotations%"
fld public final static java.lang.String EXTENDS = "%extends%"
fld public final static java.lang.String FLAGS = "%flags%"
fld public final static java.lang.String IMPLEMENTS = "%implements%"
fld public final static java.lang.String NAME = "%name%"
fld public final static java.lang.String PARAMETERS = "%parameters%"
fld public final static java.lang.String THROWS = "%throws%"
fld public final static java.lang.String TYPE = "%type%"
fld public final static java.lang.String TYPEPARAMETERS = "%typeparameters%"
meth public static int getDistance(java.lang.String,java.lang.String)
meth public static java.lang.String getHeader(com.sun.source.util.TreePath,org.netbeans.api.java.source.CompilationInfo,java.lang.String)
meth public static java.lang.String getHeader(javax.lang.model.element.Element,org.netbeans.api.java.source.CompilationInfo,java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.api.java.source.ui.ElementIcons
meth public static javax.swing.Icon getElementIcon(javax.lang.model.element.ElementKind,java.util.Collection<javax.lang.model.element.Modifier>)
supr java.lang.Object

CLSS public org.netbeans.api.java.source.ui.ElementJavadoc
meth public final static org.netbeans.api.java.source.ui.ElementJavadoc create(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element)
meth public java.lang.String getText()
meth public java.net.URL getURL()
meth public javax.swing.Action getGotoSourceAction()
meth public org.netbeans.api.java.source.ui.ElementJavadoc resolveLink(java.lang.String)
supr java.lang.Object
hfds API,CODE_TAG,DEPRECATED_TAG,INHERIT_DOC_TAG,LANGS,LINKPLAIN_TAG,LITERAL_TAG,PARAM_TAG,RETURN_TAG,SEE_TAG,SINCE_TAG,THROWS_TAG,VALUE_TAG,content,cpInfo,docURL,eu,goToSource,linkCounter,links,trees

CLSS public final org.netbeans.api.java.source.ui.ElementOpen
meth public static boolean open(org.netbeans.api.java.source.ClasspathInfo,javax.lang.model.element.Element)
meth public static boolean open(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>)
meth public static boolean open(org.openide.filesystems.FileObject,org.netbeans.api.java.source.ElementHandle<? extends javax.lang.model.element.Element>)
supr java.lang.Object
hfds log
hcls FindDeclarationVisitor

CLSS public org.netbeans.api.java.source.ui.ScanDialog
meth public static boolean runWhenScanFinished(java.lang.Runnable,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.api.java.source.ui.TypeElementFinder
cons public TypeElementFinder()
innr public abstract interface static Customizer
meth public static org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement> find(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ui.TypeElementFinder$Customizer)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.api.java.source.ui.TypeElementFinder$Customizer
meth public abstract boolean accept(org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>)
meth public abstract java.util.Set<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> query(org.netbeans.api.java.source.ClasspathInfo,java.lang.String,org.netbeans.api.java.source.ClassIndex$NameKind,java.util.Set<org.netbeans.api.java.source.ClassIndex$SearchScope>)

