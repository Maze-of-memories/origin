
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$nickname = $_GET["nickname"];

$result = mysqli_query($conn, "SELECT COUNT(NICKNAME) as cnt FROM ACCOUNT where NICKNAME = '" .$nickname."'");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
	
	echo $row[cnt];
	
}

mysqli_close($conn);

?>
