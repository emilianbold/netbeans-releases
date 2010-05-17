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

import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.RepositoryAdapter;
import org.openide.filesystems.RepositoryEvent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.UMLParsingIntegrator;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;

/**
 * NetBeans (FFJ) will allow two or more file system that will overlap each other.
 * When this occurs a user can update one file and FFJ will not send events
 * about changed elements.  We can not stop this from occuring but we have
 * decided to warn the user of the situation.
 * @author  tspiva
 * @version
 */
public class FileSystemListener extends RepositoryAdapter
{
  private static boolean mounting = false;
  private Repository repository = null;

  /**
   *  The default FileSystemListener instance returned to callers of
   * getInstance(). Note: This does not imply that this class is a singleton!
   */
  private static FileSystemListener defaultInstance = null;

  public static synchronized FileSystemListener getInstance() {
      if (defaultInstance == null)
          defaultInstance = new FileSystemListener();
      return defaultInstance;
  }

  public static void setMounting(boolean mounting) {
      FileSystemListener.mounting = mounting;
  }

  public static boolean isMounting() {
      return mounting;
  }

  public void addTo(Repository repos) {
      if (repository != null) repository.removeRepositoryListener(this);
      if ((repository = repos) != null)
          repository.addRepositoryListener(this);
  }

  public void fileSystemRemoved(RepositoryEvent e) {
      FileSystem fs = e.getFileSystem();
      if (fs instanceof LocalFileSystem && !fs.isReadOnly()) {
          Log.out("FSL: --------- " + fs.getDisplayName());
      }
  }

  /**
   * Called when new file system is added to the pool.  When a new file system
   * is added check if it is overlapping any of the previous file systems.  If
   * the file system does overlap an existing file system notify the user.
   */
  public void fileSystemAdded(RepositoryEvent e)
  {
    if(e.isAdded() == true)
    {
      final FileSystem fs      = e.getFileSystem();
      setMounting(false);

      if (fs instanceof LocalFileSystem && !fs.isReadOnly()
              && fs.isValid())
          Log.out("FSL: +++++++++ " + fs.getDisplayName());

      /*
      Frame      mainWnd = WindowManager.getDefault().getMainWindow();

      if(mainWnd.isDisplayable() == true)
      {
        if((fs.isHidden() != true) && (fs.isReadOnly() != true) && (fs.isValid() == true))
        {
          if(isOverlappingFilesystem(fs) == true)
          {
            //String newLine = System.getProperty("line.separator");
            Runnable r = new Runnable()
            {
              public void run()
              {
                String newLine = "\n";
                String msg = "The file system " + fs.getDisplayName() + newLine;
                msg += "shares a common root folder with an existing file system." + newLine;
                msg += "Overlapping a file system will cause synchronization" + newLine;
                msg += "problems with Describe.  You need to specify" + newLine;
                msg += "a single file system that will cover all overlapping file systems.";
                JOptionPane.showMessageDialog(null, msg, "Overlapping FileSystem Added", JOptionPane.ERROR_MESSAGE);
              }
            };
            GDProSupport.getGDProSupport().getEventQueue().queueRunnable(r);
          }
        }
      }
      */
    }
  }
//
//  protected void reverseEngineer(FileSystem fs) {
//      if (ProjectController.isConnected()) {
//          IProject proj = GDProSupport.getCurrentProject();
//          if (proj != null && hasJavaFiles(fs)) {
//              IStrings fileNames = new Strings();
//              getJavaFiles(fs, fileNames);
//              int fileCount = fileNames.getCount();
//              if (confirmedRE(proj.getName(), fs.getDisplayName(), fileCount)) {
//                  IUMLParsingIntegrator integrator = new UMLParsingIntegrator();
//                  integrator.setFiles(fileNames);
//                  Log.out("Reverse engineering");
//                  
//                  try
//                  {
//                     integrator.reverseEngineer( proj,
//                                             false, // this brings up the file chooser
//                                              false, // this should be false for now.
//                                              true, // this will display the progress dialog
//                                              true );// this will cause all the classes to
//                                                    //be created in their own file. Not currently enabled
//   
//                     GDSystemTreeComponent tree = (GDSystemTreeComponent)
//                             NBUtils.getComponent(GDSystemTreeComponent.class);
//                     if (tree != null) tree.refresh();
//                  }
//                  catch(Exception e)
//                  {
//                  }
//              }
//          }
//      }
//  }

//  protected boolean confirmedRE(String projectName, String repos,
//                                int fileCount) {
//      String msg = DescribeModule.getString("Dialog.ConfirmREFS.Text",
//                                  new Object[] {
//                                      projectName,
//                                      repos,
//                                      new Integer(fileCount)
//                                  } );
//      String title = DescribeModule.getString("Dialog.ConfirmREFS.Title");
//      int ans = JOptionPane.showConfirmDialog(
//                          WindowManager.getDefault()
//                                    .getMainWindow(),
//                          msg,
//                          title, JOptionPane.YES_NO_OPTION);
//      return ans == JOptionPane.YES_OPTION;
//  }

//  protected boolean hasJavaFiles(FileSystem fs) {
//      return getJavaFiles(fs, null);
//  }

  /**
   *  Given a file system, populates a collection of file names (absolute paths)
   * of all the Java source files in the file system.
   *
   * @param fs        The <code>FileSystem</code>.
   * @param fileNames The <code>IStrings</code> collection to be populated.
   * @return <code>true</code> if fs is non-null, and the file system contains
   *         at least one Java source file. If fileNames is null, the search
   *         will exit as soon as the first source file is located.
   */
//  protected boolean getJavaFiles(FileSystem fs, IStrings fileNames) {
//      // EARLY EXIT
//      if (fs == null) return false;
//
//      FileObject rootFO = fs.getRoot();
//      Enumeration allDO = rootFO.getData(true);
//
//      boolean hasJava = false;
//      int count = 0;
//      int limit = isMounting()? 2 : 1;
//      while (allDO.hasMoreElements()) {
//          try {
//              FileObject fo = (FileObject) allDO.nextElement();
//              if(fo.getExt().equalsIgnoreCase("java")) {
//                  hasJava = true;
//                  ++count;
//                  if (fileNames != null) {
//                      String fileName = FileUtil.toFile(fo).toString();
//                      Log.out("File name is " + fileName);
//                      fileNames.add(fileName);
//                  } else if (count >= limit)
//                      break;
//              }
//          } catch (Exception e) {
//              Log.stackTrace(e);
//          }
//      }
//
//      if (count < limit) hasJava = false;
//      return hasJava;
//  }

  /**
   * Check for an OVERLAPPING filesystem condition.  A file system is overlapping
   * if two file system in the repository cover the same folders in the Physical
   * file system.
   * @param The FileSystem object to check.
   */
//  protected boolean isOverlappingFilesystem(FileSystem fs)
//  {
//	Log.entry("Entering function FileSystemListener::isOverlappingFilesystem");
//
//    boolean retVal = true;
//
//    Repository repos = Repository.getDefault();
//    FileSystem[] allFileSystems = repos.toArray();
//
//    // First make sure that there none of the existing file systems overlap the
//    // the new file system.
//    //
//    // USE CASE:
//    // New FileSystem: C:\Development\Java\HelloWorld
//    // Old FileSystem: C:\Developement\Java
//    String fsPath = fs.getDisplayName();
//    if(isExistingOverlappingNew(fsPath, allFileSystems) == false)
//    {
//      // Now Make Sure that the new file system does not over lap any existing
//      // file systems
//      //
//      // USE CASE:
//      // New FileSystem: C:\Development\Java
//      // Old FileSystem: C:\Developement\Java\HelloWorld
//      if(isNewOverlappingOld(fsPath, allFileSystems) == false)
//      {
//        retVal = false;
//      }
//    }
//
//    return retVal;
//  }

  /**
   * Checks if any of the existing FileSystems are overlapping the new filesystem.
   * @param newPath The filesystem path to compare to.
   * @return true if an existing file system overlaps, false otherwise.
   */
//  protected boolean isExistingOverlappingNew(String newPath, FileSystem[] allFileSystems)
//  {
//	Log.entry("Entering function FileSystemListener::isExistingOverlappingNew");
//
//    boolean retVal = false;
//
//    int count = 0;
//    for(int i = 0; (i < allFileSystems.length) && (retVal == false); i++)
//    {
//      String fsName = allFileSystems[i].getDisplayName();
//      if(newPath.startsWith(fsName) == true)
//        count++;
//    }
//
//    // Because the file system has already been added to the repository there will
//    // be at least one file system that matches.  As long as only one matches we are
//    // ok.
//    if(count > 1)
//      retVal = true;
//
//    return retVal;
//  }

  /**
   * Checks if the new FileSystems is overlapping any of the old filesystems.
   * @param newPath The filesystem path to compare to.
   * @return true if the new file system overlaps, false otherwise.
   */
//  protected boolean isNewOverlappingOld(String newPath, FileSystem[] allFileSystems)
//  {
//	Log.entry("Entering function FileSystemListener::isNewOverlappingOld");
//
//    boolean retVal = false;
//
//    int count = 0;
//    for(int i = 0; i < allFileSystems.length; i++)
//    {
//      String fsName = allFileSystems[i].getDisplayName();
//      if(fsName.startsWith(newPath) == true)
//        count++;
//    }
//
//    // Because the file system has already been added to the repository there will
//    // be at least one file system that matches.  As long as only one matches we are
//    // ok.
//    if(count > 1)
//      retVal = true;
//
//    return retVal;
//  }
}
