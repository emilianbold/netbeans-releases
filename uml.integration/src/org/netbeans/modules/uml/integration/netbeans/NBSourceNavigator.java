/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.integration.netbeans;

//import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import java.awt.List;

import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.UiUtils;

import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
//import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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
 * SourceNavigator to
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
            ElementAndFile clazz = getJavaClass(cli);
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
            // ?? lineNoOffset = methodInfo.getLineNo();
			String name = clsInfo.getName();
			// in case the class is an inner class take the simple name, #6323040
			name = name.substring(name.lastIndexOf(".")+1);
            boolean isConstructor = methodInfo.getName().equals(name);
//			ConstructorElement consElement =  NBUtils.getMethod(methodInfo,isConstructor);
            ElementAndFile consElement =  NBUtils.getMethod(methodInfo,isConstructor);
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
            ElementAndFile fEle = NBUtils.getField(memberInfo);
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


    protected void showSourceFile(ElementAndFile element)
    {
        Log.entry("Entering function NBSourceNavigator::showSourceFile");
        
        if(element != null)
        {

	    UiUtils.open(element.getFileObject(), element.getElementHandle());

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

    /* NB60TBD
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
    */    

    /* NB60TBD
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
    */    

    /**
     * Finds the ClassElement that represents the class symbol.  The method
     * will only operate on CLD_Class symbols.
     * @param sym The symbol used to find a ClassElement.
     * @deprecated Use getJavaClass(ClassInfo clazz) instead.
     */
    /* NB60TBD
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
    */

    protected ElementAndFile getJavaClass(ClassInfo clazz)
    {
        Log.entry("Entering function NBSourceNavigator::getClassElement");
        
        NBFileUtils util = new NBFileUtils();
        ElementAndFile retVal = util.findJavaClass(clazz);
        
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
