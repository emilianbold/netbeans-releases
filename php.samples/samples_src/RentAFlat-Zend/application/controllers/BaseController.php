<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/**
 * Base Controller is base class that overrides
 * method from zend controller. These methodes are
 * then inheritted in other Controllers and so we
 * don't have to override these methodes in every
 * controller
 *
 * @author Filip Zamboj (fzamboj@netbeans.org)
 * @version 1.0
 *
 * @abstract
 */
abstract class BaseController extends Zend_Controller_Action {

    /**
     *
     * @param string $method
     * @param string $args
     */
    public function __call($method, $args) {
        $this->_redirect("index");
    }

    public function init() {
        $this->view->controller = $this->getRequest()->getControllerName();
        $this->view->action = $this->getRequest()->getActionName();

        if ($this->getRequest()->getParam("removeFromFavorites") != null) {
            Misc_Utils::removeFromFavorites($this->getRequest()->getParam("removeFromFavorites"));
            $this->_redirect("/my-favorites");
            exit;
        } elseif ($this->getRequest()->getParam("addToFavorites") != null) {
            Misc_Utils::addToFavorites($this->getRequest()->getParam("addToFavorites"));
            $this->_redirect("/my-favorites");
            exit;
        }
    }

    public function preDispatch() {
        $this->view->render('index/_menu.phtml');
    }

    public function postDispatch() {
        $this->view->render('index/_footer.phtml');
    }

}

