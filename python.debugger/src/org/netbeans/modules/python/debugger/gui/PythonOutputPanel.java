
/*
 * PythonOutputPanel.java
 *
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
 */
package org.netbeans.modules.python.debugger.gui;

import javax.swing.JTabbedPane ;
import java.awt.BorderLayout ;
import javax.swing.ImageIcon ;
import javax.swing.JPanel;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.debugger.utils.CommandLineListener;
import org.netbeans.modules.python.debugger.utils.SwingMessageArea;


/**
 * A basic StdOut panel for Debugging or running instances
 * @author jean-yves
 */
public class PythonOutputPanel
extends JPanel
{
  /** not null => tabbed pane container */
  private JTabbedPane _hostPane = null ;
  /** if not -1 => tabbedPane index */
  private int _pos = -1 ; 
  private SwingMessageArea _setOutTrace ;

  private void init()
  {
  PythonOptions pyOptions = PythonOptions.getInstance() ;

    _setOutTrace = new SwingMessageArea( pyOptions.getDbgShellFont() ,
		                         pyOptions.getDbgShellBackground() ,
		                         pyOptions.getDbgShellHeaderColor() ,
		                         pyOptions.getDbgShellErrorColor(),
		                         pyOptions.getDbgShellWarningColor() ,
		                         pyOptions.getDbgShellInfoColor()
                                        ) ;
    _setOutTrace.set_refresh(true) ;                                     

    setLayout( new BorderLayout() ) ;
    add ( BorderLayout.CENTER , _setOutTrace ) ;
   
  }
   
  /** Creates a new instance of PythonOutputPanel */
  public PythonOutputPanel( JTabbedPane hostPane , 
                            ImageIcon icon ,
                            CommandLineListener cmdListener 
                            ) 
  {
    init() ;
    _setOutTrace.addCommandLineListener(cmdListener) ;
     _hostPane = hostPane ;
    _hostPane.addTab ( "stdout content" , icon  , this ) ;
    _pos = _hostPane.indexOfComponent(this) ; 
  }
  
  /** Creates a new instance of PythonOutputPanel */
  public PythonOutputPanel() 
  { init() ; }

  @Override
  public void setEnabled( boolean enabled )
  { 
    if ( _hostPane != null )
      _hostPane.setEnabledAt( _pos , enabled ) ;
  }

  public void pauseShell()
  {
    if ( _setOutTrace != null )
      _setOutTrace.hasEnabled(false)  ;
  }

  
  public void writeMessage(String msg)
  {
    _setOutTrace.message(msg) ;
  }


  public void messageAppend(String msg)
  {
    _setOutTrace.messageAppend(msg) ;
  }

  public void headerAppend(String msg)
  {
    _setOutTrace.headerAppend(msg) ;
  }


  public void writeError(String msg)
  {
    _setOutTrace.error(msg) ; 
  }

  public void writeHeader(String msg)
  {
    _setOutTrace.headerFooter(msg) ; 
  }
  public void writeLog(String msg)
  {
    _setOutTrace.headerFooter("[LOG]"+msg) ; 
  }

  public void writeWarning(String msg)
  {
    _setOutTrace.warning(msg) ; 
  }


  public void checkColoringChanges( PythonOptions pyOptions )
  { 
    _setOutTrace.populateGUIInfos( pyOptions.getDbgShellFont() ,
		                         pyOptions.getDbgShellBackground() ,
		                         pyOptions.getDbgShellHeaderColor() ,
		                         pyOptions.getDbgShellErrorColor(),
		                         pyOptions.getDbgShellWarningColor() ,
		                         pyOptions.getDbgShellInfoColor()
                                  ) ; 
  }

  public void debuggingShellColorChanged(PythonOptions options)
  {
    checkColoringChanges(options) ;
  }



}

