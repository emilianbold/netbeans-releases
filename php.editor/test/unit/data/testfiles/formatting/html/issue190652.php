<?php
if ($this->pictures == null):
    echo "Zadne obrazky nebyli nalezeny.";
else:
    if ($this->cover != null):
        ?>
        Hlavny obrazek:
        <img src="images/property_pictures/<?= $this->id ?>thumbs/<?=$this->cover ?>"/>
        <?
    endif;
    foreach ($this->pictures as $picture):
        ?>
        <img src="images/property_pictures/<?= $this->id ?>thumbs/<?=$picture->getImage_path() ?>"/>
        <?
    endforeach;
endif;

?>

