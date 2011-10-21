import java.util.Hashtable;

public class Hashstorage {
  private Hashtable<String,String> thishash = new Hashtable<String,String>();

  public String get(String key) {
    if(thishash.containsKey(key)) { return thishash.get(key); }
    else { return ""; }
  }

  public void set(String key, String value) {
    thishash.put(key,value);
  }

  public boolean has(String key) {
    return thishash.containsKey(key);
  }

  public Integer getAsInt(String key) {
    if(thishash.containsKey(key)) {
      try { return Integer.parseInt(thishash.get(key)); }
      catch (NumberFormatException e) { return null; }
    }
    return null;
  }

  public void setFromInt(String key, Integer value) {
    thishash.put(key,value.toString());
  }

  public void increment(String key) {
    if(thishash.containsKey(key)) {
      String value = thishash.get(key);
      Integer test;
      try {
	test = Integer.parseInt(value);
	test++;
	thishash.put(key,test.toString());
      }
      catch (NumberFormatException e) { }
    }
  }
}