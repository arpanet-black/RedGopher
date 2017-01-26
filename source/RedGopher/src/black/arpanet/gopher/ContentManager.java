package black.arpanet.gopher;

import java.util.Properties;

public abstract class ContentManager extends Thread {
	
	protected volatile boolean alive = true;

	public abstract boolean init(Properties properties);
	
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
	
	

}
