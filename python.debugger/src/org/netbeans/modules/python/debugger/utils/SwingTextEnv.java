package org.netbeans.modules.python.debugger.utils;

import java.awt.*    ;
 
/**

  Define all parameters needed To establish Text Label
  display context :

  - FONT
  - BCKGROUND color
  - FOREGROUND color

  @author Jean-Yves MENGANT

*/ 

public class SwingTextEnv {

  private Font  _font ;
  private Color _backGround ;
  private Color _foreGround ;

  public SwingTextEnv( Font  font ,
                       Color backGround ,
                       Color foreGround
                     )
  {
    _font        = font ;
    _backGround  = backGround ;
    _foreGround  = foreGround ;
  }


  public Font get_font()
  { return _font ; }

  public Color get_backGround()
  { return _backGround ; }

  public Color get_foreGround()
  { return _foreGround ; }
}
