
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$nickname = $_GET["nickname"];
$email = $_GET["email"];
$max;

$result = mysqli_query($conn, "SELECT MAX(NUMBER) as max FROM ACCOUNT");
$result2 = mysqli_query($conn, "SELECT COUNT(G_MAIL) as cnt FROM ACCOUNT where G_MAIL = '".$email."'");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){
	$max = $row[max]+1;
}
$nickname = $nickname._.$max;

while($row = mysqli_fetch_array($result2, MYSQLI_ASSOC)){

	if($row[cnt] == 0){
		mysqli_query($conn, "INSERT INTO ACCOUNT(NICKNAME, G_MAIL) VALUES ('".$nickname."','".$email."')");
		echo "success";
	}else{
		echo "fail";
	}
}

mysqli_close($conn);

?>