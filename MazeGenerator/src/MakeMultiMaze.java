import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

// 테스트용 main메서드가 있는 클래스
public class MakeMultiMaze {

	public static void main(String[] args) {
		
		
		int count = 100;
		int size = 5;
		int numOfWalls = 15;
		int minPathLength = 10;
		
		
		Scanner sc = new Scanner(System.in);
		System.out.print("미로의 사이즈 : ");
		size = sc.nextInt();
		
		System.out.print("벽의 수(최대 " + (size * (size-1)*2) + "개 까지 가능) : " );
		numOfWalls = sc.nextInt();
		
		System.out.print("생성할 미로의 수 : ");
		count = sc.nextInt();
		
		String fileName = size + "x" + size + "_" + numOfWalls + "_" + count + ".maz";
		
		HashSet<String> mazeSet = new HashSet<String>();
		
		System.out.println("\n\nmaz 파일 생성중입니다...");
		long start = System.currentTimeMillis();
		
		while (mazeSet.size() < count) {

			while (true) {
				MazeGenerator mg = new MazeGenerator();
				Maze maze = mg.randomGenerate(size, numOfWalls);
				MazeChecker mc = new MazeChecker();
				mc.checkForMulti(maze);
				if (mc.isValidForMulti()) {	
					// set에 미로를 저장한다.
					mazeSet.add(maze.toString());
					
					
//					System.out.println("No. " + (mazeSet.size()));
//					maze.print();
//					System.out.println(maze.toString());
//					System.out.println("[플레이어 1] 경로 수 : " + mc.pathCnt1
//							+ ", 최단경로 : " + mc.minPathLength1);
//					System.out.println("[플레이어 2] 경로 수 : " + mc.pathCnt2
//							+ ", 최단경로 : " + mc.minPathLength2);
//					System.out.println("유효한 맵!");
//					System.out.println("");
//					System.out.println("");
//					System.out.println("");

					break;
				}
			}
		}
		
		// 생성된 미로의 set을 파일에 쓴다.
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		
		Iterator<String> i = mazeSet.iterator();
		while(i.hasNext()) {
			pw.println(i.next());
		}
		
		pw.close();
		
		System.out.println(fileName + "파일이 생성되었습니다!");
		long end = System.currentTimeMillis();

		System.out.println( "\n경과 시간 : " + ( end - start )/1000.0 );
	}
}
