<?php

namespace Synergy\Controller;

use Synergy\DB\LabelDAO;
use Synergy\Model\Label;

/**
 * Description of LabelCtrl
 *
 * @author lada
 */
class LabelCtrl {

    private $labelDao;

    function __construct() {
        $this->labelDao = new LabelDAO();
    }

    /**
     * Returns labels that matches given string (LIKE label)
     * @param string $label string to search for
     * @return Label[]
     */
    public function findMatchingLabels($label) {
        return $this->labelDao->findMatchingLabels($label);
    }

    /**
     * Returns all labels
     * @param string $label string to search for
     * @return Label[]
     */
    public function getAllLabels() {
        return $this->labelDao->getAllLabels();
    }
}

?>
