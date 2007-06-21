// Copyright 2005, 2007 Nokia Corporation. All rights reserved.
//
// The contents of this file are subject to the terms of the Common
// Development and Distribution License (the License). See LICENSE.TXT for exact terms.
// You may not use this file except in compliance with the License.  You can obtain a copy of the
// License at http://www.netbeans.org/cddl.html
//
// When distributing Covered Code, include this CDDL Header Notice in each
// file and include the License. If applicable, add the following below the
// CDDL Header, with the fields enclosed by brackets [] replaced by your own
// identifying information:
// "Portions Copyrighted [year] [name of copyright owner]"

#include "CONADefinitions.h"
#include <jni.h>

#ifndef __NDSJMETOCONA_H__
#define __NDSJMETOCONA_H__

DWORD CALLBACK DeviceNotifyCallback(DWORD dwStatus, WCHAR* pstrSerialNumber);
DWORD CALLBACK FileOperationNotifyCallback(DWORD dwFSFunction, DWORD dwState, DWORD dwTransferredBytes, DWORD dwAllBytes);

//-----------------------------------------------------------------------------
// CONAEventHandler declaration
//-----------------------------------------------------------------------------
class CONAEventHandler
{
private:
	JNIEnv* m_pEnv;
	jobject m_object;
	JavaVM* m_pVM;

	int setCurrentFolderStatus;
	int createFolderStatus;
	int putFileStatus;
	int installFileStatus;

public:
	CONAEventHandler(JNIEnv* jniEnv, jobject obj);
	~CONAEventHandler();
	
	JNIEnv* getJavaEnv();
	jobject getJavaObject();
	JavaVM* getJavaVM();
	int getState(int type);
	void setState(int state, int type);
	void connectionLost();
};

#endif //__NDSJMETOCONA_H__