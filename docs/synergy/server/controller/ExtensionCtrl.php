<?php
namespace Synergy\Controller;

use ReflectionClass;

/**
 * Extensions are focused on classes from model (Specification...). If a controller for such a class is extendable, it has to extend this class. 
 * Every time controller edit/creates/updates/deletes its object, at the end, just before returning object (or at the end of the function), it has to call
 * appropriate method of this class. The only exception is delete() method, which has to be called at the beginning (to avoid further problems with dependencies).
 * 
 *
 * @author vriha
 */
class ExtensionCtrl {

    /**
     * Type of object that given extension handles (e.g. Specification)
     * @var string 
     */
    private $extensionType;

    /**
     *
     * @var ReflectionClass[] 
     */
    private $extensions;
    private $ready;

    function __construct($extensionType) {
        $this->extensionType = $extensionType;
        $this->ready = false;
    }

    /**
     * Calls get() in all registered extensions
     * @param Object|Object[] $data either single instance of Specification, Suite, TestCase, Tribe or array of these instances (all instances in array are from the same class)
     * @return Object|Object[] $data either single instance of Specification, Suite, TestCase, Tribe or array of these instances (all instances in array are from the same class)
     */
    public final function get($data) {

        if (is_null($data) || (is_array($data) && count($data) < 1)) {
            return $data;
        }

        $this->init();
        if (is_array($data)) {
            $limit = count($data);
            for ($i = 0; $i < $limit; $i++) {
                $object = $data[$i];
                foreach ($this->extensions as $extension) {
                    $reflectMethod = $extension->getMethod('get');
                    $instance = $extension->newInstance();
                    $object = $reflectMethod->invoke($instance, $object);
                }
                $data[$i] = $object;
            }
        } else {
            foreach ($this->extensions as $extension) {
                $reflectMethod = $extension->getMethod('get');
                $instance = $extension->newInstance();
                $data = $reflectMethod->invoke($instance, $data);
            }
        }
        return $data;
    }

    /**
     * Calls edit() in all registered extensions
     * @param Object|Object[] $data either single instance of Specification, Suite, TestCase, Tribe or array of these instances (all instances in array are from the same class)
     */
    public final function edit($data) {
        if (is_null($data) || (is_array($data) && count($data) < 1)) {
            return;
        }

        $this->init();
        if (is_array($data)) {
            $limit = count($data);
            for ($i = 0; $i < $limit; $i++) {
                $object = $data[$i];
                foreach ($this->extensions as $extension) {
                    $reflectMethod = $extension->getMethod('edit');
                    $instance = $extension->newInstance();
                    $reflectMethod->invoke($instance, $object);
                }
            }
        } else {
            foreach ($this->extensions as $extension) {
                $reflectMethod = $extension->getMethod('edit');
                $instance = $extension->newInstance();
                $reflectMethod->invoke($instance, $data);
            }
        }
    }

    /**
     * Calls delete() in all registered extensions
     * @param int|int[] $id single object ID or array of IDs
     */
    public final function delete($id) {
        if (is_null($id) || (is_array($id) && count($id) < 1)) {
            return;
        }

        $this->init();
        if (is_array($id)) {
            $limit = count($id);
            for ($i = 0; $i < $limit; $i++) {
                $object = $id[$i];
                foreach ($this->extensions as $extension) {
                    $reflectMethod = $extension->getMethod('delete');
                    $instance = $extension->newInstance();
                    $reflectMethod->invoke($instance, $object);
                }
            }
        } else {
            foreach ($this->extensions as $extension) {
                $reflectMethod = $extension->getMethod('delete');
                $instance = $extension->newInstance();
                $reflectMethod->invoke($instance, $id);
            }
        }
    }

    /**
     * Calls delete() in all registered extensions
     * @param Object|Object[] $data either single instance of Specification, Suite, TestCase, Tribe or array of these instances (all instances in array are from the same class)
     * @param int $newId ID of newly created element
     */
    public final function create($data, $newId) {
        if (is_null($data) || (is_array($data) && count($data) < 1)) {
            return;
        }

        $this->init();
        if (is_array($data)) {
            $limit = count($data);
            for ($i = 0; $i < $limit; $i++) {
                $object = $data[$i];
                foreach ($this->extensions as $extension) {
                    $reflectMethod = $extension->getMethod('create');
                    $instance = $extension->newInstance();
                    $reflectMethod->invoke($instance, $object, $newId);
                }
            }
        } else {
            foreach ($this->extensions as $extension) {
                $reflectMethod = $extension->getMethod('create');
                $instance = $extension->newInstance();
                $reflectMethod->invoke($instance, $data, $newId);
            }
        }
    }

    private function init() {
        if (!$this->ready) {
            $this->requireExtensions();
        }
        $this->ready = true;
    }

    /**
     * Requires all extensions and pushes reflection class to $extension array. 
     */
    private function requireExtensions() {
        $this->extensions = array();
        if (!is_dir(dirname(__FILE__) . '/../extensions/' . $this->extensionType)) {
            return;
        }
        foreach (scandir(dirname(__FILE__) . '/../extensions/' . $this->extensionType) as $filename) {
            $path = dirname(__FILE__) . '/../extensions/' . $this->extensionType . '/' . $filename;
            if (is_file($path)) {
                require_once $path;
                $class = str_replace(".php", "", $filename);
                $reflectionClass = new ReflectionClass('\\Synergy\\Extensions\\'.$this->extensionType.'\\'.$class);
                if ($reflectionClass->implementsInterface('\\Synergy\\Interfaces\\ExtensionInterface')) {
                    array_push($this->extensions, $reflectionClass);
                }
            }
        }
    }

}

?>
