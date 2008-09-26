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
#include "XMLTag.h"

static const tstring WINDOW =           _T("window");
static const tstring WINDOWS =          _T("windows");
static const tstring _SOURCE =          _T("source");
static const tstring SOURCES =          _T("sources");
static const tstring RELOAD_SOURCES =   _T("reloadsources");
static const tstring PROPERTY =         _T("property");

typedef XMLTag DbgpResponseTag;

class DbgpWindowTag : public XMLTag {
public:
    DbgpWindowTag() {
        this->setName(WINDOW);
    }
    
    DbgpWindowTag& addWindowTag() {
        return (DbgpWindowTag&)addChildTag(WINDOW);
    }
};

class DbgpWindowsTag : public XMLTag {
public:
    DbgpWindowsTag() {
        this->setName(WINDOWS);
    }
    
    DbgpWindowTag& addWindowTag() {
        return (DbgpWindowTag&)addChildTag(WINDOW);
    }
};


typedef XMLTag DbgpSourceTag;

class DbgpSourcesTag : public XMLTag {
public:
    DbgpSourceTag& addSourceTag() {
        return (DbgpSourceTag&)addChildTag(_SOURCE);
    }
};

typedef XMLTag DbgpMessageTag;

typedef XMLTag DbgpStackTag;

typedef XMLTag DbgpStreamTag;

class DbgpPropertyTag : public XMLTag {
public:
    DbgpPropertyTag() {
        this->setName(PROPERTY);
    }
    
    DbgpPropertyTag& addPropertyTag() {
        return (DbgpPropertyTag&)addChildTag(PROPERTY);
    }
};
