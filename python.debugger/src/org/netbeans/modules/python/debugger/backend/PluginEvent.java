/**
* Copyright (C) 2003 Jean-Yves Mengant
*
*/


package org.netbeans.modules.python.debugger.backend;

/**
 * @author jean-yves
 *
 * commuenication event between Debugger Front End and Pluggin editors
 */
public class PluginEvent
{
	public final static int UNDEFINED = -1 ;

	public final static int NEWSOURCE = 0 ;
	public final static int NEWLINE   = 1 ; 
	public final static int STARTING  = 2 ; 
	public final static int ENDING    = 3 ; 
	public final static int ENTERCALL = 4 ; 
	public final static int LEAVECALL = 5 ; 
	public final static int BUSY      = 6 ; 
	public final static int NOTBUSY   = 7 ; 
	 

    private int _type = UNDEFINED ; 
    private String _source = null ; 
    private int    _line   = UNDEFINED	 ; 
    
    
    public PluginEvent( int type , String source , int line )
    {
      _type = type ; 
      _source = source ; 
      _line = line ;     	
    }

    public int get_type()
    { return _type ;}
    
    public String get_source()
    { return _source ;}

    public int get_line()
    { return _line ; }

}
