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

#include "DoorUtility.h"

#import "doorsdxl.tlb"  no_namespace named_guids

extern "C"
{
JNIEXPORT void JNICALL Java_org_netbeans_modules_uml_requirements_doorsprovider_DoorUtility_initialize
  (JNIEnv *, jclass)
{

   ::CoInitialize(NULL);
}

JNIEXPORT jstring JNICALL Java_org_netbeans_modules_uml_requirements_doorsprovider_DoorUtility_sendRequestToDoorsViaOS
  (JNIEnv *env, jclass _this, jstring request)
{

   try
   {
      DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );
      const char* str = env->GetStringUTFChars( request, 0 );
      pDoors->runStr( str );
      _bstr_t bstrResult = pDoors->Getresult();

      env->ReleaseStringUTFChars(request, str);
      return env->NewStringUTF(bstrResult);
   }
   catch(...)
   {
      jclass newExcCls = env->FindClass("org/netbeans/modules/uml/core/requirementsframework/RequirementsException");
      if(newExcCls != 0)
      {
         env->ThrowNew(newExcCls, "");
      }
   }

   return env->NewStringUTF("");
   
}

}
