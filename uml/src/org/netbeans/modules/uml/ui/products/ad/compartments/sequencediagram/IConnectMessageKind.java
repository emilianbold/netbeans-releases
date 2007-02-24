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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

/**
 *
 * @author Trey Spiva
 */
public interface IConnectMessageKind
{
   public final static int CMK_UNKNOWN              = 0;
   public final static int CMK_LEFT_TO_RIGHT        = 0;
   public final static int CMK_RIGHT_TO_LEFT        = 1;
   public final static int CMK_START                = 0;
   public final static int CMK_FINISH               = 2;
   public final static int CMK_START_LEFT_TO_RIGHT  = (CMK_START | CMK_LEFT_TO_RIGHT);
   public final static int CMK_FINISH_LEFT_TO_RIGHT = (CMK_FINISH | CMK_LEFT_TO_RIGHT);
   public final static int CMK_START_RIGHT_TO_LEFT  = (CMK_START | CMK_RIGHT_TO_LEFT);
   public final static int CMK_FINISH_RIGHT_TO_LEFT = (CMK_FINISH | CMK_RIGHT_TO_LEFT);
}
