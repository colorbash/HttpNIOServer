import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class HttpSession {

    private SocketChannel sc;
    private Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private int position = 0;

    public HttpSession(SocketChannel channel) {
        this.sc = channel;
    }

    private String readAllBuffer() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        return sb.toString();
    }

    public void sendResponse(HttpRequestResponseManager response) {
        try {
            int respCode = response.getResponseCode();
            writeString("HTTP/1.1 " + respCode);
            writeString("Content-Type: " + response.contentType2String() + "\n");

            if(respCode == 200)
                sc.write(response.getContent());
            else
                writeString(new Integer(respCode).toString());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        try {
            sc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeString(String str) throws IOException {
        sc.write(encoder.encode(CharBuffer.wrap(str + "\n")));
    }

    public String readRequest() throws IOException {
        buffer.limit(buffer.capacity());
        int read = sc.read(buffer);
        if (read < 0)
            return "";
        buffer.flip();
        buffer.position(position);
        return readAllBuffer();
    }
}
