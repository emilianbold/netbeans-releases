// HttpMonitoringApp.cpp: implementation of the CHttpMonitoringApp class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "HttpMonitoringApp.h"
#include <wininet.h>
#include <atlstr.h>
#include "XMLTag.h"
#include "DbgpResponse.h"
#include <time.h>
#include <cstdlib> // for rand()

STDMETHODIMP CTestSink::BeginningTransaction(
    /* [in] */ LPCWSTR szURL,
    /* [in] */ LPCWSTR szHeaders,
    /* [in] */ DWORD dwReserved,
    /* [out] */ LPWSTR *pszAdditionalHeaders)
{
    USES_CONVERSION;

    if (pszAdditionalHeaders)
    {
        *pszAdditionalHeaders = 0;
    }

    CComPtr<IHttpNegotiate> spHttpNegotiate;
    QueryServiceFromClient(&spHttpNegotiate);
    HRESULT hr = spHttpNegotiate ?
        spHttpNegotiate->BeginningTransaction(szURL, szHeaders,
            dwReserved, pszAdditionalHeaders) :
        S_OK;

    CComPtr<IWinInetHttpInfo> spWinInetHttpInfo;
    HRESULT hrTemp = m_spTargetProtocol->QueryInterface(IID_IWinInetHttpInfo,
        reinterpret_cast<void**>(&spWinInetHttpInfo));
    ATLASSERT(SUCCEEDED(hrTemp));
    DWORD size = 0;
    DWORD flags = 0;
    hrTemp = spWinInetHttpInfo->QueryInfo(
        HTTP_QUERY_RAW_HEADERS_CRLF | HTTP_QUERY_FLAG_REQUEST_HEADERS,
        0, &size, &flags, 0);
    ATLASSERT(SUCCEEDED(hrTemp));
    std::vector<char> vecBuf(size);
    LPSTR pbuf = &vecBuf.front();
    hrTemp = spWinInetHttpInfo->QueryInfo(
        HTTP_QUERY_RAW_HEADERS_CRLF | HTTP_QUERY_FLAG_REQUEST_HEADERS,
        pbuf, &size, &flags, 0);
    ATLASSERT(SUCCEEDED(hrTemp));

    tstring url = (szURL ? W2CT(szURL) : _T("???"));
    tstring request = _T("(Request for ") + url + _T(")\r\n");
    m_redirects = _T("(Response for ") + url + _T(")\r\n");

    request += A2CT(pbuf);
    EnsureCRLF(request);

    if (szHeaders)
    {
        request += W2CT(szHeaders);
        EnsureCRLF(request);
    }

    if (SUCCEEDED(hr) && pszAdditionalHeaders && *pszAdditionalHeaders)
    {
        request += W2CT(*pszAdditionalHeaders);
        EnsureCRLF(request);
    }
    request += _T("\r\n");
//    g_pMainWin->Log(request.c_str());

    // TEST: display the header stuff
    //MessageBox(0,request.c_str(),_T("Netbeans BHO - Request Received"),MB_OK);

    HttpDbgpResponse msg;
    //msg.setTagName(HTTP);
    msg.addChildTagWithValue(_T("type"), _T("request"));
    msg.addChildTagWithValue(_T("url"), url);
    msg.addChildTagWithValue(_T("test"), _T("foo"));
    msg.addChildTagWithValue(_T("id"), 100);


    // HTTP_QUERY_REQUEST_METHOD - Receives the HTTP verb that is
    // being used in the request, typically GET or POST.
    std::vector<char> vecBuf2(size);
    LPSTR pbuf2 = &vecBuf2.front();
    hrTemp = spWinInetHttpInfo->QueryInfo(
        HTTP_QUERY_RAW_HEADERS_CRLF | HTTP_QUERY_FLAG_REQUEST_HEADERS,
        pbuf2, &size, &flags, 0);
    ATLASSERT(SUCCEEDED(hrTemp));
    msg.addChildTagWithValue(_T("method"),A2CT(pbuf2));

    msg.addChildTagWithValue(_T("urlParams"), _T("null"));
    msg.addChildTagWithValue(_T("postText"), _T("undefined"));
    msg.addChildTagWithValue(_T("loadInit"), rand());



    msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());

    //msg.addHeader(_T("Host"), _T("www.example.com"));
    //msg.addHeader(_T("Keep-Alive"), _T("300"));
    // TODO: this could crash if lastInstance isn't valid
    if (DbgpConnection::lastInstance != NULL) {
        DbgpConnection::lastInstance->sendResponse(msg.toString());
    }
    //MessageBox(0,msg.toString().c_str(),_T("Netbeans BHO - Request Received"),MB_OK);

    return hr;
}

std::wstring getJavaTimestamp() {
    // Note on Time conversion:
    // The receiving java code expects the timestamp will be formatted
    // as: *milliseconds* since January 1, 1970, 00:00:00 GMT.
    // But the C-standard library gives the time as:
    // *seconds* elapsed since 00:00 hours, Jan 1, 1970 UTC
    time_t now;
    now = time(NULL);
    __int64 timestamp = ((__int64)now) * 1000; // convert sec to ms

    // convert timestamp to string
    TCHAR buffer[64];
    _i64tot_s(timestamp, buffer, 64, 10); 

    return buffer;

}

STDMETHODIMP CTestSink::OnResponse(
    /* [in] */ DWORD dwResponseCode,
    /* [in] */ LPCWSTR szResponseHeaders,
    /* [in] */ LPCWSTR szRequestHeaders,
    /* [out] */ LPWSTR *pszAdditionalRequestHeaders)
{
    USES_CONVERSION;

    if (pszAdditionalRequestHeaders)
    {
        *pszAdditionalRequestHeaders = 0;
    }

    CComPtr<IHttpNegotiate> spHttpNegotiate;
    QueryServiceFromClient(&spHttpNegotiate);
    HRESULT hr = spHttpNegotiate ?
        spHttpNegotiate->OnResponse(dwResponseCode, szResponseHeaders,
            szRequestHeaders, pszAdditionalRequestHeaders) :
        S_OK;

    tstring response = m_redirects;
    response += W2CT(szResponseHeaders);
    EnsureCRLF(response);
    if (szRequestHeaders)
    {
        response += _T("(Repeat request)\r\n");
        response += W2CT(szRequestHeaders);
        EnsureCRLF(response);
        if (SUCCEEDED(hr) && pszAdditionalRequestHeaders &&
            *pszAdditionalRequestHeaders)
        {
            response += W2CT(*pszAdditionalRequestHeaders);
            EnsureCRLF(response);
        }
    }
    response += _T("\r\n");

    HttpDbgpResponse msg;
    //msg.setTagName(HTTP);
    msg.addChildTagWithValue(_T("type"), _T("response"));
    msg.addChildTagWithValue(_T("url"), _T("http://www.google.com"));
    msg.addChildTagWithValue(_T("test"), _T("foo"));
    msg.addChildTagWithValue(_T("id"), 100);
    msg.addChildTagWithValue(_T("method"),_T("FOO"));//orPOST
    msg.addChildTagWithValue(_T("urlParams"), _T("null"));
    msg.addChildTagWithValue(_T("postText"), _T("undefined"));
    msg.addChildTagWithValue(_T("loadInit"), rand());
    msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());
    //msg.addHeader(_T("Host"), _T("www.google.com"));
    //msg.addHeader(_T("Keep-Alive"), _T("300"));
    // TODO: this could crash if lastInstance isn't valid
    if (DbgpConnection::lastInstance != NULL) {
        DbgpConnection::lastInstance->sendResponse(msg.toString());
    }

//    g_pMainWin->Log(response.c_str());

    // TEST: display the header stuff
//    MessageBox(0,response.c_str(),_T("Netbeans BHO - Response Received"),MB_OK);

    return hr;
}

STDMETHODIMP CTestSink::ReportProgress(
    /* [in] */ ULONG ulStatusCode,
    /* [in] */ LPCWSTR szStatusText)
{
    USES_CONVERSION;

    ATLASSERT(m_spInternetProtocolSink != 0);
    HRESULT hr = m_spInternetProtocolSink ?
        m_spInternetProtocolSink->ReportProgress(ulStatusCode, szStatusText) :
        S_OK;
    if (ulStatusCode == BINDSTATUS_REDIRECTING)
    {
        tstring url = (szStatusText ? W2CT(szStatusText) : _T("???"));
        m_redirects += _T("(Redirected to ") + url + _T(")\r\n");
    }
    return hr;
}

void CTestSink::EnsureCRLF(tstring& str)
{
    tstring::size_type len = str.length();
    if (len >= 4 && str.substr(len - 4) == _T("\r\n\r\n"))
    {
        str.erase(len - 2);
    }
    else if (len < 2 || str.substr(len - 2) != _T("\r\n"))
    {
        str += _T("\r\n");
    }
}
