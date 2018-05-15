import java.util.List;

public class Female implements Comparable<Female>{
	private String name;
	private List<Male> preference;
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

	public void setPartner(Male m) {
		partner = m;
	}
	
	public void addPreference (Male m) {
		this.preference.add(m);
	}
	
	@Override
	public int compareTo(Female f) {
		return name.compareTo(f.name);
	}
}
