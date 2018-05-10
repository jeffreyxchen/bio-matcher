import java.util.List;

public class Female implements Comparable<Female>{
	private String name;
	List<Male> preference;
	private Male partner;
	
	public Female(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Male> getPreference() {
		return preference;
	}
	
	public Male getPartner() {
		return partner;
	}
	
	public boolean isFree() {
		return partner == null;
	}
	
	public void setPreference(List<Male> pref) {
		preference = pref;
	}
	
	public void setPartner(Male f) {
		partner = f;
	}
	
	@Override
	public int compareTo(Female o) {
		return name.compareTo(o.name);
	}
}
