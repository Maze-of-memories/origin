
<?php

$conn = mysqli_connect("localhost", "root", "ahnlab", "mazeofmemories");

$nickname = $_GET["nickname"];

$result = mysqli_query($conn, "INSERT INTO ACCOUNT (NICKNAME) VALUES (".$nickname.")");

echo mysqli_insert_id($conn); 

mysqli_close($conn);

?>
