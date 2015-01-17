import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

// 미로의 벽을 만드는 클래스
public class WallGenerator {

	private int totalCnt;	// 벽이 만들어 질 수 있는 자리의 개수
	private int usingCnt;	// 사용할 벽의 개수
	private int size;		// 미로의 사이즈
	
	public int vaildMazeCnt;
	
	BufferedWriter bw;
	PrintWriter pw;
	
	public WallGenerator(int size, int usingWallCnt) {
		this.size = size;
		totalCnt = (size * (size-1)) * 2;
		usingCnt = usingWallCnt;
		try {
			pw = new PrintWriter("maze_" + size + "x" + size +  "_" + usingWallCnt + ".maz");
			bw = new BufferedWriter(pw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 벽을 생성한다.(재귀호출 사용)
	public void generateWalls() {
		boolean walls[] = new boolean[totalCnt];

		generateWall(walls, 0, usingCnt, 0);
		
		pw.close();
	}

	// 가능한 모든 조합의 벽을 구한뒤 벽을 만들고 검증한다.
	private void generateWall(boolean walls[], int curPos, int r,
			int usageCount) {
		
		

		// 세운 벽의 수 + 남은 자리가 선택해야 하는 벽의 수 보다 작으면 빠져나간다.
		if (usageCount + walls.length - curPos < r) {
			return;
		}

		// 조합이 완성되었을 때 수행할 작업
		if (usageCount == r) {

//			for (int i = 0; i < walls.length; i++) {
//				if (walls[i])
//					System.out.print("1");
//				else
//					System.out.print('0');
//			}
//			System.out.println("");
			
//			MazeGenerator mg = new MazeGenerator(size, walls);
//
//			pw.println(mg.generateToString());

//			MazeChecker mc = new MazeChecker(mg.generateToArray());
//			if(mc.isVaild()) 
//				 ++vaildMazeCnt;

		} else {
			walls[curPos] = true;
			usageCount++;
			generateWall(walls, curPos + 1, r, usageCount);

			walls[curPos] = false;
			usageCount--;
			generateWall(walls, curPos + 1, r, usageCount);
		}
	}
}
