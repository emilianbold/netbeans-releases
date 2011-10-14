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

class Application_Model_Property {

    protected $id;
    protected $reference_no;
    protected $title;
    protected $text;
    protected $disposition_id;
    protected $area;
    protected $floor;
    protected $lift;
    protected $cellar;
    protected $balcony;
    protected $location_id;
    protected $price;
    protected $created_on;
    protected $street;
    protected $property_build_id;
    protected $terace;
    protected $loggia;
    protected $garden;
    protected $garage;
    protected $parking_place;

    public function __construct(array $options = null) {
        if (is_array($options)) {
            $this->setOptions($options);
        }
    }

    public function __set($name, $value) {
        $method = 'set' . $name;
        if (('mapper' == $name) || !method_exists($this, $method)) {
            throw new Exception('Invalid content property');
        }
        $this->$method($value);
    }

    public function __get($name) {
        $method = 'get' . $name;
        if (('mapper' == $name) || !method_exists($this, $method)) {
            throw new Exception('Invalid content property');
        }
        return $this->$method();
    }

    public function setOptions(array $options) {
        $methods = get_class_methods($this);
        foreach ($options as $key => $value) {
            $method = 'set' . ucfirst($key);
            if (in_array($method, $methods)) {
                $this->$method($value);
            }
        }
        return $this;
    }

    public function getId() {
        return $this->id;
    }

    public function setId($id) {
        $this->id = $id;
    }

    public function getReference_no() {
        return $this->reference_no;
    }

    public function setReference_no($reference_no) {
        $this->reference_no = $reference_no;
    }


    public function getTitle() {
        return $this->title;
    }

    public function setTitle($title) {
        $this->title = $title;
    }


    public function getText() {
        return $this->text;
    }

    public function setText($text) {
        $this->text = $text;
    }


    public function getDisposition_id() {
        return $this->disposition_id;
    }

    public function setDisposition_id($disposition_id) {
        $this->disposition_id = $disposition_id;
    }

    public function getArea() {
        return str_replace(".0", "", $this->area);
    }

	public function getFormattedArea() {
		return number_format($this->area, 0, ".", " ");
    }

    public function setArea($area) {
        $this->area = str_replace(",", ".", $area);
    }

    public function getFloor() {
        return $this->floor;
    }

    public function setFloor($floor) {
        $this->floor = $floor;
    }

    public function getLift() {
        return $this->lift;
    }

    public function setLift($lift) {
        $this->lift = $lift;
    }

    public function getCellar() {
        return str_replace('.0', '', $this->cellar);
    }

    public function setCellar($cellar) {
        $this->cellar = str_replace(",", ".",$cellar);
    }

    public function getBalcony() {
        return str_replace('.0', '', $this->balcony);
    }

    public function setBalcony($balcony) {
        $this->balcony = str_replace(",", ".",$balcony);
    }

    public function getLocation_id() {
        return $this->location_id;
    }

    public function setLocation_id($location_id) {
        $this->location_id = $location_id;
    }

    public function getPrice() {
        return round($this->price);
    }

    public function setPrice($price) {
        $this->price = $price;
    }

    public function getCreated_on() {
        return $this->created_on;
    }

    public function setCreated_on($created_on) {
        $this->created_on = $created_on;
    }

    public function getLocation() {
        $model = new Application_Model_PropertyLocationMapper();
        $locations = $model->fetchAll('id = ' . $this->getLocation_id());
        return $locations[0];
    }

    public function getDisposition() {
        $disposition = new Application_Model_DispositionMapper();
        foreach ($disposition->fetchAll('id = ' . $this->getDisposition_id()) as $disposition_type) {
            return $disposition_type->getText();
        }
    }

    public function getStreet() {
        return $this->street;
    }

    public function setStreet($street) {
        $this->street = $street;
    }

    public function getFormattedPrice() {
        return number_format($this->getPrice(), 0, ".", " ");
    }

    public function getProperty_build_id() {
        return $this->property_build_id;
    }

    public function setProperty_build_id($property_build_id) {
        $this->property_build_id = $property_build_id;
    }

    public function getProperty_build_type() {
        $m = new Application_Model_PropertyBuildTypeMapper();
        $res = $m->fetch($this->property_build_id);
        return $res;

    }

    public function getTerace() {
        return str_replace('.0', '', $this->terace);
    }

    public function setTerace($terace) {
        $this->terace = str_replace(",", ".",$terace);
    }

    public function getLoggia() {
        return str_replace('.0', '', $this->loggia);
    }

    public function setLoggia($loggia) {
        $this->loggia = str_replace(",", ".",$loggia);
    }

    public function getGarden() {
        return str_replace('.0', '', $this->garden);
    }

    public function setGarden($garden) {
        $this->garden = str_replace(",", ".",$garden);
    }

    public function getGarage() {
        return str_replace('.0', '', $this->garage);
    }

    public function setGarage($garage) {
        $this->garage = str_replace(",", ".",$garage);
    }

    public function getParking_place() {
        return $this->parking_place;
    }

    public function setParking_place($parking_place) {
        $this->parking_place = $parking_place;
    }

}

