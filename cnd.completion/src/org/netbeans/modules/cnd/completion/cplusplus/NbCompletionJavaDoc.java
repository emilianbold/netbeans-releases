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
import org.netbeans.modules.cnd.completion.spi.dynhelp.CCHelpManager;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

import org.netbeans.editor.ext.CompletionJavaDoc;
import org.netbeans.editor.ext.ExtEditorUI;

import java.net.URL;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.JavaDocPane;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;




/**
 *
 *  @author  Martin Roskanin
 *  @since 03/2002
 */
public class NbCompletionJavaDoc extends CompletionJavaDoc {

    ParsingThread task = null;
    public static final String CONTENT_NOT_FOUND = NbBundle.getMessage(NbCompletionJavaDoc.class, "javadoc_content_not_found"); //NOI18N
    private static boolean javaSourcesMounted = true;
    private static boolean inited = false;
    private String lastBase="";
    private boolean goToSourceEnabled = false;
    
    /** Creates a new instance of NbCompletionJavaDoc */
    public NbCompletionJavaDoc(ExtEditorUI extEditorUI) {
        super(extEditorUI);
    }

//    protected JCFinder getFinder() {
//        Document doc = Utilities.getDocument(extEditorUI.getComponent());
//        assert doc != null;
//        DataObject dobj = NbEditorUtilities.getDataObject(doc);
//        assert dobj != null;
//        FileObject fo = dobj.getPrimaryFile();
//        return JCFinderFactory.getDefault().getFinder(fo);
//    }
    
//    protected CompletionJavaDoc.JavaDocTagItem[] getJavaDocTags(JavaDocTag jdt[]){
//        CompletionJavaDoc.JavaDocTagItem ret [] = new CompletionJavaDoc.JavaDocTagItem[jdt.length];
//        for (int i=0; i<jdt.length; i++){
//            ret[i] = new JavaDocTagItemImpl(jdt[i].name(),jdt[i].text());
//        }
//        return ret;
//    }

    
    
    protected boolean isGoToSourceEnabled(){
        return goToSourceEnabled;
    }
    
    protected void setGoToSourceEnabled(boolean enabled){
        JavaDocPane jdp = getJavaDocPane();
        if (jdp instanceof CCScrollDocPane){
            ((CCScrollDocPane)jdp).setGoToSourceEnabled(enabled);
        }
    }

//    protected ClassElement getClassElement(String clsFullName){
//        ClassElement ce=null;
//        if (clsFullName == null) return null;
//        try {
//            BaseDocument bDoc = extEditorUI.getDocument();
//            if (bDoc == null) return null;
//            DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
//            if (dobj == null) return null;
//            FileObject fo = dobj.getPrimaryFile();
//            ce = ClassElement.forName(clsFullName, fo);
//        } catch ( ThreadDeath td ) {
//            throw td;
//        } catch (Throwable t) { // Parser sometimes sensitive to forName call !!!
//            System.err.println("Error occurred during name resolving"); // NOI18N
//            t.printStackTrace();
//        }
//        return ce;
//    }
    
    
    protected ParsingThread setInRequestProcessorThread(final Object content){
        ParsingThread pt = new ParsingThread(content);
        RequestProcessor.getDefault().post(pt);
        return pt;
    }

    public void cancelPerformingThread(){
        super.cancelPerformingThread();
        if (task!=null){
            task.stopTask();
        }
    }

    public synchronized void actionPerformed(ActionEvent e) {
        if (task!=null){
            task.stopTask();
        }
        task = setInRequestProcessorThread(getCurrentContent());
    }    

    /** Opens source of current javadoc in editor */
    public void goToSource(){
        SyntaxSupport sup = Utilities.getSyntaxSupport(extEditorUI.getComponent());
        NbCsmSyntaxSupport nbJavaSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
        if (nbJavaSup.openSource(getCurrentContent(), false) == null){
            extEditorUI.getCompletion().setPaneVisible(false);
        }
    }
    
    /** Opens javadoc in external browser */
    public void openInExternalBrowser(){
        SyntaxSupport sup = Utilities.getSyntaxSupport(extEditorUI.getComponent());
        NbCsmSyntaxSupport nbJavaSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);

        Object currentContent = getCurrentContent();
        
        if (currentContent instanceof URL){
            org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL((URL)currentContent);
        }else if (currentContent instanceof String){
            URL url = mergeRelLink(lastBase,(String)currentContent);
            if (url!=null){
                org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        }
        
        URL[] urls = nbJavaSup.getJavaDocURLs(currentContent);
        if (urls != null && urls.length > 0) {
            org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(urls[0]); // show first URL
        }
    }

//    protected String findProperClass(String name, String pkgName){
//        if (name==null) return null;
//        String ret = null;
//
//        if (pkgName!=null && pkgName.length()>0){
//            if (getClassElement(pkgName+"."+name) != null){ //NOI18N
//                return pkgName+"."+name; //NOI18N
//            }
//        }else{
//            if (getClassElement(name) != null){
//                return name;
//            }
//        }
//
//        List classes = getFinder().findClasses(null, name, true);
//        if (classes.size()>0){
//            ret = ((JCClass)classes.get(0)).getFullName();
//            // found in Code Completion DB, but it is not mounted
//            if (getClassElement(ret) == null) ret = null; 
//        }
//        
//        return ret;
//    }
//    
//    protected boolean isNotFullyQualifiedInnerClass (String inner, String pkgName){
//        if (inner.indexOf(".") != inner.lastIndexOf(".")){ //NOI18N
//            // fully qualified
//            return false;
//        }
//
//        return (getClassElement((pkgName.length()>0) ? (pkgName+"."+inner) : inner) != null); //NOI18N
//    }
//    
//    
//    protected List parseMethodTypes(String parameters){
//        ArrayList ret = new ArrayList();
//        if (parameters == null) return ret;
//        StringTokenizer st = new StringTokenizer(parameters,","); //NOI18N
//        while (st.hasMoreTokens()) {
//            String param = st.nextToken();
//            param.trim();
//            if (param.indexOf(".") < 0){ // NOI18N
//                //it could be i.e String without java.lang package
//                String testParam = "java.lang."+param; //NOI18N
//                if (testParam.indexOf("[")>0){ //NOI18N
//                    // if param is an array
//                    testParam = testParam.substring(0,testParam.indexOf("[")); // NOI18N
//                }
//                
//                ClassElement ce = getClassElement(testParam); //NOI18N
//                if (ce!=null){
//                    param = "java.lang."+param; //NOI18N
//                }
//                
//            }
//            ret.add(param);
//        }
//        
//        return ret;
//    }
    
    /** Returns true if javadoc is mounted in FS */
    public boolean  isExternalJavaDocMounted(){
        SyntaxSupport sup = Utilities.getSyntaxSupport(extEditorUI.getComponent());
        NbCsmSyntaxSupport nbJavaSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
        Object currentContent = getCurrentContent();
        if (currentContent instanceof URL || currentContent instanceof String){
            try{
                if (lastBase == null || lastBase.length() == 0) return false;
                FileObject fo = URLMapper.findFileObject(new URL(lastBase));
                if (fo != null) return true;
            }catch(MalformedURLException mue){
                mue.printStackTrace();
                return false;
            }
        }
        
        URL urls[] = nbJavaSup.getJavaDocURLs(currentContent);
        return (urls == null || urls.length < 1) ? false : true;
    }
    
    
    /** Parses given link such as <code>java.awt.Component#addHierarchyListener</code>
     *  and returns parsed Object
     *  @return Object of JCClass, JCMethod, JCConstructor or JCField 
     */
    public Object parseLink(String link, String clsFQN, String pkgName){
        if (pkgName == null) pkgName = "";//NOI18N
        link = link.trim();
//        ClassElement  linkClsElem;
//        Object  linkMember;
//        JCClass linkClass;
//        JCFinder finder = getFinder();
//        
//        if (link.indexOf("#") > -1 ){ //NOI18N
//            if (link.startsWith("#")){ //NOI18N
//                /* Referencing a member of the current class i.e:
//                 * @see  #field
//                 * @see  #method(Type, Type,...)
//                 * @see  #method(Type argname, Type argname,...)
//                 */
//
//                linkClsElem = getClassElement(clsFQN);
//                if (linkClsElem == null) return null;
//                linkClass = finder.getExactClass(clsFQN);
//                if (linkClass == null) return null;
//                
//            }else{
//                /* Referencing another class in the current or imported packages
//                 * @see  Class#field
//                 * @see  Class#method(Type, Type,...)
//                 * @see  Class#method(Type argname, Type argname,...)
//                 * @see  package.Class#field
//                 * @see  package.Class#method(Type, Type,...)
//                 * @see  package.Class#method(Type argname, Type argname,...)
//                 */
//                
//                String refCls = link.substring(0, link.indexOf("#")); //NOI18N
//
//                if (refCls.indexOf(".") < 0){ // NOI18N
//                    // it can be class in current package
//                    String curPkgCls = (pkgName.length()>0) ? pkgName+"."+refCls : refCls; //NOI18N
//                    if (getClassElement(curPkgCls) == null){
//                        // try to find a class via finder
//                        List outCls = finder.findClasses(null, refCls, true);
//                        if (outCls.size() > 0){
//                            refCls = ((JCClass) outCls.get(0)).getFullName();
//                        }
//                    }else{
//                        refCls = curPkgCls;
//                    }
//                }
//                linkClsElem = getClassElement(refCls);
//                if (linkClsElem == null) return null;
//                
//                linkClass = finder.getExactClass(linkClsElem.getName().getFullName());
//                if (linkClass == null) return null;
//            }
//
//            if (link.indexOf("(")>0){ //NOI18N
//            //method or constructor
//                String memberLink = link.substring(link.indexOf("#")+1) ;  //NOI18N
//                String memberFullName =  (memberLink.indexOf(" ")>0) ? memberLink.substring(0,memberLink.indexOf(" ")) : memberLink; //NOI18N
//                String memberName = memberLink.substring(0,memberLink.indexOf("(")); //NOI18N
//                String memberParams = null;
//                if (link.indexOf(")")>0){ //NOI18N
//                    memberParams = link.substring(link.indexOf("(")+1,link.indexOf(")"));  //NOI18N
//                }
//                List params = parseMethodTypes(memberParams);
//                Type types[] = new Type[params.size()];
//                try{
//                    for (int i=0; i<types.length; i++){
//                        types[i] = Type.parse((String)params.get(i));
//                    }
//                }catch(IllegalArgumentException iae){
//                    types = new Type[] {};
//                }
//
//                MethodElement me = linkClsElem.getMethod(Identifier.create(memberName), types);
//
//                if (me!=null){
//                    JCMethod mtds[] = linkClass.getMethods();
//                    for (int i = 0; i<mtds.length; i++){
//                        if (JCExtension.equals(mtds[i], me)){
//                            return mtds[i];
//                        }
//                    }
//                }
//                return null;
//            }else{
//            //field or method or constructor
//                String memberLink = link.substring(link.indexOf("#")+1) ; //NOI18N
//                String memberName =  (memberLink.indexOf(" ")>0) ? memberLink.substring(0,memberLink.indexOf(" ")) : memberLink; //NOI18N
//
//                // look for fields first. If it will be the method with the same name it should be named with ()
//                FieldElement fieldElement = linkClsElem.getField(Identifier.create(memberName));
//                if (fieldElement !=null){
//                    JCField flds[] = linkClass.getFields();
//                    for (int i = 0; i<flds.length; i++){
//                        if (JCExtension.equals(flds[i], fieldElement)) return flds[i];
//                    }
//                    return null;
//                }else{
//                    // Now try to find a method or constructor
//
//                    //process all super classes
//                    MethodElement me;
//                    ClassElement processedCE = linkClsElem;
//                    FileObject contextFo = NbEditorUtilities.getDataObject(extEditorUI.getDocument()).getPrimaryFile();
//                    do {
//                        me = processedCE.getMethod(Identifier.create(memberName), new Type[] {});
//                        Identifier superCls = processedCE.getSuperclass();
//                        processedCE = (superCls == null) ? null : ClassElement.forName(superCls.getFullName(),
//                                contextFo);
//                    }while(processedCE != null && me == null);
//                    
//                    // it could be method with some parameter
//                    if (me == null){
//                        // go through all super classes
//                        processedCE = linkClsElem;
//                        do{
//                            MethodElement mes[] = processedCE.getMethods();
//                            for (int i=0; i<mes.length; i++){
//                                
//                                if (Identifier.create(memberName).equals(mes[i].getName())){
//                                    // found, now just pick up the JCMethod
//                                    JCClass linkClassLoop = finder.getExactClass(processedCE.getName().getFullName());
//                                    
//                                    if (linkClassLoop != null){
//                                        JCMethod mtds[] = linkClassLoop.getMethods();
//                                        for (int j = 0; j<mtds.length; j++){
//                                            if (JCExtension.equals(mtds[j], mes[i])) {
//                                                return mtds[j];
//                                            }
//                                        }
//                                    }
//                                }
//                                
//                            }
//
//                            Identifier superCls = processedCE.getSuperclass();
//                            processedCE = (superCls == null) ? null : ClassElement.forName(superCls.getFullName(),
//                                    contextFo);
//                        }while (processedCE != null);
//                        
//                    }else{
//                        do {
//                            JCMethod mtds[] = linkClass.getMethods();
//                            for (int i = 0; i<mtds.length; i++){
//                                if (JCExtension.equals(mtds[i], me)) return mtds[i];
//                            }
//                            linkClass = linkClass.getSuperclass();
//                        }while(linkClass!=null);
//                    }
//                    return null;
//                }
//            }
//
//        }else{
//            /* no member available, it can be package or class i.e:
//             * @see  Class
//             * @see  package.Class
//             * @see  package 
//             */
//            String refCls = (link.indexOf(" ")>0) ? link.substring(0, link.indexOf(" ")) : link; //NOI18N
//            if (refCls.indexOf(".") > 0){ //NOI18N
//                // fully qualified name or inner class
//                linkClsElem = getClassElement(refCls);
//                if (linkClsElem == null) {
//                    refCls = findProperClass(refCls, pkgName);
//                 }
//            }else{
//                // class in current package
//                refCls = findProperClass(refCls, pkgName);
//            }
//
//            linkClsElem = getClassElement(refCls);
//            if (linkClsElem == null) return null;
//            linkClass = finder.getExactClass(linkClsElem.getName().getFullName());
//            return linkClass;
//        }
        return null;
    }

    protected URL mergeRelLink(String base, String rel){
        if (base == null || base.length() == 0) return null;
        try{
            return new URL(new URL(base),rel);
        }catch(MalformedURLException mue){
            mue.printStackTrace();
        }
        return null;
    }
    
    private NbCsmSyntaxSupport jmiSup;
    protected NbCsmSyntaxSupport getCsmSyntaxSupport() {
        if (jmiSup == null) {
            SyntaxSupport sup = Utilities.getSyntaxSupport(extEditorUI.getComponent());
            jmiSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
        }
        return jmiSup;
    }    
    
    class ParsingThread implements Runnable{
        
        Object content;
        boolean running = true;
        
        public ParsingThread(Object content){
            this.content = content;
        }
        
        void stopTask(){
            running = false;
        }
        
        protected String getBoldName(String description, String name){
            if (description.indexOf(name) > -1){
                StringBuffer sb = new StringBuffer();
                sb.append(description.substring(0, description.indexOf(name)));
                sb.append("<b>"+name+"</b>"); //NOI18N
                sb.append(description.substring(description.indexOf(name)+name.length()));
                return sb.toString().replace('$', '.'); //NOI18N                
            }
            return description;
        }

        
        
////        private MethodElement findOverridenMethod(MethodElement me) {
////            if (me == null) return null;
////            ClassElement declaringClass = me.getDeclaringClass();
////            if (declaringClass == null) return null;
////            
////            MethodParameter params[] = me.getParameters();
////            Type types[] = new Type[params.length];
////            for (int i = 0; i<params.length; i++){
////                types[i] = params[i].getType();
////            }
////
////            FileObject contextFo = NbEditorUtilities.getDataObject(extEditorUI.getDocument()).getPrimaryFile();            
////            Identifier parentIdent = declaringClass.getSuperclass();
////            if (parentIdent != null){
////                ClassElement parentClass = ClassElement.forName(parentIdent.getFullName(),
////                            contextFo);
////                if (parentClass != null){
////                    MethodElement m = parentClass.getMethod(me.getName(), types);
////                    if (m!=null) {
////                        return m;
////                    }
////                }
////            }
////            
////            // search interfaces
////            Identifier interfaceIdent[] = declaringClass.getInterfaces();
////            for (int i=0; i<interfaceIdent.length; i++){
////                ClassElement interfaceElem = ClassElement.forName(interfaceIdent[i].getFullName(),
////                            contextFo);
////                if (interfaceElem == null) continue;
////                MethodElement m = interfaceElem.getMethod(me.getName(), types);
////                if (m!=null) {
////                    return m;
////                }
////            }
////            return null;
////        }
////        
////        
////        
////        /** Wraps the given JCClass to anchor tag with appropriate href */
////        private String wrapClass(JCClass clazz){
////            if (clazz==null || clazz.toString().length()==0) return "";
////            String ret = clazz.toString();
////            SyntaxSupport sup = Utilities.getSyntaxSupport(extEditorUI.getComponent());
////            NbCsmSyntaxSupport nbJavaSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
////            if (nbJavaSup==null) return ret;
////            URL[] urls = nbJavaSup.getJavaDocURLs(clazz);
////            if (urls.length > 0 && urls[0]!=null){
////                ret = "<font size='+0'><b><A href='"+urls[0].toString()+"'>"+clazz.toString()+"</A></b></font>"; //NOI18N
////            }
////            return ret;
////        }
//        
//        /** Retireves class from javadoc URL and wraps it to the anchor tag format */
//        private  String wrapClass(String url){
//            if (url==null) return ""; //NOI18N
//            String parent = url;
//            int hashIndex = parent.lastIndexOf("#"); //NOI18N
//            if (hashIndex>0){
//                parent = parent.substring(0, hashIndex);
//            }
//            
//            StringBuffer sb  = new StringBuffer(url);
//            int htmlIndex = sb.indexOf(".html"); //NOI18N
//            if (htmlIndex>0){
//                sb.delete(htmlIndex, sb.length());
//            }
//            
//            for (int i=0; i<sb.length()-1; i++){
//                if (sb.charAt(i)=='/'){
//                    String subStr = sb.substring(i+1);
//                    subStr = subStr.replace('/','.');
//                    if (getFinder().getExactClass(subStr) != null){
//                        String ret = "<font size='+0'><b><A href='"+parent+"'>"+subStr+"</A></b></font>"; //NOI18N
//                        return ret;
//                    }
//                }
//            }
//            return "";
//        }
//        
//        private boolean tryMountedJavaDoc(URL url){
//            if (url!=null && !"nbfs".equals(url.getProtocol())){ //NOI18N
//                // ignore non-NBFS protocol URLs
//                return false;
//            }
//            String clazzInfo = "";
//            if (content instanceof JCMethod){
//                JCClass clazz = ((JCMethod)content).getClazz();
//                if (clazz!=null) clazzInfo = wrapClass(clazz);
//            } else if (content instanceof JCField){
//                JCClass clazz = ((JCField)content).getClazz();
//                if (clazz!=null) clazzInfo = wrapClass(clazz);
//            } else if (content instanceof JCConstructor){
//                JCClass clazz = ((JCConstructor)content).getClazz();
//                if (clazz!=null) clazzInfo = wrapClass(clazz);
//            }
//            
//            SyntaxSupport sup = Utilities.getSyntaxSupport(extEditorUI.getComponent());
//            NbCsmSyntaxSupport nbJavaSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
//            if (url==null){
//                URL[] urls = nbJavaSup.getJavaDocURLs(content);
//                if (urls.length > 0 && urls[0]!=null){
//                    url = urls[0];
//                }else{
//                    return false;
//                }
//            }
//
//            String urlStr = url.toString();
//            
//            if (clazzInfo.length() == 0){
//                clazzInfo = wrapClass(urlStr);
//            }
//
//            String textFromURL = HTMLJavadocParser.getJavadocText(url, content instanceof JCPackage);
//            
//            if (textFromURL!=null && textFromURL.length()>0){
//                if (!textFromURL.toUpperCase().startsWith("<DL>") && //NOI18N
//                   (!textFromURL.toUpperCase().startsWith("<PRE>"))) //NOI18N
//                    clazzInfo += "<BR>"; //NOI18N
//
//                String retrievedText = clazzInfo + textFromURL;
//                lastBase = getLastBase(urlStr);
//                goToSourceEnabled = false;
//                showJavaDoc(retrievedText);
//                return true;
//            }
//            
//            return false;
//        }
        
        
        protected String getLastBase(String urlStr){
            if (urlStr==null) return null;
            return urlStr.substring(0,urlStr.lastIndexOf('/')+1);
        }
        
        protected void javaDocNotFound(){
            if (alwaysDisplayPopup()) showJavaDoc(CONTENT_NOT_FOUND);
        }
        
        protected void showJavaDoc(final String preparedText){
            if (preparedText == null) {
                javaDocNotFound();
                return;
            }
            if (running){
                Runnable r = new Runnable() {
                    public void run() {
                        getJavaDocView().setContent(preparedText);
                        setGoToSourceEnabled(isGoToSourceEnabled());
                        if (running){
                            setJavaDocVisible(true);
                        }
                    }
                };
                Utilities.runInEventDispatchThread(r);
            }
        }

        
//        private void setMethod(MethodElement me, JCMethod jcMethod){
//            if (me == null || jcMethod == null){
//                if (tryMountedJavaDoc(null)) return;
//                goToSourceEnabled = false;                
//                javaDocNotFound();
//                return;
//            }
//            JavaDoc.Method jdMethod = me.getJavaDoc();
//            
//            String jdText = ""; //NOI18N
//            JavaDocTag jdTags[] = new JavaDocTag[0];
//            
//            if (jdMethod!=null){
//                jdTags = jdMethod.getTags();
//                jdText = jdMethod.getText();  
//            }
//            
//            MethodElement parentMethod = findOverridenMethod(me);
//            while(parentMethod !=null && (jdTags.length == 0 || jdText.length() == 0) ){
//                JavaDoc.Method parentJD = parentMethod.getJavaDoc();
//                if (parentJD!=null){
//                    if (jdText.length() == 0 ){
//                        jdText = parentJD.getText();
//                    }
//                    if (jdTags.length == 0){
//                        jdTags = parentJD.getTags();
//                    }
//                }
//                parentMethod = findOverridenMethod(parentMethod);
//            }
//
//            
//            boolean notFound = jdText.length() == 0 && jdTags.length == 0;
//            String preparedText = prepareJavaDocContent(jcMethod.getClazz(),
//            getBoldName(jcMethod.toString(), jcMethod.getName()),
//            (notFound) ? CONTENT_NOT_FOUND : jdText, 
//            (jdMethod == null) ? null : getJavaDocTags(jdTags));
//            if (notFound){
//                if (tryMountedJavaDoc(null)) return;
//                javaDocNotFound();
//                return;
//            }
//            
//            showJavaDoc(preparedText);
//        }
//        
//        private void setClass(ClassElement ce, JCClass cls){
//            if (ce == null) {
//                if (tryMountedJavaDoc(null)) return;
//                goToSourceEnabled = false;              
//                javaDocNotFound();
//                return;
//            }
//            JavaDoc.Class jdClass = ce.getJavaDoc();
//            boolean notFound = jdClass == null || (jdClass.getText().length() == 0 && jdClass.getTags().length == 0);
//            String preparedText = prepareJavaDocContent(cls,"",  //NOI18N
//            notFound ? CONTENT_NOT_FOUND : jdClass.getText(), 
//            (jdClass == null) ? null : getJavaDocTags(jdClass.getTags()));
//            if (notFound){
//                if (tryMountedJavaDoc(null)) return;
//                javaDocNotFound();
//                return;
//            }
//
//            showJavaDoc(preparedText);
//        }
//                
//        private void setField(FieldElement fe, JCField jcField){
//            if (fe == null || jcField == null) {
//                if (tryMountedJavaDoc(null)) return;                
//                goToSourceEnabled = false;                
//                javaDocNotFound();
//                return;
//            }
//            JavaDoc.Field jdField = fe.getJavaDoc();
//            boolean notFound = jdField == null || (jdField.getText().length() == 0 && jdField.getTags().length == 0);
//            String preparedText = prepareJavaDocContent(jcField.getClazz(),
//            getBoldName(jcField.toString(), jcField.getName()),
//            notFound ? CONTENT_NOT_FOUND : jdField.getText(),
//            (jdField == null) ? null : getJavaDocTags(jdField.getTags()));
//            if (notFound){
//                if (tryMountedJavaDoc(null)) return;
//                javaDocNotFound();
//                return;
//            }
//            showJavaDoc(preparedText);
//        }
//        
//        private void setConstructor(ConstructorElement conElem, JCConstructor jcConstuctor){
//            if (conElem == null || jcConstuctor == null) {
//                if (tryMountedJavaDoc(null)) return;                
//                goToSourceEnabled = false;                
//                javaDocNotFound();
//                return;
//            }
//            JavaDoc.Method jdMethod = conElem.getJavaDoc();
//            boolean notFound = jdMethod == null || ( jdMethod.getText().length() == 0 && jdMethod.getTags().length == 0 );
//            String preparedText = prepareJavaDocContent(jcConstuctor.getClazz(),
//            getBoldName(jcConstuctor.toString(), jcConstuctor.getClazz().getName()),
//            notFound ? CONTENT_NOT_FOUND : jdMethod.getText(),
//            (jdMethod == null) ? null : getJavaDocTags(jdMethod.getTags()));
//            if (notFound){
//                if (tryMountedJavaDoc(null)) return;
//                javaDocNotFound();
//                return;
//            }
//            showJavaDoc(preparedText);
//        }
        
        private void setCsmQualified(CsmQualifiedNamedElement csmClass) {
            if (csmClass == null) {
                javaDocNotFound();
            } else {
                setCsmDoc(csmClass.getQualifiedName());
            }
        }
        
        private void setCsmDoc(String fqn) {
            CCHelpManager helpManager = (CCHelpManager) Lookup.getDefault().lookup(CCHelpManager.class);
            if( helpManager == null ) {
                javaDocNotFound();
            } else {
                String preparedText = helpManager.getHelp(fqn);
                showJavaDoc(preparedText);
            }            
        }
        
        public void run(){
            goToSourceEnabled = true;
        
            // TODO: build different content based on Kind, not instanceof
            if (content instanceof CsmQualifiedNamedElement) {
                setCsmQualified((CsmQualifiedNamedElement)content);
//            } else if (content instanceof JCClass){
//                setClass(getClassElement(((JCClass)content).getFullName()), ((JCClass)content));
//            }else if(content instanceof JCField){
//                JCField fld = (JCField)content;
//                ClassElement ce = getClassElement(fld.getClazz().getFullName());
//                
//                if (ce == null){
//                    goToSourceEnabled = false;    
//                    if (!tryMountedJavaDoc(null)) javaDocNotFound();
//                    return;
//                }
//                setField(ce.getField(Identifier.create(((JCField)content).getName())), fld);
//                
//            }else if(content instanceof JCMethod){
//                JCMethod mtd = (JCMethod)content;
//                ClassElement ce = getClassElement(mtd.getClazz().getFullName());
//                
//                if (ce == null) {
//                    goToSourceEnabled = false;                    
//                    if (!tryMountedJavaDoc(null)) javaDocNotFound();
//                    return;
//                }
//                JCParameter jcp [] = mtd.getParameters();
//                try{
//                    Type types[] = new Type[jcp.length];
//                    for (int i=0; i<jcp.length; i++){
//                        String array = ""; //NOI18N
//                        for (int j = 0; j<jcp[i].getType().getArrayDepth(); j++){
//                            array += "[]"; //NOI18N
//                        }
//                        types[i] = Type.parse(jcp[i].getType().getClazz().getFullName()+array);
//                    }
//                    setMethod(ce.getMethod(Identifier.create(((JCMethod)content).getName()), types), mtd);
//                    
//                }catch(IllegalArgumentException iae){
//                    iae.printStackTrace();
//                }
//            }else if (content instanceof JCConstructor){
//                JCConstructor con = (JCConstructor)content;
//                ClassElement ce = getClassElement(con.getClazz().getFullName());
//                
//                if (ce == null) {
//                    goToSourceEnabled = false;                    
//                    if (!tryMountedJavaDoc(null)) javaDocNotFound();
//                    return;
//                }
//                JCParameter jcp [] = con.getParameters();
//                try {
//                    Type types[] = new Type[jcp.length];
//                    for (int i=0; i<jcp.length; i++){
//                        String array = ""; //NOI18N
//                        for (int j = 0; j<jcp[i].getType().getArrayDepth(); j++){
//                            array += "[]"; //NOI18N
//                        }
//                        types[i] = Type.parse(jcp[i].getType().getClazz().getFullName()+array);
//                    }
//                    
//                    setConstructor(ce.getConstructor(types), con);
//                    
//                }catch (IllegalArgumentException iae){
//                    iae.printStackTrace();
//                }
            }else if (content instanceof URL){
                URL u = (URL)content;
                // filter outgoing requests.
                if ("http".equals(u.getProtocol())) return; //NOI18N
                
//                if (!tryMountedJavaDoc((URL)content)){
//                    goToSourceEnabled = false;                                        
//                    javaDocNotFound();
//                }
                if (addToHistory) addToHistory(content);
            }else if (content instanceof String){
                goToSourceEnabled = false;                
                String strCon = (String) content;
                URL url = mergeRelLink(lastBase,strCon);
                if (url==null) {
                    return;
                }
                // filter outgoing requests.
                if ("http".equals(url.getProtocol())) return; //NOI18N
                
                if (addToHistory) addToHistory(url);
//                if (!tryMountedJavaDoc(url)) javaDocNotFound();
            }else{
                goToSourceEnabled = false;                
//                if (!tryMountedJavaDoc(null)) javaDocNotFound();
            }
        }
        
    }
    
    class JavaDocTagItemImpl implements CompletionJavaDoc.JavaDocTagItem{
        String name;
        String text;
        
        public JavaDocTagItemImpl(String name, String text){
            this.name = name;
            this.text = text;
        }
        
        public String getName(){
            return name;
        }
        
        public String getText(){
            return text;
        }
        
        public int compareTo(Object o) {
            if (this == o) {
                return 0;
            }
            JavaDocTagItemImpl p = (JavaDocTagItemImpl)o;
            return name.compareTo(p.getName());
        }

        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof JavaDocTagItemImpl) {
                return name.equals(((JavaDocTagItemImpl)o).getName());
            }
            if (o instanceof String) {
                return name.equals((String)o);
            }
            return false;
        }

        public String toString() {
            return name;
        }
    }
    
}
