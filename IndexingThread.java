import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Umar on 28-Mar-16.
 */
public class IndexingThread implements  Runnable {

    List<String> paths;
    ConcurrentHashMap<String, List<String>> index;
    ConcurrentHashMap<String, ConcurrentHashMap<String,String>> attribute;
    int start, stop;
    public IndexingThread(List<String> paths, int start, int stop, ConcurrentHashMap<String, List<String>> indexing,ConcurrentHashMap<String, ConcurrentHashMap<String,String>> attribute) {
        this.index=indexing;
        this.paths=paths;
        this.start=start;
        this.stop=stop;
        this.attribute=attribute;
    }

    @Override
    public void run() {
        try {
            doIndexing();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * After Crawling it takes loaded paths and do the index
     * entries in the Index Map
     * @throws IOException
     */
    public void  doIndexing() throws IOException {
        File cnt1;
        for(int i=start;i<stop;i++){
            cnt1 = new File(paths.get(i));
            if (cnt1.isFile()) {
                BasicFileAttributes attr = Files.readAttributes(cnt1.toPath(), BasicFileAttributes.class);
                ConcurrentHashMap<String ,String > fat= new ConcurrentHashMap<>();
                fat.put("created",attr.creationTime().toString());
                fat.put("modified",attr.lastModifiedTime().toString());
                fat.put("sizebytes",attr.size()+"");
                FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(cnt1.toPath(), FileOwnerAttributeView.class);
                UserPrincipal owner = ownerAttributeView.getOwner();

//                FilePermission per=Files.getFileAttributeView(cnt1.toPath(),FilePermission.class);
                fat.put("owner",owner.getName());

//                System.out.println("---------------"+cnt1.getAbsolutePath());

                attribute.put(cnt1.getAbsolutePath(),fat);
                // First store file name and splits on spaces and extension
                List<String> lc;
                String s = cnt1.getName();

                String g = cnt1.getAbsolutePath();
                String dot[] = s.split("\\.(?=[^\\.]+$)");
                lc = index.get(dot[dot.length - 1]);
                if (lc != null) {
                    lc.add(g);
                } else {
                    List<String> kc = Collections.synchronizedList(new ArrayList<String>());
                    kc.add(g);
                    index.put(dot[dot.length - 1], kc);
                }
                String f[];
                if (dot.length>1)
                    f= dot[0].split("\\s+");
                else
                    f=s.split("\\s+");;
                // store space splits names
                storeWords(lc,f,index,g);

                lc = index.get(s);
                if (lc != null) {
                    lc.add(g);
                } else {
                    List<String> kc = Collections.synchronizedList(new ArrayList<String>());
                    kc.add(g);
                    index.put(s, kc);
                }
                //index.put(getExtension(s),g);

                if (getExtension(s).equalsIgnoreCase("txt")) {        // if name matches delete the file
                    //String g=cnt1.getAbsolutePath();
                    FileReader fr = new FileReader(g);
                    BufferedReader tr = new BufferedReader(fr);
                    String sCurrentLine;
                    while ((sCurrentLine = tr.readLine()) != null) {

                        String p[] = sCurrentLine.split("\\s+");
                        storeWords(lc,p,index,g);
                    }
                }

            }
        }
    }
    public void storeWords(List<String > lca,String[] p,ConcurrentHashMap<String, List<String>> index,String g){
        for (int k = 0; k < p.length; k++) {
            lca = index.get(p[k]);
            if (lca != null) {
                lca.add(g);
            } else {
                List<String> kc = Collections.synchronizedList(new ArrayList<String>());
                kc.add(g);
                index.put(p[k], kc);
            }
        }
    }

    /**
     *
     * @param filename Receives the path of file and separate its
     *                 extension (if any) then return extension
     * @return
     */
    public  String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int extensionPos = filename.lastIndexOf('.');
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);

        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }


}
