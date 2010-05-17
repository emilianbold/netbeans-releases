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

//import org.netbeans.jmi.javamodel.CallableFeature;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleTypeVisitor6;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;

//import org.netbeans.modules.projects.CurrentProjectNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.EventManager;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodParameterInfo;


/**
 *  Utility functions for general NetBeans housekeeping. Try not to put in
 * functions here that should go into NBFileUtils, and vice versa. As a rule,
 * anything vaguely file-ish (involving DataObjects, FileObjects and suchlike)
 * should go into NBFileUtils.
 *
 * @author  Darshan
 * @version 1.0
 */
public final class NBUtils {
    //////////////////////////// Constants ////////////////////////////////
    /**
     *  The time that must elapse (in milliseconds) after an error message has
     * been displayed before an identical error message can be displayed to the
     * user.
     */
    public static final int ERROR_NOSPAM_TIME = 1500;
    private static Frame retFrame ;
    private static ResourceBundle bundle;
    static {
        try {
            bundle = ResourceBundle.getBundle("org.netbeans.modules.uml.integration.Bundle");
        }
        catch (MissingResourceException ex) {
            Log.stackTrace(ex);
        }
    }

    public static final String EDITOR_MODE_ID = bundle != null?
        bundle.getString("EDITOR_MODE_NAME") : "explorer";

    public static final String NETBEANS_PREFIX = "NB:";

    ////////////////////////// UI related functions ///////////////////////////
    /**
     *  Obtains the main Forte (NetBeans) window's Frame object.
     *
     * @return The <code>Frame</code> for the main window, or null if the main
     *         frame couldn't be obtained.
     */
    public static Frame getMainFrame()
    {
        SwingUtilities.invokeLater(new Runnable()
                {
            public void run()
            {
                try
                {
                    retFrame = WindowManager.getDefault().getMainWindow();
                } 
                catch (Exception ignored){}
            }
                });
        
        return retFrame;
    }

    /**
     * Determines whether the UI is busy. We consider the UI busy if any of
     * these conditions is true: <br/>
     * <ol>
     * <li>The NetBeans main frame is missing or hidden.</li>
     * <li>The main frame is visible, but contains one or more visible modal
     * child windows.</li>
     * </ol>
     * @return <code>true</code> if the UI is busy.
     */
    public static boolean isUIBusy() {
        Frame forteMainWindow;

        if ((forteMainWindow = NBUtils.getMainFrame()) == null
               || !forteMainWindow.isVisible())
            return true;

        Window[] windows = forteMainWindow.getOwnedWindows();
        if (windows == null || windows.length == 0) return false;
        for (int i = 0; i < windows.length; ++i) {
            if (windows[i].isShowing() && windows[i] instanceof Dialog)
                return true;
        }

        // Before stating that the UI isn't busy, dump all open windows
        Log.out("Open windows are :");
        for (int i = 0; i < windows.length; ++i) {
            Log.out(i + ") " + windows[i].getClass().getName());
        }
        return false;
    }

    /**
     *  Locates the first TopComponent of the given class in the given mode.
     * Note that subclasses of the given class will not be considered to match,
     * since we use Class object comparison (yup, that's evil and how NetBeans
     * thinks).
     *
     * @param mode  The NetBeans workspace mode that contains the desired
     *              TopComponent.
     * @param c     The Class of which the desired TopComponent is an instance.
     * @return  The TopComponent instance of the desired class, or null if the
     *          Mode is null, the Class is null or the given Mode does not
     *          contain a TopComponent of the given Class.
     */
    public static TopComponent getComponent(Mode mode, Class c) {
        if (mode == null || c == null)
            return null;

        TopComponent[] components = mode.getTopComponents();
        for(int i = 0; i < components.length; i++)
            if (components[i].getClass().equals(c))
                return components[i];

        return null;
    }

    /**
     * Locates the first TopComponent with the given class name in the given
     * mode.
     *
     * @param mode  The NetBeans workspace mode that contains the desired
     *              TopComponent.
     * @param className The name of the Class of which the desired TopComponent
     *              is an instance.
     * @return  The TopComponent instance of the desired class, or null if the
     *          Mode is null, the Class is null or the given Mode does not
     *          contain a TopComponent of the given Class.
     */
    public static TopComponent getComponent(Mode mode, String className) {
        if (mode == null || className == null)
            return null;

        TopComponent[] components = mode.getTopComponents();
        for(int i = 0; i < components.length; i++)
            if (components[i].getClass().getName().equals(className))
                return components[i];

        return null;
    }

    /**
     *  Locates the first TopComponent of the given class open in any of the
     * open workspaces. Note that subclasses of the given class will not be
     * considered to match.
     *
     * @param c    The Class of which the desired TopComponent is an instance.
     * @return  The TopComponent instance of the desired class, or null if the
     *          the Class is null or no open Workspaces contain a TopComponent
     *          of the given Class.
     */
    public static TopComponent getComponent(Class c) {
        if (c == null)
            return null;
            
        Set modes = WindowManager.getDefault().getModes();
        Iterator iter = modes.iterator();
        while (iter.hasNext())
        {
           Object obj = iter.next();
           if (obj instanceof Mode)
           {
              Mode mode = (Mode)obj;
              TopComponent tc = getComponent(mode, c);
              if (tc != null)
              {
                 return tc;
              }
           }
        }
        return null;
    }

    /**
     *  Locates the first TopComponent of the given class open in any of the
     * open workspaces. Note that subclasses of the given class will not be
     * considered to match.
     *
     * @param className The name of the class of which the desired TopComponent
     *                  is an instance.
     * @return  The TopComponent instance of the desired class, or null if the
     *          the Class is null or no open Workspaces contain a TopComponent
     *          of the given Class.
     */
    public static TopComponent getComponent(String className) {
        if (className == null)
            return null;

         Set modes = WindowManager.getDefault().getModes();
         Iterator iter = modes.iterator();
         while (iter.hasNext())
         {
            Object obj = iter.next();
            if (obj instanceof Mode)
            {
               Mode mode = (Mode)obj;
               TopComponent tc = getComponent(mode, className);
               if (tc != null)
               {
                  return tc;
               }
            }
         }

        return null;
    }


    /**
     * Finds the mode with the given id in the current NetBeans workspace.
     * @param modeId The mode id <code>String</code>
     * @return The <code>Mode</code> found.
     */
    public static Mode getMode(String modeId) {
        try {
            Mode mode = WindowManager.getDefault().findMode(modeId);
            return mode;
        }
        catch (Exception ex) {
            Log.stackTrace(ex);
            return null;
        }
    }

    public static void dumpMode(Mode mode) {
        TopComponent[] tc = mode.getTopComponents();
        Log.out("Dumping mode " + mode);
        if (tc != null)
        {
           for (int i = 0; i < tc.length; ++i) 
           {
               Log.out("         " + i + ") " + tc[i].getName() + " ("
                       + tc[i].getClass().getName() + ")");
           }
        }
    }

    /**
     *  Retrieves the first instance of the Describe system tree in the given
     * Mode.
     *
     * @param mode The Mode to search in.
     * @return A <code>GDSystemTreeComponent</code> if found, or null if the
     *         Mode contains no Describe system trees.
     */
//    public static GDSystemTreeComponent getTree(Mode mode) {
//        return (GDSystemTreeComponent) getComponent(mode,
//                                    GDSystemTreeComponent.class);
//    }

    /**
     *  Installs the given actions into the supplied toolbar, instantiating
     * the actions if necessary, but reusing the actions found in the toolbar
     * if possible.
     *
     * @param toolbar The DataFolder in which to insert the actions.
     * @param actions An array of Class objects (subclasses of
     *                CallableSystemAction)
     * @throws IOException
     */
    public static void installActions(DataFolder toolbar, Class[] actions) {
        if (toolbar == null || actions == null) return ;

        InstanceDataObject[] order = new InstanceDataObject[actions.length];

        // createAction won't create the action if it already exists, so we
        // don't need to check if the action is already instantiated.
        try {
            for (int i = 0; i < order.length; ++i)
                order[i] = createAction(actions[i], toolbar, i);

            // Force the toolbar to use the same order of buttons as we want.
            toolbar.setOrder(order);
        } catch (IOException e) {
            Log.stackTrace(e);
        }
    }

    /**
     * A method which creates action in specified folder without setting order.
     *
     * @param actionClass the class of the action to add to the menu
     * @param folder the folder representing the Menu in which the action
     *               should be added
     * @param index  The index at which the action should be created.
     * @return The newly created action.
     * @exception IOException is throws when there is a problem creating the
     *        .instance files on the underlying filesystem
     * @see #removeAction
     * @notdeprecated Use of XML filesystem layers to install actions is
     *                preferred.
     */
    public static InstanceDataObject createAction(Class actionClass,
                                                  DataFolder folder,
                                                  int index)
                throws IOException {
        String actionName = actionClass.getName (),
        instanceName = actionName;

        if (actionClass == JToolBar.Separator.class)
            instanceName += index;

        return InstanceDataObject.create (folder, instanceName, actionName);
    }

    /**
     *  Displays an error message to the user, using the given error code to
     * obtain a localized message from the default resource bundle, using
     * MessageFormat on the resulting message if arguments are specified, and
     * using the given title for the error dialog (again, the title is localized
     * against the default resource bundle).
     *
     * @param errCode A resource bundle key for the error message.
     * @param args    The Object arguments for MessageFormat, or null if
     *                MessageFormat need not be used.
     * @param title   The resource key for the text of the error dialog's title.
     *                If null, this defaults to "Errors.title".
     */
//    public static void showErrCode(String errCode, Object[] args,
//                                   String title) {
//        showErrCode(errCode, args, title, false);
//    }

    /**
     *  Displays an error message to the user, using the given error code to
     * obtain a localized message from the default resource bundle, using
     * MessageFormat on the resulting message if arguments are specified, and
     * using the given title for the error dialog (again, the title is localized
     * against the default resource bundle).
     *
     * @param errCode  A resource bundle key for the error message.
     * @param args     The Object arguments for MessageFormat, or null if
     *                 MessageFormat need not be used.
     * @param title    The resource key for the text of the error dialog's
     *                 title. If null, this defaults to "Errors.title".
     * @param dontSpam If <code>true</code>, error messages will be suppressed
     *                 if they are identical in user-visible text (the text of
     *                 the error dialog's title is not considered significant)
     *                 to the last error message actually displayed to the user
     *                 (with dontSpam true), and the last dontSpam == true call
     *                 was made less than ERROR_NOSPAM_TIME milliseconds
     *                 before.
     */
    public static void showErrCode(String errCode, Object[] args, String title,
                                   boolean dontSpam) {
        
        String errorMesg = bundle.getString(errCode);
        if (args != null) errorMesg = MessageFormat.format(errorMesg, args);
        if (title == null) title = "Errors.title";

        if (dontSpam) {
            long lastInstant = lastErrorTime;

            lastErrorTime = System.currentTimeMillis();
            if (lastError != null && lastError.equals(errorMesg)
                    && (lastErrorTime - lastInstant) < ERROR_NOSPAM_TIME)
                return ;

            lastError = errorMesg;
        }

        final String titleStr = title;
        final String message = errorMesg;
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(
                        getMainFrame(),
                        message,
                        bundle.getString(titleStr),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        );
    }

    /**
     *  Convenience function for showErrCode(String, Object[], String, boolean),
     * with dontSpam set to true.
     *
     * @see #showErrCode(String, Object[], String, boolean)
     * @param errCode As in showErrCode
     * @param args As in showErrCode
     * @param title As in showErrCode
     */
    public static void showSpamlessErrCode(String errCode, Object[] args,
                                           String title) {
        showErrCode(errCode, args, title, true);
    }

    /**
     * Logs the fully-qualified class names of the given component, and
     * all child components (assuming the component is an AWT container),
     * traversing recursively down.
     *
     * @param c The <code>Component</code> to dump information on.
     */
    public static void dumpComponent(Component c) {
        dumpComponent(c, 0);
    }

    /**
     * Traverses the AWT component hierarchy for the given component, and
     * returns the first child component that is an instance of the given Class
     * object.
     *
     * @param top   The <code>Component</code> to search
     * @param clazz The <code>Class</code> of the desired component.
     * @return A <code>Component</code> that is an instance of the given
     *         Class, or null if none was found.
     */
    public static Component getComponent(Component top, Class clazz) {
        if (top == null || clazz == null) return null;
        if (clazz.isInstance(top)) return top;

        if (top instanceof Container) {
            Component[] children = ((Container)top).getComponents();
            for (int i = 0; i < children.length; ++i) {
                Component c = getComponent(children[i], clazz);
                if (c != null) return c;
            }
        }
        return null;
    }

    /**
     * Strips the first ampersand character from the given string.
     * @param name A <code>String</code>, possibly containing '&amp;'
     *        characters.
     * @return The given String, with the first '&amp;' character removed.
     */
    public static String stripAmpersand(String name)  {
        if (name == null) return null;

        int pos = name.indexOf('&');
        if (pos >= 0) {
            StringBuffer buf = new StringBuffer(name);
            buf.deleteCharAt(pos);
            return buf.toString();
        }

        return name;
    }

    /**
     * Strips the extension from a filename.
     *
     * @param name A filename
     * @return The filename without the extension.
     */
    public static String stripExt(String name) {
        if (name == null) return null;

        int dotPos = name.lastIndexOf('.');
        return dotPos == -1? name : name.substring(0, dotPos);
    }

    public static char getMnemonic(String name) {
        if (name == null) return '\0';

        int pos = name.indexOf('&');
        return pos == -1 || pos == name.length() - 1?
            '\0' : name.charAt(pos + 1);
    }

    public static  String getPackageName(String fullQName)
    {
        String retVal = "";
        if(fullQName==null || fullQName.length()<=0)
            return retVal;
        int lastIndex = fullQName.lastIndexOf('.');
        if(lastIndex>0)
        retVal = fullQName.substring(0,lastIndex);
        return retVal;
    }

    ////////////////////////// End UI functions ///////////////////////////////

    //////////////////// Source code utility functions ////////////////////////
    /**
     *  Returns the fully qualified decorated name of the given ClassElement.
     * @param clazz The ClassElement.
     * @return <code>null</code> if clazz is null, else the fully qualified
     *         decorated name. Decorated names use '.' to separate package
     *         names and '$' to separate (inner) class names.
     */
    /* NB60TBD 
    public static String getDecoratedName(ClassElement clazz) {
        if (clazz == null) return null;

        String pack = getPackageName(clazz);
        String name = getTypeQualifiedName(clazz);
        return pack.length() > 0? (pack + "." + name) : name;
    }
    */

    /* NB60TBD 
    public static String getDecoratedName(JavaClass clazz) {
        if (clazz == null) return null;
       */  /*fully qualified decorated name minus the package prefix*/ /* NB60TBD 
        String typeQualifiedName = "";
        
        String fullName = clazz.getName();
        String packageName = getPackageName(clazz.getName());
        if(packageName!=null && packageName.length()>0 && fullName.startsWith(packageName + "."))
            typeQualifiedName = fullName.substring(packageName.length() + 1);
        
        return packageName.length() > 0? (packageName + "." + typeQualifiedName) : typeQualifiedName;

    }
*/
    /**
     *  Returns the package name for the given ClassElement.
     * @param clazz The ClassElement.
     * @return <code>null</code> if clazz is null, else the package name ("" for
     *         ClassElements in the default package). The name will always be
     *         trimmed.
     */
    /* NB60TBD 
    public static String getPackageName(ClassElement clazz) {
        if (clazz == null) return null;

        SourceElement source = clazz.getSource();
        Identifier id = source.getPackage();
        return (id != null ? id.getFullName().trim() : "");
    }
    */
    /**
     * Returns the name of the current project.
     * @return The <code>String</code> name of the current project.
     */
//    public static String getProjectName() {
//        try {
//            CurrentProjectNode proj = CurrentProjectNode.getDefault();
//            String name = proj.getName();
//            return name;
//        }
//        catch (Exception e) {
//            Log.stackTrace(e);
//        }
//        return null;
//    }

    /**
     *  Returns the fully qualified decorated name <em>minus</em> the package
     * prefix.
     *
     * @param clazz The ClassElement from which the type-qualified name is to
     *              be obtained.
     * @return <code>null</code> if clazz is null, or the decorated name without
     *         a package prefix. Note that inner class names will be separated
     *         by '$'.
     */
    /* NB60TBD 
    public static String getTypeQualifiedName(ClassElement clazz) {
        if (clazz == null) return null;

        String pack     = getPackageName(clazz);
        Identifier name = clazz.getName();

        return getTypeQualifiedName(pack, name, null);
    }
    */
    /**
     *  Returns the fully qualified decorated name of a ClassElement (given its
     * name Identifier) <em>minus</em> the package prefix.
     *
     * @param packageName The package in which the ClassElement lives. If null,
     *                    the default package is assumed. Leading and trailing
     *                    spaces will be stripped.
     * @param id  The name Identifier of the ClassElement for which the type-
     *            qualified name is to be obtained.
     * @param clazz A fallback ClassElement whose type-qualified name will be
     *              returned if packageName does not prefix the Identifier id's
     *              fullName. A null clazz is permitted.
     * @return <code>null</code> if id is null; otherwise the decorated name
     *         without a package prefix. It is assumed that id is the name
     *         Identifier of the class in question, whose fully qualified name
     *         is prefixed by packageName; if that isn't the case,
     *         <code>getTypeQualifiedName(clazz)</code> is returned, if clazz is
     *         non-null, or id.getFullName() if clazz is null (this is almost
     *         always a bad thing). Note that inner class names will be
     *         separated by '$' (all '.' are replaced by '$') in all cases.
     */
    /* NB60TBD 
    public static String getTypeQualifiedName(String packageName,
                                              Identifier id,
                                              ClassElement clazz) {
        if (id == null) return null;

        if (packageName == null)
            packageName = "";
        else
            packageName = packageName.trim();

        String fullName = id.getFullName();

        if (packageName.length() > 0) {
            if (fullName.startsWith(packageName + "."))
                fullName = fullName.substring(packageName.length() + 1);
            else if (clazz != null)
                return getTypeQualifiedName(clazz);
        }

        return fullName.replace('.', '$');
    }
    */
    /**
     *  Retrieves a ConstructorElement for the given MethodInfo. This works
     * for both constructors and normal methods.
     *
     * @param method The MethodInfo for the method to be located. The
     *               MethodInfo must have a non-null containing class.
     * @return A <code>ConstructorElement</code> for the method, if found, or
     *         <code>null</code> otherwise.
     */
    /*public static ConstructorElement getMethod(MethodInfo method) {
        // EARLY EXIT
        if (method == null || method.getContainingClass() == null) return null;

        ClassInfo clazz = method.getContainingClass();
        return getMethod(method, method.getName().equals(
                JavaClassUtils.getInnerClassName(clazz.getName())));
    }*/

    /**
     *  Retrieves a ConstructorElement for the given MethodInfo.
     *
     * @param method The MethodInfo for the method to be located. The
     *               MethodInfo must have a non-null containing class.
     * @param isConstructor Use <code>true</code> if you're looking for a
     *                      constructor, <code>false</code> if you're looking
     *                      for an ordinary method.
     * @return A <code>ConstructorElement</code> for the method, if found, or
     *         <code>null</code> otherwise.
     */
    /*public static ConstructorElement getMethod(MethodInfo method,boolean isConstructor){
        ConstructorElement mElement = null;
        Log.out("Entering method NBEventProcessor :: methodExists");
        Log.out("Looking for method " + method.toString());
        ClassInfo clazz = method.getContainingClass();
        ClassElement classElement = fileUtils.findClass(clazz);
        if ( classElement == null ){
            Log.err(" Unable to get the class Element " );
            return null;
        }

        String methodName = method.getName();
        MethodParameterInfo[] mPars = method.getParameters();

        ConstructorElement[] allMethods = isConstructor ?
                classElement.getConstructors() : classElement.getMethods();

methodHunt:
        for ( int j = 0 ; j < allMethods.length ; j++ ){
            if(!allMethods[j].getName().getName().equals(methodName))
                continue;
            MethodParameter[] pars = allMethods[j].getParameters();
            if ( pars.length != mPars.length )
                continue;
            if (method.isMutator()) return allMethods[j];
            for (int x = 0; x < pars.length; x++) {
                if (!JavaClassUtils
                        .getInnerClassName(mPars[x].getType())
                        .equals(
                            JavaClassUtils.getInnerClassName(
                                pars[x].getType().getSourceString())))
                    continue methodHunt;
            }
            mElement = allMethods[j];
            break;
        }
        return mElement;
    }*/
    

    /**
     *  Retrieves an ElementHandle for the given MethodInfo.
     *
     * @param method The MethodInfo for the method to be located. The
     *               MethodInfo must have a non-null containing class.
     * @param isConstructor Use <code>true</code> if you're looking for a
     *                      constructor, <code>false</code> if you're looking
     *                      for an ordinary method.
     * @return An <code>ElementHandle</code> for the method, if found, or
     *         <code>null</code> otherwise.
     */
    public static ElementAndFile getMethod(MethodInfo method, final boolean isConstructor)
    {
        
        Log.out("Entering method NBEventProcessor :: methodExists");
        Log.out("Looking for method " + method.toString());
        final String methodName =method.getName();
	if (methodName == null) 
	    return null;
        MethodParameterInfo[] paraminfo = method.getParameters();
        final List<String> typeList_FullyQualified = new ArrayList<String>();
        final List<String> typeList = new ArrayList<String>();
        for (int i = 0; i < paraminfo.length; i++)
        {
	    typeList_FullyQualified.add(paraminfo[i].getCodeGenType(true));
	    typeList.add(paraminfo[i].getCodeGenType(false));
        }

        ClassInfo clazz = method.getContainingClass();
        final ElementAndFile javaClazz=NBFileUtils.findJavaClass(clazz);
	if (javaClazz == null || javaClazz.getElementHandle() == null 
	    || javaClazz.getFileObject() == null) 
	{
	    return null;
	}
	JavaSource src = JavaSource.forFileObject(javaClazz.getFileObject());
	if (src == null) 
	    return null;

	ElementAndFile retVal = getMethod(methodName, typeList_FullyQualified, isConstructor, src, javaClazz, true);
	if (retVal == null) {
	    retVal = getMethod(methodName, typeList, isConstructor, src, javaClazz, false);
	}
	return retVal;
    }


    public static ElementAndFile getMethod(final String methodName, 
					   final List<String> typeList, 
					   final boolean isConstructor,
					   JavaSource src, 
					   final ElementAndFile javaClazz,
					   final boolean fullyQualified) 
    {
	
	final ElementAndFile[] retVal = new ElementAndFile[1];
	try {
	    src.runUserActionTask(new CancellableTask<CompilationController>() {
		public void run(CompilationController cc) {
		    Element clazz = javaClazz.getElementHandle().resolve(cc);
		    List<? extends Element> elements = clazz.getEnclosedElements();
		    if (elements == null) 
			return; 
		    List<ExecutableElement> methods;
		    if (isConstructor) {
			methods = ElementFilter.constructorsIn(elements);
		    } else {
			methods = ElementFilter.methodsIn(elements);
		    }
		    if (methods == null) 
			return;
		    int typeListSize = typeList.size();
		    Iterator<ExecutableElement> iter = methods.iterator();
		    while(iter.hasNext()) {
			ExecutableElement method = iter.next();
			if (method.getSimpleName() == null 
			    || ( ( ! isConstructor) &&  (! method.getSimpleName().contentEquals(methodName)))) 
			{
			    continue;
			}
			List<? extends VariableElement> tparms = method.getParameters();			
			if (method.getParameters().size() != typeListSize) {
			    continue;
			}
			boolean paramsMatch = true;
			Iterator<String> listIter = typeList.iterator();
			Iterator<? extends VariableElement> tparmIter = tparms.iterator();
			while(listIter.hasNext() && tparmIter.hasNext()) {
			    VariableElement tparm = tparmIter.next();
			    String listName =  listIter.next();
			    if ( ! listName.contentEquals(getTypeName(tparm.asType(), fullyQualified))) {
				paramsMatch = false;
				break;
			    }
			}
			if (paramsMatch) {
			    retVal[0] = new ElementAndFile(ElementHandle.create(method),
							   cc.getFileObject());
			    return;					    
			}
		    }
		}
		public void cancel() {
		}	
	    }, true);
	} catch (Exception e) {
	    Log.stackTrace(e);	    
	}
	return retVal[0];

    }
    


    private static String umlTypeToType(String umlType) {
        
        String retVal = umlType;
        retVal = retVal.replaceAll("::", ".");
        
        int index = umlType.indexOf('[');
        String sName = index > -1 ? umlType.substring(0, index) : umlType;
        if ("String".equals(sName)) { // NOI18N
            String suffix = index > -1 ? umlType.substring(index) : "";
            retVal = "java.lang.String" + suffix; // NOI18N
        }
        return retVal;
    }
    
    /* NB60TBD 
    private static CallableFeature resolveMethod(MethodInfo method, 
                                                 JavaClass clazz,
                                                 boolean isConstructor)
    {
        CallableFeature retVal = null;
        
        List features = clazz.getFeatures();
        for(Object curObj : features)
        {
            if((curObj instanceof Method) ||
              (curObj instanceof Constructor))
            {
                CallableFeature clazzMethod = (CallableFeature)curObj;
                String curName = clazzMethod.getName();
                
                boolean haveConstructor = (curObj instanceof Constructor) && 
                                          (isConstructor == true);
                
                if((haveConstructor == true) || 
                   (curName != null && curName.equals(method.getName()) == true))
                {
                    List params = clazzMethod.getParameters();
                    int curNumOfParams = params.size();
                    
                    if(curNumOfParams == method.getParameters().length)
                    {     
                        boolean foundIt = true;
                        
                        MethodParameterInfo[] paramInfos = method.getParameters();
                        for(int index = 0; index < paramInfos.length; index++)
                        {
                            Parameter param = (Parameter)params.get(index);
                            MethodParameterInfo paramInfo = paramInfos[index];
                            
                            String curTypeName = param.getType().getName();
                            String infoTypeName = paramInfo.getType();                            
                            
                            String shortName = curTypeName;
                            
                            int innerNameIndex = shortName.lastIndexOf('$');
                            if(innerNameIndex > -1)
                            {
                                shortName = shortName.substring(innerNameIndex + 1);
                            }
                            
                            int nameIndex = shortName.lastIndexOf('.');
                            if(nameIndex > -1)
                            {
                                shortName = shortName.substring(nameIndex + 1);
                            }
                            
                            
                            if(((infoTypeName.equals(curTypeName) == false)) &&
                               (infoTypeName.equals(shortName) == false))
                            {
                                foundIt = false;
                                break;
                            }
                        }
                        
                        if(foundIt == true)
                        {
                            retVal = clazzMethod;
                        }
                    }
                }
            }
//            else if(curObj instanceof Constructor)
//            {
//                Constructor clazzMethod = (Constructor)curObj;
//                String curName = clazzMethod.getName();
//                if(curName.equals(method.getName()) == true)
//                {
//                    List params = clazzMethod.getParameters();
//                    int curNumOfParams = params.size();
//                    
//                    if(curNumOfParams == method.getParameters().length)
//                    {     
//                        boolean foundIt = true;
//                        
//                        MethodParameterInfo[] paramInfos = method.getParameters();
//                        for(int index = 0; index < paramInfos.length; index++)
//                        {
//                            Parameter param = (Parameter)params.get(index);
//                            MethodParameterInfo paramInfo = paramInfos[index];
//                            
//                            String curTypeName = param.getType().getName();
//                            String infoTypeName = paramInfo.getType();
////                            if(curTypeName.equals(paramInfo.getType()) == false)
////                            {
////                                foundIt = false;
////                                break;
////                            }
//                            String shortName = curTypeName;
//                            int nameIndex = curTypeName.lastIndexOf('.');
//                            if(nameIndex > -1)
//                            {
//                                shortName = curTypeName.substring(nameIndex + 1);
//                            }
//                            
//                            if(infoTypeName.equals(shortName) == false)
//                            {
//                                foundIt = false;
//                                break;
//                            }
//                        }
//                        
//                        if(foundIt == true)
//                        {
//                            retVal = clazzMethod;
//                        }
//                    }
//                }
//            }
        }
        
        return retVal;
    }
    */
    
//    public static CallableFeature getCallableFeature(MethodInfo method,boolean isConstructor) {
//        CallableFeature mElement = null;
//        Log.out("Entering method NBEventProcessor :: getCallableFeature");
//        Log.out("Looking for method " + method.toString());
//        ClassInfo clazz = method.getContainingClass();
//        JavaClass classElement = fileUtils.findJavaClass(clazz);
//        if ( classElement == null ){
//            Log.err(" Unable to get the class Element " );
//            return null;
//        }
//
//        String methodName = method.getName();
//        MethodParameterInfo[] mPars = method.getParameters();
//
//methodHunt:
//        for (Iterator it = classElement.getContents().iterator(); it.hasNext();){
//            Object obj = it.next();
//            if (obj instanceof CallableFeature) {
//                CallableFeature cf = (CallableFeature) obj;
//                if ((isConstructor && (cf instanceof Method))
//                    || (!isConstructor && ((cf instanceof Constructor) || (methodName.equals(cf.getName()))))) 
//                        continue;
//                Collection pars = cf.getParameters();
//                if (pars.size() != mPars.length) {
//                    continue;
//                }
//                if (method.isMutator()) return cf;
//                int x = 0;
//                for (Iterator it2 = pars.iterator(); it.hasNext(); x++) {
//                    if (!JavaClassUtils.getInnerClassName(mPars[x].getType()).equals(
//                        JavaClassUtils.getInnerClassName(((Parameter) it.next()).getType().getName())))
//                            continue methodHunt;
//                }
//                mElement = cf;
//                break;
//            }
//        }
//        return mElement;
//    }

    /**
     *  Retrieves a FieldElement for the given MemberInfo.
     *
     * @param attr The <code>MemberInfo</code> for which the FieldElement must
     *             be located.
     * @return The <code>FieldElement</code> corresponding to the MemberInfo, or
     *         <code>null</code> if a FieldElement could not be found.
     */
/* NB60TBD 
    public static FieldElement getFieldElement(MemberInfo attr) {
        if (attr == null) return null;
        ClassInfo ci = attr.getContainingClass();
        if (ci == null) return null;

        ClassElement ce = fileUtils.findClass(ci);
        if (ce == null) return null;

        return ce.getField(Identifier.create(attr.getName()));
    }
*/    


    public static ElementAndFile getField(final MemberInfo attr) {
        if (attr == null || attr.getName() == null) return null;
        ClassInfo ci = attr.getContainingClass();
        if (ci == null) return null;

        final ElementAndFile ce = fileUtils.findJavaClass(ci);
        if (ce == null || ce.getElementHandle() == null || ce.getFileObject() == null) return null;

	JavaSource src = JavaSource.forFileObject(ce.getFileObject());
	if (src == null) return null;

	final ElementAndFile[] retVal = new ElementAndFile[1];
	try {
	    src.runUserActionTask(new CancellableTask<CompilationController>() {
		public void run(CompilationController cc) {
		    Element clazz = ce.getElementHandle().resolve(cc);
		    List<? extends Element> elements = clazz.getEnclosedElements();
		    if (elements == null) return; 
		    List<VariableElement> fields = ElementFilter.fieldsIn(elements);
		    if (fields == null) return;
		    Iterator<VariableElement> iter = fields.iterator();
		    while(iter.hasNext()) {
			VariableElement nxt = iter.next();
			if (nxt.getSimpleName() != null 
			    && nxt.getSimpleName().contentEquals(attr.getName())) 
			{
			    retVal[0] = new ElementAndFile(ElementHandle.create(nxt),
							   cc.getFileObject());
			    return;
			}		    
		    }
		}
		public void cancel() {
		}	
	    }, true);
	} catch (Exception e) {
	    Log.stackTrace(e);	    
	}
	return retVal[0];
    }



    /**
     *  Updates a ClassInfo to the Describe model.
     * @param ci The <code>ClassInfo</code> containing information to be updated
     *           to the Describe model.
     */
/* NB60TBD 
    synchronized public static void update(final ClassInfo ci) {
        // EARLY EXIT
        if (EventManager.isRoundTripActive()) return ;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    ci.update();
                } catch (Exception e) {
                    Log.stackTrace(e);
                } finally {
                }
            }
        };
        UMLSupport.getUMLSupport().getRoundtripQueue().queueRunnable(r);
    }
*/
    //////////////////// End source code utility functions ////////////////////

    ///////////////////////////////////////////////////////////////////////
    // No public methods or attributes beyond this point!
    ///////////////////////////////////////////////////////////////////////
    /**
     *  Not intended to be instantiated. Static class, and all that sort of
     * stuff.
     */
    private NBUtils() {
    }

    private static String getIndent(int level) {
        char[] indent = new char[level * 2];
        return new String(indent);
    }

    private static void dumpComponent(Component c, int level) {
        if (c != null) {
            Log.out("DC: " + getIndent(level) + c.getClass().getName());

            if (c instanceof Container) {
                Component[] children = ((Container)c).getComponents();
                ++level;
                for (int i = 0; i < children.length; ++i)
                    dumpComponent(children[i], level);
            }
        }
    }
    
    /**
     * Determeines whether Describe and Describe project is connected
     */
//    private static boolean isLocalAndGlobalConnected() {
//        //		we want to switch to this tab only if we are globally connected
//        if (ProjectController.isConnected()) {
//            if (!ProjectController.isProjectConnected()) {
//                return false;
//            }
//        }
//        else {
//            return false;
//        }
//        return true;
//    }

    /**
     * The last error dialog message text shown to the user.
     */
    private static String  lastError     = null;

    /**
     * The time (as returned by System.currentTimeMillis()) when the last error
     * message was displayed.
     */
    private static long    lastErrorTime = 0;

    /**
     * Our instance of NBFileUtils, since we may need file services ourselves.
     */
    private static final NBFileUtils fileUtils = new NBFileUtils();

  
    //
    // original source of getTypeName() can be found 
    // in org.netbeans.modules.editor.java.Utilities
    //

    public static CharSequence getTypeName(TypeMirror type, boolean fqn) {
	if (type == null)
            return ""; //NOI18N
        return new TypeNameVisitor(false).visit(type, fqn);
    }
    
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static final String ERROR = "<error>"; //NOI18N
    private static final String UNKNOWN = "<unknown>"; //NOI18N

    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {
        
        private boolean varArg;
        
        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }
        
        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }
        
        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext())
                            DEFAULT_VALUE.append(", "); //NOI18N
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;                
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }
                        
        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return DEFAULT_VALUE.append(name);
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getLowerBound();
            if (bound != null && bound.getKind() != TypeKind.NULL) {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            } else {
                bound = t.getUpperBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.TYPEVAR)
                        bound = ((TypeVariable)bound).getLowerBound();
                    visit(bound, p);
                }
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    visit(bound, p);
                } else {
                    bound = SourceUtils.getBound(t);
                    if (bound != null) {
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    } else {
                        DEFAULT_VALUE.append(p ? " extends java.lang.Object" : " extends Object"); //NOI18N                        
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }


}
