/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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

 *****************************************************************************/
/*
 * Contributor(s): Robert Greig.
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.request;

/**
 * The questionable request. Tell the server which directory to use.
 * 
 */
public final class QuestionableRequest extends Request {

    /**
     * name of file that is questionable
     */
    private String questionFile;

    /**
     * Create a new QuestionableRequest
     * @param fileName name of the file that is questionable.
     */
    public QuestionableRequest(String questionFile) {
        this.questionFile = questionFile;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (questionFile == null) {
            throw new UnconfiguredRequestException(
                    "Questionable request has not been configured");
        }

        return "Questionable " + questionFile + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }
}
