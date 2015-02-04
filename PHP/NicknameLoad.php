
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$nickname = $_GET["nickname"];

$result = mysqli_query($conn, "SELECT COUNT(NICKNAME) as cnt FROM ACCOUNT where NICKNAME = '" .$nickname."'");
$result2 = mysqli_query($conn, "INSERT INTO NICKNAME VALUE (".$nickname.") FROM ACCOUNT");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
	
	echo $row[cnt];
	echo "<br>";
	
}


mysqli_close($conn);

?>
