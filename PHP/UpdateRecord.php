<?php

// 데이터베이스 연결
include "DBConnect.php";

// 테스트 필드
// $_POST['gmail'] = "june0313@gmail.com";
// $_POST['game_result'] = "WIN";

// 필드값 검사
if(isset($_POST['gmail']) && isset($_POST['game_result']))
{
	$gmail = $_POST['gmail'];
	$gameResult = $_POST['game_result'];

	/* 
	승패별 전적 업데이트 쿼리 작성
	*/

	// 승리 1 증가 쿼리
	if(!strcmp($gameResult, "WIN"))
		$query = "UPDATE MULTI_PLAY_INFO SET WIN = WIN + 1 WHERE G_MAIL = '$gmail'";
	
	// 패배 1 증가 쿼리
	if(!strcmp($gameResult, "LOSE"))
		$query = "UPDATE MULTI_PLAY_INFO SET LOSE = LOSE + 1 WHERE G_MAIL = '$gmail'";

	echo $query;

	// 쿼리 실행
	mysqli_query($conn, $query);

}
else
{
	echo "null";
}

?>