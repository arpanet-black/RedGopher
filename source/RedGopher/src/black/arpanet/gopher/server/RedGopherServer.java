package black.arpanet.gopher.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;
import static black.arpanet.gopher.util.RedGopherLogUtil.e;

public class RedGopherServer extends Thread {

	public static final String TAB = "\t";
	public static final String CRLF = "\r\n";
	
	private static final Logger LOG = LogManager.getLogger(RedGopherServer.class);
	
	private volatile boolean alive = true;
	private ThreadPoolExecutor tpe;
	private RedGopherServerProperties props;
	private ServerSocket serverSocket;

	public RedGopherServer(RedGopherServerProperties props) {
		this.props = props;
	}

	@Override
	public synchronized void start() {
		initThreadPool();
		super.start();
	}

	@Override
	public void run() {
		super.run();		

		try {
			serverSocket = new ServerSocket(props.getPort());
			i(LOG, String.format("RedGopherServer listening on port %s.", props.getPort()));
		} catch(BindException be) {
			//Port already in use
			e(LOG,String.format("Gopher Port %s already in use!", props.getPort()), be);
			be.printStackTrace();
		} catch (IOException e) {
			e(LOG,String.format("Exception starting gopher server on port %s!", props.getPort()), e);
			e.printStackTrace();
		}
				
		while(alive) {
			
			Socket clientSocket = null;

			try {				
				clientSocket = serverSocket.accept();
				
				RedGopherSession gSession = new RedGopherSession(clientSocket);
				tpe.submit(gSession);

				i(LOG, String.format("Session accepted: %s.\nCurrent session count is: %s", clientSocket.toString(), tpe.getActiveCount()));

			} catch(RejectedExecutionException ree) {
				//Gopher Session could not be scheduled for execution
				if(clientSocket != null) {
					RedGopherSession.rejectSession(clientSocket);
				} else {
					ree.printStackTrace();
				}
			} catch(SocketException se) {
				
				if(serverSocket == null || serverSocket.isClosed()) {
					i(LOG, "Gopher Server socket closed.");
					break;
				} else {
					se.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//Shut down the thread pool
		tpe.shutdown();

	}

	public void shutdown() {
		w(LOG, "Shutting down Gopher server.");
		alive = false;
		IOUtils.closeQuietly(serverSocket);		
	}

	protected void initThreadPool() {
		tpe = new ThreadPoolExecutor(props.getCorePoolSize(), props.getMaxPoolSize(), props.getKeepAliveTime(),
				props.getKeepAliveUnits(), new ArrayBlockingQueue<Runnable>(props.getMaxPoolSize()));
	}

}
