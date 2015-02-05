
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$nickname = $_GET["nickname"];

$result = mysqli_query($conn, "SELECT COUNT(NICKNAME) as cnt FROM ACCOUNT where NICKNAME = '" .$nickname."'");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){

	if($row[cnt] = 1){
		echo "false";
	}else if($row[cnt] = 0){
		mysqli_query($conn, "INSERT INTO ACCOUNT (NICKNAME) VALUES ('".$nickname."')");
		echo "success";
	}
}



mysqli_close($conn);

?>
