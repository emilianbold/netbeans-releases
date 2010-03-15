<?php
require_once dirname(__FILE__).'/../bootstrap/unit.php';

$t = new lime_test(9, new lime_output_color());

$t->comment('::slugify()');
$t->is(Jobeet::slugify('Sensio'), 'sensio', '::slugify() converts all characters to lower case');
$t->is(Jobeet::slugify('sensio labs'), 'sensio-labs', '::slugify() replaces a white space by a -');
$t->is(Jobeet::slugify('sensio   labs'), 'sensio-labs', '::slugify() replaces several white spaces by a single -');
$t->is(Jobeet::slugify('  sensio'), 'sensio', '::slugify() removes - at the beginning of a string');
$t->is(Jobeet::slugify('sensio  '), 'sensio', '::slugify() removes - at the end of a string');
$t->is(Jobeet::slugify('paris,france'), 'paris-france', '::slugify() replaces non-ASCII characters by a -');
$t->is(Jobeet::slugify(''), 'n-a', '::slugify() converts the empty string to n-a');
$t->is(Jobeet::slugify(' - '), 'n-a', '::slugify() converts a string that only contains non-ASCII characters to n-a');
if (function_exists('iconv'))
{
  $t->is(Jobeet::slugify('Développeur Web'), 'developpeur-web', '::slugify() removes accents');
}
else
{
  $t->skip('::slugify() removes accents - iconv not installed');
}