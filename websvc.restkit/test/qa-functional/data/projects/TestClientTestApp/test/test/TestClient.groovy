/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package test

import org.tellurium.dsl.DslContext

class TestClient extends DslContext {
    void defineUi() {
        ui.Container(uid: "links", locator: "//body") {
            // ------------------------------------------------- navigation tree

            // CustomerDB
            // /html/body/div/div[2]/table/tbody/tr/td/span/div/a
            UrlLink(uid: "customersdb", locator: "//a[text() = \"CustomerDB\"]")
            // customers
            // /html/body/div/div[2]/table/tbody/tr/td/span[2]/span/div/a
            UrlLink(uid: "customers", locator: "//a[text() = \"customers\"]")
            // expander for customers
            // //*[@id="I1/customers/_1"]
            Image(uid: "customersExpander", locator: "//*[@id=\"I1/customers/_1\"]")
            // {customerId}
            // /html/body/div/div[2]/table/tbody/tr/td/span[2]/span[2]/span/div/a
            UrlLink(uid: "customerId", locator: "//td/span/span/span/div/a[text() = \"{customerId}\"]")

            // discountCodes
            // /html/body/div/div[2]/table/tbody/tr/td/span[2]/span[3]/div/a
            UrlLink(uid: "discountCodes", locator: "//a[text() = \"discountCodes\"]")
            // expander for discountCodes
            // //*[@id="I1/discountCodes/_4"]
            Image(uid: "dcodesExpander", locator: "//*[@id=\"I1/discountCodes/_4\"]")
            // {discountCode}
            // /html/body/div/div[2]/table/tbody/tr/td/span[2]/span[4]/span/div/a
            UrlLink(uid: "discountCodeId", locator: "//td/span/span/span/div/a[text() = \"{discountCode}\"]")


            // ------------------------------------------------------- test form

            // add param button
            // /html/body/div/div[2]/table/tbody/tr/td[3]/div[3]/table/tbody/tr/td[6]/span/a
            UrlLink(uid: "addParam", locator: "/div/div[2]/table/tbody/tr/td[3]/div[3]/table/tbody/tr/td[6]/span/a")

            // test button
            UrlLink(uid: "test", clocator: [onclick: "ts.testResource()"])

            // //*[@id="methodSel"]
            Selector(uid: "method", locator: "//*[@id=\"methodSel\"]")

            // //*[@id="mimeSel"]
            Selector(uid: "mime", locator: "//*[@id=\"mimeSel\"]")

            // test form:
            InputBox(uid: "start", clocator: [name: "start"])
            InputBox(uid: "max", clocator: [name: "max"])
            InputBox(uid: "expandLevel", clocator: [name: "expandLevel"])
            InputBox(uid: "query", clocator: [name: "query"])
            TextBox(uid: "content", clocator: [tag: "textarea", name: "params", id: "blobParam"])

            InputBox(uid: "resourceId", clocator: [id: "tparams"])

            // --------------------------------------------------- results table

            // tab view
            // //*[@id="tabtable"]
            UrlLink(uid: "tableView", clocator: [id: "tabtable"])

            // tab view content
            // //*[@id="tableContent"]
            // XXX - can contain a table....
            Div(uid: "tableContent", clocator: [id: "tableContent"])

            // raw view
            // //*[@id="tabraw"]
            UrlLink(uid: "rawView", clocator: [id: "tabraw"])

            // raw view content
            // //*[@id="rawContent"]
            Div(uid: "rawContent", clocator: [id: "rawContent"])

            // sub-resources
            // //*[@id="tabstructure"]
            UrlLink(uid: "subresourcesView", clocator: [id: "tabstructure"])

            // sub-resources content
            // //*[@id="structureInfo"]
            Div(uid: "subresourcesContent", clocator: [id: "structureInfo"])

            // monitor view
            // //*[@id="tabmonitor"]
            UrlLink(uid: "monitorView", clocator: [id: "tabmonitor"])

            //monitor view content
            // //*[@id="monitorContent"]
            Div(uid: "monitorContent", clocator: [id: "monitorContent"])

        }
    }

    def clickOn(String node) {
        switch (node) {
            case "customers" :
            click "links.customers"
            break
            case "customerId" :
            click "links.customerId"
            break
            case "dCodes" :
            click "links.discountCodes"
            break
            case "dCodeId" :
            click "links.discountCodeId"
            break
            default:
            throw new UnsupportedOperationException("$node not implemented")
        }
        pause 1000
    }

    def expand(String node) {
        switch (node) {
            case "customers" :
            click "links.customersExpander"
            break
            case "dCodes" :
            click "links.dcodesExpander"
            break
            default:
            throw new UnsupportedOperationException("$node not implemented")
        }
        pause 1000
    }

    void doTest() {
        click "links.test"
        pause 1500
    }

    String[] getAvailableRMethods() {
        return getSelectOptions("links.method")
    }

    String[] getAvailableMIMETypes() {
        return getSelectOptions("links.mime")
    }

    String getSelectedRMethod() {
        getSelectedLabel("links.method")
    }

    String getSelectedMIMEType() {
        getSelectedLabel "links.mime"
    }

    void setSelectedRMethod(String method) {
        selectByLabel("links.method", method)
        pause 1000
    }

    void setSelectedMIMEType(String mime) {
        selectByLabel "links.mime", mime
        pause 1000
    }

    def setView(String type) {
        switch (node) {
            case "raw" :
            click "links.customers"
            break
            case "monitor" :
            click "links.customerId"
            break
            case "table" :
            click "links.discountCodes"
            break
            case "subResources" :
            click "links.discountCodeId"
            break
            default:
            throw new UnsupportedOperationException("$node not implemented")
        }
        pause 1000
    }

    String getContentFromView(String viewId) {
        //XXX - prints ERROR to the log if @name is not present at all
        //      how can we avoid that?
        if (getAttribute("links.${viewId}View", "name") != "selectedTabAnchor") {
            click "links.${viewId}View"
            pause 1000
        }
        return getText("links.${viewId}Content")
    }

    void setTestArg(String name, String value) {
        if (!"resourceId".equals(value)) {
            clearText "links.$name"
        }
        type "links.$name", value
    }

    String getTestArg(String name) {
        getText "links.$name"
    }

}

