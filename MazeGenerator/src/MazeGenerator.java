import java.util.Random;


// 기억의 미로 생성기
public class MazeGenerator {
	
	private static final int NUM_OF_WALLS = 20;
	
	private int realSize;
	private char[][] maze;
	
	// 생성자 : 미로를 초기화 한다.
	public MazeGenerator(int size) {
		this.realSize = size * 2 -1;
		maze = new char[realSize][realSize];
		
		for(int i = 0; i < maze.length; i++) {
			for(int j = 0; j < maze[i].length; j++) {
				maze[i][j] = ' ';
			}
		}
		
		// 시작지점 표시
		maze[realSize-1][0] = 'Y';
		maze[realSize-1][realSize-1] = 'S';
		
		// 목표지점 표시
		maze[0][0] = 'S';
		maze[0][realSize-1] = 'Y';
		
		// 기둥 생성
		setupCol();
		
		// 벽 생성
		setupWall();
	}
	
	// 기둥을 생성한다.
	private void setupCol() {
		for(int i = 1; i < maze.length; i += 2) {
			for(int j = 1; j < maze[i].length; j += 2) {
				maze[i][j] = '+';
			}
		}
	}
	
	// 랜덤하게 벽을 생성한다.
	// 벽이 위치할 수 있는 좌표는 (홀수, 짝수) 또는 (짝수, 홀수) 위치이다.
	private void setupWall() {
		
		int walls[] = new int[NUM_OF_WALLS];
		
		/*int row;
		int col;
		int settedWallCnt = 0;

		Random random = new Random();
		
		while(settedWallCnt < NUM_OF_WALLS) {
			// 벽이 세워질 행을 랜덤하게 선택한다.
			row = random.nextInt(realSize);
			
			// 행이 짝수인 경우 홀수 열을 랜덤하게 선택한다.
			if(row % 2 == 0) {
				col = random.nextInt(realSize/2-1) * 2 + 1;
				
				if (maze[row][col] == ' ') {
					maze[row][col] = '|';
					settedWallCnt++;
				}
			}
			else {
				col = random.nextInt(realSize/2) * 2;
				
				if (maze[row][col] == ' ') {
					maze[row][col] = '-';
					settedWallCnt++;
				}
			}			
		}*/
		
		/*for(int i = 0; i < maze.length; i++) {
			
			if(i % 2 == 0) {
				// 세로벽 생성
				for(int j = 1; j < maze[i].length; j += 2) {
					maze[i][j] = '|';
				}
			}
			else {
				// 가로벽 생성
				for(int j = 0; j < maze[i].length; j += 2) {
					maze[i][j] = '-';
				}
			}
		}*/
	}
	
	public void wallCombination() {
		int walls[] = new int[NUM_OF_WALLS];
		
		generateCombination(walls, 40, NUM_OF_WALLS);
	}
	
	public void generateCombination(int walls[], int total, int r) {
		for(int i = 0; i < total; i++) {
			
		}
	}
		
	
	// 미로를 보기 좋게 출력한다.
	public void printMaze() {
		
		for(int i = 0; i < maze.length; i++) {
			for(int j = 0; j < maze[i].length; j++) {
				System.out.print(maze[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	// 미로를 String 타입으로 리턴한다.
	public String getMaze() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < maze.length; i++) {
			builder.append(maze[i]);
		}
		
		return builder.toString();
	}
}
