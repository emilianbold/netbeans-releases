/*
* DebuggerAnnotation.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/
package org.netbeans.modules.python.debugger;

import org.openide.text.Annotation;
import org.openide.text.Annotatable;

/**
 *
 * @author jean-yves
 */
public class DebuggerAnnotation 
extends Annotation
{
  /** Annotation type constant. */
  public static final String BREAKPOINT_ANNOTATION_TYPE = 
      new String ("Breakpoint");
  /** Annotation type constant. */
  public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = 
      new String ("DisabledBreakpoint");
  /** Annotation type constant. */
  public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = 
      new String ("CondBreakpoint");
  /** Annotation type constant. */
  public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = 
      new String ("DisabledCondBreakpoint");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_ANNOTATION_TYPE =
      new String ("CurrentPC");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_ANNOTATION_TYPE2 =
      new String ("CurrentPC2");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_PART_ANNOTATION_TYPE =
      new String ("CurrentPCLinePart");
  /** Annotation type constant. */
  public static final String NEXT_TARGET_NAME =
      new String ("NextTargetName");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 =
      new String ("CurrentPC2LinePart");
  /** Annotation type constant. */
  public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =
      new String ("CallSite");
  
  
  private Annotatable _annotatable;
  private String      _type;
  
  /** Creates a new instance of DebuggerAnnotation */
  public DebuggerAnnotation (String type, Annotatable annotatable) 
  {
    _type = type;
    _annotatable = annotatable;
    if ( _annotatable != null )
      attach (annotatable);
  }
    
 
  public String getShortDescription () 
  {
    if (_type == BREAKPOINT_ANNOTATION_TYPE)
      return "Breakpoint"; 
    else 
    if ( _type == DISABLED_BREAKPOINT_ANNOTATION_TYPE)
      return "Disabled Breakpoint" ;
    else 
    if (_type == CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
      return "Disabled Conditional Breakpoint" ;
    else
    if (_type == DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
      return "Disabled Conditional Breakpoint" ;
    else
    if (_type == CURRENT_LINE_ANNOTATION_TYPE)
      return "Current Program Counter" ;
    else
    if (_type == CALL_STACK_FRAME_ANNOTATION_TYPE)
      return "Call Stack Line" ;
    return "TOOLTIP_ANNOTATION"; 
  }  
  
  public String getAnnotationType () 
  {
    return _type;
  }
  
}
