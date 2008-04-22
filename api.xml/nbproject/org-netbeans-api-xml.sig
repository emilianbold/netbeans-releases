#API master signature file
#Version 1.15
CLSS public static abstract interface org.openide.nodes.Node$Cookie
supr null
CLSS public abstract interface org.netbeans.api.xml.cookies.CheckXMLCookie
intf org.openide.nodes.Node$Cookie
meth public abstract boolean org.netbeans.api.xml.cookies.CheckXMLCookie.checkXML(org.netbeans.api.xml.cookies.CookieObserver)
supr null
CLSS public final org.netbeans.api.xml.cookies.CookieMessage
cons public CookieMessage(java.lang.String)
cons public CookieMessage(java.lang.String,int)
cons public CookieMessage(java.lang.String,int,java.lang.Object)
cons public CookieMessage(java.lang.String,int,org.openide.util.Lookup)
cons public CookieMessage(java.lang.String,java.lang.Object)
fld  constant public static final int org.netbeans.api.xml.cookies.CookieMessage.ERROR_LEVEL
fld  constant public static final int org.netbeans.api.xml.cookies.CookieMessage.FATAL_ERROR_LEVEL
fld  constant public static final int org.netbeans.api.xml.cookies.CookieMessage.INFORMATIONAL_LEVEL
fld  constant public static final int org.netbeans.api.xml.cookies.CookieMessage.WARNING_LEVEL
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final int org.netbeans.api.xml.cookies.CookieMessage.getLevel()
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.api.xml.cookies.CookieMessage.getDetail(java.lang.Class)
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.api.xml.cookies.CookieMessage.getMessage()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.openide.util.Lookup org.netbeans.api.xml.cookies.CookieMessage.getDetails()
supr java.lang.Object
CLSS public abstract interface org.netbeans.api.xml.cookies.CookieObserver
meth public abstract void org.netbeans.api.xml.cookies.CookieObserver.receive(org.netbeans.api.xml.cookies.CookieMessage)
supr null
CLSS public abstract interface org.netbeans.api.xml.cookies.TransformableCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void org.netbeans.api.xml.cookies.TransformableCookie.transform(javax.xml.transform.Source,javax.xml.transform.Result,org.netbeans.api.xml.cookies.CookieObserver) throws javax.xml.transform.TransformerException
supr null
CLSS public abstract interface org.netbeans.api.xml.cookies.ValidateXMLCookie
intf org.openide.nodes.Node$Cookie
meth public abstract boolean org.netbeans.api.xml.cookies.ValidateXMLCookie.validateXML(org.netbeans.api.xml.cookies.CookieObserver)
supr null
CLSS public abstract org.netbeans.api.xml.cookies.XMLProcessorDetail
cons public XMLProcessorDetail()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public abstract int org.netbeans.api.xml.cookies.XMLProcessorDetail.getColumnNumber()
meth public abstract int org.netbeans.api.xml.cookies.XMLProcessorDetail.getLineNumber()
meth public abstract java.lang.Exception org.netbeans.api.xml.cookies.XMLProcessorDetail.getException()
meth public abstract java.lang.String org.netbeans.api.xml.cookies.XMLProcessorDetail.getPublicId()
meth public abstract java.lang.String org.netbeans.api.xml.cookies.XMLProcessorDetail.getSystemId()
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr java.lang.Object
CLSS public final org.netbeans.api.xml.parsers.DocumentInputSource
cons public DocumentInputSource(javax.swing.text.Document)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public final void org.netbeans.api.xml.parsers.DocumentInputSource.setCharacterStream(java.io.Reader)
meth public java.io.InputStream org.xml.sax.InputSource.getByteStream()
meth public java.io.Reader org.netbeans.api.xml.parsers.DocumentInputSource.getCharacterStream()
meth public java.lang.String org.netbeans.api.xml.parsers.DocumentInputSource.getSystemId()
meth public java.lang.String org.netbeans.api.xml.parsers.DocumentInputSource.toString()
meth public java.lang.String org.xml.sax.InputSource.getEncoding()
meth public java.lang.String org.xml.sax.InputSource.getPublicId()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public void org.xml.sax.InputSource.setByteStream(java.io.InputStream)
meth public void org.xml.sax.InputSource.setEncoding(java.lang.String)
meth public void org.xml.sax.InputSource.setPublicId(java.lang.String)
meth public void org.xml.sax.InputSource.setSystemId(java.lang.String)
supr org.xml.sax.InputSource
CLSS public org.netbeans.api.xml.parsers.SAXEntityParser
cons public SAXEntityParser(org.xml.sax.XMLReader)
cons public SAXEntityParser(org.xml.sax.XMLReader,boolean)
intf org.xml.sax.XMLReader
meth protected boolean org.netbeans.api.xml.parsers.SAXEntityParser.propagateException(org.xml.sax.SAXParseException)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected org.xml.sax.InputSource org.netbeans.api.xml.parsers.SAXEntityParser.wrapInputSource(org.xml.sax.InputSource)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.api.xml.parsers.SAXEntityParser.getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.Object org.netbeans.api.xml.parsers.SAXEntityParser.getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.xml.sax.ContentHandler org.netbeans.api.xml.parsers.SAXEntityParser.getContentHandler()
meth public org.xml.sax.DTDHandler org.netbeans.api.xml.parsers.SAXEntityParser.getDTDHandler()
meth public org.xml.sax.EntityResolver org.netbeans.api.xml.parsers.SAXEntityParser.getEntityResolver()
meth public org.xml.sax.ErrorHandler org.netbeans.api.xml.parsers.SAXEntityParser.getErrorHandler()
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.setContentHandler(org.xml.sax.ContentHandler)
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.setDTDHandler(org.xml.sax.DTDHandler)
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.setEntityResolver(org.xml.sax.EntityResolver)
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.setErrorHandler(org.xml.sax.ErrorHandler)
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void org.netbeans.api.xml.parsers.SAXEntityParser.setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
supr java.lang.Object
CLSS public abstract org.netbeans.api.xml.services.UserCatalog
cons public UserCatalog()
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public java.util.Iterator org.netbeans.api.xml.services.UserCatalog.getPublicIDs()
meth public javax.xml.transform.URIResolver org.netbeans.api.xml.services.UserCatalog.getURIResolver()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
meth public org.xml.sax.EntityResolver org.netbeans.api.xml.services.UserCatalog.getEntityResolver()
meth public static org.netbeans.api.xml.services.UserCatalog org.netbeans.api.xml.services.UserCatalog.getDefault()
supr java.lang.Object
CLSS public org.netbeans.spi.xml.cookies.CheckXMLSupport
cons public CheckXMLSupport(org.xml.sax.InputSource)
cons public CheckXMLSupport(org.xml.sax.InputSource,int)
fld  constant public static final int org.netbeans.spi.xml.cookies.CheckXMLSupport.CHECK_ENTITY_MODE
fld  constant public static final int org.netbeans.spi.xml.cookies.CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE
fld  constant public static final int org.netbeans.spi.xml.cookies.CheckXMLSupport.DOCUMENT_MODE
intf org.netbeans.api.xml.cookies.CheckXMLCookie
intf org.openide.nodes.Node$Cookie
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected org.xml.sax.EntityResolver org.netbeans.spi.xml.cookies.SharedXMLSupport.createEntityResolver()
meth protected org.xml.sax.InputSource org.netbeans.spi.xml.cookies.SharedXMLSupport.createInputSource() throws java.io.IOException
meth protected org.xml.sax.XMLReader org.netbeans.spi.xml.cookies.SharedXMLSupport.createParser(boolean)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.xml.cookies.CheckXMLSupport.checkXML(org.netbeans.api.xml.cookies.CookieObserver)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.xml.cookies.SharedXMLSupport
CLSS public final org.netbeans.spi.xml.cookies.DataObjectAdapters
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
meth public static javax.xml.transform.Source org.netbeans.spi.xml.cookies.DataObjectAdapters.source(org.openide.loaders.DataObject)
meth public static org.xml.sax.InputSource org.netbeans.spi.xml.cookies.DataObjectAdapters.inputSource(org.openide.loaders.DataObject)
supr java.lang.Object
CLSS public org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail
cons public DefaultXMLProcessorDetail(javax.xml.transform.TransformerException)
cons public DefaultXMLProcessorDetail(org.xml.sax.SAXParseException)
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public int org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail.getColumnNumber()
meth public int org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail.getLineNumber()
meth public java.lang.Exception org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail.getException()
meth public java.lang.String java.lang.Object.toString()
meth public java.lang.String org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail.getPublicId()
meth public java.lang.String org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail.getSystemId()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.api.xml.cookies.XMLProcessorDetail
CLSS public final org.netbeans.spi.xml.cookies.TransformableSupport
cons public TransformableSupport(javax.xml.transform.Source)
intf org.netbeans.api.xml.cookies.TransformableCookie
intf org.openide.nodes.Node$Cookie
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
meth public void org.netbeans.spi.xml.cookies.TransformableSupport.transform(javax.xml.transform.Source,javax.xml.transform.Result,org.netbeans.api.xml.cookies.CookieObserver) throws javax.xml.transform.TransformerException
supr java.lang.Object
CLSS public org.netbeans.spi.xml.cookies.ValidateXMLSupport
cons public ValidateXMLSupport(org.xml.sax.InputSource)
intf org.netbeans.api.xml.cookies.ValidateXMLCookie
intf org.openide.nodes.Node$Cookie
meth protected native java.lang.Object java.lang.Object.clone() throws java.lang.CloneNotSupportedException
meth protected org.xml.sax.EntityResolver org.netbeans.spi.xml.cookies.SharedXMLSupport.createEntityResolver()
meth protected org.xml.sax.InputSource org.netbeans.spi.xml.cookies.SharedXMLSupport.createInputSource() throws java.io.IOException
meth protected org.xml.sax.XMLReader org.netbeans.spi.xml.cookies.SharedXMLSupport.createParser(boolean)
meth protected void java.lang.Object.finalize() throws java.lang.Throwable
meth public boolean java.lang.Object.equals(java.lang.Object)
meth public boolean org.netbeans.spi.xml.cookies.ValidateXMLSupport.validateXML(org.netbeans.api.xml.cookies.CookieObserver)
meth public final void java.lang.Object.wait() throws java.lang.InterruptedException
meth public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
meth public java.lang.String java.lang.Object.toString()
meth public native final java.lang.Class java.lang.Object.getClass()
meth public native final void java.lang.Object.notify()
meth public native final void java.lang.Object.notifyAll()
meth public native final void java.lang.Object.wait(long) throws java.lang.InterruptedException
meth public native int java.lang.Object.hashCode()
supr org.netbeans.spi.xml.cookies.SharedXMLSupport
