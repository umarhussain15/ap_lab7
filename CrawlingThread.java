import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Umar on 28-Mar-16.
 */
public class CrawlingThread implements Runnable{

    List<String> results;
    String URL;
    public CrawlingThread(List<String> S,String URL) {
        results=S;
        this.URL=URL;

    }

    @Override
    public void run() {
        crawl_it(URL);
    }
    public void crawl_it(String initialDirectory){
        File fr2 = new File(initialDirectory);
        if (fr2.exists() & fr2.isDirectory()) {                // checking if the file folder exists then search
            System.out.println("Crawling... Please wait");
            //index = new HashMap<>();
            // calling finder to search for name passed in argument and the directory
            try {
                if (finder(fr2)) {
                    System.out.println("Indexing Complete. Enter your key word");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else
            System.out.println("Directory path is corrupt");

    }
    public boolean finder(File cnt) throws IOException {

        File[] names = cnt.listFiles();                // storing all the files in the file array
        File cnt1;
        int flag = 0;
        if (names == null) {                            // if directory is empty return from it
            return false;
        }
        for (int i = 0; i < names.length; i++) {
            cnt1 = names[i];
            if (cnt1.isDirectory()) {
                finder(cnt1);                    // going into inner directory
            }
            if (cnt1.isFile()) {
                //String s = cnt1.getName();

                String g = cnt1.getAbsolutePath();
//                g = g.replace("\\", "/");
                System.out.println(g);
                results.add(g);
                // First store file name and splits on spaces and extension
            }
        }
        return false;
    }

}
