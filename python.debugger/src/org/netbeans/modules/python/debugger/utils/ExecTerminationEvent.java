
package org.netbeans.modules.python.debugger.utils;

/**


  event associated with process termination

  @author Jean-Yves MENGANT 

*/
public class ExecTerminationEvent {

  private int _code ;
  private String _errMessage ;

  public ExecTerminationEvent( int code ,
                               String errMessage
                             )
  {
    _code = code ;
    _errMessage = errMessage ;
  }

  public int get_code()
  { return _code ; }

  public String get_errMessage()
  { return _errMessage ; }
}
