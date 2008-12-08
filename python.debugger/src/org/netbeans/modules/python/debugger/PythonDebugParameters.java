
package org.netbeans.modules.python.debugger;

import java.awt.*;
import java.io.File;
import org.netbeans.modules.python.api.PythonOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** 
 * @author jean-yves
 *
 * this class is used to store python debuggers parameters
 * ( refactored to get everything from PythonOptions Core )
 */
public class PythonDebugParameters
{
  
  private final static String _NBPYTHON_DEBUG_HOME_         = "nbPython/debug";
  
  /** Front End Multi IDE interface (MUST BE SET AT IDE PLUGIN INIT TIME)  by Ide Pluggin*/
  // public static NetBeansFrontend ideFront = null ;
  private final static String _EMPTY_  = ""  ;
  private final static String _DEFAULT_HOST_  = "localhost"  ;
  private final static String _JPYDBG_  = "jpydbg.py"     ;


  /* PyLint Static Stuff starts here */
  public static String get_pyLintLocation()
  { return PythonOptions.getInstance().getPylintLocation()  ; }
  public static String get_pyLintArgs()
  { return PythonOptions.getInstance().getPylintOptions()  ; }
  public static boolean is_usePyLint()
  { return PythonOptions.getInstance().getUsePylint()  ; }
  public static boolean is_pyLintFatal()
  { return PythonOptions.getInstance().getPylintFatal() ; }
  public static boolean is_pyLintError()
  { return PythonOptions.getInstance().getPylintError() ; }
  public static boolean is_pyLintWarning()
  { return PythonOptions.getInstance().getPylintWarning() ; }
  public static boolean is_pyLintConvention()
  { return PythonOptions.getInstance().getPylintConvention() ; }
  public static boolean is_pyLintRefactor()
  { return PythonOptions.getInstance().getPylintRefactor() ; }
  /* PyLint Static Stuff ends here */

  private static boolean _hasInited = false ;
  private static String  _jpydbgScript = _JPYDBG_ ;
  private static String  _tempDir = null ;

  public static boolean hasInited()
  {
    if ( _hasInited )
      return true ;
    _hasInited = true ;
    return false ; 
  }
  
  public static String get_codePage()
  { return _EMPTY_  ; }
  

  public static int get_listeningPort()
  { return PythonOptions.getInstance().getPythonDebuggingPort() ;  }

  
  public static String get_dbgHost()
  { return _DEFAULT_HOST_ ; }

  public static boolean get_debugTrace()
  { return true ; }

  public static String get_jpydbgScript()
  { return _jpydbgScript ; }
  public static void set_jpydbgScript( String script )
  { _jpydbgScript = script ; }

  public static void set_tempDir( String tempDir )
  { _tempDir = tempDir ; }
  public static String get_tempDir()
  { return _tempDir  ; }
 
  public static String get_jpydbgScriptArgs()
  { return _EMPTY_ ; }
 
  public static int get_connectingPort()
  { return -1 ; }
  
  public static Color get_shellBackground()
  { return PythonOptions.getInstance().getDbgShellBackground() ; }
  
  public static Color get_shellError()
  { return PythonOptions.getInstance().getDbgShellErrorColor() ; }
  
  public static Font get_shellFont()
  { 
    return PythonOptions.getInstance().getDbgShellFont() ;
  }
  
  public static Color get_shellHeader()
  { return PythonOptions.getInstance().getDbgShellHeaderColor() ; }
  
  public static Color get_shellMessage()
  { return PythonOptions.getInstance().getDbgShellInfoColor(); }
  
  public static Color get_shellWarning()
  { return PythonOptions.getInstance().getDbgShellWarningColor() ; }

  public static String get_workDir()
  { return null ; }



  public static File checkSettingsDirectory()
  {
  FileObject netbeansRoot = Repository.getDefault().getDefaultFileSystem().getRoot() ;
  File root = FileUtil.toFile(netbeansRoot) ;
    File nbPythonDebugHome = new File ( root , _NBPYTHON_DEBUG_HOME_ ) ;
    if ( nbPythonDebugHome.isDirectory())
        return nbPythonDebugHome;
    nbPythonDebugHome.mkdirs();
    return nbPythonDebugHome ;
  }

  /**
   * where is the setting directory
   *
   * @return the path location
   */
  public static String getSettingsDirectory()
  {
  File f =  checkSettingsDirectory() ;

    return f.getAbsolutePath();
  }



}
