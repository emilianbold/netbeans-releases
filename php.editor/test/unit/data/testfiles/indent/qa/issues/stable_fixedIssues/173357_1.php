<?php
$query = $this->db->select("*")
        ->from($table)->offset($this->page * $rowsPerPage)^
        ->limit($rowsPerPage);
?>