
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");
/*
if(mysqli_connect_errno($conn)){
	echo "fail";
}else{
	echo "sucess";
}
*/

$result = mysqli_query($conn, "SELECT * FROM SINGLE_MAP_5");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
	
	echo "number";
	echo $row[NUMBER];
	echo $row[MAP_INFO];
	echo "<br>";
	
}

mysqli_close($conn);

?>
