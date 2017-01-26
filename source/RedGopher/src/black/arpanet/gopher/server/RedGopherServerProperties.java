package black.arpanet.gopher.server;

import java.util.concurrent.TimeUnit;

public class RedGopherServerProperties {

	private int corePoolSize;
	private int maxPoolSize;
	private long keepAliveTime;
	private TimeUnit keepAliveUnits;
	private int port;

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public TimeUnit getKeepAliveUnits() {
		return keepAliveUnits;
	}

	public void setKeepAliveUnits(TimeUnit keepAliveUnits) {
		this.keepAliveUnits = keepAliveUnits;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


}
