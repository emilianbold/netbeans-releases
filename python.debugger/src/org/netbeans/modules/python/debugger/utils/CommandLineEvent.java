/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger.utils;

/**
 *
 * @author jymen
 */
public class CommandLineEvent
{
  private String _command ;

  public CommandLineEvent ( String command )
  {
    _command = command ;
  }

  public String getCommand()
  {
    return _command ; 
  }
}
