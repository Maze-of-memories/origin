import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

// 싱글 플레이용 맵 파일을 생성한다.
public class MakeSingleMaze {

	public static void main(String[] args) {
		int count = 100;
		int size = 5;
		int numOfWalls = 15;
		int minPathLength = 8;
		int maxPathLength = 20;
		
		
		Scanner sc = new Scanner(System.in);
		System.out.print("미로의 사이즈 : ");
		size = sc.nextInt();
		
		System.out.print("벽의 수(최대 " + (size * (size-1)*2) + "개 까지 가능) : " );
		numOfWalls = sc.nextInt();
		
		System.out.print("최단경로(최소 " + ((size-1) * 2) + "이상) : ");
		minPathLength = sc.nextInt();
		
		System.out.print("최대경로(최소 " + ((size-1) * 2) + "이상) : ");
		maxPathLength = sc.nextInt();
		
		System.out.print("생성할 미로의 수 : ");
		count = sc.nextInt();
		
		sc.close();
		
		String fileName = "s" + size + "x" + size + "_" + numOfWalls + "_" + minPathLength + "_" + maxPathLength + "_" + count + ".maz";
		
		HashSet<Maze> mazeSet = new HashSet<Maze>();
		
		System.out.println("\n\nmaz 파일 생성중입니다...");
		long start = System.currentTimeMillis();
		
		while (mazeSet.size() < count) {

			while (true) {
				MazeGenerator mg = new MazeGenerator();
				Maze maze = mg.randomGenerate(size, numOfWalls);
				MazeChecker mc = new MazeChecker();
				mc.checkForSingle(maze);
				
				if (mc.isValidForSingle() &&  minPathLength <= mc.minPathLength1 && mc.minPathLength1 <= maxPathLength) {	
					maze.minPathLength1 = mc.minPathLength1;
					maze.pathCnt1 = mc.pathCnt1;
					// set에 미로를 저장한다.
					mazeSet.add(maze);
					
					
					System.out.println("No. " + (mazeSet.size()));
					maze.print();
					//System.out.println(maze.toString());
					System.out.println("[플레이어 1] 경로 수 : " + maze.pathCnt1
							+ ", 최단경로 : " + maze.minPathLength1);
					System.out.println("유효한 맵!");
					System.out.println("");
					System.out.println("");
					System.out.println("");

					break;
				}
			}
		}
		
		/*// 생성된 미로의 set을 파일에 쓴다.
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// TreeSet을 이용하여 미로를 경로수 내림차순으로 정렬한다.
 		TreeSet<Maze> mazeTreeSet = new TreeSet<Maze>(new Comparator<Maze>() {

			@Override
			public int compare(Maze m1, Maze m2) {
				
				if(m1.pathCnt1 > m2.pathCnt1)
					return -1;
				else
					return 1;
			}
 			
 		});
 		
		mazeTreeSet.addAll(mazeSet);
		
		Iterator<Maze> i = mazeTreeSet.iterator();
		while(i.hasNext()) {
			Maze m = i.next();
			pw.println(m.toString());
		}
		
		pw.close();
		
		System.out.println(fileName + "파일이 생성되었습니다!");
		long end = System.currentTimeMillis();

		System.out.println( "\n경과 시간 : " + ( end - start )/1000.0 );*/

	}

}
