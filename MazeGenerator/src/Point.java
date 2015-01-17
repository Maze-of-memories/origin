
// 좌표를 나타내는 클래스
public class Point {
	private int row;
	private int cal;
	
	public Point() {
		this(0, 0);
	}
	
	public Point(int row, int cal) {
		this.row = row;
		this.cal = cal;
	}
	
	public void setPoint(int row, int cal) {
		this.row = row;
		this.cal = cal;
	}
	
	public void setRow(int row) {
		this.row = row;
	}
	
	public void setCal(int cal) {
		this.cal = cal;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCal() {
		return cal;
	}
	
	public boolean equals(Point p) {
		return (this.row == p.getRow() && this.cal == p.getCal());
	}
	
	public void copy(Point p) {
		this.row = p.getRow();
		this.cal = p.getCal();
	}
}
