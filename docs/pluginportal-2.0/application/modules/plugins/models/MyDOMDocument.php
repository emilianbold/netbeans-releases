<?php
    class MyDOMDocument {
        private $_delegate;
        private $_validationErrors;

        public function __construct (DOMDocument $pDocument) {
            $this->_delegate = $pDocument;
            $this->_validationErrors = array();
        }

        public function __call ($pMethodName, $pArgs) {
            if ($pMethodName == "validate") {
                $eh = set_error_handler(array($this, "onValidateError"));
                $rv = $this->_delegate->validate();
                if ($eh) {
                    set_error_handler($eh);
                }
                return $rv;
            }
            else {
                return call_user_func_array(array($this->_delegate, $pMethodName), $pArgs);
            }
        }
        public function __get ($pMemberName) {
            if ($pMemberName == "errors") {
                return $this->_validationErrors;
            }
            else {
                return $this->_delegate->$pMemberName;
            }
        }
        public function __set ($pMemberName, $pValue) {
            $this->_delegate->$pMemberName = $pValue;
        }
        public function onValidateError ($pNo, $pString, $pFile = null, $pLine = null, $pContext = null) {
            $this->_validationErrors[] = preg_replace("/^.+: */", "", $pString);
        }
    }
?>