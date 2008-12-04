/*
* PythonBreakpoint.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/
package org.netbeans.modules.python.debugger.breakpoints;

import org.netbeans.api.debugger.Breakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;

/**
 *
 * @author jean-yves
 */
public class PythonBreakpoint 
extends Breakpoint
{
  
  private boolean _enabled = true;
  private Line    _line;
    

  /** Creates a new instance of PythonBreakpoint */
  public PythonBreakpoint(Line line ) 
  {
    _line = line ;
  }

  /**
  * Test whether the breakpoint is enabled.
  *
  * @return <code>true</code> if so
  */
  public boolean isEnabled () 
  {
    return _enabled;
  }
  
  /**
  * Disables the breakpoint.
  */
  public void disable () 
  {
    if (! _enabled) return;
    _enabled = false;
    firePropertyChange (PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
  }
    
  public Line getLine () 
  { return _line; }
  
  
  /**
  * Enables the breakpoint.
  */
  public void enable () 
  {
    if (_enabled) return;
    _enabled = true;
    firePropertyChange (PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
  }

    public FileObject getFileObject() {
        return (FileObject)getLine().getLookup().lookup(FileObject.class);
    }
    
    public String getFilePath() {
        return FileUtil.toFile(getFileObject()).getAbsolutePath();
    }
    
    public int getLineNumber() {
        // Note that Line.getLineNumber() starts at zero
        return getLine().getLineNumber() + 1;
    }
    
  
}
