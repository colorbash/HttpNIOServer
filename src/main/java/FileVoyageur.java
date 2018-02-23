import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class FileVoyageur {

    private static boolean isCaching = false;
    static void setIsCaching(boolean _isCaching){
        isCaching = _isCaching;
    }

    private static Map<String, FileVoyageurResponse> responsesCache = new HashMap();
    static class FileVoyageurResponse{
        String status;
        MappedByteBuffer buffer;
        int code;
        long lastModified;
    }

    public static FileVoyageurResponse findFile(String path) throws IOException{

        FileVoyageurResponse curResponce = responsesCache.get(path);
        if (curResponce == null){
            curResponce = new FileVoyageurResponse();
            if(isCaching){
                responsesCache.put(path, curResponce);
            }
        }

        File file = new File(path);
        if(!file.exists()){
            curResponce.code = 1;
            curResponce.status = "file doesnt exist";
            System.out.print("file doesnt exist ");
            System.out.println(path);
            return curResponce;
        }
        if(file.isDirectory()){
            curResponce.code = 2;
            curResponce.status = "file is directory";
            System.out.print("file is directory ");
            System.out.println(path);
            return curResponce;
        }
        if(!file.canRead()){
            curResponce.code = 3;
            curResponce.status = "file is not readable";
            System.out.print("file is not readable ");
            System.out.println(path);
            return curResponce;
        }
        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(StandardOpenOption.READ))) {
            curResponce.buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
        if(curResponce.lastModified < file.lastModified()){
            try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file.toPath(), EnumSet.of(StandardOpenOption.READ))) {

                curResponce.buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                curResponce.lastModified = file.lastModified();
                curResponce.code = 0;
                curResponce.status = "ok / load from FS";
                System.out.print("ok / load from FS ");
                System.out.println(path);
            }
        } else {
            curResponce.buffer.rewind();
            curResponce.status = "ok / get from cache ";
            System.out.print("ok / get from cache ");
            System.out.println(path);
            curResponce.code = 0;
            return  curResponce;
        }
        return  curResponce;
    }
}
