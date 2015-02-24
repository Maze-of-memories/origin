<?php

// 데이터베이스 연결
include "DBConnect.php";

// $_POST['gmail'] = "s.y.meomories@gmail.com";

// 필드값 체크
if(isset($_POST['gmail']))
{
	$gmail = $_POST['gmail'];

	// 쿼리문 작성
	$query = "SELECT * ";
	$query .= "FROM MULTI_PLAY_INFO ";
	$query .= "WHERE G_MAIL = '$gmail' ";

	// 쿼리 실행
	$result = mysqli_query($conn, $query);

	// 결과 가져오기
	$row = mysqli_fetch_assoc($result);

	// 전적이 존재하지 않으면 전부 0 출력
	if(count($row) == 0)
		echo "0_0_0";
	else
	{
		$winCnt = $row['WIN'];
		$loseCnt = $row["LOSE"];

		if($winCnt == 0 && $losecnt == 0)
			$winRate = "0";
		else
			$winRate = $winCnt / ($winCnt + $loseCnt) * 100;

		// 전적 출력
		echo $winCnt . "_";		// 승
		echo $loseCnt . "_";		// 패
		echo round($winRate, 1);	// 승률
	}
}
else
{
	echo "null";
}

?>