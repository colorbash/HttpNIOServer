import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class HttpNIOServer implements Runnable {
    private int port;
    private Selector selector;
    private ServerSocketChannel ssc = ServerSocketChannel.open();


    public HttpNIOServer(int _port)throws IOException {
        this.port = _port;
        selector = Selector.open();

        ssc.configureBlocking(false);// for multithreading

        ServerSocket serverSocket = ssc.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        serverSocket.bind(address);

        ssc.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + serverSocket.getLocalPort());
    }

    public void run()  {
        while (true) {
            try{
                selector.select();
                Set selectedKeys = selector.selectedKeys();
                Iterator it = selectedKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();

                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {//Connect
                        SocketChannel client = ssc.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        it.remove();

                    } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {//Read

                        long startTime = System.currentTimeMillis();
                        SocketChannel sc = (SocketChannel) key.channel();
                        HttpSession session = (HttpSession) key.attachment();
                        if (session == null) {
                            session = new HttpSession(sc);
                            key.attach(session);
                        }

                        String request = session.readRequest();
                        if (request.isEmpty()) {
                            session.close();
                            it.remove();
                            continue;
                        }
                        HttpRequestResponseManager response = new HttpRequestResponseManager(request);
                        session.sendResponse(response);
                        session.close();
                        it.remove();
                        System.out.print("time elapsed: ");
                        System.out.println(System.currentTimeMillis() - startTime);
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {

        FileVoyageur.setIsCaching(true);
        //FileVoyageur.setIsCaching(false);
        HttpNIOServer server = new HttpNIOServer(8080);
        server.run();
    }

}
