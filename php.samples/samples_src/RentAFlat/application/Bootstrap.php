<?php
/*
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

class Bootstrap extends Zend_Application_Bootstrap_Bootstrap {

    protected function _initDoctype() {

        $this->bootstrap('view');
        $view = $this->getResource('view');
        $view->doctype('XHTML1_STRICT');
    }

    protected function _initMenu() {
        $this->bootstrap('view');
        $view = $this->getResource('view');

        $view->placeholder('menu')
                ->setPrefix("<div id=\"menu\">")
                ->setPostfix("</div>");
    }

    protected function _initfFooter() {
        $this->bootstrap('view');
        $view = $this->getResource('view');

        $view->placeholder('footer')
                ->setPrefix("<div id=\"footbox\"><div id=\"foot\">")
                ->setPostfix("<div class=\"cleaner\"></div></div></div>");
    }

    

    public function toText($array) {
        foreach ($array as $object) {
            if ($object->getText_cz() == 1) {
                return "Ano";
            } else
                return "Ne";
        }
    }

}

class General {
    
    public static function getPerex($text) {
        if (strlen($text) > 200) {
            $firstPor = substr($text, 0, 200);
            $space = strrpos($firstPor, " ");

            return substr($firstPor, 0, $space) . " ...";
        } return $text;
    }

    public static function toText($value) {

        if ($value == 1) {
            return "Ano";
        } else
            return "Ne";
    }

    public static function addToCookie($id) {
        $session = new Zend_Session_Namespace('favorites');
        $session->setExpirationSeconds(2592000);
        if (is_numeric($id)) {
            //check if id is not in already 
            for ($i = 0; $i < count($session->favorites); $i++) {
                if ($session->favorites[$i] == $id) {
                    return;
                }
            }
            $session->favorites[] = $id;
        } return;
    }

    public static function isInFavorites($id) {

        if (is_numeric($id) && $id != null) {
            $session = new Zend_Session_Namespace('favorites');
            for ($i = 0; $i < count($session->favorites); $i++) {
                if ($session->favorites[$i] == $id) {
                    return true;
                }
            }
        }
        return false;
    }

    public static function removeFromCookie($id) {
        if (General::isInFavorites($id)) {
            $temp = array();
            $session = new Zend_Session_Namespace('favorites');
            for ($i = 0; $i < count($session->favorites); $i++) {
                if ($session->favorites[$i] != $id) {
                    $temp[] = $session->favorites[$i];
                }
            }
            $session->favorites = $temp;
        }
    }

}


