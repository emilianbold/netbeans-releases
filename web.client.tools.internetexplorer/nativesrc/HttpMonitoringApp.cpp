// HttpMonitoringApp.cpp: implementation of the CHttpMonitoringApp class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "HttpMonitoringApp.h"
#include <wininet.h>
#include "XMLTag.h"
#include "DbgpResponse.h"
#include <time.h>
#include <cstdlib> // for rand()
#include <atlstr.h>
#include "base64.h"

char* gPostText = NULL;

STDMETHODIMP CTestSink::BeginningTransaction(
    /* [in] */ LPCWSTR szURL,
    /* [in] */ LPCWSTR szHeaders,
    /* [in] */ DWORD dwReserved,
    /* [out] */ LPWSTR *pszAdditionalHeaders)
{
    USES_CONVERSION;

    HttpDbgpResponse msg;
    msg.addChildTagWithValue(_T("type"), _T("request"));

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
        0, &size, &flags, 0); // get buffer size for headers
    ATLASSERT(SUCCEEDED(hrTemp));
    std::vector<char> vecBuf(size); // allocate buffer
    LPSTR pbuf = &vecBuf.front();
    hrTemp = spWinInetHttpInfo->QueryInfo(
        HTTP_QUERY_RAW_HEADERS_CRLF | HTTP_QUERY_FLAG_REQUEST_HEADERS,
        pbuf, &size, &flags, 0); // get actual headers
    ATLASSERT(SUCCEEDED(hrTemp));

    // url - the URL requested
    tstring url = (szURL ? W2CT(szURL) : _T("???"));
    msg.addChildTagWithValue(_T("url"), url);

    // This block for displaying as debug message
    tstring request = _T("(Request for ") + url + _T(")\r\n");
    m_redirects = _T("(Response for ") + url + _T(")\r\n");
    request += A2CT(pbuf);
    EnsureCRLF(request);

    if (szHeaders)
    {
        // This block for displaying as debug message
        //request += L"----szHeaders----\r\n";
        //request += W2CT(szHeaders);
        //EnsureCRLF(request);

        // This block for http monitor functionality
        tstring headerSet = W2CT(szHeaders);
        addAllHeaders(headerSet, msg); //parse headers from szHeaders
    }

    if (SUCCEEDED(hr) && pszAdditionalHeaders && *pszAdditionalHeaders)
    {
        // This block for displaying as debug message
        request += L"----pszAdditionalHeaders----\r\n";
        request += W2CT(*pszAdditionalHeaders);
        EnsureCRLF(request);

        tstring headerSet = W2CT(*pszAdditionalHeaders);
        addAllHeaders(headerSet, msg); //parse headers from pszAdditionalHeaders
    }

    // This block for displaying as debug message
    request += _T("\r\n");
    //MessageBox(0,request.c_str(),_T("Netbeans BHO - Request Received"),MB_OK);


    /*size = 0;
    hrTemp = spWinInetHttpInfo->QueryInfo(HTTP_QUERY_CONTENT_ID,
        0, &size, &flags, 0);
    if (size > 0) {
        // TODO - this doesn't work
	    std::vector<char> vecBuf2(size);
        LPSTR buf = &vecBuf2.front();
        hrTemp = spWinInetHttpInfo->QueryInfo(HTTP_QUERY_CONTENT_ID,
            buf, &size, &flags, 0);
        ATLASSERT(SUCCEEDED(hrTemp));
        MessageBox(0,A2CT(buf),_T("Netbeans BHO - Request Received"),MB_OK);
    }*/

    // id - TODO
    msg.addChildTagWithValue(_T("id"), 123);

    // method - Fill in whether this request was a GET or POST request
    size = 0; //reset buffer size
    hrTemp = spWinInetHttpInfo->QueryInfo(HTTP_QUERY_REQUEST_METHOD,
        0, &size, &flags, 0); //determine size of buffer
    std::vector<char> getOrPostStrBufVector(size); //allocate buffer 
    LPSTR getOrPostStrBuf = &getOrPostStrBufVector.front();
    hrTemp = spWinInetHttpInfo->QueryInfo(HTTP_QUERY_REQUEST_METHOD,
        getOrPostStrBuf, &size, &flags, 0); //fill buffer
    ATLASSERT(SUCCEEDED(hrTemp));
    tstring getOrPostString = A2CT(getOrPostStrBuf);
    msg.addChildTagWithValue(_T("method"), getOrPostString); 


    if ((getOrPostString.find(_T("POST")) != tstring::npos) ||
        (getOrPostString.find(_T("post")) != tstring::npos)) { // post request
        if (gPostText != NULL) {
            // Output in the Netbeans window is tokenized for readability
            // so here we replace "&" with " " (whitespace) in the post 
            // parameters string
            std::string postText2 = gPostText;
            int idx;
            while( (idx=postText2.find_first_of('&')) >= 0 ) {
                postText2.replace( idx, 1, " " );
            }

            msg.addChildTagWithValue(_T("postText"), A2CT(postText2.c_str()));
            delete[] gPostText;
            gPostText = NULL;
        } else {
            msg.addChildTagWithValue(_T("postText"), _T("undefined"));
        }
    }
    else { // get request
        int splitPos = url.find('?');
        if (splitPos != tstring::npos) {
            std::wstring params = url.substr(splitPos + 1);
            int idx;
            while( (idx=params.find_first_of('&')) >= 0 ) {
                params.replace( idx, 1, L" " );
            }
            msg.addChildTagWithValue(_T("urlParams"), params.c_str());

        } else {
            msg.addChildTagWithValue(_T("urlParams"), _T("undefined"));
        }
}
    // loadInit - ??? (or sometimes load_init)
    msg.addChildTagWithValue(_T("loadInit"), rand()); 

    // timestamp - current time when the request was made
    msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());

    if (DbgpConnection::lastInstance != NULL) {
        ScriptDebugger* sdbg = DbgpConnection::lastInstance->getScriptDebugger();
        //if (sdbg!= NULL && sdbg->isHttpMonitorEnabled()) {
            DbgpConnection::lastInstance->sendResponse(msg.toString());
        //} 
    }

    return hr;
}

/**
 * Parses input parameter headerSet for headers and adds them to msg
 * as key-value pairs.
 *
 * headerSet is expected to be of the format used by 
 * HTTP_QUERY_RAW_HEADERS_CRLF, ie: one header per line, with "\r\n"
 * as the line-breaks. Each header is expected to be of the format
 * "header-name: header-value"
 */
void addAllHeaders(const tstring headerSet, HttpDbgpResponse &msg) {
    size_t posLineStart = 0;
    size_t posColon = 0;
    size_t posLineEnd = 0;
    int tempCounter = 0;
//DebugBreak();    
 /*   do {
        posLineEnd = headerSet.find(L"\r\n", posLineStart);
        posColon = headerSet.find(L":", posLineStart);
        
        if (posLineEnd < posColon) {
            // line does not have a colon, not handled          
            continue; 
        }
        
        // substr(start,len)
        tstring headerName = headerSet.substr(posLineStart, posColon-posLineStart);
        tstring headerValue;
        int valStartPos = posColon+2; // skip past the ": " (2 characters) 

        if (posLineEnd == wstring::npos) { //end of string
            headerValue = headerSet.substr(valStartPos); 
        } else { 
            headerValue = headerSet.substr(valStartPos, posLineEnd-valStartPos); 
        }
        msg.addHeader(headerName, headerValue);

        // move to next line, skipping the "\r\n"
        posLineStart = posLineEnd + 2; 
    } while (posLineEnd != wstring::npos);
*/
    posColon = headerSet.find(L":", posLineStart);
    // on response messages the first line is not a header but the HTTP response
    // eg: HTTP/1.1 402 OK -- filter these out. This is kind of a hack, not really
    // filtering on content, but just chopping the first line if its not have a ":" on it
    size_t posFirstLineBreak = headerSet.find(L"\r\n", posLineStart);
    if (posFirstLineBreak < posColon) {
        posLineStart = posFirstLineBreak + 2;
    }

    while (posColon < headerSet.size() && posColon != wstring::npos) {
        posLineEnd = headerSet.find_first_of(L"\r\n",posColon);

        tstring headerName = L"";
        tstring headerValue = L"";
        headerName += headerSet.substr(posLineStart, posColon-posLineStart); // substr(start,len)

        int valStartPos = posColon+2; // skip past the ": " (2 characters) 

        if (posLineEnd >= headerSet.size()) {
            // at end of string
            headerValue += headerSet.substr(valStartPos); //substr(start)
        } else {
            // probably more header(s) follow
            headerValue += headerSet.substr(
                valStartPos, posLineEnd-valStartPos); // substr(start,len)
        }
        msg.addHeader(headerName, headerValue);

        if (posLineEnd == wstring::npos) break;

        posLineStart = posLineEnd + 2; // increment past "\r\n" of previous line
        posColon = headerSet.find(L":",posLineStart);
    }
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

    HttpDbgpResponse msg;
    msg.addChildTagWithValue(_T("type"), _T("response"));

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

    // This block gets the content-type header
    CComPtr<IWinInetHttpInfo> spWinInetHttpInfo;
    HRESULT hrTemp = m_spTargetProtocol->QueryInterface(IID_IWinInetHttpInfo,
        reinterpret_cast<void**>(&spWinInetHttpInfo));
    ATLASSERT(SUCCEEDED(hrTemp));
    DWORD size = 0;
    DWORD flags = 0;
    hrTemp = spWinInetHttpInfo->QueryInfo( HTTP_QUERY_CONTENT_TYPE,
        0, &size, &flags, 0); // get buffer size for header
    ATLASSERT(SUCCEEDED(hrTemp));
    std::vector<char> vecBuf(size); // allocate buffer
    LPSTR contentTypeStr = &vecBuf.front();
    hrTemp = spWinInetHttpInfo->QueryInfo( HTTP_QUERY_CONTENT_TYPE,
        contentTypeStr, &size, &flags, 0); // get actual header
    ATLASSERT(SUCCEEDED(hrTemp));


    // For Debug output message
    tstring response = m_redirects;
    response += L"----szResponseHeaders----\r\n";
    response += W2CT(szResponseHeaders);
    EnsureCRLF(response);
    response += L"----/szResponseHeaders----\r\n";

    tstring headerSet1 = W2CT(szResponseHeaders);
    // First line of headers contains the HTTP status response.
    // Examples: "HTTP/1.0 200 OK" or "HTTP/1.0 404 Not Found"
    // But can also pull out the status code from dwResponseCode directly
    // without requiring any parsing.
    msg.addChildTagWithValue(_T("status"),dwResponseCode); // HTTP response code

    addAllHeaders(headerSet1, msg); //parse & add headers from szResponseHeaders
/*
    if (szRequestHeaders)
    {
        response += _T("(Repeat request)\r\n");
        response += W2CT(szRequestHeaders);
        EnsureCRLF(response);
        if (SUCCEEDED(hr) && pszAdditionalRequestHeaders &&
            *pszAdditionalRequestHeaders)
        {
            response += L"----pszAdditionalRequestHeaders----\r\n";
            response += W2CT(*pszAdditionalRequestHeaders);
            EnsureCRLF(response);
        }
    }
    response += _T("\r\n");*/

    //TODO - response URL
    msg.addChildTagWithValue(_T("url"), _T("http://"));
    
    //TODO - response ID. this should match the ID from the request.
    // a different ID will clear out the history in netbeans of requests
    msg.addChildTagWithValue(_T("id"), 123);

    //TODO - response Name
    msg.addChildTagWithValue(_T("name"),_T("http://"));

    // Content type: comes as a string formatted such as:
    // "Content-Type: text/html; charset=utf-8"
    // We aren't interested in charset here so that part needs to be
    // trimmed off (if present).
    tstring contentType1 =  A2CT(contentTypeStr);
    int semicolonPos = contentType1.find(_T(";"),0);
    if (semicolonPos != std::wstring::npos) {
        contentType1 = contentType1.substr(0,semicolonPos);
    }
    msg.addChildTagWithValue(_T("contentType"), contentType1);
    msg.addChildTagWithValue(_T("mimeType"), contentType1);

    //xhr, html, image, js, etc.
    tstring categoryString = getCategoryForMimeType(contentType1);
    msg.addChildTagWithValue(_T("category"), categoryString);

    // responseText should be base64 encoded. responseText is displayed in the
    // "Response Body" window in the Netbeans HTTP Monitor
    if (categoryString.find(_T("image"), 0) != tstring.npos) { 
        msg.addChildTagWithValue(_T("responseText"), encodeToBase64(_T("IMAGE")));
    //} else if (categoryString.find(_T("xhr"), 0) != tstring.npos) { 
        //show the xml response as the response text
        //msg.addChildTagWithValue(_T("responseText"), encodeToBase64(_T("???")));
    } else {
        msg.addChildTagWithValue(_T("responseText"), encodeToBase64(_T("BINARY")));
    }
    msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());

    if (DbgpConnection::lastInstance != NULL) {
        ScriptDebugger* sdbg = DbgpConnection::lastInstance->getScriptDebugger();
        //if (sdbg!= NULL && sdbg->isHttpMonitorEnabled()) {
            DbgpConnection::lastInstance->sendResponse(msg.toString());
        //} 
    }

    //if (categoryString.find(L"text") != tstring::npos) {
    //MessageBox(0,response.c_str(),_T("Netbeans BHO - Response Received"),MB_OK);
    //}
    return hr;
}

typedef std::map<tstring, tstring> TStrStrMap;
typedef std::pair<tstring, tstring> TStrStrPair;
/**
 * mimeType - string with the mime type
 * Return type - Category (eg: html, image, css, etc)
 */
tstring getCategoryForMimeType( tstring mimeType ) 
{
    TStrStrMap stringMap;
    stringMap.insert(TStrStrPair(L"application/javascript", L"js"));
    stringMap.insert(TStrStrPair(L"application/javascript", L"js"));
    stringMap.insert(TStrStrPair(L"application/json", L"xhr")); //L"json"));
    stringMap.insert(TStrStrPair(L"application/octet-stream", L"bin"));
    stringMap.insert(TStrStrPair(L"application/x-javascript", L"js"));
    stringMap.insert(TStrStrPair(L"application/x-shockwave-flash", L"flash"));
    stringMap.insert(TStrStrPair(L"image/bmp", L"image"));
    stringMap.insert(TStrStrPair(L"image/gif", L"image"));
    stringMap.insert(TStrStrPair(L"image/jpeg", L"image"));
    stringMap.insert(TStrStrPair(L"image/png", L"image"));
    stringMap.insert(TStrStrPair(L"text/css", L"css"));
    stringMap.insert(TStrStrPair(L"text/html", L"html"));
    stringMap.insert(TStrStrPair(L"text/javascript", L"js"));
    stringMap.insert(TStrStrPair(L"text/plain", L"txt"));
    stringMap.insert(TStrStrPair(L"text/xml", L"html"));

	tstring category = stringMap[mimeType];
	if(category != _T("")) {
		return category;
	}
	else {
        return _T("unknown");
    }
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
