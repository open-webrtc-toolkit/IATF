
import java.util.ArrayList;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.sun.xml.internal.messaging.saaj.util.LogDomainConstants;

public class LockServer {
    private SocketIOServer server_action;
    private SocketIOServer server_control;
    private LockObject curLock_action = new LockObject("Init_action_Lock");
    private LockObject curLock_control = new LockObject("Init_control_Lock");
    private Writetxt writetxt = null;
    private int resendInterval = 500;
    boolean resendFlag = true;
    int maxsendtimes = 4;
    int actionlocksendtimes = 0;
    int controllocksendtimes = 0;
    ArrayList<LockObject> actionlocksend = new ArrayList<LockObject>();
    ArrayList<LockObject> controllocksend = new ArrayList<LockObject>();
    private String shellPath = "/bin/sh";

    public void start(String hostname,int port_control,int port_action,String path) {
    		writetxt = new Writetxt(path);
    		writetxt.open();
    		actionlocksend.add(curLock_action);
    		controllocksend.add(curLock_control);
            Configuration config_action = new Configuration();
            config_action.setHostname(hostname);
            config_action.setPort(port_action);
            server_action = new SocketIOServer(config_action);

            Configuration config_control = new Configuration();
            config_control.setHostname(hostname);
            config_control.setPort(port_control);
            server_control = new SocketIOServer(config_control);
       
            server_action.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                LockObject data = new LockObject("server received connect!");
                System.out.println("server recievd client connect!");
                client.sendEvent("lockevent", data);
            }
        });
            server_action.addEventListener("lockevent", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
            	System.out.println("lockevent by action"+data.getLock());
            	actionlocksend.add(data);
            	curLock_action = data;
            	server_action.getBroadcastOperations().sendEvent("lockevent", data);
            	writetxt.wirte("action lockevent: "+data.getLock()+"\n");
            }
        });
            server_action.addEventListener("waitlock", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
            	server_action.getBroadcastOperations().sendEvent("waitlock", data);
            }
        });

            server_control.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                LockObject data = new LockObject("server received connect!");
                System.out.println("server recievd client connect!");
                client.sendEvent("lockevent", data);
            }
        });
            server_control.addEventListener("controlevent", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
                // broadcast messages to all clients
                System.out.println("lockevent by control"+data.getLock());
                controllocksend.add(data);
                curLock_control = data;
                server_control.getBroadcastOperations().sendEvent("controlevent", data);
                writetxt.wirte("control lockevent: "+data.getLock()+"\n");
                if(data.getLock().equals("endtest")){
                	writetxt.wirte("==========================\n\n");
                }
               
            }
        });

            server_control.addEventListener("waitlock", LockObject.class, new DataListener<LockObject>() {
            @Override
            public void onData(SocketIOClient client, LockObject data, AckRequest ackRequest) {
            	server_control.getBroadcastOperations().sendEvent("waitlock", data);
            }
        });

        
            server_control.addDisconnectListener(new DisconnectListener() {
			
			public void onDisconnect(SocketIOClient client) {
				// TODO Auto-generated method stub
				LockObject data = new LockObject("server received connect!");
                System.out.println("server recievd client connect!");
                client.sendEvent("lockevent", data);
   
			}
        });

        server_action.start();
        server_control.start();
        initResendThead();
        initStartTestThead();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
        	server_action.stop();
        	server_control.stop();
        }
    }

    public void initResendThead() {
        new Thread() {
            public void run() {
                resendFlag = true;
                while (resendFlag) {
                    try {
                        currentThread();
                        Thread.sleep(resendInterval);
                        //System.out.println("actionlocksend size is :"+ actionlocksend.size());
                        if(actionlocksend.size() > 1){
                        	actionlocksendtimes++;
                        	System.out.println("resend  action lock:"+actionlocksend.get(1).getLock());
                       	 	server_action.getBroadcastOperations().sendEvent("waitactionlock", actionlocksend.get(1));
                       	 	if(actionlocksendtimes > maxsendtimes){
                       	 		actionlocksend.remove(0);
                       	 		actionlocksendtimes = 0;
                       	 	}
                        }else{
                        	 System.out.println("resend action lock:"+actionlocksend.get(0).getLock());
                        	 server_action.getBroadcastOperations().sendEvent("waitactionlock", actionlocksend.get(0));
                        }
                        
                    } catch (Exception e) {
                        resendFlag = false;
                        System.out.println("resendFlag: set false" + e.getMessage());
                    }
                }
            }
        }.start();
    }


    public void initStartTestThead() {
        new Thread() {
            public void run() {
                resendFlag = true;
                while (resendFlag) {
                    try {
                        currentThread();
                        Thread.sleep(resendInterval);
                        //System.out.println("controllocksend size is :"+ controllocksend.size());
                        if(controllocksend.size() >1){
                        	controllocksendtimes++;
                        	System.out.println("resend control lock:"+controllocksend.get(1).getLock());
                        	server_control.getBroadcastOperations().sendEvent("waitcontrollock", controllocksend.get(1));
                       	 	if(controllocksendtimes > maxsendtimes){
                       	 		controllocksend.remove(0);
                       	 		controllocksendtimes = 0;
                       	 	}
                        }else{
                        	 System.out.println("resend control lock:"+controllocksend.get(0).getLock());
                        	 server_control.getBroadcastOperations().sendEvent("waitcontrollock", controllocksend.get(0));
                        }
              
                    } catch (Exception e) {
                        resendFlag = false;
                        System.out.println("resendFlag: set false" + e.getMessage());
                    }
                }
            }
        }.start();
    }

}
