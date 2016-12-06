import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Writetxt {
    private static final String TAG = "Writetxt";
    private String texpath = null;
    private FileWriter fw;
    private boolean isopen = false;
    public boolean isopen() {
        return isopen;
    }

    public void setIsopen(boolean isopen) {
        this.isopen = isopen;
    }

    public Writetxt(String path){
        this.texpath = path;
    }

    public String getPath() {
        return texpath;
    }

    public void open() {
        String path = texpath + "/lock.txt";
        System.out.println(path);
        File file = new File(path);
        try {
            if(!file.exists()){
                 file.createNewFile();
            }

            fw = new FileWriter(file, true);
            isopen = true;
            } catch (IOException e) {
                e.printStackTrace();
              
            }

    }

    public boolean wirte(String msg){
        if(fw != null){
            try {
                fw.write(msg);
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();    
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public void close(){
        if(fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isopen = false;
        }

    }


    public String getdate(){
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
        Date d = new Date();
        String log = sdf.format(d)+".txt";
        return log;
    }
}
