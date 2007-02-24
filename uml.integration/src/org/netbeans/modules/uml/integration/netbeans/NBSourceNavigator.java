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

package org.netbeans.modules.uml.integration.netbeans;

import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import java.awt.List;

import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.netbeans.jmi.javamodel.CallableFeature;
import org.netbeans.jmi.javamodel.ClassMember;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.src.ClassElement;
import org.openide.src.ConstructorElement;
import org.openide.src.FieldElement;
import org.openide.src.JavaDoc;
import org.openide.src.MemberElement;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.NbDocument;
//import org.openide.windows.TopComponent;


import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.integration.netbeans.actions.SourceNavigateAction;
import org.netbeans.modules.uml.integration.ide.SourceNavigator;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
//import org.netbeans.modules.uml.integration.netbeans.actions.NBSourcePaneAction;
/**
 * The class NBSourceNavigator implements the interface
 * {@link com.embarcadero.integration.SourceNavigator SourceNavigator} to
 * give the NetBeans (FFJ) integration the ability to navigate source code
 * elements.  When the integration navigate to a source code element the editor
 * window will not gain focus.
 *
 * @author  Trey Spiva
 * @version 1.0
 */
public class NBSourceNavigator implements SourceNavigator
{
    
    private int lineNoOffset = 0;
    
    /** Creates new GDSourceNavigator */
    public NBSourceNavigator()
    {
        Log.entry("Entering function NBSourceNavigator::NBSourceNavigator");
    }
    
    /**
     *  Implementation for navigating to the forte source file
     *  @param cli ClassInfo object
     */
    public void navigateTo(ClassInfo cli)
    {
        if(SourceNavigateAction.Round_Trip)
        {
            Log.out("In navigateTo class " + cli.getName());
//		ClassElement clazz = getClassElement(cli);
            JavaClass clazz = getJavaClass(cli);
            if(clazz != null /*FIXME:&& NBSourcePaneAction.isNavigable*/)
                showSourceFile(clazz);
        }
        SourceNavigateAction.Round_Trip=false;
    }
    
    /**
     *  Implementation for navigating to the method/operation in the forte source file
     *  @param methodInfo MethodInfo object
     */
    public void navigateTo(MethodInfo methodInfo)
    {
        if(SourceNavigateAction.Round_Trip)
        {
            Log.entry("Entering function NBSourceNavigator::navigateTo Method");
            Log.out(" To get the method " + methodInfo.getName());
            ClassInfo clsInfo = methodInfo.getContainingClass();
            lineNoOffset = methodInfo.getLineNo();
			String name = clsInfo.getName();
			// in case the class is an inner class take the simple name, #6323040
			name = name.substring(name.lastIndexOf(".")+1);
            boolean isConstructor = methodInfo.getName().equals(name);
//			ConstructorElement consElement =  NBUtils.getMethod(methodInfo,isConstructor);
            CallableFeature consElement =  NBUtils.getMethod(methodInfo,isConstructor);
            if ( consElement != null /*FIXME:&& NBSourcePaneAction.isNavigable*/)
            {
                Log.out("Got the method displaying..");
                try
                {
                    showSourceFile(consElement);
                }
                catch (Exception e)
                {
                    Log.stackTrace(e);
                }
            }
        }
        SourceNavigateAction.Round_Trip=false;
    }
    
    
    /**
     *  Implementation for navigating to the member/field in the forte source file
     *  @param memberInfo MemberInfo object
     */
    public void navigateTo(MemberInfo memberInfo)
    {
        if(SourceNavigateAction.Round_Trip)
        {
            Log.entry("Entering function NBSourceNavigator::navigateTo Member");
            Log.out("The member is " + memberInfo.getName());
//    		FieldElement fEle = NBUtils.getFieldElement(memberInfo);
            Field fEle = NBUtils.getField(memberInfo);
            if  ( fEle != null /*FIXME:&& NBSourcePaneAction.isNavigable*/)
            {
//    			Log.out(" Got the field element !!! " + fEle.toString());
                showSourceFile(fEle);
            }
        }
        SourceNavigateAction.Round_Trip=false;
    }
    
    
    //////////////////////////  Helper methods ///////////////////////////////////
    
    /**
     * Opens up the source editor and navigates to the source element.
     * @param element The element to navigate to.
     * @deprecated .
     */
    /*protected void showSourceFile(final MemberElement element) {
        Log.entry("Entering function NBSourceNavigator::showSourceFile");
//        if(isMaximized())
//            return;
        if(element != null) {
            Log.out("In showSourceFile .. " + element.getClass().getName());
            final EditorCookie editor = (EditorCookie)element.getCookie(EditorCookie.class);
            final LineCookie lineCookie = (LineCookie)element.getCookie(LineCookie.class);
            final SourceCookie.Editor sEditor = (SourceCookie.Editor)element.getCookie(SourceCookie.Editor.class);
     
            // I can not call editor.open() because that will cause us to switch workspaces
            // if the file has a form associated with it.
            if(lineCookie != null) {
                // I will first test if the document is available.  If the document
                // is not currently availiable I will have to wait on the opening of
                // the document.  The only way I know how to block on the loading of
                // the document is by calling show on a Line object.
                final Line.Set lineSet = lineCookie.getLineSet();
                SwingUtilities.invokeLater(new Runnable(){
     
                    public void run() {
                        lineSet.getCurrent(0).show(Line.SHOW_SHOW);
     
                    }});
//                lineSet.getCurrent(0).show(Line.SHOW_SHOW);
     
                SwingUtilities.invokeLater(new Runnable() {
                   public void run()
                   {
                      if(editor != null) {
                          try {
                              // Convert the source code element to the text element.  With the text I
                              // can find the position in the file to jump to.
                              StyledDocument doc = editor.getDocument ();
                              Element textElement  = sEditor.sourceToText(element);
     
                              int lineNum = NbDocument.findLineNumber(doc,
                              textElement.getStartOffset());
                              annotate(element, lineSet, lineNum);
                              //NavAnnotation.getInstance().attach(gotoLine);
                              //annotate(element, lineSet);
                          } catch (Exception e) {
                              Log.stackTrace(e);
                          }
                      }
                   }
                });
            }
        }
    }*/
    protected void showSourceFile(final ClassMember element)
    {
        Log.entry("Entering function NBSourceNavigator::showSourceFile");
        
        if(element != null)
        {
            Log.out("In showSourceFile .. " + element.getClass().getName());
            
            Resource source = element.getResource();
            FileObject obj = JavaModel.getFileObject(source);
            
            DataObject dataObj = null;
            try
            {
                dataObj = DataObject.find(obj);
            }
            catch (DataObjectNotFoundException e1)
            {
                // TODO Auto-generated catch block
                Log.stackTrace(e1);
            }
            if (dataObj == null)
                return;
            
            final EditorCookie editor = (EditorCookie)dataObj.getCookie(EditorCookie.class);
            final LineCookie lineCookie = (LineCookie)dataObj.getCookie(LineCookie.class);
            final SourceCookie.Editor sEditor = (SourceCookie.Editor)dataObj.getCookie(SourceCookie.Editor.class);
            
            final Line.Set lineSet = lineCookie.getLineSet();
//            SwingUtilities.invokeLater(new Runnable(){
//                    public void run() {
//                        lineSet.getCurrent(0).show(Line.SHOW_SHOW);
//                    }});
//                lineSet.getCurrent(0).show(Line.SHOW_SHOW);
            
            final int startOffset = element.getStartOffset();            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if(editor != null)
                    {
                        try
                        {
                            // Convert the source code element to the text element.  With the text I
                            // can find the position in the file to jump to.
//                           StyledDocument doc = editor.getDocument ();
                            
//                           int lineNum = JavaMetamodel.getManager().getElementPosition(element).getBegin().getLine();
                            editor.open();
//                            int startOffset = element.getStartOffset();
                            int line = NbDocument.findLineNumber(editor.getDocument(), startOffset);
                            lineSet.getCurrent(line).show(Line.SHOW_GOTO);
                            lineNoOffset = 0;
                        }
                        catch (IndexOutOfBoundsException ex)
                        {
                            lineSet.getOriginal(0).show(Line.SHOW_GOTO);
                        }
                        catch (Exception e)
                        {
                            Log.stackTrace(e);
                        }
                    }
                }
                
            });
        }
    }
    
//FIXME:
//    private boolean isMaximized() {
//        TopComponent ptree = GDSystemTreeComponent.getDefault();
//        TopComponent wspace = DesignCenterComponent.getDefault();
//
//        if ( (ptree != null && ptree.isShowing()) ||
//             (wspace != null && wspace.isShowing()) ) {
//            return false;
//        }
//
//        return true;
//    }
    
    /*protected JavaDoc getJavaDoc(MemberElement el) {
        if (el instanceof ClassElement)
            return ((ClassElement) el).getJavaDoc();
        else if (el instanceof ConstructorElement)
            return ((ConstructorElement) el).getJavaDoc();
        else if (el instanceof FieldElement)
            return ((FieldElement) el).getJavaDoc();
        return null;
    }*/
    protected org.netbeans.jmi.javamodel.JavaDoc getJavaDoc(ClassMember el)
    {
        if (el instanceof JavaClass)
            return ((JavaClass) el).getJavadoc();
        else if (el instanceof Constructor)
            return ((Constructor) el).getJavadoc();
        else if (el instanceof Field)
            return ((Field) el).getJavadoc();
        return null;
    }
    
    protected void annotate(ClassMember element, Line.Set lineset)
    {
        try
        {
//            int lineNum = JavaMetamodel.getManager().getElementPosition(element).getBegin().getLine();
            int startOffset = element.getStartOffset();
            NavAnnotation nav = NavAnnotation.getInstance();
            nav.detach();
//            Line annotant = lineset.getCurrent(lineNum + lineNoOffset);
            Line annotant = lineset.getCurrent(startOffset);
            lineNoOffset = 0;
            annotant.show(Line.SHOW_GOTO);
            nav.attach(annotant);
        }
        catch (Exception e)
        {
            Log.stackTrace(e);
        }
    }
    
    /**
     * Finds the ClassElement that represents the class symbol.  The method
     * will only operate on CLD_Class symbols.
     * @param sym The symbol used to find a ClassElement.
     * @deprecated Use getJavaClass(ClassInfo clazz) instead.
     */
    protected ClassElement getClassElement(ClassInfo clazz)
    {
        Log.entry("Entering function NBSourceNavigator::getClassElement");
        
        NBFileUtils util = new NBFileUtils();
        ClassElement retVal = util.findClass(clazz);
        
        StatusDisplayer status = StatusDisplayer.getDefault();
        if(retVal == null)
        {
            // Test if we can find the source file.  If so, display an error message.
//FIXME:
//            if(util.findSourceFile(clazz) != null) {
//                status.setStatusText(DescribeModule.getString("Errors.Navigation.NoClassInFile"));
//            } else {
//                status.setStatusText(DescribeModule.getString("Errors.Navigation.NoSourceFile"));
//            }
        }
        else
        {
            status.setStatusText("");
        }
        
        return retVal;
    }
    protected JavaClass getJavaClass(ClassInfo clazz)
    {
        Log.entry("Entering function NBSourceNavigator::getClassElement");
        
        NBFileUtils util = new NBFileUtils();
        JavaClass retVal = util.findJavaClass(clazz);
        
        StatusDisplayer status = StatusDisplayer.getDefault();
        if(retVal == null)
        {
            // Test if we can find the source file.  If so, display an error message.
//FIXME:
//            if(util.findSourceFile(clazz) != null) {
//                status.setStatusText(DescribeModule.getString("Errors.Navigation.NoClassInFile"));
//            } else {
//                status.setStatusText(DescribeModule.getString("Errors.Navigation.NoSourceFile"));
//            }
        }
        else
        {
            status.setStatusText("");
        }
        
        return retVal;
    }
    
    private static final class NavAnnotation extends Annotation
    {
        private static NavAnnotation instance = null;
        
        private NavAnnotation()
        {
        }
        
        public static NavAnnotation getInstance()
        {
            if (instance == null)
                instance = new NavAnnotation();
            return instance;
        }
        
        public String getShortDescription()
        {
            // What would a good tooltip be, anyway?
            return "";
        }
        
        public String getAnnotationType()
        {
            // NOI18N - what's I18Nable about this, anyway?
            return "com-embarcadero-netbeans-nav";
        }
    }
}
