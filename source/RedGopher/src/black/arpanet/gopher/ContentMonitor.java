package black.arpanet.gopher;

import java.util.Map;

public abstract class ContentMonitor extends Thread {
	
	protected volatile boolean alive = true;
	protected Map<String,String> params = null;

	public abstract boolean init(Map<String,Object> config);
	
	public abstract boolean loadContent();
	
	public abstract boolean update();
	
	public void shutdown() {
		this.alive = false;
	}

	@Override
	public void run() {
		super.run();
		
		while(alive) {
			alive = update();
		}
		
	}
	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

}
