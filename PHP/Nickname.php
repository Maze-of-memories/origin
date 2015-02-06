
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$nickname = $_GET["nickname"];
$email = $_GET["email"];

$result = mysqli_query($conn, "SELECT COUNT(NICKNAME) as cnt FROM ACCOUNT where NICKNAME = '".$nickname."'");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){

	if($row[cnt] == 1){
		echo "fault";
	}else if($row[cnt] == 0){
		mysqli_query($conn, "INSERT INTO ACCOUNT (NICKNAME, G_MAIL) VALUES ('".$nickname."', '".$email."')");
		echo "success";
	}
}

mysqli_close($conn);

?>
