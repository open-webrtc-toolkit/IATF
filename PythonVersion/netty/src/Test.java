public class Test {
    public static void main(String args[]){
    	String hostname=args[0];
    	String listenPort=args[1];
    	int port=Integer.valueOf(listenPort);
        LockServer ls = new LockServer();
        ls.start(hostname,port);
    }
}
