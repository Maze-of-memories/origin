import java.util.Stack;

// 주어진 미로에 길이 존재하는지 체크한다.
public class MazeChecker {
	
	private boolean isValid;	/* 미로의 유효성 여부 */
	
	private Point s1, s2, g1, g2;
	public int pathCnt1, pathCnt2;
	public int minPathLength1, minPathLength2;
	
	public MazeChecker(Maze maze) {
		int maxIdx = maze.getSize() - 1;
		
		// 두 플레이어의 시작점과 목표점
		s1 = new Point(maxIdx, 0);
		s2 = new Point(maxIdx, maxIdx);
		
		g1 = new Point(0, maxIdx);
		g2 = new Point(0, 0);
		
		pathCnt1 = numOfPaths(maze, s1, g1);
		pathCnt2 = numOfPaths(maze, s2, g2);

	}
	
	public boolean isVaild() {
		// 두 출발점으로부터 각각의 목적지 까지 최단 경로의 길이가 같다면 
		// 유효한 미로로 설정한다.
		if(pathCnt1 != 0 && pathCnt2 != 0 && pathCnt1 == pathCnt2 && minPathLength1 == minPathLength2)
			isValid = true;
		
		return isValid;
	}

	// start 포인트부터 goal 포인트까지 도달 가능한 경로의 수를 리턴한다.
	private int numOfPaths(Maze maze, Point start, Point goal) {
		
		// 다음으로 갈 위치를 저장
		Stack<Point> nextPoint = new Stack<Point>();
		
		// 방문했던 위치를 순서대로 저장
		Stack<Point> history = new Stack<Point>();
		
		// 갈 수 있는 길이 3개 이상인 곳의 좌표를 저장한다. 
		Stack<Point> basePoint = new Stack<Point>();
		
		char map[][] = maze.getMap();
		
		// 방문했던 위치를 기록할 배열
		boolean[][] isVisited = new boolean[map.length][map.length];
		
		// 출발 좌표 설정
		Point curPt = new Point(start.getRow(), start.getCal());
		
		int pathCnt = 0;
		int minPathLength = 9999;
		
		while(true) {
			// 현재 위치에서 열린 벽의 수를 저장한다.
			// basePoint 여부를 확인하기 위해 사용된다.
			int openedWallCnt = 0;
			
			// 현재 좌표와 goal 좌표가 같으면 길을 찾은 것이므로 길의 개수를 1 증가시킨다.
			if(curPt.equals(goal)) {
				pathCnt++;
				
//				System.out.println("reached : " + pathCnt);
//				System.out.println("getPathLength : " + getPathLength(isVisited));
				
				if(minPathLength >= getPathLength(isVisited))
					minPathLength = getPathLength(isVisited);
				
				// 최단경로 갱신
				if(start.equals(s1))
					minPathLength1 = minPathLength;
				else
					minPathLength2 = minPathLength;
					
				
//				System.out.println("minPathLength : " + minPathLength);
		}
			
			// 위쪽 벽 검사
			if(curPt.getRow() - 1 >= 0 && !isVisited[curPt.getRow() - 2][ curPt.getCal()]) {
				// 위쪽에 벽이 없으면 스택에 위쪽 cell의 좌표를 push한다.
				if(map[curPt.getRow() - 1][curPt.getCal()] == ' ') {
					nextPoint.push(new Point(curPt.getRow() - 2, curPt.getCal()));
					openedWallCnt++;
				}
			}
			
			// 왼쪽 벽 검사
			if(curPt.getCal() - 1 >= 0  && !isVisited[curPt.getRow()][curPt.getCal() - 2]) {
				// 왼쪽에 벽이 없으면 스택에 왼쪽 cell의 좌표를 push한다.
				if(map[curPt.getRow()][curPt.getCal() - 1] == ' ') {
					nextPoint.push(new Point(curPt.getRow(), curPt.getCal() - 2));
					openedWallCnt++;
				}
			}
			
			// 아래쪽 벽 검사
			if(curPt.getRow() + 1 < map.length && !isVisited[curPt.getRow() + 2][curPt.getCal()] ) {
				// 아래쪽에 벽이 없으면 스택에 아래쪽 cell의 좌표를 push한다.
				if(map[curPt.getRow() + 1][curPt.getCal()] == ' ') {
					nextPoint.push(new Point(curPt.getRow() + 2, curPt.getCal()));
					openedWallCnt++;
				}
			}
			
			// 오른쪽 벽 검사
			if(curPt.getCal() + 1 < map.length && !isVisited[curPt.getRow()][curPt.getCal() + 2]) {
				// 오른쪽에 벽이 없으면 스택에 오른쪽 cell의 좌표를 push한다.
				if(map[curPt.getRow()][curPt.getCal() + 1] == ' ') {
					nextPoint.push(new Point(curPt.getRow(), curPt.getCal() + 2));
					openedWallCnt++;
				}
			}
			
			// 다음으로 갈 곳이 비어있으면 더이상 갈 수 있는곳이 없는것이므로 반복문을 빠져나온다.
			if (nextPoint.isEmpty())
				break;
		
			// basepoint인 경우 현재 좌표를 저장한다.
			if(openedWallCnt > 1) {
				// basepoint 저장
				openedWallCnt--;
				
				for(int i = 0; i < openedWallCnt; i++)
					basePoint.push(new Point(curPt.getRow(), curPt.getCal()));
			}
			
			// 현재 위치를 방문했던 위치로 기록하고 
			// 스택에서 좌표를 꺼내 현재 위치로 설정한다.
			history.push(new Point(curPt.getRow(), curPt.getCal()));	/* 방문 했던 경로를 순서대로 저장 */
			isVisited[curPt.getRow()][curPt.getCal()] = true;			/* 방문 체크 */
			curPt = nextPoint.pop();
			
			
			// 사방이 막혔을 경우 지금 까지 방문한 경로에서 basepoint까지의 경로를 지운다.
			if(openedWallCnt == 0) {
				Point basePt = basePoint.pop();
				
				while(!history.peek().equals(basePt)) {
					Point delPt = history.pop();
					isVisited[delPt.getRow()][delPt.getCal()] = false;
				}
			}
		}
		
		return pathCnt;
	}
	
	private int getPathLength(boolean [][] isVisited) {
		int length = 0;
		
		for(int i = 0; i < isVisited.length; i++)
			for(int j = 0; j < isVisited.length; j++)
				if(isVisited[i][j]) 
					length++;
		
		return length;
	}
}
