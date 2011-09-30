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

/*
 * his is an Itinerary helper class. Instance of
 * his class can store flight information. The
 * lass derives Guest properties from the Guest
 * ase class.
 */

include("guest.php");

class GuestItinerary extends Guest {

    private $FID;
    private $fName;
    private $SID;
    private $source;
    private $dest;
    private $travelDate;

    function __constructor($FID, $SID, $source, $dest, $travelDate) {
        $this->FID = $FID;
        $this->SID = $SID;
        $this->source = $source;
        $this->dest = $dest;
        $this->travelDate = $travelDate;
    }

    function set_FID($FID) {
        $this->FID = $FID;
    }

    function set_FName($fName) {
        $this->fName = $fName;
    }

    function set_SID($SID) {
        $this->SID = $SID;
    }

    function set_source($source) {
        $this->source = $source;
    }

    function set_dest($dest) {
        $this->dest = $dest;
    }

    function set_travelDate($travelDate) {
        $this->travelDate = $travelDate;
    }

    function get_FID() {
        return $this->FID;
    }

    function get_SID() {
        return $this->SID;
    }

    function get_source() {
        return $this->source;
    }

    function get_dest() {
        return $this->dest;
    }

    function get_travelDate() {
        return $this->travelDate;
    }

    function get_FName() {
        return $this->fName;
    }

}
?>