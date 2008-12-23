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


    size = 0;
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
    }

    // id - TODO
    msg.addChildTagWithValue(_T("id"), 100);

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

    // urlParams
    msg.addChildTagWithValue(_T("urlParams"), _T("null"));

    if (getOrPostString.find(_T("POST")) != tstring::npos) {
        // todo - should this be case-insensitive?
        // post request
        msg.addChildTagWithValue(_T("postText"), _T("undefined"));
    }
    else {
        // postText - only applicable for POST requests, undefined otherwise
        msg.addChildTagWithValue(_T("postText"), _T("undefined"));
    }
    // loadInit - ???
    msg.addChildTagWithValue(_T("loadInit"), rand()); 

    // timestamp - current time when the request was made
    msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());

    // TODO: this could crash if lastInstance isn't valid
    if (DbgpConnection::lastInstance != NULL) {
        DbgpConnection::lastInstance->sendResponse(msg.toString());
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
    // First line contains the HTTP status response.
    // Examples: "HTTP/1.0 200 OK" or "HTTP/1.0 404 Not Found"
    // to get the status code we will get the first numeric token after the first
    // space
    int whitespaceIndex = headerSet1.find(L" ",0);
    int statusStart = headerSet1.find_first_of(L"0123456789", whitespaceIndex);
    int statusEnd = headerSet1.find(L" ",statusStart);
    tstring httpStatus = headerSet1.substr(statusStart, statusEnd-statusStart);
    msg.addChildTagWithValue(_T("status"),httpStatus);

    addAllHeaders(headerSet1, msg); //parse headers from szResponseHeaders

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
    response += _T("\r\n");


    msg.addChildTagWithValue(_T("url"), _T("http://www.example.com"));
    
    msg.addChildTagWithValue(_T("id"), 100);

    msg.addChildTagWithValue(_T("name"),_T("FOO"));

    // Content type: comes as a string formatted such as:
    // "Content-Type: text/html; charset=utf-8"
    // We aren't interested (primarily) in charset so that part needs to be
    // trimmed off if present.
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

    msg.addChildTagWithValue(_T("responseText"),_T("FOO"));

    //msg.addChildTagWithValue(_T("urlParams"), _T("null"));
    //msg.addChildTagWithValue(_T("postText"), _T("undefined"));
    //msg.addChildTagWithValue(_T("loadInit"), rand());
    msg.addChildTagWithValue(_T("timestamp"), getJavaTimestamp());
    //msg.addHeader(_T("Host"), _T("www.google.com"));
    //msg.addHeader(_T("Keep-Alive"), _T("300"));
    // TODO: this could crash if lastInstance isn't valid
    if (DbgpConnection::lastInstance != NULL) {
        DbgpConnection::lastInstance->sendResponse(msg.toString());
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
