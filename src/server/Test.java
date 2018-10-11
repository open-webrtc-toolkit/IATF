public class Test {
    public static void main(String args[]){
    	if(args.length <3){
    		System.out.println("please input ip initport controlport");
    	}else{
    		String hostname=args[0];
    		String listenPort_control=args[1];
    		String listenPort_action=args[2];
    		String path=args[3];
    		int port_control=Integer.valueOf(listenPort_control);
    		int port_action=Integer.valueOf(listenPort_action);
    		LockServer ls = new LockServer();
    		ls.start(hostname,port_control,port_action,path);
    	}
    }
}
