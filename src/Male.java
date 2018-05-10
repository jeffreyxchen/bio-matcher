import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Male implements Comparable<Male>{
	private String name;
	List<Female> preference;
	private Female partner;
	private Set<Female> proposed;
	
	public Male(String name) {
		this.name = name;
		proposed = new HashSet<Female>();
	}
	
	public String getName() {
		return name;
	}
	
	public List<Female> getPreference() {
		return preference;
	}
	
	public Set<Female> getProposed() {
		return proposed;
	}
	
	public Female getPartner() {
		return partner;
	}
	
	public boolean isFree() {
		return partner == null;
	}
	
	public void setPreference(List<Female> pref) {
		preference = pref;
	}
	
	public void setProposed(Set<Female> prop) {
		proposed = prop;
	}
	
	public void setPartner(Female f) {
		partner = f;
	}

	@Override
	public int compareTo(Male o) {
		return name.compareTo(o.name);
	}
}
