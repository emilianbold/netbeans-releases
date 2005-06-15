/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx;

import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Enumeration;

import java.util.HashMap;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.JavaModelPackage;

import org.netbeans.modules.jmx.mbeanwizard.mbeanstructure.MBeanOperationException;

import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * Helper class : all methods are static.
 */
public class WizardHelpers
{   
    
    /**
     * Checks if a name is a valid class name.
     * @param className <CODE>String</CODE> the class name to check.
     * @return <CODE>boolean</CODE> true if className is a valid class name.
     */
    public static boolean isValidClassName(String className) {
        if (className.length() == 0) {
            return false;
        }
        char[] chars = className.toCharArray();
        int segmentStart = 0;
        int i;
        for (i = 0; i < chars.length; i++) {
            if (chars[i] == '.') {
                if (i == segmentStart) {
                    return false;         //empty segment
                }
                if (!Utilities.isJavaIdentifier(
                        className.substring(segmentStart, i))) {
                    return false;         //illegal name of the segment
                }
                segmentStart = i + 1;
            }
        }
        if (i == segmentStart) {
            return false;                 //empty last segment
        }
        if (!Utilities.isJavaIdentifier(
                className.substring(segmentStart, chars.length))) {
            return false;                 //illegal name of the last segment
        }
        return true;
    }
    
    /**
     * Builds a map that containing relation between <code>SourceGroup</code>s
     * and their respective test <code>SourceGroup</code>s.
     * Each entry of the map contains a <code>SourceGroup</code> as a key
     * and an array of test <code>SourceGroup</code>s returned by
     * <code>UnitTestForSourceQuery</code> for that <code>SourceGroup</code>
     * as a value. <code>SourceGroup</code>s that have no test
     * <code>SourceGroup</code>s assigned are omitted, i.e. the resulting
     * map does not contain entries that would have empty arrays as their
     * values.
     *
     * @param  project           return only <code>SourceGroup</code>s
     *                           - ignore test folders not having
     *                           a corresponding <code>SourceGroup</code>
     * @param  sourceGroupsOnly  return only <code>SourceGroup</code>s
     *                           - ignore test folders not having
     *                           a corresponding <code>SourceGroup</code>
     * @return  created map - may be empty, may be unmodifiable,
     *                        cannot be <code>null</code>
     */
    public static Map getSourcesToTestsMap(Project project,
            final boolean sourceGroupsOnly) {
        /*
         * Idea:
         * 1) Get all SourceGroups
         * 2) For each SourceGroup, ask UnitTestForSourceQuery for its related
         *    test SourceGroups
         */

        /* .) get all SourceGroups: */
        final SourceGroup[] sourceGroups = getSourceGroups(project);
        if (sourceGroups.length == 0) {
            return Collections.EMPTY_MAP;
        }

        /* .) get test SourceGroups for each SourceGroup: */
        Map foldersToSourceGroupsMap = 
                createFoldersToSourceGroupsMap(sourceGroups);
        Object testTargetsUnion[] = new Object[sourceGroups.length];
        Map map = new HashMap((int) (sourceGroups.length * 1.33f + 0.5f), .75f);
        for (int i = 0; i < sourceGroups.length; i++) {
            Object[] testTargets = getTestTargets(sourceGroups[i],
                    foldersToSourceGroupsMap,project,sourceGroupsOnly);
            if (testTargets.length != 0) {
                map.put(sourceGroups[i], testTargets);
            }
        }
        if (map.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        if (map.size() == 1) {
            Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        }

        final int finalMapSize = map.size();
        if (finalMapSize >= (sourceGroups.length - 5)) {
            return map;
        }
        
        final Map targetMap;
        targetMap = new HashMap((int) (finalMapSize * 1.25f + .5f), .8f);
        targetMap.putAll(map);
        return targetMap;
    }

    /**
     * Returns test targets for the given <code>SourceGroup</code>.
     * The test targets are folders which are searched when tests for a class
     * from the <code>SourceGroup</code> are to be found. Each of the returned
     * test targets may be either <code>SourceGroup</code> (representing
     * a folder plus additional information such as display name) or simply
     * a <code>FileObject</code> representing a folder.
     * If parameter <code>includeSourceGroups</code> is <code>false</code>,
     * only <code>SourceGroup<code>s are returned (target folders without
     * corresponding <code>SourceGroup</code>s are ignored).
     *
     * @param  src  source group to find test targets for
     * @param  sourceGroupsOnly  skip target folders without matching
     *                           <code>SourceGroup</code>
     * @return  array which may contain <code>FileObject</code>s
     *          or <code>SourceGroup</code>s (or both);
     *          it may be empty but not <code>null</code>
     * @see  TestUtil#getFileObject2SourceGroupMap
     */
    private static Object[] getTestTargets(SourceGroup sourceGroup,
                                    Map foldersToSourceGroupsMap,
                                    Project project,
                                    final boolean sourceGroupsOnly) {
        
        /* .) get URLs of target SourceGroup's roots: */
        URL[] rootURLs = null;
        try {
            rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        }catch(java.lang.NoSuchMethodError e) {
            //OK We are running in 4.0.
            
            URL tmp = UnitTestForSourceQuery.findUnitTest(sourceGroup.getRootFolder());
            if(tmp != null) {
                rootURLs = new URL[1];
                rootURLs[0] = tmp;
            } else
                rootURLs = new URL[0];
            
        }
        
        if (rootURLs.length == 0) {
            return new Object[0];
        }
        
        /* .) convert the URLs to FileObjects: */
        FileObject[] sourceRoots = new FileObject[rootURLs.length];
        int count = 0;
        for (int i = 0; i < rootURLs.length; i++) {
            if ((sourceRoots[i] = URLMapper.findFileObject(rootURLs[i]))
                    == null) {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(
                        severity,
                        new IllegalStateException(
                           "No FileObject found for the following URL: "//NOI18N
                           + rootURLs[i]));
                }
                continue;
            }
            if (FileOwnerQuery.getOwner(sourceRoots[i]) != project) {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(
                        severity,
                        new IllegalStateException(
                    "Source root found by FileOwnerQuery points "       //NOI18N
                    + "to a different project for the following URL: "  //NOI18N
                    + rootURLs[i]));
                }
                continue;
            }
            count++;
        }
        
        /* .) find SourceGroups corresponding to the FileObjects: */
        final Object[] targets = new Object[count];
        int targetIndex = 0;
        for (int i = 0; i < sourceRoots.length; i++) {
            final FileObject sourceRoot = sourceRoots[i];
            if (sourceRoot == null) {
                continue;
            }
            Object srcGroup = foldersToSourceGroupsMap.get(sourceRoot);
            targets[targetIndex++] = (srcGroup != null)
                                     ? srcGroup
                                     : sourceGroupsOnly ? null : sourceRoot;
        }
        return skipNulls(targets);
    }
    
    /**
     * Creates a map mapping folders to source groups.
     * For a folder as a key, the map returns the source group having that
     * folder as a root. The created map is stored to variable
     * {@link #foldersToSourceGroupsMap}.
     *
     * @param  sourceGroup  source group to create a map from
     * @author  Marian Petras
     */
    private static Map createFoldersToSourceGroupsMap(
            final SourceGroup[] sourceGroups) {
        Map result;

        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }

        return result;
    }
    
    /**
     * Returns the equivalent object type of the type if it is primitive type
     * else the same type.
     * @param type <CODE>String</CODE> a type name 
     * @return <CODE>String</CODE> an Object type (not primitive type).
     */
    public static String getTypeObject(String type) {
        String resultType = type;
        if (type.equals(WizardConstants.BOOLEAN_NAME)) {
            resultType = WizardConstants.BOOLEAN_OBJ_NAME;
        } else if (type.equals(WizardConstants.BYTE_NAME)) {
            resultType = WizardConstants.BYTE_OBJ_NAME;
        } else if (type.equals(WizardConstants.CHAR_NAME)) {
            resultType = WizardConstants.CHAR_OBJ_NAME;
        } else if (type.equals(WizardConstants.INT_NAME)) {
            resultType = WizardConstants.INTEGER_OBJ_NAME;
        } else if (type.equals(WizardConstants.LONG_NAME)) {
            resultType = WizardConstants.LONG_OBJ_NAME;
        }
        return resultType;
    }
    
    private static final String TYPE = ".TYPE";
    private static final String CLASS = ".class";
    private static final String GETNAME = ".getName()";
    
    /**
     * Returns the full type name code needed of the type.
     * @param type <CODE>String</CODE> a type name
     * @return <CODE>String</CODE> code to get full type name.
     */
    public static String getFullTypeName(String type) {
        String resultType = type;
        if (type.equals(WizardConstants.BOOLEAN_NAME)) {
            resultType = WizardConstants.BOOLEAN_OBJ_FULLNAME + TYPE + GETNAME;
        } else if (type.equals(WizardConstants.BOOLEAN_OBJ_NAME)) {
            resultType = WizardConstants.BOOLEAN_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.BYTE_NAME)) {
            resultType = WizardConstants.BYTE_OBJ_FULLNAME + TYPE + GETNAME;
        } else if (type.equals(WizardConstants.BYTE_OBJ_NAME)) {
            resultType = WizardConstants.BYTE_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.CHAR_NAME)) {
            resultType = WizardConstants.CHAR_OBJ_FULLNAME + TYPE + GETNAME;
        } else if (type.equals(WizardConstants.CHAR_OBJ_NAME)) {
            resultType = WizardConstants.CHAR_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.INT_NAME)) {
            resultType = WizardConstants.INTEGER_OBJ_FULLNAME + TYPE + GETNAME;
        } else if (type.equals(WizardConstants.INTEGER_OBJ_NAME)) {
            resultType = WizardConstants.INTEGER_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.LONG_NAME)) {
            resultType = WizardConstants.LONG_OBJ_FULLNAME + TYPE + GETNAME;
        } else if (type.equals(WizardConstants.LONG_OBJ_NAME)) {
            resultType = WizardConstants.LONG_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.VOID_NAME)) {
            resultType = WizardConstants.VOID_OBJ_FULLNAME + TYPE + GETNAME;
        } else if (type.equals(WizardConstants.DATE_OBJ_NAME)) {
            resultType = WizardConstants.DATE_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.STRING_OBJ_NAME)) {
            resultType = WizardConstants.STRING_OBJ_FULLNAME + CLASS + GETNAME;
        } else if (type.equals(WizardConstants.OBJECTNAME_NAME)) {
            resultType = WizardConstants.OBJECTNAME_FULLNAME + CLASS + GETNAME;
        } else {
            resultType = type + CLASS + GETNAME;
        }
        return resultType;
    }
    
    /**
     * Returns true if this type is primitive, else returns false
     * @param type <CODE>String</CODE> a type name
     * @return <CODE>boolean</CODE> true if it is a primitive type.
     */
    public static boolean isPrimitiveType(String type) {
        boolean result = false;
        if (type.equals(WizardConstants.BOOLEAN_NAME)) {
            result = true;
        } else if (type.equals(WizardConstants.BYTE_NAME)) {
            result = true;
        } else if (type.equals(WizardConstants.CHAR_NAME)) {
            result = true;
        } else if (type.equals(WizardConstants.INT_NAME)) {
            result = true;
        } else if (type.equals(WizardConstants.LONG_NAME)) {
            result = true;
        }
        return result;
    }
    
    /**
     * Returns the default value of this type
     * @param type <CODE>String</CODE> a type name
     * @return <CODE>String</CODE> code which returns the corresponding default value.
     */
    public static String getDefaultValue(String type) {
        String resultValue = "null";
        if (type.equals(WizardConstants.BOOLEAN_NAME)) {
            resultValue = "false";
        } else if (type.equals(WizardConstants.BYTE_NAME)) {
            resultValue = "(byte)0";
        } else if (type.equals(WizardConstants.CHAR_NAME)) {
            resultValue = "(char)0";
        } else if (type.equals(WizardConstants.INT_NAME)) {
            resultValue = "0";
        } else if (type.equals(WizardConstants.LONG_NAME)) {
            resultValue = "0";
        }
        return resultValue;
    }
    
    /**
     * Returns all source groups which contains properties files related to this project.
     * @param project <CODE>Project</CODE> a project
     * @return <CODE>SourceGroup[]</CODE> source groups which contains properties files.
     */
    public static SourceGroup[] getPropSourceGroups(Project project) {
        return ProjectUtils.getSources(project).
                getSourceGroups(WizardConstants.PROPERTIES_EXT);
    }
    
    /**
     * Returns all source groups which contains java files related to this project.
     * @param project <CODE>Project</CODE> a project
     * @return <CODE>SourceGroup[]</CODE> source groups which contains java files.
     */
    public static SourceGroup[] getSourceGroups(Project project) {
        return ProjectUtils.getSources(project).getSourceGroups(
          org.netbeans.api.java.project.JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
    /**
     * Returns the corresponding package name of this folder.
     * @param project <CODE>Project</CODE> a project
     * @param packageFolder <CODE>FileObject</CODE> package folder 
     * @return <CODE>String</CODE> corresponding package name of this folder.
     */
    public static String getPackageName(Project project,FileObject packageFolder) {
        String packageName = null;
        SourceGroup[] projectSrcGroups = getSourceGroups(project);
        for (int i = 0 ; i < projectSrcGroups.length ; i++ ) {
            FileObject srcFolder = projectSrcGroups[i].getRootFolder();
            if (srcFolder.equals(packageFolder)) {
                return "";
            }
            DataFolder srcGroupDataObj = 
                    DataFolder.findFolder(srcFolder);
            for (Enumeration files = srcGroupDataObj.children(true) ; 
                    files.hasMoreElements();) {
                DataObject file = (DataObject) files.nextElement();
                if (file.getPrimaryFile().equals(packageFolder)) {
                    String srcFolderPath = 
                            FileUtil.toFile(srcFolder).getAbsolutePath();
                    String packageFolderPath = 
                            FileUtil.toFile(packageFolder).getAbsolutePath();
                    String packageFolderPathTemp = packageFolderPath.replace(
                            File.separatorChar, '/');
                    String srcFolderPathTemp = srcFolderPath.replace(
                            File.separatorChar, '/');
                    packageName = packageFolderPathTemp.replaceFirst(
                            srcFolderPathTemp,"");
                    if (packageName.indexOf("/")==0) {
                        packageName = packageName.replaceFirst("/","");
                    }
                    packageName = packageName.replace('/', '.');
                }
            }
        }
        return packageName;
    }
    
    /**
     * Returns the corresponding package name of this folder path.
     * @param project <CODE>Project</CODE> a project
     * @param packagePath <CODE>String</CODE> package folder path
     * @return <CODE>String</CODE> corresponding package name of this folder path.
     */
    public static String getPackageName(Project project,String packagePath) {
        String packageName = null;
        SourceGroup[] projectSrcGroups = getSourceGroups(project);
        for (int i = 0 ; i < projectSrcGroups.length ; i++ ) {
            FileObject srcFolder = projectSrcGroups[i].getRootFolder();
            String srcFolderPath = FileUtil.toFile(srcFolder).getAbsolutePath();
            if (srcFolderPath.equals(packagePath)) {
                return "";
            }
            if (packagePath.startsWith(srcFolderPath)) {
                packageName = 
                        packagePath.replace(File.separatorChar, '/').replaceFirst(
                        srcFolderPath.replace(File.separatorChar, '/'),"");
                if (packageName.indexOf('/')==0) {
                    packageName = packageName.replaceFirst("/","");
                }
                packageName = packageName.replace('/', '.');
                return packageName;
            }
        }
        return null;
    }
    
    /**
     * Returns the absolute path of the folder of the class file path.
     * ex: getFolderPath("/home/toto/class.java") returns "/home/toto/".
     * @param classFilePath <CODE>String</CODE> class file path
     * @return <CODE>String</CODE> class folder path.
     */
    public static String getFolderPath(String classFilePath) {
        String folderPath = classFilePath;
        int indexPoint = classFilePath.lastIndexOf(File.separatorChar);
        if (indexPoint != -1) {
            folderPath = classFilePath.substring(0,indexPoint);
        }
        return folderPath;
    }
    
    /**
     * Returns the class name of the complete class name
     * @param competeClassName <CODE>String</CODE> a class name
     * @return <CODE>String</CODE> a class name without package.
     */
    public static String getClassName(String competeClassName) {
        String className = competeClassName;
        int indexPoint = competeClassName.lastIndexOf('.');
        if (indexPoint != -1) {
            className = competeClassName.substring(indexPoint + 1);
        }
        return className;
    }
    
    /**
     * Returns the file name of the complete file name with path
     * @param competeFileName <CODE>String</CODE> a file path
     * @return <CODE>String</CODE> the file name with the extension.
     */
    public static String getFileName(String competeFileName) {
        String fileName = competeFileName;
        int indexPoint = competeFileName.lastIndexOf(File.separatorChar);
        if (indexPoint != -1) {
            fileName = competeFileName.substring(indexPoint + 1);
        }
        return fileName;
    }
    
    /**
     * Returns the package name of the complete class name
     * @param className <CODE>String</CODE> a class name
     * @return <CODE>String</CODE> package name of this class.
     */
    public static String getPackageName(String className) {
        String packageName = "";
        int indexPoint = className.lastIndexOf('.');
        if (indexPoint != -1) {
            packageName = className.substring(0,indexPoint);
        }
        return packageName;
    }
    
    /**
     * Extracts info from the netbeans project to put it into the wizard map.
     * @param wiz <CODE>WizardDescriptor</CODE> the current wizard
     * @throws java.lang.Exception if Project Location is null
     */
    public static void setProjectValues(WizardDescriptor wiz)
         throws Exception
    {
        String locationPath = getProjectLocation(wiz);

        if (locationPath == null) {
            throw new Exception("Project Location is null");
        }
        
        wiz.putProperty(WizardConstants.PROP_PROJECT_LOCATION, locationPath);

        FileObject targetFolder = Templates.getTargetFolder(wiz);
        String target = "";

        if (targetFolder != null) {
            String targetPath = File.separatorChar  + targetFolder.getPath();

            String srcPath = locationPath + File.separatorChar + WizardConstants.SRC_DIR; 

            int idx = targetPath.indexOf(srcPath);

            if (idx == 0) {
                if (targetPath.length() > srcPath.length()) {
                    String tmp = targetPath.substring(1 + srcPath.length());
                
                    if (tmp != null)
                        target = tmp.replace(File.separatorChar, '.');
                }
            }
        } 
        
        wiz.putProperty(WizardConstants.PROP_MBEAN_PACKAGE_NAME, target);
        wiz.putProperty(WizardConstants.PROP_PROJECT_NAME, getProjectName(wiz));
    }

    /**
     * Logs an info message (used for debuging).Display a message in Netbeans 
     * log file with severity = WARNING.
     * @param message <CODE>String</CODE> message to log
     */
    public static void logInfoMessage(String message)
    {
        ErrorManager.getDefault().log(ErrorManager.WARNING, 
                                      "JMX Module : " + message);
    }

    /**
     * Logs an error message (used for debuging).Display a message in Netbeans 
     * log file with severity = EXCEPTION.
     * @param message <CODE>String</CODE> message of this error
     * @param ex <CODE>Exception</CODE> an exception
     */
    public static void logErrorMessage(String message, Exception ex)
    {
        if (message != null) {
            if (ex != null) {
                System.out.println("JMX Module : " + message + 
                                   " exception : " + ex.toString());
            } else {
                System.out.println("JMX Module : " + message);
            }
        }
    
        if (ex != null) {
            ErrorManager.getDefault().notify(ex);
        } else {
            Exception e = new Exception("JMX Module : " + message);
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Reads one boolean option from the wizard map
     * @param wiz <CODE>WizardDescriptor</CODE>
     * @param prop <CODE>String</CODE> the key of the property
     * @return <CODE>boolean</CODE> the correponding boolean property.
     */
    public static boolean readBoolOption(WizardDescriptor wiz, String prop)
    {
        Boolean b = (Boolean)wiz.getProperty(prop);
        return (b == null) || b.booleanValue();
    }
    
    /**
     * Forces upper case for the first char of the given string.
     * @param name <CODE>String</CODE> the text to force the first letter uppercase
     * @return <CODE>String</CODE> name with its first letter upper case.
     */
    public static String capitalizeFirstLetter(String name)
    {
        if (name.equals("")) {
            return "";
        } else {
            String temp  = name.trim();
            String temp1 = temp.substring(0, 1);
            String temp2 = temp.substring(1);
            temp1 = temp1.toUpperCase();
            return temp1 + temp2;
        }
    }

    /**
     * Forces lower case for the first char of the given string.
     * @param name <CODE>String</CODE> the text to force the first letter lowercase
     * @return <CODE>String</CODE> name with its first letter lower case.
     */
    public static String forceFirstLetterLowerCase(String name)
    {
        if (name.equals("")) {
            return "";
        } else {
            String temp  = name.trim();
            String temp1 = temp.substring(0, 1);
            String temp2 = temp.substring(1);
            temp1 = temp1.toLowerCase();
            return temp1 + temp2;
        }
    }
    
    /**
     * Creates a file.
     * @param filename <CODE>String</CODE>a file name
     * @param path <CODE>String</CODE> a folder path
     * @param extension <CODE>String</CODE> an file extension
     * @param content <CODE>String</CODE> expected file content
     * @throws java.io.IOException thrown by create file method
     * @return <CODE>FileObject</CODE> the created file.
     */
    public static FileObject createFile(String filename, String path,
                                    String extension, String content)
         throws IOException                           
    {
        String outputName = new String(path + File.separatorChar +
                                       filename + "." + extension);
        // keep folder object
        FileObject folder = createDir(path);
        FileObject createdFile = FileUtil.createData(folder, 
                filename + "." + extension);
        FileLock lock = createdFile.lock();
        OutputStream os = createdFile.getOutputStream(lock);
        PrintStream ps = new PrintStream(os);
        ps.print(content);
        ps.flush();
        ps.close();
        lock.releaseLock();
        return createdFile;
        
    }
    
    /**
     * Creates a file.
     * @param filename <CODE>String</CODE>a file name
     * @param folder <CODE>FileObject</CODE> a folder object
     * @param extension <CODE>String</CODE> an file extension
     * @param content <CODE>String</CODE> expected file content
     * @throws java.io.IOException thrown by create file method
     * @return <CODE>FileObject</CODE> the created file.
     */
    public static FileObject createFile(String filename, FileObject folder,
                                    String extension, String content)
         throws IOException                           
    {
        // keep folder object
        FileObject createdFile = FileUtil.createData(folder, 
                filename + "." + extension);
        FileLock lock = createdFile.lock();
        OutputStream os = createdFile.getOutputStream(lock);
        PrintStream ps = new PrintStream(os);
        ps.print(content);
        ps.flush();
        ps.close();
        lock.releaseLock();
        return createdFile;
    }
    
    /**
     * Creates a directory if it doesn't already exist.
     * @param dirPath <CODE>String</CODE> folder path
     * @throws java.io.IOException thrown by create file methods
     * @return <CODE>FileObject</CODE> the created folder object
     */
    public static FileObject createDir(String dirPath) 
         throws IOException
    {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new IOException("Impossible to create " + dirPath);
        }

        FileObject dirFO = FileUtil.toFileObject(dir);
        return dirFO;
    }
    
    /**
     * Returns the project location of the given wiz.
     * @param wiz <CODE>WizardDescriptor</CODE>
     * @return <CODE>String</CODE> project location path
     */
    public static String getProjectLocation(WizardDescriptor wiz)
    {
        try {
            Project project     = Templates.getProject(wiz);
            FileObject proj_dir = project.getProjectDirectory();
            String path         = proj_dir.getPath();
            return new String(File.separatorChar + path);
            
        } catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Refresh the project logical tree view
     * @param wiz <CODE>WizardDescriptor</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     */
    public static void refreshProjectTree(WizardDescriptor wiz) 
            throws Exception {
        Project project = Templates.getProject(wiz);
        FileObject projectDir = project.getProjectDirectory();
        projectDir.getFileSystem().getRoot().refresh();
    }
   
    /**
     * Extracts the project name from the given wiz.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard 
     * @return <CODE>String</CODE> a project name
     */
    public static String getProjectName(WizardDescriptor wiz)
    {
        try {
            Project project         = Templates.getProject(wiz);
            Lookup  projectLookup   = project.getLookup();
            ProjectInformation info = (ProjectInformation)
                projectLookup.lookup(ProjectInformation.class);
            String name = info.getName();
            return name;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Checks whether the given mbean already exists or not
     * at the given location ONLY.
     * @param name <CODE>String</CODE> a MBean name
     * @param locationPath <CODE>String</CODE> a folder path
     * @return <CODE>boolean</CODE> true if corresponding MBean files exist
     */
    public static boolean mbeanExists(String name, String locationPath)
    {
        File locDir = new File(locationPath);
        
        if (!locDir.exists())
            return false;
        
        File mbeanFile = new File(locationPath + File.separatorChar + 
                                  name + WizardConstants.MBEAN_ITF_SUFFIX  + 
                                  "." + WizardConstants.JAVA_EXT);

        if (mbeanFile.exists())
            return true;
        
        File supportFile = new File(locationPath + File.separatorChar + 
                                  name + WizardConstants.MBEAN_SUPPORT_SUFFIX  + 
                                  "." + WizardConstants.JAVA_EXT);

        if (supportFile.exists())
            return true;
        
        File implfile = new File(locationPath + File.separatorChar + 
                                  name + "." + WizardConstants.JAVA_EXT);

        if (implfile.exists())
            return true; 
        
        return false;
    }
    
    /**
     * Checks whether the given agent already exists or not
     * at the given location ONLY.
     * @param name <CODE>String</CODE> a file name
     * @param locationPath <CODE>String</CODE> a folder path
     * @return <CODE>boolean</CODE> true if the corresponding file exists
     */
    public static boolean fileExists(String name, String locationPath)
    {
        File locDir = new File(locationPath);
        
        if (!locDir.exists())
            return false;
        
        File file = new File(locationPath + File.separatorChar + name);

        if (file.exists())
            return true; 
        else 
            return false;
    }

    /**
     * Delete the file if already exists, else do nothing.
     * @param name <CODE>String</CODE> a file name
     * @param locationPath <CODE>String</CODE> a folder path
     */
    public static void removeFile(String name, String locationPath)
    {
        File file = new File(locationPath + File.separatorChar + name);

        if (file.exists())
            file.delete();
    }

    /**
     * Checks whether the given agent already exists or not
     * at the given location ONLY.
     * @param fileName <CODE>String</CODE> a file path
     * @return <CODE>boolean</CODE> true if the corresponding file exists
     */
    public static boolean fileExists(String fileName)
    {
        File file = new File(fileName);

        return file.exists();
    }
    
    /**
     * Display a dialog with expected content.
     * @param content <CODE>String</CODE> expected content of the displayed dialog
     * @param messageTitle <CODE>String</CODE> title of the dialog
     * @param option <CODE>int</CODE> option of the dialog
     * @param typeMessage <CODE>int</CODE> type of message
     */
    public static void displayMessage(String content, String messageTitle, int option, int typeMessage) {
        
        int response = javax.swing.JOptionPane.showConfirmDialog(
                                            null,
                                            content,
                                            messageTitle,
                                            option,
                                            typeMessage);
    }

    /**
     * Display a dialog with expected content.
     * @param content <CODE>String</CODE> expected content
     */
    public static void displayMessage(String content) {
        
        int response = javax.swing.JOptionPane.showConfirmDialog(
                                            null,
                                            content,
                                            "Erreur",
                                            javax.swing.JOptionPane.OK_OPTION,
                                            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Returns an object describing the main class of the specified
     * file object containing java source.
     * @return <code>JavaClass</code> with the data object's
     *          main class; or <code>null</code> if the class was not
     *          found (e.g. because of a broken source file)
     * @param name <CODE>String</CODE> a class name
     * @param res <code>Resource</code> examine
     */
    public static JavaClass getJavaClass(Resource res,
                                              String name) {
        List l = res.getClassifiers();
        Iterator it = l.iterator();
        String resName = fileToClassName(res.getName());
        if (resName != null) {
            while (it.hasNext()) {
                Element e = (Element)it.next();
                if (((JavaClass)e).getName().endsWith(name)) 
                     return (JavaClass)e;
            }
        }
        return null;
    }
    
    /**
     * Returns an object describing the main method of the specified class
     *
     * @param clazz <code>JavaClass</code> examine
     * @return  <code>Method</code> with the data object's
     *          method main; or <code>null</code> if the main method was not
     *          found 
     */
    public static Method getMainMethod(JavaClass clazz) {  
        List l = clazz.getChildren();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof Method) {
                if (((Method) e).getName().equals(
                           WizardConstants.MAIN_METHOD_NAME)) 
                        return (Method) e;
            }
        }
        return null;
    }
    
    /**
     * Returns an object describing the method with the specified name 
     * of the specified class
     *
     * @param clazz <code>JavaClass</code> examine
     * @param methodName <code>String</code> method name to find
     * @return  <code>Method</code> with the data object's
     *          method main; or <code>null</code> if the main method was not
     *          found 
     */
    public static Method getMethod(JavaClass clazz, String methodName) {  
        List l = clazz.getChildren();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof Method) {
                if (((Method) e).getName().equals(
                           methodName)) 
                        return (Method) e;
            }
        }
        return null;
    }
    
    /**
     * Returns an array containing all declared methods of the specified class.
     *
     * @param clazz <code>JavaClass</code> examine
     * @return  <code>Method[]</code> class methods or <code>null</code> if 
     * there is no method 
     */
    public static Method[] getClassMethods(JavaClass clazz) {  
        List l = clazz.getChildren();
        Iterator it = l.iterator();
        Set results = new HashSet();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof Method) {
                results.add((Method) e);
            }
        }
        return (Method[]) results.toArray(new Method[results.size()]);
    }
    
    /**
     * Returns an array containing all methods of the specified class or of their
     * super classes.
     *
     * @param clazz <code>JavaClass</code> examine
     * @return  <code>Method[]</code> class methods or <code>null</code> if 
     * there is no method 
     */
    public static Method[] getMethods(JavaClass clazz, String methodName) {  
        List l = clazz.getChildren();
        Iterator it = l.iterator();
        Set results = new HashSet();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof Method) {
                if (((Method) e).getName().equals(methodName))
                    results.add((Method) e);
            }
        }
        JavaClass superClazz = clazz.getSuperClass();
        if (superClazz != null) {
            JavaModelPackage pkg = (JavaModelPackage)clazz.refImmediatePackage();  
            JavaClass superCls = (JavaClass)
                pkg.getJavaClass().resolve(superClazz.getName());
            Method[] methods = getMethods(superCls, methodName);
            for (int i = 0; i < methods.length; i++)
                results.add(methods[i]);
        }
        return (Method[]) results.toArray(new Method[results.size()]);
    }
    
    /**
     * Returns an array containing all  methods of the specified interface and 
     * of their super interfaces.
     *
     * @param clazz <code>JavaClass</code> examine 
     * @return  <code>Method[]</code> class methods or <code>null</code> if 
     * there is no method 
     */
    public static Method[] getInterfaceMethods(JavaClass clazz) {  
        List interfaces = clazz.getInterfaces();
        Set results = new HashSet();
        for (Iterator<JavaClass> it = interfaces.iterator(); it.hasNext();) {
            Method[] methods = getInterfaceMethods(it.next());
            for (int i = 0; i < methods.length; i++)
                results.add(methods[i]);
        }
        return (Method[]) results.toArray(new Method[results.size()]);
    }
    
    /**
     * Returns an array containing all public constructors of the specified class.
     *
     * @param clazz <code>JavaClass</code> examine 
     * @return  <code>Method[]</code> class methods or <code>null</code> if 
     * there is no method 
     */
    public static Constructor[] getConstructors(JavaClass clazz) {  
        List childs = clazz.getChildren();
        Set results = new HashSet();
        for (Iterator<JavaClass> it = childs.iterator(); it.hasNext();) {
            Element e = (Element)it.next();
            if (e instanceof Constructor) {
                final int mods = ((Constructor) e).getModifiers();
	        if (Modifier.isPublic(mods))
                    results.add((Constructor) e);
            }
        }
        return (Constructor[]) results.toArray(new Constructor[results.size()]);
    }
    
    /**
     * Converts filename to the fully qualified name of the main class
     * residing in the file.<br />
     * For example: <tt>test/myapp/App.java</tt> --&gt; <tt>test.myapp.App</tt>
     * @return corresponding package name. Null if the input is not
     *          well formed.
     * @param fileName <CODE>String</CODE> a file name
     */
    static String fileToClassName(String fileName) {
        if (fileName.endsWith(".java")) {                               //NOI18N
            return (fileName.substring(0, fileName.length()-5)).replace('/','.');
        } else
            return null;
    }
    
    /**
     * Prints out the content of parts of the operation exception table model(used to debug).
     * @param model <code>AbstractJMXTableModel</code> the model to parse
     * @param toParse <code>int</code> the parameter in the model to parse
     */
    public static void printOperationExceptionModel(AbstractJMXTableModel model, int toParse) {
        System.out.println("************* PARSE START *****************");
        
        for (int i = 0; i < model.size(); i++) {
            ArrayList<MBeanOperationException> excepts = (ArrayList<MBeanOperationException>)
                                                                model.getValueAt(i, toParse);
            for(int j = 0; j < excepts.size(); j++)
                System.out.println("Value of row " +i+ ": " + excepts.get(j).getExceptionClass());
        }
    }
    
    /**
     * Creates a copy of the given array, except that <code>null</code> objects
     * are omitted.
     * The length of the returned array is (<var>l</var> - <var>n</var>), where
     * <var>l</var> is length of the passed array and <var>n</var> is number
     * of <code>null</code> elements of the array. Order of
     * non-<code>null</code> elements is kept in the returned array.
     * The returned array is always a new array, even if the passed
     * array does not contain any <code>null</code> elements.
     * @param objs array to copy
     * @return array containing the same objects as the passed array, in the
     *          same order, just with <code>null</code> elements missing
     */
    public static Object[] skipNulls(final Object[] objs) {
        List resultList = new ArrayList(objs.length);
        
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                resultList.add(objs[i]);
            }
        }
        
        return resultList.isEmpty() ? new Object[0] : resultList.toArray();
    }
    
    /**
     * Instantiates a new JTextField
     * @return JTextField
     */
    
    public static JTextField instanciateJTextField() {
        
        JTextField text = new JTextField();
        text.setDragEnabled(true);
        text.setEditable(true);
        
        return text;
    }
    
    /**
     * Instanciates a JComboBox with the variable types we provide
     * @return JComboBox
     */
    public static JComboBox instanciateTypeJComboBox() {
        
        JComboBox typeCombo = new JComboBox();
        
        // the attribute's type combo box     
        typeCombo.addItem(WizardConstants.BOOLEAN_NAME);
        typeCombo.addItem(WizardConstants.BYTE_NAME);
        typeCombo.addItem(WizardConstants.CHAR_NAME);
        typeCombo.addItem(WizardConstants.DATE_OBJ_NAME);
        typeCombo.addItem(WizardConstants.INT_NAME);
        typeCombo.addItem(WizardConstants.LONG_NAME);
        typeCombo.addItem(WizardConstants.OBJECTNAME_NAME);        
        typeCombo.addItem(WizardConstants.STRING_OBJ_NAME);
        typeCombo.setSelectedItem(WizardConstants.STRING_OBJ_NAME);
        typeCombo.setEditable(true);
        
        return typeCombo;
    }
    
    /**
     * Instanciates a JComboBox with the return types we provide
     * @return JComboBox
     */
    public static JComboBox instanciateRetTypeJComboBox() {
        
        JComboBox retTypeJCombo = instanciateTypeJComboBox();
        retTypeJCombo.addItem(WizardConstants.VOID_RET_TYPE);
        retTypeJCombo.setSelectedItem(WizardConstants.VOID_RET_TYPE);
        
        return retTypeJCombo;
    }
    
    /**
     * Instanciates a JComboBox with the access types we provide (RO or R/W)
     * @return JComboBox
     */
    public static JComboBox instanciateAccessJComboBox() {
        
        JComboBox accessCombo = new JComboBox();
        
        // the attribute's acces mode combo box
        accessCombo.addItem(WizardConstants.ATTR_ACCESS_READ_WRITE);
        accessCombo.addItem(WizardConstants.ATTR_ACCESS_READ_ONLY);
        accessCombo.setSelectedItem(WizardConstants.ATTR_ACCESS_READ_WRITE);
        
        return accessCombo;
    }
    
}
