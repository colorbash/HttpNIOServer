import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;

enum ContentType{text_html, app_js, img_jpg, unknown};

enum HttpMethods{GET, POST, HEAD, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH, ERROR};

public class HttpRequestResponseManager {
    private int code;
    private ContentType contentType;
    private MappedByteBuffer content;
    private String url;
    private HttpMethods reqMethod;
    private String path2files = "files" + File.separator;

    public String contentType2String(){
        switch (contentType) {
            case app_js:{
                return "application/javascript";
            }
            case img_jpg:{
                return "image/jpeg";
            }
            case text_html:{
                return "text/html";
            }
            case unknown:{
                return "undefined";
            }
        }
        return "undefined";
    }

    private void parseRequest(String req){
        String[] reqPars = req.split(" ");// getting parameters of the request
        switch (reqPars[0]){
            case "GET":
                reqMethod = HttpMethods.GET;
                break;
            case "PUT":
                reqMethod = HttpMethods.PUT;
                break;
            case "POST":
                reqMethod = HttpMethods.POST;
                break;
            case "HEAD":
                reqMethod = HttpMethods.HEAD;
                break;
            case "TRACE":
                reqMethod = HttpMethods.TRACE;
                break;
            case "PATCH":
                reqMethod = HttpMethods.PATCH;
                break;
            case "DELETE":
                reqMethod = HttpMethods.DELETE;
                break;
            case "CONNECT":
                reqMethod = HttpMethods.CONNECT;
                break;
            case "OPTIONS":
                reqMethod = HttpMethods.OPTIONS;
                break;
            default:
                reqMethod = HttpMethods.ERROR;
        }
        url = reqPars[1];
        if (url.equals("/")){
            url = "index.html";
        }else {// collect url with proper separator
            String urlParts[] = url.split("/");
            url = "";
            for (int i=0;i<urlParts.length;++i) {
                url = url + urlParts[i] + File.separator;
            }
        }
        contentType = ContentType.unknown;
        if (url.substring(url.length()-5,url.length()).equals(".html")){
            contentType = ContentType.text_html;
        }else if (url.substring(url.length()-3,url.length()).equals(".js")){
            contentType = ContentType.app_js;
        }else if (url.substring(url.length()-4,url.length()).equals(".jpg")){
            contentType = ContentType.img_jpg;
        }
    }

    HttpRequestResponseManager(String request){
        code = 400;
        parseRequest(request);

        if (reqMethod != HttpMethods.GET){
            code = 405; // Current method of request is not GET
            return ;
        }

        try {
            String absUrl = path2files + url;
            FileVoyageur.FileVoyageurResponse fvr = FileVoyageur.findFile(absUrl);
            if (fvr.code == 0) {
                content = fvr.buffer;
                code = 200; // ALL IS OK!
            } else {
                code = 404;
                return;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getResponseCode(){
        return code;
    }

    public MappedByteBuffer getContent(){
        return content;
    }
}
