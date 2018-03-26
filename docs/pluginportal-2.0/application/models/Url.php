<?php

/**
 * File provides easy way to manipulate url parameters
 * @author Alexander Podgorny
 */
class Url {

  /**
   * Splits url into array of it's pieces as follows:
   * [scheme]://[user]:[pass]@[host]/[path]?[query]#[fragment]
   * In addition it adds 'query_params' key which contains array of
   * url-decoded key-value pairs
   *
   * @param String $sUrl Url
   * @return Array Parsed url pieces
   */
  public static function explode($sUrl) {
    $aUrl = parse_url($sUrl);
    $aUrl['query_params'] = array();
    $aPairs = explode('&', $aUrl['query']);
    foreach ($aPairs as $sPair) {
      if (trim($sPair) == '') {
        continue;
      }
      list($sKey, $sValue) = explode('=', $sPair);
      $aUrl['query_params'][$sKey] = urldecode($sValue);
    }
    return $aUrl;
  }

  /**
   * Compiles url out of array of it's pieces (returned by explodeUrl)
   * 'query' is ignored if 'query_params' is present
   *
   * @param Array $aUrl Array of url pieces
   */
  public static function implode($aUrl) {
    //[scheme]://[user]:[pass]@[host]/[path]?[query]#[fragment]

    $sQuery = '';

    // Compile query
    if (isset($aUrl['query_params']) && is_array($aUrl['query_params'])) {
      $aPairs = array();
      foreach ($aUrl['query_params'] as $sKey => $sValue) {
        $aPairs[] = $sKey . '=' . urlencode($sValue);
      }
      $sQuery = implode('&', $aPairs);
    } else {
      $sQuery = $aUrl['query'];
    }

    // Compile url
    $sUrl =
            $aUrl['scheme'] . '://' . (
            isset($aUrl['user']) && $aUrl['user'] != '' && isset($aUrl['pass']) ? $aUrl['user'] . ':' . $aUrl['pass'] . '@' : ''
            ) .
            $aUrl['host'] . (
            isset($aUrl['path']) && $aUrl['path'] != '' ? $aUrl['path'] : ''
            ) . (
            $sQuery != '' ? '?' . $sQuery : ''
            ) . (
            isset($aUrl['fragment']) && $aUrl['fragment'] != '' ? '#' . $aUrl['fragment'] : ''
            );
    return $sUrl;
  }

  /**
   * Parses url and returns array of key-value pairs of url params
   *
   * @param String $sUrl
   * @return Array
   */
  public static function getParams($sUrl) {
    $aUrl = self::explode($sUrl);
    return $aUrl['query_params'];
  }

  /**
   * Removes existing url params and sets them to those specified in $aParams
   *
   * @param String $sUrl Url
   * @param Array $aParams Array of Key-Value pairs to set url params to
   * @return  String Newly compiled url
   */
  public static function setParams($sUrl, $aParams) {
    $aUrl = self::explode($sUrl);
    $aUrl['query'] = '';
    $aUrl['query_params'] = $aParams;
    return self::implode($aUrl);
  }

  /**
   * Updates values of existing url params and/or adds (if not set) those specified in $aParams
   *
   * @param String $sUrl Url
   * @param Array $aParams Array of Key-Value pairs to set url params to
   * @return  String Newly compiled url
   */
  public static function updateParams($sUrl, $aParams) {
    $aUrl = self::explode($sUrl);
    $aUrl['query'] = '';
    $aUrl['query_params'] = array_merge($aUrl['query_params'], $aParams);
    return self::implode($aUrl);
  }

}
