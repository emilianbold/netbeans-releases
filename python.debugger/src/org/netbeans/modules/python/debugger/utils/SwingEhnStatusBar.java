
/**
* Copyright (C) 1998,1999,200,2001,2002,2003 Jean-Yves Mengant
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package org.netbeans.modules.python.debugger.utils;

import java.awt.*    ;
import java.awt.event.*;
import javax.swing.* ;

/**

 Define a Swing status bar behavior

 @Author Jean-Yves MENGANT


*/ 
public class SwingEhnStatusBar extends JPanel {

 private boolean _errorOn   = false ;
 private boolean _warningOn = false ;

  JLabel _text = new JLabel() ;

  public SwingEhnStatusBar()
  {
    super() ;
    setLayout( new CardLayout() ) ;
    setBorder( BorderFactory.createRaisedBevelBorder() ) ;
    _text.setHorizontalAlignment(SwingConstants.LEFT) ;
    add ( "panel" , _text ) ;
  }

  /** Clear any on going error */
  public  void reset()
  {
    _text.setBackground(Color.gray) ;
    _text.setForeground(Color.gray) ;
    _errorOn   = false      ;
    _warningOn = false      ;
    _text.setText("") ;
  }

  /** reset informing user */
  public void reset ( String msg )
  {
    reset() ;
    setMessage(msg ) ;
  }

  /** Display sample message */
  public  void setMessage( String msg   )
  {
    if ( ( !_errorOn ) && ( !_warningOn ) )
    {
      _text.setForeground(Swing.BLUE) ;
      _text.setBackground(Swing.WHITE) ;
      _text.setText("INFO : " + msg) ;
    }
  }

  /** use this for displaying errors */
  public void setError( String error )
  {
    _text.setForeground(Swing.RED) ;
    _text.setBackground(Swing.WHITE)   ;
    _text.setText("ERROR :: " + error) ;
    _errorOn = true ;
  }

  /** use this for displaying warnings */
  public void setWarning( String wrn )
  {
    if ( !_errorOn )
    {
      _text.setBackground(Swing.WHITE) ;
      _text.setForeground(Swing.MAGENTA) ;
      _text.setText("WARNING :: " + wrn ) ;
      _warningOn = true ;
      System.out.println("size warning :" + getPreferredSize() ) ;
    }
  }

  public boolean is_errorOn()
  { return _errorOn  ;  }

  public static void  main ( String argv[] )
  {
    // Exit the debug window frame
    class WL extends WindowAdapter{
      public void windowClosing( WindowEvent e )
      { System.exit(0)  ; }
    }

    class _SET_ implements ActionListener {
      SwingEhnStatusBar _ehn ;
      boolean _on ;
      public _SET_( SwingEhnStatusBar ehn )
      { _ehn = ehn ; }

      public void actionPerformed( ActionEvent e )
      {
        if ( _on )
        {
          _on = false ;
          _ehn.setMessage("hello" ) ;
        }
        else
        {
          _on = true ;
          _ehn.reset() ;
        }
      }
    }

    JFrame f = new JFrame("Testing Swing Status bar")  ;
    f.setForeground(Color.black) ;
    f.setBackground(Color.lightGray) ;
    f.getContentPane().setLayout(new BorderLayout() ) ;
    f.addWindowListener( new WL() ) ;

    SwingEhnStatusBar status = new SwingEhnStatusBar() ;


    // status.setText("Hello") ;
    JButton b = new JButton ("Action") ;
    b.addActionListener( new _SET_(status) ) ;

    f.getContentPane().add("North", b) ;
    f.getContentPane().add("South", status) ;
    status.setWarning("Hello New wranError Displayed") ;

    f.pack() ;
    f.setVisible(true) ;
  }
}
