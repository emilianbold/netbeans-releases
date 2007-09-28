/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
