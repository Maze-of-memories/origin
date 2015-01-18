import java.util.Random;

// 기억의 미로 생성기
public class MazeGenerator {

	// 주어진 벽 조합을 이용하여 미로를 생성한다.
	public Maze generateByWalls(int size, boolean walls[] ) {
		
		int realSize = size * 2 -1;
		
		// map 생성 후 초기화 한다.
		char[][] map = new char[realSize][realSize];
		initMap(map);
		
		int pos = 0;
		
		for(int i = 0; i < map.length; i++) {
			
			if (i % 2 == 0) {
				// 홀수 행에서는 세로벽 생성
				for (int j = 1; j < map[i].length; j += 2) {
					if(walls[pos++])
						map[i][j] = '|';
					else
						map[i][j] = ' ';
				}
			} else {
				// 짝수 행에서는 가로벽 생성
				for (int j = 0; j < map[i].length; j += 2) {
					if(walls[pos++])
						map[i][j] = '-';
					else
						map[i][j] = ' ';
				}
			}
		}
		
		// 생성된 map으로 Maze를 만든다.
		Maze maze = new Maze(map);
		
		return maze;
	}
	

	// 주어진 벽 없이 랜덤으로 미로를 생성하여 리턴한다.
	// size : 미로의 크기
	// wallCnt : 생성할 벽의 개수
	public Maze randomGenerate(int size, int wallCnt) {

		int realSize = size * 2 -1;
		
		// map 생성 후 초기화 한다.
		char[][] map = new char[realSize][realSize];
		initMap(map);
		
		// row and column
		int r, c;
		char walChar;
		Random ran = new Random();
		
		// 랜덤하게 벽을 표시하여 map을 생성한다.
		while (wallCnt != 0) {
			r = ran.nextInt(realSize);
			if (r % 2 == 0) {
				walChar = '-';
				c = ran.nextInt(size - 1) * 2 + 1;
			}
			else {
				walChar = '|';
				c = ran.nextInt(size) * 2;
			}

			if (map[c][r] != walChar) {
				map[c][r] = walChar;
				wallCnt--;
			}
		}
		
		// 생성된 map으로 Maze를 만든다.
		Maze maze = new Maze(map);
		
		return maze;
	}
	
	// 맵을 초기화 한다.
	// 길과 기둥을 표시한다.
	private void initMap(char[][] map) {

		// 길을 표시한다.
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = ' ';
			}
		}
		
		// 기둥을 표시한다.
		for(int i = 1; i < map.length; i += 2) {
			for(int j = 1; j < map[i].length; j += 2) {
				map[i][j] = '+';
			}
		}

//		// 시작지점 표시
//		map[map.length - 1][0] = 'A';
//		map[map.length - 1][map.length - 1] = 'B';
//
//		// 목표지점 표시
//		map[0][map.length - 1] = 'A';
//		map[0][0] = 'B';
	}
}
