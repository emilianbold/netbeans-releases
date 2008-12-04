/*

Utility package exception class
 * 
Copyright (C) 2008  Jean-Yves Mengant

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*/

package org.netbeans.modules.python.debugger.utils;



public class UtilsError extends Exception {

  /**
  default constructor for UtilsError
  @param no parameters 
  */
  public UtilsError (){}

  /**
  Constructor providing an Explanation message
  @param msg Exception explanation message
  */
  public UtilsError ( String msg  )
  { super(msg) ; }

}
