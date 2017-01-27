package black.arpanet.gopher.server;

import static black.arpanet.util.ArpanetLogUtil.e;
import static black.arpanet.util.ArpanetLogUtil.i;
import static black.arpanet.util.ArpanetLogUtil.w;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.apache.bval.jsr303.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import black.arpanet.gopher.db.RedGopherDbManager;

public class RedGopherAdminServer extends Thread {
	
	private static final Logger LOG = LogManager.getLogger(RedGopherAdminServer.class);

	private volatile boolean alive = true;
	private ThreadPoolExecutor tpe;	
	private RedGopherServerProperties props;
	private ServerSocket serverSocket;
	private RedGopherServer gopherServer;
	
	public RedGopherAdminServer(RedGopherServerProperties props, RedGopherServer gopherServer) {
		this.props = props;
		this.gopherServer = gopherServer;
	}
	
	@Override
	public void run() {
		super.run();
		
		try {
			serverSocket = new ServerSocket(props.getPort());
			i(LOG,String.format("RedGopherAdminServer listening on port %s.", props.getPort()));
		} catch(BindException be) {
			//Port already in use
			e(LOG,String.format("Admin port %s already in use!", props.getPort()), be);
			be.printStackTrace();
		} catch (IOException e) {
			e(LOG,String.format("Exception starting admin server on port %s.", props.getPort()), e);
			e.printStackTrace();
		}
		

		while(alive) {
			
			Socket clientSocket = null;

			try {

				clientSocket = serverSocket.accept();
				RedGopherAdminSession gSession = new RedGopherAdminSession(clientSocket);
				tpe.submit(gSession);
				i(LOG,String.format("Session accepted: %s.\nCurrent session count is: %s", clientSocket.toString(), tpe.getActiveCount()));	
				stopDb();
				shutdown();
				break;
				
			} catch(RejectedExecutionException ree) {
				//Admin Session could not be scheduled for execution
				if(clientSocket != null) {
					RedGopherAdminSession.rejectSession(clientSocket);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Shut down gopher server
		gopherServer.shutdown();

		//Shut down the thread pool
		tpe.shutdown();

	}
	
	@Override
	public synchronized void start() {
		initThreadPool();
		super.start();
	}
	
	public void shutdown() {
		w(LOG,"Shutting down Admin server!");
		alive = false;
		IOUtils.closeQuietly(serverSocket);
	}
	
	protected void initThreadPool() {
		tpe = new ThreadPoolExecutor(props.getCorePoolSize(), props.getMaxPoolSize(), props.getKeepAliveTime(),
				props.getKeepAliveUnits(), new ArrayBlockingQueue<Runnable>(props.getMaxPoolSize()));
	}
	

	protected void stopDb() {
		RedGopherDbManager.checkpointDb();
		RedGopherDbManager.shutdownDb();
	}
}
