
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$email = $_GET["email"];

$result = mysqli_query($conn, "SELECT COUNT(G_MAIL) as cnt FROM ACCOUNT where G_MAIL = '".$email."'");

while($row = mysqli_fetch_array($result, MYSQLI_ASSOC)){

	if($row[cnt] == 1){
		echo "exist";
	}else if($row[cnt] == 0){
		echo "not exist";
	}
}

mysqli_close($conn);

?>
