package org.netbeans.modules.python.debugger.utils;

import javax.swing.* ;
import java.awt.*  ; 
import java.awt.event.*;

/**
 * 
 * A way to change the mouse cursor's shape over a given Swing
 * component
 * 
 * two ways are provided by this class :
 * - Standard way using the startWaiting/stopWaiting cursor
 * - Animated dynamic cursor through the startAnimation/ run /stopAnimation
 * methods
 * 
 * @author jean-yves
 *
 */

public class AnimatedCursor
implements Runnable
{
  private Component _candidate ; 
  private Cursor[]   _cursors   ; 
  private boolean    _animated = false ; 

  public AnimatedCursor( Component c)
  {
    _candidate = c ; 
    _cursors = new Cursor[8] ; 
    _cursors[0] = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR) ; 
    _cursors[1] = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR) ; 
    _cursors[2] = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) ; 
    _cursors[3] = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR) ; 
    _cursors[4] = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR) ; 
    _cursors[5] = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR) ; 
    _cursors[6] = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR) ; 
    _cursors[7] = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR) ; 
    
  }
  
  public void startWaitingCursor()
  { 
    _animated = true ; 
    _candidate.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ; 
  } 
 
  public void stopWaitingCursor()
  { 
    _candidate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ; 
    _animated = false ; 
  } 
 
  
  public synchronized void stopAnimation()
  { _animated = false ; 
  }
  
  public synchronized void startAnimation()
  { _animated = true ; 
  }
  public boolean isAnimated()
  { return _animated ; }
  
  public void run() 
  {
    int count = 0 ; 
    System.out.println("entering animation") ; 
    while ( _animated )
    {
      try {
        Thread.sleep(200) ;
      } catch ( InterruptedException e )
      {}
      _candidate.setCursor(_cursors[count % _cursors.length]) ;
      count++ ;
    }
    System.out.println("leaving animation") ; 
    _candidate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      
  }
  
  public static void main(String[] args)
  {
  final JFrame f = new JFrame("changing cursor shape") ;
  final JButton button = new JButton("Start Animation") ; 
    button.addActionListener( 
        new ActionListener()
        {
        AnimatedCursor _cursor = new AnimatedCursor(f)  ;
          public void actionPerformed( ActionEvent e)
          {
            if ( _cursor.isAnimated() )
            {
              button.setText("start Animation") ; 
              // _cursor.stopWaitingCursor() ;
              _cursor.stopAnimation() ; 
              _cursor = new AnimatedCursor(f) ;
            }
            else
            {
              button.setText("stop Animation") ; 
               new Thread( _cursor).start()  ;
              //_cursor.startWaitingCursor() ; 
            }
          }
        }
                            ) ; 
    
    f.getContentPane().add(button) ; 
    f.pack() ; 
    f.setVisible(true) ; 

  }

}
