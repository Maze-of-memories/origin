// 미로를 나타내는  클래스
public class Maze {
	private char[][] map;
	
	public Maze(char[][] map) {
		this.map = map;
	}
	
	// map을 리턴한다.
	public char[][] getMap() {
		return map;
	}
	
	// 미로의 크기를 리턴한다.
	public int getSize() {
		return getMap().length;
	}
	
	// 미로를 화면에 출력한다.
	public void print() {
		System.out.println("");
		
		for(int i = 0; i < map.length + 2; i++) {
			if(i % 2 == 0)
				System.out.print("+ ");
			else
				System.out.print("- ");
		}
		
		System.out.println("");
		
		for(int i = 0; i < map.length; i++) {
			
			if(i %2 == 0)
				System.out.print("| ");
			else
				System.out.print("+ ");
			
			for(int j = 0; j < map[i].length; j++) {
				System.out.print(map[i][j] + " ");
			}
			
			if(i %2 == 0)
				System.out.print("| ");
			else
				System.out.print("+ ");
			
			System.out.println("");
		}
		
		for(int i = 0; i < map.length + 2; i++) {
			if(i % 2 == 0)
				System.out.print("+ ");
			else
				System.out.print("- ");
		}
		
		System.out.println("");
	}
	
	// 미로를 문자열 형태로 반환한다.
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < map.length; i++) {
			builder.append(map[i]);
		}
		
		return builder.toString();
	}
}
