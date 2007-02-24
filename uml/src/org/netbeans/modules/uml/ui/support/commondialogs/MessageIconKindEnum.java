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


/*
 *
 * Created on Jul 1, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.commondialogs;

/**
 * The MessageIconKindEnum is used to specify the icon to use when displaying
 * a message dialog to the user.
 *
 * @author Trey Spiva
 */
public interface MessageIconKindEnum
{
   public final static int EDIK_ICONINFORMATION = 0;
   public final static int EDIK_ICONHAND        = 1;
   public final static int EDIK_ICONSTOP        = 2;
   public final static int EDIK_ICONERROR       = 3;
   public final static int EDIK_ICONQUESTION    = 4;
   public final static int EDIK_ICONEXCLAMATION = 5;
   public final static int EDIK_ICONWARNING     = 6;
   public final static int EDIK_ICONASTERISK    = 7;
}
