/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.support.diagramsupport;


/**
 * @author KevinM
 *
 * Converts between Displaynames and ILayoutKind, and other conversion methods for communication with the layout server.
 */
public class ETLayoutStyleMap
{
    /*
   static public int getLayoutStyle(String diagramKindDisplayName, TSEGraph pGraph)
   {
      int pLayoutStyle = ILayoutKind.LK_NO_LAYOUT;
      if (pGraph != null && diagramKindDisplayName != null)
      {         
         if (diagramKindDisplayName.equals("Sequence Diagram"))
         {
            pLayoutStyle = ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT;
         }
         else
         {  
            String layoutStyle = pGraph.getLayoutStyle().toUpperCase();
            if (layoutStyle.equals("CIRCULAR"))
            {
               pLayoutStyle = ILayoutKind.LK_CIRCULAR_LAYOUT;
            }
            else if (layoutStyle.equals("HIERARCHICAL"))
            {
               pLayoutStyle = ILayoutKind.LK_HIERARCHICAL_LAYOUT;
            }
            else if (layoutStyle.equals("ORTHOGONAL"))
            {
               pLayoutStyle = ILayoutKind.LK_ORTHOGONAL_LAYOUT;
            }
            else if (layoutStyle.equals("SYMMETRIC"))
            {
               pLayoutStyle = ILayoutKind.LK_SYMMETRIC_LAYOUT;
            }
            else if (layoutStyle.equals("TREE"))
            {
               pLayoutStyle = ILayoutKind.LK_TREE_LAYOUT;
            }              
         }             
      }
      return pLayoutStyle;
   }
*/
//    static public int getLayoutStyle(String diagramKindDisplayName, TSEGraph pGraph, TSLayoutInputTailor layoutInputTailor) {
//        if (pGraph != null && diagramKindDisplayName != null) {
//            if (diagramKindDisplayName.equals("Sequence Diagram")) {
//                return ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT;
//            } else if (diagramKindDisplayName.equals("Collaboration Diagram")) {
//				return ILayoutKind.LK_SYMMETRIC_LAYOUT;
//			}
//			else {
//                //String layoutStyle = pGraph.getLayoutStyle().toUpperCase();
//                int layoutStyle = layoutInputTailor.getLayoutStyle(pGraph);
//                switch(layoutStyle) {
////                    case TSLayoutConstants.LAYOUT_STYLE_CIRCULAR:     return ILayoutKind.LK_CIRCULAR_LAYOUT;
//                    case TSLayoutConstants.LAYOUT_STYLE_HIERARCHICAL: return ILayoutKind.LK_HIERARCHICAL_LAYOUT;
//                    case TSLayoutConstants.LAYOUT_STYLE_ORTHOGONAL:   return ILayoutKind.LK_ORTHOGONAL_LAYOUT;
//                    case TSLayoutConstants.LAYOUT_STYLE_SYMMETRIC:    return ILayoutKind.LK_SYMMETRIC_LAYOUT;
////                    case TSLayoutConstants.LAYOUT_STYLE_TREE:         return ILayoutKind.LK_TREE_LAYOUT;
////                    case TSLayoutConstants.LAYOUT_STYLE_GRID:         return ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT;
//                }
//            }
//        }
//        return ILayoutKind.LK_NO_LAYOUT;
//    }

    /**
     * Maps the internal Layout constant which is defined at the {@link ILayoutKind} 
     * to the external constant is defined at TS library.
     */
//    public static int mapLayoutKind2TsLayout(int layout) {
//        switch (layout) {
//            case ILayoutKind.LK_HIERARCHICAL_LAYOUT:    return TSLayoutConstants.LAYOUT_STYLE_HIERARCHICAL;
//            case ILayoutKind.LK_ORTHOGONAL_LAYOUT:      return TSLayoutConstants.LAYOUT_STYLE_ORTHOGONAL;
//            case ILayoutKind.LK_SYMMETRIC_LAYOUT:       return TSLayoutConstants.LAYOUT_STYLE_SYMMETRIC;
////            case ILayoutKind.LK_TREE_LAYOUT:            return TSLayoutConstants.LAYOUT_STYLE_TREE;
////            case ILayoutKind.LK_CIRCULAR_LAYOUT:        return TSLayoutConstants.LAYOUT_STYLE_CIRCULAR;
//            case ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT: return TSLayoutConstants.LAYOUT_STYLE_GRID;
//            // case ILayoutKind.LK_GLOBAL_LAYOUT:          return TSLayoutConstants.LAYOUT_STYLE_?;
//            // case ILayoutKind.LK_INCREMENTAL_LAYOUT:     return TSLayoutConstants.LAYOUT_STYLE_?;
//            default: return TSLayoutConstants.LAYOUT_STYLE_NO_STYLE; 
//        }
//    }
//    
   /*
    * Converts an ILayoutKind into a string for communication with the layout server.
    //jyothi
   static public String getLayoutCommandString(int ilayoutKind, TSEGraph graph)
   {
      switch (ilayoutKind)
      {
          /* jyothi      
         case ILayoutKind.LK_NO_LAYOUT :
            return null;           
         case ILayoutKind.LK_HIERARCHICAL_LAYOUT :
            return TSDGraph.HIERARCHICAL;
         case ILayoutKind.LK_CIRCULAR_LAYOUT :
            return TSDGraph.CIRCULAR;
         case ILayoutKind.LK_SYMMETRIC_LAYOUT :
            return TSDGraph.SYMMETRIC;
         case ILayoutKind.LK_TREE_LAYOUT :
            return TSDGraph.TREE;
         case ILayoutKind.LK_ORTHOGONAL_LAYOUT :
            return TSDGraph.ORTHOGONAL;
         case ILayoutKind.LK_SEQUENCEDIAGRAM_LAYOUT :
            return ADDrawingAreaConstants.SEQUENCE_LAYOUT;
         case ILayoutKind.LK_UNKNOWN_LAYOUT :
         case ILayoutKind.LK_GLOBAL_LAYOUT :
            return graph != null ? graph.getLayoutStyle() : null;
         case ILayoutKind.LK_INCREMENTAL_LAYOUT :
            return graph != null ? graph.getLayoutStyle() : null;
             
      }
      return null;
   }
    */
  
}
