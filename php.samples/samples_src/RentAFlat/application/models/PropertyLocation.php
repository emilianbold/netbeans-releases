<?php

class Application_Model_PropertyLocation {

    protected $id;
    protected $city_part;
    protected $city;

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

    public function getCityPart() {
        return $this->city_part;
    }

    public function setCityPart($city_part) {
        $this->city_part = $city_part;
    }

    public function getCity() {
        return $this->city;
    }

    public function setCity($city) {
        $this->city = $city;
    }

}

