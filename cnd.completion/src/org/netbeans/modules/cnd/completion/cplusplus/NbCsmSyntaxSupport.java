/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.WeakEventListenerList;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka, Vladimir Voskresensky
* @version 1.00
*/

public class NbCsmSyntaxSupport extends CsmSyntaxSupport {

    protected static final String PACKAGE_SUMMARY = "package-summary"; // NOI18N
    
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private HashMap jcLookupCache = new HashMap(307);

    protected boolean jcValid;
    
//    private ParsingListener parsingListener;
    
    private static boolean parsingListenerInitialized;

    /** Support for firing change events */
    private static final WeakEventListenerList listenerList = new WeakEventListenerList();
    
    public NbCsmSyntaxSupport(BaseDocument doc) {
        super(doc);
        
//        initParsingListener();
//
//        if (parsingListener == null) {
//            parsingListener = new ParsingListenerImpl();
//            addParsingListener(parsingListener);
//        }
    }
    
    public CsmFinder getSupportJCFinder(){
        return getFinder();
    }
    
    public CsmFinder getFinder() {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        assert dobj != null;
        FileObject fo = dobj.getPrimaryFile();
        return CsmFinderFactory.getDefault().getFinder(fo);
    }
    
//    /** Add weak listener to listen to java module parsing events */
//    private static synchronized void addParsingListener(ParsingListener l) {
//        listenerList.add(ParsingListener.class, l);
//    }
//
//    /** Remove listener from java module parsing events */
//    private static synchronized void removeParsingListener(ParsingListener l) {
//        listenerList.remove(ParsingListener.class, l);
//    }
//
//    private static synchronized void fireParsingEvent(ParsingEvent evt) {
//        ParsingListener[] listeners = (ParsingListener[])
//             listenerList.getListeners(ParsingListener.class);
//
//        for (int i = 0; i < listeners.length; i++) {
//            listeners[i].objectParsed(evt);
//        }
//    }
    
    protected void documentModified(DocumentEvent evt) {
        super.documentModified(evt);
        jcValid = false;
    }

    protected int getMethodStartPosition(int pos) {
        DataObject dob = NbEditorUtilities.getDataObject(getDocument());
        if (dob != null) {
//            try {
//                SourceCookie.Editor sce = (SourceCookie.Editor)dob.getCookie(SourceCookie.Editor.class);
//                if (sce != null) {
//                    Element elem = sce.findElement(pos);
//                    if (elem != null) {
//                        javax.swing.text.Element swingElem = sce.sourceToText(elem);
//                        if (swingElem != null) {
//                            return swingElem.getStartOffset();
//                        }
//                    }
//                }
//            } catch (NullPointerException e) { // due to some bug in parser !!! [PENDING]
//            }
        }

        return 0;
    }

    /** Returns true if className is in import, but in a package, that hasn't updated DB */
    protected boolean isUnknownInclude(String className){
        // check for directly imported class
        boolean ret = super.isUnknownInclude(className);
        if (ret) return ret;
        
        return ret;

// XXX:
// I'm not sure about exact meaning of the code below.
// Mato explained that it is bugfix for #18078 and may be obsolete.
//
// Anyway, if it is needed the classpath of the project to which the document
// being edited belongs can be searched for.
//
//        // check for class in all unknown imported packages
//        List unknownImports = getUnknownImports();
//        for(int i=0; i<unknownImports.size(); i++){
//            String imp = (String)unknownImports.get(i);
//            if (imp.endsWith("*") && imp.length()>2) { //NOI18N
//                imp = imp.substring(0,imp.length()-2); //NOI18N
//            }else{
//                continue;
//            }
//            CsmClass exactCls = getFinder().getExactClass(imp+"."+className); //NOI18N
//            // if class is mounted in FS and is not in parser DB return true
//            if ( isMounted(imp,className) && exactCls==null ) return true;
//        }
//        
//        return ret;
    }
    
//    public int findGlobalDeclarationPosition(String varName, int varPos) {
//        Element e = getElementAtPos(varPos);
//        if (e instanceof MemberElement) {
//            MemberElement me = (MemberElement)e;
//            while (me != null) {
//                if (me instanceof ClassElement) {
//                    ClassElement ce = (ClassElement)me;
//                    FieldElement[] fields = ce.getFields();
//                    if (fields != null) {
//                        for (int i = 0; i < fields.length; i++) {
//                            if (fields[i].getName().getFullName().equals(varName)) {
//                                DataObject dob = NbEditorUtilities.getDataObject(getDocument());
//                                if (dob != null) {
//                                    SourceCookie.Editor sce = (SourceCookie.Editor)dob.getCookie(SourceCookie.Editor.class);
//                                    if (sce != null) {
//                                        javax.swing.text.Element elem = sce.sourceToText(fields[i]);
//                                        if (elem != null) {
//                                            return elem.getStartOffset();
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                me = me.getDeclaringClass();
//            }
//        }
//        return -1;
//    }

//    private Element getElementAtPos(int pos) {
//        DataObject dob = NbEditorUtilities.getDataObject(getDocument());
//        if (dob != null) {
//            SourceCookie.Editor sce = (SourceCookie.Editor)dob.getCookie(SourceCookie.Editor.class);
//            if (sce != null) {
//                return sce.findElement(pos);
//            }
//        }
//        return null;
//    }

//    /** Get the class element(s) according to the current position */
//    protected ClassElement getClassElement(int pos) {
//        ClassElement ce = null;
//        Element elem = getElementAtPos(pos);
//        if (elem instanceof ClassElement) {
//            ce = (ClassElement)elem;
//        } else if (elem instanceof MemberElement) {
//            ce = ((MemberElement)elem).getDeclaringClass();
//        } else if (elem instanceof InitializerElement) {
//            ce = ((InitializerElement)elem).getDeclaringClass();
//        }
//        return ce;
//    }

    /** Returns the CsmClass of the top class */
    public CsmClass getTopClass(){
        DataObject dob = NbEditorUtilities.getDataObject(getDocument());
        if (dob != null) { // Fix of #25154
//            ClassPath cp = ClassPath.getClassPath(dob.getPrimaryFile(), ClassPath.SOURCE);
//            if (cp != null) {
//                String topClassName = cp.getResourceName(dob.getPrimaryFile(), '.', false);
//                assert topClassName != null;
//                return getFinder().getExactClass(topClassName);
//            }
        }
        return null;
    }
    
    /* not necessary to look for innerclasses for JSP fake class
    public CsmClass getClassFromName(String className, boolean searchByName) {
        // look for the inner classes first
        CsmClass innerCls = JCExtension.findResultInnerClass(getFinder(), getTopClass(), className, this.getDocument());
        return (innerCls != null) ? innerCls : super.getClassFromName(className, searchByName);
    }    
     */
    
    /** Returns the package name of this source */
//    public String getPackage(){
//        DataObject dob = NbEditorUtilities.getDataObject(getDocument());
//        if (dob != null) {
//            SourceCookie sc = (SourceCookie)dob.getCookie(SourceCookie.class);
//            if (sc != null) {
//                SourceElement se = sc.getSource();
//                if (se != null) {
//                    Identifier pkg = se.getPackage();
//                    if (pkg!=null){
//                        return pkg.getFullName();
//                    }
//                }
//            }
//        }
//        return ""; //NOI18N
//    }
    
//    public CsmClass getClass(int pos) {
//        ClassElement ce = getClassElement(pos);
//        if (ce != null) {
//            return getFinder().getExactClass(ce.getName().getFullName());
//        }
//        return null;
//    }
//
//    public boolean isStaticBlock(int pos) {
//        Element elem = getElementAtPos(pos);
//        if (elem instanceof MethodElement) {
//            return (((MethodElement)elem).getModifiers() & Modifier.STATIC) != 0;
//        } else if (elem instanceof FieldElement) {
//            return (((FieldElement)elem).getModifiers() & Modifier.STATIC) != 0;
//        } else if (elem instanceof InitializerElement) {
//            return true;
//        }
//        return false;
//    }

//    private ClassElement[] getAllClassElements() {
//        DataObject dob = NbEditorUtilities.getDataObject(getDocument());
//        if (dob != null) {
//            SourceCookie sc = (SourceCookie)dob.getCookie(SourceCookie.class);
//            if (sc != null) {
//                SourceElement se = sc.getSource();
//                if (se != null) {
//                    return se.getAllClasses();
//                }
//            }
//        }
//        return null;
//    }
    
//    protected Map buildGlobalVariableMap(int pos) {
//        refreshClassInfo();
//        CsmFinder finder = getFinder();
//        CsmClass cls = getClass(pos);
//        if (cls != null) {
//            HashMap varMap = new HashMap();
//            List fldList = finder.findFields(cls, "", false, false, true); // NOI18N
//            for (int i = fldList.size() - 1; i >= 0; i--) {
//                CsmField fld = (CsmField)fldList.get(i);
//                varMap.put(fld.getName(), fld.getType());
//            }
//            return varMap;
//        }
//        return null;
//    }

//    protected ClassElement recurseClasses(ClassElement[] classes, String name) {
//        for (int i = 0; i < classes.length; i++) {
//            ClassElement ce = classes[i];
//            if (ce.getName().getFullName().replace('$', '.').equals(name)) {
//                return ce;
//            }
//            ClassElement inner = recurseClasses(ce.getClasses(), name);
//            if (inner != null) {
//                return inner;
//            }
//        }
//        return null;
//    }

    protected DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        if (fo != null) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return dob;
    }

    private DataObject getDataObject(CsmClass cls) {
//XXX
	String name = cls.getQualifiedName().replace('.', '/');
        FileObject fo = findResource(name+".java"); // NOI18N
        if (fo != null) {
            return getDataObject(fo);
        }
        return null;
    }

//    protected SourceElement getSourceElement(DataObject classDOB) {
//        SourceElement se = null;
//        if (classDOB != null) {
//            SourceCookie sc = (SourceCookie)classDOB.getCookie(SourceCookie.class);
//            if (sc != null) {
//                se = sc.getSource();
//            }
//        }
//        return se;
//    }
//
//    private ClassElement getClassElement(DataObject classDOB, CsmClass cls) {
//        SourceElement se = getSourceElement(classDOB);
//        ClassElement ce = null;
//        if (se != null) {
//            ce = recurseClasses(se.getClasses(), cls.getQualifiedName());
//        }
//        return ce;
//    }

//    protected void openAtElement(final DataObject classDOB, final Element e) {
//        new Thread() {
//            public void run() {
//                OpenCookie oc = (e != null)
//                                ? (OpenCookie)e.getCookie(OpenCookie.class)
//                                : (OpenCookie)classDOB.getCookie(OpenCookie.class);
//                if (oc != null) {
//                    oc.open();
//                    return;
//                }
//            }
//        }.start();
//    }

    private String getSourceName(CsmClass cls) {
        String name = cls.getName();
        int icInd = name.indexOf('.');
        if (icInd >= 0) { // inner class name
            name = name.substring(0, icInd);
        }
	// XXX
        return cls.getContainingNamespace().getName() + '.' + name;
    }

    /** Open the source according to the given object.
    * @param item completion object that is decoded and the appropriate
    *   source is opened.
    * @param findDeclaration find the declaration behaior that is different
    *   for the fields - it opens the source of the type of the field.
    * @return for simulate mode return the short display name (without package name)
    *   of the item or null if the item is not recognized.
    *   For non-simulate mode return the null if the item was successfully opened
    *   or the display name if the item's object can't be found.
    */
    public String openSource(Object item, boolean findDeclaration) {
        DataObject dob = null;
//        Element elem = null;
        String ret = null;
        boolean found = false;

        if (item instanceof CsmNamespace) {
            if (!findDeclaration) {
                String pkgName = ((CsmNamespace)item).getName();
                
                if ( (pkgName==null) || (pkgName.length() <= 0) ) return null;
                
                FileObject fo = findResource(pkgName.replace('.', '/'));
                if (fo != null) {
                    dob = getDataObject(fo);
                    if (dob != null) {
                        Node node = dob.getNodeDelegate();
                        if (node != null) {
                            found = true;
                            org.openide.nodes.NodeOperation.getDefault().explore(node); // explore the package
                        }
                        dob = null; // don't try to open dob
                    }
                }
                if (!found) {
                    ret = pkgName;
                }

            }
        } else if (item instanceof CsmClass) {
            CsmClass cls = (CsmClass)item;
            if (!CsmUtilities.isPrimitiveClass(cls)) {
                dob = getDataObject(cls);

                if (dob != null) {
                    found = true;
//                    elem = getClassElement(dob, cls);
                }
                if (!found) {
                    ret = getSourceName(cls);
                }
            }

        } else if (item instanceof CsmField) {
            CsmField fld = (CsmField)item;
            CsmClassifier cls = findDeclaration ? fld.getContainingClass() : fld.getType().getClassifier();
            if (!CsmUtilities.isPrimitiveClass(cls)) {
		// XXX
//                dob = getDataObject(cls);
//                
//                if (dob != null) {
//                    found = true;
//                    ClassElement ce = getClassElement(dob, cls);
//                    if (ce != null) {
//                        elem = JCExtension.findFieldElement(fld, ce);
//                    }
//                }
//                if (!found) {
//                    ret = getSourceName(cls);
//                }
            }
        } else if (item instanceof CsmConstructor) {
            CsmConstructor ctr = (CsmConstructor)item;
            CsmClass cls = ctr.getContainingClass();
            if (!CsmUtilities.isPrimitiveClass(cls)) {
                dob = getDataObject(cls);
                // XXX
//                if (dob != null) {
//                    found = true;
//                    ClassElement ce = getClassElement(dob, cls);
//                    if (ce != null) {
//                        elem = JCExtension.findConstructorElement(ctr, ce);
//                    }
//                }
                
                if (!found) {
                    ret = getSourceName(cls);
                }
            }

        } else if (item instanceof CsmMethod) {
            CsmMethod mtd = (CsmMethod)item;
            CsmClass cls = mtd.getContainingClass();
            if (!CsmUtilities.isPrimitiveClass(cls)) {
                dob = getDataObject(cls);
                // XXX
//                if (dob != null) {
//                    found = true;
//                    ClassElement ce = getClassElement(dob, cls);
//                    if (ce != null) {
//                        elem = JCExtension.findMethodElement(mtd, ce);
//                    }
//                }
//                if (!found) {
//                    ret = getSourceName(cls);
//                }
            }
	}

        // Add the current (probably opened) componetn to jump-list
        if (dob != null) {
//            openAtElement(dob, elem);
            NbEditorUtilities.addJumpListEntry(dob);
        }
        return ret;
    }

    
    protected URL getDocFileObjects(String fqName, String javadocFilename) {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        if (dobj == null) return null;
        FileObject fo = dobj.getPrimaryFile();
        
        
        // try to find class/package on classpath of the file being editted
        FileObject cpfo = null;
//        ClassPath cp = getMergedClassPath(fo);
//
//        // now ask the Javadoc query to get Javadoc for the object
//        String search = fqName.replace('.', '/');
//        if (javadocFilename != null) {
//            search += "/" + javadocFilename; // NOI18N
//        }
//        search += ".html"; // NOI18N
//        for (Iterator it = cp.entries().iterator(); it.hasNext();) {
//            ClassPath.Entry entry = (ClassPath.Entry)it.next();
//            URL urls[] = JavadocForBinaryQuery.findJavadoc(entry.getURL()).getRoots();
//            URL url = findResource(search, urls);            
//            if (url != null)
//                return url;
//        }
        return null;
    }
    
    /**
     * Creates merged Classpath, the classpath is
     * ClassPath.COMPILE + ClassPath.BOOT + build destination folder(s)
     * @param fo - source FileObject
     * @return ClassPath
     */
//    private static ClassPath getMergedClassPath (FileObject fo) {
//        final ClassPath scp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        List cps = new ArrayList(3);
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
//        if (cp != null)
//            cps.add(cp);
//        cp = ClassPath.getClassPath(fo, ClassPath.BOOT);
//        if (cp != null)
//            cps.add(cp);
//        List buildRoots;
//        cp = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
//        if (cp == null || scp == null) {
//            buildRoots = Collections.EMPTY_LIST;
//        } else {
//            buildRoots = new ArrayList (5);    //Mostly 1
//            for (Iterator it = cp.entries().iterator(); it.hasNext();) {
//                URL url = ((ClassPath.Entry)it.next()).getURL();
//                FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(url).getRoots();
//                for (int i=0; i<sourceRoots.length; i++) {
//                    if (scp.contains (sourceRoots[i])) {
//                        buildRoots.add (ClassPathSupport.createResource(url));
//                        break;
//                    }
//                }
//            }
//        }
//        if (buildRoots.size()!=0) {
//            cps.add(ClassPathSupport.createClassPath (buildRoots));
//        }
//        return ClassPathSupport.createProxyClassPath((ClassPath[])cps.toArray(new ClassPath[cps.size()]));
//    }

    
    private URL findResource(String resource, URL urls[]) {
        for (int i=0; i<urls.length; i++) {
            String base = urls[i].toExternalForm();
            if (!base.endsWith("/")) { // NOI18N
                base+="/"; // NOI18N
            }
            try {
                URL u = new URI(base+resource).toURL();
                FileObject fo = URLMapper.findFileObject(u);
                if (fo != null) {
                    return u;
                }
            } catch (Exception ex) {
                continue;
            }
        }
        return null;
    }
    
    public URL[] getJavaDocURLs(Object obj) {
        ArrayList urlList = new ArrayList();
        if (obj instanceof CsmNamespace) {
            CsmNamespace pkg = (CsmNamespace)obj;
            URL u = getDocFileObjects(pkg.getName(), PACKAGE_SUMMARY);
            if (u != null) {
                urlList.add(u);
            }
        } else if (obj instanceof CsmClass) {
            CsmClass cls = (CsmClass)obj;
            URL u = getDocFileObjects(cls.getQualifiedName(), null);
            if (u != null) {
                urlList.add(u);
            }
        } else if (obj instanceof CsmMethod) { // covers CsmConstructor too
            CsmMethod ctr = (CsmMethod)obj;
            CsmClass cls = ctr.getContainingClass();
            URL url = getDocFileObjects(cls.getQualifiedName(), null);
            if (url != null) {
                    StringBuffer sb = new StringBuffer("#"); // NOI18N
                    sb.append((obj instanceof CsmMethod) ? ((CsmMethod)ctr).getName() : cls.getName());
                    sb.append('('); //NOI18N
                    CsmParameter[] parms = (CsmParameter[]) ctr.getParameters().toArray(new CsmParameter[0]);
                    int cntM1 = parms.length - 1;
                    for (int j = 0; j <= cntM1; j++) {
			//XXX
                        //sb.append(parms[j].getType().format(true));
			sb.append(parms[j].getType().getText());
                        if (j < cntM1) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append(')'); //NOI18N
                    try {
                        urlList.add(new URL(url.toExternalForm() + sb));
                    } catch (MalformedURLException e) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                    }
            }
        } else if (obj instanceof CsmField) {
            CsmField fld = (CsmField)obj;
            CsmClass cls = fld.getContainingClass();
            URL u = getDocFileObjects(cls.getQualifiedName(), null);
            if (u != null) {
                try {
                    urlList.add(new URL(u.toExternalForm() + '#' + fld.getName()));
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, e.toString());
                }
            }
        }

        URL[] ret = new URL[urlList.size()];
        urlList.toArray(ret);
        return ret;
    }


    protected FileObject findResource(String resourceName) {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        assert dobj != null;
        FileObject fo = dobj.getPrimaryFile();
        
//        // try to find the resource on source classpath:
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        if (cp != null) {
//            FileObject fo2 = cp.findResource(resourceName);
//            if (fo2 != null) {
//                return fo2;
//            }
//        }
//        
//        // try to find the resource on boot classpath (ie. platform)
//        cp = ClassPath.getClassPath(fo, ClassPath.BOOT);
//        FileObject fo2 = findResourceInSources(cp, resourceName);
//        if (fo2 != null) {
//            return fo2;
//        }
//        
//        // try to find the resource on compilation classpath (ie.
//        // in sources of the libraries used on compilation classpath)
//        cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
//        fo2 = findResourceInSources(cp, resourceName);
//        if (fo2 != null) {
//            return fo2;
//        }
        
        return null;
    }

//    private FileObject findResourceInSources(ClassPath cp, String resourceName) {
//        if (cp == null) {
//            return null;
//        }
//        Iterator it = cp.entries().iterator();
//        while (it.hasNext()) {
//            ClassPath.Entry entry = (ClassPath.Entry)it.next();
//            FileObject[] sroots = SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots();
//            if (sroots.length > 0) {
//                ClassPath sources = ClassPathSupport.createClassPath (sroots);
//                FileObject fo2 = sources.findResource (resourceName);
//                if (fo2 != null) {
//                    return fo2;
//                }
//            }
//        }
//        return cp.findResource(resourceName);
//    }
    
//    class ParsingListenerImpl implements ParsingListener {
//        
//        public ParsingListenerImpl(){
//        }
//        
//        public void objectParsed(ParsingEvent evt){
//            if (evt==null) {
//                return;
//            }
//            DataObject dob = NbEditorUtilities.getDataObject(getDocument());
//            if (dob != null && dob == evt.getDataObject()) {
////                SourceElement se = evt.getSourceElement();
////                if (se==null) {
////                    return;
////                }
////                
////                SourceCookie sc = (SourceCookie)dob.getCookie(SourceCookie.class);
////                if (sc != null) {
////                    SourceElement seLocal = sc.getSource();
////                    if (seLocal != null && seLocal.equals(se)) {
////                        jcValid = false;
////                    }
////                }
//            }
//        }
//    }

//    /** Attach listener on java source hierarchy parser */
//    private static synchronized void initParsingListener(){
//        if (parsingListenerInitialized == false){
//            try {
//                final ClassLoader loader = (ClassLoader)org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
//                Class parsingClass = Class.forName(
//                    "org.netbeans.modules.java.Parsing", false, loader); //NOI18N
//                Class listenerClass = Class.forName(
//                    "org.netbeans.modules.java.Parsing$Listener", false, loader); //NOI18N
//                InvocationHandler ih =  new InvocationHandler(){
//                        public Object invoke(Object proxy, Method method, Object[] args) {
//                            if (args!=null && args[0]!=null){
//                                try{
//                                    Class parsingEventClass = args[0].getClass();
//                                    Method getJavaDataObjectMethod = parsingEventClass.getMethod(
//                                        "getJavaDataObject", EMPTY_CLASS_ARRAY); //NOI18N
//
//                                    DataObject dob = (DataObject)getJavaDataObjectMethod.invoke(
//                                        args[0], EMPTY_OBJECT_ARRAY);
//
//                                    if (dob != null) {
//                                        // getSourceElement() costly in refactoring
//                                        // so should be done only if dataObject matches
//                                        // Not checking se.getStatus()==SourceElement.STATUS_OK){
//                                        // as it's always STATUS_OK anyway in refactoring builds
//                                        fireParsingEvent(new ParsingEvent(dob));
//                                    }
//                                } catch (Throwable t) {
//                                    org.netbeans.editor.Utilities.annotateLoggable(t);
//                                }
//
//                            }
//                            return null;
//                        }
//                    };
//                Object proxyListener = java.lang.reflect.Proxy.newProxyInstance(loader, 
//                    new Class[] { listenerClass }, ih);
//                Method addParsingListener = parsingClass.getMethod(
//                    "addParsingListener",new Class[]{listenerClass});//NOI18N
//                addParsingListener.invoke(parsingClass, new Object[]{proxyListener});
//                parsingListenerInitialized = true;
//            } catch (Throwable t) {
//                org.netbeans.editor.Utilities.annotateLoggable(t);
//            }
//        }        
//    }
//    
//    /** The event class used in Listener. */
//    static class ParsingEvent extends java.util.EventObject {
//        
//        ParsingEvent(DataObject dob) {
//            super(dob);
//        }
//        
//        public DataObject getDataObject() {
//            return (DataObject)getSource();
//        }
//
//        /** @return the source element which was parsed. */
////        public SourceElement getSourceElement() {
////            SourceCookie sc = (SourceCookie)getDataObject().getCookie(SourceCookie.class);
////            return (sc != null) ? sc.getSource() : null;
////        }
//
//    }
//    
//    /** The listener interface for everybody who want to control all
//    * parsed JavaDataObjects.
//    */
//    static interface ParsingListener extends java.util.EventListener {
//        /** Method which is called everytime when some object is parsed.
//        * @param evt The event with the details.
//        */
//        public void objectParsed(ParsingEvent evt);
//    }
    
}
