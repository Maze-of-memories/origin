<?php
// 4.1.0 이전의 PHP에서는, $_FILES 대신에 $HTTP_POST_FILES를
// 사용해야 합니다.
// test

$db_connect = mysql_connect('localhost', 'root', 'ahnlab');
mysql_select_db('mazeofmemories');


$uploaddir = '/root/mom/maps/';
$uploadfile = $uploaddir . basename($_FILES['userfile']['name']);

echo '<pre>';
if (move_uploaded_file($_FILES['userfile']['tmp_name'], $uploadfile)) {
	echo "Success.\n";
	$fp = fopen($uploadfile, "r");
	
	while(!feof($fp)) {
		// 파일에서 한줄씩 읽어 DB에 저장한다.
		$line = trim(fgets($fp));

		$query = "INSERT INTO MULTI_MAP_5 (MAP_INFO) VALUES ('$line')";
		if(strlen($line) > 0)
		$result = mysql_query($query);	
		
		// echo $line;
	}
	
	fclose($fp);
} else {
	    print "파일 업로드 공격의 가능성이 있습니다!\n";
}

echo '자세한 디버깅 정보입니다:';
print_r($_FILES);

print "</pre>";

?>
