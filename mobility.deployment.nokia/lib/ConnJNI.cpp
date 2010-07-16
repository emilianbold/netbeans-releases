// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
//
// Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
//
// Oracle and Java are registered trademarks of Oracle and/or its affiliates.
// Other names may be trademarks of their respective owners.
//
// The contents of this file are subject to the terms of either the GNU
// General Public License Version 2 only ("GPL") or the Common
// Development and Distribution License("CDDL") (collectively, the
// "License"). You may not use this file except in compliance with the
// License. You can obtain a copy of the License at
// http://www.netbeans.org/cddl-gplv2.html
// or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
// specific language governing permissions and limitations under the
// License.  When distributing the software, include this License Header
// Notice in each file and include the License file at
// nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
// particular file as subject to the "Classpath" exception as provided
// by Oracle in the GPL Version 2 section of the License file that
// accompanied this code. If applicable, add the following below the
// License Header, with the fields enclosed by brackets [] replaced by
// your own identifying information:
// "Portions Copyrighted [year] [name of copyright owner]"
// 
// Contributor(s):
// 
// The Original Software is the Nokia Deployment.                      
// The Initial Developer of the Original Software is Nokia Corporation.
// Portions created by Nokia Corporation Copyright 2005, 2007.         
// All Rights Reserved.                                                
//
// If you wish your version of this file to be governed by only the CDDL
// or only the GPL Version 2, indicate your decision by adding
// "[Contributor] elects to include this software in this distribution
// under the [CDDL or GPL Version 2] license." If you do not indicate a
// single choice of license, a recipient has the option to distribute
// your version of this file under either the CDDL, the GPL Version 2 or
// to extend the choice of license to its licensees as provided above.
// However, if you add GPL Version 2 code and therefore, elected the GPL
// Version 2 license, then the option applies only if the new code is
// made subject to such option by the copyright holder.

// ConnJNI.cpp : Defines the entry point for the DLL application.
#include <windows.h>

#include "Connapi.h"
#include "CONAApplicationInstallation.h"
#include "CONAFileSystem.h"
#include "jni.h"
#include "CONA.h"
#include "CONAEvent.h"

int last_connection_id_ = -1;
DMHANDLE hDMHandle = NULL;
FSHANDLE hFSHandle = NULL;
CONAEventHandler* pEventHandler = NULL;
int isInitialized = 0;

BOOL APIENTRY DllMain(
	HANDLE hModule, 
	DWORD  ul_reason_for_call, 
	LPVOID lpReserved)
{
	return TRUE;
}

JNIEXPORT jstring JNICALL Java_com_nokia_phone_deploy_CONA_native_1getVersion
(JNIEnv *env, jobject obj) 
{
	jstring retValue = env->NewStringUTF("ConnJNI.DLL version 1.0");
	return retValue;
}

JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1connectServiceLayer
(JNIEnv *env, jobject obj)
{
	jboolean retVal = FALSE;

	DWORD outResult = CONA_OK;
	DWORD dwVersion = 10;
	WCHAR* pstrManufacturer = NULL; //L"Nokia"; // NULL, if all
	
	outResult = CONAInitialize(dwVersion, pstrManufacturer, NULL);

	if(outResult == CONA_OK)
	{
		outResult = CONAOpenDM(&hDMHandle);
		if(outResult == CONA_OK)
		{
			outResult = CONARegisterNotifyCallback(hDMHandle, CONAPI_REGISTER, &DeviceNotifyCallback);
			if(outResult == CONA_OK)
			{
				isInitialized = 1;
				pEventHandler = new CONAEventHandler(env, obj);
				retVal = TRUE;
			}else
			{
				retVal = FALSE;
			}
		}else
		{
#ifdef _DEBUG
			char errorText[100];
			switch(outResult)
			{
			case ECONA_INVALID_POINTER:
				sprintf(errorText, "ECONA_INVALID_POINTER");
				break;
			case ECONA_NOT_INITIALIZED:
				sprintf(errorText, "ECONA_NOT_INITIALIZED");
				break;
			case ECONA_NOT_ENOUGH_MEMORY:
				sprintf(errorText, "ECONA_NOT_ENOUGH_MEMORY");
				break;
			case ECONA_INIT_FAILED_COM_INTERFACE:
				sprintf(errorText, "ECONA_INIT_FAILED_COM_INTERFACE");
				break;
			case ECONA_UNKNOWN_ERROR:
				sprintf(errorText, "ECONA_UNKNOWN_ERROR");
				break;
			default:
				sprintf(errorText, "THIS SHOULD NOT SEEN!!!");
				break;
			}

			printf("CONAOpenDM error: %s\n", errorText);
#endif
			retVal = FALSE;
		}
	} else
	{
#ifdef _DEBUG
		char errorText[100];
		switch(outResult)
		{
		case ECONA_INIT_FAILED:
			sprintf(errorText, "ECONA_INIT_FAILED");
			break;
//		case ECONA_INIT_FAILED_WRONG_MODE:
//			sprintf(errorText, "ECONA_INIT_FAILED_WRONG_MODE");
//			break;
		case ECONA_INIT_FAILED_COM_INTERFACE:
			sprintf(errorText, "ECONA_INIT_FAILED_COM_INTERFACE");
			break;
		case ECONA_UNSUPPORTED_API_VERSION:
			sprintf(errorText, "ECONA_UNSUPPORTED_API_VERSION");
			break;
		case ECONA_NOT_SUPPORTED_MANUFACTURER:
			sprintf(errorText, "ECONA_NOT_SUPPORTED_MANUFACTURER");
			break;
		case ECONA_INVALID_PARAMETER:
			sprintf(errorText, "ECONA_INVALID_PARAMETER");
			break;
		case ECONA_INVALID_POINTER:
			sprintf(errorText, "ECONA_INVALID_POINTER");
			break;
		default:
			sprintf(errorText, "THIS SHOULD NOT SEEN!!!");
			break;
		}

		printf("CONAInitialize error: %s\n", errorText);
#endif
		retVal = FALSE;
	}

	return retVal;
}

JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1disconnectServiceLayer
(JNIEnv *env, jobject obj) 
{
    printf("CONA.disconnectServiceLayer: env=%X, obj=%X\n", (DWORD)env, (DWORD)obj);
	if( ! isInitialized)
	{
		return TRUE;
	}
    printf("CONA.disconnectServiceLayer: ... 0\n");

	jboolean retValue = TRUE;

	DWORD outResult = CONA_OK;

	if(hDMHandle)
	{
        printf("CONA.disconnectServiceLayer: ... 1\n");
		outResult = CONARegisterNotifyCallback(hDMHandle, CONAPI_UNREGISTER, &DeviceNotifyCallback);
        printf("CONA.disconnectServiceLayer: ... 2: outResult=%X\n", outResult);
		outResult = CONACloseDM(hDMHandle);
        printf("CONA.disconnectServiceLayer: ... 3: outResult=%X\n", outResult);
		if(outResult != CONA_OK)
		{
			retValue = FALSE;
		}

		hDMHandle = NULL;
	}

    printf("CONA.disconnectServiceLayer: ... 4\n");
	outResult = CONAUninitialize(0);
    printf("CONA.disconnectServiceLayer: ... 5: outResult=%X\n", outResult);

	if(outResult != CONA_OK)
	{
		retValue = FALSE;
	}

	if(pEventHandler)
	{
    printf("CONA.disconnectServiceLayer: ... 6: pEventHandler=%X\n", (DWORD)pEventHandler);
		delete pEventHandler;
		pEventHandler = NULL;
	}

    printf("CONA.disconnectServiceLayer: ... 7\n");
	return retValue;
}

JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1updateDeviceList
(JNIEnv *env, jobject obj) 
{
	jboolean retVal = FALSE;
	DWORD outResult = CONA_OK;
	outResult = CONARefreshDeviceList(hDMHandle, 0);
	if(outResult == CONA_OK)
	{
		retVal = TRUE;
	}

	return retVal;
}

char*  append(char *dest, char *source)
{
	int n1 = (dest == NULL) ? 0 : strlen(dest);
	int n2 = (source == NULL) ? 0 : strlen(source);
	char* b;
	b = (char*) malloc(n1 + n2 + 1);
	memcpy(b, dest, n1 * sizeof(char));
	memcpy(b + n1, source, n2*sizeof(char));
	b[n1 + n2] = '\0';
	return b;
}

JNIEXPORT jstring JNICALL Java_com_nokia_phone_deploy_CONA_native_1getDevices
(JNIEnv *env, jobject obj, jint mediaType) 
{
	jstring retValue = NULL;
	char* devicenames = NULL;
	DWORD outResult = CONA_OK;
	DWORD dwCountOfDevice = 0;
	outResult = CONAGetDeviceCount(hDMHandle, &dwCountOfDevice);

	if(outResult == CONA_OK && dwCountOfDevice > 0)
	{
		CONAPI_DEVICE *pDevices = new CONAPI_DEVICE[dwCountOfDevice];
		if(CONA_OK == CONAGetDevices(hDMHandle, &dwCountOfDevice, pDevices))
		{
			for(DWORD i = 0; i < dwCountOfDevice; i++)
			{
				for(DWORD dwConn = 0; dwConn < pDevices[i].dwNumberOfItems; dwConn++)
				{
					if(pDevices[i].pItems[dwConn].dwMedia == (DWORD)mediaType)
					{
						char  str[128];
						sprintf(
							str,
							"%S (ID:%i)",
								pDevices[i].pItems[dwConn].pstrDeviceName,
								pDevices[i].pItems[dwConn].dwDeviceID);

						if (devicenames != NULL)
						{
							char* old_device_names = devicenames;
							devicenames = append(devicenames, ",");
							free(old_device_names);
						}

						devicenames = append(devicenames, str);
					}
				}
			}
			
			if (env != NULL)
				retValue = env->NewStringUTF(devicenames);
			free(devicenames);

			CONAFreeDeviceStructure(dwCountOfDevice, pDevices);
		}
		delete[] pDevices;
		pDevices = NULL;
	}

	return retValue;
}

CONAPI_DEVICE* getDeviceByDeviceID(DWORD deviceId)
{
	CONAPI_DEVICE* retVal = NULL;
	DWORD outResult = CONA_OK;
	DWORD dwCountOfDevice = 0;
	outResult = CONAGetDeviceCount(hDMHandle, &dwCountOfDevice);

	if(outResult == CONA_OK && dwCountOfDevice > 0)
	{
		CONAPI_DEVICE* pDevices = new CONAPI_DEVICE[dwCountOfDevice];
		if(CONA_OK == CONAGetDevices(hDMHandle, &dwCountOfDevice, pDevices))
		{
			for(DWORD i = 0; i < dwCountOfDevice; i++)
			{
				for(DWORD dwConn = 0; dwConn < pDevices[i].dwNumberOfItems; dwConn++)
				{
					if(pDevices[i].pItems[dwConn].dwDeviceID == deviceId)
					{
						retVal = new CONAPI_DEVICE;
						*retVal = pDevices[i];
						retVal->pItems = new CONAPI_CONNECTION_INFO[pDevices[i].dwNumberOfItems];
						for(DWORD tConn = 0; tConn < pDevices[i].dwNumberOfItems; tConn++)
						{
							retVal->pItems[tConn] = pDevices[i].pItems[dwConn];
						}
					}
				}
			}

			CONAFreeDeviceStructure(dwCountOfDevice, pDevices);
		}
		delete[] pDevices;
		pDevices = NULL;
	}

	return retVal;
}

void freeDevice(CONAPI_DEVICE* pDevice)
{
	delete[] pDevice->pItems;
	pDevice->pItems = NULL;

	delete pDevice;
	pDevice = 0;
}


JNIEXPORT jstring JNICALL Java_com_nokia_phone_deploy_CONA_native_1getDeviceType
(JNIEnv *env, jobject obj, jint id )
{
	jstring retVal = NULL;
	DWORD mediaType = 0;
	
	CONAPI_DEVICE* pDevice = getDeviceByDeviceID((DWORD) id);

	if(pDevice == NULL)
		return NULL;

	for(DWORD dwConn = 0; dwConn < pDevice->dwNumberOfItems; dwConn++)
	{
		if(pDevice->pItems[dwConn].dwDeviceID == (DWORD) id)
		{
			mediaType = pDevice->pItems[dwConn].dwMedia;
		}
	}
	
	freeDevice(pDevice);

	switch(mediaType)
	{
		case CONAPI_MEDIA_IRDA:
			retVal = env->NewStringUTF("IRDA");
			break;
		case CONAPI_MEDIA_SERIAL:
			retVal = env->NewStringUTF("RS232");
			break;
		case CONAPI_MEDIA_BLUETOOTH:
			retVal = env->NewStringUTF("BLUETOOTH");
			break;
		case CONAPI_MEDIA_USB:
			retVal = env->NewStringUTF("USB");
			break;
		default:
			break;
	}

	return retVal;
}


JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1openConnectionTo
(JNIEnv *env, jobject obj, jint id)
{
	jboolean retVal = FALSE;

	// get device serial number
	WCHAR* pstrSerialNumber = NULL;
	DWORD mediaType = 0;

	CONAPI_DEVICE* pDevice = getDeviceByDeviceID((DWORD) id);

	if(pDevice == NULL)
		return FALSE;

	for(DWORD dwConn = 0; dwConn < pDevice->dwNumberOfItems; dwConn++)
	{
		if(pDevice->pItems[dwConn].dwDeviceID == (DWORD) id)
		{
			pstrSerialNumber = pDevice->pstrSerialNumber;
			mediaType = pDevice->pItems[dwConn].dwMedia;
		}
	}

	DWORD dwDeviceID = (DWORD) id;
	DWORD outResult = CONAOpenFS(pstrSerialNumber, &mediaType, &hFSHandle, &dwDeviceID);
	if(CONA_OK == outResult)
	{
		retVal = TRUE;
		//outResult = CONARegisterFSNotifyCallback(hFSHandle, CONAPI_REGISTER, &FileOperationNotifyCallback);
		if(outResult == CONA_OK)
		{
			retVal = TRUE;
		} else
		{
			retVal = FALSE;
		}
	} else
	{
#ifdef _DEBUG
		char errorText[100];
		switch(outResult)
		{
			case ECONA_DEVICE_NOT_FOUND:
				sprintf(errorText, "ECONA_DEVICE_NOT_FOUND");
				break;
			case ECONA_NO_CONNECTION_VIA_MEDIA:
				sprintf(errorText, "ECONA_NO_CONNECTION_VIA_MEDIA");
				break;
			case ECONA_NOT_SUPPORTED_DEVICE:
				sprintf(errorText, "ECONA_NOT_SUPPORTED_DEVICE");
				break;
			case ECONA_CONNECTION_FAILED:
				sprintf(errorText, "ECONA_CONNECTION_FAILED");
				break;
			case ECONA_CONNECTION_BUSY:
				sprintf(errorText, "ECONA_CONNECTION_BUSY");
				break;
			case ECONA_CONNECTION_LOST:
				sprintf(errorText, "ECONA_CONNECTION_LOST");
				break;
			case ECONA_FAILED_TIMEOUT:
				sprintf(errorText, "ECONA_FAILED_TIMEOUT");
				break;
			case ECONA_INVALID_POINTER:
				sprintf(errorText, "ECONA_INVALID_POINTER");
				break;
			case ECONA_NOT_INITIALIZED:
				sprintf(errorText, "ECONA_NOT_INITIALIZED");
				break;
			case ECONA_NOT_SUPPORTED_MANUFACTURER:
				sprintf(errorText, "ECONA_NOT_SUPPORTED_MANUFACTURER");
				break;
			case ECONA_NOT_ENOUGH_MEMORY:
				sprintf(errorText, "ECONA_NOT_ENOUGH_MEMORY");
				break;
			case ECONA_UNKNOWN_ERROR:
				sprintf(errorText, "ECONA_UNKNOWN_ERROR");
				break;
			default:
				break;
		}

		printf("CONAOpenFS error: %s\n", errorText);
#endif
		retVal = FALSE;
		hFSHandle = NULL;
	}

	freeDevice(pDevice);

	return retVal;
}

JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1closeConnection
(JNIEnv *env, jobject obj) 
{
	jboolean retVal = FALSE;
	DWORD outResult = CONA_OK;

	//outResult = CONARegisterFSNotifyCallback(hFSHandle, CONAPI_UNREGISTER, &FileOperationNotifyCallback);

	outResult = CONACloseFS(hFSHandle);

	if(outResult == CONA_OK)
	{
		hFSHandle = NULL;
		retVal = TRUE;
	}

	return retVal;
}


JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1setCurrentFolder
(JNIEnv *env, jobject obj, jstring jfolder) 
{
	jboolean retVal = FALSE;

	const jchar* folder = env->GetStringChars(jfolder,0);
	
	DWORD outResult = CONASetCurrentFolder(hFSHandle, (WCHAR*) folder);

	env->ReleaseStringChars(jfolder, folder);

	if(outResult == CONA_OK)
	{
		retVal = TRUE;
	}
#ifdef _DEBUG	
	else
	{
		char errorText[100];
		switch(outResult)
		{
		case ECONA_INVALID_HANDLE:
			sprintf(errorText, "ECONA_INVALID_HANDLE");
			break;
		case ECONA_INVALID_PARAMETER:
			sprintf(errorText, "ECONA_INVALID_PARAMETER");
			break;
		case ECONA_CONNECTION_BUSY:
			sprintf(errorText, "ECONA_CONNECTION_BUSY");
			break;
		case ECONA_CONNECTION_LOST:
			sprintf(errorText, "ECONA_CONNECTION_LOST");
			break;
		case ECONA_CURRENT_FOLDER_NOT_FOUND:
			sprintf(errorText, "ECONA_CURRENT_FOLDER_NOT_FOUND");
			break;
		case ECONA_FOLDER_PATH_TOO_LONG:
			sprintf(errorText, "ECONA_FOLDER_PATH_TOO_LONG");
			break;
		case ECONA_FOLDER_NAME_INVALID:
			sprintf(errorText, "ECONA_FOLDER_NAME_INVALID");
			break;
		case ECONA_FOLDER_NO_PERMISSION:
			sprintf(errorText, "ECONA_FOLDER_NO_PERMISSION");
			break;
		case ECONA_CANCELLED:
			sprintf(errorText, "ECONA_CANCELLED");
			break;
		case ECONA_FAILED_TIMEOUT:
			sprintf(errorText, "ECONA_FAILED_TIMEOUT");
			break;
		case ECONA_UNKNOWN_ERROR_DEVICE:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR_DEVICE");
			break;
		case ECONA_NOT_INITIALIZED:
			sprintf(errorText, "ECONA_NOT_INITIALIZED");
			break;
		case ECONA_UNKNOWN_ERROR:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR");
			break;
		default:
			sprintf(errorText, "THIS SHOULD NOT SEEN!!!, code=%X", outResult);
			break;
		}

		printf("CONASetCurrentFolder error: %s\n", errorText);
		retVal = FALSE;
	}
#endif //_DEBUG

	return retVal;
}

JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1createFolder
(JNIEnv *env, jobject obj, jstring jfolder) 
{
	jboolean retVal = FALSE;

	const jchar* folder = env->GetStringChars(jfolder,0);

	DWORD outResult = CONACreateFolder(hFSHandle, (WCHAR*) folder, NULL);

	env->ReleaseStringChars(jfolder, folder);

	if(outResult == CONA_OK)
	{
		retVal = TRUE;
	}
#ifdef _DEBUG	
	else
	{
		char errorText[100];
		switch(outResult)
		{
		case ECONA_INVALID_HANDLE:
			sprintf(errorText, "ECONA_INVALID_HANDLE");
			break;
		case ECONA_INVALID_PARAMETER:
			sprintf(errorText, "ECONA_INVALID_PARAMETER");
			break;
		case ECONA_CONNECTION_BUSY:
			sprintf(errorText, "ECONA_CONNECTION_BUSY");
			break;
		case ECONA_CONNECTION_LOST:
			sprintf(errorText, "ECONA_CONNECTION_LOST");
			break;
		case ECONA_INVALID_DATA_DEVICE:
			sprintf(errorText, "ECONA_INVALID_DATA_DEVICE");
			break;
		case ECONA_CURRENT_FOLDER_NOT_FOUND:
			sprintf(errorText, "ECONA_CURRENT_FOLDER_NOT_FOUND");
			break;
		case ECONA_FOLDER_PATH_TOO_LONG:
			sprintf(errorText, "ECONA_FOLDER_PATH_TOO_LONG");
			break;
		case ECONA_FOLDER_NAME_INVALID:
			sprintf(errorText, "ECONA_FOLDER_NAME_INVALID");
			break;
		case ECONA_FOLDER_ALREADY_EXIST:
			sprintf(errorText, "ECONA_FOLDER_ALREADY_EXIST");
			break;
		case ECONA_FOLDER_NO_PERMISSION:
			sprintf(errorText, "ECONA_FOLDER_NO_PERMISSION");
			break;
		case ECONA_CANCELLED:
			sprintf(errorText, "ECONA_CANCELLED");
			break;
		case ECONA_FAILED_TIMEOUT:
			sprintf(errorText, "ECONA_FAILED_TIMEOUT");
			break;
		case ECONA_UNKNOWN_ERROR_DEVICE:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR_DEVICE");
			break;
		case ECONA_NOT_INITIALIZED:
			sprintf(errorText, "ECONA_NOT_INITIALIZED");
			break;
		case ECONA_UNKNOWN_ERROR:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR");
			break;
		default:
			sprintf(errorText, "THIS SHOULD NOT SEEN!!!");
			break;
		}

		printf("CONACreateFolder error: %s\n", errorText);
		retVal = FALSE;
	}
#endif //_DEBUG

	return retVal;
}

JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1putFile
(JNIEnv *env, jobject obj, jstring jfilepath, jstring jtargetpath, jstring jfilename) 
{
	jboolean retVal = FALSE;
	DWORD dwOptions = CONA_DIRECT_PC_TO_PHONE | CONA_OVERWRITE;

	const jchar* filepath = env->GetStringChars(jfilepath, NULL);
	const jchar* targetpath = env->GetStringChars(jtargetpath, NULL);
	const jchar* filename = env->GetStringChars(jfilename, NULL);

    printf("CONACopyFile: filename=%S, filepath=%S, targetpath=%S, hFSHandle=%X, dwOptions=%X\n", (WCHAR*)filename, (WCHAR*)filepath, (WCHAR*)targetpath, (DWORD)hFSHandle, dwOptions);

//	DWORD outResult = CONA_OK;
	DWORD outResult = CONACopyFile(
		hFSHandle,
		dwOptions,
		(WCHAR*) filename,
		(WCHAR*) filepath,
		(WCHAR*) targetpath);

    printf("CONACopyFile: ...1: env=%X, filename=%S\n", (DWORD)env, (WCHAR*)filename);
	env->ReleaseStringChars(jfilename, filename);
    printf("CONACopyFile: ...1.1: targetpath=%S\n", (WCHAR*)targetpath);
	env->ReleaseStringChars(jtargetpath, targetpath);
    printf("CONACopyFile: ...1.2: filepath=%S\n", (WCHAR*)filepath);
	env->ReleaseStringChars(jfilepath, filepath);
    printf("CONACopyFile: ...2\n");

	if(outResult == CONA_OK)
	{
		printf("CONACopyFile: ...2.1\n");
		retVal = TRUE;
	}
#ifdef _DEBUG	
	else
	{
		printf("CONACopyFile: ...2.2: outResult=%X\n" + outResult);
		char errorText[100];
		switch(outResult)
		{
		case ECONA_INVALID_HANDLE:
			sprintf(errorText, "ECONA_INVALID_HANDLE");
			break;
		case ECONA_INVALID_PARAMETER:
			sprintf(errorText, "ECONA_INVALID_PARAMETER");
			break;
		case ECONA_CONNECTION_BUSY:
			sprintf(errorText, "ECONA_CONNECTION_BUSY");
			break;
		case ECONA_CONNECTION_LOST:
			sprintf(errorText, "ECONA_CONNECTION_LOST");
			break;
		case ECONA_INVALID_DATA_DEVICE:
			sprintf(errorText, "ECONA_INVALID_DATA_DEVICE");
			break;
		case ECONA_MEMORY_FULL:
			sprintf(errorText, "ECONA_MEMORY_FULL");
			break;
		case ECONA_CURRENT_FOLDER_NOT_FOUND:
			sprintf(errorText, "ECONA_CURRENT_FOLDER_NOT_FOUND");
			break;
		case ECONA_FOLDER_PATH_TOO_LONG:
			sprintf(errorText, "ECONA_FOLDER_PATH_TOO_LONG");
			break;
		case ECONA_FOLDER_NOT_FOUND:
			sprintf(errorText, "ECONA_FOLDER_NOT_FOUND");
			break;
		case ECONA_FILE_TOO_BIG_DEVICE:
			sprintf(errorText, "ECONA_FILE_TOO_BIG_DEVICE");
			break;
		case ECONA_FILE_NAME_INVALID:
			sprintf(errorText, "ECONA_FILE_NAME_INVALID");
			break;
		case ECONA_FILE_NOT_FOUND:
			sprintf(errorText, "ECONA_FILE_NOT_FOUND");
			break;
		case ECONA_FILE_ALREADY_EXIST:
			sprintf(errorText, "ECONA_FILE_ALREADY_EXIST");
			break;
		case ECONA_FILE_NO_PERMISSION:
			sprintf(errorText, "ECONA_FILE_NO_PERMISSION");
			break;
		case ECONA_FOLDER_NO_PERMISSION:
			sprintf(errorText, "ECONA_FOLDER_NO_PERMISSION");
			break;
		case ECONA_FILE_NO_PERMISSION_ON_PC:
			sprintf(errorText, "ECONA_FILE_NO_PERMISSION_ON_PC");
			break;
		case ECONA_FOLDER_NO_PERMISSION_ON_PC:
			sprintf(errorText, "ECONA_FOLDER_NO_PERMISSION_ON_PC");
			break;
		case ECONA_FILE_BUSY:
			sprintf(errorText, "ECONA_FILE_BUSY");
			break;
		case ECONA_FILE_TYPE_NOT_SUPPORTED:
			sprintf(errorText, "ECONA_FILE_TYPE_NOT_SUPPORTED");
			break;
		case ECONA_CANCELLED:
			sprintf(errorText, "ECONA_CANCELLED");
			break;
		case ECONA_FAILED_TIMEOUT:
			sprintf(errorText, "ECONA_FAILED_TIMEOUT");
			break;
		case ECONA_UNKNOWN_ERROR_DEVICE:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR_DEVICE");
			break;
		case ECONA_NOT_INITIALIZED:
			sprintf(errorText, "ECONA_NOT_INITIALIZED");
			break;
		case ECONA_UNKNOWN_ERROR:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR");
			break;
		default:
			sprintf(errorText, "THIS SHOULD NOT SEEN!!!, code=%X", outResult);
			break;
		}

		printf("CONACopyFile error: %s\n", errorText);
		retVal = FALSE;
	}
#endif //_DEBUG

    printf("CONACopyFile: ...3: retVal=%X\n", retVal);
	return retVal;
}


JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1installFile
(JNIEnv* env, jobject obj, jstring jfilepath, jstring jfilename, jstring jjad, jint fileType, jboolean defaultfolder)
{
	jboolean retVal = FALSE;
	DWORD dwOptions = -1;
	DWORD outResult = CONA_OK;

	const jchar* filepath = env->GetStringChars(jfilepath, 0);
	const jchar* filename = env->GetStringChars(jfilename, 0);
	const jchar* jad = NULL;

	if(defaultfolder)
	{
		dwOptions = (CONA_DEFAULT_FOLDER | CONA_OVERWRITE);
	} else
	{
		dwOptions = CONA_OVERWRITE;
	}

	if(fileType == CONA_APPLICATION_TYPE_JAVA)
	{
		CONAPI_APPLICATION_JAVA appStruct;
		if(jjad != NULL)
		{
			jad = env->GetStringChars(jjad, 0);
			appStruct.pstrFileNameJad = (WCHAR*) jad;
		} else
		{
			appStruct.pstrFileNameJad = NULL;
		}

		appStruct.pstrFileNameJar = (WCHAR*) filename;
		outResult = CONAInstallApplication(
			hFSHandle,
			CONA_APPLICATION_TYPE_JAVA,
			&appStruct,
			dwOptions,
			(WCHAR*) filepath,
			NULL);
	} else if(fileType == CONA_APPLICATION_TYPE_SIS)
	{
		CONAPI_APPLICATION_SIS appStruct;
		appStruct.pstrFileNameSis = (WCHAR*) filename;
		outResult = CONAInstallApplication(
			hFSHandle,
			CONA_APPLICATION_TYPE_SIS,
			&appStruct,
			dwOptions,
			(WCHAR*) filepath,
			NULL);
	} else
	{
		return FALSE;
	}

	if(jad != NULL)
	{
		env->ReleaseStringChars(jjad, jad);
	}

	env->ReleaseStringChars(jfilename, filename);
	env->ReleaseStringChars(jfilepath, filepath);

	if(outResult == CONA_OK)
	{
		retVal = TRUE;
	}
#ifdef _DEBUG	
	else
	{
		char errorText[100];
		switch(outResult)
		{
		case ECONA_INVALID_HANDLE:
			sprintf(errorText, "ECONA_INVALID_HANDLE");
			break;
		case ECONA_INVALID_PARAMETER:
			sprintf(errorText, "ECONA_INVALID_PARAMETER");
			break;
		case ECONA_CONNECTION_BUSY:
			sprintf(errorText, "ECONA_CONNECTION_BUSY");
			break;
		case ECONA_CONNECTION_LOST:
			sprintf(errorText, "ECONA_CONNECTION_LOST");
			break;
		case ECONA_INVALID_DATA_DEVICE:
			sprintf(errorText, "ECONA_INVALID_DATA_DEVICE");
			break;
		case ECONA_MEMORY_FULL:
			sprintf(errorText, "ECONA_MEMORY_FULL");
			break;
		case ECONA_CURRENT_FOLDER_NOT_FOUND:
			sprintf(errorText, "ECONA_CURRENT_FOLDER_NOT_FOUND");
			break;
		case ECONA_FOLDER_PATH_TOO_LONG:
			sprintf(errorText, "ECONA_FOLDER_PATH_TOO_LONG");
			break;
		case ECONA_FOLDER_NOT_FOUND:
			sprintf(errorText, "ECONA_FOLDER_NOT_FOUND");
			break;
		case ECONA_FILE_TOO_BIG_DEVICE:
			sprintf(errorText, "ECONA_FILE_TOO_BIG_DEVICE");
			break;
		case ECONA_FILE_NAME_INVALID:
			sprintf(errorText, "ECONA_FILE_NAME_INVALID");
			break;
		case ECONA_FILE_NOT_FOUND:
			sprintf(errorText, "ECONA_FILE_NOT_FOUND");
			break;
		case ECONA_FILE_ALREADY_EXIST:
			sprintf(errorText, "ECONA_FILE_ALREADY_EXIST");
			break;
		case ECONA_FILE_NO_PERMISSION:
			sprintf(errorText, "ECONA_FILE_NO_PERMISSION");
			break;
		case ECONA_FILE_NO_PERMISSION_ON_PC:
			sprintf(errorText, "ECONA_FILE_NO_PERMISSION_ON_PC");
			break;
		case ECONA_FILE_BUSY:
			sprintf(errorText, "ECONA_FILE_BUSY");
			break;
		case ECONA_DEVICE_INSTALLER_BUSY:
			sprintf(errorText, "ECONA_DEVICE_INSTALLER_BUSY");
			break;
		case ECONA_CANCELLED:
			sprintf(errorText, "ECONA_CANCELLED");
			break;
		case ECONA_FAILED_TIMEOUT:
			sprintf(errorText, "ECONA_FAILED_TIMEOUT");
			break;
		case ECONA_FILE_TYPE_NOT_SUPPORTED:
			sprintf(errorText, "ECONA_FILE_TYPE_NOT_SUPPORTED");
			break;
		case ECONA_NOT_SUPPORTED_DEVICE:
			sprintf(errorText, "ECONA_NOT_SUPPORTED_DEVICE");
			break;
		case ECONA_UNKNOWN_ERROR_DEVICE:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR_DEVICE");
			break;
		case ECONA_NOT_INITIALIZED:
			sprintf(errorText, "ECONA_NOT_INITIALIZED");
			break;
		case ECONA_NOT_SUPPORTED_MANUFACTURER:
			sprintf(errorText, "ECONA_NOT_SUPPORTED_MANUFACTURER");
			break;
		case ECONA_UNKNOWN_ERROR:
			sprintf(errorText, "ECONA_UNKNOWN_ERROR");
			break;
		default:
			sprintf(errorText, "THIS SHOULD NOT SEEN!!!");
			break;
		}

		printf("CONAInstallApplication error: %s\n", errorText);
		retVal = FALSE;
	}
#endif //_DEBUG

	return retVal;
}


JNIEXPORT jint JNICALL Java_com_nokia_phone_deploy_CONA_native_1getStatus
(JNIEnv* env, jobject obj, jint type)
{
	if(pEventHandler != NULL)
		return (jint) pEventHandler->getState(type);

	return (jint) -1;
}


//-----------------------------------------------------------------------------
// CONAEventHandler definitions
//-----------------------------------------------------------------------------
CONAEventHandler::CONAEventHandler(JNIEnv* jniEnv, jobject obj)
{
	jniEnv->GetJavaVM(&m_pVM);
	m_pEnv = jniEnv;
	m_object = m_pEnv->NewGlobalRef(obj);

	setCurrentFolderStatus = 0;
	createFolderStatus = 0;
	putFileStatus = 0;
	installFileStatus = 0;
}

CONAEventHandler::~CONAEventHandler()
{
    printf("CONA.CONAEventHandler.Dtor: ... 1: m_pEnv=%X\n", (DWORD)m_pEnv);
	m_pEnv->DeleteGlobalRef(m_object);
    printf("CONA.CONAEventHandler.Dtor: ... 2\n");
}

JNIEnv* CONAEventHandler::getJavaEnv()
{
	return m_pEnv;
}

jobject CONAEventHandler::getJavaObject()
{
	return m_object;
}

JavaVM* CONAEventHandler::getJavaVM()
{
	return m_pVM;
}

int CONAEventHandler::getState(int type)
{
	int retVal = 0;

	switch(type)
	{
		case CONASetCurrentFolderNtf:
			retVal = setCurrentFolderStatus;
			break;
		case CONACreateFolderNtf:
			retVal = createFolderStatus;
			break;
		case CONACopyFileNtf:
			retVal = putFileStatus;
			break;
		case CONAInstallApplicationNtf:
			retVal = installFileStatus;
			break;
		default:
			break;
	}

	return retVal;
}

void CONAEventHandler::setState(int state, int type)
{
	switch(type)
	{
		case CONASetCurrentFolderNtf:
			setCurrentFolderStatus = state;
			break;
		case CONACreateFolderNtf:
			createFolderStatus = state;
			break;
		case CONACopyFileNtf:
			putFileStatus = state;
			break;
		case CONAInstallApplicationNtf:
			installFileStatus = state;
			break;
		default:
			break;
	}
}

void CONAEventHandler::connectionLost()
{
	setCurrentFolderStatus = -1;
	createFolderStatus = -1;
	putFileStatus = -1;
	installFileStatus = -1;
}

DWORD CALLBACK DeviceNotifyCallback(DWORD dwStatus, WCHAR* pstrSerialNumber)
{
    printf("CONA.DeviceNotifyCallback: dwStatus=%X, pstrSerialNumber=%s\n", dwStatus, pstrSerialNumber);
	if(pEventHandler != NULL)
	{
		jint status = (jint) dwStatus;
		jint deviceId = (jint) -1;

		JNIEnv* env = NULL;
		pEventHandler->getJavaVM()->AttachCurrentThread((void**)&env, NULL);

		
		jfieldID instanceFieldId = env->GetStaticFieldID(
		env->GetObjectClass(pEventHandler->getJavaObject()),
		"instance",
		"Lcom/nokia/phone/deploy/CONA;");
		jobject instanceObj = env->GetStaticObjectField(env->GetObjectClass(pEventHandler->getJavaObject()), instanceFieldId);
		

		jobject obj = pEventHandler->getJavaObject();

		jmethodID fireDeviceNotify = env->GetMethodID(env->GetObjectClass(obj), "fireDeviceNotify", "(II)V");

		env->CallVoidMethod(
			obj, 
			fireDeviceNotify,
			status,
			deviceId);

		pEventHandler->getJavaVM()->DetachCurrentThread();
	}

	return CONA_OK;
}

DWORD CALLBACK FileOperationNotifyCallback(
	DWORD dwFSFunction,
	DWORD dwState,
	DWORD dwTransferredBytes,
	DWORD dwAllBytes)
{
    printf("CONA.FileOperationNotifyCallback: dwFSFunction=%X, dwState=%X, dwTransferredBytes=%X, dwAllBytes=%X\n", dwFSFunction, dwState, dwTransferredBytes, dwAllBytes);
	if(pEventHandler != NULL)
	{
		jint func = (jint) dwFSFunction;
		jint state = (jint) dwState;
		jint trans = (jint) dwTransferredBytes;
		jint all = (jint) dwTransferredBytes;

		// check if connection failed
		if(dwFSFunction == CONAConnectionLostNtf)
		{
		pEventHandler->connectionLost();
		} else
		{
		pEventHandler->setState(dwState, dwFSFunction);
		}

		JNIEnv* env = NULL;
		pEventHandler->getJavaVM()->AttachCurrentThread((void**)&env, NULL);
		jobject obj = pEventHandler->getJavaObject();
		
		jmethodID fireFileOperationNotify = env->GetMethodID(env->GetObjectClass(obj), "fireFileOperationNotify", "(IIII)V");

		env->CallVoidMethod(
			obj,
			fireFileOperationNotify,
			func,
			state,
			trans,
			all);

		pEventHandler->getJavaVM()->DetachCurrentThread();
	}
	return CONA_OK;
}