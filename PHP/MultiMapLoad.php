
<?php

include "DBConnect.php";

/*
if(mysqli_connect_errno($conn)){
	echo "fail";
}else{
	echo "sucess";
}
*/

// 랜덤 수 생성
$number = mt_rand(1, 1000000);

$query = "SELECT MAP_INFO FROM MULTI_MAP_5 WHERE NUMBER = $number";

$result = mysqli_query($conn, $query);

$row = mysqli_fetch_assoc($result);

echo "$row[MAP_INFO]";
	
mysqli_close($conn);

?>
