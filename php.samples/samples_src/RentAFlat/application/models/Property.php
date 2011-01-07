<?php

class Application_Model_Property {

    protected $id;
    protected $reference_no;
    protected $title_en;
    protected $text_en;
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

    public function getCoverObject() {
        $pictures = new Application_Model_PropertyPicturesMapper();
        $results = $pictures->fetchAll("cover = 1 and property_id = " . $this->getId());
        foreach ($results as $result) {
            return $result;
        }
    }

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

    
    public function getTitle_en() {
        return $this->title_en;
    }

    public function setTitle_en($title_en) {
        $this->title_en = $title_en;
    }

    
    public function getText_en() {
        return $this->text_en;
    }

    public function setText_en($text_en) {
        $this->text_en = $text_en;
    }

    
    public function getDisposition_id() {
        return $this->disposition_id;
    }

    public function setDisposition_id($disposition_id) {
        $this->disposition_id = $disposition_id;
    }

    public function getArea() 
    {
        return str_replace(".0", "", $this->area);
    }
	
	public function getAreaFormatted() {
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
        $location = new Application_Model_PropertyLocationMapper();
        return $location->fetchAll('id = ' . $this->getLocation_id());
    }

    
    public function getDisposition() {
        $disposition = new Application_Model_DispositionMapper();
        foreach ($disposition->fetchAll('id = ' . $this->getDisposition_id()) as $disposition_type) {
            return $disposition_type->getText_en();
        }
    }
 
    public function getTextLocation() {
        foreach ($this->getLocation() as $location) {
            return $location;
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

