package pojo;

public class MyFile {

	private String name;
	private long size;
	private String path;

	public MyFile() {
	}

	public MyFile(String name, long size, String path) {
		this.name = name;
		this.size = size;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public String getPath() {
		return path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
