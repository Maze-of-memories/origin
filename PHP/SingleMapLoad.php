
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$result = mysqli_query($conn, "SELECT Count(NUMBER) as cnt FROM SINGLE_MAP_5");
$result2 = mysqli_query($conn, "SELECT * FROM SINGLE_MAP_5");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
	
	echo $row[cnt];
	echo "<br>";
	
}
while($row = mysqli_fetch_array($result2, MYSQLI_ASSOC)){
	
	echo " ";
	echo $row[NUMBER];
	echo " ";
	echo $row[MAP_INFO];
	echo "<br>";
}

mysqli_close($conn);

?>
