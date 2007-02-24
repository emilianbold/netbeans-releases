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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces;

import java.util.HashMap;


/**
 * This singleton class is used to keep the lifeline pieces from moving multiple
 * times during a "move pieces" operation.
 *
 * @author Trey Spiva
 */
public class RecursiveHelper
{
   private static HashMap< String, PiecesInfo > m_PiecesInfos = new HashMap< String, PiecesInfo >();

   private String m_strName;


   public RecursiveHelper( String strName )
   {
      m_strName = strName;

      PiecesInfo info = getPiecesInfo( strName );
      info.m_lRefCount++;
   }
   
   public void done()
   {
      PiecesInfo info = getPiecesInfo( m_strName );
      if( --info.m_lRefCount <= 0 )
      {
         assert ( 0 == info.m_lRefCount );
         info.m_setPieces.clear();
      }
   }

   /**
    * Return true if the piece is not in the static member set of pieces, or if the helper is not active
    */
   static public boolean isOkToUsePiece( String strName, LifelineCompartmentPiece piece )
   {
      if( null == piece )
      {
         return false;
      }

      PiecesInfo info = getPiecesInfo( strName );
      if( info.m_lRefCount <= 0 )
      {
         return true;
      }

      final boolean bIsOkToUsePiece = !info.m_setPieces.contains( piece );
      if( bIsOkToUsePiece )
      {
         info.m_setPieces.add( piece );
      }

      return bIsOkToUsePiece;
   }

   
   static protected PiecesInfo getPiecesInfo( String strName )
   {
      PiecesInfo info = null;
      
      info = m_PiecesInfos.get( strName );
      if( null == info )
      {
         info = new PiecesInfo();
         m_PiecesInfos.put( strName, info );
      }
      
      return info;
   }
}
