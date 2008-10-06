/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *      jdeva <deva@neteans.org>
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
#pragma once
#include <map>
#include <string>
#include "DbgpConnection.h"
#include "XMLTag.h"
#include "DbgpTags.h"

using namespace std;

static const tstring COMMAND =          _T("command");
static const tstring RESPONSE =         _T("response");
static const tstring TRANSACTION_ID =   _T("transaction_id");
static const tstring FEATURE =          _T("feature");
static const tstring SUCCESS =          _T("success");
static const tstring STATUS =           _T("status");
static const tstring FILE_URI =         _T("fileuri");
static const tstring SUPPORTED =        _T("supported");
static const tstring STREAM =           _T("stream");
static const tstring TYPE =             _T("type");
static const tstring STD_ERR =          _T("stderr");
static const tstring REASON =           _T("reason");
static const tstring BREAKPOINT =       _T("breakpoint");
static const tstring FILE_NAME =        _T("filename");
static const tstring LINE_NO =          _T("lineno");
static const tstring ID =               _T("id");
static const tstring ENCODING =         _T("encoding");
static const tstring BASE64 =           _T("base64");

class DbgpResponse {
public:
    virtual tstring toString()=0;
    virtual void addAttribute(tstring name, tstring value)=0;
    virtual void addAttribute(tstring name, int value)=0;
};

class StandardDbgpResponse : public DbgpResponse {
public:
    StandardDbgpResponse(tstring commandName, tstring transactionID){
        tag.setName(RESPONSE);
        tag.addAttribute(COMMAND, commandName);
        tag.addAttribute(TRANSACTION_ID, transactionID);
    }

    StandardDbgpResponse(tstring commandName){
        tag.setName(RESPONSE);
        tag.addAttribute(COMMAND, commandName);
    }

    StandardDbgpResponse(){
        tag.setName(RESPONSE);
    }

    void setTagName(tstring tag){
        this->tag = tag;
    }

    void setValue(tstring value){
        tag.setValue(value);
    }

    void addAttribute(tstring name, tstring value) {
        tag.addAttribute(name, value);
    }

    void addAttribute(tstring name, int value) {
        tag.addAttribute(name, value);
    }

    tstring toString(){
        return tag.toString();
    }

protected:
    DbgpResponseTag tag;
};

typedef StandardDbgpResponse DbgpMessage;

class DbgpWindowsMessage {
public:
    DbgpWindowTag &addWindow() {
        return tag.addWindowTag();
    }

    void addAttribute(tstring name, tstring value) {
        tag.addAttribute(name, value);
    }

    tstring toString(){
        return tag.toString();
    }

private:
    DbgpWindowsTag tag;
};

class DbgpSourcesMessage {
public:
    DbgpSourcesMessage(tstring name) {
        tag.setName(name);
    }

    DbgpSourceTag &addSource() {
        return tag.addSourceTag();
    }

    void addAttribute(tstring name, tstring value) {
        tag.addAttribute(name, value);
    }

    tstring toString(){
        return tag.toString();
    }

private:
    DbgpSourcesTag tag;
};

class DbgpBreakpointMessage : public StandardDbgpResponse {
public:
    DbgpBreakpointMessage() {
        tag.addAttribute(COMMAND, STATUS);
    }
    DbgpMessageTag &addMessage() {
        return tag.addChildTag(_T("message"));
    }
};

class StackGetResponse : public StandardDbgpResponse {
public:
    StackGetResponse(tstring commandName, tstring transactionID){
        tag.setName(RESPONSE);
        tag.addAttribute(COMMAND, commandName);
        tag.addAttribute(TRANSACTION_ID, transactionID);
    }
    DbgpStackTag &addStack() {
        return tag.addChildTag(_T("stack"));
    }
};

class PropertyGetResponse : public DbgpResponse {
public:
    PropertyGetResponse(tstring commandName, tstring transactionID){
        tag.setName(RESPONSE);
        tag.addAttribute(COMMAND, commandName);
        tag.addAttribute(TRANSACTION_ID, transactionID);
    }

    DbgpPropertyTag &addProperty() {
        return tag.addPropertyTag();
    }

    void addAttribute(tstring name, tstring value) {
        tag.addAttribute(name, value);
    }

    void addAttribute(tstring name, int value) {
        tag.addAttribute(name, value);
    }

    tstring toString(){
        return tag.toString();
    }
private:
    DbgpPropertyTag tag;
};

class DbgpStreamMessage {
public:
    DbgpStreamMessage() {
        tag.setName(STREAM);
    }
    tstring toString(){
        return tag.toString();
    }
    void addAttribute(tstring name, tstring value) {
        tag.addAttribute(name, value);
    }
    void setValue(tstring value){
        tag.setValue(value);
    }
private:
    DbgpStreamTag tag;
};